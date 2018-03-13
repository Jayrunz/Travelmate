package com.jayrun.beans;

import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class CommentOnStrategy extends BmobObject {

	private static final long serialVersionUID = 2079553463527581196L;
	private BmobFile userHead;
	private String userName;
	private int userId;
	private int strategyId;
	private int likeCount;
	private int commentId;
	private String comment;

	public CommentOnStrategy() {
	}

	public BmobFile getUserHead() {
		return userHead;
	}

	public void setUserHead(BmobFile userHead) {
		this.userHead = userHead;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public int getStrategyId() {
		return strategyId;
	}

	public void setStrategyId(int strategyId) {
		this.strategyId = strategyId;
	}

	public int getLikeCount() {
		return likeCount;
	}

	public void setLikeCount(int likeCount) {
		this.likeCount = likeCount;
	}

	public int getCommentId() {
		return commentId;
	}

	public void setCommentId(int commentId) {
		this.commentId = commentId;
	}

	public String getComment() {
		return comment;
	}

	public void setComment(String comment) {
		this.comment = comment;
	}

}
