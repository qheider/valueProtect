package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    
    Optional<Company> findByCompanyCode(String companyCode);
    
    List<Company> findByArchivedFalse();
    
    List<Company> findByStatus(String status);
    
    Optional<Company> findByIdAndArchivedFalse(Long id);
}
