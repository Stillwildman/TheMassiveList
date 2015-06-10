package com.vincent.massivelist;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.Formatter;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

@SuppressLint("DefaultLocale")
public class MainListActivity extends Activity
{
	private ExpandableListView exList;
	private ExAdapter exAdapter;
	
	private EditText textInput;
	private static Spinner userSpinner;
	
	private EditText numberInput;
	
	private String defualtNum = "5";
	private String inputNum = "";
	private String input = "";
	private StringBuilder sb;
	
	ImageLoader imageLoader;
	
	private LinearLayout iconsLayout;
	private LinearLayout smileyIconLayout;
	private ImageButton showIconBtn;
	private boolean iconShown;
	private ProgressBar loading;
	private HashMap<String, Bitmap> setIconMap;
	private String user2Id = "";
	
	private Random ran;
	private TextView memoryText;
	public static EditText debugText;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_list_layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setTitle(getResources().getString(R.string.app_name) + "_v" + getResources().getString(R.string.Version));
		
		exList = (ExpandableListView) findViewById(R.id.sampleExList);
		memoryText = (TextView) findViewById(R.id.memoryText);
		
		new createAsyncList().execute(defualtNum);
		
		textInput = (EditText) findViewById(R.id.textInput);
		textInput.setFocusable(true);
		textInput.setFocusableInTouchMode(true);
		//textInput.setOnKeyListener(goKey);
		
		userSpinner = (Spinner) findViewById(R.id.userSpinner);
		
		numberInput = (EditText) findViewById(R.id.numberInput);
		numberInput.setFocusable(true);
		numberInput.setFocusableInTouchMode(true);
		
		numberInput.setOnEditorActionListener(new OnEditorActionListener() {	//監聽EditText，只有在按下Enter or 完成之類的，才會觸發事件
			@Override
			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				String number = numberInput.getText().toString();
				if (input.isEmpty())
				{
					if (numberInput.getText().toString().isEmpty())
						new createAsyncList().execute(defualtNum);
					else {
						inputNum = String.valueOf(number.toString());
						new createAsyncList().execute(inputNum);
					}
				} else
				{
					if (numberInput.getText().toString().isEmpty())
						new createAsyncList().execute(defualtNum);
					else {
						inputNum = String.valueOf(number.toString());
						new createAsyncList().execute(inputNum);
					}
				}
				return false;
			}
		});
		collapseOther();
		showMemory();
		
		imageLoader = new ImageLoader(getApplicationContext());
		
		iconsLayout = (LinearLayout) findViewById(R.id.iconsLayout);
		smileyIconLayout = (LinearLayout) findViewById(R.id.smileysIconLayout);
		showIconBtn = (ImageButton) findViewById(R.id.showIconBtn);
		
		loading = (ProgressBar) findViewById(R.id.loading);
		setIconMap = new HashMap<String, Bitmap>();
		
		debugText = (EditText) findViewById(R.id.debugText);
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	class createAsyncList extends AsyncTask<String, Integer, Void>
	{
		private int count;
		
		private Dialog dialog;
		private TextView loadingText;
		private String[] urlList;
		
		@SuppressLint("InflateParams")
		@Override
		protected void onPreExecute()
		{
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_loading_layout, null);
			dialog = new Dialog(MainListActivity.this);
			loadingText = (TextView) view.findViewById(R.id.loadingText);
			dialog.setContentView(view);
    		dialog.setTitle("Generating List...");
    		dialog.setCanceledOnTouchOutside(false);
    		
    		Window dialogWindow = dialog.getWindow();
    		WindowManager.LayoutParams windowParams = dialogWindow.getAttributes();
    		windowParams.alpha = 0.9f;
    		dialog.show();
    		
    		ran = new Random();
		}
		
		@Override
		protected Void doInBackground(String... params)
		{
			urlList = getResources().getStringArray(R.array.url_array);
			count = Integer.parseInt(params[0]);

			String uidFormated;
			String userName;
			String[] genderArr = {"Male", "Female"};
			String gender;
			String userImg;
			
			for (int i = 0; i < count; i++)
			{
				publishProgress(Integer.valueOf(i));
				uidFormated = String.format("%02d", i);
				userName = "User" + i;
				
				gender = genderArr[ran.nextInt(2)];
				userImg = urlList[ran.nextInt(urlList.length)];
				
				UsersData.addUserId(uidFormated);
				UsersData.addUserData(uidFormated, userName, String.valueOf(gender), userImg);
			}
			ThreadLogUtils.logThread();
			return null;
		}
		protected void onPostExecute(Void result)
		{
			exAdapter = new ExAdapter(MainListActivity.this);
			
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
				exList.setIndicatorBounds(exList.getRight()-40, exList.getWidth());
			else
				exList.setIndicatorBoundsRelative(exList.getRight()-40, exList.getWidth());
			
			exList.setAdapter(exAdapter);
			exList.setOnScrollListener(scrollState);
			dialog.dismiss();
			setUserSpinner();
		}
		protected void onProgressUpdate(Integer...status)
		{
			double percent = ((double)status[0] / (double)count) * 10000;
			percent = Math.floor(percent + 0.5) / 100;
			loadingText.setText(String.valueOf(status[0]) + "/" + String.valueOf(count) + "\n" + "\n" + percent + "%");
		}
	}
	
	public void showMemory()
	{
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		//int freeMemory = (int) (Runtime.getRuntime().freeMemory() / 1024);
		long availableMem = mi.availMem;
		//String freeMem = String.valueOf(freeMemory) + "MB"; 
		String avaMem = String.valueOf(Formatter.formatFileSize(this, availableMem));
		
		memoryText.setText("Mem:\n" + avaMem);
	}
	
	public void collapseOther()
	{
		exList.setOnGroupExpandListener(new OnGroupExpandListener() {
			@Override
			public void onGroupExpand(int groupPosition) {
				// TODO Auto-generated method stub
				int totalCount = exList.getExpandableListAdapter().getGroupCount();
				for (int i = 0; i < totalCount; i++)
				{
					if (i != groupPosition)
						exList.collapseGroup(i);		//除了展開的那個位置，其餘位置通通collapse!
				}
			}
		});
	}
	
	class AsyncList extends AsyncTask<String, Integer, Void>
	{
		private int count;
		
		private Dialog dialog;
		private TextView loadingText;
		
		private UserModel userModel1;
		private UserModel userModel2;
		
		int position;
		String user1Id;
		String userName1;
		String userGender1;
		String userImgUrl1;

		String userName2 = "";
		String userGender2 = "";
		String userImgUrl2 = "";
		
		@SuppressLint("InflateParams")
		@Override
		protected void onPreExecute()
		{
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_loading_layout, null);
			dialog = new Dialog(MainListActivity.this);
			loadingText = (TextView) view.findViewById(R.id.loadingText);
			dialog.setContentView(view);
    		dialog.setTitle("Generating List...");
    		dialog.setCanceledOnTouchOutside(false);
    		
    		Window dialogWindow = dialog.getWindow();
    		WindowManager.LayoutParams windowParams = dialogWindow.getAttributes();
    		windowParams.alpha = 0.9f;
    		dialog.show();
		}
		
		@Override
		protected Void doInBackground(String... params)
		{
			count = Integer.parseInt(params[0]);
			user1Id = params[1];

			for (int i = 0; i < count; i++)
			{
				publishProgress(Integer.valueOf(i));

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						position = UsersData.postDataMap.size();
						
						if (!UsersData.removedPos.isEmpty()) {
							position = UsersData.removedPos.get(0);
							UsersData.removedPos.remove(0);
						}
						userModel1 = UsersData.userDataMap.get(user1Id);
						userModel2 = UsersData.userDataMap.get(user2Id);
						
						userName1 = userModel1.NAME;
						userGender1 = userModel1.GENDER;
						userImgUrl1 = userModel1.IMAGE_URL;

						if (!user2Id.isEmpty())
						{
							userName2 = userModel2.NAME;
							userGender2 = userModel2.GENDER;
							userImgUrl2 = userModel2.IMAGE_URL;
						}
						UsersData.addPostData(position, user1Id, user2Id, userName1, userName2, 
								userGender1, userGender2, userImgUrl1, userImgUrl2, input);
						UsersData.addChildList("childSample", "www.google.com\t" + "\nbrack@gmail.com\t\n+903345678\t");
					}
				});
			}
			ThreadLogUtils.logThread();
			return null;
		}
		protected void onPostExecute(Void result)
		{
			debugText.setText("MapSize: " + UsersData.postDataMap.size() + "  AddedPos: " + position);
			exAdapter.notifyDataSetChanged();
			dialog.dismiss();
		}
		protected void onProgressUpdate(Integer...status)
		{
			double percent = ((double)status[0] / (double)count) * 10000;
			percent = Math.floor(percent + 0.5) / 100;
			loadingText.setText(String.valueOf(status[0]) + "/" + String.valueOf(count) + "\n" + "\n" + percent + "%");
		}
	}
	
	public void sendClick(View view)
	{
		//ran = new Random();
		//String ranIDcount = UsersData.getUID(ran.nextInt(UsersData.getCount()));
		final String user1Id = UsersData.getUID(userSpinner.getSelectedItemPosition());
		
		String toUserText = "";
		input = textInput.getText().toString();
		
		if (input.indexOf("@") == 0 && input.contains(" "))
		{
			toUserText = input.substring(1, input.indexOf(" "));
			if (UsersData.isUserExists(toUserText)) {
				input = input.substring(input.indexOf(" ")+1);
				textInput.setText("@" + toUserText + " ");
				textInput.setSelection(toUserText.length()+2);
			} else {
				user2Id = new String();
				textInput.setText("");
			}
		} else {
			user2Id = new String();
			textInput.setText("");
		}
		new AsyncList().execute("5000", user1Id);
		/*
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				int position = UsersData.postDataMap.size();

				if (!UsersData.removedPos.isEmpty()) {
					position = UsersData.removedPos.get(0);
					UsersData.removedPos.remove(0);
				}
				UserModel userModel1 = UsersData.userDataMap.get(user1Id);
				String userName1 = userModel1.NAME;
				String userGender1 = userModel1.GENDER;
				String userImgUrl1 = userModel1.IMAGE_URL;

				String userName2 = "";
				String userGender2 = "";
				String userImgUrl2 = "";
				if (!user2Id.isEmpty())
				{
					UserModel userModel2 = UsersData.userDataMap.get(user2Id);
					userName2 = userModel2.NAME;
					userGender2 = userModel2.GENDER;
					userImgUrl2 = userModel2.IMAGE_URL;
				}
				UsersData.addPostData(position, user1Id, user2Id, userName1, userName2, 
						userGender1, userGender2, userImgUrl1, userImgUrl2, input);
				UsersData.addChildList("childSample", "www.google.com\t" + "\nbrack@gmail.com\t\n+903345678\t");
				debugText.setText("MapSize: " + UsersData.postDataMap.size() + "  AddedPos: " + position);
				exAdapter.notifyDataSetChanged();
			}
		});
		*/
		exList.setSelectedGroup(exAdapter.getGroupCount()-1);
	}
	/*
	OnKeyListener goKey = new OnKeyListener() {					//監聽軟體鍵盤上的動作！
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {	//如果按下 Enter or 完成 之類的...
				
    			InputMethodManager input = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    			
    			if (input.isActive()) {						//會觸發 sendClick() 這個 Function
    				sendClick(v);
    				input.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    			}
    			return true;
    		}
			return false;
		}
    };
    */
    public static int getPixels(int dipValue)			//自行定義一個 Dip To Pixels 的功能！
    {
    	Resources res = Resources.getSystem();
    	int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, res.getDisplayMetrics());
    	//Log.i("Dip to Pixels~~", "" + dipValue + " to " + px);
    	return px;
    }
    
    @SuppressWarnings("deprecation")
	public void createImageBtn()			//從 image_cache 中，動態建立 ImageButton
    {
    	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);	//獲得 WindowManager 的服務
    	int screenWidth = wm.getDefaultDisplay().getWidth();		//取得目前螢幕的寬度(Pixels)
    	
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixels(40), getPixels(40), Gravity.CENTER);
    												//若直接輸入數字的話，單位會是Dip，因此要用 getPixels() 將單位轉換為Pixels，
    	iconsLayout.removeAllViews();				//在計算物件在螢幕中的空間關係，才會很準確阿~~
    	Bitmap imgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wait01);
    	
    	String SDPath = Environment.getExternalStorageDirectory().getPath();
    	String cacheDir = getResources().getString(R.string.cache_dirname);
    	StringBuilder imagePathSb = new StringBuilder();
    	String imagePath = imagePathSb.append(SDPath).append("/").append(cacheDir).append("/").toString();
    	
    	LinearLayout iconLayout = new LinearLayout(this);		//除了 new ImageButon 之外，也要 new LinearLayout 喔！
		iconLayout.setLayoutParams(new LinearLayout.LayoutParams
				(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    	
    	int btnWidthSum = 0;						// 先定義一個空的 int，待會要用來累加ImageButton的寬度
    	boolean isFirstCreate = true;
    	
    	for (String[] imgName: getImageName())		//根據 getImageName() 中獲得的size來run迴圈
    	{
    		try {
    			imgBitmap = getDecodedBitmap(imagePath + imgName[1], 80, 80);
    		}catch (Exception e) {
    			Log.e("CreateBtnFailed!!", e.getMessage().toString());
    			messageShort("Buttons Create Failed!");
    		}
    		ImageButton imgBtn = new ImageButton(this);
    		imgBtn.setImageBitmap(imgBitmap);
    		imgBtn.setScaleType(ScaleType.CENTER_CROP);
    		imgBtn.setLayoutParams(params);
    		imgBtn.setTag(imgName[0]);			// setTag() 根本只有好用阿!!!
    		
    		btnWidthSum += imgBtn.getLayoutParams().width;	//藉由 .getLayoutParams().width 獲得 imgBtn 的寬度，然後加到 btnWidthSum 中！
    		//Log.i("BtnWidth!", imgBtn.getLayoutParams().width + " of " + btnWidthSum);
    		
    		if (isFirstCreate)				//由於並不是每圈都要加入 new Layout，所以要有判斷式阿~
    		{
    			iconLayout.addView(imgBtn);
    			iconsLayout.addView(iconLayout);	//如果是第一圈 (isFirstCreate)，就先無條件的加入一次 Layout
    			isFirstCreate = false;
    		}
    		else if (btnWidthSum <= screenWidth)	//如果 imgBtn 所累加的寬度，還沒大於螢幕寬度的話，就繼續 add 在原本的Layout中
    		{
    			iconLayout.addView(imgBtn);
    		}
    		else if (btnWidthSum > screenWidth)		//反之如果 imgBtn 所累加的寬度已大於螢幕了，就再 new 一個Layout出來~
    		{
    			iconLayout = new LinearLayout(this);
    			iconLayout.setLayoutParams(new LinearLayout.LayoutParams
    					(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    			iconLayout.addView(imgBtn);
    			iconsLayout.addView(iconLayout);
    			btnWidthSum = 0;					//然後然後，要記得把 btnWidthSum 歸零以重新計算喔！
    			//shortMessage("OOPS~~~~~~~~");
    		}
    		imgBtn.setOnClickListener(btnClick);
    	}
    }
    OnClickListener btnClick = new OnClickListener()
    {
    	public void onClick(View v) {
    		setIconText(v.getTag().toString());
    	}
    };
	
	public void createIconsBtn()		//用Resources中的Drawable，來動態建立ImageButton
    {
    	smileyIconLayout.removeAllViews();
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixels(40), getPixels(40), Gravity.CENTER);
    										//與上面的 createImageBtn 都差不多阿~
    	int iconRes;
    	
    	for (String[] smileyName: getSmileyName())		//根據 getSmileyIcons() 的size來run~
    	{
    		iconRes = Integer.parseInt(smileyName[1]);
    		ImageButton iconBtn = new ImageButton(this);
    		iconBtn.setImageResource(iconRes);
    		iconBtn.setScaleType(ScaleType.CENTER_CROP);
    		iconBtn.setLayoutParams(params);
    		iconBtn.setTag(smileyName[0]);
    		iconBtn.setOnClickListener(btnClick);
    		smileyIconLayout.addView(iconBtn);
    	}
    }
	private String withSymbol(String text)		//一個將 String 的前後都加上指定符號的小功能~
	{
		sb = new StringBuilder();
		sb.append("#").append(text).append("#");
		return sb.toString();
	}
	
    private void setIconText(String iconText)
    {
    	SmileysParser.init(this);							//每次 setSmileyText 的時候，都讓 SmileysParser 重新 init 一次，
    	SmileysParser parser = SmileysParser.getInstance();	//以用來更新存放images檔名的 HashMap
    	
    	String oriText = textInput.getText().toString();
    	int index = Math.max(textInput.getSelectionStart(), 0);
    	Log.i("EditText Index", "" + index);
    	
    	sb = new StringBuilder(oriText);
    	sb.insert(index, " " + iconText + " ");
    	
    	if (iconText.contains("http://") || iconText.contains("https://"))
    	{
    		String imgPathName = getImagePathByName(iconText);
    		setIconMap.put(iconText, getDecodedBitmap(imgPathName, 60, 60));
    		textInput.setText(parser.addIconSpans(sb.toString(), setIconMap));
    	} else
    		textInput.setText(parser.addIconSpans(sb.toString(), setIconMap));
    	textInput.setSelection(index + iconText.length()+2);
    }
    
    public HashMap<String, Integer> getSmileyMap()		//要丟給 SmileysParser 吃，所以要產生 HashMap
    {
    	HashMap<String, Integer> iconNameItem = new HashMap<String, Integer>(getSmileyName().size());
    	
    	for (String[] icons: getSmileyName())
    	{
    		iconNameItem.put(icons[0], Integer.parseInt(icons[1]));
    	}
    	return iconNameItem;
    }
    
    public List<String[]> getSmileyName()		//將 Resources 中的 Drawable 撈出來，並建立在 List<String[]> 中
    {
    	List<String[]> smileyIconList = new ArrayList<String[]>();
    	String resStr;
    	
    	R.drawable drawable = new R.drawable();
    	Field[] drawRes = R.drawable.class.getFields();
    	
    	for (Field f: drawRes)
    	{
    		try
    		{
    			if (f.getName().contains("smiley"))		//藉由判斷名稱，來篩選出我們要的 Drawable
    			{
    				resStr = String.valueOf(f.getInt(drawable));
    				smileyIconList.add(createStringArr(withSymbol(f.getName()), resStr));
    			}						//將 Drawable 的資訊放到 smileyIconList 中，格式為：#(DrawableName)#[0]，(DrawableID)[1]
    		}
    		catch (IllegalArgumentException e) {
    			e.printStackTrace();
    			Log.e("getSmileyFailed!", e.getMessage().toString());
    		}
    		catch (IllegalAccessException e) {
    			e.printStackTrace();
    			Log.e("getSmileyFailed!", e.getMessage().toString());
    		}
    	}
    	return smileyIconList;
    }
    
    public HashMap<String, String> getImageMap()		//由於 SmileyParser 是吃 HashMap 來分析資料，所以這裡也把 imageNames 做成HashMap！
    {
    	HashMap<String, String> imgNameItem = new HashMap<String, String>(getImageName().size());
    	String imgName;
    	String imgFullName;
    	for (String[] img: getImageName())			//用 for each 呼叫 getImageName()，run完後就有一個HashMap啦~~
    	{
    		imgName = img[0];
    		imgFullName = img[1];
    		imgNameItem.put(imgName, imgFullName);
    	}
    	return imgNameItem;
    }
    
    public List<String[]> getImageName()			//將已儲存的Image檔案名稱放進一個 List<String[]> 裡
    {
    	File cacheDir;
    	try
    	{			/*---------使用 File 之前，都要先做以下的判斷阿!!! 若沒SD，就用內部的cache資料夾；--------*/
    		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
    			cacheDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.cache_dirname));
    		else
    			cacheDir = getCacheDir();
    		if (!cacheDir.exists())
    			cacheDir.mkdirs();
    				/*-------若自訂的 cache 資料夾不存在，就新增出來！----*/
    		
    		File[] imgCount = cacheDir.listFiles();		// List 出 cacheDir 裡的所有檔案

    		List<String[]> imageNameList = new ArrayList<String[]>();

    		String imgFullName;
    		//StringBuilder imgNameSb;
    		String imgName;

    		for (File img: imgCount)
    		{
    			imgFullName = img.getName();	//完整檔案名稱，包含副檔名
    			imgName = imgFullName.replace("%3A", ":").replace("%2F", "/").replace("%2B", "+");
    			
    			//Log.i("imgName", imgName);
    			//Log.i("imgFullName", imgFullName);

    			imageNameList.add(createStringArr(imgName, imgFullName)); //將 已修改過的檔名[0] & 完整檔名[1] add 進 imageNameList 中！
    		}
    		return imageNameList;
    	}
    	catch (Exception e) {
    			e.printStackTrace();
    			Log.e("FileNotFound!", e.toString());
    	}
    	return null;
    }
    private String[] createStringArr(String imgName, String imgFullName)		//產生 String[] 的一個小東東~
    {
    	String[] listName = {imgName, imgFullName};
    	return listName;
    }
    
    public void showIconClick(View view)
    {
    	if (!iconShown)
    		showIcons();
    	else
    		hideIcons();
    }
    
    private void showIcons()
    {
    	iconsLayout.setVisibility(View.VISIBLE);
    	smileyIconLayout.setVisibility(View.VISIBLE);
    	showIconBtn.setImageResource(android.R.drawable.ic_menu_more);
    	createImageBtn();
    	createIconsBtn();
    	iconShown = true;
    }
    
    private void hideIcons()
    {
    	iconsLayout.setVisibility(View.GONE);
    	smileyIconLayout.setVisibility(View.GONE);
    	showIconBtn.setImageResource(android.R.drawable.ic_menu_add);
    	iconShown = false;
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK || event.getAction() == KeyEvent.KEYCODE_BACK)
		{
			if (iconShown)
				hideIcons();
			else {
				android.os.Process.killProcess(android.os.Process.myPid());
			}
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    
    public void LoadingShow()
    {
    	loading.setVisibility(View.VISIBLE);
    }
    
    public void LoadingHide()
    {
    	loading.setVisibility(View.GONE);
    }
    
    public static Bitmap getDecodedBitmap(String imgPath, int reqWidth, int reqHeight)	//根據 Image 的大小來壓縮...
	{
    	int reqWidthPix = getPixels(reqWidth);		//把我們 Require 的值轉成 Pixels
		int reqHeightPix = getPixels(reqHeight);
    	
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;			// inJustDecodeBounds = true 時，就可以直接抓出圖片屬性，而不用整張都載入
		BitmapFactory.decodeFile(imgPath, options);	// decodeFile 是使用檔案的路徑喔~
		options.inSampleSize = getInSampleSize(options, reqWidthPix, reqHeightPix);	//藉由 getInSampleSize，指定 inSampleSize 的數值
		
		int imgWidth = options.outWidth;		//獲得來源 Image 的寬度 長度 & Type
		int imgHeight = options.outHeight;
		String imgType = options.outMimeType;	//這一段只是為了把資訊Log出來，其實可以不用寫~
		Log.i("ImageInfo~", imgType + " " + imgWidth + " x " + imgHeight);
		
		options.inJustDecodeBounds = false;	//屬性抓完了，就可以把 inJustDecodeBounds 給關掉了~
		Bitmap imageInSampleSize = BitmapFactory.decodeFile(imgPath, options);	//這時後 options 中的數值是已經被重新指定過了喔！
		return createScaleBitmap(imageInSampleSize, reqWidthPix, reqHeightPix, options.inSampleSize);
	}
	
	private static int getInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		final int width = options.outWidth;			//來源 Image 的寬度&高度~
		final int height = options.outHeight;
		int inSampleSize = 1;
		
		if (width > reqWidth || height > reqHeight)	//如果來源的長寬 大於 Require 的話...
		{
			final int halfWidth = width / 2;		//就除一半阿~
			final int halfHeight = height / 2;
			
			while ((halfWidth / inSampleSize) > reqWidth && (halfHeight / inSampleSize) > reqHeight)
			{
				inSampleSize *= 2;
			}
		}
		return inSampleSize;
	}
	
	private static Bitmap createScaleBitmap(Bitmap image, int dstWidth, int dstHeight, int inSampleSize)
	{
		Bitmap scaledImg = Bitmap.createScaledBitmap(image, dstWidth, dstHeight, false);
		if (image != scaledImg) {
			image.recycle();
			return scaledImg;
		}
		else {
			scaledImg.recycle();
			return image;
		}
	}
	
	public String getImagePathByName(String name)				//將一段正常的URL丟進來，以獲取該Image檔案的完整路徑
	{
		String SDPath = Environment.getExternalStorageDirectory().getPath();
		String cacheDir = getResources().getString(R.string.cache_dirname);
		String imgPathName;
		
		HashMap<String, String> imageMap = getImageMap();		//呼叫 getImageMap()
		
		if (imageMap.containsKey(name)) {						//如果 imageMap 裡有丟進來的那段URL的話..
			imgPathName = SDPath + "/" + cacheDir + "/" + imageMap.get(name);	// URL即為 imageMap 的 key，藉由URL獲得完整的檔名！
			return imgPathName;
		}
		else {
			messageShort("Can't Find Image Name in HashMap!");	//有時後會出現這個，表示下載還沒完成，HashMap的 key & value 還沒建立起來...
			return null;										//但 URL 就已經先丟過來了，所以當然找不到啦~~	
		}
	}
	
	@SuppressLint("InflateParams")
	@SuppressWarnings("deprecation")
	public void popImageWindow(String imgUrl)	//彈出顯示image用的視窗，雖然名子裡有pop，但其實是用 AlertDialog
	{
		WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
		int screenWidth = wm.getDefaultDisplay().getWidth();
		int screenHeight = wm.getDefaultDisplay().getHeight();
		
		int windowWidth = (int) (screenWidth / 1.2);
		int windowHeight = (int) (screenHeight / 2.2);
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MainListActivity.this);
		LayoutInflater inflater = getLayoutInflater();
		View view = inflater.inflate(R.layout.dialog_img_layout, null);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
				(windowWidth, windowHeight, Gravity.CENTER);
		view.setLayoutParams(params);
		dialogBuilder.setView(view);
		
		AlertDialog dialog = dialogBuilder.create();
		
		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams windowParams = dialogWindow.getAttributes();
		windowParams.alpha = 0.9f;
		windowParams.width = windowWidth;
		windowParams.height = windowHeight;
		
		dialog.setCanceledOnTouchOutside(true);
		dialog.show();
		
		ImageView iv = (ImageView) view.findViewById(R.id.imagePopLayout);
		iv.setLayoutParams(params);
		iv.setScaleType(ScaleType.CENTER_CROP);
		final String imgName = getImagePathByName(imgUrl);
		Bitmap imgBitmap = BitmapFactory.decodeFile(imgName);
		iv.setImageBitmap(imgBitmap);
		
		iv.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.setDataAndType(Uri.fromFile(new File(imgName)), "image/*");
				startActivity(intent);
			}
		});
	}
	
	public void toUser2 (String userID)		//接收從ExAdapter傳來的值，並顯示出User2
	{
		UserModel userModel = UsersData.userDataMap.get(userID);
		//String[] userData = UsersData.findUserDataById(userID);
		user2Id = userID;
		textInput.setText("@" + userModel.NAME + " ");
		textInput.setSelection(textInput.getText().length());
	}
	
	private void setUserSpinner()
	{
		String[] IdArr = UsersData.getUidArr();
		String[] userArr = UsersData.getUserNameArr();
		sb = new StringBuilder();
		for (int i = 0; i < IdArr.length; i++) {
			sb.append(IdArr[i]).append("-").append(userArr[i]).append(",");
		}
		String[] userDataArr = sb.toString().split(",");
		
		ArrayAdapter<String> arrAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, userDataArr);
		arrAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		userSpinner.setAdapter(arrAdapter);
		
		userSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				String userID = UsersData.getUID(position);
				exAdapter.setCurrentID(userID);
				textInput.setText("");
				Log.i("CurrentID", userID);
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) { }
		});
	}
	
	public void userAddClick(View view)
	{
		EditText addInput = (EditText) findViewById(R.id.userNameInput);
		String userName = addInput.getText().toString().replace(" ", "");
		if (!userName.isEmpty())
		{
			addInput.setText("");
			
			String uidFormated = String.format("%02d", UsersData.getCount());
			String[] genderArr = {"Male", "Female"};
			String gender = genderArr[ran.nextInt(2)];
			String[] urlList = getResources().getStringArray(R.array.url_array);
			String userImgUrl = urlList[ran.nextInt(urlList.length)];
			
			UsersData.addUserId(uidFormated);
			UsersData.addUserData(uidFormated, userName, String.valueOf(gender), userImgUrl);
			setUserSpinner();
			messageShort(userName + " Has Added! Your ID is " + uidFormated);
		} else
			messageShort("(ˊ_>ˋ)");
	}
	
	OnScrollListener scrollState = new OnScrollListener() {
		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			switch (scrollState)
			{
			case AbsListView.OnScrollListener.SCROLL_STATE_IDLE:
				exAdapter.isScrollFling = false;
				exAdapter.isScrollTouching = false;
				exAdapter.notifyDataSetChanged();
				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL:
				exAdapter.isScrollFling = false;
				exAdapter.isScrollTouching = true;
				break;
			case AbsListView.OnScrollListener.SCROLL_STATE_FLING:
				exAdapter.isScrollFling = true;
				break;
			}
		}
		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			//exList.setSelectedGroup(firstVisibleItem);
		}
	};
	
	public void messageShort(String msg)
	{
		Toast.makeText(MainListActivity.this, msg, Toast.LENGTH_SHORT).show();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		MenuInflater menuInflater = getMenuInflater();
		menuInflater.inflate(R.menu.menu_options, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
		case R.id.menu_clear:
			imageLoader.clearCache();
			SmileysParser.init(this);
			exList.invalidateViews();
			if (iconShown)
				hideIcons();
			
			messageShort("Image Cache Cleared");
			break;
		
		case R.id.menu_test:
			LinearLayout addUserLayout = (LinearLayout) findViewById(R.id.addUserLayout);
			if (!addUserLayout.isShown())
				addUserLayout.setVisibility(View.VISIBLE);
			else
				addUserLayout.setVisibility(View.GONE);
			break;
			
		}
		return true;
	}
}