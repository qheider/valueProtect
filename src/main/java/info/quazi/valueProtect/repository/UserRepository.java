package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUserName(String userName);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.userName = :userName")
    Optional<User> findByUserNameWithRoles(@Param("userName") String userName);
}
