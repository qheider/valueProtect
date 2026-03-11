package info.quazi.valueProtect.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import info.quazi.valueProtect.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Service
public class PdfProcessingService {

    private static final Logger log = LoggerFactory.getLogger(PdfProcessingService.class);

    private final JwtUtil jwtUtil;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    @Value("${pdf.processing.api.url:http://localhost:8000/process-pdf}")
    private String pdfProcessingApiUrl;

    @Value("${pdf.processing.api.enabled:true}")
    private boolean pdfProcessingEnabled;

    @Value("${pdf.processing.api.timeout:60000}") // Default 60 seconds
    private long apiTimeout;

    public PdfProcessingService(JwtUtil jwtUtil, ObjectMapper objectMapper) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(10000))
                .build();
    }

    /**
     * Asynchronously process PDF document by calling external API
     * 
     * @param pdfUrl URL to the PDF file
     * @param appraisalId Appraisal ID for tracking
     */
    @Async
    public void processPdfDocument(String pdfUrl, String appraisalId) {
        if (!pdfProcessingEnabled) {
            log.info("PDF processing is disabled. Skipping processing for appraisal: {}", appraisalId);
            return;
        }

        try {
            log.info("Starting PDF processing for appraisal: {}, PDF URL: {}", appraisalId, pdfUrl);

            // Generate JWT token for authentication
            String jwtToken = jwtUtil.generatePdfProcessingToken();
            log.debug("Generated JWT token (first 20 chars): {}...", jwtToken.substring(0, Math.min(20, jwtToken.length())));

            // Prepare request payload
            Map<String, String> requestBody = new HashMap<>();
            requestBody.put("pdf_url", pdfUrl);
            requestBody.put("prompt", "Summarize the document and extract structured data");
            requestBody.put("output_basename", "appraisal-" + appraisalId);

            String jsonBody = objectMapper.writeValueAsString(requestBody);
            log.debug("Request payload: {}", jsonBody);

            // Build HTTP request
            log.debug("Sending request to: {}", pdfProcessingApiUrl);
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(pdfProcessingApiUrl))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + jwtToken)
                    .timeout(Duration.ofMillis(apiTimeout))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            // Send request
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                log.info("PDF processing successful for appraisal: {}. Response: {}", 
                        appraisalId, response.body());
            } else {
                log.warn("PDF processing API returned non-success status for appraisal: {}. Status: {}, Response: {}", 
                        appraisalId, response.statusCode(), response.body());
            }

        } catch (Exception e) {
            log.error("Failed to process PDF for appraisal: {}. Error: {}", 
                    appraisalId, e.getMessage(), e);
            // Don't throw exception - this is async and we don't want to fail the main upload process
        }
    }

    /**
     * Set PDF processing enabled/disabled (for testing)
     */
    public void setPdfProcessingEnabled(boolean enabled) {
        this.pdfProcessingEnabled = enabled;
    }
}
