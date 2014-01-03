package com.fezzee.androidpubsub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.ItemPublishEvent;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.Subscription;
import org.jivesoftware.smackx.pubsub.listener.ItemEventListener;

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

public class FeedActivity extends Activity {
	
	protected static XMPPConnection connection;
	protected static PubSubManager mgr;

	private Handler mHandler = new Handler();
	
    private ListView listview;
    
    private ArrayList<PubSubItem> listdata = new ArrayList<PubSubItem>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_feed);
		
		listview = (ListView) this.findViewById(R.id.feedview);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		     public void onItemClick(AdapterView<?> parentAdapter, View view, int position, long id) 
		     {
		    	 Log.d("FeedActivity::onCreate-listview.setOnItemClick","CLICKED");
		    	
		     }
		});
		
		final ProgressDialog dialog2 = ProgressDialog.show(FeedActivity.this, "Connecting...", "Please wait...", false);
	     Thread t = new Thread(new Runnable() {
	      @Override
	      public void run() {
	      
	       // Create a connection
	       ConnectionConfiguration connConfig = new ConnectionConfiguration(Constants.HOST, Constants.PORT, Constants.SERVICE);
	       connection = new XMPPConnection(connConfig);
	         try {
	           connection.connect();
	           Log.d("FeedActivity::onCreate",  "[SettingsDialog] Connected to "+connection.getHost());
	        
	           connection.login(Constants.USERNAME, Constants.PASSWORD, Constants.RESOURCE);
	           Log.d("FeedActivity::onCreate",  "Logged in as " + connection.getUser());
	           
	           // Create a pubsub manager using an existing Connection
	           mgr = new PubSubManager(connection,"pubsub." + Constants.HOST);
	           
	           
	 	       List<Subscription> subscriptions = mgr.getSubscriptions();
	 	       for (Iterator<Subscription> iterator = subscriptions.iterator(); iterator.hasNext();) {
	 	        	  Subscription sub = iterator.next();
	 	        	  Log.i("SUBSCRIPTION",sub.getNode());
	 	        	  //listdata.add(new PubSubNodeItem(sub.getNode()));
	 	        	  LeafNode node = mgr.getNode(sub.getNode());
	 	        	  //REGISTER THE LISTENERS
	 	        	  node.addItemEventListener(new ItemEventCoordinator<Item>());     

	 	        	  Collection<? extends Item> items = node.getItems();
	 	        	  Collection<String> ids = new ArrayList<String>();
	 	        	  for (Iterator<? extends Item> iterator2 = items.iterator(); iterator2.hasNext();) {
	 	        		Item item = iterator2.next();
	 	        		Log.d("ITEM",item.getId());
	 	        		ids.add(item.getId());
	 	        	  }
	 	        	 Collection<? extends Item> items2 = node.getItems(ids);
	 	        	for (Iterator<? extends Item> iterator3 = items2.iterator(); iterator3.hasNext();) {
	 	        		Item item = iterator3.next();
	 	        		Log.d("ITEM FULL",item.toXML());
	 	        		listdata.add(new PubSubItem(node.getId(), item.getId(), item.toXML()));
	 	        	}
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
		getMenuInflater().inflate(R.menu.feed, menu);
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
	    	case R.id.action_subscribe:
	    		finish();
	    		startActivity(new Intent(getBaseContext(), SubscribeActivity.class));
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	private void setListAdapter(PubSubManager mgr)
	{
		
		Log.d("FeedActivity::setListAdapter","Entered");
        final FeedListAdapter adapter = new FeedListAdapter(
        		this, R.layout.listitem_feed, listdata);
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
  	            Toast.makeText(FeedActivity.this, toast, Toast.LENGTH_LONG).show();
  	        }
  	    });
  	} // end of showToast method
  	
  	private class ItemEventCoordinator<T>  implements ItemEventListener<Item>      
  	{          
  		@Override          
  		public void handlePublishedItems(ItemPublishEvent<Item> items)          
  		{              	
  			Collection<? extends Item> items2 = items.getItems();
	        for (Iterator<? extends Item> iterator3 = items2.iterator(); iterator3.hasNext();) {
	        	Item item = iterator3.next();
	        	Log.d(">>>",item.getId() + " : " + item.getNamespace() + " : " + item.getNode() + " : " + item.toString() );
	        	listdata.add(new PubSubItem(items.getNodeId(), item.getId(), item.toXML()));
	        	//item.elementName returns 'item' (cannot change?)
	        	//item.getId returns a unique ID of the Item ('note' + timeval in our example)
	        	//item.getNamespace returns null
	        	//item.getNode returns null
	        	//item.toString returns the object type (Payload) and XML
	        }
  			setListAdapter(mgr);
  		}	
  	}
  	
	
} //end of Feed Activity Class
