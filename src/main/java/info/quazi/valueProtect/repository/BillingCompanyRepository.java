package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.BillingCompany;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BillingCompanyRepository extends JpaRepository<BillingCompany, Long> {
}
