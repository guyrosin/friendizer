package com.teamagly.friendizer.model;

import org.json.JSONException;
import org.json.JSONObject;

import com.restfb.Facebook;

/**
 * 
 * Describe a Like object: id - Facebook ID of the page
 * 						   name - name of the page
 * 						   category - category of the page
 * 						   created_time - ISO-8601 datetime representing when the User was connected to the Page
 *
 */
public class Like {

	@Facebook
	String id;
	
	@Facebook
	String name;
	
	@Facebook
	String category;
	
	@Facebook
	String created_time;
	
	/**
	 * 
	 * The constructor creates a Like object
	 * 
	 * @param id
	 * @param name
	 * @param category
	 * @param created_time
	 */
	public Like(String id, String name, String category, String created_time) 
	{
		this.id = id;
		this.name = name;
		this.category = category;
		this.created_time = created_time;
	}
	
	/**
	 * The default constructor creates a Like object 
	 */
	public Like()
	{
		this.id = "";
		this.name = "";
		this.category = "";
		this.created_time = "";
	}
	
	/**
	 * 
	 * The constructor creates a Like object from a given JSON object
	 * 
	 * @param obj - the JSON object
	 * @throws JSONException
	 */
    public Like(JSONObject obj) throws JSONException 
    {
		id = obj.getString("id");
		name = obj.getString("name");
		category = obj.getString("category");
		created_time = obj.getString("created_time");
    }
    
    /**
     * 
     * The function creates a JSON object representing the Like object
     * 
     * @return JSONObject
     */
    public JSONObject toJSONObject() 
    {
    	JSONObject obj = new JSONObject();
    	
    	try 
    	{
    	    obj.put("id", id);
    	    obj.put("name", name);
    	    obj.put("category", category);
    	    obj.put("created_time", created_time);
    	} 
    	catch (JSONException e) {}
    	
    	return obj;
    }
    
    public String getId() {
		return id;
	}
    
    public void setId(String id) {
		this.id = id;
	}
    
    public String getName() {
		return name;
	}
    
    public void setName(String name) {
		this.name = name;
	}
    
    public String getCategory() {
		return category;
	}
    
    public void setCategory(String category) {
		this.category = category;
	}
    
    public String getCreated_time() {
		return created_time;
	}
    
    public void setCreated_time(String created_time) {
		this.created_time = created_time;
	}
    
    @Override
    public String toString() 
    {
    	return toJSONObject().toString();
    }
}
