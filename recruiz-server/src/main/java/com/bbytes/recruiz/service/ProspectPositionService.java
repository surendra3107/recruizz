package com.bbytes.recruiz.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectPosition;
import com.bbytes.recruiz.enums.ProspectActivityType;
import com.bbytes.recruiz.enums.ProspectPositionStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.ProspectPositionRepository;
import com.bbytes.recruiz.rest.dto.models.ProspectPostionDTO;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class ProspectPositionService extends AbstractService<ProspectPosition, Long> {

	private static final Logger logger = LoggerFactory.getLogger(ProspectPositionService.class);

	@Autowired
	private ProspectPositionRepository prospectPositionRepository;

	@Autowired
	private ProspectService prospectService;
	
	@Autowired
	private ProspectActivityService prospectActivityService;
	
	@Autowired
	private ClientService clientService;

	@Autowired
	public ProspectPositionService(ProspectPositionRepository prospectPositionRepository) {
		super(prospectPositionRepository);
		this.prospectPositionRepository = prospectPositionRepository;

	}
	
	public List<ProspectPosition> getByClientName(String clientName){
		return prospectPositionRepository.findByClientName(clientName);
	}
	
	public Page<ProspectPosition> getByClientName(String clientName,Pageable pageable){
		return prospectPositionRepository.findByClientNameAndStatus(clientName, ProspectPositionStatus.Pending.getDisplayName(), pageable);
	}
	
	public Page<ProspectPosition> getByStatus(Pageable pageable) throws RecruizException {
	
		List<ProspectPosition> prospectPositions = new ArrayList<>();
		
		for(ProspectPosition position : prospectPositionRepository.findByIsConvertedToClientAndStatus(true,ProspectPositionStatus.Pending.getDisplayName())){
			if(clientService.clientExist(position.getClientName())){
				prospectPositions.add(position);
			}
		}
		
		return getByStatus(pageable,prospectPositions);
	}
	
	public Page<ProspectPosition> getByStatus(Pageable pageable ,List<ProspectPosition> prospectPositions) throws RecruizException{
		
		
		int start = pageable.getOffset();
		int end = (start + pageable.getPageSize()) > prospectPositions.size() ? prospectPositions.size()
				: (start + pageable.getPageSize());
		final Page<ProspectPosition> page = new PageImpl<ProspectPosition>(prospectPositions.subList(start, end), pageable,
				prospectPositions.size());
		return page;
	}
	
	public ProspectPosition getById(long prospectPositionId) throws RecruizException {
		if (exists(prospectPositionId))
			return findOne(prospectPositionId);
		else
			throw new RecruizException(ErrorHandler.PROSPECT_POSITION_NOT_EXIST,
					ErrorHandler.PROSPECT_POSITION_NOT_FOUND);
	}

	public List<ProspectPosition> getAllPostion(long prospectId) throws RecruizException {
		
		List<ProspectPosition> prospectPositions = null;
		Prospect prospect = prospectService.getProspectById(prospectId);
		if (prospect != null) {
			
			if(prospect.getStatus().equals(ProspectActivityType.Converted.getDisplayName())){
				prospectPositions = getByClientName(prospect.getCompanyName());
			}
			else{
				prospectPositions = prospectPositionRepository.findByProspect(prospect);
			}
			Collections.sort(prospectPositions, Collections.reverseOrder());
			return prospectPositions;
		} else {
			logger.info(ErrorHandler.PROSPECT_NOT_FOUND);
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_FOUND, ErrorHandler.PROSPECT_NOT_EXIST);
		}
	}

	public String convertSetToString(Set<String> values) {
		// used for location
		String str = "";
		if (values != null && !values.isEmpty()) {
			for (String string : values) {
				if (!str.isEmpty()) {
					str = str + " | " + string;
				} else {
					str = string;
				}
			}
		}
		return str;

	}

	@Transactional
	public Boolean addPositionInProspect(long prospectId, List<ProspectPostionDTO> prospectPostionDTOs)
			throws RecruizException {
		// here we taking list of prospectPostionDTOs in future we take multiple
		// position.

		Prospect prospect = prospectService.getProspectById(prospectId);
		if (prospect == null)
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_FOUND, ErrorHandler.PROSPECT_NOT_EXIST);

		if (prospectPostionDTOs == null || prospectPostionDTOs.isEmpty())
			throw new RecruizException(ErrorHandler.PROSPECT_POSITION_CAN_NOT_EMPTY,
					ErrorHandler.PROSPECT_POSITION_CAN_NOT_NULL);

		for (ProspectPostionDTO prospectPostionDTO : prospectPostionDTOs) {

			if (prospectPostionDTO.getPositionName() == null || prospectPostionDTO.getPositionName().isEmpty())
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_NAME_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_NAME_CAN_NOT_NULL);

			if (prospectPostionDTO.getClosureDate() == null)
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_CLOSURE_DATE_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_CLOSURE_DATE_CAN_NOT_NULL);

			if (prospectPostionDTO.getNumberOfOpenings() == 0)
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_OPENING_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_OPENING_CAN_NOT_NULL);

			if (prospectPostionDTO.getMinExperience() == null)
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_MIN_EXP_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_MIN_EXP_CAN_NOT_NULL);

			if (prospectPostionDTO.getMaxExperience() == null)
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_MAX_EXP_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_MAX_EXP_CAN_NOT_NULL);

			if (prospectPostionDTO.getKeySkills() == null)
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_SKILLS_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_SKILLS_CAN_NOT_NULL);

			if (prospectPostionDTO.getLocation() == null)
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_LOCATION_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_LOCATION_CAN_NOT_NULL);

			if (prospectPostionDTO.getEducationQualification() == null)
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_EDUCATION_QUALIFICATION_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_EDUCATION_QUALIFICATION_CAN_NOT_NULL);
			
			if (prospectPostionDTO.getType() == null || prospectPostionDTO.getType().isEmpty() )
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_TYPE_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_TYPE_CAN_NOT_NULL);
			
			if (prospectPostionDTO.getIndustry() == null || prospectPostionDTO.getIndustry().isEmpty() )
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_INDUSTRY_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_INDUSTRY_CAN_NOT_NULL);
			
			if (prospectPostionDTO.getFunctionalArea() == null || prospectPostionDTO.getFunctionalArea().isEmpty() )
				throw new RecruizException(ErrorHandler.PROSPECT_POSITION_FUNCTIONAL_AREA_CAN_NOT_EMPTY,
						ErrorHandler.PROSPECT_POSITION_FUNCTIONAL_AREA_CAN_NOT_NULL);
		}

		try {
			List<ProspectPosition> prospectPositions = new ArrayList<ProspectPosition>();
			for (ProspectPostionDTO prospectPostionDTO : prospectPostionDTOs) {
				ProspectPosition prospectPosition = new ProspectPosition();
				prospectPosition.setPositionName(prospectPostionDTO.getPositionName());
				prospectPosition.setClosureDate(prospectPostionDTO.getClosureDate());
				prospectPosition.setNumberOfOpenings(prospectPostionDTO.getNumberOfOpenings());
				prospectPosition.setMinExperience(prospectPostionDTO.getMinExperience());
				prospectPosition.setMaxExperience(prospectPostionDTO.getMaxExperience());
				prospectPosition.setKeySkills(prospectPostionDTO.getKeySkills());
				prospectPosition.setLocation(prospectPostionDTO.getLocationSet());
				prospectPosition.setEducationQualification(prospectPostionDTO.getEducationQualification());
				prospectPosition.setType(prospectPostionDTO.getType());
				prospectPosition.setRemoteWork(prospectPostionDTO.isRemoteWork());
				prospectPosition.setMinSal(prospectPostionDTO.getMinSal());
				prospectPosition.setMaxSal(prospectPostionDTO.getMaxSal());
				prospectPosition.setIndustry(prospectPostionDTO.getIndustry());
				prospectPosition.setFunctionalArea(prospectPostionDTO.getFunctionalArea());
				prospectPosition.setProspect(prospect);
				prospectPosition.setStatus(ProspectPositionStatus.Pending.getDisplayName());
				prospectPosition.setCurrency(prospectPostionDTO.getCurrency());
				prospectPositions.add(prospectPosition);
			}
			List<ProspectPosition> addedProspectPositions = save(prospectPositions);
			String positionName = "";
			for (ProspectPosition prospectPosition : addedProspectPositions) {
				if (!positionName.isEmpty()) {
					positionName = positionName + "," + prospectPosition.getPositionName();
				} else {
					positionName = prospectPosition.getPositionName();
				}
			}

			// making entry to prospect activity after adding
			//prospectActivityService.addActivity("Position added :"+positionName, prospect.getOwner(), prospect.getProspectId() + "",
					//ProspectActivityType.Added.getDisplayName());
			prospectActivityService.addPositionToProspect(prospect,positionName);
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return false;
	}

	@Transactional
	public Boolean deletePositionInProspect(long prospectPositionId) throws RecruizException {
		ProspectPosition prospectPosition = null;
		Boolean isExist = false;

		if (!exists(prospectPositionId))
			throw new RecruizException(ErrorHandler.PROSPECT_POSITION_NOT_EXIST,
					ErrorHandler.PROSPECT_POSITION_NOT_FOUND);
		else {
			prospectPosition = findOne(prospectPositionId);
		}

		if (prospectPosition != null) {
			delete(prospectPosition);
			isExist = exists(prospectPositionId) ? false : true;
			if(isExist){
				prospectActivityService.deletePositionInProspect(prospectPosition.getProspect().getProspectId(),prospectPosition.getPositionName());
			}
		}
		return isExist;
	}

	@Transactional
	public Boolean deleteAllPositionInProspect(long prospectId) throws RecruizException {
		Boolean isExist = false;
		Prospect prospect = prospectService.getProspectById(prospectId);
		if (prospect == null)
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_FOUND, ErrorHandler.PROSPECT_NOT_EXIST);

		Set<ProspectPosition> prospectPositions = prospect.getProspectPositions();
		if (prospectPositions != null) {
			prospect.getProspectPositions().clear();
			prospect = prospectService.getProspectById(prospectId);
			isExist = (prospect.getProspectPositions() == null || prospect.getProspectPositions().isEmpty()) ? true
					: false;
		}
		return isExist;
	}

	@Transactional
	public Boolean updatePosition(long prospectPositionId, ProspectPostionDTO prospectPostionDTO)
			throws RecruizException {

		Boolean isSuccess = false;
		ProspectPosition prospectPosition = null;

		if (prospectPostionDTO == null)
			throw new RecruizException(ErrorHandler.PROSPECT_POSITION_CAN_NOT_EMPTY,
					ErrorHandler.PROSPECT_POSITION_CAN_NOT_NULL);

		if (!exists(prospectPositionId))
			throw new RecruizException(ErrorHandler.PROSPECT_POSITION_NOT_EXIST,
					ErrorHandler.PROSPECT_POSITION_NOT_FOUND);
		else {
			prospectPosition = findOne(prospectPositionId);
		}
		try {
			prospectPosition.getKeySkills().clear();
			prospectPosition.getLocation().clear();
			prospectPosition.getEducationQualification().clear();
			prospectPosition.setPositionName(prospectPostionDTO.getPositionName());
			prospectPosition.setClosureDate(prospectPostionDTO.getClosureDate());
			prospectPosition.setNumberOfOpenings(prospectPostionDTO.getNumberOfOpenings());
			prospectPosition.setMinExperience(prospectPostionDTO.getMinExperience());
			prospectPosition.setMaxExperience(prospectPostionDTO.getMaxExperience());
			prospectPosition.setKeySkills(prospectPostionDTO.getKeySkills());
			prospectPosition.setLocation(prospectPostionDTO.getLocationSet());
			prospectPosition.setEducationQualification(prospectPostionDTO.getEducationQualification());
			prospectPosition.setType(prospectPostionDTO.getType());
			prospectPosition.setRemoteWork(prospectPostionDTO.isRemoteWork());
			prospectPosition.setMinSal(prospectPostionDTO.getMinSal());
			prospectPosition.setMaxSal(prospectPostionDTO.getMaxSal());
			prospectPosition.setIndustry(prospectPostionDTO.getIndustry());
			prospectPosition.setFunctionalArea(prospectPostionDTO.getFunctionalArea());
			prospectPosition.setCurrency(prospectPostionDTO.getCurrency());
			prospectPosition = save(prospectPosition);
			prospectActivityService.detailsPositionUpdated(prospectPosition.getProspect().getProspectId(),prospectPosition.getPositionName());
			isSuccess = true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return isSuccess;
	}
	
	@Transactional
	public void changeStatusOfRequestedProspectPosition(long prospectPositionId) throws RecruizException{
		ProspectPosition prospectPosition = getById(prospectPositionId);
		if(prospectPosition!=null){
			prospectPosition.setStatus(ProspectPositionStatus.Processed.getDisplayName());
			save(prospectPosition);
		}
	}
	
	
}
