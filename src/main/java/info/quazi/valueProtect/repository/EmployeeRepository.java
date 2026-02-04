package info.quazi.valueProtect.repository;

import info.quazi.valueProtect.entity.Company;
import info.quazi.valueProtect.entity.Employee;
import info.quazi.valueProtect.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByArchivedFalseOrArchivedIsNull();
    
    Optional<Employee> findByUserAndArchivedFalse(User user);
    
    Optional<Employee> findByUser(User user);
    
    List<Employee> findByCompanyAndArchivedFalse(Company company);
}
