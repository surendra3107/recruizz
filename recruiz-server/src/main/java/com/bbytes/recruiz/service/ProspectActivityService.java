package com.bbytes.recruiz.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Prospect;
import com.bbytes.recruiz.domain.ProspectActivity;
import com.bbytes.recruiz.enums.ProspectActivityType;
import com.bbytes.recruiz.enums.ProspectStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.ProspectActivityRepository;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class ProspectActivityService extends AbstractService<ProspectActivity, Long> {

	private static final Logger logger = LoggerFactory.getLogger(ProspectActivityService.class);

	private ProspectActivityRepository prospectActivityRepository;

	@Autowired
	private UserService userService;

	@Autowired
	public ProspectActivityService(ProspectActivityRepository prospectActivityRepository) {
		super(prospectActivityRepository);
		this.prospectActivityRepository = prospectActivityRepository;
	}

	@Transactional(readOnly = true)
	public List<ProspectActivity> getByProspectId(String prospectId) throws RecruizException {
		if (!isProspectActivityExist(prospectId))
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_FOUND, ErrorHandler.PROSPECT_NOT_EXIST);
		return prospectActivityRepository.findByProspectId(prospectId);
	}

	@Transactional(readOnly = true)
	public Page<ProspectActivity> getProspectActivityById(String prospectId, Pageable pageable)
			throws RecruizException {
		if (!isProspectActivityExist(prospectId))
			throw new RecruizException(ErrorHandler.PROSPECT_NOT_FOUND, ErrorHandler.PROSPECT_NOT_EXIST);
		return prospectActivityRepository.findByProspectIdOrderByWhatTimeDesc(prospectId, pageable);
	}

	@Transactional(readOnly = true)
	public boolean isProspectActivityExist(String prospectId) {
		return prospectActivityRepository.findByProspectId(prospectId) == null ? false : true;
	}

	/***********************************
	 * To add prospectActivity ********
	 ***********************************
	 * @param what
	 * @param who
	 * @param prospectId
	 * @param type
	 */
	@Transactional
	public void addActivity(String what, String who, String prospectId, String type) {

		try {
			ProspectActivity newActivity = new ProspectActivity();
			newActivity.setWhat(what);
			newActivity.setWho(who);
			newActivity.setProspectId(prospectId);
			newActivity.setType(type);
			/*if (type.equalsIgnoreCase(ProspectActivityType.Added.getDisplayName())
					&& prospectActivityRepository.findByTypeAndProspectId(type, prospectId) != null
					&& !prospectActivityRepository.findByTypeAndProspectId(type, prospectId).isEmpty()) {
				return;
			}*/
			save(newActivity);
		} catch (Exception e) {
			logger.error(e.getMessage());
		}

	}
	
	/**************************************************************************************
	 * Whenever prospect is updated then it has to called to update the prospect activity**
	 **************************************************************************************
	 * @param prospectFromDB
	 */
	@Transactional
	public void detailsUpdated(Prospect prospectFromDB) {
		addActivity("Prospect details modified",
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				prospectFromDB.getProspectId() + "", ProspectActivityType.DetailsUpdated.getDisplayName());
	}
	
	@Transactional
	public void detailsPositionUpdated(Long prospectId,String positionName) {
		addActivity("Position : "+positionName+"  has modified",
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				prospectId + "", ProspectActivityType.DetailsUpdated.getDisplayName());
	}

	/****************************************************************************************
	 * Whenever prospect is deleted then it has to called to the delete prospect activity ***
	 ****************************************************************************************
	 * @param prospectId
	 */
	@Transactional
	public void deleteProspect(Long prospectId) {
		addActivity("Prospect deleted",
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				prospectId + "", ProspectActivityType.Deleted.getDisplayName());
	}
	
	@Transactional
	public void deletePositionInProspect(Long prospectId,String positionName) {
		addActivity("Position deleted : "+positionName,
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				prospectId + "", ProspectActivityType.Deleted.getDisplayName());
	}

	/*************************************
	 * It will changeg status to conveted*
	 *************************************
	 * @param prospectId
	 */
	@Transactional
	public void changeToConverted(Long prospectId) {
		addActivity("Prospect converted to Client",
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				prospectId + "", ProspectActivityType.Converted.getDisplayName());
	}

	/****************************
	 * it will change the status*
	 ****************************
	 * @param prospectId
	 * @param status
	 */
	@Transactional
	public void changeStatus(Long prospectId, String status) {
		addActivity("Prospect Changed to " + status,
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				prospectId + "", ProspectStatus.valueOf(status).getDisplayName());
	}
	
	/**************************************************
	 * It will capture Lost activity & take the reason*
	 **************************************************
	 * @param prospectId
	 * @param reason
	 */
	@Transactional
	public void changeLostStatus(Long prospectId, String reason) {
		addActivity("Prospect Changed to Lost . Reason is " + reason,
				userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")",
				prospectId + "", ProspectStatus.valueOf("Lost").getDisplayName());
	}
	
	
	/***************************************************************************
	 * when position added to prospect then it will record in prospect activity*
	 ***************************************************************************
	 * @param prospect
	 * @param positionName
	 */
	@Transactional
	public void addPositionToProspect(Prospect prospect, String positionName){
		try{
			String msg = "Position by name  " + positionName + " is added under "+prospect.getCompanyName();
			ProspectActivity newActivity = new ProspectActivity(userService.getLoggedInUserEmail() + "(" + userService.getLoggedInUserObject().getName() + ")", msg, String.valueOf(prospect.getProspectId()), ProspectActivityType.AddedPositionToProspect.getDisplayName());
			save(newActivity);
		}catch(Exception ex){
			logger.info(ex.getMessage(), ex);
		}
	}
}
