package info.quazi.valueProtect.dto;

import info.quazi.valueProtect.entity.PassProtect;

public class CreatePassProtectRequest {
    
    private String companyName;
    private String companyPassword;
    private String companyUserName;
    private String note;

    public CreatePassProtectRequest() {
    }

    public PassProtect toEntity() {
        PassProtect entity = new PassProtect();
        entity.setCompanyName(this.companyName);
        entity.setCompanyPassword(this.companyPassword);
        entity.setCompanyUserName(this.companyUserName);
        entity.setNote(this.note);
        return entity;
    }

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
}
