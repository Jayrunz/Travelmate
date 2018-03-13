package com.jayrun.beans;

import java.util.ArrayList;
import java.util.List;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;
import cn.bmob.v3.datatype.BmobGeoPoint;
import cn.bmob.v3.datatype.BmobRelation;

public class Words extends BmobObject {

	/**
	 * 
	 */
	private static final long serialVersionUID = -293348055297991992L;
	private int bubbleColor;
	private String content;
	private BmobRelation likes;
	private List<String> likeUsersId = new ArrayList<String>();
	private User user;
	private ScenicInfo scenic;
	private boolean isText;
	private BmobFile graffiti;
	private String locationInfo;
	private BmobGeoPoint location;

	public Words() {
	}

	public String getLocationInfo() {
		return locationInfo;
	}

	public void setLocationInfo(String locationInfo) {
		this.locationInfo = locationInfo;
	}

	public BmobGeoPoint getLocation() {
		return location;
	}

	public void setLocation(BmobGeoPoint location) {
		this.location = location;
	}

	public BmobFile getGraffiti() {
		return graffiti;
	}

	public void setGraffiti(BmobFile graffiti) {
		this.graffiti = graffiti;
	}

	public boolean getIsText() {
		return isText;
	}

	public void setIsText(boolean isText) {
		this.isText = isText;
	}

	public int getBubbleColor() {
		return bubbleColor;
	}

	public void setBubbleColor(int bubbleColor) {
		this.bubbleColor = bubbleColor;
	}

	public List<String> getLikeUsersId() {
		return likeUsersId;
	}

	public void setLikeUsersId(List<String> likeUsersId) {
		this.likeUsersId = likeUsersId;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public BmobRelation getLikes() {
		return likes;
	}

	public void setLikes(BmobRelation likes) {
		this.likes = likes;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public ScenicInfo getScenic() {
		return scenic;
	}

	public void setScenic(ScenicInfo scenic) {
		this.scenic = scenic;
	}

}
