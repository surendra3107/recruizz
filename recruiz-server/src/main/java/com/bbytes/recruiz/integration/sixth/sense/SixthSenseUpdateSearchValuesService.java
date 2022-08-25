package com.bbytes.recruiz.integration.sixth.sense;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.integration.SixthSenseCity;
import com.bbytes.recruiz.domain.integration.SixthSenseFunctionalArea;
import com.bbytes.recruiz.domain.integration.SixthSenseFunctionalAreaRole;
import com.bbytes.recruiz.domain.integration.SixthSenseIndustry;
import com.bbytes.recruiz.domain.integration.SixthSensePGDegree;
import com.bbytes.recruiz.domain.integration.SixthSensePGDegreeSpecialization;
import com.bbytes.recruiz.domain.integration.SixthSensePPGDegree;
import com.bbytes.recruiz.domain.integration.SixthSensePPGDegreeSpecialization;
import com.bbytes.recruiz.domain.integration.SixthSenseUGDegree;
import com.bbytes.recruiz.domain.integration.SixthSenseUGDegreeSpecialization;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.service.TenantResolverService;
import com.bbytes.recruiz.utils.ErrorHandler;
import com.bbytes.recruiz.utils.TenantContextHolder;
import com.google.common.collect.Lists;

/**
 * This will read from excel file all constant search values and update the
 * database. if the property ("sixth.sense.search.values.update") is set to true
 * in properties file.
 * 
 * @author akshay
 *
 */

@Service("SixthSenseUpdateSearchValuesService")
public class SixthSenseUpdateSearchValuesService {

	private static final Logger logger = LoggerFactory.getLogger(SixthSenseUpdateSearchValuesService.class);

	@Value("${sixth.sense.search.values.update}")
	private boolean isValueUpdateable;

	@Autowired
	private TenantResolverService tenantResolverService;

	@Autowired
	private SixthSenseCityService sixthSenseCityService;

	@Autowired
	private SixthSenseIndustryService senseIndustryService;

	@Autowired
	private SixthSenseFuncAreaService sixthSenseFuncAreaService;

	@Autowired
	private SixthSenseFuncAreaRoleService sixthSenseFuncAreaRoleService;

	@Autowired
	private SixthSensePPGDegreeService sixthSensePPGDegreeService;

	@Autowired
	private SixthSensePPGDegreeSpecService sixthSensePPGDegreeSpecService;

	@Autowired
	private SixthSensePGDegreeService sixthSensePGDegreeService;

	@Autowired
	private SixthSensePGDegreeSpecService sixthSensePGDegreeSpecService;

	@Autowired
	private SixthSenseUGDegreeService sixthSenseUGDegreeService;

	@Autowired
	private SixthSenseUGDegreeSpecService sixthSenseUGDegreeSpecService;

	@Autowired
	private ResourceLoader resourceloader;

	@PostConstruct
	public void init() throws Exception {
		// this flag will check if the search values should be updated in
		// database
		if (!isValueUpdateable) {
			return;
		}

		logger.error("**************Updating search values from excel file ************");

		List<String> existingTenants = tenantResolverService.findAllTenants();
		if (existingTenants != null && !existingTenants.isEmpty()) {
			for (String tenant : existingTenants) {
				try {
					TenantContextHolder.setTenant(tenant);	
					updateSearchValue();
				} finally {
					TenantContextHolder.clearContext();
				}

			}
		}
	}

	@Async
	public void updateSixthSenseSearchValue(String tenantId) throws IOException, RecruizException {
		TenantContextHolder.setTenant(tenantId);
		updateSearchValue();
	}

	private void updateSearchValue() throws IOException, RecruizException {
		logger.debug("Current tenant is " + TenantContextHolder.getTenant());

		List<Map<Integer, String>> rowAsMapCityList = updateSearchValuesFromFile(SixthSenseFileConstants.CITY_FILE);
		saveCity(rowAsMapCityList);
		logger.error("**************Cities values saved ************");

		List<Map<Integer, String>> rowAsMapIndustryList = updateSearchValuesFromFile(SixthSenseFileConstants.INDUSTRY_FILE);
		saveIndustry(rowAsMapIndustryList);
		logger.error("**************Industry values saved ************");

		List<Map<Integer, String>> rowAsMapFunctionalAreaList = updateSearchValuesFromFile(SixthSenseFileConstants.FUNCTIONAL_AREA_FILE);
		saveFuncationalArea(rowAsMapFunctionalAreaList);
		logger.error("**************Functional area values saved ************");

		List<Map<Integer, String>> rowAsMapFunctionalAreaRoleList = updateSearchValuesFromFile(
				SixthSenseFileConstants.FUNCTIONAL_AREA_ROLES_FILE);
		saveFuncationalAreaRole(rowAsMapFunctionalAreaRoleList);
		logger.error("**************Functional area roles values saved ************");

		List<Map<Integer, String>> rowAsMapPPGDegreeList = updateSearchValuesFromFile(SixthSenseFileConstants.PPG_DEGREE_FILE);
		savePPGDegree(rowAsMapPPGDegreeList);
		logger.error("**************PPG Degree values saved ************");

		List<Map<Integer, String>> rowAsMapPPGDegreeSpecList = updateSearchValuesFromFile(
				SixthSenseFileConstants.PPG_DEGREE_SPECIALIZATION_FILE);
		savePPGDegreeSpec(rowAsMapPPGDegreeSpecList);
		logger.error("**************PPG Degree Specialization values saved ************");

		List<Map<Integer, String>> rowAsMapPGDegreeList = updateSearchValuesFromFile(SixthSenseFileConstants.PG_DEGREE_FILE);
		savePGDegree(rowAsMapPGDegreeList);
		logger.error("**************PG Degree values saved ************");

		List<Map<Integer, String>> rowAsMapPGDegreeSpecList = updateSearchValuesFromFile(
				SixthSenseFileConstants.PG_DEGREE_SPECIALIZATION_FILE);
		savePGDegreeSpec(rowAsMapPGDegreeSpecList);
		logger.error("**************PG Degree Specialization values saved ************");

		List<Map<Integer, String>> rowAsMapUGDegreeList = updateSearchValuesFromFile(SixthSenseFileConstants.UG_DEGREE_FILE);
		saveUGDegree(rowAsMapUGDegreeList);
		logger.error("**************UG Degree values saved ************");

		List<Map<Integer, String>> rowAsMapUGDegreeSpecList = updateSearchValuesFromFile(
				SixthSenseFileConstants.UG_DEGREE_SPECIALIZATION_FILE);
		saveUGDegreeSpec(rowAsMapUGDegreeSpecList);
		logger.error("**************UG Degree Specialization values saved ************");
	}

	@Transactional
	private void saveCity(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		sixthSenseCityService.delete(sixthSenseCityService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSenseCity> cities = new ArrayList<SixthSenseCity>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSenseCity sixthSenseCity = new SixthSenseCity();
				sixthSenseCity.setName(rowAsMap.get(0));
				sixthSenseCity.setCode(rowAsMap.get(1));
				sixthSenseCity.setGroupLabel(rowAsMap.get(2));
				cities.add(sixthSenseCity);
			}
			sixthSenseCityService.save(cities);
		}
	}

	@Transactional
	private void saveIndustry(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		senseIndustryService.delete(senseIndustryService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSenseIndustry> industryList = new ArrayList<SixthSenseIndustry>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSenseIndustry sixthSenseIndustry = new SixthSenseIndustry();
				sixthSenseIndustry.setName(rowAsMap.get(0));
				sixthSenseIndustry.setCode(rowAsMap.get(1));
				industryList.add(sixthSenseIndustry);
			}
			senseIndustryService.save(industryList);
		}
	}

	@Transactional
	private void saveFuncationalArea(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		sixthSenseFuncAreaService.delete(sixthSenseFuncAreaService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSenseFunctionalArea> functionalAreaList = new ArrayList<SixthSenseFunctionalArea>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSenseFunctionalArea sixthSenseFunctionalArea = new SixthSenseFunctionalArea();
				sixthSenseFunctionalArea.setName(rowAsMap.get(0));
				sixthSenseFunctionalArea.setCode(rowAsMap.get(1));
				functionalAreaList.add(sixthSenseFunctionalArea);
			}
			sixthSenseFuncAreaService.save(functionalAreaList);
		}
	}

	@Transactional
	private void saveFuncationalAreaRole(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		sixthSenseFuncAreaRoleService.delete(sixthSenseFuncAreaRoleService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSenseFunctionalAreaRole> functionalAreaRoleList = new ArrayList<SixthSenseFunctionalAreaRole>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSenseFunctionalAreaRole sixthSenseFunctionalAreaRole = new SixthSenseFunctionalAreaRole();
				sixthSenseFunctionalAreaRole.setName(rowAsMap.get(0));
				sixthSenseFunctionalAreaRole.setCode(rowAsMap.get(1));
				sixthSenseFunctionalAreaRole.setFunctionalAreaCode(rowAsMap.get(2));
				sixthSenseFunctionalAreaRole.setGroupLabel(rowAsMap.get(3));
				functionalAreaRoleList.add(sixthSenseFunctionalAreaRole);
			}
			sixthSenseFuncAreaRoleService.save(functionalAreaRoleList);
		}
	}

	@Transactional
	private void savePPGDegree(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		sixthSensePPGDegreeService.delete(sixthSensePPGDegreeService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSensePPGDegree> ppgDegreeList = new ArrayList<SixthSensePPGDegree>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSensePPGDegree sixthSensePPGDegree = new SixthSensePPGDegree();
				sixthSensePPGDegree.setName(rowAsMap.get(0));
				sixthSensePPGDegree.setCode(rowAsMap.get(1));
				ppgDegreeList.add(sixthSensePPGDegree);
			}
			sixthSensePPGDegreeService.save(ppgDegreeList);
		}
	}

	@Transactional
	private void savePPGDegreeSpec(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		sixthSensePPGDegreeSpecService.delete(sixthSensePPGDegreeSpecService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSensePPGDegreeSpecialization> ppgDegreeSpecList = new ArrayList<SixthSensePPGDegreeSpecialization>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSensePPGDegreeSpecialization sixthSensePPGDegreeSpec = new SixthSensePPGDegreeSpecialization();
				sixthSensePPGDegreeSpec.setName(rowAsMap.get(0));
				sixthSensePPGDegreeSpec.setCode(rowAsMap.get(1));
				sixthSensePPGDegreeSpec.setDegreeCode(rowAsMap.get(2));
				sixthSensePPGDegreeSpec.setGroupLabel(rowAsMap.get(3));
				ppgDegreeSpecList.add(sixthSensePPGDegreeSpec);
			}
			sixthSensePPGDegreeSpecService.save(ppgDegreeSpecList);
		}
	}

	@Transactional
	private void savePGDegree(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		sixthSensePGDegreeService.delete(sixthSensePGDegreeService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSensePGDegree> pgDegreeList = new ArrayList<SixthSensePGDegree>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSensePGDegree sixthSensePGDegree = new SixthSensePGDegree();
				sixthSensePGDegree.setName(rowAsMap.get(0));
				sixthSensePGDegree.setCode(rowAsMap.get(1));
				pgDegreeList.add(sixthSensePGDegree);
			}
			sixthSensePGDegreeService.save(pgDegreeList);
		}
	}

	@Transactional
	private void savePGDegreeSpec(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		sixthSensePGDegreeSpecService.delete(sixthSensePGDegreeSpecService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSensePGDegreeSpecialization> pgDegreeSpecList = new ArrayList<SixthSensePGDegreeSpecialization>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSensePGDegreeSpecialization sixthSensePGDegreeSpec = new SixthSensePGDegreeSpecialization();
				sixthSensePGDegreeSpec.setName(rowAsMap.get(0));
				sixthSensePGDegreeSpec.setCode(rowAsMap.get(1));
				sixthSensePGDegreeSpec.setDegreeCode(rowAsMap.get(2));
				sixthSensePGDegreeSpec.setGroupLabel(rowAsMap.get(3));
				pgDegreeSpecList.add(sixthSensePGDegreeSpec);
			}
			sixthSensePGDegreeSpecService.save(pgDegreeSpecList);
		}
	}

	@Transactional
	private void saveUGDegree(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		sixthSenseUGDegreeService.delete(sixthSenseUGDegreeService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSenseUGDegree> ugDegreeList = new ArrayList<SixthSenseUGDegree>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSenseUGDegree sixthSenseUGDegree = new SixthSenseUGDegree();
				sixthSenseUGDegree.setName(rowAsMap.get(0));
				sixthSenseUGDegree.setCode(rowAsMap.get(1));
				ugDegreeList.add(sixthSenseUGDegree);
			}
			sixthSenseUGDegreeService.save(ugDegreeList);
		}
	}

	@Transactional
	private void saveUGDegreeSpec(List<Map<Integer, String>> rowAsMapList) {

		// dropping all values and saving from file
		sixthSenseUGDegreeSpecService.delete(sixthSenseUGDegreeSpecService.findAll());

		// each 0-100 chunks making into 20 each partition
		List<List<Map<Integer, String>>> rowAsListChunks = Lists.partition(rowAsMapList, 20);

		for (List<Map<Integer, String>> rowAsListChunk : rowAsListChunks) {

			List<SixthSenseUGDegreeSpecialization> ugDegreeSpecList = new ArrayList<SixthSenseUGDegreeSpecialization>();
			for (Map<Integer, String> rowAsMap : rowAsListChunk) {
				SixthSenseUGDegreeSpecialization sixthSenseuGDegreeSpec = new SixthSenseUGDegreeSpecialization();
				sixthSenseuGDegreeSpec.setName(rowAsMap.get(0));
				sixthSenseuGDegreeSpec.setCode(rowAsMap.get(1));
				sixthSenseuGDegreeSpec.setDegreeCode(rowAsMap.get(2));
				sixthSenseuGDegreeSpec.setGroupLabel(rowAsMap.get(3));
				ugDegreeSpecList.add(sixthSenseuGDegreeSpec);
			}
			sixthSenseUGDegreeSpecService.save(ugDegreeSpecList);
		}
	}

	/**
	 * to update search constant values stored in file
	 * 
	 * @throws IOException
	 * @throws RecruizException
	 */
	private List<Map<Integer, String>> updateSearchValuesFromFile(String filePath) throws IOException, RecruizException {
		List<Map<Integer, String>> rowAsMapList = new ArrayList<Map<Integer, String>>();
		try {
			Workbook workbook;
			InputStream excelFile = resourceloader.getResource(filePath).getInputStream();

			workbook = new XSSFWorkbook(excelFile);

			Sheet workSheet = workbook.getSheetAt(0);

			// Iterate through each rows from first sheet
			Iterator<Row> iterator = workSheet.iterator();

			while (iterator.hasNext()) {
				Map<Integer, String> rowAsMap = new LinkedHashMap<Integer, String>();
				Row row = iterator.next();
				if (row.getRowNum() == 0) {
					continue;// skip first row, as it contains column names
				}

				// For each row, iterate through each columns
				for (int j = 0; j < row.getLastCellNum(); j++) {

					Cell cell = row.getCell(j);

					if (cell != null) {
						if (CellType.BLANK == cell.getCellTypeEnum()) {
							continue;
						}
						cell.setCellType(CellType.STRING);
						if (CellType.STRING == cell.getCellTypeEnum()) {
							rowAsMap.put(cell.getColumnIndex(), cell.getStringCellValue());
						} else if (CellType.NUMERIC == cell.getCellTypeEnum()) {
							rowAsMap.put(cell.getColumnIndex(), String.valueOf(cell.getNumericCellValue()));
						}
					}
				}
				// adding each row of file into list
				if (rowAsMap != null && !rowAsMap.isEmpty())
					rowAsMapList.add(rowAsMap);
			}
			workbook.close();
		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), ErrorHandler.FILE_DOES_NOT_EXIST);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
			throw new RecruizException(e.getMessage(), "Error while reading file");
		}
		return rowAsMapList;
	}

}