package com.bbytes.recruiz.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.UserRole;

public interface UserRolesRepository extends JpaRepository<UserRole, Long> {

	UserRole findOneByRoleName(String roleName);

	UserRole findOneById(long id);
}
