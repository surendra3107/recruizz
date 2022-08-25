package com.bbytes.recruiz.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.Tax;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.TaxRepository;
import com.bbytes.recruiz.rest.dto.models.TaxDTO;
import com.bbytes.recruiz.utils.ErrorHandler;

@Service
public class TaxService extends AbstractService<Tax, Long> {

	private TaxRepository taxRepository;

	@Autowired
	private DTOToDomainConverstionService dTOToDomainConverstionService;

	@Autowired
	public TaxService(TaxRepository taxRepository) {
		super(taxRepository);
		this.taxRepository = taxRepository;
	}

	public Boolean isTaxNameExist(String taxName) throws RecruizException {
		if (taxName == null || taxName.isEmpty())
			throw new RecruizException(ErrorHandler.INVALID_TAX_NAME, ErrorHandler.INCORRECT_TAX_NAME);
		return taxRepository.findByTaxNameIgnoreCase(taxName) != null ? true : false;
	}
	
	public Tax getByTaxName(String taxName){
		return taxRepository.findByTaxNameIgnoreCase(taxName);
	}

	/*************
	 * To add Tax*
	 *************
	 * @param taxDTO
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Tax addTax(TaxDTO taxDTO) throws RecruizException {

		if (taxDTO == null)
			throw new RecruizException(ErrorHandler.INVALID_TAX_DTO, ErrorHandler.INCORRECT_TAX_DTO);

		if (taxDTO.getTaxName() == null || taxDTO.getTaxName().isEmpty())
			throw new RecruizException(ErrorHandler.INVALID_TAX_NAME, ErrorHandler.INCORRECT_TAX_NAME);

		if (isTaxNameExist(taxDTO.getTaxName())) {
			throw new RecruizException(ErrorHandler.TAX_NAME_ALREADY_EXIST, ErrorHandler.TAX_NAME_EXIST);
		}

		try {
			return save(dTOToDomainConverstionService.convertTax(taxDTO));
		} catch (Exception e) {
			throw new RecruizException(ErrorHandler.TAX_ADD_FAILED, ErrorHandler.TAX_ADD_FAILURE, e);
		}
	}

	/****************
	 * To update Tax*
	 ****************
	 * @param id
	 * @param taxDTO
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Tax updateTax(long id, TaxDTO taxDTO) throws RecruizException {

		if (!exists(id))
			throw new RecruizException(ErrorHandler.INVALID_TAX, ErrorHandler.TAX_NOT_FOUND);

		if (taxDTO == null)
			throw new RecruizException(ErrorHandler.INVALID_TAX_DTO, ErrorHandler.INCORRECT_TAX_DTO);

		if (taxDTO.getTaxName() == null || taxDTO.getTaxName().isEmpty())
			throw new RecruizException(ErrorHandler.INVALID_TAX_NAME, ErrorHandler.INCORRECT_TAX_NAME);

		if (taxDTO.getTaxNumber() == null)
			throw new RecruizException(ErrorHandler.TAX_NUMBER_INVALID, ErrorHandler.TAX_NUMBER_NOT_VALID);

		try {
			Tax updateTax = findOne(id);
			updateTax.setTaxName(taxDTO.getTaxName());
			updateTax.setTaxNumber(taxDTO.getTaxNumber());
			return save(updateTax);
		} catch (Exception e) {
			throw new RecruizException(ErrorHandler.TAX_UPDATE_FAILED, ErrorHandler.TAX_UPDATE_FAILURE, e);
		}
	}

	/*************
	 * To get Tax*
	 *************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	public Tax getTax(long id) throws RecruizException {
		if (!exists(id))
			throw new RecruizException(ErrorHandler.INVALID_TAX, ErrorHandler.TAX_NOT_FOUND);
		return findOne(id);
	}

	/******************
	 * To get all Tax**
	 ******************
	 * @return
	 */
	public List<Tax> getTax() {
		return findAll();
	}

	/****************
	 * To delete Tax*
	 ****************
	 * @param id
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Boolean deleteTax(long id) throws RecruizException {
		if (!exists(id))
			throw new RecruizException(ErrorHandler.INVALID_TAX, ErrorHandler.TAX_NOT_FOUND);
		try {
			delete(id);
			return !exists(id);
		} catch (Exception e) {
			throw new RecruizException(ErrorHandler.TAX_DELETE_FAILED, ErrorHandler.TAX_DELETE_FAILURE, e);
		}
	}

	/********************
	 * To delete all tax*
	 ********************
	 * @return
	 * @throws RecruizException
	 */
	@Transactional
	public Boolean deleteAllTax() throws RecruizException {
		try {
			deleteAll();
			if (findAll().isEmpty())
				return true;
			else
				return false;
		} catch (Exception e) {
			throw new RecruizException(ErrorHandler.TAX_DELETE_FAILED, ErrorHandler.TAX_DELETE_FAILURE, e);
		}
	}
}
