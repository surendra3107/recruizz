package com.bbytes.recruiz.repository;

import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.bbytes.recruiz.domain.Client;
import com.bbytes.recruiz.domain.Position;
import com.bbytes.recruiz.domain.Team;
import com.bbytes.recruiz.domain.User;

public interface PositionRepositoryCustom {

    Page<Position> findPositionForClientAndOwnerOrClientAndHrExecutivesIn(Client client, String ownerEmail,
	    Set<User> users, List<Team> teams, Pageable pageable);

    Page<Position> findPositionForClientAndOwnerOrClientAndHrExecutivesIn(Client client, String ownerEmail,
	    Set<User> users, Pageable pageable);
}
