package cluj.stratupweekend.cloudclipboard.prefs;


/**
 * Define constants like name or keys for application preferences 
 */
public class AppPrefs{
    
	public static String TAG = "CloudClipboard"; 
	
    private AppPrefs() {}
    
    /** Name of shared preference */
    public static final String NAME = "AppPrefs";
    
    /**
     * Time interval of monitor checking in milliseconds
     * <p>TYPE: int</p>
     */
    public static final String KEY_MONITOR_INTERVAL = "monitor.interval";
    
    /**
     * Id of current operating clipboard
     * <p>TYPE: int</p>
     */
    public static final String KEY_OPERATING_CLIPBOARD = "clipboard";

    public static final int DEF_MONITOR_INTERVAL = 1000;

    public static final int DEF_OPERATING_CLIPBOARD = 1; // 1 = default clipboard

    /**
     * The current operating clipboard id
     * <p>
     * This is for cache usage. Every time MyClips creates (in onCreate()), it
     * reads from shared preference to here and writes back as it pauses (in
     * onPause()). When ClipboardMonitor is created (in onCreate()), it also
     * initialize this value.
     */
    public static volatile int operatingClipboardId;
}
