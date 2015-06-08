package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersData {
	
	private static ArrayList<String> UID = new ArrayList<String>();
	private static ArrayList<String[]> userData = new ArrayList<String[]>();
	
	public static List<Map<String, String[]>> userMapList = new ArrayList<Map<String, String[]>>();
	public static ArrayList<String> mainText = new ArrayList<String>();
	public static ArrayList<String[]> userIdStack = new ArrayList<String[]>();
	
	public UsersData ()
	{
		//UsersData.UID = UIDs;
		//UsersData.userData = userData;
	}
	
	public static void addUserIdAndData(String id, String name, int gender, String userImgUrl)
	{
		String sex = String.valueOf(gender);
		String[] dataArr = new String[] {name, sex, userImgUrl};
		
		UID.add(id);
		userData.add(dataArr);
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
		for (String[] s: userData)
		{
			tempList.add(s[0]);
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
	
	public static void addUserMap(String idCount)
	{
			HashMap<String, String[]> items = new HashMap<String, String[]>();
			items.put(UID.get(Integer.parseInt(idCount)), userData.get(Integer.parseInt(idCount)));
			userMapList.add(items);
	}
	
	public static void addMainText(String text)
	{
		mainText.add(text);
	}
	
	public static void addUserIdStack(String id1, String id2)
	{
		userIdStack.add(new String[] {id1, id2});
	}
	
	public static String[] findUserDataById(String userId)
	{
		for (int i = 0; i < UID.size(); i++)
		{
			if (UID.get(i).equals(userId))
				return userData.get(i);
		}
		return new String[]{"Unknow", "XX", ""};
	}
	
	public static void deleteOneLine(int position)
	{
		userMapList.remove(position);
		mainText.remove(position);
		userIdStack.remove(position);
	}
}
