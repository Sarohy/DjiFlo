package com.fiv.djiflo.djiflo.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import com.fiv.djiflo.djiflo.DataLayer.PlayList;
import com.fiv.djiflo.djiflo.R;
import com.makeramen.roundedimageview.RoundedImageView;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by apple on 16/11/2017.
 */

public class PlayListAdapter extends BaseAdapter implements Filterable {
    Context context;
    ArrayList<PlayList> playLists;
    private static LayoutInflater inflater=null;
    private ArrayList<PlayList> filterList;
    private PFilter filter;

    public PlayListAdapter(Context context, ArrayList<PlayList> songs) {
        this.context=context;
        this.playLists=songs;
        this.filterList=playLists;
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
            vi = inflater.inflate(R.layout.listview_playlist_list, null);

        TextView title = (TextView)vi.findViewById(R.id.tv_playlist_name); // title
        RoundedImageView thumb_image=(RoundedImageView) vi.findViewById(R.id.iv_playlist); // thumb image

        title.setText(filterList.get(i).getName());
        Picasso.with(context).load(filterList.get(i).getImageURL()).into(thumb_image);
        return vi;
    }
    @Override
    public Filter getFilter()
    {
        if(filter == null){
            filter = new PFilter(); }
        return filter;
    }
    private class PFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0){
                constraint=constraint.toString().toLowerCase();
                ArrayList<PlayList> filteredList = new ArrayList<PlayList>();
                for(int i=0; i < playLists.size(); i++){
                    if(playLists.get(i).getName().toLowerCase().contains(constraint)){
                        filteredList.add(playLists.get(i));
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            } else{
                results.count = playLists.size();
                results.values = playLists;
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) { filterList = (ArrayList<PlayList>) results.values;
            notifyDataSetChanged();
        }
    }
}
