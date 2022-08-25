package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Employee;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Employee findByPresonalEmail(String email);
    
    Employee findByEmpID(String empId);
    
    @Modifying
	@Transactional
	@Query(value = "delete from custom_field_employee where name = :name" , nativeQuery=true)
	void deleteCustomFieldWithName(@Param("name") String name);
    
}
