package com.fezzee.androidpubsub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.Node;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
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
		    	 
		    	 final int fPosition = position;
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
		    	 
			           
				    	 Log.d("SubscribeActivity::listview.onItemClick","CLICKED!");
				    	 if (listdata.get(fPosition).getSubscription()==R.drawable.unsubscribed)
				    	 {
				    		 PubSubNodeItem psnode = listdata.get(fPosition);
				    		 psnode.setSubscription(R.drawable.subscribed);
				    		 Node node = mgr.getNode(psnode.getNodeName()) ;    
				    		 node.subscribe(Constants.USERNAME+"@"+Constants.HOST);      
				    		
				    	 } else {
				    		 PubSubNodeItem psnode = listdata.get(fPosition);
				    		 psnode.setSubscription(R.drawable.unsubscribed);
				    		 Node node = mgr.getNode(psnode.getNodeName()) ;    
				    		 node.unsubscribe(Constants.USERNAME+"@"+Constants.HOST);
				    	 }
		    	 
			           
		    	       setListAdapter(mgr);
		    	       
			         } catch (ClassCastException cce) {
			                Log.e("PublishActivity::onCreate", "Did you register Smack's XMPP Providers and Extensions in advance? - " +
			                		"SmackAndroid.init(this)?\n" + cce.getMessage());
			                cce.printStackTrace();
		                    showToast("[Class Cast Exception] " + cce.getMessage());
			                connection = null;
			         } catch (XMPPException ex) {
			                Log.e("PublishActivity::onCreate", "XMPPException for '"+  Constants.USERNAME + "'");
			                ex.printStackTrace();
			                
			                showToast("[XMPP Exception] " + ex.getMessage());
			                connection = null;    
			         } catch (Exception e) {
			              //all other exceptions
			        	   Log.e("PublishActivity::onCreate", "Unhandled Exception"+  e.getMessage()); 
			        	   e.printStackTrace();
			        	   showToast("[Unhandled Exception] " + e.getMessage());
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
	           Log.d("SubscribeActivity::onCreate",  "[TIMEOUT] - " + SmackConfiguration.getPacketReplyTimeout());
	        	 
	           connection.connect();
	           Log.d("SubscribeActivity::onCreate",  "Connected to "+connection.getHost());
	        
	           connection.login(Constants.USERNAME, Constants.PASSWORD, Constants.RESOURCE);
	           Log.d("SubscribeActivity::onCreate",  "Logged in as " + connection.getUser());
	           
	           // Create a pubsub manager using an existing Connection
	           final PubSubManager mgr = new PubSubManager(connection,"pubsub." + Constants.HOST);
	           
	           
	           //first get all nodes
	           DiscoverItems nodes = mgr.discoverNodes(null);
	           for (Iterator<DiscoverItems.Item> items = nodes.getItems(); items.hasNext();) {
	        	   
	        	    DiscoverItems.Item i = items.next();
	   			    final LeafNode node = mgr.getNode(i.getNode());
	   				final String name = node.getId();
	   				Log.d("SubscribeActivity::onCreate", "Node '" + name );
	   				
	   			    PubSubNodeItem nodeItem = new PubSubNodeItem(name);
	   			    nodeItem.setSubscription(R.drawable.unsubscribed);
	   			    
	   			    //then mark the relevant ones as subscribed
	 	            List<Subscription> subscriptions = mgr.getSubscriptions();
	 	            for (Iterator iterator = subscriptions.iterator(); iterator.hasNext();) {
	 	        	   Subscription subs = (Subscription) iterator.next();
	 	        	   if (subs.getNode().equals(name)) {
	 	        		   //Log.i("SUBSCRIPTIONS",subs.toXML());
	 	        	   	   //Log.i("SUBSCRIPTIONS",subs.getState().toString());
	 	        	   	   nodeItem.setSubscription(R.drawable.subscribed);
		   			       break;
	 	        	   }
	 	            }
	 	           listdata.add(nodeItem);
	           }
	           
	           
	           setListAdapter(mgr);
	           

	         } catch (ClassCastException cce) {
	                Log.e("PublishActivity::onCreate", "Did you register Smack's XMPP Providers and Extensions in advance? - " +
	                		"SmackAndroid.init(this)?\n" + cce.getMessage());
	                cce.printStackTrace();
                 showToast("[Class Cast Exception] " + cce.getMessage());
	                connection = null;
	         } catch (XMPPException ex) {
	                Log.e("PublishActivity::onCreate", "XMPPException for '"+  Constants.USERNAME + "'");
	                ex.printStackTrace();
	                
	                showToast("[XMPP Exception] " + ex.getMessage());
	                connection = null;    
	         } catch (Exception e) {
	              //all other exceptions
	        	   Log.e("PublishActivity::onCreate", "Unhandled Exception"+  e.getMessage()); 
	        	   e.printStackTrace();
	        	   showToast("[Unhandled Exception] " + e.getMessage());
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
        
		Log.d("SubscribeActivity::setListAdapter","Entered");
        final SubscribeListAdapter adapter = new SubscribeListAdapter(
        		this, R.layout.listitem_subscribe, listdata);
        //Log.d("SubscribeActivity::setListAdapter","here 2....");
        // Add the incoming message to the view
        mHandler.post(new Runnable() {
          public void run() {
        	  listview.setAdapter(adapter);
        	  
          }
        });
         
	}	//end of 'setListAdapter'
	
	//display Toast from any thread
  	public void showToast(final String toast)
  	{
  	    runOnUiThread(new Runnable() {
  	        public void run()
  	        {
  	            Toast.makeText(SubscribeActivity.this, toast, Toast.LENGTH_LONG).show();
  	        }
  	    });
  	}
}
