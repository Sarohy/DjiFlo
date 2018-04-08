package com.fiv.djiflo.djiflo.View.Adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.fiv.djiflo.djiflo.DataLayer.Song;
import com.fiv.djiflo.djiflo.R;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by apple on 16/11/2017.
 */

public class SongPlayingAdapter extends BaseAdapter implements Filterable {
    Context context;
    ArrayList<Song> songs;
    private static LayoutInflater inflater=null;
    private ArrayList<Song> filterList;
    private SFilter filter;

    public SongPlayingAdapter(Context context, ArrayList<Song> songs) {
        this.context=context;
        this.songs=songs;
        this.filterList=songs;
        inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getCount() {
        return filterList.size();
    }

    @Override
    public Object getItem(int i) {
        return filterList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        View vi=view;
        if(vi==null)
            vi = inflater.inflate(R.layout.listview_song_list, null);

        TextView title = (TextView)vi.findViewById(R.id.tv_song_name); // title
        final TextView artist = (TextView)vi.findViewById(R.id.tv_song_artist); // artist name
        TextView duration = (TextView)vi.findViewById(R.id.tv_song_duration); // duration
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.imgAlbumArt); // thumb image

        title.setText(filterList.get(i).getName());
        duration.setText(filterList.get(i).getDuration());
        Picasso.with(context).load(filterList.get(i).getArtURL()).into(thumb_image);
        final FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference ArtistRef = database.getReference().child("Artists").child(filterList.get(i).getArtist());;
        ArtistRef.addChildEventListener(new ChildEventListener() {

            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Log.d("Message",dataSnapshot.getKey());
                if (dataSnapshot.getKey().toLowerCase().equals("name")){
                    artist.setText(dataSnapshot.getValue(String.class));
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
        return vi;
    }
    @Override
    public Filter getFilter()
    {
        if(filter == null){
        filter = new SFilter(); }
        return filter;
    }
    private class SFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0){
                constraint=constraint.toString().toLowerCase();
                ArrayList<Song> filteredList = new ArrayList<Song>();
                for(int i=0; i < songs.size(); i++){
                    if(songs.get(i).getName().toLowerCase().contains(constraint)){
                        filteredList.add(songs.get(i));
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            } else{
                results.count = songs.size();
                results.values = songs;
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) { filterList = (ArrayList<Song>) results.values;
            notifyDataSetChanged();
        }
    }
}
