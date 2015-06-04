package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.vincent.massivelist.LinkTextView.LinkTextViewMovementMethod;
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
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseExpandableListAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

@SuppressLint("NewApi")
public class ExAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	private LayoutInflater inflater;
	private List<Map<Integer, String[]>> listGroup;
	private List<List<Map<String, String>>> listChild;
	
	private Random ran;
	
	SmileysParser parser;
	private HashMap<String, Bitmap> imgMap;
	private StringBuilder urlSb;
	
	ImageLoader imageLoader;
	
	private String user2Text;
	private String changedText;
	private boolean textChanged;
	
	private ArrayList<String> mainTextList;
	private String groupText;
	private String[] groupUserData;
	private int userId;
	private String userName;
	private String userGender;
	private String userImgUrl;
	private ArrayList<Integer> IDList;
	
	public ExAdapter(Context context, List<Map<Integer, String[]>> listGroup,List<List<Map<String, String>>> listChild)
	{
		this.context = context;
		this.listGroup = listGroup;
		this.listChild = listChild;
		
		inflater = LayoutInflater.from(context);
		imageLoader = new ImageLoader(context.getApplicationContext());
		imgMap = new HashMap<String, Bitmap>();
		
		SmileysParser.init(context);
		parser = SmileysParser.getInstance();
		
		mainTextList = new ArrayList<String>();
		IDList = new ArrayList<Integer>();
	}

	@Override
	public int getGroupCount() {
		return listGroup.size();
	}
	
	@Override
	public Object getGroup(int groupPosition) {
		return listGroup.get(groupPosition);
	}
	
	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}
	
	@SuppressLint("InflateParams")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		ViewHolder holder;				//�o�N�O�� Efficient �� ViewHolder & convertView ���Q�ΡA�U�ظ`�ٸ귽���I
		
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
			
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		
		((ViewGroup)convertView).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		
		
		if (!mainTextList.isEmpty())
			groupText = mainTextList.get(groupPosition);
		
		if (!listGroup.isEmpty())
		{
			groupUserData = (String[]) listGroup.get(groupPosition).get(IDList.get(groupPosition));
			userName = groupUserData[0];
			userImgUrl = groupUserData[2];
		}
			
		String groupNumber = "";
		
		try
		{
			if (!userImgUrl.isEmpty())
				imageLoader.DisplayImage(userImgUrl, holder.image);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
					(MainListActivity.getPixels(35), MainListActivity.getPixels(35));
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
		
		if (groupText.contains("http://") || groupText.contains("https://"))
		{
			List<String> imgUrlList = getImgUrlString(groupText);
			
			for (String imgUrl: imgUrlList)
			{
				if (!imgUrl.equals(context.getString(R.string.UnknowImageURL)))
				{
					if (imgMap.containsKey(imgUrl))
					{
						try {
							holder.mainText.setText(parser.addIconSpans(groupText, imgMap));
						} catch (Exception e) {
							holder.mainText.setText(parser.addWaitSpans(groupText, imgUrl.substring(imgUrl.lastIndexOf("."))));
							((MainListActivity) context).shortMessage("Slow Down Please!");
						}
					}
					else {
						try {
							holder.mainText.setText(parser.addWaitSpans(groupText, imgUrl));
							downloadBitmapByUrl(imgUrl);
							Log.i("ImageFile", "OH YEAH~~~~~~~~~~");
						} catch (Exception e) {
							e.printStackTrace();
							Log.e("ImageFile", "NO!!!!! What happed~~~");
						}
					}
				} else {
					holder.mainText.setText(parser.addIconSpans(groupText, imgMap));
					Log.i("Input URL", "Unknow Image URL!!!!!!");
				}
			}
		} else
			holder.mainText.setText(parser.addIconSpans(groupText, null));
		
		holder.userText1.setText(Html.fromHtml("<u>" + userName + "</u>"));
		
		holder.image.setFocusable(false);
		holder.image.setFocusableInTouchMode(false);
		holder.image.setClickable(true);
		holder.image.setTag(userImgUrl);
		holder.image.setOnClickListener(imgClick);
		
		holder.mainText.setLongClickable(true);
		holder.mainText.setTag(holder.mainText.getText().toString());
		holder.mainText.setOnLongClickListener(longClick);
		holder.mainText.setAutoLinkMask(Linkify.ALL);		//��ʳ]�wLinkMask�A���b�o�̳]���ܡA���u�|���A�нu�A���|���I���ƥ�I
		holder.mainText.setMovementMethod(LinkTextViewMovementMethod.getInstance());	//�]���n�A setMovementMethod �����A
																						//�o�̫��w���ڭ̫Ȼs�ƪ� LinkTextView ~ 
		holder.userText1.setFocusable(false);
		holder.userText1.setFocusableInTouchMode(false);
		holder.userText1.setClickable(true);
		holder.userText1.setTag(holder.userText1.getText());
		
		//holder.userText1.setId();
		
		//holder.userText1.setOnClickListener(user1Click);
		
		holder.userText2.setFocusable(false);
		holder.userText2.setFocusableInTouchMode(false);
		holder.userText2.setClickable(true);
		holder.userText2.setTag(holder.userText2.getText());
		//holder.userText2.setOnClickListener(user2Click);
		
		if (!user2Text.isEmpty())			//�p�G user2Text ���OEmpty�A�]�N�O���ܰʹL����...
		{
			if (!user2Text.equals(groupNumber))		
			{
				holder.toText1.setText("-->");
				holder.toText2.setVisibility(View.VISIBLE);
				holder.userText2.setVisibility(View.VISIBLE);
				holder.userText2.setText(Html.fromHtml("<u>" + user2Text + "</u>"));
			} else {
				holder.userText2.setVisibility(View.GONE);
				holder.toText2.setVisibility(View.GONE);
				holder.toText1.setText(":");
				holder.mainText.setText("");
			}
		} else
		{
			holder.toText1.setText(":");
			holder.toText2.setVisibility(View.GONE);
			holder.userText2.setVisibility(View.GONE);
		}
		
		holder.mainText.setTextColor(Color.BLACK);							//���B�����аѷӤU����convertView!
		holder.userText1.setTextColor(Color.BLACK);
		holder.userText1.setTextColor(Color.DKGRAY);
		
		convertView.setBackgroundColor(Color.WHITE);				//�C�� View ��o�̳��n����Color�]�^White�A�A�h�P�_if
		if (isDivisible(groupPosition, 50))						//���M�ھ�ViewHolder Reuse view���S�ʡA
			convertView.setBackgroundColor(Color.GRAY);				//�w�]��Gray��view�N�Ⲿ�X�h�F�A�٬O�|���W�Q���^�ӮM�Φb���諸��m�W�I
		
		((MainListActivity) context).showMemory();
		return convertView;
	}
	
	@Override
	public int getChildrenCount(int groupPosition) {
		// TODO Auto-generated method stub
		return listChild.get(groupPosition).size();
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return listChild.get(groupPosition).get(childPosition);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		// TODO Auto-generated method stub
		return childPosition;
	}
	
	@SuppressLint("InflateParams") @Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout layout = (RelativeLayout) inflater.inflate(R.layout.ex_child, null);
		
		//TextView sampleText = (TextView) layout.findViewById(R.id.childText1);
		
		//String childText = ((Map<String, String>)getChild(groupPosition, childPosition)).get("childSample");
		//sampleText.setText(childText);
		//sampleText.setMovementMethod(LinkTextViewMovementMethod.getInstance());
		
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
	
	static class ViewHolder		//�ۦ�w�q�@��ViewHolder�A�̭���n�bconvetView���Ψ쪺�F��
	{
		TextView mainText;
		TextView userText1;
		TextView userText2;
		TextView toText1;
		TextView toText2;
		ImageView image;
	}
	
	private boolean isDivisible(int position, int target)		//�Ω�P�_���w�� position �O�_�� target ������~(��_>��)
	{
		int total = getGroupCount() / target;
		
		for (int i = 1; i <= total; i++)
		{
			if (position+1 == target * i)
				return true;
		}
		return false;
	}
	
	private List<String> getImgUrlString(String text)	//�NgroupText��L�ӡA�ǥ�����r�M�U�ذj��ӧ�䤤�� URLs �إߨ� List<String> ��
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
					imageLoader.getBitmap(urlString, false);	//���� ImageLoader �̪� getBitmap ��k�A�]��false��ܤ��ΥL��decodeFile()
					handler.obtainMessage(0, urlString).sendToTarget();	//�U������N URL �e�h�� handler ��~
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
				String imgPathName = ((MainListActivity) context).getImagePathByName(urlString);	//�ǥ�URL��o���㪺ImagePathName
				
				try {
					Bitmap imgBitmap = MainListActivity.getDecodedBitmap(imgPathName, 60, 60);
					imgMap.put(urlString, imgBitmap);		//�N�U���n��Decode���᪺Bitmap��J�s���� ImageMap ���I(key�Y��URL~)
				}
				catch(Exception e) {
					Log.e("ImageBitmap", "OH!!!!!NO~~~~~~~~~");			//�p�G�Ƥӧ֡A�W���� getDecodedBitmap() �����u�@�٨Ӥ��Χ���...
					((MainListActivity)context).shortMessage("OH!!!!! NO~~~~~");	//�M��A�S View �쨺�@�q����...���N OH NO �F��
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
			String[] items = {"�ƻs����", "�����r"};
			
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
						((MainListActivity) context).shortMessage("Text Copied!");
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
					((MainListActivity) context).shortMessage("Text Copied!");
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
				((MainListActivity) context).shortMessage("Text Copied!");
			}
		});
	}
	/*
	OnClickListener user1Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String text = v.getTag().toString();
			MainListActivity.toUser2(text);
			
			EditText textInput = (EditText) ((MainListActivity) context).findViewById(R.id.textInput);
			textInput.setText(String.valueOf(userId));
			((MainListActivity) context).shortMessage("" + userId);
		}
	};
	
	OnClickListener user2Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String text = v.getTag().toString();
			MainListActivity.toUser2(text);
			
			EditText textInput = (EditText) ((MainListActivity) context).findViewById(R.id.textInput);
			textInput.setText(String.valueOf(userId));
			((MainListActivity) context).shortMessage("" + userId);
		}
	};
	*/
	OnClickListener imgClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String imgUrl = v.getTag().toString();
			((MainListActivity) context).popImageWindow(imgUrl);
		}
	};
	/**
	 * @param user2		User2�W�l
	 * @param mainText	��s�᪺mainText
	 * @param changed	�O�_�n�M�ηs��mainText
	 */
	public void setUserAndTextChanged(String user2, String mainText, boolean changed)	//�ʺA��s exAdapter �����e
	{
		user2Text = user2;
		changedText = mainText;
		textChanged = changed;
		notifyDataSetChanged();
	}
	
	public void addListGroupItems(Map<Integer, String[]> items, int ID)
	{
		listGroup.add(items);
		IDList.add(ID);
		notifyDataSetChanged();
	}
	
	public void addMainText(String text)
	{
		mainTextList.add(text);
		notifyDataSetChanged();
	}
}
