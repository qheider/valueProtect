package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.StripeWebhookEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StripeWebhookEventRepository extends JpaRepository<StripeWebhookEvent, String> {
}
