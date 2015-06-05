package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;

public class UsersData {
	
	private static ArrayList<String> UID = new ArrayList<String>();
	private static ArrayList<String[]> userData = new ArrayList<String[]>();
	
	public static int Male = 0;
	public static int Female = 1;
	
	public UsersData (ArrayList<String> UIDs, ArrayList<String[]> userData)
	{
		UsersData.UID = UIDs;
		UsersData.userData = userData;
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
	
	@SuppressLint("UseSparseArrays")
	public static Map<String, String[]> getUserMap(String idCount)
	{
		int count = UID.size();
		List<Map<String, String[]>> itemList = new ArrayList<Map<String, String[]>>();
		
		for (int i = 0; i < count; i++)
		{
			HashMap<String, String[]> items = new HashMap<String, String[]>();
			items.put(UID.get(i), userData.get(i));
			itemList.add(items);
		}
		return itemList.get(Integer.parseInt(idCount));
	}
}
