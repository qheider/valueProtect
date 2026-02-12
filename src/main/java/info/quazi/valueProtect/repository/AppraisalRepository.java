package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.Appraisal;
import info.quazi.valueProtect.entity.Appraisal.AppraisalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalRepository extends JpaRepository<Appraisal, String> {
    
    Optional<Appraisal> findByAppraisalId(String appraisalId);
    
    // Find appraisals by appraiser (employee) - for regular users to see their own appraisals
    @Query("SELECT a FROM Appraisal a WHERE a.appraiser.id = :appraiserId")
    List<Appraisal> findByAppraiserId(@Param("appraiserId") Long appraiserId);
    
    // Find all appraisals created by employees of a specific company - for admin users
    @Query("SELECT a FROM Appraisal a WHERE a.appraiser.company.id = :companyId")
    List<Appraisal> findByCompanyId(@Param("companyId") Long companyId);
    
    // Find appraisals by appraiser within a company - security check
    @Query("SELECT a FROM Appraisal a WHERE a.appraiser.id = :appraiserId AND a.appraiser.company.id = :companyId")
    List<Appraisal> findByAppraiserIdAndCompanyId(@Param("appraiserId") Long appraiserId, 
                                                  @Param("companyId") Long companyId);
    
    // Security method to verify appraisal belongs to company
    @Query("SELECT a FROM Appraisal a WHERE a.appraisalId = :appraisalId AND a.appraiser.company.id = :companyId")
    Optional<Appraisal> findByAppraisalIdAndCompanyId(@Param("appraisalId") String appraisalId, 
                                                      @Param("companyId") Long companyId);
    
    @Query("SELECT a FROM Appraisal a WHERE a.property.propertyId = :propertyId")
    List<Appraisal> findByPropertyId(@Param("propertyId") String propertyId);
    
    List<Appraisal> findByStatus(AppraisalStatus status);
    
    @Query("SELECT COUNT(a) FROM Appraisal a WHERE a.appraiser.company.id = :companyId")
    long countByCompanyId(@Param("companyId") Long companyId);
    
    @Query("SELECT COUNT(a) FROM Appraisal a WHERE a.appraiser.id = :appraiserId")
    long countByAppraiserId(@Param("appraiserId") Long appraiserId);
}