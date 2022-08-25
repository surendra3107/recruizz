package com.bbytes.recruiz.service;

import java.io.File;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Employee;
import com.bbytes.recruiz.domain.EmployeeActivity;
import com.bbytes.recruiz.domain.EmployeeFile;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.RoundCandidate;
import com.bbytes.recruiz.enums.BoardStatus;
import com.bbytes.recruiz.enums.EmployeeStatus;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.EmployeeRepository;
import com.bbytes.recruiz.rest.dto.models.EmployeeDTO;
import com.bbytes.recruiz.rest.dto.models.YetToOnBoardEmployeeeDTO;
import com.bbytes.recruiz.utils.GlobalConstants;

@Service
public class EmployeeService extends AbstractService<Employee, Long> {

	private EmployeeRepository employeeRepository;

	@Autowired
	public EmployeeService(EmployeeRepository employeeRepository) {
		super(employeeRepository);
		this.employeeRepository = employeeRepository;
	}

	@Autowired
	private ReportService reportService;

	@Autowired
	private RoundCandidateService roundCandidateService;

	@Autowired
	private OnBoardingDetailsService onBoardingDetailsService;

	@Autowired
	private PositionService positionService;

	@Autowired
	private EmployeeActivityService activityService;

	@Autowired
	private CandidateService candidateService;

	@Autowired
	private EmployeeFileService employeeFileService;

	@Transactional
	public Employee editEmployee(Long eid, EmployeeDTO employeeDTO) throws RecruizException {
		Employee oldEmployee = employeeRepository.findOne(eid);
		oldEmployee.setAlternateEmail(employeeDTO.getAlternateEmail());
		oldEmployee.setCity(employeeDTO.getCity());
		oldEmployee.setCountry(employeeDTO.getCountry());
		oldEmployee.setDob(employeeDTO.getDob());
		oldEmployee.setDoj(employeeDTO.getDoj());
		oldEmployee.setEmpID(employeeDTO.getEmpID());
		oldEmployee.setEmploymentStatus(employeeDTO.getEmploymentStatus());
		oldEmployee.setEmploymentType(employeeDTO.getEmploymentType());
		oldEmployee.setFirstName(employeeDTO.getFirstName());
		oldEmployee.setGender(employeeDTO.getGender());
		oldEmployee.setHrContact(employeeDTO.getHrContact());
		oldEmployee.setJobLocation(employeeDTO.getJobLocation());
		oldEmployee.setJobTitle(employeeDTO.getJobTitle());
		oldEmployee.setLastName(employeeDTO.getLastName());
		oldEmployee.setMiddleName(employeeDTO.getMiddleName());
		oldEmployee.setOfficialEmail(employeeDTO.getOfficialEmail());
		oldEmployee.setPlacedAt(employeeDTO.getPlacedAt());
		oldEmployee.setPostalCode(employeeDTO.getPostalCode());
		oldEmployee.setPrimaryContact(employeeDTO.getPrimaryContact());
		oldEmployee.setReportingManager(employeeDTO.getReportingManager());
		oldEmployee.setRole(employeeDTO.getRole());
		oldEmployee.setState(employeeDTO.getState());
		oldEmployee.setStreet(employeeDTO.getStreet());
		oldEmployee.setTeam(employeeDTO.getTeam());
		oldEmployee.setCustomField(employeeDTO.getCustomField());
		employeeRepository.save(oldEmployee);

		if (employeeDTO.getEmploymentStatus().equalsIgnoreCase(EmployeeStatus.MovedOut.name())) {
			Candidate candidate = candidateService.getCandidateByEmail(oldEmployee.getPresonalEmail());
			if (null != candidate) {
				RoundCandidate roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate, oldEmployee.getPositionCode());
				if (roundCandidate != null) {
					roundCandidate.setStatus(BoardStatus.MovedOut.name());
					roundCandidateService.save(roundCandidate);
				}
			}
		}

		return oldEmployee;
	}

	public Page<Employee> getAllEmployee(Pageable pageRequestObject) {
		Page<Employee> employeeList = employeeRepository.findAll(pageRequestObject);
		return employeeList;
	}

	public Page<EmployeeDTO> getAllEmployeeDTO(Pageable pageRequestObject) {
		Page<Employee> employeeList = employeeRepository.findAll(pageRequestObject);
		List<EmployeeDTO> empDTOList = new LinkedList<>();
		for (Employee emp : employeeList) {
			EmployeeDTO empDTO = new EmployeeDTO();
			empDTO.setEmpID(emp.getEmpID());
			empDTO.setFirstName(emp.getFirstName());
			empDTO.setLastName(empDTO.getLastName());
			empDTO.setClientName(emp.getClientName());
			empDTO.setPositionName(emp.getPositionTitle());
			empDTO.setJobTitle(emp.getJobTitle());
			empDTO.setDoj(emp.getDoj());
			empDTO.setEmploymentStatus(emp.getEmploymentStatus());
			empDTO.setEmploymentType(emp.getEmploymentType());
			empDTO.setID(emp.getId());
			empDTO.setCompletedOnBoardingCount(
					onBoardingDetailsService.getOnBoardingCountForEmployee(emp, GlobalConstants.OnBoardingTaskStateNew, true));
			empDTO.setInProgressOnBoardingCount(
					onBoardingDetailsService.getOnBoardingCountForEmployee(emp, GlobalConstants.OnBoardingTaskStateNew, false));
			empDTOList.add(empDTO);
		}
		Page<EmployeeDTO> employeeDto = new PageImpl<>(empDTOList, pageRequestObject, employeeList.getTotalElements());
		return employeeDto;
	}

	// to get employeee dashboard count
	public Map<String, Object> getDashboardCount() throws RecruizException {
		return reportService.getEmployeeDashBoardCount();
	}

	@Transactional
	public void saveEmployee(Employee employee) {
		Employee existingEmployee = employeeRepository.findByPresonalEmail(employee.getPresonalEmail());
		if (null != existingEmployee) {
			employee.setId(existingEmployee.getId());
			// List<OnBoardingDetails> existingOnBoardingDetails =
			// onBoardingDetailsService.get
		}
		save(employee);
	}

	public Page<YetToOnBoardEmployeeeDTO> getYetToOnBoardEmployeeDTO(Pageable pageRequestObject) {
		Page<RoundCandidate> roundCandidates = roundCandidateService.getYetToBoardCandidate(pageRequestObject);
		List<YetToOnBoardEmployeeeDTO> yetToOnBoardList = new LinkedList<>();
		for (RoundCandidate roundCandidate : roundCandidates) {
			if (null != yetToOnBoardList && containsProcessedEmail(yetToOnBoardList, roundCandidate.getCandidate().getEmail())) {
				continue;
			}

			List<RoundCandidate> allOfferedCandidate = roundCandidateService
					.getCandidateByStatusAndCandidate(roundCandidate.getCandidate());
			Map<String, String> positionTitleMap = new HashMap<>();
			Map<String, String> positionStatusMap = new HashMap<>();
			Map<String, String> positionRoundMap = new HashMap<>();
			for (RoundCandidate candidate : allOfferedCandidate) {
				Position position = positionService.getOneByPositionCode(candidate.getPositionCode());
				positionTitleMap.put(candidate.getPositionCode(), position.getClient().getClientName() + " / " + position.getTitle());
				positionStatusMap.put(candidate.getPositionCode(), BoardStatus.valueOf(candidate.getStatus()).getDisplayName());
				positionRoundMap.put(candidate.getPositionCode(), candidate.getRound().getId() + "");
			}

			YetToOnBoardEmployeeeDTO yetToBeEmployee = new YetToOnBoardEmployeeeDTO();
			yetToBeEmployee.setName(roundCandidate.getCandidate().getFullName());
			yetToBeEmployee.setEmailID(roundCandidate.getCandidate().getEmail());
			yetToBeEmployee.setCurrentlyOfferedPosition(positionTitleMap);
			yetToBeEmployee.setOfferedPositionStatus(positionStatusMap);
			yetToBeEmployee.setOfferedPositionRoundMap(positionRoundMap);
			yetToBeEmployee.setOfferedInPositionCount(positionTitleMap.size());
			yetToOnBoardList.add(yetToBeEmployee);

		}
		Page<YetToOnBoardEmployeeeDTO> page = new PageImpl<>(yetToOnBoardList, pageRequestObject, roundCandidates.getTotalElements());
		return page;
	}

	public boolean containsProcessedEmail(List<YetToOnBoardEmployeeeDTO> list, String email) {
		for (YetToOnBoardEmployeeeDTO object : list) {
			if (object.getEmailID().equalsIgnoreCase(email)) {
				return true;
			}
		}
		return false;
	}

	public Page<EmployeeActivity> getAllActivityForEmployee(Long eid, Pageable pageRequestObject) {
		return activityService.getEmployeeActivity(eid, pageRequestObject);
	}

	// to get employee details by personal email
	public Employee getEmployeeByPresonalEmail(String emailId) {
		return employeeRepository.findByPresonalEmail(emailId);
	}

	@Transactional(readOnly = true)
	public EmployeeDTO getEmployeeeById(Long eid) {
		Employee emp = employeeRepository.findOne(eid);
		if (null == emp) {
			return null;
		}
		EmployeeDTO empDTO = convertEmployeeeToEmpDTO(emp);
		return empDTO;
	}

	// to convert employee to employee dto object
	private EmployeeDTO convertEmployeeeToEmpDTO(Employee emp) {
		EmployeeDTO empDTO = new EmployeeDTO();
		empDTO.setEmpID(emp.getEmpID());
		empDTO.setFirstName(emp.getFirstName());
		empDTO.setLastName(empDTO.getLastName());
		empDTO.setClientName(emp.getClientName());
		empDTO.setPositionName(emp.getPositionTitle());
		empDTO.setDoj(emp.getDoj());
		empDTO.setID(emp.getId());
		empDTO.setAlternateEmail(emp.getAlternateEmail());
		empDTO.setCity(emp.getCity());
		empDTO.setCountry(emp.getCountry());
		empDTO.setDob(emp.getDob());
		empDTO.setEmpID(emp.getEmpID());
		empDTO.setEmploymentStatus(emp.getEmploymentStatus());
		empDTO.setEmploymentType(emp.getEmploymentType());
		empDTO.setGender(emp.getGender());
		empDTO.setHrContact(emp.getHrContact());
		empDTO.setID(emp.getId());
		empDTO.setJobLocation(emp.getJobLocation());
		empDTO.setJobTitle(emp.getJobTitle());
		empDTO.setMiddleName(emp.getMiddleName());
		empDTO.setOfficialEmail(emp.getOfficialEmail());
		empDTO.setPlacedAt(emp.getPlacedAt());
		empDTO.setPostalCode(emp.getPostalCode());
		empDTO.setPersonalEmail(emp.getPresonalEmail());
		empDTO.setPrimaryContact(emp.getPrimaryContact());
		empDTO.setReportingManager(emp.getReportingManager());
		empDTO.setRole(emp.getRole());
		empDTO.setState(emp.getState());
		empDTO.setStreet(emp.getStreet());
		empDTO.setTeam(emp.getTeam());
		
		if (emp.getCustomField() != null && !emp.getCustomField().isEmpty())
			empDTO.getCustomField().putAll(emp.getCustomField());
		
		return empDTO;
	}

	@Transactional
	public void deleteEmployee(Long eid) {
		try {
			Employee emp = employeeRepository.findOne(eid);

			Candidate candidate = candidateService.getCandidateByEmail(emp.getPresonalEmail());
			if (null != candidate) {
				RoundCandidate roundCandidate = roundCandidateService.getRoundcandidateByPosition(candidate, emp.getPositionCode());
				if (roundCandidate != null) {
					roundCandidate.setStatus(BoardStatus.MovedOut.name());
					roundCandidateService.save(roundCandidate);
				}
			}
			List<EmployeeFile> files = employeeFileService.getEmployeeFilesByeid(eid + "");
			if (null != files && !files.isEmpty()) {
				for (EmployeeFile employeeFile : files) {
					File file = new File(employeeFile.getFilePath());
					if (file.exists()) {
						file.delete();
					}
				}
				employeeFileService.delete(files);
			}

			employeeRepository.delete(emp);
		} catch (Exception ex) {

		}

	}

	public Employee getByEmpId(String empId) {
		return employeeRepository.findByEmpID(empId);
	}
	
	public void deleteCustomFieldWithName(String name) {
		employeeRepository.deleteCustomFieldWithName(name);
	}

}
