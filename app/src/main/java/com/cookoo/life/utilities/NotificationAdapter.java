package com.cookoo.life.utilities;

import java.util.ArrayList;

import com.cookoo.life.notification.NotifCategories;
import com.cookoo.life.service.NotifListenerService;
import com.cookoo.life.R;

import android.content.Context;
import android.service.notification.StatusBarNotification;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NotificationAdapter extends ArrayAdapter<StatusBarNotification>{

    Context context; 
    int layoutResourceId;    
    ArrayList<StatusBarNotification> data = null;
    
    LayoutInflater mInflater;
    
    /*
     * Constants related to each icon
     */
    private static final int alarm = R.drawable.nc2_alarm;
    private static final int battery_crit = R.drawable.nc2_battery_crit;
    private static final int battery_low = R.drawable.nc2_battery_low;
    private static final int call_missed = R.drawable.nc2_call;
    private static final int voicemail = R.drawable.nc2_voicemail;
    private static final int seen_voicemail = R.drawable.seen_voicemail;
    private static final int call = R.drawable.nc2_call;
    private static final int email = R.drawable.nc2_email;
    private static final int private_message = R.drawable.nc2_private;
    private static final int social_message = R.drawable.nc2_social;
    private static final int schedule = R.drawable.nc2_schedule;
    private static final int system_high = R.drawable.nc2_system;
  //  private static final int system_low = R.drawable.nc2_system_high;
    
    private static boolean seen = false;
    
	public static final long SECOND_MILLS = 1000L;
	public static final long MINUTE_MILLS = 60000L;
	public static final long HOUR_MILLS = 3600000L;
	public static final long DAY_MILLS = 86400000L;
	
    
    StatusBarNotification cNotif;
    
    public NotificationAdapter(Context context, int layoutResourceId, ArrayList<StatusBarNotification> mValues) {
        super(context, layoutResourceId, mValues);
        this.layoutResourceId = layoutResourceId;
        this.context = context;
        this.data = mValues;
        
        if (this.data.size() == 0){
        	this.clear();
        }
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View row = convertView;
        NotificationHolder holder = null; 
        cNotif = data.get(position); 
        
        if(row == null)
        {

        	layoutResourceId = R.layout.listview_item_row;
        	mInflater = LayoutInflater.from(context);
            row = mInflater.inflate(layoutResourceId, parent, false);
            holder = new NotificationHolder();
            holder.imgIcon = (ImageView)row.findViewById(R.id.imageAlert);
            holder.title = (TextView)row.findViewById(R.id.txtTitle);
            holder.description = (TextView)row.findViewById(R.id.txtDescription);
            holder.posted = (TextView)row.findViewById(R.id.txtPosted2);
            row.setTag(holder);
        }
        else
            holder = (NotificationHolder)row.getTag();

        for(int i = 0; i < NotifCategories.AndroidNotifCategories.length; i++ ){
    		if ( String.valueOf(cNotif.getPackageName()).equals( NotifCategories.AndroidNotifCategories[i]) ){


    			if ( NotifListenerService.SeenCollection != null ){
    			if ( NotifListenerService.SeenCollection.containsKey(cNotif.getPostTime()) ){
    				seen = true;
    			}
    			}
    			
    			Log.d("notification_logic","notification ADDED - id: "+ cNotif.getPostTime() +"package : "+
                        cNotif.getPackageName()+ " text : "+ cNotif.getNotification().tickerText +
                        " image-id: "+cNotif.getNotification().icon);

    			// created date
				long createdDate = cNotif.getPostTime();
				long passedTime = System.currentTimeMillis() - createdDate;

				if (passedTime < SECOND_MILLS) {
					holder.posted
							.setText(R.string.fragment_notification_list_created_righ_now);
				} else if (passedTime < MINUTE_MILLS) {
					int time = (int) (passedTime / SECOND_MILLS);
					holder.posted.setText(context.getResources().getString(
							R.string.fragment_notification_list_created_time,
							time, "s"));
				} else if (passedTime < HOUR_MILLS) {
					int time = (int) (passedTime / MINUTE_MILLS);
					holder.posted.setText(context.getResources().getString(
							R.string.fragment_notification_list_created_time,
							time, "m"));
				} else if (passedTime < DAY_MILLS) {
					int time = (int) (passedTime / HOUR_MILLS);
					holder.posted.setText(context.getResources().getString(
							R.string.fragment_notification_list_created_time,
							time, "h"));
				} else {
					int time = (int) (passedTime / DAY_MILLS);
					holder.posted.setText(context.getResources().getString(
							R.string.fragment_notification_list_created_time,
							time, "d"));
				}
				
				int notifColor = NotifCategories.AndroidColors[i];
    			
    			// Return the Voicemail icon if the icon matches
    			if (cNotif.getPackageName().equals("com.android.phone") &&
                        (cNotif.getNotification().tickerText == null ||
                                cNotif.getNotification().tickerText.toString().trim().isEmpty()) &&
                        (cNotif.getNotification().icon == 17301630 || cNotif.getNotification().icon == 0)){
    				
    				 holder.title.setText("Voicemail");                   // SET TYPE VOICE MAIL - TEMPORARY FIX ANDROID BUG
    				 holder.description.setText("New voicemail received");

    				 if (!seen){
    					 holder.imgIcon.setImageResource(voicemail);
    					 holder.title.setTextColor(context.getResources().getColor(notifColor));
            			 holder.description.setTextColor(context.getResources().getColor(notifColor));
            			 holder.posted.setTextColor(context.getResources().getColor(notifColor));
    				 }else{
    					 holder.imgIcon.setImageResource(seen_voicemail);
        				 holder.title.setTextColor(context.getResources().getColor(R.color.gray));
            			 holder.description.setTextColor(context.getResources().getColor(R.color.gray));
            			 holder.posted.setTextColor(context.getResources().getColor(R.color.gray));
    				 }
  
    				 
           		}else if ( cNotif.getPackageName().equals("com.android.phone") &&
                        cNotif.getNotification().icon == 2130837640 ){
           			     Log.d("notification_logic", "com.android.phone found with icon 2130837640");

           		}else{

           			if(NotifCategories.AndroidNotifIcons[i] != 0){
           				holder.title.setText(NotifCategories.AndroidNotifNames[i]);
                        if(cNotif.getNotification().tickerText != null &&
                                !cNotif.getNotification().tickerText.toString().isEmpty() ) {
           				    holder.description.setText(cNotif.getNotification().tickerText);
                        }
                        else
                            holder.description.setText(" ");
           			 if (!seen){
    					 holder.title.setTextColor(context.getResources().getColor(notifColor));
            			 holder.description.setTextColor(context.getResources().getColor(notifColor));
            			 holder.posted.setTextColor(context.getResources().getColor(notifColor));
            			 holder.imgIcon.setImageResource(NotifCategories.AndroidNotifIcons[i]);
    				 }else{
        				 holder.title.setTextColor(context.getResources().getColor(R.color.gray));
            			 holder.description.setTextColor(context.getResources().getColor(R.color.gray));
            			 holder.posted.setTextColor(context.getResources().getColor(R.color.gray));
            			 holder.imgIcon.setImageResource(NotifCategories.AndroidSeenIcons[i]);
    				 }

           			}
           		}
    			seen = false;
    			break;
    				
    		}
        }
        
        if(holder.title.getText().equals("")){
        	holder.title.setText(cNotif.getPackageName());
        }
        return row;
    }

    
    static class NotificationHolder
    {
        ImageView imgIcon;
        TextView title;
        TextView description;
        TextView posted;
    }
}