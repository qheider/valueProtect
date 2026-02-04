package info.quazi.valueProtect.service;

import info.quazi.valueProtect.entity.Company;
import info.quazi.valueProtect.repository.CompanyRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    @Transactional
    public Company create(Company company) {
        company.setArchived(false);
        
        if (company.getStatus() == null || company.getStatus().isEmpty()) {
            company.setStatus("ACTIVE");
        }
        
        return companyRepository.save(company);
    }

    @Transactional
    public Company update(Long id, Company companyDetails) {
        Company company = companyRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));

        if (companyDetails.getName() != null) {
            company.setName(companyDetails.getName());
        }
        if (companyDetails.getCompanyCode() != null) {
            company.setCompanyCode(companyDetails.getCompanyCode());
        }
        if (companyDetails.getEmail() != null) {
            company.setEmail(companyDetails.getEmail());
        }
        if (companyDetails.getPhone() != null) {
            company.setPhone(companyDetails.getPhone());
        }
        if (companyDetails.getWebsite() != null) {
            company.setWebsite(companyDetails.getWebsite());
        }
        if (companyDetails.getAddressLine1() != null) {
            company.setAddressLine1(companyDetails.getAddressLine1());
        }
        if (companyDetails.getAddressLine2() != null) {
            company.setAddressLine2(companyDetails.getAddressLine2());
        }
        if (companyDetails.getCity() != null) {
            company.setCity(companyDetails.getCity());
        }
        if (companyDetails.getState() != null) {
            company.setState(companyDetails.getState());
        }
        if (companyDetails.getCountry() != null) {
            company.setCountry(companyDetails.getCountry());
        }
        if (companyDetails.getPostalCode() != null) {
            company.setPostalCode(companyDetails.getPostalCode());
        }
        if (companyDetails.getTaxId() != null) {
            company.setTaxId(companyDetails.getTaxId());
        }
        if (companyDetails.getRegistrationNumber() != null) {
            company.setRegistrationNumber(companyDetails.getRegistrationNumber());
        }
        if (companyDetails.getDescription() != null) {
            company.setDescription(companyDetails.getDescription());
        }
        if (companyDetails.getStatus() != null) {
            company.setStatus(companyDetails.getStatus());
        }

        @SuppressWarnings("null")
        Company savedCompany = companyRepository.save(company);
        return savedCompany;
    }

    @Transactional(readOnly = true)
    public Company findById(Long id) {
        return companyRepository.findByIdAndArchivedFalse(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<Company> findAll() {
        return companyRepository.findByArchivedFalse();
    }

    @Transactional(readOnly = true)
    public List<Company> findByStatus(String status) {
        return companyRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Company findByCompanyCode(String companyCode) {
        return companyRepository.findByCompanyCode(companyCode)
                .orElseThrow(() -> new RuntimeException("Company not found with code: " + companyCode));
    }

    @Transactional
    public void softDelete(Long id) {
        if (id == null) {
            throw new RuntimeException("Company ID cannot be null");
        }
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Company not found with id: " + id));
        company.setArchived(true);
        companyRepository.save(company);
    }
}
