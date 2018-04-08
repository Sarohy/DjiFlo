package com.fiv.djiflo.djiflo.util;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;


import com.fiv.djiflo.djiflo.R;

import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;


public class UtilFunctions {
	/**
	 * Check if service is running or not
	 * @param serviceName
	 * @param context
	 * @return
	 */
	public static boolean isServiceRunning(String serviceName, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for(RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if(serviceName.equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	/**
	 * Get the album image from albumId
	 * @param context
	 * @param album_id
	 * @return
	 */
	public static Bitmap getAlbumart(Context context,Long album_id){
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
	    try{
	        final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
	        Uri uri = ContentUris.withAppendedId(sArtworkUri, album_id);
	        ParcelFileDescriptor pfd = context.getContentResolver().openFileDescriptor(uri, "r");
	        if (pfd != null){
	            FileDescriptor fd = pfd.getFileDescriptor();
	            bm = BitmapFactory.decodeFileDescriptor(fd, null, options);
	            pfd = null;
	            fd = null;
	        }
	    } catch(Error ee){}
	    catch (Exception e) {}
	    return bm;
	}
	/**
	 * @param context
	 * @return
	 */
	public static Bitmap getDefaultAlbumArt(Context context){
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
	    try{
	    	bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art, options);
	    } catch(Error ee){}
	    catch (Exception e) {}
	    return bm;
	}
	/**
	 * Convert milliseconds into time hh:mm:ss
	 * @param milliseconds
	 * @return time in String
	 */
	public static String getDuration(long milliseconds) {
		long sec = (milliseconds / 1000) % 60;
		long min = (milliseconds / (60 * 1000))%60;
		long hour = milliseconds / (60 * 60 * 1000);

		String s = (sec < 10) ? "0" + sec : "" + sec;
		String m = (min < 10) ? "0" + min : "" + min;
		String h = "" + hour;
		
		String time = "";
		if(hour > 0) {
			time = h + ":" + m + ":" + s;
		} else {
			time = m + ":" + s;
		}
		return time;
	}
	
	public static boolean currentVersionSupportBigNotification() {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if(sdkVersion >= android.os.Build.VERSION_CODES.JELLY_BEAN){
			return true;
		}
		return false;
	}
	
	public static boolean currentVersionSupportLockScreenControls() {
		int sdkVersion = android.os.Build.VERSION.SDK_INT;
		if(sdkVersion >= android.os.Build.VERSION_CODES.ICE_CREAM_SANDWICH){
			return true;
		}
		return false;
	}
	public static class DownloadImage extends AsyncTask<String,Void,Bitmap>{
		private String src;
		@Override
		protected Bitmap doInBackground(String... strings) {
			src = strings[0];
			try {
				URL url = new URL(src);
				HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				connection.setDoInput(true);
				connection.connect();
				InputStream input = connection.getInputStream();
				Bitmap myBitmap = BitmapFactory.decodeStream(input);
				return myBitmap;
			} catch (IOException e) {
				// Log exception
				return null;
			}

		}
	}
}
