package com.teamagly.friendizer.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;


@PersistenceCapable
public class OnlineUser {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private Long id;
	
	@Persistent(defaultFetchGroup = "true")
	private User user;
	
	@Persistent
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
