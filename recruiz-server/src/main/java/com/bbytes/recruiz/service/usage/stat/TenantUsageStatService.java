package com.bbytes.recruiz.service.usage.stat;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.bbytes.recruiz.domain.PlutusOrganizationInfo;
import com.bbytes.recruiz.domain.TenantResolver;
import com.bbytes.recruiz.domain.User;
import com.bbytes.recruiz.domain.integration.SixthSenseUser;
import com.bbytes.recruiz.exception.RecruizException;
import com.bbytes.recruiz.repository.TenantResolverRepository;
import com.bbytes.recruiz.repository.usage.stat.UsageStatRepository;
import com.bbytes.recruiz.utils.GlobalConstants;

@Service
public class TenantUsageStatService {

    private static Logger logger = LoggerFactory.getLogger(TenantUsageStatService.class);

    @Autowired
    private UsageStatRepository usageStatRepository;

    // create usage table for given tenant
    public void createUsageTableForTenant(String tenantID) {
	usageStatRepository.createTanantUsageTable(tenantID);
    }

    /**
     * To insert into usage table, this is a async call
     * 
     * @param tenant
     * @param userEmail
     * @param userName
     * @param actionType
     */
    @Async
    public void insertInToUsageStatTable(String tenant, String userEmail, String userName, String actionType) {
	usageStatRepository.insertIntoUsageTable(tenant, userEmail, userName, actionType);
    }

    // to get total logged in hour for a date
    public Long getUserLoggedInHourForDate(String tenant, String userEmail, Date startDate,Date endDate) {
	return usageStatRepository.toGetLoggedInHourForGivenDate(tenant, userEmail, startDate,endDate);
    }
    
 // to get total logged in hour for a date
    public Long getUserLoggedInHourForHourInterval(String tenant, String userEmail, Date startDate,Date endDate) {
	return usageStatRepository.toGetLoggedInHourForHourInterval(tenant, userEmail, startDate,endDate);
    }

    // to get total idle time for a user for given interval
    public Long getUserTotalIdleTimeForAInterval(String tenant, String userEmail, String intervalInMinute,
	    String idleTimeLimit) {
	return usageStatRepository.toGetTotalIdleTimeForInterval(tenant, userEmail, intervalInMinute, idleTimeLimit);
    }

    // to get total idle time of a user for given date
    public Long getIdleDurationForAUserForGivenDate(String tenant, String userEmail, String idleTimeOut,
	    Date startDate,Date endDate) {
	return usageStatRepository.getIdleTimeForADay(tenant, userEmail, startDate,endDate, idleTimeOut);
    }

    // to get total active user count for given date
    public Long getActiveUserCount(String tenant, Date dateToCheck) {
	return usageStatRepository.getActiveUserCountForGivenDate(tenant, dateToCheck);
    }
    
    // to get total active user count for given date
    public Long getIdleTimeForAllUser(String tenant, String interval) {
	return usageStatRepository.getTotalIdleForAllUserForInterval(tenant, interval, GlobalConstants.USER_IDLE_TIMEOUT);
    }
    
 // to get total active user count for given date
    public Long getTotalLoggedinTiimeFrTenantForDate(String tenant, Date date,Date endDate) {
	return usageStatRepository.toGetLoggedInHourForGivenDateForAllUser(tenant, date,endDate);
    }
    
    public Long getTotalLoggedinTimeForTenantForHour(String tenant, Date date,Date endDate) {
 	return usageStatRepository.toGetLoggedInHourForHourIntervalForAllUser(tenant, date,endDate);
     }
    
    public Long getTotalIdleTimeForTenant(String tenant, String idleTimeOut, String startDate,String endDate) {
	return usageStatRepository.getIdleTimeForADayForTenant(tenant, idleTimeOut,startDate,endDate);
    }
    
    

}
