package com.bbytes.recruiz.utils;

import com.bbytes.recruiz.domain.User;

public class ActivityMessageConstants {

    public static String getCreatedByMsg(User user) {
	return "Created by " + user.getName() + "(" + user.getEmail() + ")";
    }
    
    public static String getPositionCreatedByMsg(User user) {
	return "Position created by " + user.getName() + "(" + user.getEmail() + ")";
    }

    public static String getUpdatedByMsg(User user) {
	return "Modified by " + user.getName() + "(" + user.getEmail() + ")";
    }

    public static String getStatusChangedMsg(User user,String oldStatus,String newStatus) { 
	return "Status changed from " + oldStatus +" to " + newStatus +" by "+ user.getName() + "(" + user.getEmail() + ")";
    }
    
    public static String getInterviewerModifiedMsg(User user) {
	return "Interviewer(s) modified by "+ user.getName() + "(" + user.getEmail() + ")";
    }
    
    public static String getDMModifiedMsg(User user) {
	return "Decision Maker(s) modified by "+ user.getName() + "(" + user.getEmail() + ")";
    }
    
    public static String getNewInterviewerAddedMsg(User user) {
	return "Some new interviewer(s) added by "+ user.getName() + "(" + user.getEmail() + ")";
    }

    public static String getInterviewerRemovedMsg(User user) {
	return "Some interviewer(s) are removed by "+ user.getName() + "(" + user.getEmail() + ")";
    }

    public static String getDMRemovedMsg(User user) {
	return "Some decision maker(s) are removed by "+ user.getName() + "(" + user.getEmail() + ")";
    }
    
    public static String getHRAddedToPositionMsg(User user) {
	return " added by " + user.getName() + "(" + user.getEmail() + ")";
    }
    

}
