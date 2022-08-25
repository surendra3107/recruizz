package com.bbytes.recruiz.service;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.PositionOfferCost;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.PositionOfferCostRepository;
import com.bbytes.recruiz.rest.dto.models.PositionOfferCostDTO;
import com.bbytes.recruiz.rest.dto.models.RestResponse;

@Service
public class PositionOfferCostService extends AbstractService<PositionOfferCost, Long> {

	
private static final Logger logger = LoggerFactory.getLogger(PositionOfferCostService.class);
	
	@Autowired
	PositionOfferCostRepository positionOfferCostRepository;
	
	@Autowired
	PositionService positionService;
	
	@Autowired
	public PositionOfferCostService(PositionOfferCostRepository positionOfferCostRepository) {
		super(positionOfferCostRepository);
		this.positionOfferCostRepository = positionOfferCostRepository;
	}

	public RestResponse addPositionOfferCost(PositionOfferCostDTO costDTO) throws RecruizException {
		
		Position position = positionService.getPositionById(costDTO.getPositionId());
		
		if(position==null)
			return new RestResponse(RestResponse.FAILED, "", "Position Not Found !");
		
		try{
			PositionOfferCost positionOfferCost = new PositionOfferCost();
			
			positionOfferCost.setBillRate(costDTO.getBillRate());
			positionOfferCost.setBillHours(costDTO.getBillHours());
			positionOfferCost.setBillingDate(new Date());
			positionOfferCost.setProjectDuration(costDTO.getProjectDuration());
			positionOfferCost.setOneTimeCost(costDTO.getOneTimeCost());
			positionOfferCost.setHeadHunting(costDTO.getHeadHunting());
			positionOfferCost.setStatus(true);
			positionOfferCost.setPosition_id(position.getId());
			
			positionOfferCost.setCreationDate(new Date());
			positionOfferCost.setModificationDate(new Date());
			
			positionOfferCostRepository.save(positionOfferCost);
			
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "", "Internal server error!");
		}
		
		return new RestResponse(RestResponse.SUCCESS, "", " Add position Cost Successfully !");
	}

	public RestResponse updatePositionOfferCost(PositionOfferCostDTO costDTO) throws RecruizException {
		
	Position position = positionService.getPositionById(costDTO.getPositionId());
		
	
	
		if(position==null)
			return new RestResponse(RestResponse.FAILED, "", "Position Not Found !");
		
		try{
			PositionOfferCost positionOfferCost = positionOfferCostRepository.getOne(costDTO.getId());
			
			if(positionOfferCost==null)
				return new RestResponse(RestResponse.FAILED, "", "Not Found any result for update !");
			
			positionOfferCost.setBillRate(costDTO.getBillRate());
			positionOfferCost.setBillHours(costDTO.getBillHours());
			positionOfferCost.setBillingDate(new Date());
			positionOfferCost.setProjectDuration(costDTO.getProjectDuration());
			positionOfferCost.setOneTimeCost(costDTO.getOneTimeCost());
			positionOfferCost.setHeadHunting(costDTO.getHeadHunting());
			positionOfferCost.setStatus(true);
			positionOfferCost.setPosition_id(position.getId());
			
			positionOfferCost.setModificationDate(new Date());
			
			positionOfferCostRepository.save(positionOfferCost);
			
		}catch(Exception e){
			e.printStackTrace();
			return new RestResponse(RestResponse.FAILED, "", "Internal server error!");
		}
		
		return new RestResponse(RestResponse.SUCCESS, "", " Update position Cost Successfully !");
	}

	public List<PositionOfferCost> findAllByPosition(String positionId) {
		
		return positionOfferCostRepository.findAllByPosition(positionId);
	}

}
