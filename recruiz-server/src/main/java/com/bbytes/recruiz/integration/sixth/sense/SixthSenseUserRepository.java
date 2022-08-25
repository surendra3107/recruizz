package com.bbytes.recruiz.integration.sixth.sense;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.integration.SixthSenseUser;

public interface SixthSenseUserRepository extends JpaRepository<SixthSenseUser, Long> {

	SixthSenseUser findByUserName(String userName);
	
	SixthSenseUser findByUser(User user);
	
	//@Query("From SixthSenseUser p where p.loggedUserEmail=?1")
	@Query("SELECT sSu FROM sixth_sense_user sSu WHERE sSu.loggedUserEmail = :loggedInUserEmail")
	//@Query(value = "SELECT * from sixth_sense_user where login_user_email =?1", nativeQuery = true)
	SixthSenseUser findByEmail(@Param("loggedInUserEmail") String loggedInUserEmail);
}
