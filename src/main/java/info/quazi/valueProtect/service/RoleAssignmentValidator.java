package info.quazi.valueProtect.service;

import info.quazi.valueProtect.entity.CompanyType;
import info.quazi.valueProtect.entity.RoleName;
import info.quazi.valueProtect.exception.InvalidRoleAssignmentException;
import org.springframework.stereotype.Component;

@Component
public class RoleAssignmentValidator {

    public void validate(CompanyType companyType, RoleName roleName) {
        if (companyType == null) {
            throw new IllegalArgumentException("Company type is required");
        }

        if (roleName == RoleName.APPRAISER &&
                (companyType == CompanyType.LENDER || companyType == CompanyType.BROKER)) {
            throw new InvalidRoleAssignmentException(
                    "APPRAISER role is not allowed for company type: " + companyType);
        }
    }
}