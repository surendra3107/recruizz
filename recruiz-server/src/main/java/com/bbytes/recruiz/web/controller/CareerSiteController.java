package com.bbytes.recruiz.web.controller;

import java.io.IOException;
import java.text.ParseException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.enums.Status;
import com.bbytes.recruiz.enums.UsageActionType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.rest.dto.models.CareerSiteDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.service.ExternalUserService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.SearchService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.usage.stat.TenantUsageStatService;
import com.bbytes.recruiz.utils.GlobalConstants;
import com.bbytes.recruiz.utils.SuccessHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;

import ch.qos.logback.classic.Logger;

@RestController
public class CareerSiteController {

	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(CareerSiteController.class);
	
	@Autowired
	protected PositionService positionService;

	@Autowired
	private SearchService searchService;

	@Autowired
	protected ExternalUserService externalUserService;

	@Autowired
	protected TenantResolverService tenantResolverService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private TenantUsageStatService tenantUsageStatService;
	
	@RequestMapping(value = "/careersite/position/{sourcedFrom:.+}/{urlCode:.+}", method = RequestMethod.GET)
	public RestResponse getPositionDetails(@PathVariable("sourcedFrom") String sourcedFrom,
			@PathVariable("urlCode") String urlCode, HttpServletRequest request)
			throws RecruizException, ParseException {

		// making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			sourcedFrom, "",
			UsageActionType.CareerSitePositionDetails.name());

	    
	    Map<String, Object> positionDTO = externalUserService.getPositionForExternal(urlCode, sourcedFrom,
				request.getRequestURI().toString());

		RestResponse response = new RestResponse(RestResponse.SUCCESS, positionDTO);
		return response;
	}

	@RequestMapping(value = "/careersite/position/{sourcedFrom:.+}/{urlCode:.+}", method = RequestMethod.POST)
	public RestResponse addCandidate(@PathVariable("sourcedFrom") String sourcedFrom,
			@PathVariable("urlCode") String urlCode, @RequestParam(value = "name", required = false) String name,
			@RequestParam(value = "email", required = false) String email,
			@RequestParam(value = "mobile", required = false) String mobile,
			@RequestPart(value = "resume") MultipartFile resume,
			@RequestParam(value = "sourceMobile", required = false) String sourceMobile,
			@RequestParam(value = "sourceEmail", required = false) String sourceEmail,
			@RequestParam(value = "sourceName", required = false) String sourceName, HttpServletRequest request)
			throws ParseException, IllegalStateException, IOException {

		
		logger.error("sourcedFrom = "+sourcedFrom+" name = "+name+" email = "+email+" sourceEmail ="+sourceEmail+" sourceName = "+sourceName);
		
	 // making entry to usage stat table
	 		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
	 			sourcedFrom, "Career Site",
	 			UsageActionType.AddCandidateToPositionCareerSite.name());
	 
		try {
			externalUserService.processCandidate(sourcedFrom, urlCode, name, mobile, email, resume, sourceMobile,
					sourceEmail, sourceName, request.getRequestURI().toString(), null);
		} catch (RecruizException e) {
			return new RestResponse(RestResponse.FAILED, e.getMessage(), e.getErrConstant());
		}
		RestResponse response = new RestResponse(RestResponse.SUCCESS, SuccessHandler.APPLIED);
		return response;
	}
	
	
	@RequestMapping(value = "/careersite/position", method = RequestMethod.GET)
	public RestResponse getAllActivePosition(@RequestParam(value = "groupBy", required = false) String groupBy)
			throws RecruizException {

	 // making entry to usage stat table
	 		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
	 			"Career Site", "Career Site",
	 			UsageActionType.CareerSiteGetAllPosition.name());
	    
		CareerSiteDTO careerSiteDTO = new CareerSiteDTO();
		List<String> positionIndustryList = positionService.getDistinctIndustryForCareerSite();
		careerSiteDTO.setPositionIndustryList(positionIndustryList);
		List<String> positionLocationList = positionService.getDistinctLocationForCareerSite();
		careerSiteDTO.setPositionLocationList(positionLocationList);
		
		List<Position> positionList = positionService.getPositionByStatusAndPublishCareersite(Status.Active.toString());
		RestResponse postionsResponse = null;
		if (groupBy != null && GlobalConstants.GROUP_BY_DEPT.equals(groupBy)) {
			Map<String, List<Position>> positionByDept = groupByDepartmentOrClient(positionList);
			careerSiteDTO.setPositionByDept(positionByDept);
		} else {
			careerSiteDTO.setPositionList(positionList);
		}
		postionsResponse = new RestResponse(RestResponse.SUCCESS, careerSiteDTO);
		return postionsResponse;
	}

//	@RequestMapping(value = "/careersite/position/industrylist", method = RequestMethod.GET)
//	public RestResponse getAllIndustryList() throws RecruizException {
//		List<String> positionIndustryList = positionService.getDistinctIndustryForCareerSite();
//		RestResponse postionsIndustryListResponse = new RestResponse(RestResponse.SUCCESS, positionIndustryList);
//		return postionsIndustryListResponse;
//	}
//
//	
//	@RequestMapping(value = "/careersite/position/locationlist", method = RequestMethod.GET)
//	public RestResponse getAllLocationList() throws RecruizException {
//		List<String> positionLocationList = positionService.getDistinctLocationForCareerSite();
//		RestResponse postionsLocationListResponse = new RestResponse(RestResponse.SUCCESS, positionLocationList);
//		return postionsLocationListResponse;
//	}
	
	@RequestMapping(value = "/careersite/search/fulltext", method = RequestMethod.GET)
	public RestResponse searchActivePosition(@RequestParam(value = "searchText", required = true) String searchText,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "groupBy", required = false) String groupBy) throws RecruizException {

	 // making entry to usage stat table
	 		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
	 			"Career Site", "Career Site",
	 			UsageActionType.CareerSiteSerachActivePosition.name());
	    
		if (searchText == null || searchText.isEmpty())
			return null;

		Pageable pageable = pageableService.getSearchPageRequestObject(pageNo, sortField);
		Page<Position> positionList = searchService.globalSearchPositionForCareerSite(searchText, pageable);

		RestResponse postionsResponse = null;
		if (groupBy != null && GlobalConstants.GROUP_BY_DEPT.equals(groupBy)) {
			Map<String, List<Position>> positionByDept = groupByDepartmentOrClient(positionList);
			postionsResponse = new RestResponse(RestResponse.SUCCESS, positionByDept);
		} else {
			postionsResponse = new RestResponse(RestResponse.SUCCESS, positionList);
		}

		return postionsResponse;
	}

	@RequestMapping(value = "/careersite/search", method = RequestMethod.GET)
	public RestResponse searchActivePosition(@RequestParam(value = "location", required = true) String location,
			@RequestParam(value = "industry", required = true) String industry,
			@RequestParam(value = "pageNo", required = false) String pageNo,
			@RequestParam(value = "sortField", required = false) String sortField,
			@RequestParam(value = "groupBy", required = false) String groupBy) throws RecruizException {

	 // making entry to usage stat table
		tenantUsageStatService.insertInToUsageStatTable(TenantContextHolder.getTenant(),
			"Career Site", "Career Site",
			UsageActionType.CareerSiteSerachActivePosition.name());
	    
		if (location == null || industry == null)
			return new RestResponse(RestResponse.FAILED, "Location or industry required to filter position",
					"position_filter_missing");

		Pageable pageable = pageableService.getSearchPageRequestObject(pageNo, sortField);
		Page<Position> positionList = positionService.findByLocationOrIndustryAndStatusAndPublishCareerSiteIsTrue(
				location.getBytes(), industry, Status.Active.toString(), pageable);

		RestResponse postionsResponse = null;
		if (groupBy != null && GlobalConstants.GROUP_BY_DEPT.equals(groupBy)) {
			Map<String, List<Position>> positionByDept = groupByDepartmentOrClient(positionList);
			postionsResponse = new RestResponse(RestResponse.SUCCESS, positionByDept);
		} else {
			postionsResponse = new RestResponse(RestResponse.SUCCESS, positionList);
		}

		return postionsResponse;
	}

	private Map<String, List<Position>> groupByDepartmentOrClient(Page<Position> positionList) {
		return groupByDepartmentOrClient(positionList.getContent());
	}

	private Map<String, List<Position>> groupByDepartmentOrClient(List<Position> positionList) {
		Map<String, List<Position>> positionByDept = new LinkedHashMap<>();
		for (Position position : positionList) {
			String deptName = position.getClient().getClientName();
			List<Position> positionListByDept = positionByDept.get(deptName);
			if (positionListByDept == null) {
				positionListByDept = new LinkedList<>();
				positionByDept.put(deptName, positionListByDept);
			}

			positionListByDept.add(position);
		}

		return positionByDept;
	}
}
