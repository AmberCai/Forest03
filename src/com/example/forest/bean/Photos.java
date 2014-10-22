package com.example.forest.bean;

import java.sql.Time;

public class Photos {
	
	int id;
	String path;
	Time time;
	boolean sendsuc_flag = false;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Time getTime() {
		return time;
	}
	public void setTime(Time time) {
		this.time = time;
	}
	public boolean isSendsuc_flag() {
		return sendsuc_flag;
	}
	public void setSendsuc_flag(boolean sendsuc_flag) {
		this.sendsuc_flag = sendsuc_flag;
	}
	
}
