package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.vincent.massivelist.LinkTextView.LinkTextViewMovementMethod;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ExAdapter extends BaseExpandableListAdapter {
	
	private Context context;
	private LayoutInflater inflater;
	private List<Map<String, String>> listGroup;
	private List<List<Map<String, String>>> listChild;
	
	private Random ran;
	private int ranCount;
	private ArrayList<Integer> ranPosList;
	private ArrayList<String> ranColorList;
	private StringBuilder htmlSb;
	
	SmileysParser parser;
	private HashMap<String, Bitmap> imgMap;
	private StringBuilder urlSb;
	
	private String[] url_array;
	private ArrayList<Integer> ranUrlNumList;
	ImageLoader imageLoader;
	//GetWebImg webImg;
	
	private List<String[]> iconName;
	private ArrayList<Integer> ranHtmlCountList;
	private ArrayList<String> ranHtmlColorList;
	private ArrayList<Integer> ranHtmlIconList;
	
	private String toUserText;
	private String changedText;
	private boolean textChanged;
	
	public ExAdapter(Context context, List<Map<String, String>> listGroup,List<List<Map<String, String>>> listChild, String[] urlList)
	{
		this.context = context;
		this.listGroup = listGroup;
		this.listChild = listChild;
		
		inflater = LayoutInflater.from(context);
		imageLoader = new ImageLoader(context.getApplicationContext());
		//webImg = new GetWebImg(context);
		imgMap = new HashMap<String, Bitmap>();
		
		ranCount = (int) (getGroupCount() * 0.5);
		setRanColor();
		
		SmileysParser.init(context);
		parser = SmileysParser.getInstance();
		
		this.url_array = urlList;
		ranUrlNumList = new ArrayList<Integer>();
		getRanArrNum();
		
		iconName = ((MainListActivity) context).getImageName();  //獲得已存在cache中的image檔名
		ranHtmlCountList = new ArrayList<Integer>();			//這3個東西，是用來將 隨機Color & 隨機imageName 存成List，
		ranHtmlColorList = new ArrayList<String>();				//然後要在某個 isDivisible 的地方顯示用的~
		ranHtmlIconList = new ArrayList<Integer>();
		setRanHtmlAtDivisible(5);
		
		
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return listGroup.size();
	}
	
	@Override
	public Object getGroup(int groupPosition) {
		// TODO Auto-generated method stub
		return listGroup.get(groupPosition);
	}
	
	@Override
	public long getGroupId(int groupPosition) {
		// TODO Auto-generated method stub
		return groupPosition;
	}
	
	@SuppressLint("InflateParams")
	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
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
			
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		
		((ViewGroup)convertView).setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
		
		String groupText = (String) listGroup.get(groupPosition).get("groupSample");
		String groupNumber = (String) listGroup.get(groupPosition).get("groupNumber");
		
		if (textChanged)				//暫時性使用的方案，如果有 Text Changed 的話，就覆蓋 groupText 的內容~
			groupText = changedText;
		
		try
		{
			imageLoader.DisplayImage(url_array[ranUrlNumList.get(groupPosition)], holder.image);
			RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams
					(MainListActivity.getPixels(35), MainListActivity.getPixels(35));
			params.setMargins(10, 10, 5, 0);
			holder.image.setPadding(5, 5, 5, 5);
			holder.image.setLayoutParams(params);
			
			/*	//(The "GetWebImg" way, from PTT)
				if (webImg.IsCache(url_array[ranUrlNumList.get(groupPosition)]) == false)
					webImg.LoadUrlPic(url_array[ranUrlNumList.get(groupPosition)], handler);
				else if (webImg.IsDownLoadFine(url_array[ranUrlNumList.get(groupPosition)]) == true)
				{
					holder.image.setImageBitmap(webImg.getImg(url_array[ranUrlNumList.get(groupPosition)]));
					holder.loadingImage.setVisibility(View.GONE);
					holder.image.setVisibility(View.VISIBLE);
				} else {}
			 */
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
					Log.d("GetImageURL!!", imgUrl);
					
					if (imgMap.containsKey(imgUrl))
					{
						try {
							holder.mainText.setText(parser.addIconSpans(groupText, imgMap));
							Log.i("ExistsFileViewed", imgUrl.substring(imgUrl.lastIndexOf("/")));
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
		
		holder.userText1.setText(groupNumber);
		
		holder.image.setFocusable(false);
		holder.image.setFocusableInTouchMode(false);
		holder.image.setClickable(true);
		holder.image.setTag(url_array[ranUrlNumList.get(groupPosition)]);
		holder.image.setOnClickListener(imgClick);
		
		holder.mainText.setFocusable(false);
		holder.mainText.setFocusableInTouchMode(false);
		holder.mainText.setClickable(true);
		holder.mainText.setAutoLinkMask(Linkify.ALL);		//手動設定LinkMask，但在這裡設的話，它只會幫你標線，不會有點擊事件！
		holder.mainText.setMovementMethod(LinkTextViewMovementMethod.getInstance());	//因此要再 setMovementMethod 給它，
																					//這裡指定給我們客製化的 LinkTextView ~ 
		holder.userText1.setFocusable(false);
		holder.userText1.setFocusableInTouchMode(false);
		holder.userText1.setClickable(true);
		holder.userText1.setTag(holder.userText1.getText());
		holder.userText1.setOnClickListener(userClick);
		
		holder.userText2.setFocusable(false);
		holder.userText2.setFocusableInTouchMode(false);
		holder.userText2.setClickable(true);
		holder.userText2.setTag(holder.userText2.getText());
		holder.userText2.setOnClickListener(user2Click);
		
		if (toUserText.length() != 0)			//如果 toUserText 長度不等於 0 的話，也就是有變動過的話...
		{
			if (toUserText != groupNumber)		
			{
				holder.toText1.setText("-->");
				holder.toText2.setVisibility(View.VISIBLE);
				holder.userText2.setVisibility(View.VISIBLE);
				holder.userText2.setText(toUserText);
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
		
		holder.mainText.setTextColor(Color.BLACK);							//此處解釋請參照下面的convertView!
		holder.userText1.setTextColor(Color.BLACK);
		holder.userText1.setTextColor(Color.DKGRAY);
		for (int i = 0; i < ranCount; i++)								//run ranCount 次的迴圈，比對目前的Position與被選出來的Position是否一樣
		{
			if (groupPosition+1 == ranPosList.get(i))					// ranPosList 中的值是從 1 開始，groupPosition是從 0 開始，所以要+1
			{
				holder.mainText.setTextColor(Integer.parseInt(ranColorList.get(i)));
				holder.userText1.setTextColor(Integer.parseInt(ranColorList.get(ranCount-(i+1))));		//反向從 ranColorList 中取出值來！
			}
		}
		
		convertView.setBackgroundColor(Color.WHITE);				//每次 View 到這裡都要先把Color設回White，再去判斷if
		if (isDivisible(groupPosition, 100))						//不然根據ViewHolder Reuse view的特性，
			convertView.setBackgroundColor(Color.GRAY);				//已設為Gray的view就算移出去了，還是會馬上被拿回來套用在不對的位置上！
		/*
		if (!iconName.isEmpty())
		{
			for (int i = 0; i < ranHtmlCountList.size(); i++)
			{
				if (groupPosition+1 == ranHtmlCountList.get(i))
					holder.text1.setText(parser.addSmileySpans(htmlText(groupText, ranHtmlColorList.get(i), ranHtmlIconList.get(i))));
			}
		}
		*/
		((MainListActivity) context).showMemory();
		return convertView;
	}
	/*
	@SuppressLint("HandlerLeak")
	Handler handler = new Handler()		//告訴BaseAdapter資料已經更新了 (給 GetWebImg 用的 Handler)
	{
		@Override
		public void handleMessage(Message msg)
		{
			Log.d("Handler", "notifyDataSetChanged");
			notifyDataSetChanged();
			super.handleMessage(msg);
		}
	};
	 */
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
		
		TextView sampleText = (TextView) layout.findViewById(R.id.childText1);
		
		@SuppressWarnings("unchecked")
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
	
	private void setRanColor()							//產生 ranCount 個的隨機 Position 與 Color，並且各放入對應的ArrayList中~
	{
		ran = new Random();
		
		ranPosList = new ArrayList<Integer>();
		ranColorList = new ArrayList<String>();
		
		StringBuilder ranColorSb = new StringBuilder();		//專業的都要用 StringBuild or StringBuffer 阿！ 
		
		for (int i = 0; i < ranCount; i++)
		{
			int ranPos = ran.nextInt(getGroupCount())+1;
			int ranColor = 0xff000000 | ran.nextInt(0x00ffffff);	// Random 出  Color 代碼，沒字母，只有數字，短至6位，長至8位，
																	//不確定是幾進制，而且產出結果都是以 "-" 開頭，
			ranColorSb.delete(0, 9);								//重點是，竟然還可以直接用 setTextColor 來套用?! Tell me why~~~~(ˊ_>ˋ)
			ranColorSb.append(String.valueOf(ranColor));
			
			ranPosList.add(ranPos);
			ranColorList.add(ranColorSb.toString());
			
			//Log.i("RanColor", "" + ranColor);
		}
		Log.i("RanCount", "" + ranCount);
	}
	
	public Spanned htmlText(String text, String ranColor, int ranIcon)
	{
		ran = new Random();
		
		int textLen = text.length() / 2;							//將收到的字串，取一半長 (測試用!)
		//int ranColor = 0xff000000 | ran.nextInt(0x00ffffff);		//產生隨機 Color 代碼~
		
		String text1 = text.substring(0, textLen);
		String text2 = text.substring(textLen);
		
		htmlSb = new StringBuilder();
		
		if (iconName.size() != 0)							//ranIcon = Random for IconList(ImageList)，總值是 0~List.size();
		{													//也就是 ranHtmlIconList
			htmlSb.insert(0,"<b>").append(text1).append("</b>")
			.append(iconName.get(ranIcon)[0]).append("<font color=").append(ranColor)
			.append("><i>").append(text2).append("</i></font>");
		}
		return Html.fromHtml(htmlSb.toString());				//以上都跟 html 無關！只有這行的 Html.fromHtml() 才跟 html 有關阿~
	}
	
	private void getRanArrNum()
	{
		for (int i = 0; i < getGroupCount(); i++)
		{
			ranUrlNumList.add(ran.nextInt(url_array.length));
		}
	}
	
	private void setRanHtmlAtDivisible(int position)
	{
		int total = getGroupCount() / position;
		for (int i = 1; i <= total; i++)
		{
			ranHtmlCountList.add(position * i);
		}
		ran = new Random();
		int ranColor;
		int ranIconNum;
		
		for (int i = 0; i < ranHtmlCountList.size(); i++)
		{
			ranColor = 0xff000000 | ran.nextInt(0x00ffffff);
			ranHtmlColorList.add(String.valueOf(ranColor));
			if (!iconName.isEmpty())
			{
				ranIconNum = ran.nextInt(iconName.size());
				ranHtmlIconList.add(ranIconNum);
			}
		}
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
					((MainListActivity)context).shortMessage("OH!!!!! NO~~~~~");	//然後你又 View 到那一段的話...那就 OH NO 了阿
				}
				((MainListActivity) context).LoadingHide();
				SmileysParser.init(context);
				parser = SmileysParser.getInstance();
				
				notifyDataSetChanged();
				break;
			}
		}
	};
	
	OnClickListener userClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String text = (String) v.getTag();
			MainListActivity.toUser2(text);
		}
	};
	
	OnClickListener user2Click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String text = (String) v.getTag();
			MainListActivity.toUser2(text);
		}
	};
	
	OnClickListener imgClick = new OnClickListener() {
		@Override
		public void onClick(View v) {
			String imgUrl = v.getTag().toString();
			((MainListActivity) context).popImageWindow(imgUrl);
		}
	};
	/**
	 * @param user2		User2名子
	 * @param mainText	更新後的mainText
	 * @param changed	是否要套用新的mainText
	 */
	public void setUserAndTextChanged(String user2, String mainText, boolean changed)	//動態更新 exAdapter 的內容
	{
		toUserText = user2;
		changedText = mainText;
		textChanged = changed;
		notifyDataSetChanged();
	}
}
