package com.bbytes.recruiz.search.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.domain.Candidate;
import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.Vendor;
import com.bbytes.recruiz.exception.RecruizEmptySearchCriteriaException;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.PositionRepository;
import com.bbytes.recruiz.rest.dto.models.ClientOpeningCountDTO;
import com.bbytes.recruiz.search.builder.PositionSearchBuilder;
import com.bbytes.recruiz.search.domain.PositionSearch;
import com.bbytes.recruiz.service.CheckUserPermissionService;
import com.bbytes.recruiz.service.TeamService;

public class PositionSearchRepoImpl extends AbstractSearchRepoImpl implements PositionSearchRepoCustom {

	@Autowired
	protected PositionRepository positionRepository;

	@Autowired
	private CheckUserPermissionService checkPermissionService;

	@Autowired
	private PositionSearchRepo positionSearchRepo;
	
	@Autowired
	private TeamService teamService;

	@Override
	public Class<?> getSearchClass() {
		return PositionSearch.class;
	}

	@Override
	public List<PositionSearch> getResult(PositionSearchBuilder positionSearchBuilder) throws RecruizException {
		applyAdditionalFilter(positionSearchBuilder);
		return positionSearchBuilder.executeQuery(getCurrentTenant(), elasticsearchTemplate);
	}

	@Override
	public List<Long> getResultAsIds(PositionSearchBuilder positionSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		return positionSearchBuilder.executeQueryForIds(getCurrentTenant(), elasticsearchTemplate);
	}

	/**
	 * Returns positions Ids with pageable, sortField and sort order
	 */
	@Override
	public Page<Long> getResultAsIds(PositionSearchBuilder positionSearchBuilder, Pageable pageable,
			String sortFieldName, SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		return positionSearchBuilder.executeQueryForIds(getCurrentTenant(), pageable, sortFieldName, order,
				elasticsearchTemplate);
	}

	@Override
	public List<Position> getAllResultAsNativeModel(PositionSearchBuilder positionSearchBuilder)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		return positionRepository.findAll(getResultAsIds(positionSearchBuilder));
	}

	@Override
	public List<Position> getResultAsNativeModelOwnerOrHrExec(PositionSearchBuilder positionSearchBuilder,
			User loggedInUser, Set<User> userSet) throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		return positionRepository.findDistinctByOwnerOrHrExecutivesInAndIdIn(loggedInUser.getEmail(), userSet,
				getResultAsIds(positionSearchBuilder));
	}

	@Override
	public Page<Position> getAllResultAsNativeModel(PositionSearchBuilder positionSearchBuilder, Pageable pageable)
			throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		final List<Long> ids = getResultAsIds(positionSearchBuilder);
		if (!ids.isEmpty())
			return positionRepository.findByIdIn(ids, pageable);
		return new PageImpl<Position>(new ArrayList<Position>());
	}

	/*
	 * @Override public Page<Position>
	 * getResultAsNativeModelOwnerOrHrExec(PositionSearchBuilder
	 * positionSearchBuilder, User loggedInUser, Set<User> userSet, Pageable
	 * pageable, String sortFieldName, SortOrder order) throws RecruizException,
	 * RecruizEmptySearchCriteriaException { Page<Position> result = new
	 * PageImpl<>(new ArrayList<Position>(), pageable, 0); //List<Position> result =
	 * new ArrayList<Position>(); applyAdditionalFilter(positionSearchBuilder);
	 * 
	 * //PageImpl<Long> ids = (PageImpl<Long>) getResultAsIds(positionSearchBuilder,
	 * pageable, sortFieldName, order); List<Position> ids =
	 * getAllResultAsNativeModel(positionSearchBuilder);
	 * //System.out.println("getNumber =="+ids.getNumber()+"getNumberOfElements  "
	 * +ids.getNumberOfElements()+"getTotalElements  "+ids.getTotalElements()
	 * +"getSize  "+ids.getSize()); final List<Long> pids = new ArrayList<>(); for
	 * (Position id : ids) { pids.add(id.getId()); }
	 * 
	 * if (!pids.isEmpty() && checkPermissionService.isSuperAdmin()) { result =
	 * positionRepository.findByIdIn(pids,pageable); //return new PageImpl<>(result,
	 * pageable, result.size()); return result;
	 * 
	 * }
	 * 
	 * //pids has list of all position based on search criteria. Now we need to show
	 * this based on owner or hr exec or teams. if (!pids.isEmpty()){
	 * 
	 * List<Team> teams =
	 * teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
	 * List<Position> res
	 * =positionRepository.findDistinctByOwnerOrHrExecutivesOrTeamInAndIdIn(
	 * loggedInUser.getEmail(), userSet, teams, pids, pageable.getSort()); result =
	 * new PageImpl<>(res, pageable, res.size());
	 * //positionRepository.findDistinctByOwnerOrHrExecutivesOrTeamInAndIdIn(
	 * loggedInUser.getEmail(), userSet, teams, pids, pageable.getSort());
	 * 
	 * //Old Repo Find query //result =
	 * positionRepository.findDistinctByOwnerAndIdInOrHrExecutivesInAndIdIn(
	 * loggedInUser.getEmail(), pids, userSet, pids, pageable.getSort()); }
	 * 
	 * 
	 * //return new PageImpl<>(result, pageable, result.size()); return result; }
	 */

	@Override
	public Page<Position> getResultAsNativeModelOwnerOrHrExec(PositionSearchBuilder positionSearchBuilder,
			User loggedInUser, Set<User> userSet, Pageable pageable, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException {

	    Page<Position> result  = null;
		applyAdditionalFilter(positionSearchBuilder);
		//List<Position> ids = positionSearchRepo.getAllResultAsNativeModel(positionSearchBuilder);
		List<Position> ids = getAllResultAsNativeModel(positionSearchBuilder);
		final List<Long> pids = new ArrayList<>();
		for (Position id : ids) {
			pids.add(id.getId());
		}

		if (!pids.isEmpty() && checkPermissionService.isSuperAdmin()) {
			List<Position> res = positionRepository.findByIdIn(pids);
		    
		    int start = pageable.getOffset();
			int end = (start + pageable.getPageSize()) > res.size() ? res.size()
					: (start + pageable.getPageSize());
			result = new PageImpl<Position>(res.subList(start, end),
					pageable, res.size());
		   
			return result;
		}
		// pids has list of all position based on search criteria. Now we need to show
		// this based on owner or hr exec or teams.    
		if (!pids.isEmpty()) {
			List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
			List<Position> res = positionRepository.findDistinctByOwnerOrHrExecutivesOrTeamInAndIdIn(
					loggedInUser.getEmail(), userSet, teams, pids, pageable.getSort());
			 result = new PageImpl<>(res, pageable, res.size());
			return result;
		}

		return result;
	}
	
	
	@Override
	public Page<Position> getResultAsNativeModelOwnerOrHrExecForNonAdmin(PositionSearchBuilder positionSearchBuilder,
			User loggedInUser, Set<User> userSet, Pageable pageable, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException {

	    Page<Position> result  = null;
		applyAdditionalFilter(positionSearchBuilder);
		List<Position> ids = positionSearchRepo.getAllResultAsNativeModel(positionSearchBuilder);
		//List<Position> idsgg = getAllResultAsNativeModel(positionSearchBuilder);
		final List<Long> pids = new ArrayList<>();
		for (Position id : ids) {
			pids.add(id.getId());
		}

		// pids has list of all position based on search criteria. Now we need to show
		// this based on owner or hr exec or teams.    
		if (!pids.isEmpty()) {
			List<Team> teams = teamService.getAllTeamsForUser(userService.getLoggedInUserObject());
			/*List<Position> res = positionRepository.findDistinctByOwnerOrHrExecutivesOrTeamInAndIdIn(
					loggedInUser.getEmail(), userSet, teams, pids, pageable.getSort());*/
			
			List<Position> res = positionRepository.findDistinctByOwnerOrHrExecutivesInOrTeamIn(loggedInUser.getEmail(),
					userService.loggedInUserSet(), teams,pageable.getSort());
			
			res.retainAll(ids);
			
			List<Position> dataByPageable = new ArrayList<>();
			if(res.size()>0){			
				int startData = pageable.getPageNumber()*10;
				int endData = startData + 10;

				if(endData<=res.size()){
					for (;startData<endData;startData++) {
						Position newData = res.get(startData);
						if(newData!=null){
							dataByPageable.add(newData);
						}
					}
				}else{	
					endData = res.size();

					for (;startData<endData;startData++) {
						Position newData = res.get(startData);
						if(newData!=null){
							dataByPageable.add(newData);
						}
					}
				}
			}
			
			
			
			 result = new PageImpl<>(dataByPageable, pageable, res.size());
			return result;
		}

		return result;
	}
	

	@Override
	public Page<Position> getResultAsNativeModelOwnerOrVendor(PositionSearchBuilder positionSearchBuilder,
			User loggedInUser, Set<Vendor> vendorSet, Pageable pageable, String sortFieldName, SortOrder order)
			throws RecruizException, RecruizEmptySearchCriteriaException {

		List<Position> result = new ArrayList<Position>();
		applyAdditionalFilter(positionSearchBuilder);
		PageImpl<Long> ids = (PageImpl<Long>) getResultAsIds(positionSearchBuilder, pageable, sortFieldName, order);
		final List<Long> pids = new ArrayList<>();
		for (Long id : ids) {
			pids.add(id);
		}
		if (!pids.isEmpty())
			result = positionRepository.findDistinctByOwnerAndIdInOrVendorsInAndIdIn(loggedInUser.getEmail(), pids,
					vendorSet, pids, pageable.getSort());

		return new PageImpl<>(result, pageable, result.size());
	}

	@Override
	public Page<Position> getAllResultAsNativeModelByClient(PositionSearchBuilder positionSearchBuilder, Client client,
			Pageable pageable) throws RecruizException, RecruizEmptySearchCriteriaException {
		applyAdditionalFilter(positionSearchBuilder);
		final List<Long> ids = getResultAsIds(positionSearchBuilder);
		if (!ids.isEmpty())
			return positionRepository.findByClientAndIdIn(client, ids, pageable);
		return new PageImpl<Position>(new ArrayList<Position>());
	}

	@Override
	public Page<Position> getResultAsNativeModelOwnerOrHrExecByClient(PositionSearchBuilder positionSearchBuilder,
			User loggedInUser, Set<User> userSet, Client client, Pageable pageable, String sortFieldName,
			SortOrder order) throws RecruizException, RecruizEmptySearchCriteriaException {

		Page<Position> result = new PageImpl<>(new ArrayList<Position>(), pageable, 0);
		applyAdditionalFilter(positionSearchBuilder);
		List<Position> ids = positionSearchRepo.getAllResultAsNativeModel(positionSearchBuilder);
		/*List<Long> idsii = getResultAsIds(positionSearchBuilder);  
		List<Position> po = getAllResultAsNativeModel(positionSearchBuilder);
		PageImpl<Long> ids = (PageImpl<Long>) getResultAsIds(positionSearchBuilder, pageable, sortFieldName, order);*/
		

		
		
		final List<Long> pids = new ArrayList<>();
		final List<Long> positionIds = new ArrayList<>();
		for (Position id : ids) {
			pids.add(id.getId());
		}

		if (!pids.isEmpty()) {

			if (checkPermissionService.isSuperAdmin()) {
				List<Position> res = positionRepository.findDistinctByClientAndIdIn(client, pids, pageable.getSort());

				for (Position position : res) {
					positionIds.add(position.getId());
				}

				result = positionRepository.findByIdIn(positionIds, pageable);

			} else {

				List<Position> res = positionRepository
						.findDistinctByClientAndOwnerAndIdInOrClientAndHrExecutivesInAndIdIn(client,
								loggedInUser.getEmail(), pids, client, userSet, pids, pageable.getSort());

				for (Position position : res) {
					positionIds.add(position.getId());
				}

				result = positionRepository.findByIdIn(positionIds, pageable);
			}

		}

		return result;

	}

}
