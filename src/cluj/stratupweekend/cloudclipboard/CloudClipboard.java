package cluj.stratupweekend.cloudclipboard;

import cluj.stratupweekend.cloudclipboard.service.ClipboardMonitor;
import android.os.Bundle;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;

public class CloudClipboard extends Activity {
	
	public static String TAG = "CloudClipboard";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_cloud_clipboard);
		
		
		startClipboardMonitor();
	}
	
	@Override
	protected void onStop() {
		super.onStop();
		
		stopService(new Intent(this,
                ClipboardMonitor.class));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.cloud_clipboard, menu);
		return true;
	}

	
	private void startClipboardMonitor() {
        ComponentName service = startService(new Intent(this,
                ClipboardMonitor.class));
        if (service == null) {
            Log.e(TAG, "Can't start service "
                    + ClipboardMonitor.class.getName());
        }
    }
}
