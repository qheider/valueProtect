package info.quazi.valueProtect.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreatePaymentSessionRequest {

    @NotBlank(message = "Success URL is required")
    private String successUrl;

    @NotBlank(message = "Cancel URL is required")
    private String cancelUrl;

    @Email(message = "Tenant email must be valid")
    private String tenantEmail;

    private Integer amountCents;

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public String getTenantEmail() {
        return tenantEmail;
    }

    public void setTenantEmail(String tenantEmail) {
        this.tenantEmail = tenantEmail;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public void setAmountCents(Integer amountCents) {
        this.amountCents = amountCents;
    }
}
