package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.PassProtect;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PassProtectRepository extends JpaRepository<PassProtect, Long> {
    
    Optional<PassProtect> findByIdAndArchivedFalseAndCreatedByUserId(Long id, Long createdByUserId);
    
    List<PassProtect> findByCompanyNameContainingIgnoreCaseAndArchivedFalseAndCreatedByUserId(
        String companyName, Long createdByUserId);
}
