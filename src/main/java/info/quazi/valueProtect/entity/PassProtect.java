package info.quazi.valueProtect.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "passprotect")
public class PassProtect extends BaseEntity {

    @Column(name = "companyName", length = 255)
    private String companyName;

    @Column(name = "companyPassword", length = 255)
    private String companyPassword;

    @Column(name = "companyUserName", length = 255)
    private String companyUserName;

    @Lob
    @Column(name = "note", columnDefinition = "LONGTEXT")
    private String note;

    @Column(name = "created_by_user_id")
    private Long createdByUserId;

    @ManyToOne
    @JoinColumn(name = "employee_id")
    private Employee employee;

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyPassword() {
        return companyPassword;
    }

    public void setCompanyPassword(String companyPassword) {
        this.companyPassword = companyPassword;
    }

    public String getCompanyUserName() {
        return companyUserName;
    }

    public void setCompanyUserName(String companyUserName) {
        this.companyUserName = companyUserName;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public void setCreatedByUserId(Long createdByUserId) {
        this.createdByUserId = createdByUserId;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}
