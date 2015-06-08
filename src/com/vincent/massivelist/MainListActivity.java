package com.vincent.massivelist;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_list_layout);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		this.setTitle(getResources().getString(R.string.app_name) + "_v" + getResources().getString(R.string.Version));
		
		exList = (ExpandableListView) findViewById(R.id.sampleExList);
		
		new createAsyncList().execute(defualtNum);
		
		textInput = (EditText) findViewById(R.id.textInput);
		textInput.setFocusable(true);
		textInput.setFocusableInTouchMode(true);
		//textInput.setOnKeyListener(goKey);
		
		userSpinner = (Spinner) findViewById(R.id.userSpinner);
		
		numberInput = (EditText) findViewById(R.id.numberInput);
		numberInput.setFocusable(true);
		numberInput.setFocusableInTouchMode(true);
		
		numberInput.setOnEditorActionListener(new OnEditorActionListener() {	//��ťEditText�A�u���b���UEnter or �����������A�~�|Ĳ�o�ƥ�
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
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
	class createAsyncList extends AsyncTask<String, Integer, Void>
	{
		private List<List<Map<String, String>>> listChild;
		private int count;
		
		private Dialog dialog;
		private TextView loadingText;
		private String[] urlList;
		
		@SuppressLint("InflateParams")
		@Override
		protected void onPreExecute()
		{
			listChild = new ArrayList<List<Map<String, String>>>();
			
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

			String uid;
			String userName;
			int gender;
			String userImg;
			
			for (int i = 0; i < count; i++)
			{
				publishProgress(Integer.valueOf(i));
				uid = String.format("%02d", i);
				userName = "User" + i;
				gender = ran.nextInt(2);
				userImg = urlList[ran.nextInt(urlList.length)];
				UsersData.addUserIdAndData(uid, userName, gender, userImg);
			}
			ThreadLogUtils.logThread();
			return null;
		}
		protected void onPostExecute(Void result)
		{
			exAdapter = new ExAdapter(MainListActivity.this, listChild);
			
			if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.JELLY_BEAN_MR2)
				exList.setIndicatorBounds(exList.getRight()-40, exList.getWidth());
			else
				exList.setIndicatorBoundsRelative(exList.getRight()-40, exList.getWidth());
			
			exList.setAdapter(exAdapter);
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
						exList.collapseGroup(i);		//���F�i�}�����Ӧ�m�A��l��m�q�qcollapse!
				}
			}
		});
	}
	
	public void sendClick(View view)
	{
		//ran = new Random();
		//String ranIDcount = UsersData.getUID(ran.nextInt(UsersData.getCount()));
		String user1Id = UsersData.getUID(userSpinner.getSelectedItemPosition());
		
		String toUserText = "";
		input = textInput.getText().toString();
		Log.i("toUserText", textInput.getText().toString());
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
		UsersData.addUserMap(user1Id);
		UsersData.addUserIdStack(user1Id, user2Id);

		if (!input.isEmpty())					//�P�_ textInput ���O�Ū��ܡA�N��ܨӦۨϥΪ̪�input
			UsersData.addMainText(input);
		else
			UsersData.addMainText(getString(R.string.TestingText));
			
		exAdapter.addChildList();
		exAdapter.notifyDataSetChanged();
	}
	/*
	OnKeyListener goKey = new OnKeyListener() {					//��ť�n����L�W���ʧ@�I
		@Override
		public boolean onKey(View v, int keyCode, KeyEvent event) {
			// TODO Auto-generated method stub
			if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_UP) {	//�p�G���U Enter or ���� ������...
				
    			InputMethodManager input = (InputMethodManager)v.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
    			
    			if (input.isActive()) {						//�|Ĳ�o sendClick() �o�� Function
    				sendClick(v);
    				input.hideSoftInputFromWindow(v.getApplicationWindowToken(), 0);
    			}
    			return true;
    		}
			return false;
		}
    };
    */
    public static int getPixels(int dipValue)			//�ۦ�w�q�@�� Dip To Pixels ���\��I
    {
    	Resources res = Resources.getSystem();
    	int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dipValue, res.getDisplayMetrics());
    	//Log.i("Dip to Pixels~~", "" + dipValue + " to " + px);
    	return px;
    }
    
    @SuppressWarnings("deprecation")
	public void createImageBtn()			//�q image_cache ���A�ʺA�إ� ImageButton
    {
    	WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);	//��o WindowManager ���A��
    	int screenWidth = wm.getDefaultDisplay().getWidth();		//���o�ثe�ù����e��(Pixels)
    	
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixels(40), getPixels(40), Gravity.CENTER);
    												//�Y������J�Ʀr���ܡA���|�ODip�A�]���n�� getPixels() �N����ഫ��Pixels�A
    	iconsLayout.removeAllViews();				//�b�p�⪫��b�ù������Ŷ����Y�A�~�|�ܷǽT��~~
    	Bitmap imgBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.wait01);
    	
    	String SDPath = Environment.getExternalStorageDirectory().getPath();
    	String cacheDir = getResources().getString(R.string.cache_dirname);
    	StringBuilder imagePathSb = new StringBuilder();
    	String imagePath = imagePathSb.append(SDPath).append("/").append(cacheDir).append("/").toString();
    	
    	LinearLayout iconLayout = new LinearLayout(this);		//���F new ImageButon ���~�A�]�n new LinearLayout ��I
		iconLayout.setLayoutParams(new LinearLayout.LayoutParams
				(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    	
    	int btnWidthSum = 0;						// ���w�q�@�ӪŪ� int�A�ݷ|�n�ΨӲ֥[ImageButton���e��
    	boolean isFirstCreate = true;
    	
    	for (String[] imgName: getImageName())		//�ھ� getImageName() ����o��size��run�j��
    	{
    		try {
    			imgBitmap = getDecodedBitmap(imagePath + imgName[1], 80, 80);
    		}catch (Exception e) {
    			Log.e("CreateBtnFailed!!", e.getMessage().toString());
    			shortMessage("Buttons Create Failed!");
    		}
    		ImageButton imgBtn = new ImageButton(this);
    		imgBtn.setImageBitmap(imgBitmap);
    		imgBtn.setScaleType(ScaleType.CENTER_CROP);
    		imgBtn.setLayoutParams(params);
    		imgBtn.setTag(imgName[0]);			// setTag() �ڥ��u���n�Ϊ�!!!
    		
    		btnWidthSum += imgBtn.getLayoutParams().width;	//�ǥ� .getLayoutParams().width ��o imgBtn ���e�סA�M��[�� btnWidthSum ���I
    		//Log.i("BtnWidth!", imgBtn.getLayoutParams().width + " of " + btnWidthSum);
    		
    		if (isFirstCreate)				//�ѩ�ä��O�C�鳣�n�[�J new Layout�A�ҥH�n���P�_����~
    		{
    			iconLayout.addView(imgBtn);
    			iconsLayout.addView(iconLayout);	//�p�G�O�Ĥ@�� (isFirstCreate)�A�N���L���󪺥[�J�@�� Layout
    			isFirstCreate = false;
    		}
    		else if (btnWidthSum <= screenWidth)	//�p�G imgBtn �Ҳ֥[���e�סA�٨S�j��ù��e�ת��ܡA�N�~�� add �b�쥻��Layout��
    		{
    			iconLayout.addView(imgBtn);
    		}
    		else if (btnWidthSum > screenWidth)		//�Ϥ��p�G imgBtn �Ҳ֥[���e�פw�j��ù��F�A�N�A new �@��Layout�X��~
    		{
    			iconLayout = new LinearLayout(this);
    			iconLayout.setLayoutParams(new LinearLayout.LayoutParams
    					(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
    			iconLayout.addView(imgBtn);
    			iconsLayout.addView(iconLayout);
    			btnWidthSum = 0;					//�M��M��A�n�O�o�� btnWidthSum �k�s�H���s�p���I
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
	
	public void createIconsBtn()		//��Resources����Drawable�A�ӰʺA�إ�ImageButton
    {
    	smileyIconLayout.removeAllViews();
    	LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(getPixels(40), getPixels(40), Gravity.CENTER);
    										//�P�W���� createImageBtn ���t���h��~
    	int iconRes;
    	
    	for (String[] smileyName: getSmileyName())		//�ھ� getSmileyIcons() ��size��run~
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
	private String withSymbol(String text)		//�@�ӱN String ���e�᳣�[�W���w�Ÿ����p�\��~
	{
		sb = new StringBuilder();
		sb.append("#").append(text).append("#");
		return sb.toString();
	}
	
    private void setIconText(String iconText)
    {
    	SmileysParser.init(this);							//�C�� setSmileyText ���ɭԡA���� SmileysParser ���s init �@���A
    	SmileysParser parser = SmileysParser.getInstance();	//�H�Ψӧ�s�s��images�ɦW�� HashMap
    	
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
    
    public HashMap<String, Integer> getSmileyMap()		//�n�ᵹ SmileysParser �Y�A�ҥH�n���� HashMap
    {
    	HashMap<String, Integer> iconNameItem = new HashMap<String, Integer>(getSmileyName().size());
    	
    	for (String[] icons: getSmileyName())
    	{
    		iconNameItem.put(icons[0], Integer.parseInt(icons[1]));
    	}
    	return iconNameItem;
    }
    
    public List<String[]> getSmileyName()		//�N Resources ���� Drawable ���X�ӡA�ëإߦb List<String[]> ��
    {
    	List<String[]> smileyIconList = new ArrayList<String[]>();
    	String resStr;
    	
    	R.drawable drawable = new R.drawable();
    	Field[] drawRes = R.drawable.class.getFields();
    	
    	for (Field f: drawRes)
    	{
    		try
    		{
    			if (f.getName().contains("smiley"))		//�ǥѧP�_�W�١A�ӿz��X�ڭ̭n�� Drawable
    			{
    				resStr = String.valueOf(f.getInt(drawable));
    				smileyIconList.add(createStringArr(withSymbol(f.getName()), resStr));
    			}						//�N Drawable ����T��� smileyIconList ���A�榡���G#(DrawableName)#[0]�A(DrawableID)[1]
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
    
    public HashMap<String, String> getImageMap()		//�ѩ� SmileyParser �O�Y HashMap �Ӥ��R��ơA�ҥH�o�̤]�� imageNames ����HashMap�I
    {
    	HashMap<String, String> imgNameItem = new HashMap<String, String>(getImageName().size());
    	String imgName;
    	String imgFullName;
    	for (String[] img: getImageName())			//�� for each �I�s getImageName()�Arun����N���@��HashMap��~~
    	{
    		imgName = img[0];
    		imgFullName = img[1];
    		imgNameItem.put(imgName, imgFullName);
    	}
    	return imgNameItem;
    }
    
    public List<String[]> getImageName()			//�N�w�x�s��Image�ɮצW�٩�i�@�� List<String[]> ��
    {
    	File cacheDir;
    	try
    	{			/*---------�ϥ� File ���e�A���n�����H�U���P�_��!!! �Y�SSD�A�N�Τ�����cache��Ƨ��F--------*/
    		if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))
    			cacheDir = new File(Environment.getExternalStorageDirectory(), getString(R.string.cache_dirname));
    		else
    			cacheDir = getCacheDir();
    		if (!cacheDir.exists())
    			cacheDir.mkdirs();
    				/*-------�Y�ۭq�� cache ��Ƨ����s�b�A�N�s�W�X�ӡI----*/
    		
    		File[] imgCount = cacheDir.listFiles();		// List �X cacheDir �̪��Ҧ��ɮ�

    		List<String[]> imageNameList = new ArrayList<String[]>();

    		String imgFullName;
    		//StringBuilder imgNameSb;
    		String imgName;

    		for (File img: imgCount)
    		{
    			imgFullName = img.getName();	//�����ɮצW�١A�]�t���ɦW
    			imgName = imgFullName.replace("%3A", ":").replace("%2F", "/").replace("%2B", "+");
    			
    			//Log.i("imgName", imgName);
    			//Log.i("imgFullName", imgFullName);

    			imageNameList.add(createStringArr(imgName, imgFullName)); //�N �w�ק�L���ɦW[0] & �����ɦW[1] add �i imageNameList ���I
    		}
    		return imageNameList;
    	}
    	catch (Exception e) {
    			e.printStackTrace();
    			Log.e("FileNotFound!", e.toString());
    	}
    	return null;
    }
    private String[] createStringArr(String imgName, String imgFullName)		//���� String[] ���@�Ӥp�F�F~
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
    
    public static Bitmap getDecodedBitmap(String imgPath, int reqWidth, int reqHeight)	//�ھ� Image ���j�p�����Y...
	{
    	int reqWidthPix = getPixels(reqWidth);		//��ڭ� Require �����ন Pixels
		int reqHeightPix = getPixels(reqHeight);
    	
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;			// inJustDecodeBounds = true �ɡA�N�i�H������X�Ϥ��ݩʡA�Ӥ��ξ�i�����J
		BitmapFactory.decodeFile(imgPath, options);	// decodeFile �O�ϥ��ɮת����|��~
		options.inSampleSize = getInSampleSize(options, reqWidthPix, reqHeightPix);	//�ǥ� getInSampleSize�A���w inSampleSize ���ƭ�
		
		int imgWidth = options.outWidth;		//��o�ӷ� Image ���e�� ���� & Type
		int imgHeight = options.outHeight;
		String imgType = options.outMimeType;	//�o�@�q�u�O���F���TLog�X�ӡA���i�H���μg~
		Log.i("ImageInfo~", imgType + " " + imgWidth + " x " + imgHeight);
		
		options.inJustDecodeBounds = false;	//�ݩʧ짹�F�A�N�i�H�� inJustDecodeBounds �������F~
		Bitmap imageInSampleSize = BitmapFactory.decodeFile(imgPath, options);	//�o�ɫ� options �����ƭȬO�w�g�Q���s���w�L�F��I
		return createScaleBitmap(imageInSampleSize, reqWidthPix, reqHeightPix, options.inSampleSize);
	}
	
	private static int getInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight)
	{
		final int width = options.outWidth;			//�ӷ� Image ���e��&����~
		final int height = options.outHeight;
		int inSampleSize = 1;
		
		if (width > reqWidth || height > reqHeight)	//�p�G�ӷ������e �j�� Require ����...
		{
			final int halfWidth = width / 2;		//�N���@�b��~
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
	
	public String getImagePathByName(String name)				//�N�@�q���`��URL��i�ӡA�H�����Image�ɮת�������|
	{
		String SDPath = Environment.getExternalStorageDirectory().getPath();
		String cacheDir = getResources().getString(R.string.cache_dirname);
		String imgPathName;
		
		HashMap<String, String> imageMap = getImageMap();		//�I�s getImageMap()
		
		if (imageMap.containsKey(name)) {						//�p�G imageMap �̦���i�Ӫ����qURL����..
			imgPathName = SDPath + "/" + cacheDir + "/" + imageMap.get(name);	// URL�Y�� imageMap �� key�A�ǥ�URL��o���㪺�ɦW�I
			return imgPathName;
		}
		else {
			shortMessage("Can't Find Image Name in HashMap!");	//���ɫ�|�X�{�o�ӡA��ܤU���٨S�����AHashMap�� key & value �٨S�إ߰_��...
			return null;										//�� URL �N�w�g����L�ӤF�A�ҥH��M�䤣���~~	
		}
	}
	
	@SuppressLint("InflateParams")
	@SuppressWarnings("deprecation")
	public void popImageWindow(String imgUrl)	//�u�X���image�Ϊ������A���M�W�l�̦�pop�A�����O�� AlertDialog
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
	
	public void toUser2 (String userID)		//�����qExAdapter�ǨӪ��ȡA����ܥXUser2
	{
		String[] userData = UsersData.findUserDataById(userID);
		user2Id = userID;
		textInput.setText("@" + userData[0] + " ");
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
    /*
	public void scrollToButtom()
	{
		exList.post(new Runnable() {
			@Override
			public void run() {
				exList.setSelectedGroup(exAdapter.getGroupCount()-1);
			}
		});
	}
	*/
	
	public void userAddClick(View view)
	{
		EditText addInput = (EditText) findViewById(R.id.userNameInput);
		String userName = addInput.getText().toString().replace(" ", "");
		if (!userName.isEmpty())
		{
			addInput.setText("");
			
			String uid = String.format("%02d", UsersData.getCount());
			int gender = ran.nextInt(2);
			String[] urlList = getResources().getStringArray(R.array.url_array);
			String userImgUrl = urlList[ran.nextInt(urlList.length)];
			
			UsersData.addUserIdAndData(uid, userName, gender, userImgUrl);
			setUserSpinner();
			shortMessage(userName + " Has Added! Your ID is " + uid);
		} else
			shortMessage("(��_>��)");
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
			exList.invalidateViews();
			if (iconShown)
				hideIcons();
			
			shortMessage("Image Cache Cleared");
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