package com.bbytes.recruiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectContactInfo;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.ProspectContactInfoRepository;
import com.bbytes.recruiz.rest.dto.models.ProspectContactInfoDTO;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class ProspectContactInfoService extends AbstractService<ProspectContactInfo, Long> {

	private static final Logger logger = LoggerFactory.getLogger(ProspectContactInfoService.class);

	private ProspectContactInfoRepository prospectContactInfoRepository;

	@Autowired
	private ProspectService prospectService;

	@Autowired
	public ProspectContactInfoService(ProspectContactInfoRepository prospectContactInfoRepository) {
		super(prospectContactInfoRepository);
		this.prospectContactInfoRepository = prospectContactInfoRepository;
	}

	@Transactional(readOnly = true)
	public boolean isProspectContactInfoExist(long id) {
		return findOne(id) == null ? false : true;
	}
	
	@Transactional(readOnly = true)
	public boolean isEmailExist(String email) {
		if(email == null || email.isEmpty())
			return false;
		return prospectContactInfoRepository.findByEmail(email) == null ? false : true;
	}

	@Transactional(readOnly = true)
	public boolean isMobileExist(String mobile) {
		if(mobile == null || mobile.isEmpty())
			return false;
		return prospectContactInfoRepository.findByMobile(mobile) == null ? false : true;
	}

	/***********************************************
	 * To add a new prospectContactInfo in prospect*
	 ***********************************************
	 * @param prospectId
	 * @param prospectContactInfoDTO
	 * @return ProspectContactInfo
	 * @throws RecruizException
	 */
	@Transactional
	public ProspectContactInfo addProspectContactInfo(long prospectId, ProspectContactInfoDTO prospectContactInfoDTO)
			throws RecruizException {

		if (!prospectService.isProspectExist(prospectId))
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_EXIST);

		if (prospectContactInfoDTO == null)
			throw new RecruizException(ErrorHandler.PROSPECT_CONTACT_CAN_NOT_EMPTY);

		ProspectContactInfo nwProspectContactInfo = null;
		try {
			Prospect prospect = prospectService.getProspectById(prospectId);
			nwProspectContactInfo = new ProspectContactInfo();
			nwProspectContactInfo.setName(prospectContactInfoDTO.getName());
			nwProspectContactInfo.setMobile(prospectContactInfoDTO.getMobile());
			nwProspectContactInfo.setEmail(prospectContactInfoDTO.getEmail());
			nwProspectContactInfo.setDesignation(prospectContactInfoDTO.getDesignation());
			nwProspectContactInfo.setProspect(prospect);
			nwProspectContactInfo = save(nwProspectContactInfo);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RecruizException(ErrorHandler.PROSPECT_CONTACT_INFO_CREATION_FAILED, e);
		}
		return nwProspectContactInfo;
	}

	/*****************************************
	 * To update existing prospectContactInfo*
	 *****************************************
	 * @param id
	 * @param prospectContactInfoDTO
	 * @return ProspectContactInfo
	 * @throws RecruizException
	 */
	@Transactional
	public ProspectContactInfo updateProspectContactInfo(long id, ProspectContactInfoDTO prospectContactInfoDTO)
			throws RecruizException {

		if (!isProspectContactInfoExist(id))
			throw new RecruizException(ErrorHandler.PROSPECT_CONTACT_INFO_NOT_EXIST);

		if (prospectContactInfoDTO == null)
			throw new RecruizException(ErrorHandler.PROSPECT_CONTACT_CAN_NOT_EMPTY);

		if (prospectContactInfoDTO.getEmail() != null && prospectContactInfoDTO.getEmail().isEmpty())
			throw new RecruizException(ErrorHandler.PROSPECT_CONTACT_INFO_EMAIL_CAN_NOT_EMPTY);

		if (prospectContactInfoDTO.getMobile() != null && prospectContactInfoDTO.getMobile().isEmpty())
			throw new RecruizException(ErrorHandler.PROSPECT_CONTACT_INFO_MOBILE_CAN_NOT_EMPTY);

		if (prospectContactInfoDTO.getName() != null) {
			if (prospectContactInfoDTO.getName().isEmpty())
				throw new RecruizException(ErrorHandler.PROSPECT_CONTACT_INFO_NAME_CAN_NOT_EMPTY);
		}

		ProspectContactInfo updateProspectContactInfo = null;
		try {
			updateProspectContactInfo = findOne(id);
			updateProspectContactInfo.setName(prospectContactInfoDTO.getName());
			updateProspectContactInfo.setMobile(prospectContactInfoDTO.getMobile());
			updateProspectContactInfo.setEmail(prospectContactInfoDTO.getEmail());
			updateProspectContactInfo.setDesignation(prospectContactInfoDTO.getDesignation());
			updateProspectContactInfo = save(updateProspectContactInfo);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RecruizException(ErrorHandler.UPDATE_FAILED, e);
		}
		return updateProspectContactInfo;
	}

	/*********************************************
	 * To get all prospectContactInfo in prospect*
	 * *******************************************
	 * 
	 * @param prospectId
	 * @return List<ProspectContactInfo>
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public List<ProspectContactInfo> getProspectContactInfo(long prospectId) throws RecruizException {

		if (!prospectService.isProspectExist(prospectId))
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_EXIST);

		List<ProspectContactInfo> prospectContactInfos = null;
		try {
			prospectContactInfos = prospectContactInfoRepository
					.findByProspect(prospectService.getProspectById(prospectId));
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RecruizException(ErrorHandler.GET_FAILED, e);
		}
		return prospectContactInfos;
	}

	/******************************
	 * To get prospectContactInfo *
	 ******************************
	 * @param prospectContactInfoId
	 * @return ProspectContactInfo
	 * @throws RecruizException
	 */
	@Transactional(readOnly = true)
	public ProspectContactInfo getProspectContactInfoById(long prospectContactInfoId) throws RecruizException {

		if (!isProspectContactInfoExist(prospectContactInfoId))
			throw new RecruizException(ErrorHandler.PROSPECT_CONTACT_INFO_NOT_EXIST);

		ProspectContactInfo prospectContactInfo = null;
		try {
			prospectContactInfo = findOne(prospectContactInfoId);
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RecruizException(ErrorHandler.GET_FAILED, e);
		}
		return prospectContactInfo;
	}

	/***********************************
	 * To delete prospect contact info**
	 ***********************************
	 * @param prospectContactInfoId
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Boolean deleteProspectContactInfo(long prospectContactInfoId) throws RecruizException {

		if (!isProspectContactInfoExist(prospectContactInfoId))
			throw new RecruizException(ErrorHandler.PROSPECT_CONTACT_INFO_NOT_EXIST);
		Boolean isDeleted = false;
		try {
			delete(prospectContactInfoId);
			if (!exists(prospectContactInfoId))
				isDeleted = true;
		} catch (Exception e) {
			logger.error(e.getMessage());
			throw new RecruizException(ErrorHandler.DELETE_FAILED, e);
		}
		return isDeleted;
	}
}
