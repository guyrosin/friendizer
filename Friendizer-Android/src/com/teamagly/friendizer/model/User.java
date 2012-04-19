package com.teamagly.friendizer.model;

import java.io.Serializable;
import java.util.Date;

public class User implements Serializable {

    FacebookUser fb; // Data from Facebook
    FriendizerUser fz; // Data from our servers

    public enum FBQueryType {
	GRAPH, FQL
    }

    private static final long serialVersionUID = 8643574985757595599L;

    public User(FacebookUser fbUser) {
	fb = new FacebookUser(fbUser);
	fz = null;
    }

    public User(FriendizerUser fzUser) {
	fz = new FriendizerUser(fzUser);
	fb = null;
    }

    public User() {
	fb = new FacebookUser();
	fz = new FriendizerUser();
    }

    /**
     * @param friendizerUser
     * @param facebookUser
     */
    public User(FriendizerUser fzUser, FacebookUser fbUser) {
	fb = new FacebookUser(fbUser);
	fz = new FriendizerUser(fzUser);
    }

    // Updates the Facebook data from the given FacebookUser object
    public void updateFacebookData(FacebookUser fbUser) {
	fb = new FacebookUser(fbUser);
    }

    // Updates the Friendizer data from the given FriendizerUser object
    public void updateFriendizerData(FriendizerUser fzUser) {
	fz = new FriendizerUser(fzUser);
    }

    // Getters and setters

    /**
     * @return the firstName
     */
    public String getFirstName() {
	return fb.firstName;
    }

    /**
     * @param firstName
     *            the firstName to set
     */
    public void setFirstName(String firstName) {
	this.fb.firstName = firstName;
    }

    /**
     * @return the name
     */
    public String getName() {
	return fb.name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
	this.fb.name = name;
    }

    /**
     * @return the gender
     */
    public String getGender() {
	return fb.gender;
    }

    /**
     * @param gender
     *            the gender to set
     */
    public void setGender(String gender) {
	this.fb.gender = gender;
    }

    /**
     * @return the age
     */
    public String getAge() {
	return fb.age;
    }

    /**
     * @param age
     *            the age to set
     */
    public void setAge(String age) {
	this.fb.age = age;
    }

    /**
     * @return the picURL
     */
    public String getPicURL() {
	return fb.picURL;
    }

    /**
     * @param picURL
     *            the picURL to set
     */
    public void setPicURL(String picURL) {
	this.fb.picURL = picURL;
    }

    /**
     * @return the id
     */
    public long getId() {
	if (fz != null)
	    return fz.id;
	else
	    return fb.id;
    }

    /**
     * @param id
     *            the id to set
     */
    public void setId(long id) {
	this.fz.id = id;
    }

    /**
     * @return the value
     */
    public long getValue() {
	return fz.value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(long value) {
	this.fz.value = value;
    }

    /**
     * @return the money
     */
    public long getMoney() {
	return fz.money;
    }

    /**
     * @param money
     *            the money to set
     */
    public void setMoney(long money) {
	this.fz.money = money;
    }

    /**
     * @return the ownerID
     */
    public long getOwnerID() {
	return fz.ownerID;
    }

    /**
     * @param ownerID
     *            the ownerID to set
     */
    public void setOwnerID(long ownerID) {
	this.fz.ownerID = ownerID;
    }

    /**
     * @return the ownsList
     */
    public FriendizerUser[] getOwnsList() {
	return fz.ownsList;
    }

    /**
     * @param ownsList
     *            the ownsList to set
     */
    public void setOwnsList(FriendizerUser[] ownsList) {
	this.fz.ownsList = ownsList;
    }

    /**
     * @return the latitude
     */
    public double getLatitude() {
	return fz.latitude;
    }

    /**
     * @param latitude
     *            the latitude to set
     */
    public void setLatitude(double latitude) {
	this.fz.latitude = latitude;
    }

    /**
     * @return the longitude
     */
    public double getLongitude() {
	return fz.longitude;
    }

    /**
     * @param longitude
     *            the longitude to set
     */
    public void setLongitude(double longitude) {
	this.fz.longitude = longitude;
    }

    /**
     * @return the since
     */
    public Date getSince() {
	return fz.since;
    }

    /**
     * @param since
     *            the since to set
     */
    public void setSince(Date since) {
	this.fz.since = since;
    }

    /**
     * @return the distance
     */
    public long getDistance() {
	return fz.distance;
    }

    /**
     * @param distance
     *            the distance to set
     */
    public void setDistance(long distance) {
	this.fz.distance = distance;
    }

    /**
     * @return the matching
     */
    public int getMatching() {
	return fz.matching;
    }

    /**
     * @param matching
     *            the matching to set
     */
    public void setMatching(int matching) {
	this.fz.matching = matching;
    }
}
