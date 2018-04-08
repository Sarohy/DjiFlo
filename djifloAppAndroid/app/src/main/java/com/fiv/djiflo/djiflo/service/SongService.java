package com.fiv.djiflo.djiflo.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.RemoteControlClient;
import android.media.RemoteControlClient.MetadataEditor;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;

import com.fiv.djiflo.djiflo.DataLayer.Song;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.View.Activity.HomeActivity;
import com.fiv.djiflo.djiflo.View.Activity.SongDetailActivity;
import com.fiv.djiflo.djiflo.controls.Controls;
import com.fiv.djiflo.djiflo.receiver.NotificationBroadcast;
import com.fiv.djiflo.djiflo.util.PlayerConstants;
import com.fiv.djiflo.djiflo.util.UtilFunctions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

public class SongService extends Service implements AudioManager.OnAudioFocusChangeListener{
	String LOG_CLASS = "SongService";
	private MediaPlayer mp;
	int NOTIFICATION_ID = 1111;
	public static final String NOTIFY_PREVIOUS = "com.fiv.music.djiflo.previous";
	public static final String NOTIFY_DELETE = "com.fiv.music.djiflo.delete";
	public static final String NOTIFY_PAUSE = "com.fiv.music.djiflo.pause";
	public static final String NOTIFY_PLAY = "com.fiv.music.djiflo.play";
	public static final String NOTIFY_NEXT = "com.fiv.music.djiflo.next";
	FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference MusicRef = database.getReference().child("Musics");;


	private ComponentName remoteComponentName;
	private RemoteControlClient remoteControlClient;
	AudioManager audioManager;
	Bitmap mDummyAlbumArt;
	private static Timer timer; 
	private static boolean currentVersionSupportBigNotification = false;
	private static boolean currentVersionSupportLockScreenControls = false;
	private boolean isAdd=false;
	private boolean check;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		mp = new MediaPlayer();
        audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        
        currentVersionSupportBigNotification = UtilFunctions.currentVersionSupportBigNotification();
        currentVersionSupportLockScreenControls = UtilFunctions.currentVersionSupportLockScreenControls();
        timer = new Timer();
        mp.setOnCompletionListener(new OnCompletionListener() {
			@Override
			public void onCompletion(MediaPlayer mp) {
				Controls.nextControl(getApplicationContext());
			}
		});
		super.onCreate();
	}
	/**
	 * Send message from timer
	 * @author jonty.ankit
	 */
	private class MainTask extends TimerTask{ 
        public void run(){
            handler.sendEmptyMessage(0);
        }
    } 
	
	 @SuppressLint("HandlerLeak")
	 private final Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg){
        	if(mp != null && check){
				Song s= PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
				String time=s.getDuration();
				String [] splits=time.split(":");
				int maxD=Integer.valueOf(splits[1])+60*Integer.valueOf(splits[0]);
        		int progress = (int) ((mp.getCurrentPosition()*100) / (maxD*1000));
        		Integer i[] = new Integer[3];
				if (((int)mp.getCurrentPosition()/ 1000)==41&&!isAdd){
					MusicRef = database.getReference().child("Musics");
					s.incStream();
					MusicRef=MusicRef.child(s.getMusicId());
					MusicRef.setValue(s);
					isAdd=true;
				}
        		i[0] = mp.getCurrentPosition();
        		i[1] = maxD*1000;
        		i[2] = progress;
        		try{
					HomeActivity.updateBar(i);
					SongDetailActivity.updateBar(i);
        		}catch(Exception e){}
        	}
    	}
    };
    @SuppressLint("NewApi")
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		try {
			if (PlayerConstants.SONGS_LIST.size() > 0) {
				Song data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
				if (currentVersionSupportLockScreenControls) {
					RegisterRemoteClient();
				}
				String songPath = data.getMusicURL();
				playSong(songPath, data);
				newNotification();

				PlayerConstants.SONG_CHANGE_HANDLER = new Handler(new Callback() {
					@Override
					public boolean handleMessage(Message msg) {
						Song data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
						String songPath = data.getMusicURL();
						newNotification();
						isAdd=false;
						try {
							playSong(songPath, data);
							HomeActivity.changeUI();
							//SongDetailActivity.changeUI();
						} catch (Exception e) {
							e.printStackTrace();
						}
						return false;
					}
				});

				PlayerConstants.PLAY_PAUSE_HANDLER = new Handler(new Callback() {
					@Override
					public boolean handleMessage(Message msg) {
						String message = (String) msg.obj;
						if (mp == null)
							return false;
						if (message.equalsIgnoreCase(getResources().getString(R.string.play))) {
							PlayerConstants.SONG_PAUSED = false;
							if (currentVersionSupportLockScreenControls) {
								remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
							}
							mp.start();
						} else if (message.equalsIgnoreCase(getResources().getString(R.string.pause))) {
							PlayerConstants.SONG_PAUSED = true;
							if (currentVersionSupportLockScreenControls) {
								remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PAUSED);
							}
							mp.pause();
						}
						newNotification();
						try {
							HomeActivity.changeButton();
							SongDetailActivity.changeButton();
						} catch (Exception e) {
						}
						Log.d("TAG", "TAG Pressed: " + message);
						return false;
					}
				});

			}
			} catch(Exception e){
				e.printStackTrace();
			}
		return START_STICKY;
	}

	/**
	 * Notification
	 * Custom Bignotification is available from API 16
	 */
	@SuppressLint("NewApi")
	private void newNotification() {
		String songName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getName();
		RemoteViews simpleContentView = new RemoteViews(getApplicationContext().getPackageName(),R.layout.custom_notification);
		RemoteViews expandedView = new RemoteViews(getApplicationContext().getPackageName(), R.layout.big_notification);
		 
		final Notification notification = new NotificationCompat.Builder(getApplicationContext())
        .setSmallIcon(R.drawable.ic_music)
        .setContentTitle(songName).build();

		setListeners(simpleContentView);
		setListeners(expandedView);
		
		notification.contentView = simpleContentView;
		if(currentVersionSupportBigNotification){
			notification.bigContentView = expandedView;
		}
		
		try{
			UtilFunctions.DownloadImage d=new UtilFunctions.DownloadImage(){
				@Override
				protected void onPostExecute(Bitmap bitmap) {
					notification.contentView.setImageViewBitmap(R.id.imageViewAlbumArt, bitmap);
					if (currentVersionSupportBigNotification) {
						notification.bigContentView.setImageViewBitmap(R.id.imageViewAlbumArt, bitmap);
					}
					super.onPostExecute(bitmap);
					startNoti(notification);
				}
			};
			d.execute(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtURL());
		}catch(Exception e){
			e.printStackTrace();
		}
		if(PlayerConstants.SONG_PAUSED){
			notification.contentView.setViewVisibility(R.id.btnPause, View.GONE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.GONE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.VISIBLE);
			}
		}else{
			notification.contentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
			notification.contentView.setViewVisibility(R.id.btnPlay, View.GONE);

			if(currentVersionSupportBigNotification){
				notification.bigContentView.setViewVisibility(R.id.btnPause, View.VISIBLE);
				notification.bigContentView.setViewVisibility(R.id.btnPlay, View.GONE);
			}
		}

		notification.contentView.setTextViewText(R.id.textSongName, songName);
		final FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ArtistRef = database.getReference().child("Artists").child(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtist());;
		ArtistRef.addChildEventListener(new ChildEventListener() {

			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				Log.d("Message",dataSnapshot.getKey());
				if (dataSnapshot.getKey().toLowerCase().equals("name")){
					notification.contentView.setTextViewText(R.id.textAlbumArtist,(dataSnapshot.getValue(String.class)));
					if(currentVersionSupportBigNotification){
						notification.contentView.setTextViewText(R.id.textAlbumArtist, dataSnapshot.getValue(String.class));
					}
				}
			}

			@Override
			public void onChildChanged(DataSnapshot dataSnapshot, String s) {

			}

			@Override
			public void onChildRemoved(DataSnapshot dataSnapshot) {

			}

			@Override
			public void onChildMoved(DataSnapshot dataSnapshot, String s) {

			}

			@Override
			public void onCancelled(DatabaseError databaseError) {

			}
		});
		if(currentVersionSupportBigNotification){
			notification.bigContentView.setTextViewText(R.id.textSongName, songName);
		}
		notification.flags |= Notification.FLAG_ONGOING_EVENT;
		startForeground(NOTIFICATION_ID, notification);
	}
	public void  startNoti(Notification notification){
		startForeground(NOTIFICATION_ID, notification);
	}
	/**
	 * Notification click listeners
	 * @param view
	 */
	public void setListeners(RemoteViews view) {
		Intent previous = new Intent(NOTIFY_PREVIOUS);
		Intent delete = new Intent(NOTIFY_DELETE);
		Intent pause = new Intent(NOTIFY_PAUSE);
		Intent next = new Intent(NOTIFY_NEXT);
		Intent play = new Intent(NOTIFY_PLAY);
		
		PendingIntent pPrevious = PendingIntent.getBroadcast(getApplicationContext(), 0, previous, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPrevious, pPrevious);

		PendingIntent pDelete = PendingIntent.getBroadcast(getApplicationContext(), 0, delete, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnDelete, pDelete);
		
		PendingIntent pPause = PendingIntent.getBroadcast(getApplicationContext(), 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPause, pPause);
		
		PendingIntent pNext = PendingIntent.getBroadcast(getApplicationContext(), 0, next, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnNext, pNext);
		
		PendingIntent pPlay = PendingIntent.getBroadcast(getApplicationContext(), 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
		view.setOnClickPendingIntent(R.id.btnPlay, pPlay);

	}
	@Override
	public void onDestroy() {
		if(mp != null){
			mp.stop();
			mp = null;
		}
		super.onDestroy();
	}
	/**
	 * Play song, Update Lockscreen fields
	 * @param songPath
	 * @param data
	 */
	@SuppressLint("NewApi")
	private void playSong(String songPath, Song data) {
		try {
			check=false;
			if(currentVersionSupportLockScreenControls){
				UpdateMetadata(data);
				remoteControlClient.setPlaybackState(RemoteControlClient.PLAYSTATE_PLAYING);
			}
			mp.reset();
			mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mp.setDataSource(songPath);
			mp.prepare();
			mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
				@Override
				public void onPrepared(MediaPlayer mediaPlayer) {
					mp.start();
					check=true;
				}
			});
			timer.scheduleAtFixedRate(new MainTask(), 0, 100);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	@SuppressLint("NewApi")
	private void RegisterRemoteClient(){
		remoteComponentName = new ComponentName(getApplicationContext(), new NotificationBroadcast().ComponentName());
		 try {
		   if(remoteControlClient == null) {
			   audioManager.registerMediaButtonEventReceiver(remoteComponentName);
			   Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
			   mediaButtonIntent.setComponent(remoteComponentName);
			   PendingIntent mediaPendingIntent = PendingIntent.getBroadcast(this, 0, mediaButtonIntent, 0);
			   remoteControlClient = new RemoteControlClient(mediaPendingIntent);
			   audioManager.registerRemoteControlClient(remoteControlClient);
		   }
		   remoteControlClient.setTransportControlFlags(
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY |
				   RemoteControlClient.FLAG_KEY_MEDIA_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_PLAY_PAUSE |
				   RemoteControlClient.FLAG_KEY_MEDIA_STOP |
				   RemoteControlClient.FLAG_KEY_MEDIA_PREVIOUS |
				   RemoteControlClient.FLAG_KEY_MEDIA_NEXT);
	  }catch(Exception ex) {
	  }
	}
	@SuppressLint("NewApi")
	private void UpdateMetadata(Song data){
		if (remoteControlClient == null)
			return;
		MetadataEditor metadataEditor = remoteControlClient.editMetadata(true);
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_ARTIST, data.getArtist());
		metadataEditor.putString(MediaMetadataRetriever.METADATA_KEY_TITLE, data.getName());
		//mDummyAlbumArt = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArt();
		if(mDummyAlbumArt == null){
			mDummyAlbumArt = BitmapFactory.decodeResource(getResources(), R.drawable.default_album_art);
		}
		metadataEditor.putBitmap(MetadataEditor.BITMAP_KEY_ARTWORK, mDummyAlbumArt);
		metadataEditor.apply();
		audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
	}

	@Override
	public void onAudioFocusChange(int focusChange) {}
}