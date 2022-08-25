package com.bbytes.recruiz.service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectContactInfo;
import com.bbytes.recruiz.domain.ProspectPosition;
import com.bbytes.recruiz.enums.ProspectActivityType;
import com.bbytes.recruiz.enums.ProspectStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.ProspectRepository;
import com.bbytes.recruiz.rest.dto.models.ClientDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectContactInfoDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectDTO;
import com.bbytes.recruiz.rest.dto.models.ProspectPostionDTO;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class ProspectService extends AbstractService<Prospect, Long> {

	private static final Logger logger = LoggerFactory.getLogger(ProspectService.class);

	private ProspectRepository prospectRepository;

	@Autowired
	private UserService userService;

	@Autowired
	private ProspectActivityService prospectActivityService;

	@Autowired
	private ClientService clientService;

	@Autowired
	private ProspectContactInfoService prospectContactInfoService;

	@Autowired
	private ProspectPositionService prospectPositionService;

	@Autowired
	public ProspectService(ProspectRepository prospectRepository) {
		super(prospectRepository);
		this.prospectRepository = prospectRepository;
	}

	@Transactional(readOnly = true)
	public Prospect getProspectById(long prospectId) throws RecruizException {
		return prospectRepository.findOne(prospectId);

	}

	@Transactional(readOnly = true)
	public Prospect getProspectByMobile(String mobile) throws RecruizException {
		return prospectRepository.findByMobile(mobile);
	}

	public Prospect getProspectByClient(Client client) throws RecruizException {
		return prospectRepository.findByClient(client);
	}

	@Transactional(readOnly = true)
	public Prospect getProspectByEmail(String email) throws RecruizException {
		return prospectRepository.findByEmail(email);
	}

	@Transactional(readOnly = true)
	public Prospect getProspectByCompanyName(String companyNmae) throws RecruizException {
		return prospectRepository.findByCompanyName(companyNmae);
	}

	public Boolean checkDummyByCompanyName(String companyName) throws RecruizException {
		Prospect prospect = prospectRepository.findByCompanyName(companyName);
		return prospect.isDummy() == true ? true : false;
	}

	@Transactional(readOnly = true)
	public List<Prospect> getDummyProspect() throws RecruizException {
		return prospectRepository.findByDummy(true);
	}

	@Transactional(readOnly = true)
	public boolean isProspectExist(String name) {
		return prospectRepository.findByName(name) == null ? false : true;
	}

	@Transactional(readOnly = true)
	public boolean isProspectCompanyNameExist(String companyName) {
		return prospectRepository.findByCompanyName(companyName) == null ? false : true;
	}

	@Transactional(readOnly = true)
	public boolean isProspectEmailExist(String email) {
		if (email == null)
			return false;
		return prospectRepository.findByEmail(email) == null ? false : true;
	}

	@Transactional(readOnly = true)
	public boolean isProspectMobileExist(String mobile) {
		if (mobile == null)
			return false;
		return prospectRepository.findByMobile(mobile) == null ? false : true;
	}

	@Transactional(readOnly = true)
	public boolean isProspectExist(long prospectId) {
		return findOne(prospectId) == null ? false : true;
	}

	@Transactional(readOnly = true)
	public Page<Prospect> getAllProspectForSearchIndex(Pageable pageable) {
		return prospectRepository.findAll(pageable);
	}

	/*******************************
	 * To get all prospect(pageble)*
	 *******************************
	 * @param pageable
	 * @return Page<Prospect>
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public Page<Prospect> getAllProspect(Pageable pageable) throws RecruizException {
		return prospectRepository.findAll(pageable);
	}

	/*******************************
	 * To get all prospect by owner (pageble, owner)*
	 *******************************
	 * @param pageable
	 * @return Page<Prospect>
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public Page<Prospect> getAllProspectByOwner(Pageable pageable, String ownerEmail) throws RecruizException {
		return prospectRepository.findDistinctByOwner(ownerEmail, pageable);
	}

	/********************************************************
	 * To Validate ProspectStatusEnum contains status or not*
	 ********************************************************
	 * @param status
	 * @return boolean
	 */
	public static boolean containsProspectStatus(String status) {
		for (ProspectStatus prospectStatus : ProspectStatus.values()) {
			if (prospectStatus.name().equalsIgnoreCase(status)) {
				return true;
			}
		}
		return false;
	}

	/**************************************************
	 * To add new prospect ***************************
	 * ************************************************
	 * 
	 * @param prospectDTO
	 * @return Prospect
	 * @throws RecruizException
	 */
	@Transactional
	public Prospect addProspect(ProspectDTO prospectDTO) throws RecruizException {

		Prospect nwProspect = null;
		if (prospectDTO == null)
			throw new RecruizException(ErrorHandler.PROSPECT_CAN_NOT_EMPTY, ErrorHandler.PROSPECT_CAN_NOT_EMPTY);

		if (isProspectCompanyNameExist(prospectDTO.getCompanyName()))
			throw new RecruizException(ErrorHandler.PROSPECT_COMPANY_ALREADY_EXIST, ErrorHandler.PROSPECT_COMPANY_ALREADY_EXIST);

		if (isProspectEmailExist(prospectDTO.getEmail()))
			throw new RecruizException(ErrorHandler.PROSPECT_EMAIL_ALREADY_EXIST, ErrorHandler.PROSPECT_EMAIL_ALREADY_EXIST);

		if (isProspectMobileExist(prospectDTO.getMobile()))
			throw new RecruizException(ErrorHandler.PROSPECT_MOBILE_ALREADY_EXIST, ErrorHandler.PROSPECT_MOBILE_ALREADY_EXIST);

		if (prospectDTO.getCompanyName() == null || prospectDTO.getCompanyName().isEmpty())
			throw new RecruizException(ErrorHandler.PROSPECT_COMPANY_NAME_CAN_NOT_EMPTY, ErrorHandler.PROSPECT_COMPANY_NAME_CAN_NOT_EMPTY);

		if (prospectDTO.getName() == null || prospectDTO.getName().isEmpty())
			throw new RecruizException(ErrorHandler.PROSPECT_NAME_CAN_NOT_EMPTY, ErrorHandler.PROSPECT_NAME_CAN_NOT_EMPTY);

		if (prospectDTO.getLocation() == null || prospectDTO.getLocation().isEmpty())
			throw new RecruizException(ErrorHandler.PROSPECT_LOCATION_CAN_NOT_EMPTY, ErrorHandler.PROSPECT_LOCATION_CAN_NOT_EMPTY);

		for (ProspectContactInfoDTO prospectContactInfoDTO : prospectDTO.getProspectContactInfo()) {
			if (prospectContactInfoService.isMobileExist(prospectContactInfoDTO.getMobile()))
				throw new RecruizException(ErrorHandler.MOBILE_NUMBER_EXIST, ErrorHandler.PROSPECT_MOBILE_ALREADY_EXIST);

			if (prospectContactInfoService.isEmailExist(prospectContactInfoDTO.getEmail()))
				throw new RecruizException(ErrorHandler.EMAIL_EXIST, ErrorHandler.PROSPECT_EMAIL_ALREADY_EXIST);

		}

		Set<ProspectPosition> prospectPositions = new HashSet<ProspectPosition>();

		try {
			nwProspect = new Prospect();
			nwProspect.setCompanyName(prospectDTO.getCompanyName());
			nwProspect.setName(prospectDTO.getName());
			nwProspect.setEmail(prospectDTO.getEmail());
			nwProspect.setMobile(prospectDTO.getMobile());
			if (prospectDTO.getOwner() != null && !prospectDTO.getOwner().isEmpty())
				nwProspect.setOwner(prospectDTO.getOwner());
			else
				nwProspect.setOwner(userService.getLoggedInUserEmail());
			nwProspect.setDesignation(prospectDTO.getDesignation());
			nwProspect.setLocation(prospectDTO.getLocation());
			nwProspect.setAddress(prospectDTO.getAddress());
			nwProspect.setSource(prospectDTO.getSource());
			nwProspect.setMode(prospectDTO.getMode());
			nwProspect.setWebsite(prospectDTO.getWebsite());
			// if (prospectDTO.getIndustry() != null)
			nwProspect.setIndustry(prospectDTO.getIndustry());
			// else
			// nwProspect.setIndustry(IndustryOptions.IT_SW.getDisplayName());

			// if (prospectDTO.getCategory() != null)
			nwProspect.setCategory(prospectDTO.getCategory());
			// else
			// nwProspect.setCategory(CategoryOptions.IT_Software_Application_Programming.getDisplayName());
			nwProspect.setProspectRating(prospectDTO.getProspectRating());
			nwProspect.setDealSize(prospectDTO.getDealSize());
			nwProspect.setPercentage(prospectDTO.getPercentage());
			nwProspect.setValue(prospectDTO.getValue());
			nwProspect.setCurrency(prospectDTO.getCurrency());
			nwProspect.setCustomField(prospectDTO.getCustomField());

			if (!prospectDTO.getProspectContactInfo().isEmpty()) {
				Set<ProspectContactInfo> nwProspectContactInfos = new HashSet<ProspectContactInfo>();
				for (ProspectContactInfoDTO prospectContactInfoDTO : prospectDTO.getProspectContactInfo()) {
					ProspectContactInfo nwProspectContactInfo = new ProspectContactInfo();
					nwProspectContactInfo.setEmail(prospectContactInfoDTO.getEmail());
					nwProspectContactInfo.setName(prospectContactInfoDTO.getName());
					nwProspectContactInfo.setMobile(prospectContactInfoDTO.getMobile());
					nwProspectContactInfo.setDesignation(prospectContactInfoDTO.getDesignation());
					// nwProspectContactInfos.add(prospectContactInfoService.save(nwProspectContactInfo));
					nwProspectContactInfo.setProspect(nwProspect);
					nwProspectContactInfos.add(nwProspectContactInfo);
				}
				nwProspect.setProspectContactInfo(nwProspectContactInfos);
			}

			for (ProspectPostionDTO prospectPostionDTO : prospectDTO.getProspectPostionDTOs()) {
				ProspectPosition nwProspectPosition = new ProspectPosition();
				nwProspectPosition.setPercentage(prospectPostionDTO.getPercentage());
				nwProspectPosition.setValue(prospectPostionDTO.getValue());
				nwProspectPosition.setPositionName(prospectPostionDTO.getPositionName());
				nwProspectPosition.setProspect(nwProspect);
				nwProspectPosition.setKeySkills(prospectPostionDTO.getKeySkills());
				prospectPositions.add(nwProspectPosition);

			}

			if (prospectPositions != null && !prospectPositions.isEmpty())
				nwProspect.setProspectPositions(prospectPositions);
			nwProspect = save(nwProspect);

			// making entry to prospect activity after adding
			prospectActivityService.addActivity("Prospect added", nwProspect.getOwner(), nwProspect.getProspectId() + "",
					ProspectActivityType.Added.getDisplayName());
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RecruizException(ErrorHandler.PROSPECT_CREATION_FAILED, e);
		}
		return nwProspect;
	}

	/*********************
	 * To update prospect*
	 *********************
	 * @param prospectId
	 * @param prospectDTO
	 * @return Prospect
	 * @throws RecruizException
	 */
	@Transactional
	public Prospect updateProspect(long prospectId, ProspectDTO prospectDTO) throws RecruizException {

		if (!exists(prospectId))
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_EXIST, ErrorHandler.ID_NOT_EXIST);

		if (prospectDTO == null)
			throw new RecruizException(ErrorHandler.PROSPECT_CAN_NOT_EMPTY, ErrorHandler.PROSPECT_CAN_NOT_EMPTY);

		if (prospectDTO.getCompanyName() == null || prospectDTO.getCompanyName().isEmpty())
			throw new RecruizException(ErrorHandler.PROSPECT_COMPANY_NAME_CAN_NOT_EMPTY, ErrorHandler.PROSPECT_COMPANY_NAME_CAN_NOT_EMPTY);

		if (prospectDTO.getName() == null || prospectDTO.getName().isEmpty())
			throw new RecruizException(ErrorHandler.PROSPECT_NAME_CAN_NOT_EMPTY, ErrorHandler.PROSPECT_NAME_CAN_NOT_EMPTY);

		/*
		 * if (prospectDTO.getMobile() == null ||
		 * prospectDTO.getMobile().isEmpty()) throw new
		 * RecruizException(ErrorHandler.PROSPECT_MOBILE_CAN_NOT_EMPTY,
		 * ErrorHandler.PROSPECT_MOBILE_CAN_NOT_EMPTY);
		 * 
		 * if (prospectDTO.getEmail() == null ||
		 * prospectDTO.getEmail().isEmpty()) throw new
		 * RecruizException(ErrorHandler.PROSPECT_EMAIL_CAN_NOT_EMPTY,
		 * ErrorHandler.PROSPECT_EMAIL_CAN_NOT_EMPTY);
		 */

		// This checks when comapany name while updating is differ & if it is
		// differ. & differOne is exist then it throw error

		/*
		 * if (isProspectCompanyNameExist(prospectDTO.getCompanyName())) { if
		 * (!findOne(prospectId).getCompanyName().equals(prospectDTO.
		 * getCompanyName())) throw new
		 * RecruizException(ErrorHandler.PROSPECT_COMPANY_ALREADY_EXIST); }
		 */

		/*
		 * if (isProspectExist(prospectDTO.getName())) { if
		 * (!findOne(prospectId).getName().equals(prospectDTO.getName())) throw
		 * new RecruizException(ErrorHandler.PROSPECT_NAME_ALREADY_EXIST,
		 * ErrorHandler.EMAIL_EXISTS); }
		 */

		if (isProspectEmailExist(prospectDTO.getEmail())) {
			if (!findOne(prospectId).getEmail().equals(prospectDTO.getEmail()))
				throw new RecruizException(ErrorHandler.PROSPECT_EMAIL_ALREADY_EXIST, ErrorHandler.PROSPECT_EMAIL_ALREADY_EXIST);
		}

		if (isProspectMobileExist(prospectDTO.getMobile())) {
			if (!findOne(prospectId).getMobile().equals(prospectDTO.getMobile()))
				throw new RecruizException(ErrorHandler.PROSPECT_MOBILE_ALREADY_EXIST, ErrorHandler.PROSPECT_MOBILE_ALREADY_EXIST);
		}

		for (ProspectContactInfoDTO prospectContactInfoDTO : prospectDTO.getProspectContactInfo()) {
			if (checkProspectContactInfoMobileExist(prospectContactInfoDTO.getId(), prospectContactInfoDTO.getMobile())) {
				throw new RecruizException(ErrorHandler.MOBILE_NUMBER_EXIST, ErrorHandler.PROSPECT_MOBILE_ALREADY_EXIST);
			}
			if (checkProspectContactInfoEmailExist(prospectContactInfoDTO.getId(), prospectContactInfoDTO.getEmail())) {
				throw new RecruizException(ErrorHandler.EMAIL_EXIST, ErrorHandler.PROSPECT_EMAIL_ALREADY_EXIST);
			}
		}

		Prospect updateProspect = null;
		Set<ProspectPosition> prospectPositions = new HashSet<ProspectPosition>();

		try {
			updateProspect = findOne(prospectId);
			updateProspect.setCompanyName(prospectDTO.getCompanyName());
			updateProspect.setName(prospectDTO.getName());
			updateProspect.setEmail(prospectDTO.getEmail());
			updateProspect.setMobile(prospectDTO.getMobile());
			if (prospectDTO.getOwner() != null && !prospectDTO.getOwner().isEmpty())
				updateProspect.setOwner(prospectDTO.getOwner());
			else
				updateProspect.setOwner(userService.getLoggedInUserEmail());
			updateProspect.setDesignation(prospectDTO.getDesignation());
			updateProspect.setLocation(prospectDTO.getLocation());
			updateProspect.setAddress(prospectDTO.getAddress());
			updateProspect.setSource(prospectDTO.getSource());
			updateProspect.setWebsite(prospectDTO.getWebsite());

			updateProspect.setCustomField(prospectDTO.getCustomField());

			// if (prospectDTO.getIndustry() != null)
			updateProspect.setIndustry(prospectDTO.getIndustry());
			// else
			// updateProspect.setIndustry(IndustryOptions.IT_SW.getDisplayName());

			// if (prospectDTO.getCategory() != null)
			updateProspect.setCategory(prospectDTO.getCategory());
			// else
			// updateProspect.setCategory(CategoryOptions.IT_Software_Application_Programming.getDisplayName());

			updateProspect.setProspectRating(prospectDTO.getProspectRating());
			updateProspect.setDealSize(prospectDTO.getDealSize());
			updateProspect.setPercentage(prospectDTO.getPercentage());
			updateProspect.setValue(prospectDTO.getValue());
			updateProspect.setCurrency(prospectDTO.getCurrency());

			Set<ProspectContactInfo> nwProspectContactInfos = new HashSet<ProspectContactInfo>();
			if (!prospectDTO.getProspectContactInfo().isEmpty()) {
				for (ProspectContactInfoDTO prospectContactInfoDTO : prospectDTO.getProspectContactInfo()) {
					ProspectContactInfo nwProspectContactInfo = null;
					if (prospectContactInfoDTO.getId() != 0) {
						nwProspectContactInfo = prospectContactInfoService.getProspectContactInfoById(prospectContactInfoDTO.getId());
						nwProspectContactInfo.setEmail(prospectContactInfoDTO.getEmail());
						nwProspectContactInfo.setName(prospectContactInfoDTO.getName());
						nwProspectContactInfo.setMobile(prospectContactInfoDTO.getMobile());
						nwProspectContactInfo.setDesignation(prospectContactInfoDTO.getDesignation());
						nwProspectContactInfos.add(nwProspectContactInfo);
					} else {
						nwProspectContactInfo = new ProspectContactInfo();
						nwProspectContactInfo.setEmail(prospectContactInfoDTO.getEmail());
						nwProspectContactInfo.setName(prospectContactInfoDTO.getName());
						nwProspectContactInfo.setMobile(prospectContactInfoDTO.getMobile());
						nwProspectContactInfo.setDesignation(prospectContactInfoDTO.getDesignation());
						nwProspectContactInfo.setProspect(updateProspect);
						nwProspectContactInfos.add(nwProspectContactInfo);
					}
				}
			}

			for (ProspectPostionDTO prospectPostionDTO : prospectDTO.getProspectPostionDTOs()) {
				ProspectPosition nwProspectPosition = null;

				if (prospectPostionDTO.getPositionId() != 0) {
					nwProspectPosition = prospectPositionService.findOne(prospectPostionDTO.getPositionId());
					nwProspectPosition.setPercentage(prospectPostionDTO.getPercentage());
					nwProspectPosition.setValue(prospectPostionDTO.getValue());
					nwProspectPosition.setPositionName(prospectPostionDTO.getPositionName());
					nwProspectPosition.setProspect(updateProspect);
					nwProspectPosition.setKeySkills(prospectPostionDTO.getKeySkills());
					prospectPositions.add(nwProspectPosition);
				} else {
					nwProspectPosition = new ProspectPosition();
					nwProspectPosition.setPercentage(prospectPostionDTO.getPercentage());
					nwProspectPosition.setValue(prospectPostionDTO.getValue());
					nwProspectPosition.setPositionName(prospectPostionDTO.getPositionName());
					nwProspectPosition.setProspect(updateProspect);
					nwProspectPosition.setKeySkills(prospectPostionDTO.getKeySkills());
					prospectPositions.add(nwProspectPosition);
				}
			}

			updateProspect.getProspectContactInfo().clear();
			updateProspect.getProspectContactInfo().addAll(nwProspectContactInfos);
			updateProspect.getProspectPositions().addAll(prospectPositions);
			updateProspect = save(updateProspect);
			prospectActivityService.detailsUpdated(updateProspect);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RecruizException(ErrorHandler.PROSPECT_UPDATE_FAILED, ErrorHandler.UPDATE_FAILED, e);
		}
		return updateProspect;
	}

	/**********************
	 * To delete prospect**
	 **********************
	 * @param prospectId
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Boolean deleteProspect(long prospectId) throws RecruizException {

		if (!exists(prospectId))
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_EXIST, ErrorHandler.PROSPECT_NOT_EXIST);

		Boolean isDeleted = false;
		try {
			delete(prospectId);
			if (!exists(prospectId)) {
				isDeleted = true;
				prospectActivityService.deleteProspect(prospectId);
			}

		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RecruizException(ErrorHandler.DELETE_FAILED, e);
		}
		return isDeleted;
	}

	/********************************
	 * To convert prospect to client*
	 ********************************
	 * @param prospectId
	 * @param status
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Prospect convertProspectToClient(Long prospectId, String status) throws RecruizException {

		if (!exists(prospectId))
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_EXIST, ErrorHandler.PROSPECT_NOT_EXIST);

		Prospect prospect = null;
		if (containsProspectStatus(status) || ProspectActivityType.Converted.getDisplayName().equals(status)) {

			prospect = getProspectById(prospectId);

			if (prospect.getCompanyName() != null && !prospect.getCompanyName().isEmpty()) {
				if (clientService.clientExist(prospect.getCompanyName()))
					throw new RecruizException(ErrorHandler.PROSPECT_CLIENT_NAME_DUPLICATE, ErrorHandler.PROSPECT_CLIENT_NAME_REDUNDECY);
			}

			try {
				// if (ProspectStatus.Won.getDisplayName().equals(status)) {

				ClientDTO clientDTO = new ClientDTO();
				clientDTO.setClientName(prospect.getCompanyName());
				clientDTO.setClientLocation(prospect.getLocation());
				clientDTO.setAddress(prospect.getAddress());
				clientDTO.setWebsite(prospect.getWebsite());
				// prospect is dummy. if you converted to client then client
				// should also dummy
				clientDTO.setDummy(checkDummyByCompanyName(prospect.getCompanyName()));
				/*
				 * Set<ClientDecisionMaker> clientDecisionMakers = new
				 * HashSet<ClientDecisionMaker>(); ClientDecisionMaker
				 * clientDecisionMaker = new ClientDecisionMaker();
				 * clientDecisionMaker.setEmail(prospect.getEmail());
				 * clientDecisionMaker.setMobile(prospect.getMobile());
				 * clientDecisionMaker.setName(prospect.getName());
				 * clientDecisionMakers.add(clientDecisionMaker);
				 * clientDTO.setClientDecisionMaker(clientDecisionMakers);
				 */
				Client client = clientService.addClient(clientDTO);
				prospect.setClient(client);
				prospectActivityService.changeToConverted(prospectId);
				prospect.setStatus(ProspectActivityType.Converted.getDisplayName());
				save(prospect);
				// breaking the relationship of prospect & prospect position
				// here
				for (ProspectPosition prospectPosition : prospect.getProspectPositions()) {
					prospectPosition.setClientName(prospect.getCompanyName());
					prospectPosition.setProspect(null);
					prospectPosition.setIsConvertedToClient(true);
					prospectPositionService.save(prospectPosition);
				}
				// }
			} catch (Exception e) {
				logger.error(e.getMessage());
				throw new RecruizException(ErrorHandler.PROSPECT_CLIENT_CONVERTION_FAILED, e);
			}

		} else {
			throw new RecruizException(ErrorHandler.PROSPECT_STATUS_NOT_FOUND, ErrorHandler.PROSPECT_STATUS_NOT_FOUND);
		}
		return prospect;
	}

	/****************************
	 * To change prospect status*
	 ****************************
	 * @param prospectId
	 * @param status
	 * @return Prospect
	 * @throws RecruizException
	 */
	@Transactional
	public Prospect changeProspectStatus(Long prospectId, String status) throws RecruizException {

		if (!exists(prospectId))
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_EXIST, ErrorHandler.PROSPECT_NOT_EXIST);

		if (ProspectActivityType.Converted.getDisplayName().equals(status)) {
			if (findOne(prospectId).getStatus().equals(ProspectStatus.Won.getDisplayName()))
				throw new RecruizException(ErrorHandler.PROSPECT_ALREADY_CONVERTED_TO_CLIENT,
						ErrorHandler.PROSPECT_ALREADY_CONVERTED_TO_CLIENT);
		}

		Prospect prospect = null;
		if (containsProspectStatus(status)) {
			try {
				prospect = getProspectById(prospectId);
				prospect.setStatus(ProspectStatus.valueOf(status).name());
				prospect = save(prospect);
				prospectActivityService.changeStatus(prospectId, status);

			} catch (Exception e) {
				logger.error(e.getMessage());
				throw new RecruizException(ErrorHandler.PROSPECT_STATUS_CHANGED_FAILED, e);
			}
		} else {
			throw new RecruizException(ErrorHandler.PROSPECT_STATUS_NOT_FOUND, ErrorHandler.PROSPECT_STATUS_NOT_FOUND);
		}
		return prospect;
	}

	/**************************************************
	 * When status is lost then it will capture reason*
	 **************************************************
	 * @param prospectId
	 * @param reason
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Boolean prospectReason(Long prospectId, String reason) throws RecruizException {

		Boolean reasonUpdate = false;
		try {
			if (isProspectExist(prospectId)) {
				Prospect prospect = getProspectById(prospectId);
				prospect.setReason(reason);
				save(prospect);
				prospectActivityService.changeLostStatus(prospectId, reason);
				reasonUpdate = true;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(ErrorHandler.CHANGE_STATUS_FAILED, ErrorHandler.REASON_SAVE_FAILED, e);
		}
		return reasonUpdate;
	}

	/********************************
	 * To check company exist or not*
	 ********************************
	 * @param prospectId
	 * @param company
	 * @return
	 * @throws RecruizException
	 */
	public Boolean checkCompanyExist(Long prospectId, String company) throws RecruizException {

		Boolean isExist = false;
		if (isProspectCompanyNameExist(company)) {
			if (prospectId != 0) {
				if (getProspectById(prospectId).getCompanyName().equals(company))
					isExist = false;
				else
					isExist = true;
			} else {
				isExist = true;
			}
		}
		return isExist;
	}

	/*******************************************
	 * To check mobile exist or not in prospect*
	 *******************************************
	 * @param prospectId
	 * @param mobile
	 * @return
	 * @throws RecruizException
	 */
	public Boolean checkMobileExist(Long prospectId, String mobile) throws RecruizException {

		Boolean isExist = false;
		if (isProspectMobileExist(mobile)) {
			if (prospectId != 0) {
				if (getProspectById(prospectId).getMobile() != null && !getProspectById(prospectId).getMobile().isEmpty()
						&& getProspectById(prospectId).getMobile().equals(mobile))
					isExist = false;
				else
					isExist = true;
			} else {
				isExist = true;
			}
		}
		return isExist;
	}

	/*******************************************
	 * To check email exist or not in prospect**
	 *******************************************
	 * @param prospectId
	 * @param email
	 * @return
	 * @throws RecruizException
	 */
	public Boolean checkEmailExist(Long prospectId, String email) throws RecruizException {

		Boolean isExist = false;
		if (isProspectEmailExist(email)) {
			if (prospectId != 0) {
				if (getProspectById(prospectId).getEmail() != null && !getProspectById(prospectId).getEmail().isEmpty()
						&& getProspectById(prospectId).getEmail().equals(email))
					isExist = false;
				else
					isExist = true;
			} else {
				isExist = true;
			}
		}
		return isExist;
	}

	/*******************************************************************
	 * To check mobile exist or not in prospect contact info mobile no**
	 *******************************************************************
	 * @param prospectContactInfoId
	 * @param mobile
	 * @return
	 * @throws RecruizException
	 */
	public Boolean checkProspectContactInfoMobileExist(Long prospectContactInfoId, String mobile) throws RecruizException {

		Boolean isExist = false;
		if (prospectContactInfoService.isMobileExist(mobile)) {
			if (prospectContactInfoId != 0) {
				if (prospectContactInfoService.getProspectContactInfoById(prospectContactInfoId).getMobile().equals(mobile))
					isExist = false;
				else
					isExist = true;
			} else {
				isExist = true;
			}
		}
		return isExist;
	}

	/*******************************************************
	 * To check email exist or not in prospect contact info*
	 *******************************************************
	 * @param prospectContactInfoId
	 * @param email
	 * @return
	 * @throws RecruizException
	 */
	public Boolean checkProspectContactInfoEmailExist(Long prospectContactInfoId, String email) throws RecruizException {

		Boolean isExist = false;
		if (prospectContactInfoService.isEmailExist(email)) {
			if (prospectContactInfoId != 0) {
				if (prospectContactInfoService.getProspectContactInfoById(prospectContactInfoId).getEmail().equals(email))
					isExist = false;
				else
					isExist = true;
			} else {
				isExist = true;
			}
		}
		return isExist;
	}

	public void deleteCustomFieldWithName(String name) {
		prospectRepository.deleteCustomFieldWithName(name);
	}
}
