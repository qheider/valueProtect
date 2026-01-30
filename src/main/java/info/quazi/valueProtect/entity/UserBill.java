package info.quazi.valueProtect.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "userbill")
public class UserBill extends BaseEntity {

    @Column(name = "amountPaid")
    private Double amountPaid;

    @Lob
    @Column(name = "comment", columnDefinition = "LONGTEXT")
    private String comment;

    @Column(name = "currentBalance")
    private Double currentBalance;

    @Column(name = "minPayment")
    private Double minPayment;

    @Column(name = "minPaymentDueDate")
    private LocalDateTime minPaymentDueDate;

    @Column(name = "previousPayment")
    private Double previousPayment;

    @Column(name = "previousStatementDate")
    private LocalDateTime previousStatementDate;

    @Column(name = "statementDate")
    private LocalDateTime statementDate;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @ManyToOne
    @JoinColumn(name = "billingCompany_id")
    private BillingCompany billingCompany;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    @Column(name = "accountNumber", length = 255)
    private String accountNumber;

    @Column(name = "billNumber", length = 255)
    private String billNumber;

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(Double currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Double getMinPayment() {
        return minPayment;
    }

    public void setMinPayment(Double minPayment) {
        this.minPayment = minPayment;
    }

    public LocalDateTime getMinPaymentDueDate() {
        return minPaymentDueDate;
    }

    public void setMinPaymentDueDate(LocalDateTime minPaymentDueDate) {
        this.minPaymentDueDate = minPaymentDueDate;
    }

    public Double getPreviousPayment() {
        return previousPayment;
    }

    public void setPreviousPayment(Double previousPayment) {
        this.previousPayment = previousPayment;
    }

    public LocalDateTime getPreviousStatementDate() {
        return previousStatementDate;
    }

    public void setPreviousStatementDate(LocalDateTime previousStatementDate) {
        this.previousStatementDate = previousStatementDate;
    }

    public LocalDateTime getStatementDate() {
        return statementDate;
    }

    public void setStatementDate(LocalDateTime statementDate) {
        this.statementDate = statementDate;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public BillingCompany getBillingCompany() {
        return billingCompany;
    }

    public void setBillingCompany(BillingCompany billingCompany) {
        this.billingCompany = billingCompany;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public void setAccountNumber(String accountNumber) {
        this.accountNumber = accountNumber;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }
}
