package info.quazi.valueProtect.dto;

import info.quazi.valueProtect.entity.PassProtect;

import java.time.LocalDateTime;

public class PassProtectDto {
    
    private Long id;
    private String companyName;
    private String companyPassword;
    private String companyUserName;
    private String note;
    private LocalDateTime dateCreated;
    private LocalDateTime dateUpdated;
    private Boolean archived;

    public PassProtectDto() {
    }

    public static PassProtectDto fromEntity(PassProtect entity) {
        PassProtectDto dto = new PassProtectDto();
        dto.setId(entity.getId());
        dto.setCompanyName(entity.getCompanyName());
        dto.setCompanyPassword(entity.getCompanyPassword());
        dto.setCompanyUserName(entity.getCompanyUserName());
        dto.setNote(entity.getNote());
        dto.setDateCreated(entity.getDateCreated());
        dto.setDateUpdated(entity.getDateUpdated());
        dto.setArchived(entity.getArchived());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public LocalDateTime getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDateTime dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDateTime getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDateTime dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public Boolean getArchived() {
        return archived;
    }

    public void setArchived(Boolean archived) {
        this.archived = archived;
    }
}
