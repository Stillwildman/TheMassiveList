package com.vincent.massivelist;

import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;

public class SmileysParser
{
	private static SmileysParser instance;
	
	public static SmileysParser getInstance()
	{
		return instance;
	}
	public static void init(Context context)
	{
		instance = new SmileysParser(context);
	}
	private final Context context;
	private final HashMap<String, Integer> smileyMap;
	private final List<String[]> smileyName;
	private final Pattern smileyMapPattern;
	
	private final HashMap<String, String> imgMap;
	private final List<String[]> imageNames;
	private final Pattern imgMapPattern;
	
	private SmileysParser(Context context)
	{
		this.context = context;
		this.smileyMap = buildSmileyMap();
		this.smileyName = getSmileyName();
		this.smileyMapPattern = buildPattern();
		
		this.imgMap = buildImgMap();
		this.imageNames = getImgNames();
		this.imgMapPattern = imgMapPattern();
	}

	private HashMap<String, Integer> buildSmileyMap()
	{
		return ((MainListActivity) context).getSmileyMap();
	}
	
	private List<String[]> getSmileyName()
	{
		return ((MainListActivity) context).getSmileyName();
	}
	
	private HashMap<String, String> buildImgMap()
	{
		return ((MainListActivity) context).getImageMap();
	}
	
	private List<String[]> getImgNames()
	{
		return ((MainListActivity) context).getImageName();
	}

	private Pattern buildPattern()
	{
		StringBuilder patternString = new StringBuilder(smileyName.size() * 3);
		patternString.append('(');
		
		for (String[] iconName : smileyName)
		{
			patternString.append(Pattern.quote(iconName[0]));
			patternString.append('|');
		}
		patternString.replace(patternString.length() - 1, patternString.length(), ")");
		
		return Pattern.compile(patternString.toString());
	}
	
	private Pattern imgMapPattern()
	{
		if (!imageNames.isEmpty())
		{
			StringBuilder patternString = new StringBuilder(imageNames.size() * 3);
			patternString.append('(');
			
			for (String[] imgNames : imageNames)
			{
				patternString.append(Pattern.quote(imgNames[0]));
				patternString.append('|');
			}
			patternString.replace(patternString.length() - 1, patternString.length(), ")");
			
			return Pattern.compile(patternString.toString());
		}
		return Pattern.compile("Image Files Empty!");
	}

	@SuppressWarnings("deprecation")
	public CharSequence addIconSpans(CharSequence text)
	{
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		
		String SDPath = Environment.getExternalStorageDirectory().getPath();
		String cacheDir = context.getResources().getString(R.string.cache_dirname);
		
		Matcher smileyMatcher = smileyMapPattern.matcher(text);
		
		while (smileyMatcher.find())
		{
			int resId = smileyMap.get(smileyMatcher.group());
			Drawable resDraw = context.getResources().getDrawable(resId);
			resDraw.setBounds(0, 0, 50, 50);

			ImageSpan imageSpan = new ImageSpan(resDraw, ImageSpan.ALIGN_BOTTOM); 
			builder.setSpan(imageSpan, smileyMatcher.start(), smileyMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}

		String textString = text.toString();

		if (!imgMapPattern.toString().equals("Image Files Empty!"))
		{
			Matcher imgMatcher = imgMapPattern.matcher(text);
			
			while (imgMatcher.find())
			{
				String imgFileName = imgMap.get(imgMatcher.group());
				String imgPathName = SDPath + "/" + cacheDir + "/" + imgFileName;
				Log.d("ImageMatched!!!", imgFileName);
				
				Bitmap image = MainListActivity.getDecodedBitmap(imgPathName, 100, 100);
				//Drawable resDraw = Drawable.createFromPath(SDPath + "/" + cacheDir + "/" + imgFileName);
				//resDraw.setBounds(0, 0, 50, 50);

				//Bitmap bitImg = compressImage(drawableToBitmap(resDraw));
				ImageSpan imageSpan = new ImageSpan(image, ImageSpan.ALIGN_BOTTOM); 
				builder.setSpan(imageSpan, imgMatcher.start(), imgMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
			
		} else if (textString.contains("/") && !textString.contains("//"))
		{
			Log.i("EmptyPattern~~", textString + "\n" + textString.indexOf("/") + " " + textString.lastIndexOf("/"));
			Drawable waitDraw = context.getResources().getDrawable(R.drawable.wait01);
			waitDraw.setBounds(0, 0, 50, 50);
			ImageSpan imageSpan = new ImageSpan(waitDraw, ImageSpan.ALIGN_BOTTOM);
			builder.setSpan(imageSpan, textString.indexOf("/"), textString.lastIndexOf("/")+1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return builder;
	}
	
	public CharSequence addWaitSpans(CharSequence text, String extension)
	{
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		String textString = text.toString();
		
		Drawable waitDraw = context.getResources().getDrawable(R.drawable.wait01);
		waitDraw.setBounds(0, 0, 50, 50);
		
		ImageSpan imageSpan = new ImageSpan(waitDraw, ImageSpan.ALIGN_BOTTOM);
		builder.setSpan(imageSpan, textString.indexOf("http://"),textString.lastIndexOf(extension)+4, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		
		Log.d("FUCK SPANS!!!", textString.substring(textString.indexOf("http://"), textString.lastIndexOf(extension)+4));
		
		return builder;
	}
	
	/*
	private static Bitmap drawableToBitmap(Drawable draw)
	{
		int width = draw.getIntrinsicWidth();
		int height = draw.getIntrinsicHeight();
		
		Bitmap bitImg = Bitmap.createBitmap(width, height, Config.ARGB_8888);
		Canvas canvas = new Canvas(bitImg);
		
		draw.setBounds(0, 0, width, height);
		draw.draw(canvas);
		
		return bitImg;
	}
	
	private static Bitmap compressImage(Bitmap image)
	{
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		image.compress(Bitmap.CompressFormat.PNG, 90, baos);
		
		int options = 80;
		
		while (baos.toByteArray().length / 1024 > 100)
		{
			baos.reset();
			image.compress(Bitmap.CompressFormat.PNG, options, baos);
			options -= 10;
		}
		ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
		Bitmap bitmap = BitmapFactory.decodeStream(bais, null, null);
		
		return bitmap;
	}
	*/
}