package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.PropertyFeatures;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyFeaturesRepository extends JpaRepository<PropertyFeatures, String> {
    
    Optional<PropertyFeatures> findByProperty_PropertyId(String propertyId);
    
    void deleteByProperty_PropertyId(String propertyId);
}