package com.bbytes.recruiz.integration.sixth.sense;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.integration.SixthSenseUser;
import com.bbytes.recruiz.service.AbstractService;

@Service
public class SixthSenseUserService extends AbstractService<SixthSenseUser, Long> {

	private SixthSenseUserRepository sixthSenseUserRepository;

	@Autowired
	public SixthSenseUserService(SixthSenseUserRepository sixthSenseUserRepository) {
		super(sixthSenseUserRepository);
		this.sixthSenseUserRepository = sixthSenseUserRepository;
	}

	public SixthSenseUser getUserByUserName(String userName) {
		return sixthSenseUserRepository.findByUserName(userName);
	}

	public SixthSenseUser findByUser(User user) {
		return sixthSenseUserRepository.findByUser(user);
	}
}
