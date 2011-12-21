package com.teamagly.friendizer.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

/**
 * Represents an ordinary user of Friendizer application
 * @author Leon
 *
 */
@PersistenceCapable
public class User {
	
	@PrimaryKey
	private Long id;
	
	@Persistent
	private String username;
	
	@Persistent
	private Integer value;
	
	@Persistent
	private Integer money;
	
	@Persistent
	private Long coordinateX;
	
	@Persistent
	private Long coordinateY;
	
	@Persistent
	private Boolean online;
	
	
	public User() {
		value = 1000;
		money = 1000;
	}
	
	public void init() {
		value = 1000;
		money = 1000;
	}
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public Integer getValue() {
		return value;
	}
	public void setValue(Integer value) {
		this.value = value;
	}
	public Integer getMoney() {
		return money;
	}
	public void setMoney(Integer money) {
		this.money = money;
	}

	public Long getCoordinateX() {
		return coordinateX;
	}

	public void setCoordinateX(Long coordinateX) {
		this.coordinateX = coordinateX;
	}

	public Long getCoordinateY() {
		return coordinateY;
	}

	public void setCoordinateY(Long coordinateY) {
		this.coordinateY = coordinateY;
	}

	public Boolean getOnline() {
		return online;
	}

	public void setOnline(Boolean online) {
		this.online = online;
	}

}
