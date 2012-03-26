package com.teamagly.friendizer;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public abstract class FriendsAdapter extends ArrayAdapter<FBUserInfo> {

    protected static LayoutInflater inflater = null;
    public ImageLoader imageLoader;

    public FriendsAdapter(Context context, int textViewResourceId, List<FBUserInfo> objects) {
	super(context, textViewResourceId, objects);
	inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	imageLoader = new ImageLoader(context);
    }

    public abstract View getView(int position, View convertView, ViewGroup parent);
    // View vi=convertView;
    // if(convertView==null)
    // vi = inflater.inflate(R.layout.item, null);
    //
    // TextView text=(TextView)vi.findViewById(R.id.text);;
    // ImageView image=(ImageView)vi.findViewById(R.id.image);
    // text.setText("item "+position);
    // imageLoader.DisplayImage(data[position], image);
    // return vi;
}