package com.inspur.emmcloud.bean;

import java.io.Serializable;

import org.json.JSONObject;

import android.R.integer;

/**
 * 
 {"creationDate":1465957172920,"lastUpdate":null,"state":"ACTIVED",
 "id":"CAL:a00591fe581940e49cc9cf7a487142e9",
 "name":"生活日历","color":"ORANGE","owner":66666}
 *
 */
public class MyCalendar implements Serializable{
	private String id;
	private String name;
	private String color;
	private String owner;
	private String state;
	private boolean community;
	
	public MyCalendar(){
		
	}
	public MyCalendar(JSONObject obj){
		try {
			if (obj.has("id")) {
				id = obj.getString("id");
			}
			if (obj.has("name")) {
				name = obj.getString("name");
			}
			if (obj.has("color")) {
				color = obj.getString("color");
			}
			if (obj.has("owner")) {
				owner = obj.getString("owner");
			}
			if (obj.has("state")) {
				state = obj.getString("state");
			}
			if (obj.has("community")) {
				community = obj.getBoolean("community");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public MyCalendar(String response){
		try {
			JSONObject obj = new JSONObject(response);
			if (obj.has("id")) {
				id = obj.getString("id");
			}
			if (obj.has("name")) {
				name = obj.getString("name");
			}
			if (obj.has("color")) {
				color = obj.getString("color");
			}
			if (obj.has("owner")) {
				owner = obj.getString("owner");
			}
			if (obj.has("state")) {
				state = obj.getString("state");
			}
			if (obj.has("community")) {
				community = obj.getBoolean("community");
			}
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
	}
	
	public String getId(){
		return id;
	}
	 
	public void setId(String id){
		this.id = id;
	}
	
	public String getName(){
		return name;
	}
	
	public String getColor(){
		return color;
	}
	
	public String getOwner(){
		return owner;
	}
	
	public void setOwner(String owner){
		this.owner = owner;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public void setColor(String color){
		this.color = color;
	}
	
	public String getState(){
		return state;
	}
	
	public void setState(String state){
		this.state = state;
	}
	
	public boolean getCommunity(){
		return community;
	}
}
