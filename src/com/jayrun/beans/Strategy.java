package com.jayrun.beans;

import java.util.ArrayList;
import java.util.List;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobRelation;

public class Strategy extends BmobObject {

	private static final long serialVersionUID = 6429526726978810835L;
	// private String scenicId;
	// private String scenic;
	// private BmobFile userHead;
	// private String userName;
	// private String userId;
	// private int strategyId;
	private String strategy;
	// private int likeCount;
	// private int commentCount;
	private BmobFile img1;
	private BmobFile img2;
	private BmobFile img3;
	private List<Comment> commentList = new ArrayList<Comment>();
	private List<String> likeUserIds = new ArrayList<String>();
	private User user;
	private ScenicInfo scenic;
	private BmobRelation likes;
	private BmobRelation comments;

	public Strategy() {
	}

	public List<Comment> getCommentList() {
		return commentList;
	}

	public void setCommentList(List<Comment> commentList) {
		this.commentList = commentList;
	}

	public List<String> getLikeUserIds() {
		return likeUserIds;
	}

	public void setLikeUserIds(List<String> likeUserIds) {
		this.likeUserIds = likeUserIds;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public ScenicInfo getScenic() {
		return scenic;
	}

	public void setScenic(ScenicInfo scenic) {
		this.scenic = scenic;
	}

	public BmobRelation getLikes() {
		return likes;
	}

	public void setLikes(BmobRelation likes) {
		this.likes = likes;
	}

	public BmobRelation getComments() {
		return comments;
	}

	public void setComments(BmobRelation comments) {
		this.comments = comments;
	}

	public String getStrategy() {
		return strategy;
	}

	public void setStrategy(String strategy) {
		this.strategy = strategy;
	}

	public BmobFile getImg1() {
		return img1;
	}

	public void setImg1(BmobFile img1) {
		this.img1 = img1;
	}

	public BmobFile getImg2() {
		return img2;
	}

	public void setImg2(BmobFile img2) {
		this.img2 = img2;
	}

	public BmobFile getImg3() {
		return img3;
	}

	public void setImg3(BmobFile img3) {
		this.img3 = img3;
	}

}
