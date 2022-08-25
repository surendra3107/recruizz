package com.bbytes.recruiz.repository.event;

import javax.persistence.PostRemove;

import org.springframework.beans.factory.annotation.Autowired;

import com.bbytes.recruiz.domain.CustomFields;
import com.bbytes.recruiz.enums.CustomFieldEntityType;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.service.CandidateService;
import com.bbytes.recruiz.service.ClientService;
import com.bbytes.recruiz.service.EmployeeService;
import com.bbytes.recruiz.service.PositionService;
import com.bbytes.recruiz.service.ProspectService;
import com.bbytes.recruiz.service.SpringProfileService;
import com.bbytes.recruiz.utils.AutowireHelper;

/**
 * Equivalent of a domain method annotated by <code>PrePersist</code>.
 * <p/>
 * This handler shows how to implement your custom UUID generation.
 * 
 */
public class CustomFieldDBEventListener {

	@Autowired
	private SpringProfileService springProfileService;
	
	@Autowired
	private CandidateService candidateService;
	
	@Autowired
	private ClientService clientService;
	
	@Autowired
	private EmployeeService employeeService;
	
	@Autowired
	private ProspectService prospectService;

	@Autowired
	private PositionService positionService;
	
	/**
	 * Remove uses from tenant resolver after user delete command
	 * 
	 * @throws RecruizException
	 */
	@PostRemove
	public void afterDelete(CustomFields customFields) throws RecruizException {
		AutowireHelper.autowire(this, this.springProfileService);
		AutowireHelper.autowire(this, this.candidateService);
		AutowireHelper.autowire(this, this.clientService);
		AutowireHelper.autowire(this, this.employeeService);
		AutowireHelper.autowire(this, this.positionService);
		AutowireHelper.autowire(this, this.prospectService);
		

		CustomFieldEntityType customFieldEntityType = customFields.getEntityType();
		if(customFieldEntityType.equals(CustomFieldEntityType.Candidate)) {
			candidateService.deleteCustomFieldWithName(customFields.getName());
		} else if(customFieldEntityType.equals(CustomFieldEntityType.Client)) {
			clientService.deleteCustomFieldWithName(customFields.getName());
		}
		else if(customFieldEntityType.equals(CustomFieldEntityType.Employees)) {
			employeeService.deleteCustomFieldWithName(customFields.getName());
		}
		else if(customFieldEntityType.equals(CustomFieldEntityType.Position)) {
			positionService.deleteCustomFieldWithName(customFields.getName());
		}
		else if(customFieldEntityType.equals(CustomFieldEntityType.Prospects)) {
			prospectService.deleteCustomFieldWithName(customFields.getName());
		}
	}

}