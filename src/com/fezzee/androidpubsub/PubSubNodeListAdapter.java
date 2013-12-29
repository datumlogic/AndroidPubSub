package com.fezzee.androidpubsub;

import java.util.List;

import org.jivesoftware.smack.packet.Presence;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;



public class PubSubNodeListAdapter extends ArrayAdapter<PubSubNodeItem> {
 
    Context context;
 
    public PubSubNodeListAdapter(Context context, int resourceId,
            List<PubSubNodeItem> items) {
        super(context, resourceId, items);
        this.context = context;
    }
 
    /*private view holder class*/
    private class ViewHolder {
        TextView txtNodeName;
        TextView txtItemCount;
    }
 
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        final PubSubNodeItem rowItem = getItem(position);
 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.listitem_publish, null);
            holder = new ViewHolder();
            
            holder.txtNodeName = (TextView) convertView.findViewById(R.id.nodename);
            holder.txtItemCount = (TextView) convertView.findViewById(R.id.itemcount);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
 

        holder.txtItemCount.setText("" + rowItem.getItemCount());
        holder.txtNodeName.setText(rowItem.getNodeName());

        return convertView;
    }
}
