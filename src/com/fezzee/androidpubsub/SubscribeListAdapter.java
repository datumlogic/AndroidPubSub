package com.fezzee.androidpubsub;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;



public class SubscribeListAdapter extends ArrayAdapter<PubSubNodeItem> {
 
    Context context;
 
    public SubscribeListAdapter(Context context, int resourceId,
            List<PubSubNodeItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }
 
    /*private view holder class*/
    private class ViewHolder {
        TextView txtNodeName;
        ImageView imgSubState;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final PubSubNodeItem rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_subscribe, null);
            holder = new ViewHolder();
            
            holder.txtNodeName = (TextView) convertView.findViewById(R.id.nodename);
            holder.imgSubState = (ImageView) convertView.findViewById(R.id.subscribed);

            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 
        holder.txtNodeName.setText(rowItem.getNodeName());
        holder.imgSubState.setImageResource(rowItem.getSubscription());

        return convertView;
    }
}
