package com.jayrun.beans;

import java.util.List;
import cn.bmob.v3.BmobObject;
import cn.bmob.v3.datatype.BmobFile;

public class ScenicInfo extends BmobObject {
	/**
	 * 
	 */
	private static final long serialVersionUID = -6449992735982481841L;
	private String name;
	private String describtion;
	private Integer readedCount;
	private BmobFile pic;
	private String location;
	private String level;
	private String label;
	private String province;
	private String city;
	private List<String> urls;
	private Integer urlCursor;

	public List<String> getUrls() {
		return urls;
	}

	public void setUrls(List<String> urls) {
		this.urls = urls;
	}

	public Integer getUrlCursor() {
		return urlCursor;
	}

	public void setUrlCursor(Integer urlCursor) {
		this.urlCursor = urlCursor;
	}

	public ScenicInfo() {

	}

	public String getProvince() {
		return province;
	}

	public void setProvince(String province) {
		this.province = province;
	}

	public String getCity() {
		return city;
	}

	public void setCity(String city) {
		this.city = city;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescribtion() {
		return describtion;
	}

	public void setDescribtion(String describtion) {
		this.describtion = describtion;
	}

	public BmobFile getPic() {
		return pic;
	}

	public void setPic(BmobFile pic) {
		this.pic = pic;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public Integer getReadedCount() {
		return readedCount;
	}

	public void setReadedCount(Integer readedCount) {
		this.readedCount = readedCount;
	}

	public String getLevel() {
		return level;
	}

	public void setLevel(String level) {
		this.level = level;
	}

}
