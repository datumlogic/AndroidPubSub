package com.fezzee.androidpubsub;

/*
 * This class is the public notification category.
 */
public class PubSubNodeItem {
	private String nodeName;
	private int itemCount;
	private int subscriptionState; //holds the image resource that denotes Subscribed or Unsubscribed
	private String inputXML;//
	
	
	public PubSubNodeItem(String nodeName, int itemCount)
	{
		 this.nodeName = nodeName;
		 this.itemCount = itemCount;
	}
	
	public PubSubNodeItem(String nodeName)
	{
		 this.nodeName = nodeName;
	}
	
	public String getNodeName() {
	    return this.nodeName;
	}
	public void setNodeName(String nodeName) {
	    this.nodeName = nodeName;
	}
	
	public int getSubscription() {
	    return this.subscriptionState;
	}
	public void setSubscription(int state) {
	    this.subscriptionState = state;
	}
	
	public int getItemCount() {
	    return this.itemCount;
	}
	public void setItemCount(int itemCount) {
	    this.itemCount = itemCount;
	}
	
	public void incrementItemCount() {
	    this.itemCount += 1;
	}
}
