package com.fiv.djiflo.djiflo.View.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;

import com.fiv.djiflo.djiflo.DataLayer.User;
import com.fiv.djiflo.djiflo.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by apple on 16/11/2017.
 */

public class UserAdapter extends BaseAdapter implements Filterable {
    Context context;
    ArrayList<User> users;
    private static LayoutInflater inflater=null;
    private ArrayList<User> filterList;
    private UFilter filter;

    public UserAdapter(Context context, ArrayList<User> songs) {
        this.context=context;
        this.users=songs;
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
            vi = inflater.inflate(R.layout.listview_user_list, null);

        TextView title = (TextView)vi.findViewById(R.id.tv_user_name); // title
        ImageView thumb_image=(ImageView)vi.findViewById(R.id.iv_user_photo); // thumb image


        title.setText(filterList.get(i).getName());
        Picasso.with(context).load(filterList.get(i).getImageURL()).placeholder(R.drawable.ic_person_white_36dp).into(thumb_image);
        return vi;
    }
    @Override
    public Filter getFilter()
    {
        if(filter == null){
            filter = new UFilter(); }
        return filter;
    }
    private class UFilter extends Filter {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) { FilterResults results = new FilterResults();
            if(constraint != null && constraint.length() > 0){
                constraint=constraint.toString().toLowerCase();
                ArrayList<User> filteredList = new ArrayList<User>();
                for(int i=0; i < users.size(); i++){
                    if(users.get(i).getName().toLowerCase().contains(constraint)){
                        filteredList.add(users.get(i));
                    }
                }
                results.count = filteredList.size();
                results.values = filteredList;
            } else{
                results.count = users.size();
                results.values = users;
            }
            return results;
        }
        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) { filterList = (ArrayList<User>) results.values;
            notifyDataSetChanged();
        }
    }

}
