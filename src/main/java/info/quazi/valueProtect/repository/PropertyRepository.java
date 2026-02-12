package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PropertyRepository extends JpaRepository<Property, String> {
    
    Optional<Property> findByPropertyId(String propertyId);
    
    Optional<Property> findByApn(String apn);
    
    @Query("SELECT p FROM Property p WHERE p.addressLine1 = :address AND p.city = :city AND p.zipPostalCode = :zipCode")
    Optional<Property> findByAddress(@Param("address") String address, 
                                   @Param("city") String city, 
                                   @Param("zipCode") String zipCode);
    
    boolean existsByPropertyId(String propertyId);
    
    boolean existsByApn(String apn);
}