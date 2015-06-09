package com.vincent.massivelist;

public class PostModel {

	public String UID1;
	public String UID2;
	public String NAME1;
	public String NAME2;
	public String GENDER1;
	public String GENDER2;
	public String IMAGE_URL1;
	public String IMAGE_URL2;
	public String POST;
	
	public PostModel(String uid1, String uid2, String name1, String name2, 
			String gender1, String gender2, String imgUrl1, String imgUrl2, String post)
	{
		this.UID1 = uid1;
		this.UID2 = uid2;
		this.NAME1 = name1;
		this.NAME2 = name2;
		this.GENDER1 = gender1;
		this.GENDER2 = gender2;
		this.IMAGE_URL1 = imgUrl1;
		this.IMAGE_URL2 = imgUrl2;
		this.POST = post;
	}
}
