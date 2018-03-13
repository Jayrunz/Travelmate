package com.jayrun.beans;

import cn.bmob.v3.BmobObject;

public class Suggestion extends BmobObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8676926231876761274L;
	private String userId;
	private String userPhoneNumber;
	private String userEmail;
	private String suggestion;

	public Suggestion() {
		super();
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	public String getUserPhoneNumber() {
		return userPhoneNumber;
	}

	public void setUserPhoneNumber(String userPhoneNumber) {
		this.userPhoneNumber = userPhoneNumber;
	}

	public String getUserEmail() {
		return userEmail;
	}

	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}

	public String getSuggestion() {
		return suggestion;
	}

	public void setSuggestion(String suggestion) {
		this.suggestion = suggestion;
	}

}
