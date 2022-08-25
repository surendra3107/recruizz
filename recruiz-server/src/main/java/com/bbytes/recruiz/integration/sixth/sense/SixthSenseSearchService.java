package com.bbytes.recruiz.integration.sixth.sense;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.validator.EmailValidator;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClientException;

import com.bbytes.recruiz.auth.jwt.TokenAuthenticationProvider;
import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.integration.IntegrationProfileDetails;
import com.bbytes.recruiz.domain.integration.SixthSenseCandidateProfileCache;
import com.bbytes.recruiz.domain.integration.SixthSenseResumeView;
import com.bbytes.recruiz.domain.integration.SixthSenseUser;
import com.bbytes.recruiz.domain.integration.SixthSenseUserUsage;
import com.bbytes.recruiz.enums.AdvancedSearchIn;
import com.bbytes.recruiz.enums.AdvancedSearchType;
import com.bbytes.recruiz.enums.ResumeFreshness;
import com.bbytes.recruiz.enums.ViewUsageType;
import com.bbytes.recruiz.enums.WebMode;
import com.bbytes.recruiz.enums.integration.AdvancedSearchNoticePeriod;
import com.bbytes.recruiz.enums.integration.SixthSenseDesignationType;
import com.bbytes.recruiz.enums.integration.SixthSenseErrorConstant;
import com.bbytes.recruiz.enums.integration.SixthSenseExcludeCompanyType;
import com.bbytes.recruiz.enums.integration.SixthSenseJobStatus;
import com.bbytes.recruiz.enums.integration.SixthSenseJobType;
import com.bbytes.recruiz.enums.integration.SixthSensePGDegreeType;
import com.bbytes.recruiz.enums.integration.SixthSensePPGDegreeType;
import com.bbytes.recruiz.enums.integration.SixthSenseShow;
import com.bbytes.recruiz.enums.integration.SixthSenseSortBy;
import com.bbytes.recruiz.enums.integration.SixthSenseSource;
import com.bbytes.recruiz.enums.integration.SixthSenseUGDegreeType;
import com.bbytes.recruiz.enums.integration.SixthSenseUniversityType;
import com.bbytes.recruiz.enums.integration.SixthSenseYOPType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.exception.RecruizWarnException;
import com.bbytes.recruiz.rest.dto.models.BaseDTO;
import com.bbytes.recruiz.rest.dto.models.DownloadResumeDto;
import com.bbytes.recruiz.rest.dto.models.RestResponse;
import com.bbytes.recruiz.rest.dto.models.integration.MassMailSendRequest;
import com.bbytes.recruiz.rest.dto.models.integration.MassMailSendResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseATSAPISecretKey;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseAdvanceSearchRequest;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseBaseDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseCandidateDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseCandidateProfileDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseCandidateProfileResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseCaptchaProcess;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseDeleteUserDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseFailedUserDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseGrouptResultResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseJobPortalDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseMessageObject;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseOTPProcess;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseOTPProcessResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSensePortalManageResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseResultDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseResultResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseSearchResultDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseSessionResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseSourceResponse;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseUserCredential;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseUserDTO;
import com.bbytes.recruiz.rest.dto.models.integration.SixthSenseUserResponse;
import com.bbytes.recruiz.service.CandidateActivityService;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.DataModelToDTOConversionService;
import com.bbytes.recruiz.service.FileService;
import com.bbytes.recruiz.service.IResumeParserService;
import com.bbytes.recruiz.service.IntegrationProfileDetailsService;
import com.bbytes.recruiz.service.PageableService;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.service.UserService;
import com.bbytes.recruiz.utils.DateTimeUtils;
import com.bbytes.recruiz.utils.DateUtil;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.IntegrationConstants;
import com.bbytes.recruiz.utils.SearchUtil;
import com.bbytes.recruiz.utils.StringUtils;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Sixth Sense Recruiz Search Service - to talk with sixth sense APIs
 *
 * @author akshay
 *
 */
@Service
public class SixthSenseSearchService {

	private static final Logger logger = LoggerFactory.getLogger(SixthSenseSearchService.class);

	private static final String KEYWORD_HIGHLIGHT_CODE = "<input type=\'hidden\' id=\'wds\' name=\'wds\' value=\'keyword\' />";

	private static final String KEYWORD_HIGHLIGHT_SCRIPT = "<script src=\"ssUrl/resfind/js/jquery.highlight.js\" type=\"text/javascript\"></script> "
			+ " <style>\r\n" + "            .highlight {\r\n" + "               background: yellow !important;\r\n"
			+ "               font-weight: bold;\r\n" + "               font-style: normal;\r\n"
			+ "               padding: 1px;\r\n" + "           }\r\n" + "        </style> "

			+ " <script type=\"text/javascript\">\r\n" + "           var \\$ = jQuery.noConflict();\r\n"
			+ "           \\$(document).ready(function() {\r\n"
			+ "               var wds = \\$(\"#wds\").val().split(',');\r\n" + "var names = [];"
			+ "for (var i = 0; i < wds.length; i++){\r\n" + "names.push(wds[i]);" + "}\r\n"
			+ "\\$(\"#mainCvWrap\").highlight(['c\\#'], { wordsNonwordsOnly: true });\r\n"
			+ "               if (wds.length > 0){\r\n"
			+ "                   \\$(\"#mainCvWrap\").highlight(names, { wordsOnly: true});\r\n"
			+ "\\$(\"#mainCvWrap\").highlight(['c\\#'], { wordsNonwordsOnly: true });\r\n" + "               }\r\n"
			+ "\\$('.helpWindowDiv').remove();" + "           });\r\n" + "       </script> ";

	private static final String KEYWORD_HIGHLIGHT_SCRIPT_CSHARP = "<script src=\"ssUrl/resfind/js/jquery.highlight.js\" type=\"text/javascript\"></script> "
			+ " <style>\r\n" + "            .highlight {\r\n" + "               background: yellow !important;\r\n"
			+ "               font-weight: bold;\r\n" + "               font-style: normal;\r\n"
			+ "               padding: 1px;\r\n" + "           }\r\n" + "        </style> "

			+ " <script type=\"text/javascript\">\r\n" + "           var \\$ = jQuery.noConflict();\r\n"
			+ "           \\$(document).ready(function() {\r\n"
			+ "               var wds = \\$(\"#wds\").val().split(',');\r\n" + "var names = [];"
			+ "for (var i = 0; i < wds.length; i++){\r\n" + "names.push(wds[i]);" + "}\r\n"
			+ "\\$(\"#mainCvWrap\").highlight(['c\\#'], { wordsNonwordsOnly: true });\r\n"
			+ "               if (wds.length > 0){\r\n"
			+ "                   \\$(\"#mainCvWrap\").highlight(names, { wordsOnly: true});\r\n"
			+ "\\$(\"#mainCvWrap\").highlight(['c\\#'], { wordsNonwordsOnly: true });\r\n" + "               }\r\n"
			+ "\\$('.helpWindowDiv').remove();" + "           });\r\n" + "       </script> ";

	private int SIXTH_SENSE_PAGE_SIZE = 80;

	@Autowired
	private SixthSenseRecruizClient sixthSenseRecruizClient;

	@Autowired
	private DataModelToDTOConversionService dataModelToDTOConversionService;

	@Autowired
	private IntegrationProfileDetailsService integrationProfileService;

	@Autowired
	private SixthSenseSessionTokenStore sixthSenseSessionTokenStore;

	@Autowired
	private TokenAuthenticationProvider tokenAuthenticationProvider;

	@Autowired
	private CandidateActivityService candidateActivityService;

	@Autowired
	private SixthSenseCandidateProfileCacheService sixthSenseCandidateProfileCacheService;

	@Autowired
	private SixthSenseUserUsageService sixthSenseUserUsageService;

	@Autowired
	private SixthSenseResumeViewService sixthSenseResumeViewService;

	@Autowired
	private SixthSenseUserRepository sixthSenseUserRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private PageableService pageableService;

	@Autowired
	private IResumeParserService resumeParserService;

	@Autowired
	private SixthSenseIndustryService sixthSenseIndustryService;

	@Autowired
	private SixthSenseCityService sixthSenseCityService;

	@Autowired
	private SixthSenseFuncAreaService sixthSenseFuncAreaService;

	@Autowired
	private SixthSenseFuncAreaRoleService sixthSenseFuncAreaRoleService;

	@Autowired
	private SixthSensePPGDegreeService sixthSensePPGDegreeService;

	@Autowired
	private SixthSensePGDegreeService sixthSensePGDegreeService;

	@Autowired
	private SixthSenseUGDegreeService sixthSenseUGDegreeService;

	@Autowired
	private SixthSensePPGDegreeSpecService sixthSensePPGDegreeSpecService;

	@Autowired
	private SixthSensePGDegreeSpecService sixthSensePGDegreeSpecService;

	@Autowired
	private SixthSenseUGDegreeSpecService sixthSenseUGDegreeSpecService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private FileService fileService;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Value("${export.folderPath.path}")
	private String rootFolderPath;

	@Value("${base.url}")
	private String baseUrl;

	@Value("${sixth.sense.admin.userid:administrator}")
	protected String sixthSenseAdminUserId;

	@Value("${sixth.sense.admin.pwd:recruiz123}")
	protected String sixthSenseAdminPwd;

	private static final String SIXTHSENE_FOLDER_STRUCTURE = "sixth_sense_candidate_profile";

	/**
	 * This method is used to open the session at sixth sense level
	 *
	 * @param sixthSenseUserCredential
	 * @return
	 * @throws RecruizWarnException
	 */
	public RestResponse openSixthSenseSession(SixthSenseUserCredential sixthSenseUserCredential) throws RecruizWarnException {
		RestResponse restResponse = null;
		try {
			ResponseEntity<SixthSenseSessionResponse> response = sixthSenseRecruizClient
					.openSession(sixthSenseUserCredential);

			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				String sessionToken = response.getBody().getSessionToken();
				sixthSenseSessionTokenStore.addSixthSenseSessionId(sixthSenseUserCredential.getUserName(),
						TenantContextHolder.getTenant(), sessionToken);
				if(sessionToken!=null && !sessionToken.isEmpty())
					return new RestResponse(RestResponse.SUCCESS,sessionToken);
				else
					return new RestResponse(RestResponse.FAILED,"Sixth Sense Open Session Resource Access Exception");
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.LoggedIn_Another_System.getCode()) {
				restResponse = resetSixthSenseSession(sixthSenseUserCredential);
				return restResponse;
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Licence_Expired
					.getCode()) {
				return new RestResponse(RestResponse.FAILED,SixthSenseErrorConstant.Licence_Expired.getReason());
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Max_concurrent_user
					.getCode()) {
				return new RestResponse(RestResponse.FAILED,SixthSenseErrorConstant.Max_concurrent_user.getReason());
			} else if (response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Unauthoried_Invalid_Credential.getCode()) {
				return new RestResponse(RestResponse.FAILED,SixthSenseErrorConstant.Unauthoried_Invalid_Credential.getReason());
			}
		} catch (ResourceAccessException rae) {
			logger.error("#### Sixth Sense Open Session Resource Access Exception ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
			logger.error("#### Sixth Sense Open Session Error ### for tenant - " + TenantContextHolder.getTenant() + " "
					+ e.getMessage(), e);
		}

		return null;
	}

	
	public RestResponse openSixthSenseAdminSession(SixthSenseUserCredential sixthSenseUserCredential) throws RecruizWarnException {
		RestResponse restResponse = null;
		try {
			ResponseEntity<SixthSenseSessionResponse> response = sixthSenseRecruizClient
					.openSession(sixthSenseUserCredential);

			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				String sessionToken = response.getBody().getSessionToken();
				sixthSenseSessionTokenStore.addSixthSenseSessionId(sixthSenseUserCredential.getUserName(),
						TenantContextHolder.getTenant(), sessionToken);
				if(sessionToken!=null && !sessionToken.isEmpty())
					return new RestResponse(RestResponse.SUCCESS,sessionToken);
				else
					return new RestResponse(RestResponse.FAILED,"Sixth Sense Open Session Resource Access Exception");
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.LoggedIn_Another_System.getCode()) {
				restResponse = resetSixthSenseAdminSession(sixthSenseUserCredential);
				return restResponse;
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Licence_Expired
					.getCode()) {
				return new RestResponse(RestResponse.FAILED,SixthSenseErrorConstant.Licence_Expired.getReason());
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Max_concurrent_user
					.getCode()) {
				return new RestResponse(RestResponse.FAILED,SixthSenseErrorConstant.Max_concurrent_user.getReason());
			} else if (response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Unauthoried_Invalid_Credential.getCode()) {
				return new RestResponse(RestResponse.FAILED,SixthSenseErrorConstant.Unauthoried_Invalid_Credential.getReason());
			}
		} catch (ResourceAccessException rae) {
			logger.error("#### Sixth Sense Open Session Resource Access Exception ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
			logger.error("#### Sixth Sense Open Session Error ### for tenant - " + TenantContextHolder.getTenant() + " "
					+ e.getMessage(), e);
		}

		return null;
	}
	
	
	
	
	public RestResponse resetSixthSenseAdminSession(SixthSenseUserCredential sixthSenseUserCredential)
			throws RecruizWarnException {
		RestResponse restResponse = null;
		ResponseEntity<SixthSenseSessionResponse> response = null;
		
		String sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUserCredential.getUserName(),
				TenantContextHolder.getTenant());
		
		
		try {
						
				response = sixthSenseRecruizClient.resetAdminSession(sixthSenseUserCredential);
				
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				// String userEmail = userService.getLoggedInUserEmail();
				String sessionToken = response.getBody().getSessionToken();
				if(sessionToken==null) {
					restResponse = new RestResponse(RestResponse.FAILED,response.getBody().getMessageObject().getMessage());
				} else {
					sixthSenseSessionTokenStore.deleteAndPutSixthSenseSessionId(sixthSenseUserCredential.getUserName(),
						TenantContextHolder.getTenant(), sessionToken);
					restResponse = new RestResponse(RestResponse.SUCCESS,sessionToken,
						response.getBody().getMessageObject().getMessage());
				}
				

			} else {
				restResponse = new RestResponse(RestResponse.FAILED,
						response.getBody().getMessageObject().getMessage());
			}

		} catch (ResourceAccessException rae) {
/*			logger.error("#### Sixth Sense Reset Session Resource Access Exception ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);*/
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
			logger.error("#### Sixth Sense Reset Session Error ### for tenant - " + TenantContextHolder.getTenant()
			+ " " + e.getMessage(), e);
		}

		return restResponse;
	}
	
	
	
	/**
	 * This method is used to reset the session at sixth sense level
	 *
	 * @param sixthSenseUserCredential
	 * @return
	 * @throws RecruizWarnException
	 */
	public RestResponse resetSixthSenseSession(SixthSenseUserCredential sixthSenseUserCredential)
			throws RecruizWarnException {
		RestResponse restResponse = null;
		ResponseEntity<SixthSenseSessionResponse> response = null;
		String sessionId = null;
		SixthSenseUser sixthSenseUser = null;
		
		try {
			String userEmail = userService.getLoggedInUserEmail();
			sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userEmail);
			if(sixthSenseUser!=null) {
				sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(),
						TenantContextHolder.getTenant());
			}
			
			if(sixthSenseUser!=null && sessionId!=null){
				response = sixthSenseRecruizClient.resetSession(sixthSenseUserCredential);
				
			} else {
				restResponse = new RestResponse(RestResponse.FAILED, "Unable to reset Job Portal Session. Logout and try again.");
				restResponse.setReason("Unable to reset Job Portal Session. Logout and try again!");
				return restResponse;
				
			}
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				// String userEmail = userService.getLoggedInUserEmail();
				String sessionToken = response.getBody().getSessionToken();
				if(sessionToken==null) {
					restResponse = new RestResponse(RestResponse.FAILED,response.getBody().getMessageObject().getMessage());
				} else {
					sixthSenseSessionTokenStore.deleteAndPutSixthSenseSessionId(sixthSenseUserCredential.getUserName(),
						TenantContextHolder.getTenant(), sessionToken);
					restResponse = new RestResponse(RestResponse.SUCCESS,sessionToken,
						response.getBody().getMessageObject().getMessage());
				}
				

			} else {
				restResponse = new RestResponse(RestResponse.FAILED,
						response.getBody().getMessageObject().getMessage());
			}

		} catch (ResourceAccessException rae) {
/*			logger.error("#### Sixth Sense Reset Session Resource Access Exception ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);*/
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
			logger.error("#### Sixth Sense Reset Session Error ### for tenant - " + TenantContextHolder.getTenant()
			+ " " + e.getMessage(), e);
		}

		return restResponse;
	}

	public void closeSixthSenseSession(boolean adminSession) {
		try {
			
			String userId = userService.getLoggedInUserEmail();
			SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userId);
			String sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(),
					TenantContextHolder.getTenant());
			
			if(sixthSenseUser!=null && sessionId!=null){

				ResponseEntity<SixthSenseSessionResponse> response = sixthSenseRecruizClient.closeSession(false);
				//userId = sixthSenseAdminUserId;
				if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
					// deleting session id from redis
					if (!adminSession) {
						userId = userService.getLoggedInUserEmail();
						sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userId);
						if (sixthSenseUser != null)
							sixthSenseSessionTokenStore.deleteSixthSenseSessionId(sixthSenseUser.getUserName(),
									TenantContextHolder.getTenant());
					} else {
						sixthSenseSessionTokenStore.deleteSixthSenseSessionId(userId, TenantContextHolder.getTenant());
					}
				} else {
					logger.error("Sixth sense error code while closing session :"
							+ response.getBody().getMessageObject().getCode());
					logger.error("Failed to close sixth sense session for tenant and user "
							+ TenantContextHolder.getTenant() + " " + userId);
				}
			}
			
			
			
			
			if(adminSession){

				ResponseEntity<SixthSenseSessionResponse> response = sixthSenseRecruizClient.closeSession(adminSession);
				userId = sixthSenseAdminUserId;
				if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
					// deleting session id from redis
					if (!adminSession) {
						userId = userService.getLoggedInUserEmail();
						sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userId);
						if (sixthSenseUser != null)
							sixthSenseSessionTokenStore.deleteSixthSenseSessionId(sixthSenseUser.getUserName(),
									TenantContextHolder.getTenant());
					} else {
						sixthSenseSessionTokenStore.deleteSixthSenseSessionId(userId, TenantContextHolder.getTenant());
					}
				} else {
					logger.error("Sixth sense error code while closing session :"
							+ response.getBody().getMessageObject().getCode());
					logger.error("Failed to close sixth sense session for tenant and user "
							+ TenantContextHolder.getTenant() + " " + userId);
				}
			}
			
			
			
		} catch (ResourceAccessException conEx) {
			logger.error("#### Sixth Sense Close Session Resource Access Exception ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + conEx.getMessage(), conEx);
		} catch (Exception e) {
			logger.error("#### Sixth Sense Close Session Error ### for tenant - " + TenantContextHolder.getTenant()
			+ e.getMessage(), e);
		}
	}

	/**
	 * This method is used to get all sources from sixth sense
	 *
	 * @return
	 * @throws RecruizWarnException
	 */
	public RestResponse getSources() throws RecruizWarnException {
		try {
			this.openAdminSession();

			ResponseEntity<SixthSenseSourceResponse> response = sixthSenseRecruizClient.getSources();
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				List<BaseDTO> sourceList = new ArrayList<BaseDTO>();
				if (response.getBody() != null && response.getBody().getSources() != null) {
					List<String> sources = response.getBody().getSources();
					for (String source : sources) {
						SixthSenseSource sixthSenseSource = SixthSenseSource.getSourceValue(source);
						BaseDTO baseDTO = new BaseDTO();
						baseDTO.setId(sixthSenseSource.name());
						baseDTO.setValue(sixthSenseSource.getDisplayName());
						boolean exists = false;
						if (null != sourceList && !sourceList.isEmpty()) {
							for (BaseDTO dto : sourceList) {
								if (dto.getId().equalsIgnoreCase(sixthSenseSource.name())) {
									exists = true;
									break;
								}
							}
						}

						if (!exists) {
							sourceList.add(baseDTO);
						}

					}
				}
				return new RestResponse(RestResponse.SUCCESS, sourceList);
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody().getMessageObject().getMessage());
			}

		} catch (ResourceAccessException rae) {
/*			logger.error("#### #### Sixth Sense get sources Resource Exception ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);*/
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
/*			logger.error("#### #### Sixth Sense get sources Error ### for tenant - " + TenantContextHolder.getTenant()
			+ " " + e.getMessage(), e);*/
			throw new RecruizWarnException(e.getMessage(), e);
		} finally {
			this.closeSixthSenseSession(true);
		}
	}

	/**
	 * This method is used to get all sources from sixth sense
	 *
	 * @return
	 * @throws RecruizWarnException
	 */
	public RestResponse getSourcesList() throws RecruizWarnException {
		try {
			this.openAdminSession();

			ResponseEntity<SixthSenseSourceResponse> response = sixthSenseRecruizClient.getSources();
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				List<String> sources = new ArrayList<String>();
				if (response.getBody() != null && response.getBody().getSources() != null) {
					sources = response.getBody().getSources();
				}
				return new RestResponse(RestResponse.SUCCESS, sources);
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody().getMessageObject().getMessage());
			}

		} catch (ResourceAccessException rae) {
/*			logger.error("#### #### Sixth Sense get sources list Resource Exception ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);*/
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
/*			logger.error("#### #### Sixth Sense get sources list Error ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + e.getMessage(), e);*/
			throw new RecruizWarnException(e.getMessage(), e);
		} finally {
			this.closeSixthSenseSession(true);
		}
	}

	/**
	 * This method is used to update the ATS API Secret Key
	 *
	 * @param sixthSenseATSAPISecretKey
	 * @return
	 * @throws Throwable
	 */
	public RestResponse updateATSAPISecretKey(String sixthSenseBaseUrl,
			SixthSenseATSAPISecretKey sixthSenseATSAPISecretKey) throws Throwable {
		try {
			ResponseEntity<SixthSenseResultResponse> response = sixthSenseRecruizClient
					.updateATSAPISecretKey(sixthSenseBaseUrl, sixthSenseATSAPISecretKey);
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS, response.getBody().getMessageObject().getMessage());
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Licence_Expired
					.getCode()) {
				return new RestResponse(RestResponse.FAILED, SixthSenseErrorConstant.Licence_Expired.getMessage(),
						SixthSenseErrorConstant.Licence_Expired.getReason());
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Invalid_Client_Id
					.getCode()) {
				return new RestResponse(RestResponse.FAILED, SixthSenseErrorConstant.Invalid_Client_Id.getMessage(),
						SixthSenseErrorConstant.Invalid_Client_Id.getReason());
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody().getMessageObject().getMessage());
			}

		} catch (ResourceAccessException rae) {
			logger.error("#### Sixth Sense update ATS API Secret Key Resource Access Exception ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RestClientException restEx) {
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
			logger.error("#### Sixth Sense update ATS API Secret Key Error ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + e.getMessage(), e);
			throw new RecruizWarnException(e.getMessage(), e);
		}
	}

	/**
	 * This method is used to get list of all sixth sense users
	 *
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws RecruizWarnException
	 */
	public RestResponse getAllUsers(SixthSenseUserDTO sixthSenseUserDTO) throws RecruizWarnException {

		this.openAdminSession();

		try {
			ResponseEntity<SixthSenseUserResponse> response = sixthSenseRecruizClient.getAllUsers(sixthSenseUserDTO);
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS, response.getBody().getUsers());
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody().getMessageObject().getMessage());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense Get all Users Error ### for tenant - " + TenantContextHolder.getTenant()
			+ e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR);

		} finally {
			this.closeSixthSenseSession(true);
		}
	}

	public void openAdminSession() throws RecruizWarnException {
		SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
		sixthSenseUserCredential.setUserName(sixthSenseAdminUserId);
		sixthSenseUserCredential.setPassword(sixthSenseAdminPwd);
		RestResponse res = openSixthSenseAdminSession(sixthSenseUserCredential);
		if(res!=null){
			if(!res.isSuccess()){
/*				logger.error("#### Sixth Sense open Session failure in openAdminSession() ### for tenant - "
						+ TenantContextHolder.getTenant());*/
				throw new RecruizWarnException("Sixth Sense open Session failure in openAdminSession()",
						"Sixth Sense open Session failure in openAdminSession()");
			}	
		}else{		
/*			logger.error("#### Sixth Sense open Session failure in openAdminSession() ### for tenant - "
					+ TenantContextHolder.getTenant());*/
			throw new RecruizWarnException("Sixth Sense open Session failure in openAdminSession()",
					"Sixth Sense open Session failure in openAdminSession()");
		}
	}

	/**
	 * This method is used to create sixth sense users
	 *
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws RecruizWarnException
	 */
	public List<String> createUser(SixthSenseUserDTO sixthSenseUserDTO) throws RecruizWarnException {

		List<String> userEmailList = new ArrayList<String>();
		this.openAdminSession();
		SixthSenseUserDTO updateUserDTO = new SixthSenseUserDTO();

		try {
			ResponseEntity<SixthSenseUserResponse> response = sixthSenseRecruizClient.createUser(sixthSenseUserDTO);
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				userEmailList.addAll(response.getBody().getSuccessUserList());
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.User_Creation_Error
					.getCode()) {

				logger.error("######## User creation Failed for sixsense user with code :\n\n"
						+ response.getBody().getMessageObject().getCode() + " \n "
						+ response.getBody().getMessageObject().getMessage());

				if (response.getBody().getFailedUserList() != null
						&& !response.getBody().getFailedUserList().isEmpty()) {
					for (SixthSenseFailedUserDTO faliedUser : response.getBody().getFailedUserList()) {
						if ("Username Already Exists".equals(faliedUser.getError())) {
							for (SixthSenseUserCredential userCred : sixthSenseUserDTO.getUsers()) {
								if (userCred.getUserName().equals(faliedUser.getUser())) {
									updateUserDTO.getUsers().add(userCred);
								}
							}
						}
					}

					if (updateUserDTO.getUsers() != null && !updateUserDTO.getUsers().isEmpty()) {
						ResponseEntity<SixthSenseUserResponse> updateResponse = sixthSenseRecruizClient
								.updateUser(updateUserDTO);
						if (updateResponse.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success
								.getCode()) {
							userEmailList.addAll(updateResponse.getBody().getSuccessUserList());
						}
					}
				}

			} else {
				logger.error("########" + response.getBody().getMessageObject().getCode());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense create Users Error ### for tenant - " + TenantContextHolder.getTenant()
			+ e.getMessage(), e);

		} finally {
			this.closeSixthSenseSession(true);
		}
		return userEmailList;
	}

	public List<String> saveSixthSenseUser(List<SixthSenseJobPortalDTO> sixthSenseJobPortalDTOs)
			throws RecruizException {

		List<SixthSenseUser> sixthSenseUsers = constructSixthSenseUsers(sixthSenseJobPortalDTOs);

		SixthSenseUserDTO sixthSenseUserDTO = dataModelToDTOConversionService.converSixthSenseUser(sixthSenseUsers);

		List<String> successResultList = createUser(sixthSenseUserDTO);

		List<String> addedEmails = new ArrayList<String>();
		for (SixthSenseUser sixthSenseUser : sixthSenseUsers) {
			if (successResultList.contains(sixthSenseUser.getUserName())) {

				SixthSenseUserCredential userData = new SixthSenseUserCredential();
				userData.setUserName(sixthSenseUser.getUserName());
				userData.setPassword(sixthSenseUser.getPassword());
				this.resetSixthSenseSession(userData);

				addedEmails.add(sixthSenseUser.getUserName());
				if (tenantResolverService.findSixthSenseUserByUserName(sixthSenseUser.getUserName()) != null) {
					tenantResolverService.updateSixthSenseUser(sixthSenseUser);
				} else {
					tenantResolverService.saveSixthSenseUser(sixthSenseUser);
				}
			}
		}
		return addedEmails;
	}

	public List<String> updateViewUsage(List<SixthSenseJobPortalDTO> sixthSenseJobPortalDTOs) throws RecruizException {

		List<String> updatedUserList = new ArrayList<String>();
		for (SixthSenseJobPortalDTO item : sixthSenseJobPortalDTOs) {
			User user = tenantResolverService.findUserByEmail(item.getEmail());
			if (user != null) {
				SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(user.getEmail());
				if (sixthSenseUser != null) {
					sixthSenseUser.setUsageType(item.getUsageType());
					sixthSenseUser.setViewCount(item.getViewCount());
					sixthSenseUser.setSources(StringUtils.commaSeparate(item.getSources()));
					tenantResolverService.updateSixthSenseUser(sixthSenseUser);
					updatedUserList.add(item.getEmail());
				}
			}
		}
		return updatedUserList;
	}

	public List<String> deleteSixthSenseUser(List<SixthSenseJobPortalDTO> sixthSenseJobPortalDTOs)
			throws RecruizWarnException {

		List<SixthSenseUser> sixthSenseUsers = constructSixthSenseUsers(sixthSenseJobPortalDTOs);

		SixthSenseDeleteUserDTO sixthSenseDeleteUserDTO = dataModelToDTOConversionService
				.converSixthSenseDeleteUser(sixthSenseUsers);

		List<String> successResultList = deleteUser(sixthSenseDeleteUserDTO);

		List<String> addedEmails = new ArrayList<String>();
		for (SixthSenseUser sixthSenseUser : sixthSenseUsers) {
			if (sixthSenseUser != null && sixthSenseUser.getUserName() != null) {
				if (successResultList.contains(sixthSenseUser.getUserName())) {
					addedEmails.add(sixthSenseUser.getUserName());
					tenantResolverService.deleteSixthSenseUser(sixthSenseUser);
				}
			}
		}
		return addedEmails;
	}

	/**
	 * This method is used to update sixth sense users
	 *
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws RecruizWarnException
	 */
	public RestResponse updateUser(SixthSenseUserDTO sixthSenseUserDTO) throws RecruizWarnException {

		this.openAdminSession();

		try {
			ResponseEntity<SixthSenseUserResponse> response = sixthSenseRecruizClient.updateUser(sixthSenseUserDTO);
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS, response.getBody().getSuccessUserList());
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense update Users Error ### for tenant - " + TenantContextHolder.getTenant()
			+ e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR);

		} finally {
			this.closeSixthSenseSession(true);
		}
	}

	/**
	 * This method is used to delete sixth sense users
	 *
	 * @param sixthSenseUserDTO
	 * @return
	 * @throws RecruizWarnException
	 */
	public List<String> deleteUser(SixthSenseDeleteUserDTO sixthSenseDeleteUserDTO) throws RecruizWarnException {

		Set<String> userEmailList = new HashSet<String>();
		this.openAdminSession();

		try {
			ResponseEntity<SixthSenseUserResponse> response = sixthSenseRecruizClient
					.deleteUser(sixthSenseDeleteUserDTO);
			if (response.getBody() != null
					&& response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				userEmailList.addAll(response.getBody().getSuccessUserList());
			} else if (response.getBody() != null && response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.User_Deletion_Error.getCode()) {
				for (SixthSenseFailedUserDTO faliedUser : response.getBody().getFailedUserList()) {
					userEmailList.add(faliedUser.getUser());
				}
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense delete Users Error ### for tenant - " + TenantContextHolder.getTenant()
			+ e.getMessage(), e);
			return null;

		} finally {
			this.closeSixthSenseSession(true);
		}
		return new ArrayList<>(userEmailList);
	}

	/**
	 * This method is used to start portal manage transaction
	 *
	 * @param sources
	 * @return
	 */
	public RestResponse startPortalManageTransaction(String sources) {

		try {
			ResponseEntity<SixthSensePortalManageResponse> response = sixthSenseRecruizClient
					.startPortalManageTransaction(sources);

			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS, response.getBody());
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense start portal manage transaction Error ### for tenant - "
					+ TenantContextHolder.getTenant() + e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR);

		}
	}

	/**
	 * This method is used to start portal manage transaction
	 *
	 * @param sources
	 * @return
	 */
	public RestResponse endPortalManageTransaction(String sources) {

		try {
			ResponseEntity<SixthSensePortalManageResponse> response = sixthSenseRecruizClient
					.endPortalManageTransaction(sources);

			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS, response.getBody());
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody().getMessageObject());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense end portal manage transaction Error ### for tenant - "
					+ TenantContextHolder.getTenant() + e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR);

		}
	}

	/**
	 * This method is used to reset all the existing portal logins
	 *
	 * @param sources
	 * @return
	 */
	public RestResponse resetPortalSources(String sources) {

		try {
			ResponseEntity<SixthSensePortalManageResponse> response = sixthSenseRecruizClient
					.resetPortalSources(sources);

			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS, response.getBody());
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense reset portal sources Error ### for tenant - "
					+ TenantContextHolder.getTenant() + e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR);

		}
	}

	/**
	 * This method is used reload all new portal logins given by sources
	 *
	 * @param sources
	 * @return
	 */
	public RestResponse reloadPortalSources(String sources) {

		try {
			ResponseEntity<SixthSensePortalManageResponse> response = sixthSenseRecruizClient
					.reloadPortalSources(sources);

			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS, response.getBody());
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense reload portal sources Error ### for tenant - "
					+ TenantContextHolder.getTenant() + e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR);

		}
	}

	/**
	 * This method is used to get the list of all portal source credentials
	 *
	 * @param sources
	 * @return
	 * @throws RecruizWarnException
	 */
	public RestResponse getListOfPortalSourceCredentials(String sources) throws RecruizWarnException {

		if (sources == null || sources.isEmpty())
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SOURCE_MISSING,
					ErrorHandler.SIXTH_SENSE_SOURCE_MISSING);

		this.openAdminSession();
		// starting portal transaction
		this.startPortalManageTransaction(sources);

		try {
			ResponseEntity<SixthSensePortalManageResponse> response = sixthSenseRecruizClient
					.getListOfPortalSourceCredentials(sources);

			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS,
						dataModelToDTOConversionService.convertSixthSensePortalManageResponse(response.getBody()));
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Licence_Expired
					.getCode()) {
				throw new RecruizWarnException(SixthSenseErrorConstant.Licence_Expired.getMessage(),
						SixthSenseErrorConstant.Licence_Expired.getReason());
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense get list of portal source credentials Error ### for tenant - "
					+ TenantContextHolder.getTenant() + e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR);

		} finally {
			// ending portal transaction
			this.endPortalManageTransaction(sources);
			this.closeSixthSenseSession(true);
		}
	}

	/**
	 * This method is used to update portal source credentials
	 *
	 * @param sixthSensePortalManageResponse
	 * @return
	 * @throws RecruizWarnException
	 */
	public RestResponse updatePortalSourceCredentials(SixthSensePortalManageResponse sixthSensePortalManageResponse)
			throws RecruizWarnException {

		if (sixthSensePortalManageResponse.getSources() == null
				|| sixthSensePortalManageResponse.getSources().isEmpty())
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SOURCE_MISSING,
					ErrorHandler.SIXTH_SENSE_SOURCE_MISSING);

		this.openAdminSession();
		// starting portal transaction
		this.startPortalManageTransaction(sixthSensePortalManageResponse.getSources());
		// reseting all existing logins
		this.resetPortalSources(sixthSensePortalManageResponse.getSources());

		try {
			ResponseEntity<SixthSensePortalManageResponse> response = sixthSenseRecruizClient
					.updatePortalSourceCredentials(sixthSensePortalManageResponse);

			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS, response.getBody());
			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense update portal source credentials Error ### for tenant - "
					+ TenantContextHolder.getTenant() + e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR);

		} finally {
			// reloading new portal logins
			this.reloadPortalSources(sixthSensePortalManageResponse.getSources());
			// ending portal transaction
			this.endPortalManageTransaction(sixthSensePortalManageResponse.getSources());
			this.closeSixthSenseSession(true);
		}
	}

	// * This method is used to renew the session at sixth sense level
	// *
	// * @param sixthSenseUserName
	// * @return
	// * @throws RecruizWarnException

	/*
	 * public RestResponse renewSixthSenseSession(String sixthSenseUserName) throws
	 * RecruizWarnException { try { // String userEmail =
	 * userService.getLoggedInUserEmail(); String oldSessionId =
	 * sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUserName,
	 * TenantContextHolder.getTenant());
	 * 
	 * SixthSenseRenewSessionRequest sixthSenseRenewSessionRequest = new
	 * SixthSenseRenewSessionRequest();
	 * sixthSenseRenewSessionRequest.setUserName(sixthSenseUserName);
	 * sixthSenseRenewSessionRequest.setOldSessionToken(oldSessionId);
	 * 
	 * ResponseEntity<SixthSenseSessionResponse> response =
	 * sixthSenseRecruizClient.renewSession(sixthSenseRenewSessionRequest); if
	 * (response.getBody().getMessageObject().getCode() ==
	 * SixthSenseErrorConstant.Success.getCode()) { String newSessionToken =
	 * response.getBody().getSessionToken(); // deleting old session id from redis
	 * and updating new one
	 * sixthSenseSessionTokenStore.deleteAndPutSixthSenseSessionId(
	 * sixthSenseUserName, TenantContextHolder.getTenant(), newSessionToken); return
	 * new RestResponse(RestResponse.SUCCESS,
	 * response.getBody().getMessageObject().getMessage()); } else { return new
	 * RestResponse(RestResponse.FAILED,
	 * response.getBody().getMessageObject().getMessage()); }
	 * 
	 * } catch (ResourceAccessException rae) { logger. error(
	 * "#### Sixth Sense renew Session Resource Access Ex ### fortenant - " +
	 * TenantContextHolder.getTenant() + " " + rae.getMessage()+""+ rae); throw new
	 * RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
	 * ErrorHandler.SIXTH_SENSE_SERVER_DOWN); } catch (RecruizWarnException rezEx) {
	 * throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant()); }
	 * catch (Exception e) { logger.error(
	 * "#### Sixth Sense renew Session Error ### for tenant - " +
	 * TenantContextHolder.getTenant() + " " + e.getMessage(), e); return new
	 * RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR); } }
	 */

	public String getCandidateProfile(String profileUrl, String source, String resumeId, String keypwrdsToHighlight)
			throws Throwable {

		SixthSenseCandidateProfileResponse candidateProfile = getCandidateProfileView(profileUrl, source, resumeId,
				keypwrdsToHighlight);
		if (candidateProfile != null) {
			return candidateProfile.getProfileHtml();
		}
		return null;
	}

	public String generateAuthToken() {

		final String authToken = tokenAuthenticationProvider.getAuthTokenForUser(userService.getLoggedInUserEmail(),
				TenantContextHolder.getTenant(), WebMode.EXTERNAL_APP,
				IntegrationConstants.SIXTH_SENSE_TOKEN_VALIDITY_HOUR);

		return authToken;
	}

	/**
	 * This method is used to get Candidate profile from job portal of candidate
	 *
	 * @param profileUrl
	 * @param source
	 * @param currentCompany
	 * @param fullName
	 * @return
	 * @throws Throwable
	 */
	public SixthSenseCandidateProfileResponse getCandidateProfileView(String profileUrl, String source, String resumeId,
			String keywordsToBeHighlighted) throws Throwable {
		String newScript = KEYWORD_HIGHLIGHT_SCRIPT;
		String tenantId = TenantContextHolder.getTenant();
		String newKeywords = "";
		int k = 0;
	/*	SixthSenseCandidateProfileCache candidateProfileCache = sixthSenseCandidateProfileCacheService
				.findByResumeId(resumeId, tenantId);*/

		SixthSenseCandidateProfileResponse profileResponse = new SixthSenseCandidateProfileResponse();

		if (true/*candidateProfileCache == null*/) {

			String strArray[] = keywordsToBeHighlighted.split(",");
			for (int i = 0; i < strArray.length; i++) {

				if (strArray[i].equalsIgnoreCase("c#")) {
					newScript = KEYWORD_HIGHLIGHT_SCRIPT_CSHARP;
					k = 1;
				} else {
					if (newKeywords.equals("")) {
						newKeywords = strArray[i];
					} else {
						newKeywords = newKeywords + "," + strArray[i];
					}

				}
			}
			logger.info("newKeywords ====" + newKeywords);
			if (k == 1) {
				newKeywords = newKeywords + "," + "c";
				keywordsToBeHighlighted = newKeywords;
			}

			try {

				SixthSenseUser userData = sixthSenseUserRepository.findByEmail(userService.getLoggedInUserEmail());

				if (userData != null) {
					userData.setCaptchaStatus("0");
					userData.setLoggedUserEmail(userService.getLoggedInUserEmail());
					userData.setCaptchaSession(null);
					sixthSenseUserRepository.save(userData);
				} else {
					userData = new SixthSenseUser();
					userData.setCaptchaStatus("0");
					userData.setLoggedUserEmail(userService.getLoggedInUserEmail());
					userData.setPassword("");
					userData.setUserName(userService.getLoggedInUserEmail());
					userData.setViewCount(0);
					userData.setCaptchaSession(null);
					sixthSenseUserRepository.save(userData);
				}
				SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
				ResponseEntity<SixthSenseResultResponse> response = null;
				try {
					String userEmail = userService.getLoggedInUserEmail();
					SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userEmail);
					String sessionId = null;
					if (sixthSenseUser != null)
						sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(),
								TenantContextHolder.getTenant());

					if (sessionId == null || sessionId.isEmpty()) {
						sixthSenseUserCredential.setUserName(sixthSenseUser.getUserName());
						sixthSenseUserCredential.setPassword(sixthSenseUser.getPassword());
						// opening session for sixth sense

						RestResponse res = openSixthSenseSession(sixthSenseUserCredential);
						if(res!=null){
							if(!res.isSuccess()){
/*								logger.error("#### Sixth Sense open Session failure in getCandidateProfileView() ### for tenant - "
										+ TenantContextHolder.getTenant());*/
								throw new RecruizWarnException("Sixth Sense open Session failure in getCandidateProfileView()",
										"Sixth Sense open Session failure in getCandidateProfileView()");
							}	
						}else{		
/*							logger.error("#### Sixth Sense open Session failure in getCandidateProfileView() ### for tenant - "
									+ TenantContextHolder.getTenant());*/
							throw new RecruizWarnException("Sixth Sense open Session failure in getCandidateProfileView()",
									"Sixth Sense open Session failure in getCandidateProfileView()");
						}
					}

					// logger.error("In view profile    session_id = "+sessionId+"  user_login email = "+userService.getLoggedInUserEmail());
					 logger.error("(view Candidate frofile) second step hit SixthSense  Api userEmail = "+userService.getLoggedInUserEmail()+"time = "+new Date());
					ResponseEntity<String> resData = sixthSenseRecruizClient
							.getCandidateProfileInString(getCandidateProfileObject(source, profileUrl, null));
					 logger.error("(view Candidate frofile) third step get response from SixthSense  Api userEmail = "+userService.getLoggedInUserEmail()+"time = "+new Date());
					profileResponse = getCandidateProfileViewInString(profileUrl, source, resumeId,
							keywordsToBeHighlighted, newScript, tenantId, resData, sessionId);
					return profileResponse;
				} catch (Exception e) {
					logger.error("Exception === " + e);
				}

				/*				profileResponse.setResponseCode(response.getBody().getMessageObject().getCode());

				if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {

					String finalProfileHtml = profileHTMLWithUrlReplace(source, response.getBody().getProfileHTML(),
							keywordsToBeHighlighted);
					// writeUsingFileWriter(finalProfileHtml);

					String highlightedDetails = finalProfileHtml;
					if (null != keywordsToBeHighlighted && !keywordsToBeHighlighted.trim().isEmpty()) {
						String highlightKey = KEYWORD_HIGHLIGHT_CODE.replace("keyword", keywordsToBeHighlighted);
						String ssUrl = integrationProfileService.getSixthSenseBaseUrl();
						String highlightScript = newScript.replace("ssUrl", ssUrl);
						// highlightScript =
						// KEYWORD_HIGHLIGHT_SCRIPT.replace("DATA", datanew);
						highlightedDetails = finalProfileHtml.replaceAll("<body>", "<body>" + highlightKey);
						highlightedDetails = highlightedDetails.replaceAll("</body>", highlightScript + "</body>");
					}
					candidateProfileCache = new SixthSenseCandidateProfileCache();
					candidateProfileCache.setTenantId(tenantId);
					candidateProfileCache.setResumeId(resumeId);
					candidateProfileCache.setSource(source);
					candidateProfileCache.setProfileUrl(profileUrl);
					candidateProfileCache.setHtmlProfile(response.getBody().getProfileHTML());
					candidateProfileCache.setProfileData(response.getBody().getProfileData());
					candidateProfileCache.setHtmlProfileData(highlightedDetails);
					sixthSenseCandidateProfileCacheService.save(candidateProfileCache);

					profileResponse.setProfileHtml(highlightedDetails);
					profileResponse.setProfileData(response.getBody().getProfileData());

					// use for counting view usage
					this.calculateViewUsage(resumeId, source);

				} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Captcha_Occurred
						.getCode()) {

					profileResponse.setResolved(false);
					profileResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					profileResponse.setResolveSource(source);
					profileResponse.setResolveHTMLRaw(v2captchHTMLUrlReplace(response.getBody().getCaptchaHTML()));
					profileResponse
							.setHiddenParameterMap(getHiddenParamaterForCaptcha(response.getBody().getCaptchaHTML()));

					String userEmail = userService.getLoggedInUserEmail();
					SixthSenseUser sixthSenseUserNew = tenantResolverService.findSixthSenseUserByUserName(userEmail);// new
					if (sixthSenseUserNew != null) {
						sixthSenseUserNew.setCaptchaStatus("1");
						tenantResolverService.updateSixthSenseUser(sixthSenseUserNew);
					}

				} else if (response != null && (response.getBody().getMessageObject()
						.getCode() == SixthSenseErrorConstant.Channel_LoggedIn_Another_System.getCode())) {
					String finalProfileHtml = profileHTMLWithUrlReplace(source,
							response.getBody().getMessageObject().getMessage(), keywordsToBeHighlighted);
					profileResponse.setProfileHtml(finalProfileHtml);
				} else if (response != null && response.getBody().getMessageObject()
						.getCode() == SixthSenseErrorConstant.Session_Invalid.getCode()) {
					SixthSenseUser sixthSenseUser = tenantResolverService
							.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
					SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
					// renew session for sixth sense if session is invalid
					sixthSenseUserCredential.setUserName(sixthSenseUser.getUserName());
					sixthSenseUserCredential.setPassword(sixthSenseUser.getPassword());
					RestResponse resetSessionResponse = resetSixthSenseSession(sixthSenseUserCredential);
					if (resetSessionResponse != null && resetSessionResponse.isSuccess()) {
						return getCandidateProfileView(profileUrl, source, resumeId, keywordsToBeHighlighted);
					} else {
						if (resetSessionResponse != null)
							throw new RecruizWarnException(resetSessionResponse.getReason(),
									resetSessionResponse.getReason());
						else
							throw new RecruizWarnException("Sixth Sense reset session came with no response ",
									"Sixth Sense reset session failed");
					}
				}*/
			} catch (Exception e) {
				logger.error("#### Sixth Sense Candidate profile view Error ### for tenant - "
						+ TenantContextHolder.getTenant() + e.getMessage(), e);
			}
		} /*else {

			if (!keywordsToBeHighlighted.equalsIgnoreCase("null") && null != keywordsToBeHighlighted
					&& !keywordsToBeHighlighted.trim().isEmpty()) {
				String strArray[] = keywordsToBeHighlighted.split(",");
				for (int i = 0; i < strArray.length; i++) {

					if (strArray[i].equalsIgnoreCase("c#")) {
						newScript = KEYWORD_HIGHLIGHT_SCRIPT_CSHARP;
						k = 1;
					} else {
						if (newKeywords.equals("")) {
							newKeywords = strArray[i];
						} else {
							newKeywords = newKeywords + "," + strArray[i];
						}

					}
				}
				logger.info("newKeywords ====" + newKeywords);
				if (k == 1) {
					newKeywords = newKeywords + "," + "c";
					keywordsToBeHighlighted = newKeywords;
				}

				String finalProfileHtml = profileHTMLWithUrlReplace(source, candidateProfileCache.getHtmlProfile(),
						keywordsToBeHighlighted);
				String highlightedDetails = finalProfileHtml;
				if (null != keywordsToBeHighlighted && !keywordsToBeHighlighted.trim().isEmpty()) {
					String highlightKey = KEYWORD_HIGHLIGHT_CODE.replace("keyword", keywordsToBeHighlighted);
					String ssUrl = integrationProfileService.getSixthSenseBaseUrl();
					String highlightScript = newScript.replace("ssUrl", ssUrl);
					// highlightScript =
					// KEYWORD_HIGHLIGHT_SCRIPT.replace("DATA", datanew);
					highlightedDetails = finalProfileHtml.replaceAll("<body>", "<body>" + highlightKey);
					highlightedDetails = highlightedDetails.replaceAll("</body>", highlightScript + "</body>");
				}
				profileResponse.setProfileHtml(highlightedDetails);
				profileResponse.setResponseCode(SixthSenseErrorConstant.Success_Exist_User.getCode());
				profileResponse.setProfileData(candidateProfileCache.getProfileData());
				// writeUsingFileWriter(highlightedDetails);
			} else {
				if (candidateProfileCache.getHtmlProfileData() != null) {
					profileResponse.setProfileHtml(candidateProfileCache.getHtmlProfileData());
				} else {
					profileResponse.setProfileHtml(candidateProfileCache.getHtmlProfile());
				}
				profileResponse.setResponseCode(SixthSenseErrorConstant.Success_Exist_User.getCode());
				profileResponse.setProfileData(candidateProfileCache.getProfileData());
			}

			
			 * String finalProfileHtml = profileHTMLWithUrlReplace(source,
			 * candidateProfileCache.getHtmlProfile()); String highlightedDetails =
			 * finalProfileHtml; if (null != keywordsToBeHighlighted &&
			 * !keywordsToBeHighlighted.trim().isEmpty()) { String highlightKey =
			 * KEYWORD_HIGHLIGHT_CODE.replace("keyword", keywordsToBeHighlighted); String
			 * ssUrl = integrationProfileService.getSixthSenseBaseUrl(); String
			 * highlightScript = newScript.replace("ssUrl", ssUrl); // highlightScript =
			 * KEYWORD_HIGHLIGHT_SCRIPT.replace("DATA", datanew); highlightedDetails =
			 * finalProfileHtml.replaceAll("<body>", "<body>" + highlightKey);
			 * highlightedDetails = highlightedDetails.replaceAll("</body>", highlightScript
			 * + "</body>"); } profileResponse.setProfileHtml(highlightedDetails);
			 * profileResponse.setResponseCode(SixthSenseErrorConstant. Success_Exist_User.
			 * getCode());
			 
		}*/
		profileResponse.setNewKeywords(newKeywords);
		return profileResponse;
	}

	public SixthSenseCandidateProfileResponse getCandidateProfileViewInString(String profileUrl, String source,
			String resumeId, String keywordsToBeHighlighted,
			String newScript, String tenantId, ResponseEntity<String> resData, String sessionId) throws Throwable {

		SixthSenseCandidateProfileResponse profileResponse = new SixthSenseCandidateProfileResponse();
		try {
			// writeUsingFileWriter(resData.toString());
			List<String> dataInString = resData.getHeaders().get("SIXTH_SENSE_MSG_OBJ");
			dataInString.get(0).toString();

			JSONParser parser = new JSONParser();
			JSONObject json = (JSONObject) parser.parse(dataInString.get(0).toString());
			JSONObject messageObject = (JSONObject) json.get("messageObject");
			long co = (long) messageObject.get("code");
			int code = (int) co;
			String message = (String) messageObject.get("message");
			SixthSenseResultResponse responseData = convertFileDataIntoJson(resData.getBody());
			// ResponseEntity<SixthSenseResultResponse> response = new
			// ResponseEntity<SixthSenseResultResponse>();
			/*
			 * ResponseEntity<SixthSenseResultResponse> response = sixthSenseRecruizClient
			 * .getCandidateProfile(getCandidateProfileObject(source, profileUrl, null));
			 */
			// JSONObject messageObject = (JSONObject)
			// resData.get("messageObject");
			// int code = (int) messageObject.get("code");
			// writeUsingFileWriter(responseData.getProfileHTML());

			profileResponse.setResponseCode(code);
			profileResponse.setMessage(message);
			if (code == SixthSenseErrorConstant.Success.getCode()) {

				String finalProfileHtml = profileHTMLWithUrlReplace(source, responseData.getProfileHTML(),
						keywordsToBeHighlighted);

				String highlightedDetails = finalProfileHtml;
				if (null != keywordsToBeHighlighted && !keywordsToBeHighlighted.trim().isEmpty()) {
					String highlightKey = KEYWORD_HIGHLIGHT_CODE.replace("keyword", keywordsToBeHighlighted);
					String ssUrl = integrationProfileService.getSixthSenseBaseUrl();
					String highlightScript = newScript.replace("ssUrl", ssUrl);
					// highlightScript =
					// KEYWORD_HIGHLIGHT_SCRIPT.replace("DATA", datanew);
					highlightedDetails = finalProfileHtml.replaceAll("<body>", "<body>" + highlightKey);
					highlightedDetails = highlightedDetails.replaceAll("</body>", highlightScript + "</body>");
				}
				SixthSenseCandidateProfileCache	candidateProfileCache = new SixthSenseCandidateProfileCache();
				candidateProfileCache.setTenantId(tenantId);
				candidateProfileCache.setResumeId(resumeId);
				candidateProfileCache.setSource(source);
				candidateProfileCache.setProfileUrl(profileUrl);
				candidateProfileCache.setHtmlProfile(responseData.getProfileHTML());
				candidateProfileCache.setProfileData(responseData.getProfileData());
				candidateProfileCache.setHtmlProfileData(highlightedDetails);

				sixthSenseCandidateProfileCacheService.save(candidateProfileCache);

				profileResponse.setProfileHtml(highlightedDetails);
				profileResponse.setProfileData(responseData.getProfileData());
				// use for counting view usage
				this.calculateViewUsage(resumeId, source);

			} else if (code == SixthSenseErrorConstant.Captcha_Occurred.getCode()) {

				profileResponse.setResolved(false);
				profileResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
				profileResponse.setResolveSource(source);
				profileResponse.setResolveHTMLRaw(v2captchHTMLUrlReplace(responseData.getCaptchaHTML()));
				profileResponse.setHiddenParameterMap(getHiddenParamaterForCaptcha(responseData.getCaptchaHTML()));

				SixthSenseUser userData = sixthSenseUserRepository.findByEmail(userService.getLoggedInUserEmail());

				String userEmail = userService.getLoggedInUserEmail();
				SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userEmail);

				logger.error(" before session_id  = "+sessionId);
				
				if (sessionId == null && sixthSenseUser != null)
					sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(),
							TenantContextHolder.getTenant());

				logger.error(" after session_id  = "+sessionId);
				
				if (userData != null) {
					userData.setCaptchaStatus("1");
					userData.setCaptchaSession(sessionId);
					userData.setLoggedUserEmail(userService.getLoggedInUserEmail());
					sixthSenseUserRepository.save(userData);
				}
				//logger.error("In veiw profile captcha html  = "+profileResponse.getResolveHTMLRaw()+"  session_id ="+sessionId);
				/*String resolveHtmlRow = profileResponse.getResolveHTMLRaw();
				String recIdHtml = "fill-recId";
				String sourceHtml = "fill-source";

				resolveHtmlRow = resolveHtmlRow.replace(recIdHtml,sessionId);
				resolveHtmlRow = resolveHtmlRow.replace(sourceHtml,sessionId);

				profileResponse.setResolveHTMLRaw(resolveHtmlRow);*/
				
				logger.error("In veiw profile captcha response =  "+code+" message ="+message);
				
				
				String resolveHtmlRow = profileResponse.getResolveHTMLRaw();
				String userIdString = "userID="+userService.getLoggedInUserEmail();
				String addUserIdSessionId = userIdString+";"+sessionId;

				resolveHtmlRow = resolveHtmlRow.replace(userIdString,addUserIdSessionId);
				

				profileResponse.setResolveHTMLRaw(resolveHtmlRow);
				
				
				logger.error("Capcha occured in view profile "+resolveHtmlRow+"    session_id = "+sessionId+"  user_login email = "+userService.getLoggedInUserEmail());

			}else if (responseData != null && code == SixthSenseErrorConstant.FatalError.getCode()) {

				profileResponse.setResponseCode(code);
				profileResponse.setMessage(message);
				return profileResponse;

			} else if (responseData != null
					&& (code == SixthSenseErrorConstant.LoggedIn_Another_System.getCode())) {
				/*	String finalProfileHtml = profileHTMLWithUrlReplace(source, message, keywordsToBeHighlighted);
				profileResponse.setProfileHtml(finalProfileHtml);*/
				SixthSenseUser sixthSenseUser = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseUserCredential.setUserName(sixthSenseUser.getUserName());
				sixthSenseUserCredential.setPassword(sixthSenseUser.getPassword());
				RestResponse resetSessionResponse = resetSixthSenseSession(sixthSenseUserCredential);			
				if (resetSessionResponse != null && resetSessionResponse.isSuccess()) {
					return getCandidateProfileView(profileUrl, source, resumeId, keywordsToBeHighlighted);
				} else {
					if (resetSessionResponse != null)
						throw new RecruizWarnException(resetSessionResponse.getReason(),
								resetSessionResponse.getReason());
					else
						throw new RecruizWarnException("Sixth Sense reset session came with no response ",
								"Sixth Sense reset session failed");
				} 

			} else if (responseData != null && code == SixthSenseErrorConstant.Session_Invalid.getCode()) {
				SixthSenseUser sixthSenseUser = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseUserCredential.setUserName(sixthSenseUser.getUserName());
				sixthSenseUserCredential.setPassword(sixthSenseUser.getPassword());

				RestResponse res = openSixthSenseSession(sixthSenseUserCredential);
				if(res!=null){
					if(!res.isSuccess()){
						// logger.error("#### Sixth Sense open Session failure in getCandidateProfileViewInString() ### for tenant - "
						//		+ TenantContextHolder.getTenant());
						throw new RecruizWarnException("Sixth Sense open Session failure in getCandidateProfileViewInString()",
								"Sixth Sense open Session failure in getCandidateProfileViewInString()");
					}	
				}else{		
					// logger.error("#### Sixth Sense open Session failure in getCandidateProfileViewInString() ### for tenant - "
					//		+ TenantContextHolder.getTenant());
					throw new RecruizWarnException("Sixth Sense open Session failure in getCandidateProfileViewInString()",
							"Sixth Sense open Session failure in getCandidateProfileViewInString()");
				}

				return getCandidateProfileView(profileUrl, source, resumeId, keywordsToBeHighlighted);

			}
		} catch (Exception e) {
			logger.error("#### Sixth Sense Candidate profile view Error ### for tenant - "
					+ TenantContextHolder.getTenant() + e.getMessage(), e);
		}

		return profileResponse;

	}

	private static SixthSenseResultResponse convertFileDataIntoJson(String dataInString) {
		// read json file data to String
		SixthSenseResultResponse response = new SixthSenseResultResponse();
		;
		try {
			// String jsonString = removeBadChars(dataInString);
			byte[] jsonData = dataInString.getBytes();
			// create ObjectMapper instance
			ObjectMapper objectMapper = new ObjectMapper();
			// convert json string to object
			response = objectMapper.readValue(jsonData, SixthSenseResultResponse.class);

		} catch (JsonParseException e) {
			e.printStackTrace();
		} catch (JsonMappingException e) {
			// TODO Auto-generated catch
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}

	public static String removeBadChars(String s) {
		if (s == null)
			return null;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < s.length(); i++) {
			if (Character.isHighSurrogate(s.charAt(i))) {
				continue;
			}
			sb.append(s.charAt(i));
		}
		return sb.toString();
	}

	/**
	 * This method is used to get Candidate profile data only from job portal of
	 * candidate
	 *
	 * @param profileUrl
	 * @param source
	 * @param currentCompany
	 * @param fullName
	 * @return
	 * @throws Throwable
	 */
	public SixthSenseCandidateProfileResponse getCandidateProfileData(String profileUrl, String source, String resumeId,
			String keywordsToBeHighlighted) throws Throwable {

		String tenantId = TenantContextHolder.getTenant();

		SixthSenseCandidateProfileCache candidateProfileCache = sixthSenseCandidateProfileCacheService
				.findByResumeId(resumeId, tenantId);

		SixthSenseCandidateProfileResponse profileResponse = new SixthSenseCandidateProfileResponse();


		SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
		String userEmail = userService.getLoggedInUserEmail();
		SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userEmail);
		String sessionId = null;
		if (sixthSenseUser != null)
			sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(),
					TenantContextHolder.getTenant());

		if (sessionId == null || sessionId.isEmpty()) {
			sixthSenseUserCredential.setUserName(sixthSenseUser.getUserName());
			sixthSenseUserCredential.setPassword(sixthSenseUser.getPassword());
			// opening session for sixth sense

			RestResponse res = openSixthSenseSession(sixthSenseUserCredential);
			if(res!=null){
				if(!res.isSuccess()){
					//logger.error("#### Sixth Sense open Session failure in getCandidateProfileData() ### for tenant - "
					//		+ TenantContextHolder.getTenant());
					throw new RecruizWarnException("Sixth Sense open Session failure in getCandidateProfileData()",
							"Sixth Sense open Session failure in getCandidateProfileData()");
				}	
			}else{		
				logger.error("#### Sixth Sense open Session failure in getCandidateProfileData() ### for tenant - "
						+ TenantContextHolder.getTenant());
				throw new RecruizWarnException("Sixth Sense open Session failure in getCandidateProfileData()",
						"Sixth Sense open Session failure in getCandidateProfileData()");
			}
		}

		if (candidateProfileCache == null) {

			try {
				ResponseEntity<SixthSenseResultResponse> response = sixthSenseRecruizClient
						.getCandidateProfile(getCandidateProfileObject(source, profileUrl, null));

				if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {

					profileResponse.setProfileData(response.getBody().getProfileData());

					// use for counting view usage
					this.calculateViewUsage(resumeId, source);

				} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Captcha_Occurred
						.getCode()) {

					profileResponse.setResolved(false);
					profileResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					profileResponse.setResolveSource(source);
					profileResponse.setResolveHTMLRaw(v2captchHTMLUrlReplace(response.getBody().getCaptchaHTML()));
					profileResponse
					.setHiddenParameterMap(getHiddenParamaterForCaptcha(response.getBody().getCaptchaHTML()));
				}else if (response != null && response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.FatalError.getCode()) {

					profileResponse.setResponseCode(response.getBody().getMessageObject().getCode());
					profileResponse.setMessage(SixthSenseErrorConstant.FatalError.getMessage());
					return profileResponse;

				}  else if (response != null && (response.getBody().getMessageObject()
						.getCode() == SixthSenseErrorConstant.LoggedIn_Another_System.getCode())) {
					/*	String finalProfileHtml = profileHTMLWithUrlReplace(source,
							response.getBody().getMessageObject().getMessage(), keywordsToBeHighlighted);
					profileResponse.setProfileHtml(finalProfileHtml);*/


					SixthSenseUser sixthSense = tenantResolverService
							.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
					SixthSenseUserCredential sixthSenseCredential = new SixthSenseUserCredential();
					// renew session for sixth sense if session is invalid
					sixthSenseCredential.setUserName(sixthSense.getUserName());
					sixthSenseCredential.setPassword(sixthSense.getPassword());
					RestResponse resetSessionResponse = resetSixthSenseSession(sixthSenseCredential);			
					if (resetSessionResponse != null && resetSessionResponse.isSuccess()) {
						return getCandidateProfileView(profileUrl, source, resumeId, keywordsToBeHighlighted);
					} else {
						if (resetSessionResponse != null)
							throw new RecruizWarnException(resetSessionResponse.getReason(),
									resetSessionResponse.getReason());
						else
							throw new RecruizWarnException("Sixth Sense reset session came with no response ",
									"Sixth Sense reset session failed");
					} 


				} else if (response != null && response.getBody().getMessageObject()
						.getCode() == SixthSenseErrorConstant.Session_Invalid.getCode()) {
					SixthSenseUser sixthSenseUserData = tenantResolverService
							.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
					// renew session for sixth sense if session is invalid
					sixthSenseUserCredential.setUserName(sixthSenseUserData.getUserName());
					sixthSenseUserCredential.setPassword(sixthSenseUserData.getPassword());

					RestResponse res = openSixthSenseSession(sixthSenseUserCredential);
					if(res!=null){
						if(!res.isSuccess()){
							// logger.error("#### Sixth Sense open Session failure in getCandidateProfileData() ### for tenant - "
							// 		+ TenantContextHolder.getTenant());
							throw new RecruizWarnException("Sixth Sense open Session failure in getCandidateProfileData()",
									"Sixth Sense open Session failure in getCandidateProfileData()");
						}	
					}else{		
						logger.error("#### Sixth Sense open Session failure in getCandidateProfileData() ### for tenant - "
								+ TenantContextHolder.getTenant());
						throw new RecruizWarnException("Sixth Sense open Session failure in getCandidateProfileData()",
								"Sixth Sense open Session failure in getCandidateProfileData()");
					}

					return getCandidateProfileView(profileUrl, source, resumeId, keywordsToBeHighlighted);

				}
			} catch (Exception e) {
				logger.error("#### Sixth Sense Candidate profile view Error ### for tenant - "
						+ TenantContextHolder.getTenant() + e.getMessage(), e);
			}
		} else {
			profileResponse.setProfileData(candidateProfileCache.getProfileData());
		}
		return profileResponse;
	}

	/**
	 * Method is used to count the view usage
	 *
	 * @param resumeId
	 * @param source
	 */
	@Transactional
	private void calculateViewUsage(String resumeId, String source) {

		String email = userService.getLoggedInUserEmail();

		// start date set
		Date startDate = DateTime.now().withTimeAtStartOfDay().toDate();

		// end date set
		Date endDate = DateTime.now().plusDays(1).withTimeAtStartOfDay().toDate();

		SixthSenseUserUsage userUsage = sixthSenseUserUsageService.findBetweenDates(email, startDate, endDate);

		SixthSenseResumeView resumeViewFromDb = sixthSenseResumeViewService.findByResumeId(resumeId);

		if (resumeViewFromDb == null) {
			sixthSenseResumeViewService.saveResumeView(resumeId, source);
			this.saveUserViewUsage(email, userUsage);
		}

		// if resume expired with number of days mentioned then we are updating
		// view date and assuming count
		if (resumeViewFromDb != null && sixthSenseResumeViewService.isResumeExpire(resumeId)) {

			if (resumeViewFromDb != null) {
				resumeViewFromDb.setViewOnDate(new Date());
			}

			sixthSenseResumeViewService.saveResumeView(resumeId, source);
			this.saveUserViewUsage(email, userUsage);

		}
	}

	@Transactional
	private void saveUserViewUsage(String email, SixthSenseUserUsage userUsage) {
		if (userUsage != null) {
			int viewCount = userUsage.getViewCount();
			userUsage.setViewCount(++viewCount);
		} else {
			userUsage = new SixthSenseUserUsage();
			userUsage.setDateTime(new Date());
			userUsage.setEmail(email);
			userUsage.setViewCount(1);
			userUsage.setUsageType(tenantResolverService.findSixthSenseUserByUserName(email).getUsageType());
		}
		sixthSenseUserUsageService.save(userUsage);
	}

	public String getUsageType(SixthSenseUser sixthSenseUser) {
		return (sixthSenseUser != null && sixthSenseUser.getUsageType() != null) ? sixthSenseUser.getUsageType()
				: ViewUsageType.UNLIMITED_VIEW.toString();

	}

	@Transactional
	public boolean isViewAllowed(String email, String resumeId) {

		// this will check if profile is cached or new profile or 90 Days old
		// using below method
		if (!checkViewCondition(resumeId))
			return true;

		Date startDate, endDate = null;

		SixthSenseUser sixthSenseUser = tenantResolverService
				.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());

		ViewUsageType type = ViewUsageType.valueOf(this.getUsageType(sixthSenseUser));

		int allowedViewCount = this.getSixthSenseUserViewCount(sixthSenseUser);

		// switch case will check according to usage type
		switch (type) {

		case PER_DAY_VIEW:

			startDate = DateTime.now().withTimeAtStartOfDay().toDate();
			endDate = DateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).toDate();

			int perDayViewCount = sixthSenseUserUsageService.getViewCountBetweenDates(email, startDate, endDate);

			if (perDayViewCount >= allowedViewCount) {
				return false;
			} else {
				return true;
			}

		case PER_WEEK_VIEW:

			startDate = DateTime.now().withDayOfWeek(DateTimeUtils.MONDAY).withTimeAtStartOfDay().toDate();
			endDate = DateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59)
					.withDayOfWeek(DateTimeUtils.SUNDAY).toDate();

			int perWeekViewCount = sixthSenseUserUsageService.getViewCountBetweenDates(email, startDate, endDate);

			if (perWeekViewCount >= allowedViewCount) {
				return false;
			} else {
				return true;
			}

		case PER_MONTH_VIEW:

			startDate = DateTime.now().withDayOfMonth(1).withTimeAtStartOfDay().toDate();
			Calendar c = Calendar.getInstance();
			int monthMaxDays = c.getActualMaximum(Calendar.DAY_OF_MONTH);
			endDate = DateTime.now().withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59)
					.withDayOfMonth(monthMaxDays).toDate();

			int perMonthViewCount = sixthSenseUserUsageService.getViewCountBetweenDates(email, startDate, endDate);

			if (perMonthViewCount >= allowedViewCount) {
				return false;
			} else {
				return true;
			}
		case UNLIMITED_VIEW:
			return true;
		default:
			return true;
		}

	}

	/**
	 * This method is check for profile view condition with following steps
	 *
	 * @param resumeId
	 * @return
	 */
	private boolean checkViewCondition(String resumeId) {

		SixthSenseCandidateProfileCache candidateProfileCache = sixthSenseCandidateProfileCacheService
				.findByResumeId(resumeId, TenantContextHolder.getTenant());

		SixthSenseResumeView resumeView = sixthSenseResumeViewService.findByResumeId(resumeId);

		if (candidateProfileCache != null) {
			return false;
		}

		if (resumeView == null) {
			return true;
		}

		if (resumeView != null && sixthSenseResumeViewService.isResumeExpire(resumeId)) {
			return true;
		}

		return false;
	}

	/**
	 * Method will return allowed view count per user
	 *
	 * @param sixthSenseUser
	 * @return
	 */
	public int getSixthSenseUserViewCount(SixthSenseUser sixthSenseUser) {

		if (sixthSenseUser != null) {
			return (sixthSenseUser.getViewCount() <= 0 ? -1 : sixthSenseUser.getViewCount());
		}
		return -1;
	}

	private String profileHTMLWithUrlReplace(String source, String candidateProfileHTML, String keywordsToBeHighlighted)
			throws MalformedURLException {

		final String sixthSenseUrl = integrationProfileService.getSixthSenseBaseUrl();

		// checking html content https then all rendering urls should be replace
		// with https
		String htmlContent = null;
		if (sixthSenseUrl.contains("https")) {
			URL url = new URL(sixthSenseUrl);
			htmlContent = candidateProfileHTML.replaceAll("http://" + url.getAuthority() + ":80", sixthSenseUrl);
			// to solve styling issue getting screwed becos of mixed content
			// https calling http links in profile html
			htmlContent = htmlContent.replaceAll("http://", "https://");
			htmlContent = htmlContent.replaceAll(":80", "");
		} else {
			htmlContent = candidateProfileHTML;
		}
		logger.debug(htmlContent);

		final String authToken = this.generateAuthToken();

		/*
		 * final String downloadAPIUrl = baseUrl +
		 * "/api/v1/sixthsense/candidate/resume/download?jobSource=" + source +
		 * "&keywordsToBeHighlighted="+ keywordsToBeHighlighted + "&tkn=" + authToken +
		 * "&documentUrl=";
		 */

		final String downloadAPIUrl = baseUrl + "/api/v1/sixthsense/candidate/resume/download?jobSource=" + source
				+ "&keywordsToBeHighlighted=" + keywordsToBeHighlighted + "&tkn=" + authToken + "&documentUrl=";

		final String profileImageAPIUrl = baseUrl + "/api/v1/sixthsense/candidate/profile/image?jobSource=" + source
				+ "&tkn=" + authToken + "&profileImageUrl=";

		String profileHtml = htmlContent.replaceAll("%%SIXTHSENSEAPIDOCUMENTPLACEHOLDER%%\\?rurl=", downloadAPIUrl);

		String finalProfileHtml = profileHtml.replaceAll("%%SIXTHSENSEAPIUIOBJECTPLACEHOLDER%%\\?rurl=",
				profileImageAPIUrl);
		return finalProfileHtml;
	}

	private String v2captchHTMLUrlReplace(String captchaHTML) {

		final String resolveUrl = "/auth/sixthsense/resolve/v2/captcha/search";
		// final String resolveUrl =
		// "http:localhost:9000""/auth/sixthsense/resolve/v2/captcha/search";
		// String replaceString =
		// "\"+(window.location.protocol+window.location.hostname+((window.location.port==\"\")?\"\":(\":\"+window.location.port)))+\"%%SIXTHSENSEAPIRESOLVECAPTCHAPLACEHOLDER%%";
		String replaceString = "%%SIXTHSENSEAPIRESOLVECAPTCHAPLACEHOLDER%%";

		String html = captchaHTML.replace(replaceString, resolveUrl);

		logger.debug("\n\n\n\n Captcha html : \n\n\n" + html + "\n\n\n\n");

		// html.replace("&actionurl=\\\"+(window.location.protocol",
		// "&actionurl=\"+((window.location.protocol)+\":\"+");

		return html;
	}



	public SixthSenseResultResponse getCandidateProfileData(String profileUrl, String source) throws Throwable {
		try {
			ResponseEntity<SixthSenseResultResponse> response = sixthSenseRecruizClient
					.getCandidateProfile(getCandidateProfileObject(source, profileUrl, null));
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return response.getBody();
			} else {
				return response.getBody();
			}

		} catch (ResourceAccessException rae) {
			logger.error("#### Sixth Sense candidate profile data Resource Access Ex ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
			logger.error("#### Sixth Sense candidate profile data Error ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + e.getMessage(), e);
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_ERROR,
					ErrorHandler.SIXTH_SENSE_SERVER_ERROR);
		}
	}

	public void downloadResumeAndSaveFile(String profileUrl, String source, String resumeId, String fullName,
			String currentCompany, String keywordsToBeHighlighted) throws Throwable {

		String documentUrl = getDocumentUrl(getCandidateProfile(profileUrl, source, resumeId, keywordsToBeHighlighted));
		if (documentUrl != null && !documentUrl.isEmpty()) {
			DownloadResumeDto dto = downloadCandidateResume(getCandidateProfileObject(source, null, documentUrl));
			File resumeFile = dto.getFile();
			if (resumeFile != null && resumeFile.exists()) {
				String path = createFolderStructureForCandidateProfile(resumeId);
				saveCandidateResumeFile(resumeId, resumeFile, path);
			}
		}
	}

	public void saveCandidateResumeFile(String resumeId, File resumeFile, String path) throws IOException {
		String extension = FilenameUtils.getExtension(resumeFile.getName());
		Files.copy(resumeFile.toPath(), new File(
				createFolderStructureForCandidateProfile(resumeId) + File.separator + resumeId + "." + extension)
				.toPath(),
				StandardCopyOption.REPLACE_EXISTING);
	}

	private String getDocumentUrl(String profileHTML) {
		Matcher documentUrlPatternOnMatcher = null;
		String documentUrlRegexPattern = IntegrationConstants.DOCUMENT_URL_REGEX_PATTERN;
		// Create a Pattern object
		Pattern documentUrlPatternObj = Pattern.compile(documentUrlRegexPattern);
		if (profileHTML != null && !profileHTML.isEmpty()) {
			// Now create matcher object for document url.
			documentUrlPatternOnMatcher = documentUrlPatternObj.matcher(profileHTML);

			// looping all regex pattern, extracting the string
			while (documentUrlPatternOnMatcher.find()) {
				String documentUrl = documentUrlPatternOnMatcher.group(1);
				return documentUrl;
			}
		}
		return null;
	}

	private byte[] getCaptchaImageContent(String source, String catpchaHtmlContent) throws RecruizWarnException {

		byte[] captchaImage = null;
		String captchaImageUrl = getCaptchaImageUrl(catpchaHtmlContent);

		if (captchaImageUrl != null) {
			captchaImage = getCaptchaImage(source, captchaImageUrl);
		}

		return captchaImage;

	}

	private Map<String, String> getHiddenParamaterForCaptcha(String catpchaHtmlContent) {

		Map<String, String> hiddenParamaterMap = new HashMap<String, String>();

		Document doc = Jsoup.parse(catpchaHtmlContent);

		Elements elements = doc.select("input[type=hidden]");

		for (Element element : elements) {
			String name = element.attr("name");
			String value = element.attr("value");
			hiddenParamaterMap.put(name, value);
		}
		return hiddenParamaterMap;
	}

	private String getCaptchaImageUrl(String catpchaHtmlContent) {

		try {
			Document doc = Jsoup.parse(catpchaHtmlContent);

			Element meta = doc.select("img").first();

			String content = meta.attr("src");
			String[] output = content.split("rurl=");

			if (output.length > 1) {
				return output[1];
			}
		} catch (Exception ex) {
			logger.error("Error Occured in getting captcha image for monster.");
		}

		return null;
	}

	/**
	 * This method is used to get captcha image
	 *
	 * @param source
	 * @param captchaImageUrl
	 * @return
	 * @throws RecruizWarnException
	 */
	public byte[] getCaptchaImage(String source, String captchaImageUrl) throws RecruizWarnException {
		try {
			ResponseEntity<byte[]> response = sixthSenseRecruizClient
					.getCaptchaImage(getCaptchaProcessObject(source, captchaImageUrl));
			SixthSenseMessageObject messageObj = sixthSenseRecruizClient.postResponseHeader(response.getHeaders());
			if (messageObj.getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return response.getBody();
			}
		} catch (ResourceAccessException rae) {
			logger.error("#### Sixth Sense Captcha Image Resource Access Ex ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
			logger.error("#### Sixth Sense Captcha Image Error ### for tenant - " + TenantContextHolder.getTenant()
			+ " " + e.getMessage(), e);
		}
		return null;
	}

	/**
	 * This method is used to get OTP HTML
	 *
	 * @param sixthSenseOTPProcess
	 * @return
	 * @throws RecruizWarnException
	 */
	public SixthSenseOTPProcessResponse checkOTP(SixthSenseOTPProcess sixthSenseOTPProcess)
			throws RecruizWarnException {

		if (sixthSenseOTPProcess.getSource() == null || sixthSenseOTPProcess.getSource().isEmpty())
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SOURCE_MISSING,
					ErrorHandler.SIXTH_SENSE_SOURCE_MISSING);

		SixthSenseOTPProcessResponse OTPResponse = new SixthSenseOTPProcessResponse();
		try {
			ResponseEntity<SixthSenseOTPProcessResponse> response = sixthSenseRecruizClient
					.checkOTP(sixthSenseOTPProcess);
			SixthSenseMessageObject messageObj = sixthSenseRecruizClient.postResponseHeader(response.getHeaders());
			if (messageObj.getCode() == SixthSenseErrorConstant.OTP_Occurred.getCode()) {

				OTPResponse.setResolved(false);
				OTPResponse.setResolveSource(sixthSenseOTPProcess.getSource());
				OTPResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_OTP);
				OTPResponse.setOtpHtml(response.getBody().getOtpHtml());
				OTPResponse.setHiddenParameterMap(getHiddenParamaterForCaptcha(response.getBody().getOtpHtml()));

				return OTPResponse;
			} else if (messageObj.getCode() == SixthSenseErrorConstant.OTP_Resolved_Success.getCode()) {
				OTPResponse.setResolved(true);
				return OTPResponse;
			} else if (messageObj.getCode() == SixthSenseErrorConstant.OTP_Input_Screen_Error.getCode()) {
				OTPResponse.setResolved(false);
				OTPResponse.setSolveAttempt(IntegrationConstants.UNSOLVED);
				// ending portal transaction
				this.resetPortalSources(sixthSenseOTPProcess.getSource());
				this.endPortalManageTransaction(sixthSenseOTPProcess.getSource());
				this.closeSixthSenseSession(false);
				return OTPResponse;
			}else if (response != null && messageObj.getCode() == SixthSenseErrorConstant.FatalError.getCode()) {

				OTPResponse.setResponseCode(messageObj.getCode());
				OTPResponse.setMessage(SixthSenseErrorConstant.FatalError.getMessage());
				return OTPResponse;

			}  else if (response != null
					&& (messageObj.getCode() == SixthSenseErrorConstant.LoggedIn_Another_System.getCode()
					|| messageObj.getCode() == SixthSenseErrorConstant.Session_Invalid.getCode())) {
				// renew session for sixth sense if session is invalid/expired
				SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
				sixthSenseUserCredential.setUserName(sixthSenseAdminUserId);
				sixthSenseUserCredential.setPassword(sixthSenseAdminPwd);
				this.resetSixthSenseSession(sixthSenseUserCredential);
				this.endPortalManageTransaction(sixthSenseOTPProcess.getSource());
				this.closeSixthSenseSession(false);
				return OTPResponse;
			}
		} catch (ResourceAccessException rae) {
			logger.error("#### Sixth Sense Check OTP Resource Access Ex ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
			logger.error("#### Sixth Sense Check OTP Error ### for tenant - " + TenantContextHolder.getTenant() + " "
					+ e.getMessage(), e);
		}
		return OTPResponse;
	}

	/**
	 * This method is used to resolve the OTP
	 *
	 * @param sixthSenseOTPProcess
	 * @return
	 * @throws RecruizWarnException
	 */
	public SixthSenseOTPProcessResponse resolveOTP(String source, Map<String, String> hiddenParameterMap)
			throws RecruizWarnException {

		if (source == null || source.isEmpty())
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SOURCE_MISSING,
					ErrorHandler.SIXTH_SENSE_SOURCE_MISSING);

		SixthSenseOTPProcessResponse OTPResponse = new SixthSenseOTPProcessResponse();
		try {
			ResponseEntity<SixthSenseOTPProcessResponse> response = sixthSenseRecruizClient.resolveOTP(source,
					hiddenParameterMap);
			SixthSenseMessageObject messageObj = sixthSenseRecruizClient.postResponseHeader(response.getHeaders());
			if (messageObj.getCode() == SixthSenseErrorConstant.OTP_Resolved_Success.getCode()) {

				OTPResponse.setResolved(true);

			} else if (messageObj.getCode() == SixthSenseErrorConstant.OTP_Occurred.getCode()) {

				OTPResponse.setResolved(false);
				OTPResponse.setResolveSource(source);
				OTPResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_OTP);
				OTPResponse.setOtpHtml(response.getBody().getOtpHtml());
				OTPResponse.setHiddenParameterMap(getHiddenParamaterForCaptcha(response.getBody().getOtpHtml()));

			}
		} catch (Exception e) {
			logger.error("#### Sixth Sense Resolve OTP Error ### for tenant - " + TenantContextHolder.getTenant()
			+ e.getMessage(), e);
		}
		return OTPResponse;
	}

	/**
	 * This method is used to get UI Data
	 *
	 * @param candidateProfile
	 * @return
	 * @throws RecruizWarnException
	 */
	public byte[] getUIData(SixthSenseCandidateProfileDTO candidateProfile) throws RecruizWarnException {
		try {
			ResponseEntity<byte[]> response = sixthSenseRecruizClient.getUIData(candidateProfile);
			SixthSenseMessageObject messageObj = sixthSenseRecruizClient.postResponseHeader(response.getHeaders());
			if (messageObj.getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return response.getBody();
			}
		} catch (ResourceAccessException rae) {
			logger.error("#### Sixth Sense UI data Resource Access Ex ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
			logger.error("#### Sixth Sense UI Data Error ### for tenant - " + TenantContextHolder.getTenant() + " "
					+ e.getMessage(), e);
		}
		return null;
	}

	/**
	 * This method is used to resolve the captcha image for candidate profile
	 *
	 * @param source
	 * @param hiddenParameterMap
	 * @return
	 */
	public SixthSenseCandidateProfileResponse resolveCaptchaForProfile(String source, String resumeId,
			String profileUrl, Map<String, String> hiddenParameterMap) {

		SixthSenseCandidateProfileCache candidateProfileCache = null;

		SixthSenseCandidateProfileResponse profileResponse = new SixthSenseCandidateProfileResponse();
		try {

			ResponseEntity<SixthSenseResultResponse> response = sixthSenseRecruizClient.resolveCaptcha(source,
					hiddenParameterMap, null);

			if (response != null && (response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Captcha_Candidate_Profile_Success.getCode()
					|| response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode())) {

				// storing (caching) profile html data
				candidateProfileCache = new SixthSenseCandidateProfileCache();
				candidateProfileCache.setTenantId(TenantContextHolder.getTenant());
				candidateProfileCache.setResumeId(resumeId);
				candidateProfileCache.setSource(source);
				candidateProfileCache.setProfileUrl(profileUrl);
				candidateProfileCache.setHtmlProfile(response.getBody().getProfileHTML());

				// sixthSenseCandidateProfileCacheService.save(candidateProfileCache);

				profileResponse.setProfileHtml(response.getBody().getProfileHTML());

			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Captcha_Occurred
					.getCode()) {

				profileResponse.setResolved(false);
				profileResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
				profileResponse.setResolveSource(source);
				profileResponse.setResolveHTML(getCaptchaImageContent(source, response.getBody().getCaptchaHTML()));
				profileResponse
				.setHiddenParameterMap(getHiddenParamaterForCaptcha(response.getBody().getCaptchaHTML()));

			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense Resolve Captcha Image for candidate profile Error ### for tenant - "
					+ TenantContextHolder.getTenant() + e.getMessage(), e);
		}
		return profileResponse;
	}

	/**
	 * This method is used to resolve the captcha image for search result
	 *
	 * @param source
	 * @param hiddenParameterMap 
	 * @param loggedInUser     
	 * @param queryJson
	 * @param sessionId 
	 * @param loggedInUser       
	 * @return
	 * @throws Exception
	 */

	public SixthSenseGrouptResultResponse resolveCaptchaForSearch(String source, Map<String, String> hiddenParameterMap,
			String queryJson) throws Exception {

		//SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
		Pageable pageable = getPageable(Integer.parseInt(hiddenParameterMap.get(IntegrationConstants.PAGE_NUM)), 1);
		String loggedInUser = hiddenParameterMap.get("userID");
		String pageno = hiddenParameterMap.get("pagenum");
		//	String sourceHtmlSessionID = hiddenParameterMap.get("source");
		//	String recIdHtmlSessionID = hiddenParameterMap.get("recId");


		logger.error("get parameter loggedInUser = "+loggedInUser+"   pageNo ="+pageno);
		
		SixthSenseUser userData = sixthSenseUserRepository.findByEmail(loggedInUser.split(";")[0]);
		logger.error("get sixthsense User details = "+userData);
		logger.error("userData.getCaptchaSession()  = "+userData.getCaptchaSession());
		String sessionId = null; 
		if (userData != null) {
			sessionId = userData.getCaptchaSession();
	
		}

		
		if(sessionId==null){
			try{
				sessionId = loggedInUser.split(";")[1];
				logger.error("get capcha session_id  from hidden parameters = "+sessionId);
			}catch(Exception e){
				
			}
		}
		

		logger.error("capcha session_id =  "+sessionId);
		if(sessionId==null){
			
			SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(loggedInUser);
			sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(),
					TenantContextHolder.getTenant());
			logger.error("capcha session_id Still null get from tenantResolver = "+sessionId);
		}

		if (userData != null) {
			hiddenParameterMap.replace("userID", loggedInUser.split(";")[0]);
	
		}	
		
		
		SixthSenseGrouptResultResponse groupResultResponse = new SixthSenseGrouptResultResponse();
		try {

			ResponseEntity<SixthSenseResultResponse> response = sixthSenseRecruizClient.resolveCaptcha(source,
					hiddenParameterMap, sessionId);
			 logger.error("In try ( 1 )***********  Login email_Id =   "+userService.getLoggedInUserEmail()+"  responseCode = "+response.getBody().getMessageObject().getCode()+" responseMessage ="+response.getBody().getMessageObject().getMessage()+"   session_id == " + sessionId);

			if (response != null && (response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Captcha_Search_Success.getCode()
					|| response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode())) {

				logger.error("Captcha Resolved ====  " + ", TenantContextHolder ==" + TenantContextHolder.getTenant()
				+ ",  Login User Email == " + userService.getLoggedInUserEmail() + ", Login Username == "
				+ userService.getLoggedInUserName() + ", session_id == " + sessionId
				+ "   *************************************************");
				try{
					SixthSenseGrouptResultResponse res =  getCombineResult(pageable, response);
					 logger.error("In try ( 2 ) Login  email_Id = "+userService.getLoggedInUserEmail()+"  getCombinedResult Successfully, send to controller ****************"+"session_id == " + sessionId);
					return res;
				}catch(NullPointerException e){
					logger.error("In try ( 3 ) Login  email_Id = "+userService.getLoggedInUserEmail()+"  null pointer Exception come **********"+"session_id == " + sessionId);
					throw new NullPointerException("null pointer Exception come");
				}


			} else if (response != null && response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Captcha_Occurred.getCode()) {
				logger.error("In try ( 4 ) Login  email_Id = "+userService.getLoggedInUserEmail()+" again Captcha_Occurred come *********************"+"session_id == " + sessionId);
				// here checking captcha html can come in captcha html attribute
				// or on search attributes as hits = -1
				if (response.getBody().getCaptchaHTML() == null) {
					return getCombineResult(pageable, response);
				}
				groupResultResponse.setResolved(false);
				groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
				groupResultResponse.setResolveHTMLRaw(v2captchHTMLUrlReplace(response.getBody().getCaptchaHTML()));
				groupResultResponse.setResolveSource(source);
				groupResultResponse.setHiddenParameterMap(getHiddenParamaterForCaptcha(response.getBody().getCaptchaHTML()));

				return groupResultResponse;

			} else if (response != null && (response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Captcha_Candidate_Profile_Success.getCode()
					|| response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode())) {

				groupResultResponse.setResolved(true);
				 logger.error("In try ( 5 )  Login email_Id = "+userService.getLoggedInUserEmail()+" Captcha solved for profile view *********************"+"session_id == " + sessionId);
				return groupResultResponse;
			}

		} catch (Exception e) {

			logger.error("In catch ( 1 )  Login email_Id = "+userService.getLoggedInUserEmail()+" In catch block ================********="+"  pageno ="+pageno+"  queryJson =="+queryJson+"   session_id == " + sessionId+"   HiddenParameters == "+hiddenParameterMap);


			logger.error("#### Sixth Sense  Resolve Captcha Image for search Error ### for tenant Getting NUll - "
					+ TenantContextHolder.getTenant() + e.getMessage());

			String searchQueryJson = convertStingIntoSerachquery(queryJson, pageno);

			 logger.error("In catch ( 2 )  Login email_Id = "+userService.getLoggedInUserEmail()+" ********   searchQueryJson = "+searchQueryJson+"   session_id == " + sessionId);

			/*sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(),
					TenantContextHolder.getTenant());*/
			ResponseEntity<SixthSenseResultResponse> response = sixthSenseRecruizClient
					.resolveCaptchaAfterException(searchQueryJson, sessionId);

			 logger.error("In catch ( 3 )  Login email_Id = "+userService.getLoggedInUserEmail()+" ********   responseCode = "+response.getBody().getMessageObject().getCode()+"   session_id == " + sessionId);

			if (response != null && (response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Captcha_Search_Success.getCode()
					|| response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode())) {

				 logger.error("After  Exception session_id = " + sessionId + "   get Search result === ");

				return getCombineResult(pageable, response);

			} else {
				logger.error("Search  Result ============response code ==="
						+ response.getBody().getMessageObject().getCode());
				throw new Exception();
			}

		}
		logger.error("Search  Result Nothing ::::)");
		return null;
	}

	private String convertStingIntoSerachquery(String queryJson, String pageno) {

		if (queryJson != null) {
			String arr2[] = queryJson.split(";");
			String finalJsonValue = "{ 'SOURCES' : 'naukri', 'PAGENO' :" + pageno + ", 'SEARCHTYPE' : 'advanceSearch',";

			/*
			 * "SOURCES" : "naukri", "PAGENO" : 3, "SEARCHTYPE" : "advanceSearch",
			 */

			for (int i = 0; i < arr2.length; i++) {
				String addData = "' " + ":" + " '";
				String data = arr2[i];
				String newData = data.replace(":", addData);
				addData = "'" + newData + "'";

				if (i > 0) {
					addData = "," + addData;
				}

				finalJsonValue = finalJsonValue + addData;
			}

			finalJsonValue = finalJsonValue + "}";

			return finalJsonValue;
		}

		return null;
	}

	/**
	 * This method is used to add candidate profile into Recruiz
	 *
	 * @param profileUrl
	 * @param source
	 * @param resumeId
	 * @param fullName
	 * @param currentCompany
	 * @return
	 * @throws Throwable
	 */
	public Candidate addToRecruiz(String profileUrl, String source, String resumeId, String fullName,
			String currentCompany, SixthSenseCandidateDTO candidateDTO, Map<String, String> candidateInfo)
					throws Throwable {

		String candidateHash = SearchUtil.candidateHash(fullName, currentCompany);
		Candidate candidateFromDB = candidateService.getByCandidateSha1HashOrExternalAppCandidateId(candidateHash,
				resumeId);

		if (candidateFromDB == null) {
			candidateFromDB = addCandidateToRecruiz(profileUrl, source, resumeId, fullName, currentCompany,
					candidateFromDB, candidateDTO, candidateInfo);
		}
		// checking data older than 1 day
		long eligibleForUpdation = DateTime.now().getMillis() - (1 * 24 * 60 * 60 * 1000);
		if (candidateFromDB != null && candidateFromDB.getModificationDate().getTime() < eligibleForUpdation) {
			candidateFromDB = addCandidateToRecruiz(profileUrl, source, resumeId, fullName, currentCompany,
					candidateFromDB, candidateDTO, candidateInfo);
		}

		return candidateFromDB;
	}

	public Candidate addCandidateToRecruiz(String profileUrl, String source, String resumeId, String fullName,
			String currentCompany, Candidate candidateFromDB, SixthSenseCandidateDTO candidateDTO,
			Map<String, String> candidateInfo) throws Throwable {
		try {

			candidateFromDB = saveDownloadedProfileIntoDB(profileUrl, source, resumeId, fullName, currentCompany,
					candidateFromDB, candidateDTO, candidateInfo);

		} catch (Exception e) {
			logger.error("#### Sixth Sense Add to recruiz Error ### for tenant - " + TenantContextHolder.getTenant()
			+ e.getMessage(), e);
		}
		return candidateFromDB;
	}

	private Candidate saveDownloadedProfileIntoDB(String profileUrl, String source, String resumeId, String fullName,
			String currentCompany, Candidate candidateFromDB, SixthSenseCandidateDTO candidateDTO,
			Map<String, String> candidateInfo) throws Throwable {

		String profileData = null, documentUrl = null;

		SixthSenseCandidateProfileCache candidateProfileCache = sixthSenseCandidateProfileCacheService
				.findByResumeId(resumeId, TenantContextHolder.getTenant());
		String htmlData = null;
		if (candidateProfileCache != null) {
			SixthSenseResultResponse response = getCandidateProfileData(profileUrl, source);
			documentUrl = getDocumentUrl(response.getProfileHTML());
			profileData = response.getProfileData();
			htmlData = response.getProfileHTML();
		} else {
			documentUrl = getDocumentUrl(candidateProfileCache.getHtmlProfile());
			profileData = candidateProfileCache.getProfileData();
			htmlData = candidateProfileCache.getHtmlProfile();
		}
		if (documentUrl != null && !documentUrl.isEmpty()) {
			DownloadResumeDto dto = downloadCandidateResume(getCandidateProfileObject(source, null, documentUrl));
			File resumeFile = dto.getFile();
			if (resumeFile != null && resumeFile.exists())
				candidateFromDB = saveSixthSenseConvertedProfileIntoDB(resumeId, fullName, currentCompany, source,
						resumeFile, profileData, candidateFromDB, candidateDTO, candidateInfo, htmlData);
		}
		return candidateFromDB;
	}

	public Candidate extractProfileData(String profileData, Candidate candidate) {

		String dataList[] = new String[30];

		if (profileData != null && !profileData.isEmpty()) {
			StringTokenizer strToken = new StringTokenizer(profileData, "^^^");
			int index = 0;
			while (strToken.hasMoreTokens()) {
				dataList[index++] = strToken.nextElement().toString();
			}
		}

		if (dataList != null) {
			// taking email address from naukri as priority then if not valid
			// email then taking from resume file
			if (EmailValidator.getInstance().isValid(StringUtils.commaSeparateStringToList(dataList[0]).get(0))) {
				candidate.setEmail(StringUtils.commaSeparateStringToList(dataList[0]).get(0));
			}
			candidate.setMobile(StringUtils.commaSeparateStringToList(dataList[1]).get(0));
			candidate.setFullName(dataList[2]);
			candidate.setDob(DateUtil.parseDate(dataList[3]));
			candidate.setGender(StringUtils.parseGender(dataList[4]));
			candidate.setCurrentLocation(dataList[5]);
			candidate.setSource(dataList[6]);

			List<String> designationList = StringUtils.pipeSeparateStringToList(dataList[7]);
			candidate.setCurrentTitle((designationList.size() > 0 && !designationList.get(0).trim().isEmpty())
					? (designationList.get(0).trim())
							: "N/A");

			candidate.setPreferredLocation(dataList[9] != null ? dataList[9] : "N/A");

			List<String> list = StringUtils.pipeSeparateStringToList(dataList[10]);
			String totalExp = list.size() > 0 ? list.get(0).replaceAll("Exp:", "").trim() : null;
			if (totalExp != null && !totalExp.isEmpty() && !totalExp
					.equalsIgnoreCase("Fresher")/* && org.apache.commons.lang.StringUtils.isNumeric(totalExp) */) {
				List<String> expList = Arrays.asList(totalExp.split("\\s+"));
				totalExp = (expList.size() > 0 ? StringUtils.getDecimalNumberFromString(expList.get(0)) : 0) + "."
						+ (expList.size() > 1 ? StringUtils.getDecimalNumberFromString(expList.get(1)) : 0);
				/*
				 * if(totalExp.contains("yr")){ totalExp.replace("yr", "."); }
				 * 
				 * if(totalExp.contains("0m")){ totalExp.replace(". 0m", ""); }else{
				 * totalExp.replace("m", ""); totalExp.replace(". ", "."); } totalExp.trim();
				 */
				try{
					candidate.setTotalExp(Double.valueOf(totalExp));
				}catch(Exception e){
					if(expList.size()==2)
						totalExp = expList.get(0);
					else
						totalExp = expList.get(0)+"."+(expList.get(2).trim());
					/*totalExp = list.size() > 0 ? list.get(0).replaceAll("Exp:", "").trim() : null;

					if(totalExp.contains(" years")){ totalExp.replace(" years", "."); }

					if(totalExp.contains("months")){ totalExp.replace(". 0m", ""); }else{
						  totalExp.replace("m", ""); totalExp.replace(". ", "."); } totalExp.trim();*/

					try{
						candidate.setTotalExp(Double.valueOf(totalExp));
					}catch(Exception ex){}

				}
			}

			String ctc = list.size() > 1 ? list.get(1).replaceAll("CTC:", "").trim() : null;
			if (ctc != null && !ctc.isEmpty() && !org.apache.commons.lang.StringUtils.isNumeric(ctc)) {
				candidate.setCurrentCtc(StringUtils.parseSalaryString(ctc));
			}

			Set<String> skillSet = new HashSet<String>(
					StringUtils.commaSeparateStringToList(dataList[11] != null ? dataList[11] : "N/A"));

			Set<String> skillSetToBeRemoved = new HashSet<>();
			for (String key : skillSet) {
				if (!StringUtils.isValidSkill(key)) {
					skillSetToBeRemoved.add(key);
				}
			}

			if(skillSetToBeRemoved.size()>0){
				skillSet.removeAll(skillSetToBeRemoved);
				candidate.setKeySkills(skillSet);
			}


			candidate.setCurrentCompany(dataList[12] != null ? dataList[12] : "N/A");
			candidate.setPreviousEmployment(dataList[13] != null ? dataList[13] : "N/A");
			candidate.setIndustry(dataList[16] != null ? dataList[16] : "N/A");
		}
		return candidate;

	}


	private Candidate saveSixthSenseConvertedProfileIntoDB(String resumeId, String name, String company, String source,
			File resumeFile, String profileData, Candidate existingCandidate, SixthSenseCandidateDTO candidateDTO,
			Map<String, String> candidateInfo, String htmlData) throws RecruizException, IOException {

		Candidate candidate = null;
		try {
			candidate = resumeParserService.parseResume(resumeFile);
		} catch (Exception e) {
			logger.error(" >>>>>>>>>>>>>>> Parser issue during add to recruiz api call due to failure in parser");
		}

		if (candidate == null && candidateInfo != null && candidateInfo.get("email") != null
				&& candidateInfo.get("mobile") != null) {
			candidate = new Candidate();
			candidate.setEmail(candidateInfo.get("email"));
			candidate.setMobile(candidateInfo.get("mobile"));
			candidate.setFullName(candidateDTO.getFullName());
/*			logger.error(
					" >>>>>>>>> Creating basic candidate to make sure we have a candidate object created even after parser failure");*/
		} else {
			// checking already existing candidate via sixth sense
			if (existingCandidate != null) {
				candidate = existingCandidate.copy(candidate);
			}
		}

		if (null != candidateDTO) {
			candidate.setFullName(candidateDTO.getFullName());
			candidate.setCurrentCompany(candidateDTO.getCurrentCompany());
			candidate.setCurrentTitle(candidateDTO.getCurrentTitle());
			candidate.setCurrentLocation(candidateDTO.getCurrentLocation());
			candidate.setCurrentCtc(StringUtils.getFormattedCTCForCandidate(candidateDTO.getCurrentCtc()));
			candidate.setHighestQual(candidateDTO.getHighestQual());
			candidate.setTotalExp(StringUtils.getFormattedExpForCandidate(candidateDTO.getTotalExp()));
			candidate.setPreferredLocation(candidateDTO.getPreferredLocation());
		}

		// checking candidate exist in db with same email
		Candidate candidateFromDB = candidateService.getCandidateByEmail(candidate.getEmail());

		if (candidateFromDB != null) {
			candidate = candidateFromDB.copy(candidate);
		}

		candidate.setExternalAppCandidateId(resumeId);
		candidate.setCurrentCompany(company);
		candidate.setFullName(name);
		candidate.calculateCandidateSha1Hash();

		candidate = extractProfileData(profileData, candidate);
		candidate = setCandidateInformation(htmlData, candidate);
		// updating source info older than 6 months for existing candidate
		if (candidate.getCid() > 0) {
			candidateService.setSourceInfo(candidate, userService.getLoggedInUserEmail(),
					SixthSenseSource.getSourceValue(source).getDisplayName(), null);
			candidateService.save(candidate);
			candidateActivityService.detailsUpdated(candidate);
		} else {
			candidate.setSource(SixthSenseSource.getSourceValue(source).getDisplayName());
			candidate.setSourcedOnDate(new Date());
			candidate = candidateService.addCandidate(candidate);
		}

		candidateService.uploadCandidateFile(candidate, resumeFile);
		// logger.error(" >>>>>>>>>>>>>>> Canddiate name after add to recruiz api call : " + candidate.getFullName());
		return candidate;
	}

	public SixthSenseCandidateProfileDTO getCandidateProfileObject(String source, String profileUrl, String documentUrl)
			throws Throwable {

		SixthSenseCandidateProfileDTO candidateProfile = new SixthSenseCandidateProfileDTO();
		candidateProfile.setSource(source);
		candidateProfile.setCandidateProfileURL(profileUrl);
		candidateProfile.setDocumentURL(documentUrl);
		return candidateProfile;
	}

	private SixthSenseCaptchaProcess getCaptchaProcessObject(String source, String captchaImageUrl) {

		SixthSenseCaptchaProcess captchaProcess = new SixthSenseCaptchaProcess();
		captchaProcess.setSource(source);
		captchaProcess.setCaptchaImageURL(captchaImageUrl);
		return captchaProcess;
	}

	private String createFolderStructureForCandidateProfile(String resumeId) {
		String folderPath = rootFolderPath + File.separator + SIXTHSENE_FOLDER_STRUCTURE + File.separator
				+ TenantContextHolder.getTenant() + File.separator + resumeId;

		File directory = new File(folderPath);
		if (!directory.exists())
			directory.mkdirs();
		return directory.getPath();
	}

	/**
	 * This method is used to download the resume of candidate from job portal
	 *
	 * @param candidateProfileRequest
	 * @return
	 * @throws Throwable
	 * @throws Exception
	 */
	public DownloadResumeDto downloadCandidateResume(SixthSenseCandidateProfileDTO candidateDownloadCVRequest) throws Throwable {
		SixthSenseUser sixthSenseUser = tenantResolverService
				.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
		try {
			DownloadResumeDto dto = new DownloadResumeDto();
			ResponseEntity<byte[]> response = sixthSenseRecruizClient
					.downloadCandidateResume(candidateDownloadCVRequest);

			SixthSenseMessageObject messageObj = sixthSenseRecruizClient.postResponseHeader(response.getHeaders());

			if(messageObj.getCode()==SixthSenseErrorConstant.FatalError.getCode()){

				dto.setResponseCode(messageObj.getCode());
				dto.setMessage(SixthSenseErrorConstant.FatalError.getMessage());
				return dto;
			}

			if (messageObj.getCode() == SixthSenseErrorConstant.Success.getCode()) {
				String fileName = getContentDispositionHeader(response.getHeaders());
				// writing binary array into file
				String bodyStrng = new String(response.getBody(), "UTF-8");
				if (fileName == null || fileName.equalsIgnoreCase("null") || fileName.equalsIgnoreCase("")) {		
					fileName = "CandidateResume";
				}
				File documentFile = com.bbytes.recruiz.utils.FileUtils.writeToFile(fileName, response.getBody());
				dto.setResponseCode(messageObj.getCode());
				dto.setMessage(messageObj.getMessage());
				dto.setFile(documentFile);
				return dto;
			}else if (response != null && (messageObj
					.getCode() == SixthSenseErrorConstant.LoggedIn_Another_System.getCode())) {
				/*	String finalProfileHtml = profileHTMLWithUrlReplace(source,
						response.getBody().getMessageObject().getMessage(), keywordsToBeHighlighted);
				profileResponse.setProfileHtml(finalProfileHtml);*/


				SixthSenseUser sixthSense = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseCredential.setUserName(sixthSense.getUserName());
				sixthSenseCredential.setPassword(sixthSense.getPassword());
				RestResponse resetSessionResponse = resetSixthSenseSession(sixthSenseCredential);			
				if (resetSessionResponse != null && resetSessionResponse.isSuccess()) {
					return downloadCandidateResume(candidateDownloadCVRequest);
				} else {
					if (resetSessionResponse != null)
						throw new RecruizWarnException(resetSessionResponse.getReason(),
								resetSessionResponse.getReason());
					else
						throw new RecruizWarnException("Sixth Sense reset session came with no response ",
								"Sixth Sense reset session failed");
				} 


			} else if (response != null && messageObj
					.getCode() == SixthSenseErrorConstant.Session_Invalid.getCode()) {
				SixthSenseUser sixthSenseUserData = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseCredential.setUserName(sixthSenseUserData.getUserName());
				sixthSenseCredential.setPassword(sixthSenseUserData.getPassword());

				RestResponse res = openSixthSenseSession(sixthSenseCredential);
				if(res!=null){
					if(!res.isSuccess()){
						logger.error("#### Sixth Sense open Session failure in downloadCandidateResume() ### for tenant - "
								+ TenantContextHolder.getTenant());
						throw new RecruizWarnException("Sixth Sense open Session failure in downloadCandidateResume()",
								"Sixth Sense open Session failure in downloadCandidateResume()");
					}	
				}else{		
					logger.error("#### Sixth Sense open Session failure in downloadCandidateResume() ### for tenant - "
							+ TenantContextHolder.getTenant());
					throw new RecruizWarnException("Sixth Sense open Session failure in downloadCandidateResume()",
							"Sixth Sense open Session failure in downloadCandidateResume()");
				}

				return downloadCandidateResume(candidateDownloadCVRequest);

			} else {
				throw new RecruizWarnException(messageObj.getMessage(), messageObj.getMessage());
			}
		} catch (ResourceAccessException rae) {
/*			logger.error("#### Sixth Sense renew Session Resource Access Ex ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);*/
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
/*			logger.error("#### Sixth Sense renew Session Error ### for tenant - " + TenantContextHolder.getTenant()
			+ " " + e.getMessage(), e);*/
			throw new RecruizWarnException(e.getMessage(), e);
		}
	}

	private String getContentDispositionHeader(HttpHeaders headers) {
		String disposition = headers.getFirst(IntegrationConstants.CONTENT_DISPOSITION);
		String fileName = disposition.replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
		return fileName;
	}

	/**
	 * Send mass mail
	 *
	 */
	public RestResponse sendMassMail(MassMailSendRequest massMailSendRequest) throws RecruizWarnException {

		String htmlData = massMailSendRequest.getJobDescription();
		// String newHtmlData = htmlData.replace("· ", "");
		String newHtmlData = cleanTextContent(htmlData);
		massMailSendRequest.setJobDescription(newHtmlData);
		String userEmail = userService.getLoggedInUserEmail();
		SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userEmail);
		String sessionId = null;
		if (sixthSenseUser != null)
			sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(),
					TenantContextHolder.getTenant());

		SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
		if (sixthSenseUser == null) {
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_JOB_PORTAL_DISABLED_MSG,
					ErrorHandler.SIXTH_SENSE_JOB_PORTAL_DISABLED);
		}
		String sources = sixthSenseUser.getSources();

		if (sources == null || sources.isEmpty()) {
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SOURCE_MISSING,
					ErrorHandler.SIXTH_SENSE_SOURCE_NOT_CONFIGURED);
		}

		if (sessionId == null || sessionId.isEmpty()) {
			sixthSenseUserCredential.setUserName(sixthSenseUser.getUserName());
			sixthSenseUserCredential.setPassword(sixthSenseUser.getPassword());
			// opening session for sixth sense
			RestResponse res = openSixthSenseSession(sixthSenseUserCredential);
			if(res!=null){
				if(!res.isSuccess()){
/*					logger.error("#### Sixth Sense open Session failure in sendMassMail() ### for tenant - "
							+ TenantContextHolder.getTenant());*/
					throw new RecruizWarnException("Sixth Sense open Session failure in sendMassMail()",
							"Sixth Sense open Session failure in sendMassMail()");
				}	
			}else{		
/*				logger.error("#### Sixth Sense open Session failure in sendMassMail() ### for tenant - "
						+ TenantContextHolder.getTenant());*/
				throw new RecruizWarnException("Sixth Sense open Session failure in sendMassMail()",
						"Sixth Sense open Session failure in sendMassMail()");
			}
		}

		try {
			ResponseEntity<MassMailSendResponse> response = sixthSenseRecruizClient.sendMassMail(massMailSendRequest);
			if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()) {
				return new RestResponse(RestResponse.SUCCESS, response.getBody());
			}else if (response != null && response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.FatalError.getCode()) {

				return new RestResponse(RestResponse.FAILED, SixthSenseErrorConstant.FatalError.getMessage());

			} else if (response != null && (response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.LoggedIn_Another_System.getCode())) {
				/*	String finalProfileHtml = profileHTMLWithUrlReplace(source,
						response.getBody().getMessageObject().getMessage(), keywordsToBeHighlighted);
				profileResponse.setProfileHtml(finalProfileHtml);*/


				SixthSenseUser sixthSense = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseCredential.setUserName(sixthSense.getUserName());
				sixthSenseCredential.setPassword(sixthSense.getPassword());
				RestResponse resetSessionResponse = resetSixthSenseSession(sixthSenseCredential);			
				if (resetSessionResponse != null && resetSessionResponse.isSuccess()) {
					return sendMassMail(massMailSendRequest);
				} else {
					if (resetSessionResponse != null)
						throw new RecruizWarnException(resetSessionResponse.getReason(),
								resetSessionResponse.getReason());
					else
						throw new RecruizWarnException("Sixth Sense reset session came with no response ",
								"Sixth Sense reset session failed");
				} 


			} else if (response != null && response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Session_Invalid.getCode()) {
				SixthSenseUser sixthSenseUserData = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseCredential.setUserName(sixthSenseUserData.getUserName());
				sixthSenseCredential.setPassword(sixthSenseUserData.getPassword());

				RestResponse res = openSixthSenseSession(sixthSenseCredential);
				if(res!=null){
					if(!res.isSuccess()){
/*						logger.error("#### Sixth Sense open Session failure in sendMassMail() ### for tenant - "
								+ TenantContextHolder.getTenant());*/
						throw new RecruizWarnException("Sixth Sense open Session failure in sendMassMail()",
								"Sixth Sense open Session failure in sendMassMail()");
					}	
				}else{		
/*					logger.error("#### Sixth Sense open Session failure in sendMassMail() ### for tenant - "
							+ TenantContextHolder.getTenant());*/
					throw new RecruizWarnException("Sixth Sense open Session failure in sendMassMail()",
							"Sixth Sense open Session failure in sendMassMail()");
				}

				return sendMassMail(massMailSendRequest);

			} else {
				return new RestResponse(RestResponse.FAILED, response.getBody().getMessageObject().getMessage());
			}

		} catch (Exception e) {
			logger.error("#### Sixth Sense Get all Users Error ### for tenant - " + TenantContextHolder.getTenant()
			+ e.getMessage(), e);
			return new RestResponse(RestResponse.FAILED, ErrorHandler.SIXTH_SENSE_SERVER_ERROR);

		}
	}

	/**
	 * Return group job portal search response
	 *
	 * @param sixthSenseAdvanceSearchRequest
	 * @param pageNo
	 * @return
	 * @throws Exception
	 */
	public SixthSenseGrouptResultResponse getSearchResult(SixthSenseAdvanceSearchRequest sixthSenseAdvanceSearchRequest,
			int pageNo) throws Exception {


		String userEmail = userService.getLoggedInUserEmail();
		SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userEmail);

		SixthSenseUser userData = sixthSenseUserRepository.findByEmail(userService.getLoggedInUserEmail());

		if(sixthSenseUser!=null)
		if (userData != null) {
			userData.setCaptchaStatus("0");
			userData.setLoggedUserEmail(userService.getLoggedInUserEmail());
			userData.setCaptchaSession(null);
			sixthSenseUserRepository.save(userData);
		} else {
			userData = new SixthSenseUser();
			userData.setCaptchaStatus("0");
			userData.setLoggedUserEmail(userService.getLoggedInUserEmail());
			userData.setPassword("");
			userData.setUserName(userService.getLoggedInUserEmail());
			userData.setCaptchaSession(null);
			sixthSenseUserRepository.save(userData);
		}

		String sessionId = null;
		if (sixthSenseUser != null)
			sessionId = sixthSenseSessionTokenStore.getSixthSenseSessionId(sixthSenseUser.getUserName(),
					TenantContextHolder.getTenant());

		SixthSenseUserCredential sixthSenseUserCredential = new SixthSenseUserCredential();
		if (sixthSenseUser == null) {
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_JOB_PORTAL_DISABLED_MSG,
					ErrorHandler.SIXTH_SENSE_JOB_PORTAL_DISABLED);
		}
		String sources = sixthSenseUser.getSources();

		if (sources == null || sources.isEmpty()) {
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SOURCE_MISSING,
					ErrorHandler.SIXTH_SENSE_SOURCE_NOT_CONFIGURED);
		}
		if (sixthSenseAdvanceSearchRequest.getSearchType() != null
				&& sixthSenseAdvanceSearchRequest.getSearchType().trim().equalsIgnoreCase("similarSearch")) {
			sources = sixthSenseAdvanceSearchRequest.getSources();
		}

		if (sessionId == null || sessionId.isEmpty()) {
			sixthSenseUserCredential.setUserName(sixthSenseUser.getUserName());
			sixthSenseUserCredential.setPassword(sixthSenseUser.getPassword());
			// opening session for sixth sense
			RestResponse res = openSixthSenseSession(sixthSenseUserCredential);
			if(res!=null){
				if(!res.isSuccess()){
/*					logger.error("#### Sixth Sense open Session failure in getSearchResult() ### for tenant - "
							+ TenantContextHolder.getTenant());*/
					throw new RecruizWarnException("Sixth Sense open Session failure in getSearchResult()",
							"Sixth Sense open Session failure in getSearchResult()");
				}	
			}else{		
/*				logger.error("#### Sixth Sense open Session failure in getSearchResult() ### for tenant - "
						+ TenantContextHolder.getTenant());*/
				throw new RecruizWarnException("Sixth Sense open Session failure in getSearchResult()",
						"Sixth Sense open Session failure in getSearchResult()");
			}
		}
		// update source based on user config in admin page

		if(sixthSenseAdvanceSearchRequest.getSources()!=null)
			sixthSenseAdvanceSearchRequest.setSources(sixthSenseAdvanceSearchRequest.getSources());

		int sourceCount = StringUtils.commaSeparateStringToList(sixthSenseUser.getSources()).size();
		sixthSenseAdvanceSearchRequest.setPageNo(pageNo);
		try {
			ResponseEntity<SixthSenseResultResponse> response = null;
			logger.error("second step send request and hit SixthSense APi userEmail = "+userService.getLoggedInUserEmail()+"time = "+new Date());
			response = sixthSenseRecruizClient.getSearchResult(sixthSenseAdvanceSearchRequest);
			logger.error("third step get response from SixthSense APi userEmail = "+userService.getLoggedInUserEmail()+"time = "+new Date());
			Pageable pageable = getPageable(pageNo, sourceCount); 

			if (response != null
					&& (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Success.getCode()
					|| response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Captcha_Occurred.getCode())) {

				if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Captcha_Occurred
						.getCode()) {
					userData = sixthSenseUserRepository.findByEmail(userService.getLoggedInUserEmail());// new
					userData.setCaptchaStatus("1");
					userData.setCaptchaSession(sessionId);
					sixthSenseUserRepository.save(userData);
/*					logger.error("Captcha Occured ==== " + ", TenantContextHolder ==" + TenantContextHolder.getTenant()
					+ ",  Login User Email == " + userService.getLoggedInUserEmail() + ", Login Username == "
					+ userService.getLoggedInUserName() + ", session_id == " + sessionId
					+ "   **********************************************");*/

					SixthSenseGrouptResultResponse res = getCombineResult(pageable, response);

					String resolveHtmlRow = res.getResolveHTMLRaw();
					String userIdString = "userID="+userService.getLoggedInUserEmail();
					String addUserIdSessionId = userIdString+";"+sessionId;

					resolveHtmlRow = resolveHtmlRow.replace(userIdString,addUserIdSessionId);
					

					res.setResolveHTMLRaw(resolveHtmlRow);
					return res;

				}
				return getCombineResult(pageable, response);

			}else if (response != null && response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.FatalError.getCode()) {
				SixthSenseGrouptResultResponse groupResultResponse = new SixthSenseGrouptResultResponse();
				groupResultResponse.setResponseCode(response.getBody().getMessageObject().getCode());
				groupResultResponse.setMessage(SixthSenseErrorConstant.FatalError.getMessage());
				return groupResultResponse;

			}  else if (response != null && (response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.LoggedIn_Another_System.getCode())) {
				/*	String finalProfileHtml = profileHTMLWithUrlReplace(source,
						response.getBody().getMessageObject().getMessage(), keywordsToBeHighlighted);
				profileResponse.setProfileHtml(finalProfileHtml);*/


				SixthSenseUser sixthSense = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseCredential.setUserName(sixthSense.getUserName());
				sixthSenseCredential.setPassword(sixthSense.getPassword());
				RestResponse resetSessionResponse = resetSixthSenseSession(sixthSenseCredential);			
				if (resetSessionResponse != null && resetSessionResponse.isSuccess()) {
					response = sixthSenseRecruizClient.getSearchResult(sixthSenseAdvanceSearchRequest);
					return getCombineResult(pageable, response);
				} else {
					if (resetSessionResponse != null)
						throw new RecruizWarnException(resetSessionResponse.getReason(),
								resetSessionResponse.getReason());
					else
						throw new RecruizWarnException("Sixth Sense reset session came with no response ",
								"Sixth Sense reset session failed");
				} 


			} else if (response != null && response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.Session_Invalid.getCode()) {
				SixthSenseUser sixthSenseUserData = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseCredential.setUserName(sixthSenseUserData.getUserName());
				sixthSenseCredential.setPassword(sixthSenseUserData.getPassword());

				RestResponse res = openSixthSenseSession(sixthSenseCredential);
				if(res!=null){
					if(!res.isSuccess()){
/*						logger.error("#### Sixth Sense open Session failure in getSearchResult() ### for tenant - "
								+ TenantContextHolder.getTenant());*/
						throw new RecruizWarnException("Sixth Sense open Session failure in getSearchResult()",
								"Sixth Sense open Session failure in getSearchResult()");
					}	
				}else{		
/*					logger.error("#### Sixth Sense open Session failure in getSearchResult() ### for tenant - "
							+ TenantContextHolder.getTenant());*/
					throw new RecruizWarnException("Sixth Sense open Session failure in getSearchResult()",
							"Sixth Sense open Session failure in getSearchResult()");
				}

				response = sixthSenseRecruizClient.getSearchResult(sixthSenseAdvanceSearchRequest);
				return getCombineResult(pageable, response);

			} else if (response != null && (response.getBody().getMessageObject()
					.getCode() == SixthSenseErrorConstant.OTP_Occurred.getCode())) {
				SixthSenseGrouptResultResponse groupResultResponse = new SixthSenseGrouptResultResponse();
				String message = " Automatic OTP Resolution Failed. Please Contact Administrator";
				String resMes = response.getBody().getMessageObject().getMessage();
				String finalMessage = resMes + message;
				groupResultResponse.setResolveHTMLRaw(finalMessage);
				groupResultResponse.setResponseCode(response.getBody().getMessageObject().getCode());

				return groupResultResponse;
			} else if (response.getBody().getMessageObject().getCode() == SixthSenseErrorConstant.Licence_Expired
					.getCode()) {
				throw new RecruizWarnException(SixthSenseErrorConstant.Licence_Expired.getMessage(),
						SixthSenseErrorConstant.Licence_Expired.getReason());
			} else {
				throw new RecruizWarnException(response.getBody().getMessageObject().getMessage(),
						response.getBody().getMessageObject().getMessage());
			}
		} catch (ResourceAccessException rae) {
/*			logger.error("#### #### Sixth Sense search result Resource Ex ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);*/
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
/*			logger.error("#### #### Sixth Sense search result Error ### for tenant - " + TenantContextHolder.getTenant()
			+ " " + e.getMessage(), e);*/
			throw new RecruizWarnException(e.getMessage(), e);
		}

	}

	/*
	 * private SixthSenseGrouptResultResponse getSearchResultWithDotNet(
	 * SixthSenseAdvanceSearchRequest sixthSenseAdvanceSearchRequest, int
	 * sourceCount, int pageNo, SixthSenseUserCredential sixthSenseUserCredential,
	 * String userEmail, SixthSenseUser sixthSenseUser) throws Exception {
	 * 
	 * try { ResponseEntity<String> resString =
	 * sixthSenseRecruizClient.getSearchResultInString(
	 * sixthSenseAdvanceSearchRequest); Pageable pageable = getPageable(pageNo,
	 * sourceCount);
	 * 
	 * List<String> dataInString =
	 * resString.getHeaders().get("SIXTH_SENSE_MSG_OBJ");
	 * dataInString.get(0).toString();
	 * 
	 * JSONParser parser = new JSONParser(); JSONObject json = (JSONObject)
	 * parser.parse(dataInString.get(0).toString()); JSONObject messageObject =
	 * (JSONObject) json.get("messageObject"); long co= (long)
	 * messageObject.get("code"); int code = (int) co; String message = (String)
	 * messageObject.get("message"); SixthSenseResultResponse responseData =
	 * convertFileDataIntoJson(resString.getBody());
	 * 
	 * 
	 * if (resString != null && (code == SixthSenseErrorConstant.Success.getCode()
	 * || code == SixthSenseErrorConstant.Captcha_Occurred.getCode())) {
	 * 
	 * if(code == SixthSenseErrorConstant.Captcha_Occurred.getCode()){
	 * SixthSenseUser sixthSenseUserNew =
	 * tenantResolverService.findSixthSenseUserByUserName(userEmail);//new
	 * sixthSenseUserNew.setCaptchaStatus("1");
	 * tenantResolverService.updateSixthSenseUser(sixthSenseUserNew); }
	 * 
	 * return getCombineResultForDotNet(pageable,
	 * responseData.getSearchResults(),code); } else if (resString != null && (code
	 * == SixthSenseErrorConstant.LoggedIn_Another_System.getCode() || code ==
	 * SixthSenseErrorConstant.Session_Invalid.getCode())) { // renew session for
	 * sixth sense if session is invalid
	 * sixthSenseUserCredential.setUserName(sixthSenseUser.getUserName());
	 * sixthSenseUserCredential.setPassword(sixthSenseUser.getPassword());
	 * RestResponse resetSessionResponse =
	 * resetSixthSenseSession(sixthSenseUserCredential); if (resetSessionResponse !=
	 * null && resetSessionResponse.isSuccess()) {
	 * sixthSenseRecruizClient.getSearchResult(sixthSenseAdvanceSearchRequest);
	 * 
	 * } } else if (resString != null && (code ==
	 * SixthSenseErrorConstant.OTP_Occurred.getCode())) {
	 * SixthSenseGrouptResultResponse groupResultResponse = new
	 * SixthSenseGrouptResultResponse(); String messag =
	 * " Automatic OTP Resolution Failed. Please Contact Administrator"; String
	 * resMes = message; String finalMessage = resMes+messag;
	 * groupResultResponse.setResolveHTMLRaw(finalMessage);
	 * groupResultResponse.setResponseCode(code);
	 * 
	 * return groupResultResponse; } else if (code ==
	 * SixthSenseErrorConstant.Licence_Expired.getCode()) { throw new
	 * RecruizWarnException(SixthSenseErrorConstant.Licence_Expired.getMessage() ,
	 * SixthSenseErrorConstant.Licence_Expired.getReason()); } else { throw new
	 * RecruizWarnException(message, message); } } catch (ResourceAccessException
	 * rae) { logger. error(
	 * "#### #### Sixth Sense search result Resource Ex ### for tenant - " +
	 * TenantContextHolder.getTenant() + " " + rae.getMessage(), rae); throw new
	 * RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
	 * ErrorHandler.SIXTH_SENSE_SERVER_DOWN); } catch (RecruizWarnException rezEx) {
	 * throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant()); }
	 * catch (Exception e) { logger.error(
	 * "#### #### Sixth Sense search result Error ### for tenant - " +
	 * TenantContextHolder.getTenant() + " " + e.getMessage(), e); throw new
	 * RecruizWarnException(e.getMessage(), e); } return null; }
	 */
	private Pageable getPageable(int pageNo, int sourceCount) {
		if (pageNo != 0)
			pageNo = pageNo - 1;
		final Pageable pageable = pageableService.searchPageRequest(pageNo, sourceCount * SIXTH_SENSE_PAGE_SIZE);
		return pageable;
	}

	private SixthSenseGrouptResultResponse getCombineResult(final Pageable pageable,
			ResponseEntity<SixthSenseResultResponse> response) throws RecruizWarnException {

		SixthSenseGrouptResultResponse groupResultResponse = new SixthSenseGrouptResultResponse();
		List<SixthSenseSearchResultDTO> searchResults = response.getBody().getSearchResults();
		Map<String, Page<SixthSenseResultDTO>> resultMap = new HashMap<String, Page<SixthSenseResultDTO>>();
		List<SixthSenseResultDTO> totalSearchResults = new ArrayList<SixthSenseResultDTO>();
		int totalHitsCount = 0;
		for (SixthSenseSearchResultDTO searchResult : searchResults) {

			switch (searchResult.getSource()) {

			case IntegrationConstants.NAUKRI_SOURCE:
				// generally this condition for captcha
				if (searchResult.getHits() == -1) {
					logger.warn("\n\n\n Logging search result on hit -1 \n\n\n\n" + searchResult + "n\n\nmesssage \n\n"
							+ searchResult.getResultMessage());
					groupResultResponse.setResolved(false);
					groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					groupResultResponse.setResolveHTMLRaw(v2captchHTMLUrlReplace(searchResult.getResultMessage()));
					groupResultResponse.setResolveSource(IntegrationConstants.NAUKRI_SOURCE);
					groupResultResponse
					.setHiddenParameterMap(getHiddenParamaterForCaptcha(searchResult.getResultMessage()));
				} else {
					totalSearchResults.addAll(searchResult.getResultData());
					totalHitsCount += searchResult.getHits();
				}
				break;

			case IntegrationConstants.MONSTER_SOURCE:
				// generally this condition for captcha
				if (searchResult.getHits() == -1) {
					groupResultResponse.setResolved(false);
					groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					byte[] captchImageContent = getCaptchaImageContent(searchResult.getSource(),
							searchResult.getResultMessage());
					if (null == captchImageContent || captchImageContent.length <= 0) {
						groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_OTP);
						groupResultResponse.setResolveHTMLRaw(searchResult.getResultMessage());
					} else {
						groupResultResponse.setResolveHTML(captchImageContent);
					}

					groupResultResponse.setResolveSource(IntegrationConstants.MONSTER_SOURCE);
				} else {
					totalSearchResults.addAll(searchResult.getResultData());
					totalHitsCount += searchResult.getHits();
				}
				break;

			case IntegrationConstants.TIMES_SOURCE:
				// generally this condition for captcha
				if (searchResult.getHits() == -1) {
					groupResultResponse.setResolved(false);
					groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					groupResultResponse.setResolveHTML(
							getCaptchaImageContent(searchResult.getSource(), searchResult.getResultMessage()));
					groupResultResponse.setResolveSource(IntegrationConstants.TIMES_SOURCE);
				} else {
					totalSearchResults.addAll(searchResult.getResultData());
					totalHitsCount += searchResult.getHits();
				}
				break;

			case IntegrationConstants.SHINE_SOURCE:
				// generally this condition for captcha
				if (searchResult.getHits() == -1) {
					groupResultResponse.setResolved(false);
					groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					groupResultResponse.setResolveHTML(
							getCaptchaImageContent(searchResult.getSource(), searchResult.getResultMessage()));
					groupResultResponse.setResolveSource(IntegrationConstants.SHINE_SOURCE);
				} else {
					totalSearchResults.addAll(searchResult.getResultData());
					totalHitsCount += searchResult.getHits();
				}
				break;
			}
		}
		resultMap.put(IntegrationConstants.PORTAL_SEARCH,
				new PageImpl<SixthSenseResultDTO>(totalSearchResults, pageable, totalHitsCount));
		groupResultResponse.setSearchResultMap(resultMap);
		groupResultResponse.setResponseCode(response.getBody().getMessageObject().getCode());
		groupResultResponse.setMessage(response.getBody().getMessageObject().getMessage());
		return groupResultResponse;
	}

	private SixthSenseGrouptResultResponse getCombineResultForDotNet(final Pageable pageable,
			List<SixthSenseSearchResultDTO> list, int code) throws RecruizWarnException {

		SixthSenseGrouptResultResponse groupResultResponse = new SixthSenseGrouptResultResponse();
		List<SixthSenseSearchResultDTO> searchResults = list;
		Map<String, Page<SixthSenseResultDTO>> resultMap = new HashMap<String, Page<SixthSenseResultDTO>>();
		List<SixthSenseResultDTO> totalSearchResults = new ArrayList<SixthSenseResultDTO>();
		int totalHitsCount = 0;
		for (SixthSenseSearchResultDTO searchResult : searchResults) {

			switch (searchResult.getSource()) {

			case IntegrationConstants.NAUKRI_SOURCE:
				// generally this condition for captcha
				if (searchResult.getHits() == -1) {
					logger.warn("\n\n\n Logging search result on hit -1 \n\n\n\n" + searchResult + "n\n\nmesssage \n\n"
							+ searchResult.getResultMessage());
					groupResultResponse.setResolved(false);
					groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					groupResultResponse.setResolveHTMLRaw(v2captchHTMLUrlReplace(searchResult.getResultMessage()));
					groupResultResponse.setResolveSource(IntegrationConstants.NAUKRI_SOURCE);
					groupResultResponse
					.setHiddenParameterMap(getHiddenParamaterForCaptcha(searchResult.getResultMessage()));
				} else {
					totalSearchResults.addAll(searchResult.getResultData());
					totalHitsCount += searchResult.getHits();
				}
				break;

			case IntegrationConstants.MONSTER_SOURCE:
				// generally this condition for captcha
				if (searchResult.getHits() == -1) {
					groupResultResponse.setResolved(false);
					groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					byte[] captchImageContent = getCaptchaImageContent(searchResult.getSource(),
							searchResult.getResultMessage());
					if (null == captchImageContent || captchImageContent.length <= 0) {
						groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_OTP);
						groupResultResponse.setResolveHTMLRaw(searchResult.getResultMessage());
					} else {
						groupResultResponse.setResolveHTML(captchImageContent);
					}

					groupResultResponse.setResolveSource(IntegrationConstants.MONSTER_SOURCE);
				} else {
					totalSearchResults.addAll(searchResult.getResultData());
					totalHitsCount += searchResult.getHits();
				}
				break;

			case IntegrationConstants.TIMES_SOURCE:
				// generally this condition for captcha
				if (searchResult.getHits() == -1) {
					groupResultResponse.setResolved(false);
					groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					groupResultResponse.setResolveHTML(
							getCaptchaImageContent(searchResult.getSource(), searchResult.getResultMessage()));
					groupResultResponse.setResolveSource(IntegrationConstants.TIMES_SOURCE);
				} else {
					totalSearchResults.addAll(searchResult.getResultData());
					totalHitsCount += searchResult.getHits();
				}
				break;

			case IntegrationConstants.SHINE_SOURCE:
				// generally this condition for captcha
				if (searchResult.getHits() == -1) {
					groupResultResponse.setResolved(false);
					groupResultResponse.setResolveType(IntegrationConstants.RESOLVE_TYPE_CAPTCHA);
					groupResultResponse.setResolveHTML(
							getCaptchaImageContent(searchResult.getSource(), searchResult.getResultMessage()));
					groupResultResponse.setResolveSource(IntegrationConstants.SHINE_SOURCE);
				} else {
					totalSearchResults.addAll(searchResult.getResultData());
					totalHitsCount += searchResult.getHits();
				}
				break;
			}
		}
		resultMap.put(IntegrationConstants.PORTAL_SEARCH,
				new PageImpl<SixthSenseResultDTO>(totalSearchResults, pageable, totalHitsCount));
		groupResultResponse.setSearchResultMap(resultMap);
		groupResultResponse.setResponseCode(code);
		return groupResultResponse;
	}

	@Transactional
	public IntegrationProfileDetails storeSixthSenseIntegrationObject(Map<String, String> profileDetails) {
		IntegrationProfileDetails sixthSenseIntegrationDetails = null;
		String defaulOrgEmail = StringUtils.getDefaultOrgEmail();
		sixthSenseIntegrationDetails = integrationProfileService.getDetailsByEmailAndModuleType(defaulOrgEmail,
				IntegrationConstants.SIXTH_SENSE_APP_ID);
		if (sixthSenseIntegrationDetails == null) {
			sixthSenseIntegrationDetails = new IntegrationProfileDetails();
		}

		sixthSenseIntegrationDetails.setUserEmail(defaulOrgEmail);
		sixthSenseIntegrationDetails.setIntegrationDetails(profileDetails);
		sixthSenseIntegrationDetails.setIntegrationModuleType(IntegrationConstants.SIXTH_SENSE_APP_ID);
		integrationProfileService.save(sixthSenseIntegrationDetails);
		return sixthSenseIntegrationDetails;
	}

	@Transactional(readOnly = true)
	public IntegrationProfileDetails getSixthSenseIntegration() {
		String defaulOrgEmail = StringUtils.getDefaultOrgEmail();
		IntegrationProfileDetails sixthSenseIntegrationDetails = integrationProfileService
				.getDetailsByEmailAndModuleType(defaulOrgEmail, IntegrationConstants.SIXTH_SENSE_APP_ID);
		if (sixthSenseIntegrationDetails != null) {
			return sixthSenseIntegrationDetails;
		}
		return null;
	}

	@Transactional
	public void deleteSixthSenseIntegration() {
		String defaulOrgEmail = StringUtils.getDefaultOrgEmail();
		IntegrationProfileDetails sixthSenseIntegrationDetails = integrationProfileService
				.getDetailsByEmailAndModuleType(defaulOrgEmail, IntegrationConstants.SIXTH_SENSE_APP_ID);
		if (sixthSenseIntegrationDetails != null) {
			integrationProfileService.delete(sixthSenseIntegrationDetails);
		}
	}

	public List<SixthSenseUser> constructSixthSenseUsers(List<SixthSenseJobPortalDTO> sixthSenseJobPortalDTOs) {

		List<SixthSenseUser> sixthSenseUsers = new ArrayList<SixthSenseUser>();
		for (SixthSenseJobPortalDTO item : sixthSenseJobPortalDTOs) {
			User user = tenantResolverService.findUserByEmail(item.getEmail());
			if (user != null) {
				SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(user.getEmail());
				if (sixthSenseUser == null) {
					sixthSenseUser = new SixthSenseUser();
					sixthSenseUser.setUserName(user.getEmail());
					sixthSenseUser.setPassword(StringUtils.randomString());
					sixthSenseUser.setUser(user);
				}
				sixthSenseUser.setUsageType(item.getUsageType());
				sixthSenseUser.setViewCount(item.getViewCount());
				sixthSenseUser.setSources(StringUtils.commaSeparate(item.getSources()));
				sixthSenseUsers.add(sixthSenseUser);
			}
		}
		return sixthSenseUsers;
	}

	public Map<String, Object> getDropdownValues() {

		Map<String, Object> dropdownListMap = new HashMap<String, Object>();

		List<BaseDTO> designTypeList = new ArrayList<BaseDTO>();
		List<BaseDTO> excludeCompTypeList = new ArrayList<BaseDTO>();
		List<BaseDTO> includeCompTypeList = new ArrayList<BaseDTO>();
		List<BaseDTO> jobStatusList = new ArrayList<BaseDTO>();
		List<BaseDTO> jobTypeList = new ArrayList<BaseDTO>();
		List<BaseDTO> noticePeriodList = new ArrayList<BaseDTO>();
		List<BaseDTO> ppgDegreeTypeList = new ArrayList<BaseDTO>();
		List<BaseDTO> pgDegreeTypeList = new ArrayList<BaseDTO>();
		List<BaseDTO> ugDegreeTypeList = new ArrayList<BaseDTO>();
		List<BaseDTO> resumeFreshnessList = new ArrayList<BaseDTO>();
		List<BaseDTO> searchInList = new ArrayList<BaseDTO>();
		List<BaseDTO> searchTypeList = new ArrayList<BaseDTO>();
		List<BaseDTO> showList = new ArrayList<BaseDTO>();
		List<BaseDTO> sortByList = new ArrayList<BaseDTO>();
		List<BaseDTO> sourceList = new ArrayList<BaseDTO>();
		List<BaseDTO> univTypeList = new ArrayList<BaseDTO>();
		List<BaseDTO> yopTypeList = new ArrayList<BaseDTO>();

		for (SixthSenseDesignationType designTypeType : SixthSenseDesignationType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(designTypeType.getCode()));
			baseDTO.setValue(designTypeType.getDisplayName());
			designTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_DESIGNATION_TYPE, designTypeList);

		for (SixthSenseExcludeCompanyType excludeCompType : SixthSenseExcludeCompanyType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(excludeCompType.getCode()));
			baseDTO.setValue(excludeCompType.getDisplayName());
			excludeCompTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_EXCLUDE_COMP_TYPE, excludeCompTypeList);

		for (SixthSenseExcludeCompanyType includeCompType : SixthSenseExcludeCompanyType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(includeCompType.getCode()));
			baseDTO.setValue(includeCompType.getDisplayName());
			includeCompTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_INCLUDE_COMP_TYPE, includeCompTypeList);

		for (SixthSenseJobStatus jobStatus : SixthSenseJobStatus.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(jobStatus.getCode()));
			baseDTO.setValue(jobStatus.getDisplayName());
			jobStatusList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_JOB_STATUS, jobStatusList);

		for (SixthSenseJobType jobType : SixthSenseJobType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(jobType.getCode()));
			baseDTO.setValue(jobType.getDisplayName());
			jobTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_JOB_TYPE, jobTypeList);

		for (AdvancedSearchNoticePeriod noticePeriod : AdvancedSearchNoticePeriod.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(noticePeriod.getCode()));
			baseDTO.setValue(noticePeriod.getDisplayName());
			noticePeriodList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_NOTICE_PERIOD, noticePeriodList);

		for (SixthSensePPGDegreeType ppgDegreeType : SixthSensePPGDegreeType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(ppgDegreeType.getCode()));
			baseDTO.setValue(ppgDegreeType.getDisplayName());
			ppgDegreeTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_PPG_DEGREE_TYPE, ppgDegreeTypeList);

		for (SixthSensePGDegreeType pgDegreeType : SixthSensePGDegreeType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(pgDegreeType.getCode()));
			baseDTO.setValue(pgDegreeType.getDisplayName());
			pgDegreeTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_PG_DEGREE_TYPE, pgDegreeTypeList);

		for (SixthSenseUGDegreeType ugDegreeType : SixthSenseUGDegreeType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(ugDegreeType.getCode()));
			baseDTO.setValue(ugDegreeType.getDisplayName());
			ugDegreeTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_UG_DEGREE_TYPE, ugDegreeTypeList);

		for (ResumeFreshness resumeFreshness : ResumeFreshness.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(resumeFreshness.getCode());
			baseDTO.setValue(resumeFreshness.getDisplayName());
			resumeFreshnessList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_RESUME_FRESHNESS, resumeFreshnessList);

		for (SixthSenseShow searchIn : SixthSenseShow.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(searchIn.getCode()));
			baseDTO.setValue(searchIn.getDisplayName());
			searchInList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_SHOW, searchInList);

		for (AdvancedSearchType searchType : AdvancedSearchType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(searchType.name());
			baseDTO.setValue(searchType.getDisplayName());
			searchTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_SEARCH_TYPE, searchTypeList);

		for (AdvancedSearchIn show : AdvancedSearchIn.values()) {
			// removing all keyword since UI not required
			if (show.getCode() > 0) {
				BaseDTO baseDTO = new BaseDTO();
				baseDTO.setId(Integer.toString(show.getCode()));
				baseDTO.setValue(show.getDisplayName());
				showList.add(baseDTO);
			}
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_SEARCH_IN, showList);

		for (SixthSenseSortBy sortBy : SixthSenseSortBy.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(sortBy.getCode()));
			baseDTO.setValue(sortBy.getDisplayName());
			sortByList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_SORT_BY, sortByList);

		for (SixthSenseSource source : SixthSenseSource.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(source.name());
			baseDTO.setValue(source.getDisplayName());
			sourceList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_SOURCE, sourceList);

		for (SixthSenseUniversityType univType : SixthSenseUniversityType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(univType.getCode()));
			baseDTO.setValue(univType.getDisplayName());
			univTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_UNIVERSITY_TYPE, univTypeList);

		for (SixthSenseYOPType yopType : SixthSenseYOPType.values()) {
			BaseDTO baseDTO = new BaseDTO();
			baseDTO.setId(Integer.toString(yopType.getCode()));
			baseDTO.setValue(yopType.getDisplayName());
			yopTypeList.add(baseDTO);
		}
		dropdownListMap.put(IntegrationConstants.DROPDOWN_YOP_TYPE, yopTypeList);

		List<BaseDTO> industryList = sixthSenseIndustryService.getIndustryList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_INDUSTRY, industryList);

		List<SixthSenseBaseDTO> cityList = sixthSenseCityService.getCityList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_CITY, cityList);

		List<BaseDTO> funcAreaList = sixthSenseFuncAreaService.getFuncAreaList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_FUNCTIONAL_AREA, funcAreaList);

		List<SixthSenseBaseDTO> roleList = sixthSenseFuncAreaRoleService.getRoleList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_FUNCTIONAL_AREA_ROLE, roleList);

		List<BaseDTO> ppgDegreeList = sixthSensePPGDegreeService.getPPGDegreeList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_PPG_DEGREE, ppgDegreeList);

		List<SixthSenseBaseDTO> ppgDegreeSpecList = sixthSensePPGDegreeSpecService.getPPGDegreeSpecList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_PPG_DEGREE_SPEC, ppgDegreeSpecList);

		List<BaseDTO> pgDegreeList = sixthSensePGDegreeService.getPGDegreeList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_PG_DEGREE, pgDegreeList);

		List<SixthSenseBaseDTO> pgDegreeSpecList = sixthSensePGDegreeSpecService.getPGDegreeSpecList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_PG_DEGREE_SPEC, pgDegreeSpecList);

		List<BaseDTO> ugDegreeList = sixthSenseUGDegreeService.getUGDegreeList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_UG_DEGREE, ugDegreeList);

		List<SixthSenseBaseDTO> ugDegreeSpecList = sixthSenseUGDegreeSpecService.getUGDegreeSpecList();
		dropdownListMap.put(IntegrationConstants.DROPDOWN_UG_DEGREE_SPEC, ugDegreeSpecList);

		return dropdownListMap;
	}

	// to delete sixth sense user
	public void deleteSixthSenseUser(SixthSenseDeleteUserDTO deleteUserDTO) throws RecruizWarnException {

		if (deleteUserDTO.getUsers() == null || deleteUserDTO.getUsers().isEmpty())
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_USER_DELETION_ERROR,
					ErrorHandler.SIXTH_SENSE_USER_DELETION_ERROR);

		List<SixthSenseJobPortalDTO> sixthSenseJobPortalDTOs = new ArrayList<>();

		for (String userEmail : deleteUserDTO.getUsers()) {
			SixthSenseJobPortalDTO dto = new SixthSenseJobPortalDTO();
			dto.setEmail(userEmail);
			sixthSenseJobPortalDTOs.add(dto);
		}

		List<SixthSenseUser> sixthSenseUsers = constructSixthSenseUsers(sixthSenseJobPortalDTOs);

		List<String> successResultList = deleteUser(deleteUserDTO);

		List<String> addedEmails = new ArrayList<String>();
		for (SixthSenseUser sixthSenseUser : sixthSenseUsers) {
			if (sixthSenseUser != null && sixthSenseUser.getUserName() != null) {
				if (successResultList.contains(sixthSenseUser.getUserName())) {
					addedEmails.add(sixthSenseUser.getUserName());
					tenantResolverService.deleteSixthSenseUser(sixthSenseUser);
				}
			}
		}
	}

	public String getSimilarProfileHtml(SixthSenseCandidateProfileDTO candidateDownloadCVRequest) throws Throwable {
		SixthSenseUser sixthSenseUser = tenantResolverService
				.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
		try {

			ResponseEntity<byte[]> response = sixthSenseRecruizClient
					.downloadCandidateResume(candidateDownloadCVRequest);

			SixthSenseMessageObject messageObj = sixthSenseRecruizClient.postResponseHeader(response.getHeaders());

			if (messageObj.getCode() == SixthSenseErrorConstant.Success.getCode()) {
				// writing binary array into file
				String bodyStrng = new String(response.getBody(), "UTF-8");
				return bodyStrng;
			}else if (response != null && messageObj.getCode() == SixthSenseErrorConstant.FatalError.getCode()) {

				return SixthSenseErrorConstant.FatalError.getMessage();

			}  else if (response != null && (messageObj
					.getCode() == SixthSenseErrorConstant.LoggedIn_Another_System.getCode())) {
				/*	String finalProfileHtml = profileHTMLWithUrlReplace(source,
						response.getBody().getMessageObject().getMessage(), keywordsToBeHighlighted);
				profileResponse.setProfileHtml(finalProfileHtml);*/


				SixthSenseUser sixthSense = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseCredential.setUserName(sixthSense.getUserName());
				sixthSenseCredential.setPassword(sixthSense.getPassword());
				RestResponse resetSessionResponse = resetSixthSenseSession(sixthSenseCredential);			
				if (resetSessionResponse != null && resetSessionResponse.isSuccess()) {
					return	getSimilarProfileHtml(candidateDownloadCVRequest);
				} else {
					if (resetSessionResponse != null)
						throw new RecruizWarnException(resetSessionResponse.getReason(),
								resetSessionResponse.getReason());
					else
						throw new RecruizWarnException("Sixth Sense reset session came with no response ",
								"Sixth Sense reset session failed");
				} 


			} else if (response != null && messageObj
					.getCode() == SixthSenseErrorConstant.Session_Invalid.getCode()) {
				SixthSenseUser sixthSenseUserData = tenantResolverService
						.findSixthSenseUserByUserName(userService.getLoggedInUserEmail());
				SixthSenseUserCredential sixthSenseCredential = new SixthSenseUserCredential();
				// renew session for sixth sense if session is invalid
				sixthSenseCredential.setUserName(sixthSenseUserData.getUserName());
				sixthSenseCredential.setPassword(sixthSenseUserData.getPassword());

				RestResponse res = openSixthSenseSession(sixthSenseCredential);
				if(res!=null){
					if(!res.isSuccess()){
/*						logger.error("#### Sixth Sense open Session failure in downloadCandidateResume() ### for tenant - "
								+ TenantContextHolder.getTenant());*/
						throw new RecruizWarnException("Sixth Sense open Session failure in downloadCandidateResume()",
								"Sixth Sense open Session failure in downloadCandidateResume()");
					}	
				}else{		
/*					logger.error("#### Sixth Sense open Session failure in downloadCandidateResume() ### for tenant - "
							+ TenantContextHolder.getTenant());*/
					throw new RecruizWarnException("Sixth Sense open Session failure in downloadCandidateResume()",
							"Sixth Sense open Session failure in downloadCandidateResume()");
				}

				return	getSimilarProfileHtml(candidateDownloadCVRequest);
			} else {
				throw new RecruizWarnException(messageObj.getMessage(), messageObj.getMessage());
			}
		} catch (ResourceAccessException rae) {
/*			logger.error("#### Sixth Sense renew Session Resource Access Ex ### for tenant - "
					+ TenantContextHolder.getTenant() + " " + rae.getMessage(), rae);*/
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_SERVER_DOWN_MSG,
					ErrorHandler.SIXTH_SENSE_SERVER_DOWN);
		} catch (RecruizWarnException rezEx) {
			throw new RecruizWarnException(rezEx.getMessage(), rezEx.getErrConstant());
		} catch (Exception e) {
/*			logger.error("#### Sixth Sense renew Session Error ### for tenant - " + TenantContextHolder.getTenant()
			+ " " + e.getMessage(), e);*/
			throw new RecruizWarnException(e.getMessage(), e);
		}
	}

	public RestResponse getSixthSenseCaptchaStatus() throws RecruizWarnException {

		RestResponse restResponse = null;

		try {
			SixthSenseUser userData = sixthSenseUserRepository.findByEmail(userService.getLoggedInUserEmail());

			if (userData.getCaptchaStatus().equals("0")) {
				restResponse = new RestResponse(RestResponse.SUCCESS, null);
			} else {
				restResponse = new RestResponse(RestResponse.FAILED,
						"Job portal server error, please try after sometime");
				Thread.sleep(5000);
			}

		} catch (Exception e) {
			throw new RecruizWarnException(ErrorHandler.SIXTH_SENSE_JOB_PORTAL_DISABLED_MSG,
					ErrorHandler.SIXTH_SENSE_JOB_PORTAL_DISABLED);
		}
		return restResponse;
	}

	/*
	 * private static void writeUsingFileWriter(String data) { File file = new
	 * File("E://Narinder_Tanwar/FileWriter.txt"); FileWriter fr = null; try { fr =
	 * new FileWriter(file); fr.write(data); } catch (IOException e) {
	 * e.printStackTrace(); }finally{ //close resources try { fr.close(); } catch
	 * (IOException e) { e.printStackTrace(); } } }
	 */
	public Candidate setCandidateInformation(String htmlData, Candidate candidate) {
		String arr2[] = htmlData.split("<label>Notice Period<\\/label> <div class=\"desc\">");
		// writeUsingFileWriter(arr2[0]);
		if (arr2.length > 1) {
			String newData = arr2[1];
			String notice[] = newData.split("<\\/div>");
			String noticeData = notice[0];

			noticeData = noticeData.trim();

			if (noticeData == null)
				return candidate;

			if (noticeData.equalsIgnoreCase("0") || noticeData.equals("") || noticeData.isEmpty()) {
				candidate.setNoticePeriod(0);
				return candidate;
			}

			arr2 = noticeData.split(" ");

			if (arr2[1].equalsIgnoreCase("days")) {

				int days = Integer.parseInt(arr2[0]);
				if (days <= 15) {
					candidate.setNoticePeriod(15);
				} else {
					candidate.setNoticePeriod(days);
				}

				return candidate;
			} else if (arr2[1].equalsIgnoreCase("month")) {
				candidate.setNoticePeriod(30);
				return candidate;
			} else if (arr2[1].equalsIgnoreCase("months")) {
				int days = Integer.parseInt(arr2[0]);
				if (days == 2) {
					candidate.setNoticePeriod(60);
				} else if (days == 3) {
					candidate.setNoticePeriod(90);
				} else {
					candidate.setNoticePeriod(100);
				}
				return candidate;
			}

		}
		candidate.setNoticePeriod(0);
		return candidate;

	}

	private static String cleanTextContent(String text) {
		// strips off all non-ASCII characters
		text = text.replaceAll("[^\\x00-\\x7F]", "");

		// erases all the ASCII control characters
		text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

		// removes non-printable characters from Unicode
		text = text.replaceAll("\\p{C}", "");

		text = text.replaceAll("[^\\p{ASCII}]", "");

		return text.trim();
	}

	public String getEmailDomain() {

		String domainName = "";

		String userEmail = userService.getLoggedInUserEmail();
		if (!userEmail.isEmpty() && !userEmail.equalsIgnoreCase(null)) {
			String[] parts = userEmail.split("@");
			if (parts.length == 2) {
				domainName = parts[1];
			}

		}

		return domainName;

	}

	public RestResponse getUserSources() {
		try{
			String userEmail = userService.getLoggedInUserEmail();
			SixthSenseUser sixthSenseUser = tenantResolverService.findSixthSenseUserByUserName(userEmail);
			if(sixthSenseUser!=null)
				return	new RestResponse(RestResponse.SUCCESS, sixthSenseUser.getSources());

			return new RestResponse(RestResponse.FAILED, "User not found");
		}catch(Exception e){
			e.printStackTrace();
		}

		return new RestResponse(RestResponse.FAILED, "Internal server error");
	}

}
