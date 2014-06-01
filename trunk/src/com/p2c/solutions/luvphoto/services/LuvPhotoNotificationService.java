package com.p2c.solutions.luvphoto.services;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;

import com.p2c.solutions.luvphoto.MainActivity_;
import com.p2c.solutions.luvphoto.R;
import com.p2c.solutions.luvphoto.core.webservices.JsonMessage;
import com.p2c.solutions.luvphoto.core.webservices.WebServiceInvoker.JsonResult;
import com.p2c.solutions.luvphoto.helper.ServiceNotificationHelper;

public class LuvPhotoNotificationService extends Service {

	private Timer timer = new Timer();
    private static long UPDATE_INTERVAL = 86400000;
    private static long DELAY_INTERVAL = 10000;
    
    private NotificationManager notificationMgr;
    @Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		notificationMgr = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		Calendar calendar = Calendar.getInstance();
		startNotificationService(calendar);
	}
	
	private void startNotificationService(final Calendar calendar) {
					
		timer.scheduleAtFixedRate(new TimerTask() {
			
			@Override
			public void run() {			
								
				displayNotificationMessage(calendar);
					
				try {
					
					Thread.sleep(UPDATE_INTERVAL);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}					
				
			}
		}, DELAY_INTERVAL, UPDATE_INTERVAL);
	}
		
	private void displayNotificationMessage(Calendar calendar){
        		
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		Log.i("Dayofweek",""+dayOfWeek);
		if(dayOfWeek == 1)
		{
			ServiceNotificationHelper notificationHeleper = new ServiceNotificationHelper(getApplicationContext());
			JsonResult result = notificationHeleper.GetNotification();
			if(result.getMessage() == JsonMessage.SUCCESSFULL)
			{
				String notificationContent = result.toString();
				
				NotificationCompat.Builder mBuilder =
				        new NotificationCompat.Builder(this)
				        .setSmallIcon(R.drawable.notification3)
				        .setContentTitle("Luv Photo")
				        .setContentText(notificationContent);
				
				Intent i = new Intent(this, MainActivity_.class);				
				
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
				stackBuilder.addParentStack(MainActivity_.class);
				stackBuilder.addNextIntent(i);
				PendingIntent resultPendingIntent =  stackBuilder.getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder.setContentIntent(resultPendingIntent);
				
		        //Random r = new Random();
		        //int i1=r.nextInt(80-65) + 65;
		        notificationMgr.notify(8888, mBuilder.build());	
			}		        
		}
    }

	@Override
	public void onDestroy() {
		stopNotificationService();
	}

	private void stopNotificationService() {
		if (timer != null)
            timer.cancel();
        //Toast.makeText(this, "Service Timer stopped...", Toast.LENGTH_LONG).show();
	}
}

