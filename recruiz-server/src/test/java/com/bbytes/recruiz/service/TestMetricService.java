package com.bbytes.recruiz.service;

import java.sql.SQLException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.google.visualization.datasource.base.TypeMismatchException;

@Service
public class TestMetricService extends RecruizBaseApplicationTests {

	@Autowired
	private MetricsService metricsService;

	String tenantId = "BBytes";

	Organization org;

	User testUser;

	@Test
	public void testOverallGenderMix() throws RecruizException, SQLException {
		TenantContextHolder.setTenant(tenantId);
		metricsService.getOverallCandidateGenderMix();
	}

	@Test
	public void testOverallPositionGenderMix() throws RecruizException, SQLException, TypeMismatchException {
		TenantContextHolder.setTenant(tenantId);
		metricsService.overallPositionGenderMix();
	}

}
