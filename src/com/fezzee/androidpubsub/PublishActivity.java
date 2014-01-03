package com.fezzee.androidpubsub;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SmackAndroid;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smackx.packet.DiscoverItems;
import org.jivesoftware.smackx.pubsub.AccessModel;
import org.jivesoftware.smackx.pubsub.ConfigureForm;
import org.jivesoftware.smackx.pubsub.FormType;
import org.jivesoftware.smackx.pubsub.Item;
import org.jivesoftware.smackx.pubsub.LeafNode;
import org.jivesoftware.smackx.pubsub.PayloadItem;
import org.jivesoftware.smackx.pubsub.PubSubManager;
import org.jivesoftware.smackx.pubsub.PublishModel;
import org.jivesoftware.smackx.pubsub.SimplePayload;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;




public class PublishActivity extends Activity {
	
	protected static XMPPConnection connection;
	protected static PubSubManager mgr;
	protected SmackAndroid asmk; 

	private Handler mHandler = new Handler();
	
    private ListView listview;
    
    private ArrayList<PubSubNodeItem> listdata = new ArrayList<PubSubNodeItem>();
    
    Editable editable = null;
   
    //INVESTIGATION- there were two ways of specifying PREFS- one using the default PREFS 
  	//and one specifying- is there any security implications of not using default?
  	//private static final String PREFS_NAME = "TwitterLogin";
  	SharedPreferences settings = null;
  	SharedPreferences.Editor editor = null;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_publish);
		
		
		//settings = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);//PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		settings = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		editor = settings.edit();
				
		Constants.USERNAME = settings.getString("userName","gene4");//gene //fola
		Constants.PASSWORD = settings.getString("userPwd","gene4");//"gene123"; //fola123
		Constants.HOST = settings.getString("host","ec2-54-201-47-27.us-west-2.compute.amazonaws.com");//"ec2-54-201-47-27.us-west-2.compute.amazonaws.com";
		Constants.PORT = Integer.parseInt( settings.getString("port","5222") );//5222;
		Constants.TIMEOUT = Integer.parseInt(settings.getString("timeout","5000") );//5222;
		Constants.SERVICE = settings.getString("service","ec2-54-201-47-27.us-west-2.compute.amazonaws.com");//"ec2-54-201-47-27.us-west-2.compute.amazonaws.com";
		
		//This app on the Android desktop always automatically gets the name of the first Activity 
		//Remove the android:label for the first activity, which forces the android:label of the app to be used
		//Finally., set the Activity Title here
		//setTitle(R.string.publish_name);
		setTitle(Constants.USERNAME);
		
		//if you don't use this you get a ClassCastException: UnparsedResultIQ cannot be cast to DiscoverInfo
		asmk = SmackAndroid.init(this);
		
		//only need to do this initially
		SmackConfiguration.setPacketReplyTimeout(Constants.TIMEOUT);
		
		listview = (ListView) this.findViewById(R.id.publishview);
		listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {

		     public void onItemClick(AdapterView<?> parentAdapter, View view, int position,
		                             long id) {

		        // We know the View is a TextView so we can cast it
		        //TextView clickedView = (TextView) view;
		        //Toast.makeText(PublishActivity.this, "Item with id ["+id+"] - Position ["+position+"] - Text ["+clickedView.getText()+"]", Toast.LENGTH_SHORT).show();
		        final long idx = id;
		        
		        Context context = view.getContext();
		        LinearLayout layout = new LinearLayout(context);
		        layout.setOrientation(LinearLayout.VERTICAL);

		        final EditText reviewBox = new EditText(context);
		        reviewBox.setHint("Review (1-3)");
		        layout.addView(reviewBox);

		        final EditText descriptionBox = new EditText(context);
		        descriptionBox.setHint("Description");
		        layout.addView(descriptionBox);
		        
		        //final EditText input = new EditText(PublishActivity.this);
				String message = "Enter a Report to Publish:";
			    

				new AlertDialog.Builder(PublishActivity.this)
				    .setTitle("Node Configuration")
				    .setMessage(message)
				    .setView(layout) //input)
				    .setPositiveButton("Publish", new DialogInterface.OnClickListener() {
				         public void onClick(DialogInterface dialog, int whichButton) {
				             
				        	 String nodeName = listdata.get((int)idx).getNodeName();
				             Toast.makeText(PublishActivity.this, "Publish Node: " + nodeName + " [" + reviewBox.getText() + " : " + descriptionBox.getText() + "]", Toast.LENGTH_SHORT).show();
				             
				             publishNode(nodeName, reviewBox.getText().toString(), descriptionBox.getText().toString());
				             
				         }// end of onClick
				    }) // end of setPositiveButton
				    .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
				         public void onClick(DialogInterface dialog, int whichButton) {
				 
				        	 String nodeName = listdata.get((int)idx).getNodeName();
						        Toast.makeText(PublishActivity.this, "Delete ["+ nodeName +"]", Toast.LENGTH_SHORT).show();
						        deleteNode(nodeName);
				         }
				    })
				    .setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
				         public void onClick(DialogInterface dialog, int whichButton) {
				                // Do nothing.
				         }
				    }).show();
		     }
		});
		
		
		
		final ProgressDialog dialog2 = ProgressDialog.show(PublishActivity.this, "Connecting...", "Please wait...", false);
	     Thread t = new Thread(new Runnable() {
	      @Override
	      public void run() {
	      
	       // Create a connection
	       ConnectionConfiguration connConfig = new ConnectionConfiguration(Constants.HOST, Constants.PORT, Constants.SERVICE);
	       connection = new XMPPConnection(connConfig);
	         try {
	           Log.d("PublishActivity::onCreate",  "[TIMEOUT] - " + SmackConfiguration.getPacketReplyTimeout());
	        	 
	           connection.connect();
	           Log.d("PublishActivity::onCreate",  "Connected to "+connection.getHost());
	        
	           connection.login(Constants.USERNAME, Constants.PASSWORD, Constants.RESOURCE);
	           Log.d("PublishActivity::onCreate",  "Logged in as " + connection.getUser());
	           
	           // Create a pubsub manager using an existing Connection
	           mgr = new PubSubManager(connection,"pubsub." + Constants.HOST);
	           
	           DiscoverItems nodes = mgr.discoverNodes(null);
	           
	           for (Iterator<DiscoverItems.Item> items = nodes.getItems(); items.hasNext();) {
	        	
	        	    DiscoverItems.Item i = items.next();
	        	    Log.d("PublishActivity::onCreate", "Creating Node '" + i.getNode() + "'");
	   			    final LeafNode node = mgr.getNode(i.getNode());
	   			
	   				Collection<? extends Item> items2 = node.getItems();
	   				int count = items2.size();
	   				final String name = node.getId();
	   				Log.d("PublishActivity::onCreate", "Node '" + name + "' Count: " + count);
	   				listdata.add(new PubSubNodeItem(name,count));
	   				
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
	    
	    
	} //end of onCreate


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.publish, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    	case R.id.action_newnode:
	    		createNode();
	    		return true;
	    	case R.id.action_deleteall:
	    		deleteAllNodes();
	    		return true;
	    	case R.id.action_subscribe:
	    		//finish();
	    		startActivity(new Intent(getBaseContext(), SubscribeActivity.class));
	    		return true;
	    	case R.id.action_feed:
	    		//finish();
	    		startActivity(new Intent(getBaseContext(), FeedActivity.class));
	    		return true;
	    	case R.id.action_settings:
	    		startActivity(new Intent(getBaseContext(), SettingsPreferenceActivity.class));
	    		return true;
	        default:
	            return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
	  protected void onDestroy() {
	    super.onDestroy();
	    Log.d("PublishActivity::onDestroy","Entered");
	    listview.setOnItemClickListener(null);
	    //if (connection != null) {
		//	connection.disconnect();
		//  	connection=null;
		//} 
	  }
	
	@Override
    public void onPause() {
        super.onPause();
        Log.d("PublishActivity::onPause","Entered");
        //listview.setOnItemClickListener(null);

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("PublishActivity::onResume","Entered");
            if(listview != null){
                //listview.setOnItemClickListener(exampleListener);
             }
    }
	
	private void publishNode(String nodeName,String val,String val2)
	{
		final String fNode = nodeName;
		final String fVal = val;
		final String fVal2 = val2;
		final ProgressDialog dialog = ProgressDialog.show(PublishActivity.this, "Connecting...", "Please wait...", false);
	     Thread t = new Thread(new Runnable() {
	      @Override
	      public void run() {
	      
	       // Create a connection
	       ConnectionConfiguration connConfig = new ConnectionConfiguration(Constants.HOST, Constants.PORT, Constants.SERVICE);
	       connection = new XMPPConnection(connConfig);
	         try {
	        	 
	           connection.connect();
	           Log.d("PublishActivity::newNode",  "[SettingsDialog] Connected to "+connection.getHost());
	        
	           connection.login(Constants.USERNAME, Constants.PASSWORD, Constants.RESOURCE);
	           Log.d("PublishActivity::newNode",  "Logged in as " + connection.getUser());
	           
	           // Create a pubsub manager using an existing Connection
	           mgr = new PubSubManager(connection,"pubsub." + Constants.HOST);
	          LeafNode node =  mgr.getNode(fNode);
	          
	          //'note' will need to be a reserved word that can not be used as a field name when creating a 'notification' schema
	          //if you change the value of 'note' then you have to change getValue in PubSubItem
	          SimplePayload payload = new SimplePayload("note","pubsub:test:note", "<note xmlns='pubsub:test:note'><rating type='choice' length='3'>" + fVal + "</rating><description type='' length='' validation='regex'>" +  fVal2 + "</description></note>");
	          PayloadItem<SimplePayload> item2 = new PayloadItem<SimplePayload>("note" + System.currentTimeMillis(), payload);
	          
	          
	          node.publish(item2);
	           
	          for (Iterator<PubSubNodeItem> iterator = listdata.iterator(); iterator.hasNext();) {
	        	   PubSubNodeItem NodeItem = iterator.next();
	        	   if (NodeItem.getNodeName().equals(fNode))
	        	   {
	        		   NodeItem.incrementItemCount();
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
	         
	         dialog.dismiss();
	      }
	   }); // end of thread

	    t.start();
	    dialog.show();	
	}
	
	private void deleteNode(String nodeName)
	{
		final String fNode = nodeName;
		Log.d("PublishActivity::deleteNode","Deleting node: " + fNode);
		final ProgressDialog dialog = ProgressDialog.show(PublishActivity.this, "Connecting...", "Please wait...", false);
	     Thread t = new Thread(new Runnable() {
	      @Override
	      public void run() {
	      
	       // Create a connection
	       ConnectionConfiguration connConfig = new ConnectionConfiguration(Constants.HOST, Constants.PORT, Constants.SERVICE);
	       connection = new XMPPConnection(connConfig);
	         try {
	           connection.connect();
	           Log.d("PublishActivity::newNode",  "[SettingsDialog] Connected to "+connection.getHost());
	        
	           connection.login(Constants.USERNAME, Constants.PASSWORD, Constants.RESOURCE);
	           Log.d("PublishActivity::newNode",  "Logged in as " + connection.getUser());
	           
	           // Create a pubsub manager using an existing Connection
	           mgr = new PubSubManager(connection,"pubsub." + Constants.HOST);
	           
	           Log.d("PublishActivity::deleteNode","Deleting(2) node: " + fNode);
	           mgr.deleteNode(fNode);
	           
	           //can I just define equality op or a comparitor in PubSubNodeItem or 
	           //define a custom listdata datatype
	           //so I could just say listdata.remove(fNode)?
	           for (Iterator iterator = listdata.iterator(); iterator.hasNext();) {
	        	   PubSubNodeItem NodeItem = (PubSubNodeItem) iterator.next();
	        	   if (NodeItem.getNodeName().equals(fNode)) {
	        		   listdata.remove(NodeItem);
	        		   break;
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
	         
	         dialog.dismiss();
	      }
	   }); // end of thread

	    t.start();
	    dialog.show();	
	}
	
	
	
	private void createNode()
	{
		// Set an EditText view to get user input 
		final EditText input = new EditText(PublishActivity.this);
		String message = "Enter a Node name:";
	    

		new AlertDialog.Builder(PublishActivity.this)
		    .setTitle("Add Favorite")
		    .setMessage(message)
		    .setView(input)
		    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton) {
		             editable = input.getText(); 
		             // deal with the editable
		             //Toast.makeText(FavoritesActivity.this, editable.toString(), Toast.LENGTH_SHORT).show();
		             
		             final ProgressDialog dialog2 = ProgressDialog.show(PublishActivity.this, "Connecting...", "Please wait...", false);
		     	     Thread t = new Thread(new Runnable() {
		     	      @Override
		     	      public void run() {
		     	      
		     	       // Create a connection
		     	       ConnectionConfiguration connConfig = new ConnectionConfiguration(Constants.HOST, Constants.PORT, Constants.SERVICE);
		     	       connection = new XMPPConnection(connConfig);
		     	         try 
		     	         {
		     	           connection.connect();
		     	           Log.d("PublishActivity::newNode",  "[SettingsDialog] Connected to "+connection.getHost());
		     	        
		     	           connection.login(Constants.USERNAME, Constants.PASSWORD, Constants.RESOURCE);
		     	           Log.d("PublishActivity::newNode",  "Logged in as " + connection.getUser());
		     	           
		     	           // Create a pubsub manager using an existing Connection
		     	           mgr = new PubSubManager(connection,"pubsub." + Constants.HOST);
		     	           
		     	           
		     	           LeafNode newleaf = mgr.createNode(editable.toString());//let the ID be auto assigned
		     	           ConfigureForm form = new ConfigureForm(FormType.submit);
		     	           form.setAccessModel(AccessModel.open);
		     	           form.setDeliverPayloads(true);
		     	           form.setNotifyRetract(true);
		     	           form.setPersistentItems(true);
		     	           form.setPublishModel(PublishModel.open);
		     	           newleaf.sendConfigurationForm(form);
		     	           
		     	           listdata.add(new PubSubNodeItem(editable.toString(),0));
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
		    })
		    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
		         public void onClick(DialogInterface dialog, int whichButton) {
		                // Do nothing.
		         }
		    }).show();
		
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
        
		Log.d("PublishActivity::setListAdapter","Entered");
        final PubSubNodeListAdapter adapter = new PubSubNodeListAdapter(
        		this, R.layout.listitem_publish, listdata);
        Log.d("PublishActivity::setListAdapter","Adapter created");
        // Add the incoming message to the view
        mHandler.post(new Runnable() {
          public void run() {
        	  listview.setAdapter(adapter);
        	  
          }
        });
	}
        
    public void deleteAllNodes()
    {
        final ProgressDialog dialog2 = ProgressDialog.show(PublishActivity.this, "Connecting...", "Please wait...", false);
   	    Thread t = new Thread(new Runnable() {
   	        @Override
   	        public void run() {
   	      
   	           // Create a connection
   	           ConnectionConfiguration connConfig = new ConnectionConfiguration(Constants.HOST, Constants.PORT, Constants.SERVICE);
   	           connection = new XMPPConnection(connConfig);
   	           try 
   	           {
   	        	   
   	               connection.connect();
   	               Log.d("PublishActivity::deleteAllNodes",  "[SettingsDialog] Connected to "+connection.getHost());
   	        
   	               connection.login(Constants.USERNAME, Constants.PASSWORD, Constants.RESOURCE);
   	               Log.d("PublishActivity::deleteAllNodes",  "Logged in as " + connection.getUser());
   	           
   	               // Create a pubsub manager using an existing Connection
   	               mgr = new PubSubManager(connection,"pubsub." + Constants.HOST);
   	               
   	           
   	               DiscoverItems nodes = mgr.discoverNodes(null);
   	           
   	               for (Iterator<DiscoverItems.Item> items = nodes.getItems(); items.hasNext();) {
   	        	
   	        	       DiscoverItems.Item i = items.next();
   	        	       mgr.deleteNode(i.getNode());

   	   				   Log.d("PublishActivity::deleteAllNodes", "Delete Node: '" + i.getNode());
 
   	               }
   	               
   	               listdata = new ArrayList<PubSubNodeItem>();
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
   	           
   	        connection = null;
   	        dialog2.dismiss();
   	      } //end of run
   	   }); // end of thread

   	  t.start();
   	  dialog2.show();
        	
    }//end of deleteAlNodes
         
    
  //display Toast from any thread
  	public void showToast(final String toast)
  	{
  	    runOnUiThread(new Runnable() {
  	        public void run()
  	        {
  	            Toast.makeText(PublishActivity.this, toast, Toast.LENGTH_LONG).show();
  	        }
  	    });
  	}
		
}// end of class

