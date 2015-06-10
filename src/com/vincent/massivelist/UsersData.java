package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;

@SuppressLint("UseSparseArrays")
public class UsersData {
	
	private static ArrayList<String> UID = new ArrayList<String>();
	
	public static List<List<Map<String, String>>> listChild = new ArrayList<List<Map<String, String>>>();
	
	public static HashMap<String, UserModel> userDataMap = new HashMap<String, UserModel>();
	public static HashMap<Integer, PostModel> postDataMap = new HashMap<Integer, PostModel>();
	
	public static ArrayList<Integer> mapPosList = new ArrayList<Integer>();
	public static ArrayList<Integer> removedPos = new ArrayList<Integer>();
	
	public UsersData ()
	{
		//UsersData.UID = UIDs;
		//UsersData.userData = userData;
	}
	
	public static void addUserId(String id)
	{
		UID.add(id);
	}
	
	public static int getCount()
	{
		return UID.size();
	}
	
	public static String getUID(int idCount)
	{
		return UID.get(idCount);
	}
	
	public static String[] getUidArr()
	{
		String[] uidTempArr = new String[UID.size()];
		String[] uidArr = UID.toArray(uidTempArr);
		return uidArr;
	}
	
	public static String[] getUserNameArr()
	{
		ArrayList<String> tempList = new ArrayList<String>();
		UserModel say;
		for (String s: UID)
		{
			say = userDataMap.get(s);
			tempList.add(say.NAME);
		}
		String[] userTempArr = new String[tempList.size()];
		String[] userArr = tempList.toArray(userTempArr);
		return userArr;
	}
	
	public static boolean isUserExists(String userName)
	{
		for (String name: getUserNameArr())
		{
			if (name.equals(userName))
				return true;
		}
		return false;
	}
	/*
	public static String[] findUserDataById(String userId)
	{
		for (int i = 0; i < UID.size(); i++)
		{
			if (UID.get(i).equals(userId))
				return userData.get(i);
		}
		return new String[] {"Unknow", "XX", ""};
	}
	*/
	public static void deleteOneLine(int position)
	{
		int pos = mapPosList.get(position);
		
		postDataMap.remove(pos);
		mapPosList.remove(position);
		
		removedPos.add(pos);
		
		MainListActivity.debugText.setText("MapSize: " + postDataMap.size() + "  RemovedPos: " + position);
	}
	
	public static void addChildList(String key, String content)
	{
		List<Map<String, String>> listChildItems = new ArrayList<Map<String, String>>();
		Map<String, String> listChildItem = new HashMap<String, String>();

		listChildItem.put(key, content);
		listChildItems.add(listChildItem);
		
		listChild.add(listChildItems);
	}
	
	public static void addUserData(String uid, String name, String gender, String imgUrl)
	{
		userDataMap.put(uid, new UserModel(name, gender, imgUrl));
	}
	
	public static void addPostData(int position, String uid1,String uid2, String name1, String name2, 
			String gender1, String gender2, String imgUrl1, String imgUrl2, String post)
	{
		postDataMap.put(position, new PostModel(uid1, uid2, name1, name2, gender1, gender2, imgUrl1, imgUrl2, post));
		mapPosList.add(position);
	}
}
