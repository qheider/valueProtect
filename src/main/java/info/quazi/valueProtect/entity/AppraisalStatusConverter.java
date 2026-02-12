package info.quazi.valueProtect.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * JPA converter for AppraisalStatus enum to handle database values that use display names
 * instead of enum constant names.
 */
@Converter(autoApply = true)
public class AppraisalStatusConverter implements AttributeConverter<Appraisal.AppraisalStatus, String> {

    @Override
    public String convertToDatabaseColumn(Appraisal.AppraisalStatus status) {
        if (status == null) {
            return null;
        }
        return status.getDisplayName();
    }

    @Override
    public Appraisal.AppraisalStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.trim().isEmpty()) {
            return null;
        }

        // Find the enum by display name
        for (Appraisal.AppraisalStatus status : Appraisal.AppraisalStatus.values()) {
            if (status.getDisplayName().equals(dbData)) {
                return status;
            }
        }

        throw new IllegalArgumentException("Unknown AppraisalStatus display name: " + dbData);
    }
}