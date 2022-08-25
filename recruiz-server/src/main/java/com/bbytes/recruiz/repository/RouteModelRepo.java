package com.bbytes.recruiz.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.bbytes.recruiz.domain.RouteModel;

public interface RouteModelRepo extends JpaRepository<RouteModel, String> {

	public RouteModel findByMailId(String mail);
}
