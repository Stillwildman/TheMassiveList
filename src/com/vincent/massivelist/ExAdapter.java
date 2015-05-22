package com.vincent.massivelist;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
	private Drawable waitIcon;
	//private Bitmap imgBitmap;
	
	private String[] url_array;
	private ArrayList<Integer> ranUrlNumList;
	ImageLoader imageLoader;
	FileCache fileCache;
	//GetWebImg webImg;
	
	private List<String[]> iconName;
	private ArrayList<Integer> ranHtmlCountList;
	private ArrayList<String> ranHtmlColorList;
	private ArrayList<Integer> ranHtmlIconList;
	
	public ExAdapter(Context context, List<Map<String, String>> listGroup,List<List<Map<String, String>>> listChild, String[] urlList)
	{
		this.context = context;
		this.listGroup = listGroup;
		this.listChild = listChild;
		
		waitIcon = context.getResources().getDrawable(R.drawable.wait01);
		waitIcon.setBounds(0, 0, 50, 50);
		
		inflater = LayoutInflater.from(context);
		imageLoader = new ImageLoader(context.getApplicationContext());
		fileCache = new FileCache(context);
		//webImg = new GetWebImg(context);
		
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
	
	@SuppressLint("InflateParams") @Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent)
	{
		// TODO Auto-generated method stub
		ViewHolder holder;				//這就是很 Efficient 的 ViewHolder & convertView 的利用，各種節省資源阿！
		
		if (convertView == null)
		{
			convertView = inflater.inflate(R.layout.ex_group, null);
			
			holder = new ViewHolder();
			holder.text1 = (TextView) convertView.findViewById(R.id.groupText1);
			holder.text2 = (TextView) convertView.findViewById(R.id.groupText2);
			holder.image = (ImageView) convertView.findViewById(R.id.Image1);
			holder.loadingImage = (ProgressBar) convertView.findViewById(R.id.loadingImage);
			
			convertView.setTag(holder);
		} else
			holder = (ViewHolder) convertView.getTag();
		
		String groupText = (String) listGroup.get(groupPosition).get("groupSample");
		String groupNumber = (String) listGroup.get(groupPosition).get("groupNumber");
		
		try
		{
			//imageLoader.DisplayImage(url_array[ranUrlNumList.get(groupPosition)], holder.image);

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
			//holder.image.setImageBitmap(Icon);
		}
		catch (OutOfMemoryError e) {
			e.printStackTrace();
			//Icon.recycle();
			Log.e("OOM Oops!", e.getMessage().toString());
			Log.e("Memory","Out!");
		} catch (Exception e) {
			e.printStackTrace();
			Log.e("Oops!", e.getMessage().toString());
		} finally {
			notifyDataSetChanged();
		}
		
		if (groupText.contains("http://"))
		{
			String imgUrl = getImgUrlString(groupText);
			
			if (!imgUrl.equals("Unkonw Image URL!"))
			{
				File imgFile = fileCache.getFile(imgUrl);

				if (imgFile.exists()) {
					try {
						holder.text1.setText(parser.addIconSpans(groupText));
						Log.i("ExistsFileViewed", imgUrl.substring(imgUrl.lastIndexOf("/")));
					} catch (Exception e) {
						//Log.e("ImageFileFielded", e.getMessage().toString());
						holder.text1.setText(parser.addWaitSpans(groupText, imgUrl.substring(imgUrl.lastIndexOf("."))));
						((MainListActivity) context).shortMessage("Slow Down Please~~");
					}
				}
				else {
					try {
						holder.text1.setText(parser.addWaitSpans(groupText, imgUrl.substring(imgUrl.lastIndexOf("."))));
						downloadBitmapByUrl(imgUrl);
						Log.i("ImageFile", "OH YEAH~~~~~~~~~~");
					} catch (Exception e) {
						e.printStackTrace();
						Log.e("ImageFile", "NO!!!!! What happed~~~");
					}
				}
			} else
				((MainListActivity) context).shortMessage("Unknow URL!!");
		} else
			holder.text1.setText(parser.addIconSpans(groupText));
		
		holder.text2.setText(groupNumber);

		if (holder.image.getDrawable() != null)
		{
			holder.loadingImage.setVisibility(View.GONE);
			holder.image.setVisibility(View.VISIBLE);
			/*												//這一段，是用來產生隨機數量的image在 isDivisible 2 的地方
			holder.exGroupLinear.removeAllViews();
			if (isDivisible(groupPosition, 2))
			{
				int ranNum = ran.nextInt(9)+2;
				for (int i = 0; i < ranNum; i++)
				{
					ImageView iv = new ImageView(context);
					imageLoader.DisplayImage(url_array[ran.nextInt(url_array.length)], iv);
					LinearLayout.LayoutParams params = new LinearLayout.LayoutParams
							(LayoutParams.MATCH_PARENT, 50, Gravity.CENTER);
					holder.exGroupLinear.addView(iv, params);
				}
			}
			*/
		}
		
		holder.text1.setTextColor(Color.BLACK);							//此處解釋請參照下面的convertView!
		holder.text2.setTextColor(Color.BLACK);
		for (int i = 0; i < ranCount; i++)								//run ranCount 次的迴圈，比對目前的Position與被選出來的Position是否一樣
		{
			if (groupPosition+1 == ranPosList.get(i))					// ranPosList 中的值是從 1 開始，groupPosition是從 0 開始，所以要+1
			{
				holder.text1.setTextColor(Integer.parseInt(ranColorList.get(i)));
				holder.text2.setTextColor(Integer.parseInt(ranColorList.get(ranCount-(i+1))));		//反向從 ranColorList 中取出值來！
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
		TextView text1;
		TextView text2;
		ImageView image;
		ProgressBar loadingImage;
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
	
	private String getImgUrlString(String text)
	{
		if (text.contains(".png"))
			return text.substring(text.indexOf("http://"), text.lastIndexOf(".png")+4);
		if (text.contains(".jpg"))
			return text.substring(text.indexOf("http://"), text.lastIndexOf(".jpg")+4);
		if (text.contains(".gif"))
			return text.substring(text.indexOf("http://"), text.lastIndexOf(".gif")+4);
		if (text.contains(".bmp"))
			return text.substring(text.indexOf("http://"), text.lastIndexOf(".bmp")+4);
		return "Unknow Image URL!";
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
					imageLoader.getBitmap(urlString, false);
					handler.obtainMessage(0, urlString).sendToTarget();
				}
			}).start();
		}
		catch (Exception e) {
			e.printStackTrace();
			Log.e("urlMAP~~~~~", "Didn't get the Position!");
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
				SmileysParser.init(context);
				parser = SmileysParser.getInstance();
				
				((MainListActivity) context).LoadingHide();
				//((MainListActivity) context).createImageBtn();
				notifyDataSetChanged();
				
				break;
			}
		}
	};
}
