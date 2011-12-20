package com.teamagly.friendizer.server;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


@PersistenceCapable
public class OnlineUser {
	
	@PrimaryKey
	private Long id;
	
	@Persistent
	private User user;
	
	
	private Long coordinateX;
	
	@Persistent
	private Long coordinateY;

	public OnlineUser() {
				
	}
	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

}
