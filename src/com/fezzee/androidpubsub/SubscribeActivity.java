package com.fezzee.androidpubsub;

import java.util.ArrayList;
import java.util.Iterator;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubManager;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

public class SubscribeActivity extends Activity {
	
	protected static XMPPConnection connection;

	private Handler mHandler = new Handler();
	
    private ListView listview;
    
    private ArrayList<PubSubNodeItem> listdata = new ArrayList<PubSubNodeItem>();
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_subscribe);
		
		listview = (ListView) this.findViewById(R.id.subscribeview);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		     public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
		                             long id) {
		    	 //TODO
		    	 Log.d("SubscribeActivity::listview.onItemClick","CLICKED!");
		    	 if (listdata.get(position).getSubscription()==R.drawable.unsubscribed)
		    	 {
		    		 listdata.get(position).setSubscription(R.drawable.subscribed);
		    	 } else {
		    		 listdata.get(position).setSubscription(R.drawable.unsubscribed);
		    	 }
		    	 
		    	 final ProgressDialog dialog2 = ProgressDialog.show(SubscribeActivity.this, "Connecting...", "Please wait...", false);
			     Thread t = new Thread(new Runnable() {
			      @Override
			      public void run() {
			      
			       // Create a connection
			       ConnectionConfiguration connConfig = new ConnectionConfiguration(Constants.HOST, Constants.PORT, Constants.SERVICE);
			       connection = new XMPPConnection(connConfig);
			         try {
			           connection.connect();
			           Log.d("SubscribeActivity::onCreate",  "[SettingsDialog] Connected to "+connection.getHost());
			        
			           connection.login(Constants.USERNAME, Constants.PASSWORD, Constants.RESOURCE);
			           Log.d("SubscribeActivity::onCreate",  "Logged in as " + connection.getUser());
			           
			           // Create a pubsub manager using an existing Connection
			           final PubSubManager mgr = new PubSubManager(connection,"pubsub." + Constants.HOST);
		    	 
		    	       setListAdapter(mgr);
		    	       
			         } catch (ClassCastException cce) {
			                Log.e("SubscribeActivity::onCreate", "Did you register Smack's XMPP Providers and Extensions in advance? - " +
			                		"SmackAndroid.init(this)?\n" + cce.getMessage());
			                cce.printStackTrace();
			                connection = null;
			         } catch (XMPPException ex) {
			                Log.e("SubscribeActivity::onCreate", "XMPPException for '"+  Constants.USERNAME + "'");
			                ex.printStackTrace();
			                connection = null;    
			         } catch (Exception e) {
			              //all other exceptions
			        	   Log.e("SubscribeActivity::onCreate", "Unhandled Exception"+  e.getMessage()); 
			        	   e.printStackTrace();
			        	   connection = null;
			         }
			         dialog2.dismiss();
			      }
			   }); // end of thread

			    t.start();
			    dialog2.show();
		     }
		});
		
		final ProgressDialog dialog2 = ProgressDialog.show(SubscribeActivity.this, "Connecting...", "Please wait...", false);
	     Thread t = new Thread(new Runnable() {
	      @Override
	      public void run() {
	      
	       // Create a connection
	       ConnectionConfiguration connConfig = new ConnectionConfiguration(Constants.HOST, Constants.PORT, Constants.SERVICE);
	       connection = new XMPPConnection(connConfig);
	         try {
	           connection.connect();
	           Log.d("SubscribeActivity::onCreate",  "[SettingsDialog] Connected to "+connection.getHost());
	        
	           connection.login(Constants.USERNAME, Constants.PASSWORD, Constants.RESOURCE);
	           Log.d("SubscribeActivity::onCreate",  "Logged in as " + connection.getUser());
	           
	           // Create a pubsub manager using an existing Connection
	           final PubSubManager mgr = new PubSubManager(connection,"pubsub." + Constants.HOST);
	           
	           DiscoverItems nodes = mgr.discoverNodes(null);
	           for (Iterator<DiscoverItems.Item> items = nodes.getItems(); items.hasNext();) {
	   			LeafNode node = mgr.getNode(items.next().getNode());
	   			//int count = node.getItems().size();
	   			final String name = node.getId();
	   			Log.d("SubscribeActivity::onCreate", "Node '" + name); // + "' Count: " + count
	   			PubSubNodeItem nodeItem = new PubSubNodeItem(name);
	   			nodeItem.setSubscription(R.drawable.unsubscribed);
	   			listdata.add(nodeItem);
	           }
	           
	           setListAdapter(mgr);
	           

	         } catch (ClassCastException cce) {
	                Log.e("SubscribeActivity::onCreate", "Did you register Smack's XMPP Providers and Extensions in advance? - " +
	                		"SmackAndroid.init(this)?\n" + cce.getMessage());
	                cce.printStackTrace();
	                connection = null;
	         } catch (XMPPException ex) {
	                Log.e("SubscribeActivity::onCreate", "XMPPException for '"+  Constants.USERNAME + "'");
	                ex.printStackTrace();
	                connection = null;    
	         } catch (Exception e) {
	              //all other exceptions
	        	   Log.e("SubscribeActivity::onCreate", "Unhandled Exception"+  e.getMessage()); 
	        	   e.printStackTrace();
	        	   connection = null;
	         }
	         dialog2.dismiss();
	      }
	   }); // end of thread

	    t.start();
	    dialog2.show();

	}
		
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.subscribe, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.action_publish:
	    		finish();
	    		startActivity(new Intent(getBaseContext(), PublishActivity.class));
	    		return true;
	    	case R.id.action_feed:
	    		finish();
	    		startActivity(new Intent(getBaseContext(), FeedActivity.class));
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void setListAdapter(PubSubManager mgr)
	{
		// This is the array adapter, it takes the context of the activity as a 
        // first parameter, the type of list view as a second parameter and your 
        // array as a third parameter.
        //final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
        //        this, 
        //        android.R.layout.simple_list_item_1,
        //        listdata );
        
		Log.d("SubscribeActivity::setListAdapter","here 1....");
        final SubscribeListAdapter adapter = new SubscribeListAdapter(
        		this, R.layout.listitem_subscribe, listdata);
        Log.d("SubscribeActivity::setListAdapter","here 2....");
        // Add the incoming message to the view
        mHandler.post(new Runnable() {
          public void run() {
        	  listview.setAdapter(adapter);
        	  
          }
        });
         
	}	
}
