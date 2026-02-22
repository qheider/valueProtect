package info.quazi.valueProtect.service;

import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.exception.StripeException;
import com.stripe.model.Event;
import com.stripe.model.StripeObject;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;
import info.quazi.valueProtect.dto.AppraisalDto;
import info.quazi.valueProtect.dto.CreatePaymentSessionRequest;
import info.quazi.valueProtect.dto.PaymentSessionResponse;
import info.quazi.valueProtect.entity.Appraisal;
import info.quazi.valueProtect.entity.StripeWebhookEvent;
import info.quazi.valueProtect.repository.AppraisalRepository;
import info.quazi.valueProtect.repository.StripeWebhookEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StripePaymentService {

    private static final Logger log = LoggerFactory.getLogger(StripePaymentService.class);

    private final AppraisalService appraisalService;
    private final AppraisalRepository appraisalRepository;
    private final StripeWebhookEventRepository stripeWebhookEventRepository;
    private final PaymentEmailService paymentEmailService;
    private final WorkflowEngineService workflowEngineService;

    @Value("${stripe.api.key:}")
    private String stripeApiKey;

    @Value("${stripe.webhook.secret:}")
    private String stripeWebhookSecret;

    @Value("${app.payment.default-fee-cents:49900}")
    private int defaultFeeCents;

    @Value("${app.payment.currency:usd}")
    private String currency;

    public StripePaymentService(
            AppraisalService appraisalService,
            AppraisalRepository appraisalRepository,
            StripeWebhookEventRepository stripeWebhookEventRepository,
            PaymentEmailService paymentEmailService,
            WorkflowEngineService workflowEngineService) {
        this.appraisalService = appraisalService;
        this.appraisalRepository = appraisalRepository;
        this.stripeWebhookEventRepository = stripeWebhookEventRepository;
        this.paymentEmailService = paymentEmailService;
        this.workflowEngineService = workflowEngineService;
    }

    public PaymentSessionResponse createCheckoutSession(String appraisalId, CreatePaymentSessionRequest request) {
        validateStripeApiKey();

        AppraisalDto appraisal = appraisalService.getAppraisal(appraisalId);
        int amount = resolveAmount(request.getAmountCents());

        Stripe.apiKey = stripeApiKey;

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(request.getSuccessUrl())
                .setCancelUrl(request.getCancelUrl())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency(currency)
                                                .setUnitAmount((long) amount)
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName("Appraisal Service Fee")
                                                                .setDescription("Appraisal ID: " + appraisalId)
                                                                .build())
                                                .build())
                                .build())
                .putMetadata("appraisalId", appraisalId)
                .putMetadata("workflowKey", "APPRAISAL_PAYMENT")
                        .setPaymentIntentData(
                            SessionCreateParams.PaymentIntentData.builder()
                                .putMetadata("appraisalId", appraisalId)
                                .putMetadata("workflowKey", "APPRAISAL_PAYMENT")
                                .build());

        if (request.getTenantEmail() != null && !request.getTenantEmail().isBlank()) {
            paramsBuilder.setCustomerEmail(request.getTenantEmail());
        }

        try {
            Session session = Session.create(paramsBuilder.build());

            PaymentSessionResponse response = new PaymentSessionResponse();
            response.setAppraisalId(appraisal.getAppraisalId());
            response.setCheckoutSessionId(session.getId());
            response.setCheckoutUrl(session.getUrl());
            response.setPaymentStatus("UNPAID");

            if (request.getTenantEmail() != null && !request.getTenantEmail().isBlank()) {
                paymentEmailService.sendPaymentLink(request.getTenantEmail(), appraisalId, session.getUrl());
            }

            return response;
        } catch (StripeException e) {
            throw new RuntimeException("Failed to create Stripe checkout session", e);
        }
    }

    @Transactional
    public void handleWebhook(String payload, String signatureHeader) {
        validateWebhookConfiguration(signatureHeader);

        Event event;
        try {
            event = Webhook.constructEvent(payload, signatureHeader, stripeWebhookSecret);
        } catch (SignatureVerificationException e) {
            throw new SecurityException("Invalid Stripe webhook signature");
        }

        if (stripeWebhookEventRepository.existsById(event.getId())) {
            log.info("Ignoring duplicate Stripe event: {}", event.getId());
            return;
        }

        String appraisalId = null;

        switch (event.getType()) {
            case "checkout.session.completed" -> appraisalId = handleCheckoutSessionCompleted(event);
            case "payment_intent.succeeded" -> appraisalId = handlePaymentIntentSucceeded(event);
            case "payment_intent.payment_failed" -> log.warn("Stripe payment failed event received: {}", event.getId());
            case "charge.refunded" -> log.info("Stripe refund event received: {}", event.getId());
            default -> log.debug("Unhandled Stripe event type: {}", event.getType());
        }

        stripeWebhookEventRepository.save(new StripeWebhookEvent(event.getId(), event.getType(), appraisalId));
    }

    private String handleCheckoutSessionCompleted(Event event) {
        StripeObject stripeObject = extractStripeObject(event);
        if (!(stripeObject instanceof Session session)) {
            return null;
        }

        String appraisalId = session.getMetadata() != null ? session.getMetadata().get("appraisalId") : null;
        if (appraisalId == null || appraisalId.isBlank()) {
            log.warn("checkout.session.completed missing appraisalId metadata for event {}", event.getId());
            return null;
        }

        markPaidAndQueue(appraisalId);
        return appraisalId;
    }

    private String handlePaymentIntentSucceeded(Event event) {
        StripeObject stripeObject = extractStripeObject(event);
        if (!(stripeObject instanceof com.stripe.model.PaymentIntent paymentIntent)) {
            return null;
        }

        String appraisalId = paymentIntent.getMetadata() != null ? paymentIntent.getMetadata().get("appraisalId") : null;
        if (appraisalId == null || appraisalId.isBlank()) {
            log.debug("payment_intent.succeeded missing appraisalId metadata for event {}", event.getId());
            return null;
        }

        markPaidAndQueue(appraisalId);
        return appraisalId;
    }

    private StripeObject extractStripeObject(Event event) {
        return event.getDataObjectDeserializer().getObject().orElse(null);
    }

    private void markPaidAndQueue(String appraisalId) {
        Appraisal appraisal = appraisalRepository.findByAppraisalId(appraisalId)
                .orElseThrow(() -> new IllegalArgumentException("Appraisal not found for payment confirmation: " + appraisalId));

        if (appraisal.getStatus() != Appraisal.AppraisalStatus.REVIEW) {
            appraisal.setStatus(Appraisal.AppraisalStatus.REVIEW);
            appraisalRepository.save(appraisal);
            log.info("Payment confirmed for appraisal {}, status moved to REVIEW", appraisalId);
        } else {
            log.info("Payment confirmed for appraisal {}, status already REVIEW", appraisalId);
        }

        workflowEngineService.moveToAppraiserQueue(appraisalId);
    }

    private int resolveAmount(Integer requestAmountCents) {
        if (requestAmountCents != null && requestAmountCents > 0) {
            return requestAmountCents;
        }
        return defaultFeeCents;
    }

    private void validateStripeApiKey() {
        if (stripeApiKey == null || stripeApiKey.isBlank()) {
            throw new IllegalStateException("Stripe API key is not configured");
        }
    }

    private void validateWebhookConfiguration(String signatureHeader) {
        if (stripeWebhookSecret == null || stripeWebhookSecret.isBlank()) {
            throw new IllegalStateException("Stripe webhook secret is not configured");
        }
        if (signatureHeader == null || signatureHeader.isBlank()) {
            throw new SecurityException("Missing Stripe-Signature header");
        }
    }
}
