package cluj.stratupweekend.cloudclipboard.service;

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import com.pubnub.api.Callback;
import com.pubnub.api.Pubnub;

import cluj.stratupweekend.cloudclipboard.CloudClipboard;
import cluj.stratupweekend.cloudclipboard.R;
import cluj.stratupweekend.cloudclipboard.prefs.AppPrefs;

//import com.myclips.LogTag;
//import com.myclips.MyClips;
//import com.myclips.R;
//import com.myclips.db.Clip;
//import com.myclips.db.ClipboardDbAdapter;
//import com.myclips.prefs.AppPrefs;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.FileObserver;
import android.os.IBinder;
import android.text.ClipboardManager;
import android.util.Log;
import android.widget.Toast;

/**
 * Starts a background thread to monitor the states of clipboard and stores
 * any new clips into the SQLite database.
 * <p>
 * <i>Note:</i> the current android clipboard system service only supports
 * text clips, so in browser, we can just save images to external storage
 * (SD card). This service also monitors the downloads of browser, if any
 * image is detected, it will be stored into SQLite database, too.   
 */
public class ClipboardMonitor extends Service {
	
	public static String TAG = "CloudClipboard";
    
    /** Image type to be monitored */
    private static final String[] IMAGE_SUFFIXS = new String[] {
        ".jpg", ".jpeg", ".gif", ".png"
    };
    /** Path to browser downloads */
    private static final String BROWSER_DOWNLOAD_PATH = "/sdcard/download";
    
    private NotificationManager mNM;
    private MonitorTask mTask = new MonitorTask();
    private ClipboardManager mCM;
    private SharedPreferences mPrefs;

    Pubnub pubnub = new
			  Pubnub("pub-c-4022c9ea-2a2d-4e82-a47f-236087d30af3",
			  "sub-c-bbc72840-830c-11e2-9881-12313f022c90", "sec-c-OWJlYzIwNGMtN2VhZC00YjYwLThmMzAtOTRjZjNjY2YxMTI0", false); 
	String channel  = "as";
    
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        mNM = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        showNotification();
        mCM = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        mPrefs = getSharedPreferences(AppPrefs.NAME, MODE_PRIVATE);
        AppPrefs.operatingClipboardId = mPrefs.getInt(
                AppPrefs.KEY_OPERATING_CLIPBOARD,
                AppPrefs.DEF_OPERATING_CLIPBOARD);
        mTask.start();   
        
        Hashtable args = new Hashtable(1);
		args.put("channel", channel);
        try {
			pubnub.subscribe(args, new Callback() {
				public void connectCallback(String channel,
						Object message) {
					Log.d(TAG, message.toString());
					
				}

				public void disconnectCallback(String channel,
						Object message) {
					Log.d(TAG, message.toString());
					
				}

				@Override
				public void reconnectCallback(String channel,
						Object message) {
					Log.d(TAG, message.toString());
					
				}

				public void successCallback(String channel,
						Object message) {
					// TODO put in clipboard here
					Log.d(TAG, "success callback");
					@SuppressWarnings("unused")
					String m = message.toString();
					mCM.setText(m);
					
				}

				public void errorCallback(String channel,
						Object message) {
					Log.e(TAG, message.toString());
				}
			});

		} catch (Exception e) {
				e.printStackTrace();
		}
        
    }

    private void showNotification() {
        Notification notif = new Notification(R.drawable.myclips_icon,
                "CloudClipboard clipboard monitor is started",
                System.currentTimeMillis());
        notif.flags |= (Notification.FLAG_ONGOING_EVENT | Notification.FLAG_NO_CLEAR);
        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
                new Intent(this, CloudClipboard.class), 0);
        notif.setLatestEventInfo(this, getText(R.string.clip_monitor_service),
                "Tap here to enter MyClips UI", contentIntent);
        // Use layout id because it's unique
        mNM.notify(R.string.clip_monitor_service, notif);
    }
    
    @Override
    public void onDestroy() {
        mNM.cancel(R.string.clip_monitor_service);
        mTask.cancel();
//        mDbAdapter.close();
        pubnub.unsubscribe(channel);
    }
    
    @Override
    public void onStart(Intent intent, int startId) {
    	Log.i(TAG, "onStart service");
    }

    /**
     * Monitor task: monitor new text clips in global system clipboard and
     * new image clips in browser download directory
     */
    private class MonitorTask extends Thread {

        private volatile boolean mKeepRunning = false;
        private String mOldClip = null;
//        private BrowserDownloadMonitor mBDM = new BrowserDownloadMonitor();
        
        public MonitorTask() {
            super("ClipboardMonitor");
        }

        /** Cancel task */
        public void cancel() {
            mKeepRunning = false;
            interrupt();
        }
        
        @Override
        public void run() {
        	mOldClip = mCM.getText().toString();
            mKeepRunning = true;
//            mBDM.startWatching();
            while (true) {
                doTask();
                try {
                    Thread.sleep(mPrefs.getInt(AppPrefs.KEY_MONITOR_INTERVAL,
                            AppPrefs.DEF_MONITOR_INTERVAL));
                } catch (InterruptedException ignored) {
                }
                if (!mKeepRunning) {
                    break;
                }
            }
//            mBDM.stopWatching();
        }
        
        private void doTask() {
            if (mCM.hasText()) {
                String newClip = mCM.getText().toString();
                if (!newClip.equals(mOldClip)) {
                    Log.i(TAG, "detect new text clip: " + newClip.toString());
                    mOldClip = newClip;
                    
                    // TODO push text here
                    Hashtable args = new Hashtable(2);

                    String message = newClip;

                    args.put("message", message);
                    args.put("channel", channel); 

                    pubnub.publish(args, new Callback() {
                        public void successCallback(String channel, Object message) {
//                            notifyUser(message.toString());
                        	Log.d(TAG, message.toString());
                        }

                        public void errorCallback(String channel, Object message) {
//                        notifyUser(channel + " : " + message.toString());
                        	Log.e(TAG, message.toString());
                        }
                    });

                    
//                    Toast.makeText(getApplicationContext(), "s-a dat copy la <<" + newClip + ">>", Toast.LENGTH_SHORT).show();
                    
//                    mDbAdapter.insertClip(Clip.CLIP_TYPE_TEXT,
//                            newClip.toString(),
//                            AppPrefs.operatingClipboardId);
//                            //mPrefs.getInt(AppPrefs.KEY_OPERATING_CLIPBOARD,
//                            //        AppPrefs.DEF_OPERATING_CLIPBOARD));
                    Log.i(TAG, "new text clip inserted: " + newClip.toString());
                }
            }
        }
        
        /**
         * Monitor change of download directory of browser. It listens two
         * events: <tt>CREATE</tt> and <tt>CLOSE_WRITE</tt>. <tt>CREATE</tt>
         * event occurs when new file created in download directory. If this
         * file is image, new image clip will be inserted into database when 
         * receiving <tt>CLOSE_WRITE</tt> event, meaning file is sucessfully
         * downloaded.
         */
//        private class BrowserDownloadMonitor extends FileObserver {
//
//            private Set<String> mFiles = new HashSet<String>();
//            
//            public BrowserDownloadMonitor() {
//                super(BROWSER_DOWNLOAD_PATH, CREATE | CLOSE_WRITE);
//            }
//            
//            private void doDownloadCompleteAction(String path) {
////                mDbAdapter.insertClip(Clip.CLIP_TYPE_IMAGE,
////                        BROWSER_DOWNLOAD_PATH + "/" + path,
////                        AppPrefs.operatingClipboardId);
////                        //mPrefs.getInt(AppPrefs.KEY_OPERATING_CLIPBOARD,
////                        //        AppPrefs.DEF_OPERATING_CLIPBOARD));
//                Log.i(TAG, "new image clip inserted: " + path);
//            }
//            
//            @Override
//            public void onEvent(int event, String path) {
//                switch (event) {
//                    case CREATE:
//                        for (String s : IMAGE_SUFFIXS) {
//                            if (path.endsWith(s)) {
//                                Log.i(TAG, "detect new image: " + path);
//                                mFiles.add(path);
//                                break;
//                            }
//                        }
//                        break;
//                    case CLOSE_WRITE:
//                        if (mFiles.remove(path)) { // File download completes
//                            doDownloadCompleteAction(path);
//                        }
//                        break;
//                    default:
//                        Log.w(TAG, "BrowserDownloadMonitor go unexpected event: "
//                                + Integer.toHexString(event));                        
////                        throw new RuntimeException("BrowserDownloadMonitor" +
////                        		" got unexpected event");
//                }
//            }
//        }
    }
}
