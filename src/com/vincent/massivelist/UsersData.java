package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.annotation.SuppressLint;

public class UsersData {
	
	private static ArrayList<Integer> UID = new ArrayList<Integer>();
	private static ArrayList<String[]> userData = new ArrayList<String[]>();
	
	public static int Male = 0;
	public static int Female = 1;
	
	public UsersData (ArrayList<Integer> UIDs, ArrayList<String[]> userData)
	{
		UsersData.UID = UIDs;
		UsersData.userData = userData;
	}
	
	public static void addUserIdAndData(int id, String name, int gender, String userImgUrl)
	{
		String sex;
		if (gender == 0)
			sex = "Boy";
		else if (gender == 1)
			sex = "Girl";
		else
			sex = "Unknow";
		
		String[] dataArr = new String[] {name, sex, userImgUrl};
		
		UID.add(id);
		userData.add(dataArr);
	}
	
	public static int getCount()
	{
		return UID.size();
	}
	
	public static int getUID(int idCount)
	{
		return UID.get(idCount);
	}
	
	public static Integer[] getUidArr()
	{
		Integer[] uidTempArr = new Integer[UID.size()];
		Integer[] uidArr = UID.toArray(uidTempArr);
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
	
	@SuppressLint("UseSparseArrays")
	public static Map<Integer, String[]> getUserMap(int idCount)
	{
		int count = UID.size();
		List<Map<Integer, String[]>> itemList = new ArrayList<Map<Integer, String[]>>();
		
		for (int i = 0; i < count; i++)
		{
			HashMap<Integer, String[]> items = new HashMap<Integer, String[]>();
			items.put(UID.get(i), userData.get(i));
			itemList.add(items);
		}
		return itemList.get(idCount);
	}
}
