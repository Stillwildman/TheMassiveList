package com.vincent.massivelist;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Environment;
import android.os.Handler;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

public class ImageLoader
{
    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());
    ExecutorService executorService;
    Handler handler = new Handler();	//handler to display images in UI thread
    
    String imgCachePath;
    
    public ImageLoader(Context context)
    {
        fileCache  =  new FileCache(context);
        executorService = Executors.newFixedThreadPool(5);
        imgCachePath = Environment.getExternalStorageDirectory().getPath() + "/" + context.getString(R.string.cache_dirname) + "/"; 
    }
    
    
    final int tempImage = R.drawable.wait01;
    
    public void DisplayImage(String url, ImageView imageView)
    {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        
        if(bitmap != null)
            imageView.setImageBitmap(bitmap);
        else
        {
            queuePhoto(url, imageView);
            imageView.setImageResource(tempImage);
        }
    }
        
    private void queuePhoto(String url, ImageView imageView)
    {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        executorService.submit(new PhotosLoader(p));
    }
    
    public Bitmap getBitmap(String url, boolean needDecode) 
    {
        File f = fileCache.getFile(url);
        
        //from SD cache
        Bitmap b  =  decodeFile(f);
        
        if(b != null)
            return b;
        else {
        	//from web
        	try
        	{
        		Log.d("getBitmap", "Getitng Bitmap~~");
        		Bitmap bitmap = null;
        		URL imageUrl  =  new URL(url);
        		HttpURLConnection conn  =  (HttpURLConnection)imageUrl.openConnection();
        		conn.setConnectTimeout(30000);
        		conn.setReadTimeout(30000);
        		conn.setInstanceFollowRedirects(true);
        		InputStream is = conn.getInputStream();
        		OutputStream os  =  new FileOutputStream(f);
        		Utils.CopyStream(is, os);
        		os.close();
        		conn.disconnect();
        		
        		if (needDecode) {
        			String imgFileName = imgCachePath + f.getName();
        			bitmap  =  MainListActivity.getDecodedBitmap(imgFileName, 50, 50);
        			//bitmap = decodeFile(f);
        			return bitmap;
        		} else
        			return null;
        	}
        	catch (Throwable ex)
        	{
        		Log.e("getBitmapFiled", ""+ex);
        		ex.printStackTrace();
        		if(ex instanceof OutOfMemoryError)
        			memoryCache.clear();
        		return null;
        	}
        }
    }

    //decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(File f)
    {
        try
        {
        	Log.d("DecodeFile", "Decoding Bitmap~~");
            //decode image size
            BitmapFactory.Options bfOptions1  =  new BitmapFactory.Options();
            bfOptions1.inJustDecodeBounds  =  true;
            FileInputStream stream1 = new FileInputStream(f);
            BitmapFactory.decodeStream(stream1, null, bfOptions1);
            stream1.close();
            
            //Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = 70;
            int width_tmp = bfOptions1.outWidth, height_tmp = bfOptions1.outHeight;
            int scale = 1;
            while(true)
            {
                if(width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            
            //decode with inSampleSize
            BitmapFactory.Options bfOptions2  =  new BitmapFactory.Options();
            bfOptions2.inSampleSize = scale;
            FileInputStream stream2 = new FileInputStream(f);
            Bitmap bitmap = BitmapFactory.decodeStream(stream2, null, bfOptions2);
            stream2.close();
            Log.d("DecodeFile", "Decoding DONE!!");
            return bitmap;
        }
        catch (FileNotFoundException e) {
        	e.printStackTrace();
        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    //Task for the queue
    private class PhotoToLoad
    {
        public String url;
        public ImageView imageView;
        public PhotoToLoad(String u, ImageView i)
        {
            url = u; 
            imageView = i;
        }
    }
    
    class PhotosLoader implements Runnable
    {
        PhotoToLoad photoToLoad;
        PhotosLoader(PhotoToLoad photoToLoad)
        {
            this.photoToLoad = photoToLoad;
        }
        
        @Override
        public void run()
        {
            try
            {
                if (imageViewReused(photoToLoad))
                    return;
                
                Bitmap bmp = getBitmap(photoToLoad.url, true);
                memoryCache.put(photoToLoad.url, bmp);
                
                if (imageViewReused(photoToLoad))
                    return;
                
                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            }
            catch(Throwable th) {
                th.printStackTrace();
            }
        }
    }
    
    boolean imageViewReused(PhotoToLoad photoToLoad)
    {
        String tag = imageViews.get(photoToLoad.imageView);
        if(tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }
    
    //Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable
    {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;
        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {bitmap = b; photoToLoad = p;}
        public void run()
        {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else {}
                photoToLoad.imageView.setImageResource(tempImage);
        }
    }

    public void clearCache()
    {
        memoryCache.clear();
        fileCache.clear();
    }
}
