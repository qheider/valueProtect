package info.quazi.valueProtect.controller;

import info.quazi.valueProtect.dto.CreatePaymentSessionRequest;
import info.quazi.valueProtect.dto.PaymentSessionResponse;
import info.quazi.valueProtect.service.StripePaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "Stripe Payments", description = "Stripe checkout session and webhook endpoints")
public class StripePaymentController {

    private final StripePaymentService stripePaymentService;

    public StripePaymentController(StripePaymentService stripePaymentService) {
        this.stripePaymentService = stripePaymentService;
    }

    @PostMapping("/appraisals/{appraisalId}/payment-session")
    @Operation(
            summary = "Create Stripe payment session",
            description = "Creates Stripe Checkout session for an appraisal and sends payment link email when tenant email is provided."
    )
    @ApiResponse(responseCode = "201", description = "Checkout session created")
    @ApiResponse(responseCode = "400", description = "Invalid input")
    @ApiResponse(responseCode = "401", description = "Unauthorized")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN') or hasRole('LENDER')")
    public ResponseEntity<PaymentSessionResponse> createPaymentSession(
            @PathVariable @Parameter(description = "Appraisal ID") String appraisalId,
            @Valid @RequestBody CreatePaymentSessionRequest request) {

        PaymentSessionResponse response = stripePaymentService.createCheckoutSession(appraisalId, request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/webhooks/stripe")
    @Operation(
            summary = "Stripe webhook",
            description = "Receives Stripe webhook events, verifies signature, updates appraisal status, and triggers workflow transition."
    )
    @ApiResponse(responseCode = "200", description = "Webhook processed")
    @ApiResponse(responseCode = "400", description = "Invalid webhook payload")
    public ResponseEntity<Map<String, String>> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader(value = "Stripe-Signature", required = false) String signatureHeader) {

        stripePaymentService.handleWebhook(payload, signatureHeader);
        return ResponseEntity.ok(Map.of("status", "processed"));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException e) {
        return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(SecurityException e) {
        return new ResponseEntity<>(Map.of("error", e.getMessage()), HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntime(RuntimeException e) {
        return new ResponseEntity<>(Map.of("error", "Stripe processing failed"), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
