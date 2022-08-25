package com.bbytes.recruiz.repository;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.UserRole;

public interface UserRepository extends JpaRepository<User, Long> {

	User findOneByEmail(String email);

	Set<User> findAll(Specification<User> spec);

	List<User> findOneByUserRole(UserRole roleName);

	Set<User> findByUserRole(UserRole roleName);

	Set<User> findByJoinedStatusAndUserRole(boolean status, UserRole roleName);

	Set<User> findByUserRoleIn(Set<UserRole> roles);

	Set<User> findByEmailIn(Collection<String> emails);

	List<User> findByOrganization(Organization org);

	List<User> findByJoinedStatus(Boolean status);

	List<User> findByUserTypeAndJoinedStatusIsTrue(String userType);

	User findOneByEmailAndPassword(String email, String password);

	User findOneByEmailAndAccountStatus(String email, boolean status);

	User findOneByEmailAndJoinedStatus(String email, boolean status);

	Set<User> findByOrOrganizationAndJoinedStatus(Organization org, boolean status);

	List<User> findByMarkForDelete(Boolean status);

	@Query("select r.id,r.roleName,(select count(role) from user u where r.id = u.userRole) from user_roles r")
	List<User> getUserCountGroupedByRole();

	List<User> findByUserType(String type);

	List<User> findByUserTypeAndAccountStatus(String userType, boolean status);
	
	List<User> findByUserTypeAndAccountStatusAndJoinedStatus(String userType, boolean status, boolean joinedStatus);

	
	@Query("select u.email from user u")
	List<String> findAllEmails();

	User findByEmailAndUserType(String eamil, String type);

	List<User> findByUserTypeAndVendorIdAndAccountStatus(String userType, String vendorId, boolean status);

	Long countByUserType(String userType);

	Long countByVendorId(String vendorId);

	@Query("select count(distinct vendorId) from user")
	Long getVendorCount();

	Set<User> findByVendorId(String vendorId);

	Long countByJoinedStatus(boolean joinedStatus);

	Long countByAccountStatus(boolean accountStatus);
}