package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.vincent.massivelist.LinkTextView.LinkTextViewMovementMethod;

@SuppressLint("NewApi")
public class ExAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	private LayoutInflater inflater;
	
	private Random ran;
	
	SmileysParser parser;
	private HashMap<String, Bitmap> imgMap;
	private StringBuilder urlSb;
	
	ImageLoader imageLoader;
	
	private String mainText = "";
	private String user1Id;
	private String user2Id;
	private String user1Name;
	private String user2Name;
	private String user1Gender;
	private String user2Gender = "";
	private String user1ImgUrl;
	
	private String currentID;
	public boolean isScrollFling;
	public boolean isScrollTouching;
	
	private PostModel postModel;
	
	public ExAdapter(Context context)
	{
		this.context = context;
		
		inflater = LayoutInflater.from(context);
		imageLoader = new ImageLoader(context.getApplicationContext());
		imgMap = new HashMap<String, Bitmap>();
		
		SmileysParser.init(context);
		parser = SmileysParser.getInstance();
	}

	@Override
	public int getGroupCount() {
		return UsersData.postDataMap.size();
	}
	
	@Override
	public Object getGroup(int groupPosition) {
		return UsersData.postDataMap.get(groupPosition);
	}
	
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	@SuppressLint("InflateParams")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewHolder holder;				//這就是很 Efficient 的 ViewHolder & convertView 的利用，各種節省資源阿！
		
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.ex_group, null);
			
			holder = new ViewHolder();
			holder.mainText = (TextView) convertView.findViewById(R.id.groupText);
			holder.userText1 = (TextView) convertView.findViewById(R.id.groupText_User1);
			holder.userText2 = (TextView) convertView.findViewById(R.id.groupText_User2);
			holder.toText1 = (TextView) convertView.findViewById(R.id.toText1);
			holder.toText2 = (TextView) convertView.findViewById(R.id.toText2);
			holder.image = (ImageView) convertView.findViewById(R.id.Image1);
			holder.deleteBtn = (ImageButton) convertView.findViewById(R.id.deleteBtn);
			holder.groupNumber = (TextView) convertView.findViewById(R.id.groupNumber);
			
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		
		((ViewGroup)convertView).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		
		postModel = UsersData.postDataMap.get(UsersData.mapPosList.get(groupPosition));
		
		if (UsersData.postDataMap.containsKey(UsersData.mapPosList.get(groupPosition)))
		{
			mainText = postModel.POST;
		
			user1Id = postModel.UID1;
			user1Name = postModel.NAME1;
			user1Gender = postModel.GENDER1;
			user1ImgUrl = postModel.IMAGE_URL1;
			
			user2Id = postModel.UID2;
			user2Name = postModel.NAME2;
			user2Gender = postModel.GENDER2;
		}
		if (!isScrollFling)
		{
			try
			{
				if (!user1ImgUrl.isEmpty())
					imageLoader.DisplayImage(user1ImgUrl, holder.image);
				RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
						(MainListActivity.getPixels(40), MainListActivity.getPixels(40));
				params.setMargins(10, 10, 5, 0);
				holder.image.setPadding(5, 5, 5, 5);
				holder.image.setLayoutParams(params);
			}
			catch (OutOfMemoryError e) {
				e.printStackTrace();
				Log.e("OOM Oops!", e.getMessage().toString());
			} catch (Exception e) {
				e.printStackTrace();
				Log.e("Oops!", e.getMessage().toString());
			} finally {
				notifyDataSetChanged();
			}

			if (mainText.contains("http://") || mainText.contains("https://"))
			{
				List<String> imgUrlList = getImgUrlString(mainText);

				for (String imgUrl: imgUrlList)
				{
					if (!imgUrl.equals(context.getString(R.string.UnknowImageURL)))
					{
						if (imgMap.containsKey(imgUrl))
						{
							try {
								holder.mainText.setText(parser.addIconSpans(mainText, imgMap));
							} catch (Exception e) {
								holder.mainText.setText(parser.addWaitSpans(mainText, imgUrl.substring(imgUrl.lastIndexOf("."))));
								((MainListActivity) context).messageShort("Slow Down Please!");
							}
						}
						else {
							try {
								holder.mainText.setText(parser.addWaitSpans(mainText, imgUrl));
								downloadBitmapByUrl(imgUrl);
								Log.i("ImageFile", "OH YEAH~~~~~~~~~~");
							} catch (Exception e) {
								e.printStackTrace();
								Log.e("ImageFile", "NO!!!!! What happed~~~");
							}
						}
					} else {
						holder.mainText.setText(parser.addIconSpans(mainText, imgMap));
						Log.i("Input URL", "Unknow Image URL!!!!!!");
					}
				}
			} else
				holder.mainText.setText(parser.addIconSpans(mainText, null));

			holder.mainText.setVisibility(View.VISIBLE);
			if (mainText.length() == 1 && mainText.equals(" "))
				holder.mainText.setVisibility(View.GONE);

			holder.userText1.setText(Html.fromHtml("<u>" + user1Name + "</u>"));
			
			if (!isScrollTouching)
			{
				holder.userText2.setVisibility(View.INVISIBLE);
				holder.toText2.setVisibility(View.INVISIBLE);
				holder.toText1.setText(":");

				if (!user2Name.isEmpty())			//如果 userName2 不是Empty，也就是有變動過的話...
				{
					holder.toText1.setText("-->");
					holder.toText2.setVisibility(View.VISIBLE);
					holder.userText2.setVisibility(View.VISIBLE);
					holder.userText2.setText(Html.fromHtml("<u>" + user2Name + "</u>"));
				}

				holder.image.setFocusable(false);
				holder.image.setFocusableInTouchMode(false);
				holder.image.setClickable(true);
				holder.image.setTag(user1ImgUrl);
				holder.image.setOnClickListener(imgClick);

				holder.mainText.setLongClickable(true);
				holder.mainText.setTag(holder.mainText.getText().toString());
				holder.mainText.setOnLongClickListener(longClick);
				holder.mainText.setAutoLinkMask(Linkify.ALL);		//手動設定LinkMask，但在這裡設的話，它只會幫你標線，不會有點擊事件！
				holder.mainText.setMovementMethod(LinkTextViewMovementMethod.getInstance());	//因此要再 setMovementMethod 給它，
																								//這裡指定給我們客製化的 LinkTextView ~ 
				holder.userText1.setFocusable(false);
				holder.userText1.setFocusableInTouchMode(false);
				holder.userText1.setClickable(true);
				holder.userText1.setTag(user1Id);
				holder.userText1.setOnClickListener(user1Click);

				holder.userText2.setFocusable(false);
				holder.userText2.setFocusableInTouchMode(false);
				holder.userText2.setClickable(true);
				holder.userText2.setTag(user2Id);
				holder.userText2.setOnClickListener(user2Click);

				holder.mainText.setTextColor(Color.BLACK);							//此處解釋請參照下面的convertView!
				holder.userText1.setTextColor(Color.BLACK);
				holder.userText2.setTextColor(Color.BLACK);
				if (user1Gender.equals("Male"))
					holder.userText1.setTextColor(Color.BLUE);
				if (user1Gender.equals("Female"))
					holder.userText1.setTextColor(Color.RED);
				if (user2Gender.equals("Male"))
					holder.userText2.setTextColor(Color.BLUE);
				if (user2Gender.equals("Female"))
					holder.userText2.setTextColor(Color.RED);

				holder.deleteBtn.setTag((int)groupPosition);
				holder.deleteBtn.setOnClickListener(deleteClick);

				//convertView.setBackgroundColor(Color.WHITE);				//每次 View 到這裡都要先把Color設回White，再去判斷if
				//if (isDivisible(groupPosition, 100))						//不然根據ViewHolder Reuse view的特性，
				//	convertView.setBackgroundColor(Color.GRAY);				//已設為Gray的view就算移出去了，還是會馬上被拿回來套用在不對的位置上！
			}
		}
		((MainListActivity) context).showMemory();
		holder.groupNumber.setText(""+(groupPosition+1));
		return convertView;
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return UsersData.listChild.get(groupPosition).size();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return UsersData.listChild.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}
	
	@SuppressWarnings("unchecked")
	@SuppressLint("InflateParams")
	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.ex_child, null);
		
		TextView sampleText = (TextView) layout.findViewById(R.id.childText1);
		
		String childText = ((Map<String, String>)getChild(groupPosition, childPosition)).get("childSample");
		sampleText.setText(childText);
		sampleText.setMovementMethod(LinkTextViewMovementMethod.getInstance());
		
		return layout;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return false;
	}
	
	static class ViewHolder		//自行定義一個ViewHolder，裡面放要在convetView中用到的東西
	{
		TextView mainText;
		TextView userText1;
		TextView userText2;
		TextView toText1;
		TextView toText2;
		ImageView image;
		ImageButton deleteBtn;
		TextView groupNumber;
	}
	
	private boolean isDivisible(int position, int target)		//用於判斷指定的 position 是否為 target 的倍數~(ˋ_>ˊ)
	{
		int total = getGroupCount() / target;
		
		for (int i = 1; i <= total; i++)
		{
			if (position+1 == target * i)
				return true;
		}
		return false;
	}
	
	private List<String> getImgUrlString(String text)	//將groupText丟過來，藉由關鍵字和各種迴圈來把其中的 URLs 建立到 List<String> 裡
	{
		String[] urlArr = text.split("http");
		String url;
		List<String> urlList = new ArrayList<String>();
		
		for (int i = 1; i < urlArr.length; i++)
		{
			if (urlArr[i].contains(".png"))
			{
				url = urlArr[i].substring(0, urlArr[i].lastIndexOf(".png")+4);
				urlSb = new StringBuilder(url);
				urlSb.insert(0, "http");
				urlList.add(urlSb.toString());
			}
			else if (urlArr[i].contains(".jpg"))
			{
				url = urlArr[i].substring(0, urlArr[i].lastIndexOf(".jpg")+4);
				urlSb = new StringBuilder(url);
				urlSb.insert(0, "http");
				urlList.add(urlSb.toString());
			}
			else if (urlArr[i].contains(".gif"))
			{
				url = urlArr[i].substring(0, urlArr[i].lastIndexOf(".gif")+4);
				urlSb = new StringBuilder(url);
				urlSb.insert(0, "http");
				urlList.add(urlSb.toString());
			}
			else if (urlArr[i].contains(".bmp"))
			{
				url = urlArr[i].substring(0, urlArr[i].lastIndexOf(".bmp")+4);
				urlSb = new StringBuilder(url);
				urlSb.insert(0, "http");
				urlList.add(urlSb.toString());
			}
			else
				urlList.add(context.getString(R.string.UnknowImageURL));
		}
		return urlList;
	}
	
	public void downloadBitmapByUrl(final String urlString)
	{
		try
		{
			((MainListActivity) context).LoadingShow();
			new Thread(new Runnable() {
				@Override
				public void run() {
					Log.d("BitmapDownload", "Downloading~~~~");
					imageLoader.getBitmap(urlString, false);	//偷用 ImageLoader 裡的 getBitmap 方法，設為false表示不用他的decodeFile()
					handler.obtainMessage(0, urlString).sendToTarget();	//下載完後將 URL 送去給 handler 玩~
				}
			}).start();
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.e("urlMAP~~~~~", "CANNOT Download!!!!");
		}
	}
	
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()
	{
		@Override
		public void handleMessage(Message msg)
		{
			switch (msg.what)
			{
			case 0:
				String urlString = null;
				if (msg.obj instanceof String)
					urlString = (String) msg.obj;
				
				Log.d("DownloadedURL!", urlString.substring(urlString.lastIndexOf("/")));
				String imgPathName = ((MainListActivity) context).getImagePathByName(urlString);	//藉由URL獲得完整的ImagePathName
				
				try {
					Bitmap imgBitmap = MainListActivity.getDecodedBitmap(imgPathName, 60, 60);
					imgMap.put(urlString, imgBitmap);		//將下載好並Decode完後的Bitmap放入新版的 ImageMap 中！(key即為URL~)
				}
				catch(Exception e) {
					Log.e("ImageBitmap", "OH!!!!!NO~~~~~~~~~");			//如果滑太快，上面的 getDecodedBitmap() 中的工作還來不及完成...
					((MainListActivity)context).messageShort("OH!!!!! NO~~~~~");	//然後你又 View 到那一段的話...那就 OH NO 了阿
				}
				((MainListActivity) context).LoadingHide();
				SmileysParser.init(context);
				parser = SmileysParser.getInstance();
				
				notifyDataSetChanged();
				break;
			}
		}
	};
	
	OnLongClickListener longClick = new OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			LinkTextView.LinkTextViewMovementMethod.cancelMotion();
			
			final String mainText = v.getTag().toString();
			String[] items = {"複製內文", "選取文字"};
			
			AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
			dialogBuilder.setItems(items, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					switch (which)
					{
					case 0:
						ClipboardManager clipBoard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
						ClipData cliper = ClipData.newPlainText("TheSeletedAll", mainText);
						clipBoard.setPrimaryClip(cliper);
						((MainListActivity) context).messageShort("Text Copied!");
						break;
					case 1:
						showTextSelectDialog(mainText);
						break;
					}
				}
			});
			AlertDialog dialog = dialogBuilder.create();
			dialog.setCanceledOnTouchOutside(true);
			dialog.show();
			
			return false;
		}
	};
	
	@SuppressWarnings("deprecation")
	@SuppressLint("InflateParams")
	public void showTextSelectDialog(String mainText)
	{
		View view = inflater.inflate(R.layout.dialog_text_selection, null);
		
		final EditText textSelector = (EditText) view.findViewById(R.id.TextSelection);
		textSelector.setText(parser.addIconSpans(mainText, imgMap));
		textSelector.setTextIsSelectable(true);
		textSelector.selectAll();
		/*
		textSelector.setOnTouchListener(new OnTouchListener() {
			@SuppressLint("ClickableViewAccessibility")
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				int action = event.getAction();
				Layout layout = textSelector.getLayout();
				int line = 0;
				int off = 0;
				switch (action)
				{
				case MotionEvent.ACTION_DOWN:
					line = layout.getLineForVertical(textSelector.getScrollY() + (int) event.getY());
					off = layout.getOffsetForHorizontal(line, (int) event.getX());
					Selection.setSelection(textSelector.getEditableText(), off);
					break;
				case MotionEvent.ACTION_MOVE:
				case MotionEvent.ACTION_UP:
					line = layout.getLineForVertical(textSelector.getScrollY() + (int) event.getY());
					int curOff = layout.getOffsetForHorizontal(line, (int) event.getX());
					Selection.setSelection(textSelector.getEditableText(), off, curOff);
					break;
				}
				return false;
			}
		});
		*/
		textSelector.setCustomSelectionActionModeCallback(new Callback() {
			@Override
			public boolean onCreateActionMode(ActionMode mode, Menu menu) {
				// Called when action mode is first created. The menu supplied
				// will be used to generate action buttons for the action mode.
				mode.setTitle("Make Selection!");
				menu.add(0, 1, 0, "Copy That!").setIcon(R.drawable.copy_icon);
				return true;
			}
			@Override
			public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
				menu.removeItem(android.R.id.selectAll);
				menu.removeItem(android.R.id.cut);
				menu.removeItem(android.R.id.copy);
				menu.removeItem(android.R.id.paste);
				return true;
			}
			@Override
			public void onDestroyActionMode(ActionMode mode) {
				// Called when an action mode is about to be exited and destroyed.
			}
			@Override
			public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
				switch (item.getItemId()) {
				case 1:
					int start = textSelector.getSelectionStart();
					int end = textSelector.getSelectionEnd();
					String selectedText = textSelector.getText().toString().substring(start, end);
					
					ClipboardManager clipBoard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
					ClipData cliper = ClipData.newPlainText("TheSelected", selectedText);
					clipBoard.setPrimaryClip(cliper);
					((MainListActivity) context).messageShort("Text Copied!");
					return true;
				default:
					break;
				}
				return false;
			}
		});
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		int screenWidth = wm.getDefaultDisplay().getWidth();
		int screenHeight = wm.getDefaultDisplay().getHeight();
		int windowWidth = (int) (screenWidth / 1.2);
		int windowHeight = (int) (screenHeight / 1.6);
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
				(windowWidth, windowHeight, Gravity.CENTER_VERTICAL);
		textSelector.setLayoutParams(params);
		
		AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(context);
		dialogBuilder.setView(view);
		dialogBuilder.setPositiveButton("Done!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { }
		});
		dialogBuilder.setNegativeButton("Select All", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { }
		});
		dialogBuilder.setNeutralButton("Copy!", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { }
		});
		AlertDialog dialog = dialogBuilder.create();
		
		Window dialogWindow = dialog.getWindow();
		WindowManager.LayoutParams windowParams = dialogWindow.getAttributes();
		windowParams.alpha = 0.9f;
		dialog.setCanceledOnTouchOutside(false);
		dialog.show();
		
		dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				textSelector.selectAll();
			}
		});
		dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				int start = textSelector.getSelectionStart();
				int end = textSelector.getSelectionEnd();
				String selectedText = textSelector.getText().toString().substring(start, end);
				ClipboardManager clipBoard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
				ClipData cliper = ClipData.newPlainText("TheSelected", selectedText);
				clipBoard.setPrimaryClip(cliper);
				((MainListActivity) context).messageShort("Text Copied!");
			}
		});
	}
	
	OnClickListener user1Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String IDtext = v.getTag().toString();
			MainListActivity.debugText.setText(IDtext + " ");
			
			UserModel userModel = UsersData.userDataMap.get(IDtext);
			MainListActivity.debugText.append(userModel.NAME + " " + userModel.GENDER + " " + userModel.IMAGE_URL);
			
			if (IDtext.equals(currentID)) {
				//((MainListActivity) context).messageShort("(ˋ_>ˊ)");
			}
			else
				((MainListActivity) context).toUser2(IDtext);
		}
	};
	
	OnClickListener user2Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String IDtext = v.getTag().toString();
			MainListActivity.debugText.setText(IDtext + " ");
			
			UserModel userModel = UsersData.userDataMap.get(IDtext);
			MainListActivity.debugText.append(userModel.NAME + " " + userModel.GENDER + " " + userModel.IMAGE_URL);
			
			if (IDtext.equals(currentID)) {
				//((MainListActivity) context).messageShort("(ˋ_>ˊ)");
			}
			else
				((MainListActivity) context).toUser2(IDtext);
		}
	};
	
	OnClickListener imgClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String imgUrl = v.getTag().toString();
			((MainListActivity) context).popImageWindow(imgUrl);
		}
	};
	
	OnClickListener deleteClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			UsersData.deleteOneLine((Integer) v.getTag());
			notifyDataSetChanged();
		}
	};
	
	public void setCurrentID(String ID)
	{
		currentID = ID;
	}
}
