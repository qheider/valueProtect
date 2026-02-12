package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.AppraisalDocument;
import info.quazi.valueProtect.entity.AppraisalDocument.DocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AppraisalDocumentRepository extends JpaRepository<AppraisalDocument, String> {
    
    Optional<AppraisalDocument> findByDocumentId(String documentId);
    
    List<AppraisalDocument> findByAppraisalId(String appraisalId);
    
    List<AppraisalDocument> findByAppraisalIdAndDocumentType(String appraisalId, DocumentType documentType);
    
    // Security method to verify document belongs to company through appraisal
    @Query("SELECT d FROM AppraisalDocument d " +
           "JOIN Appraisal a ON d.appraisalId = a.appraisalId " +
           "WHERE d.documentId = :documentId AND a.appraiser.company.id = :companyId")
    Optional<AppraisalDocument> findByDocumentIdAndCompanyId(@Param("documentId") String documentId, 
                                                            @Param("companyId") Long companyId);
    
    void deleteByAppraisalId(String appraisalId);
    
    @Query("SELECT COUNT(d) FROM AppraisalDocument d WHERE d.appraisalId = :appraisalId")
    long countByAppraisalId(@Param("appraisalId") String appraisalId);
}