package com.fiv.djiflo.djiflo.View.Activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.GradientDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.fiv.djiflo.djiflo.DataLayer.Song;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.View.Adapter.SongPlayingAdapter;
import com.fiv.djiflo.djiflo.controls.Controls;
import com.fiv.djiflo.djiflo.service.SongService;
import com.fiv.djiflo.djiflo.util.PlayerConstants;
import com.fiv.djiflo.djiflo.util.UtilFunctions;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class SongDetailActivity extends AppCompatActivity {

	Button btnBack;
	static Button btnPause;
	Button btnNext;
	static Button btnPlay;
	static TextView textNowPlaying;
	static TextView textAlbumArtist;
	static LinearLayout linearLayoutPlayer;
	static ProgressBar progressBar;
	static Context context;
	static ImageView ivAlbumArt;
	static TextView textBufferDuration, textDuration;
	static ListView lvPlayingQue;
	Button btnLike;
	Button btnComment;
	SharedPreferences spLikes;
	SharedPreferences.Editor spEditorLikes;
	SharedPreferences spUser;

	FirebaseDatabase database = FirebaseDatabase.getInstance();
	DatabaseReference LikesRef = database.getReference();
	private boolean check=false;
	private static ImageView rootView;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_song_detail);
		context = this;
		init();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.activity_detail_song_menu, menu);
		MenuItem item = menu.findItem(R.id.action_search);
		return true;
	}


	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_dedicate) {
			startActivity(new Intent(getApplicationContext(),DedicateActivity.class));
			return true;
		}
		if (id == R.id.action_artist) {
			startActivity(new Intent(getApplicationContext(),SongArtistActivity.class));
			return true;
		}
		if (id == R.id.action_share){
			Intent sharingIntent = new Intent(Intent.ACTION_SEND);
			sharingIntent.setType("text/plain");
			String shareBody = "Hi! Listen "+ PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getName()+" song at "+String.valueOf(R.string.app_name)+" http://play.google.com/store/apps/details?id="+getPackageName();
			sharingIntent.putExtra(Intent.EXTRA_SUBJECT, PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getName());
			sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
			startActivity(Intent.createChooser(sharingIntent, "Share via"));
		}

		return super.onOptionsItemSelected(item);
	}
	@SuppressLint("HandlerLeak")
	private void init() {
		getViews();
		setListeners();
		progressBar.getProgressDrawable().setColorFilter(getResources().getColor(R.color.white), Mode.SRC_IN);
		PlayerConstants.PROGRESSBAR_HANDLER = new Handler(){
			 @Override
		        public void handleMessage(Message msg){
				 Integer i[] = (Integer[])msg.obj;
				 textBufferDuration.setText(UtilFunctions.getDuration(i[0]));
				 textDuration.setText(UtilFunctions.getDuration(i[1]));
				 progressBar.setProgress(i[2]);
		    	}
		};
		ArrayList<String> playingQue=new ArrayList<>();
		ArrayList<Song> songs;
		songs = PlayerConstants.SONGS_LIST;
		for (int i=0;i<songs.size();i++)
			playingQue.add(songs.get(i).getName());
		lvPlayingQue.setAdapter(new SongPlayingAdapter(this,songs));
		final String albumId = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtURL();
		new DownloadImage().execute(albumId);
		final String musicId=PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicId();
		spUser=getSharedPreferences("User",MODE_PRIVATE);
		String userId=spUser.getString("UserId",null);
		LikesRef=database.getReference().child("UserLikes");
		LikesRef=LikesRef.getRef().child(userId);
		LikesRef.addChildEventListener(new ChildEventListener() {
			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				if (dataSnapshot.getKey().equals(musicId)){
					Button button = findViewById(R.id.btnLike);
					button.setBackgroundResource(R.drawable.red_heart);
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
		textNowPlaying.setText(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getName());
		final FirebaseDatabase database = FirebaseDatabase.getInstance();
		DatabaseReference ArtistRef = database.getReference().child("Artists").child(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtist());;
		ArtistRef.addChildEventListener(new ChildEventListener() {

			@Override
			public void onChildAdded(DataSnapshot dataSnapshot, String s) {
				Log.d("Message",dataSnapshot.getKey());
				if (dataSnapshot.getKey().toLowerCase().equals("name")){
					textAlbumArtist.setText(dataSnapshot.getValue(String.class));
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
	}
	@SuppressLint("StaticFieldLeak")
	public static class DownloadImage extends AsyncTask<String,Void,Bitmap>{

		private String src;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Bitmap bitmap) {
			super.onPostExecute(bitmap);
			ivAlbumArt.setImageBitmap(bitmap);
			Bitmap blurredBitmap = BlurBuilder.blur( context, bitmap );
			rootView.setImageBitmap(blurredBitmap);
		}

		@Override
		protected Bitmap doInBackground(String... strings) {
			src = strings[0];
			try {
				URL url = new URL(src);
				Log.d("Download",src);
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
	private void setListeners() {
		btnBack.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.previousControl(getApplicationContext());
			}
		});
		btnPause.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.pauseControl(getApplicationContext());
			}
		});
		btnPlay.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Controls.playControl(getApplicationContext());
			}
		});
		btnNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Controls.nextControl(getApplicationContext());
			}
		});
		btnLike.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				spLikes=getSharedPreferences("UserLikes",MODE_PRIVATE);
				spUser=getSharedPreferences("User",MODE_PRIVATE);
				String userId=spUser.getString("UserId",null);
				LikesRef=database.getReference().child("UserLikes");
				LikesRef=LikesRef.getRef().child(userId);
				spEditorLikes=spLikes.edit();
				String musicId=PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicId();
				if (!spLikes.getBoolean(musicId,false)){
					if (userId!=null){
						LikesRef=LikesRef.getRef().child(musicId);
						LikesRef.setValue(true);
						spEditorLikes.putBoolean(musicId,true);
						spEditorLikes.apply();
						Button button=findViewById(R.id.btnLike);
						button.setBackgroundResource(R.drawable.red_heart);
					}
				}
				else {
					if (userId!=null){
						LikesRef=LikesRef.getRef().child(musicId);
						LikesRef.removeValue();
						spEditorLikes.putBoolean(musicId,false);
						spEditorLikes.apply();
						Button button=findViewById(R.id.btnLike);
						button.setBackgroundResource(R.drawable.white_heart);
					}
				}
			}
		});
		btnComment.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				Intent i=new Intent(getApplicationContext(),CommentActivity.class);
				i.putExtra("MusicId",PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getMusicId());
				startActivity(i);
			}
		});
		int[] colors = {0, 0xFFFFFFFF, 0}; // white for the example
		lvPlayingQue.setDivider(new GradientDrawable(GradientDrawable.Orientation.RIGHT_LEFT, colors));
		lvPlayingQue.setDividerHeight(1);
	}
	public static void changeUI(){

		updateUI();
		changeButton();
	}
	private void getViews() {
		btnBack = (Button) findViewById(R.id.btnBack);
		btnPause = (Button) findViewById(R.id.btnPause);
		btnNext = (Button) findViewById(R.id.btnNext);
		btnPlay = (Button) findViewById(R.id.btnPlay);
		textNowPlaying = (TextView) findViewById(R.id.textNowPlaying);
		linearLayoutPlayer = (LinearLayout) findViewById(R.id.linearLayoutPlayer);
		textAlbumArtist = (TextView) findViewById(R.id.textAlbumArtist);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		textBufferDuration = (TextView) findViewById(R.id.textBufferDuration);
		textDuration = (TextView) findViewById(R.id.textDuration);
		textNowPlaying.setSelected(true);
		textAlbumArtist.setSelected(true);
		ivAlbumArt= (ImageView) findViewById(R.id.imgAlbumArt);
		lvPlayingQue= (ListView) findViewById(R.id.lvPlayingQue);
		btnLike= (Button) findViewById(R.id.btnLike);
		btnComment= (Button) findViewById(R.id.btnComment);
		rootView=findViewById(R.id.ll_back);
	}
	@Override
	protected void onResume() {
		super.onResume();
		boolean isServiceRunning = UtilFunctions.isServiceRunning(SongService.class.getName(), getApplicationContext());
		if (isServiceRunning&&check) {
			updateUI();
			check=false;
		}
		changeButton();
	}
	@Override
	protected void onPause() {
		super.onPause();
		check=true;
	}
	public static void changeButton() {
		if(PlayerConstants.SONG_PAUSED){
			btnPause.setVisibility(View.GONE);
			btnPlay.setVisibility(View.VISIBLE);
		}else{
			btnPause.setVisibility(View.VISIBLE);
			btnPlay.setVisibility(View.GONE);
		}
	}
	private static void updateUI() {
		try{
			final String albumId = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtURL();
			new DownloadImage().execute(albumId);

			final FirebaseDatabase database = FirebaseDatabase.getInstance();
			DatabaseReference ArtistRef = database.getReference().child("Artists").child(PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getArtist());;
			ArtistRef.addChildEventListener(new ChildEventListener() {

				@Override
				public void onChildAdded(DataSnapshot dataSnapshot, String s) {
					Log.d("Message",dataSnapshot.getKey());
					if (dataSnapshot.getKey().toLowerCase().equals("name")){
						textAlbumArtist.setText(dataSnapshot.getValue(String.class));
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
			String songName = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER).getName();
			textNowPlaying.setText(songName);
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	public static void updateBar(Integer []i){
		progressBar.setProgress(i[2]);
		int sec=i[0] / 1000;
		int min=sec/60;
		sec=sec%60;
		String str=String.format("%02d:%02d",min,sec);
		textBufferDuration.setText(str);
		sec=i[1] / 1000;
		min=sec/60;
		sec=sec%60;
		str=String.format("%02d:%02d",min,sec);
		textDuration.setText(str);
	}
	public static class BlurBuilder {
		private static final float BITMAP_SCALE = 0.4f;
		private static final float BLUR_RADIUS = 7.5f;

		public static Bitmap blur(Context context, Bitmap image) {
			int width = Math.round(image.getWidth() * BITMAP_SCALE);
			int height = Math.round(image.getHeight() * BITMAP_SCALE);

			Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
			Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

			RenderScript rs = RenderScript.create(context);
			ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
			Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
			Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
			theIntrinsic.setRadius(BLUR_RADIUS);
			theIntrinsic.setInput(tmpIn);
			theIntrinsic.forEach(tmpOut);
			tmpOut.copyTo(outputBitmap);

			return outputBitmap;
		}
	}
}
