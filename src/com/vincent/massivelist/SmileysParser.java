package com.vincent.massivelist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
	
	private final List<String[]> imageNames;
	private final Pattern imgMapPattern;
	
	private final List<String> urlArr;
	
	private SmileysParser(Context context)
	{
		this.context = context;
		this.smileyMap = buildSmileyMap();
		this.smileyName = getSmileyName();
		this.smileyMapPattern = buildPattern();
		
		this.imageNames = getImgNames();
		this.imgMapPattern = imgMapPattern();
		
		this.urlArr = new ArrayList<String>();
	}

	private HashMap<String, Integer> buildSmileyMap()
	{
		return ((MainListActivity) context).getSmileyMap();
	}
	
	private List<String[]> getSmileyName()
	{
		return ((MainListActivity) context).getSmileyName();
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
	public CharSequence addIconSpans(CharSequence text, HashMap<String, Bitmap> imageMap)
	{
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		
		Matcher smileyMatcher = smileyMapPattern.matcher(text);
		
		while (smileyMatcher.find())
		{
			int resId = smileyMap.get(smileyMatcher.group());
			Drawable resDraw = context.getResources().getDrawable(resId);
			resDraw.setBounds(0, 0, 50, 50);

			ImageSpan imageSpan = new ImageSpan(resDraw, ImageSpan.ALIGN_BOTTOM); 
			builder.setSpan(imageSpan, smileyMatcher.start(), smileyMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		
		if (!imgMapPattern.toString().equals("Image Files Empty!"))
		{
			Matcher imgMatcher = imgMapPattern.matcher(text);
			
			while (imgMatcher.find())
			{
				Bitmap images = imageMap.get(imgMatcher.group());

				ImageSpan imageSpan = new ImageSpan(images, ImageSpan.ALIGN_BOTTOM); 
				builder.setSpan(imageSpan, imgMatcher.start(), imgMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		}
		return builder;
	}
	
	public CharSequence addWaitSpans(CharSequence text, String url)
	{
		SpannableStringBuilder builder = new SpannableStringBuilder(text);
		
		Matcher waitMatcher = waitPattern(url).matcher(text);
		
		Drawable waitDraw = context.getResources().getDrawable(R.drawable.wait01);
		waitDraw.setBounds(0, 0, 50, 50);
		
		while (waitMatcher.find())
		{
			ImageSpan imageSpan = new ImageSpan(waitDraw, ImageSpan.ALIGN_BOTTOM);
			builder.setSpan(imageSpan, waitMatcher.start(), waitMatcher.end(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		Log.d("FUCK SPANS!!!", url);
		return builder;
	}
	
	private Pattern waitPattern(String url)
	{
		urlArr.add(url);
		
		StringBuilder patternString = new StringBuilder(urlArr.size() * 3);
		patternString.append('(');

		for (String urlString : urlArr)
		{
			patternString.append(Pattern.quote(urlString));
			patternString.append('|');
		}
		patternString.replace(patternString.length() - 1, patternString.length(), ")");

		return Pattern.compile(patternString.toString());
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