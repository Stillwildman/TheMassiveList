package com.vincent.massivelist;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManager.MemoryInfo;
import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
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
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class MainListActivity extends Activity
{
	private ExpandableListView exList;
	private ExAdapter exAdapter;
	
	private EditText textInput;
	
	private EditText numberInput;
	
	private List<Map<String, String>> listGroup;
	private List<List<Map<String, String>>> listChild;
	
	private String defualtNum = "500";
	private String inputNum = "";
	private String input = "";
	private StringBuilder sb;
	
	ImageLoader imageLoader;
	
	private LinearLayout iconsLayout;
	private LinearLayout smileyIconLayout;
	private ImageButton showIconBtn;
	private boolean iconShown;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_list_layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setTitle(getResources().getString(R.string.app_name) + "_v" + getResources().getString(R.string.Version));
		
		exList = (ExpandableListView) findViewById(R.id.sampleExList);
		
		final String testingText = getResources().getString(R.string.TestingText);
		new createAsyncList().execute(defualtNum, testingText);
		
		textInput = (EditText) findViewById(R.id.textInput);
		textInput.setFocusable(true);
		textInput.setFocusableInTouchMode(true);
		textInput.setOnKeyListener(goKey);
		
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
						new createAsyncList().execute(defualtNum, testingText);
					else {
						inputNum = String.valueOf(number.toString());
						new createAsyncList().execute(inputNum, testingText);
					}
				} else
				{
					if (numberInput.getText().toString().isEmpty())
						new createAsyncList().execute(defualtNum, input);
					else {
						inputNum = String.valueOf(number.toString());
						new createAsyncList().execute(inputNum, input);
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
	}
	
	class createAsyncList extends AsyncTask<String, Integer, Void>
	{
		private int count;
		private String text;
		
		private Dialog dialog;
		private TextView loadingText;
		private String[] urlList;
    	//InputMethodManager input;
		
		@SuppressLint("InflateParams") @Override
		protected void onPreExecute()
		{
			//input = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			listGroup = new ArrayList<Map<String, String>>();
			listChild = new ArrayList<List<Map<String, String>>>();
			
			LayoutInflater inflater = getLayoutInflater();
			View view = inflater.inflate(R.layout.dialog_layout, null);
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
			GetStringByUrl urlString = new GetStringByUrl(getString(R.string.ImageUrl));	//從Testing server上取得Image URL的清單
			String[] urlTempString = urlString.getString().split(",");		//URL清單中的每個檔名，是以","做區隔，所以在這裡將他分割為Array
			StringBuilder urlSb;
			ArrayList<String> urlTempList = new ArrayList<String>();
			for (String s: urlTempString)
			{
				urlSb = new StringBuilder(s);
				urlSb.insert(0, "http://60.199.201.66/");		//上面獲得的檔名為 images/(fileName)，因此在前面再加上Server的路徑~
				urlTempList.add(urlSb.toString());
			}
			urlTempString = new String[urlTempList.size()];		//將 ArrayList 轉為 String[]
			urlList = urlTempList.toArray(urlTempString);
			
			count = Integer.parseInt(params[0]);
			text = params[1];

			/*--------------從這裡--------------------------一直到------------------------ */

			ArrayList<Integer> ranNumList = new ArrayList<Integer>();		//用來儲存 ranNum
			ArrayList<Integer> ranMultiList = new ArrayList<Integer>();		//用來儲存 ranMulti
			
			int ranCount = (int) (count * 0.1);								//新增一個int，值為總行數(count)的10分之1
			Random ran = new Random();
			StringBuilder sb;

			for (int i = 0; i < ranCount; i++)			//產生"ranCount"個的亂數
			{
				int ranNum = ran.nextInt(count)+1;			//亂數範圍為1~count
				int ranMulti = ran.nextInt(16)+5;			//產生5~20的亂數，用來決定sb要加幾倍
				Log.i("ranNumber",""+ranNum);
				Log.i("ranMultiple",""+ranMulti);

				ranNumList.add(ranNum);
				ranMultiList.add(ranMulti);
			}

			for (int i = 1; i <= count; i++)
			{
				publishProgress(Integer.valueOf(i));
				
				Map<String, String> listGroupItem = new HashMap<String, String>();

				listGroupItem.put("groupSample", text);		//正常put進固定的內容
				listGroupItem.put("groupNumber", " "+i);

				for (int j = 0; j < ranCount; j++)						//從這裡開始run "ranCount" 次的迴圈
				{
					if (i == ranNumList.get(j))							//如果該次的 i 等於ranNumList其中一個數字的話...
					{													//由於 i 是從 1 開始去run，所以一定是從ranNumList中最小的值開始抓到
						sb = new StringBuilder();			
						//Log.i("ranNumberList",""+ranNumList.get(j));	//把該次比對到的值Log出來，從最小到最大...
																		//所以在這裡也順便給 ranNumList 給做了排序...
																		//意外發現 Bubble Sort 之外的另一個排序法阿！
						for (int k = 0; k < ranMultiList.get(j); k++)
						{
							sb.append("This is the Chosen One! ");		//看該次的ranMultiList的值是多少，就run幾次
						}
						listGroupItem.put("groupSample", sb.toString());	//把該次的內容put進hashMap裡，覆蓋原本put的值
					}
				}
				listGroup.add(listGroupItem);

				/*----------到這裡，來給在既有的Group數量下，產生一定比例的亂數(挑出10分之1個)，
				 * 然後被選中的那幾個，再依ranMultiList中的值，給與指定倍數的成長！-----------I'm fucking Brilliant!-----*/

				List<Map<String, String>> listChildItems = new ArrayList<Map<String, String>>();
				Map<String, String> listChildItem = new HashMap<String, String>();

				listChildItem.put("childSample", ""+i);
				listChildItems.add(listChildItem);
				listChild.add(listChildItems);
			}
			ThreadLogUtils.logThread();
			return null;
		}
		protected void onPostExecute(Void result)
		{
			exAdapter = new ExAdapter(MainListActivity.this, listGroup, listChild, urlList);
			//exList.setIndicatorBounds(0,100);
			exList.setAdapter(exAdapter);
			dialog.dismiss();
	    	//input.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
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
		TextView memTip = (TextView) findViewById(R.id.memTip);
		
		ActivityManager am = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		MemoryInfo mi = new MemoryInfo();
		am.getMemoryInfo(mi);
		//int freeMemory = (int) (Runtime.getRuntime().freeMemory() / 1024);
		long availableMem = mi.availMem;
		//String freeMem = String.valueOf(freeMemory) + "MB"; 
		String avaMem = String.valueOf(Formatter.formatFileSize(this, availableMem));
		
		memTip.setText("Memory: " + avaMem);
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
	
	public void sendClick(View view)
	{
		input = textInput.getText().toString();
		
		if (!input.isEmpty())										//判斷 textInput 不是空的話，就顯示來自使用者的input
		{
			if (inputNum.isEmpty())
				new createAsyncList().execute(defualtNum, input);
			else
				new createAsyncList().execute(inputNum, input);
		} else
		{
			if (inputNum.isEmpty())									//否則就顯示預設的 TestingText
				new createAsyncList().execute(defualtNum, getResources().getString(R.string.TestingText));
			else
				new createAsyncList().execute(inputNum, getResources().getString(R.string.TestingText));
		}
		textInput.setText("");			
	}
	
	OnKeyListener goKey = new OnKeyListener() {						//監聽軟體鍵盤上的動作！
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {	//如果按下 Enter or 完成 之類的...
				
    			InputMethodManager input = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    			
    			if (input.isActive()) {							//會觸發 sendClick() 這個 Function
    				sendClick(v);
    				//input.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    			}
    			return true;
    		}
			return false;
		}
    };
    
    private int getPixels(int dipValue)			//自行定義一個 Dip To Pixels 的功能！
    {
    	Resources res = getResources();
    	int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, res.getDisplayMetrics());
    	Log.i("Dip to Pixels~~", "" + dipValue + " to " + px);
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
    	Drawable iconDraw;
    	
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
    		iconDraw = Drawable.createFromPath(imagePath + imgName[1]);
    		
    		ImageButton imgBtn = new ImageButton(this);
    		imgBtn.setImageDrawable(iconDraw);
    		imgBtn.setScaleType(ScaleType.CENTER_CROP);
    		imgBtn.setLayoutParams(params);
    		imgBtn.setTag(imgName[0]);			// setTag() 根本只有好用阿!!!
    		
    		btnWidthSum += imgBtn.getLayoutParams().width;	//藉由 .getLayoutParams().width 獲得 imgBtn 的寬度，然後加到 btnWidthSum 中！
    		Log.i("BtnWidth!", imgBtn.getLayoutParams().width + " of " + btnWidthSum);
    		
    		if (isFirstCreate)				//由於並不是每圈都要加入 new Layout，所以要有判斷式阿~
    		{
    			iconLayout.addView(imgBtn);
    			iconsLayout.addView(iconLayout);	//如果是第一圈 (isFirstCreate)，就先無條件的加入一次 Layout
    			isFirstCreate = false;
    		}
    		else if (btnWidthSum <= screenWidth)	//如果 imgBtn 所累加的寬度，還沒大於螢幕寬度的話，就繼續在 add 在原本的Layout中
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
    		setSmileyText(v.getTag().toString());
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
	
    public void setSmileyText(String smileyText)
    {
    	SmileysParser.init(this);							//每次 setSmileyText 的時候，都讓 SmileysParser 重新 init 一次，
    	SmileysParser parser = SmileysParser.getInstance();	//以用來更新存放images檔名的 HashMap
    	
    	String oriText = textInput.getText().toString();
    	int index = Math.max(textInput.getSelectionStart(), 0);
    	Log.i("EditText Index", "" + index);
    	
    	sb = new StringBuilder(oriText);
    	sb.insert(index, smileyText);
    	
    	textInput.setText(parser.addIconSpans(sb.toString()));
    	textInput.setSelection(index + smileyText.length());
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
    		StringBuilder imgNameSb;
    		String imgName;

    		for (File img: imgCount)
    		{
    			imgFullName = img.getName();	//完整檔案名稱，包含副檔名
    			imgNameSb = new StringBuilder(imgFullName).insert(imgFullName.lastIndexOf("%2F")+3, "/")
    					.insert(imgFullName.lastIndexOf(".")+1, "/");	//修改Image的檔名~
    			imgName = imgNameSb.substring(imgNameSb.lastIndexOf("%2F")+3, imgNameSb.lastIndexOf("."))
    					.replace("%2B", "+");	//新的 image name，含 /../ 不含副檔名

    			Log.i("imgName", imgName);
    			Log.i("imgFullName", imgFullName);

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
    
    public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK || event.getAction() == KeyEvent.KEYCODE_BACK)
		{
			if (iconShown)
				hideIcons();
			else
				android.os.Process.killProcess(android.os.Process.myPid());
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
    
	public void shortMessage(String msg)
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
			
			if (iconShown)
				hideIcons();
			
			shortMessage("Image Cache Cleared");
			break;
		
		case R.id.menu_test:
			//createIconsBtn();
			break;
		}
		return true;
	}
}