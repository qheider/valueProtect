package info.quazi.valueProtect.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PaymentEmailService {

    private static final Logger log = LoggerFactory.getLogger(PaymentEmailService.class);
    private final JavaMailSender mailSender;

    @Value("${app.email.from:no-reply@valueprotect.com}")
    private String fromEmail;

    public PaymentEmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSender = mailSenderProvider.getIfAvailable();
    }

    public void sendPaymentLink(String tenantEmail, String appraisalId, String checkoutUrl) {
        if (mailSender == null) {
            log.warn("JavaMailSender is not configured. Payment link not emailed. appraisalId={}, tenantEmail={}, checkoutUrl={}",
                    appraisalId,
                    tenantEmail,
                    checkoutUrl);
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(tenantEmail);
        message.setSubject("Complete your appraisal payment");
        message.setText(buildPaymentBody(appraisalId, checkoutUrl));

        mailSender.send(message);
        log.info("Payment link email sent. appraisalId={}, tenantEmail={}", appraisalId, tenantEmail);
    }

    private String buildPaymentBody(String appraisalId, String checkoutUrl) {
        return "Your appraisal is ready for payment.\n\n"
                + "Appraisal ID: " + appraisalId + "\n"
                + "Payment link: " + checkoutUrl + "\n\n"
                + "If you did not request this payment, please contact support.";
    }
}
