package com.fiv.djiflo.djiflo.View.Activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.ErrorCodes;
import com.firebase.ui.auth.IdpResponse;
import com.fiv.djiflo.djiflo.DataLayer.PlayList;
import com.fiv.djiflo.djiflo.DataLayer.Song;
import com.fiv.djiflo.djiflo.DataLayer.User;
import com.fiv.djiflo.djiflo.R;
import com.fiv.djiflo.djiflo.View.Fragments.NotificationFragment;
import com.fiv.djiflo.djiflo.View.Fragments.PlaylistFragment;
import com.fiv.djiflo.djiflo.View.Fragments.SearchFragment;
import com.fiv.djiflo.djiflo.View.Fragments.SongFragment;
import com.fiv.djiflo.djiflo.controls.Controls;
import com.fiv.djiflo.djiflo.util.PlayerConstants;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;
import com.wang.avi.AVLoadingIndicatorView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity implements SearchFragment.SearchFragmentInterface {

    FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference UserRef = database.getReference().child("Users");

    static TextView playingSong;
    static Button btnPause, btnPlay;
    static LinearLayout linearLayoutPlayingSong;
    public static DiscreteSeekBar progressBar;
    static RoundedImageView imageViewAlbumArt;
    static Context context;
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    private String userId;
    static AVLoadingIndicatorView loaderPlaySong;
    SearchFragment searchFragment;
    private View mRootView;
    private static final int RC_SIGN_IN = 100;
    private  boolean check=false;

    @Override
    protected void onPause() {
        check=true;
        super.onPause();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setIcon(R.mipmap.ic_launcher);
        mRootView=findViewById(R.id.root);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        auth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user!=null){
                    sharedPreferences=getSharedPreferences("User",MODE_PRIVATE);
                    if (sharedPreferences.getString("UserId",null)==null) {
                        editor = sharedPreferences.edit();
                        User u = new User();
                        u.setId(user.getUid());
                        if (user.getEmail() != null)
                            u.setEmailId(user.getEmail());
                        else
                            u.setEmailId("");

                        if (user.getDisplayName() != null)
                            u.setName(user.getDisplayName());
                        else
                            u.setName("User");
                        if (user.getPhoneNumber() != null)
                            u.setPhone(user.getPhoneNumber());
                        else
                            u.setPhone("");
                        if (user.getPhotoUrl() != null)
                            u.setImageURL("http://" + user.getPhotoUrl().getAuthority() + user.getPhotoUrl().getPath());
                        UserRef.child(user.getUid()).setValue(u);
                        editor.putString("UserId", user.getUid());
                        editor.apply();
                        accountDetailSave();
                    }
                }
            }
        });
        if (auth.getCurrentUser() != null) {
            // already signed in
            accountDetailSave();
            getViews();
            setListeners();
            setFragment(new SongFragment());
        } else {
            // not signed in
            startActivityForResult(AuthUI.getInstance().createSignInIntentBuilder().setTheme(R.style.AppTheme).setIsSmartLockEnabled(false).setLogo(R.mipmap.ic_launcher).setAvailableProviders(getSelectedProviders())
                    .build(),RC_SIGN_IN);
        }
    }

//    @Override
//    protected void onResume() {
//        if (check)
//            setFragment(new SongFragment());
//        super.onResume();
//    }

    protected void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment, fragment);
        fragmentTransaction.addToBackStack("Fragment");
        fragmentTransaction.commit();
    }
    private List<AuthUI.IdpConfig> getSelectedProviders() {
        List<AuthUI.IdpConfig> selectedProviders = new ArrayList<>();
        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.GOOGLE_PROVIDER)
                        .setPermissions(getGooglePermissions())
                        .build());
        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.FACEBOOK_PROVIDER)
                        .build());
        selectedProviders.add(new AuthUI.IdpConfig.Builder(AuthUI.EMAIL_PROVIDER).build());
        selectedProviders.add(
                new AuthUI.IdpConfig.Builder(AuthUI.PHONE_VERIFICATION_PROVIDER).build());
        return selectedProviders;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            Toast.makeText(getApplicationContext(),"Hi",Toast.LENGTH_LONG).show();
            handleSignInResponse(resultCode, data);
            getViews();
            setListeners();
            setFragment(new SongFragment());
            return;
        }

        //showSnackbar(R.string.unknown_response);
    }
    @MainThread
    private List<String> getFacebookPermissions() {
        List<String> result = new ArrayList<>();
            result.add("user_friends");
            result.add("user_photos");
        return result;
    }

    @MainThread
    private List<String> getGooglePermissions() {
        List<String> result = new ArrayList<>();
        result.add("https://www.googleapis.com/auth/youtube.readonly");
        result.add(Scopes.DRIVE_FILE);
        return result;
    }
    @MainThread
    private void showSnackbar(@StringRes int errorMessageRes) {
        Snackbar.make(mRootView, errorMessageRes, Snackbar.LENGTH_LONG).show();
    }
    @MainThread
    private void handleSignInResponse(int resultCode, Intent data) {
        IdpResponse response = IdpResponse.fromResultIntent(data);

        // Successfully signed in
        if (resultCode == RESULT_OK) {
            startSignedInActivity(response);
            //finish();
            return;
        } else {
            // Sign in failed
            if (response == null) {
                // User pressed back button
                showSnackbar(R.string.sign_in_cancelled);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection);
                return;
            }

            if (response.getErrorCode() == ErrorCodes.UNKNOWN_ERROR) {
                showSnackbar(R.string.unknown_error);
                return;
            }
        }

        showSnackbar(R.string.unknown_sign_in_response);
    }
    private void startSignedInActivity(IdpResponse response) {
        setFragment(new SongFragment());
   }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_home_menu, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        final SearchView searchView = (SearchView) MenuItemCompat.getActionView(item);
        searchView.setOnSearchClickListener(new SearchView.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchFragment=new SearchFragment(HomeActivity.this);
                setFragment(searchFragment);
            }
        });
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                searchFragment.filter(newText);
                return false;
            }
        });
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_noti) {
            setFragment(new NotificationFragment());
            return true;
        }
        if (id == R.id.action_profile) {
            startActivity(new Intent(getApplication(),ProfileActivity.class));
            return true;
        }
        if (id == R.id.action_logout){
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                            finish();
                        }
                    });
        }

        return super.onOptionsItemSelected(item);
    }



    private void accountDetailSave() {
        sharedPreferences=getSharedPreferences("User",MODE_PRIVATE);
        userId=sharedPreferences.getString("UserId",null);
    }




    public static void updateUI() {
        try{
            Song data = PlayerConstants.SONGS_LIST.get(PlayerConstants.SONG_NUMBER);
            playingSong.setText(data.getName());
            Picasso.with(context).load(data.getArtURL()).into(imageViewAlbumArt);
            linearLayoutPlayingSong.setVisibility(View.VISIBLE);
        }catch(Exception e){}
    }
    public static void changeButton() {
        if(PlayerConstants.SONG_PAUSED){
            btnPause.setVisibility(View.GONE);
            btnPlay.setVisibility(View.VISIBLE);
        }
        else{
            btnPause.setVisibility(View.VISIBLE);
            btnPlay.setVisibility(View.GONE);
        }
    }
    public static void changeUI(){
        updateUI();
        changeButton();
    }
    public static void playingRequest(){
        btnPause.setVisibility(View.GONE);
        btnPlay.setVisibility(View.GONE);
        loaderPlaySong.setVisibility(View.VISIBLE);
        loaderPlaySong.show();
    }
    private void getViews() {
        playingSong = (TextView) findViewById(R.id.textNowPlaying);
        btnPause = (Button) findViewById(R.id.btnPause);
        btnPlay = (Button) findViewById(R.id.btnPlay);
        linearLayoutPlayingSong = (LinearLayout) findViewById(R.id.linearLayoutPlayingSong);
        progressBar = (DiscreteSeekBar) findViewById(R.id.progressBar);
        imageViewAlbumArt = (RoundedImageView) findViewById(R.id.imageViewAlbumArt);
        loaderPlaySong= (AVLoadingIndicatorView) findViewById(R.id.loaderPlaySong);

    }
    public static void updateBar(Integer []i){
        progressBar.setProgress(i[2]);
        loaderPlaySong.setVisibility(View.GONE);
        btnPause.setVisibility(View.VISIBLE);

    }
    private void setListeners() {
        btnPlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controls.playControl(getApplicationContext());
            }
        });
        btnPause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Controls.pauseControl(getApplicationContext());
            }
        });
        linearLayoutPlayingSong.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplication(), SongDetailActivity.class);
                startActivity(i);
            }
        });
        imageViewAlbumArt.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplication(), SongDetailActivity.class);
                startActivity(i);
            }
        });
        progressBar.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });
    }

    @Override
    public void selectedPlaylist(PlayList p) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction =
                fragmentManager.beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putSerializable("playlist", p);
        PlaylistFragment playlistFragment = new PlaylistFragment();
        playlistFragment.setArguments(bundle);
        fragmentTransaction.add(R.id.fragment, playlistFragment);
        fragmentTransaction.addToBackStack("fragment");
        fragmentTransaction.commit();
    }
}
