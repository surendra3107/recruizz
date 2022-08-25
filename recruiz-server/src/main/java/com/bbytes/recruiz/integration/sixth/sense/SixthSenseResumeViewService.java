package com.bbytes.recruiz.integration.sixth.sense;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bbytes.recruiz.domain.integration.SixthSenseResumeView;
import com.bbytes.recruiz.enums.integration.SixthSenseSource;
import com.bbytes.recruiz.service.AbstractService;
import com.bbytes.recruiz.utils.IntegrationConstants;

@Service
public class SixthSenseResumeViewService extends AbstractService<SixthSenseResumeView, String> {

	private SixthSenseResumeViewRepository resumeViewRepository;

	@Autowired
	public SixthSenseResumeViewService(SixthSenseResumeViewRepository userUsageRepository) {
		super(userUsageRepository);
		this.resumeViewRepository = userUsageRepository;
	}

	@Transactional(readOnly = true)
	public SixthSenseResumeView findByResumeId(String resumeId) {
		return resumeViewRepository.findByResumeId(resumeId);
	}

	@Transactional(readOnly = true)
	public boolean isResumeExpire(String resumeId) {

		int dayDiff = resumeViewRepository.getDateDiffenceOfViewResume(resumeId);

		return dayDiff > IntegrationConstants.SIXTH_SENSE_RESUME_VIEW_VALIDITY_DAYS ? true : false;
	}

	@Transactional
	public SixthSenseResumeView saveResumeView(String resumeId, String source) {

		SixthSenseResumeView resumeView = resumeViewRepository.findByResumeId(resumeId);
		if (resumeView == null) {
			resumeView = new SixthSenseResumeView();
			resumeView.setResumeId(resumeId);
			resumeView.setViewOnDate(new Date());
			resumeView.setSource(SixthSenseSource.valueOf(source).getDisplayName());
		}
		resumeView = resumeViewRepository.save(resumeView);
		return resumeView;
	}
}
