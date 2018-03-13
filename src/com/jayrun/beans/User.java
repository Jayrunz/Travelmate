package com.jayrun.beans;

import cn.bmob.v3.BmobUser;
import cn.bmob.v3.datatype.BmobFile;

public class User extends BmobUser {

	/**
	 * 用户信息
	 */
	private static final long serialVersionUID = 6440455116664757150L;
	private BmobFile userHead;
	private String nickName;
	private String signature;
	private String sex;
	private int age;
	private String work;
	private String interest;
	private String school;
	private String address;

	public User() {
		super();
	}

	public String getSex() {
		return sex;
	}

	public void setSex(String sex) {
		this.sex = sex;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getWork() {
		return work;
	}

	public void setWork(String work) {
		this.work = work;
	}

	public String getInterest() {
		return interest;
	}

	public void setInterest(String interest) {
		this.interest = interest;
	}

	public String getSchool() {
		return school;
	}

	public void setSchool(String school) {
		this.school = school;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getSignature() {
		return signature;
	}

	public void setSignature(String signature) {
		this.signature = signature;
	}

	public BmobFile getUserHead() {
		return userHead;
	}

	public void setUserHead(BmobFile userHead) {
		this.userHead = userHead;
	}

	public String getNickName() {
		return nickName;
	}

	public void setNickName(String nickName) {
		this.nickName = nickName;
	}

}
