package com.bbytes.recruiz.service;

import java.sql.SQLException;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.RecruizBaseApplicationTests;
import com.bbytes.recruiz.domain.Organization;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.Report;
import com.bbytes.recruiz.utils.TenantContextHolder;

@Service
public class TestReportService extends RecruizBaseApplicationTests {

	@Autowired
	private ReportService reportService;

	String tenantId = "test_org";

	Organization org;

	User testUser;

	@Test
	public void testOverallCandidateSourcingChannels() throws RecruizException, SQLException {
		TenantContextHolder.setTenant(tenantId);
		Report report = reportService.overallCandidateSourcingChannels();
		Assert.assertNotNull(report.getReportData());
	}

	@Test
	public void testOverallPositionStatusMix() throws RecruizException, SQLException {
		TenantContextHolder.setTenant(tenantId);
		Report report = reportService.overallPositionStatus();
		Assert.assertNotNull(report.getReportData());
	}

	@Test
	public void testOverallClientStatusMix() throws RecruizException, SQLException {
		TenantContextHolder.setTenant(tenantId);
		Report report = reportService.overallClientStatus();
		Assert.assertNotNull(report.getReportData());
	}
	
	@Test
	public void testOverallClientPositionCandidateCount() throws RecruizException, SQLException {
		TenantContextHolder.setTenant(tenantId);
		Report report = reportService.overallClientPositionCandidateCount();
		Assert.assertNotNull(report.getReportData());
	}

}
