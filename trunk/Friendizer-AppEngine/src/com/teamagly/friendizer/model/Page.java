package com.teamagly.friendizer.model;

import com.restfb.Facebook;

public class Page {
	@Facebook("page_id")
	String id;

	@Facebook("pic_square")
	String picURL;

	@Facebook
	String name;

	@Facebook
	String type;
}
