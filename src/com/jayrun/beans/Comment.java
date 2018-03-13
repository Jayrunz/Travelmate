package com.jayrun.beans;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class Comment extends BmobObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = 8621972278867663L;
	// private BmobFile userHead;
	// private String userName;
	// private String userNameTo;
	// private String strategyId;
	// private String userId;
	// private String userIdTo;
	// private int commentId;
	private String comment;
	private User userFro;
	private Strategy strategy;
	private User userTo;

	public Comment() {
		super();
	}

	public User getUserFro() {
		return userFro;
	}

	public void setUserFro(User userFro) {
		this.userFro = userFro;
	}

	public Strategy getStrategy() {
		return strategy;
	}

	public void setStrategy(Strategy strategy) {
		this.strategy = strategy;
	}

	public User getUserTo() {
		return userTo;
	}

	public void setUserTo(User userTo) {
		this.userTo = userTo;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
