package com.bbytes.recruiz.mail.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.RouteModel;
import com.bbytes.recruiz.repository.RouteModelRepo;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class RouteModelService extends AbstractService<RouteModel, String> {

	private RouteModelRepo routeRepo;

	@Autowired
	public RouteModelService(RouteModelRepo repo) {
		super(repo);
		routeRepo = repo;
	}

	public RouteModel findByMailId(String mail) {
		return routeRepo.findByMailId(mail);
	}

	public boolean routeExist(String mail) {
		return routeRepo.findByMailId(mail) !=null ? true : false;
	}

}
