package org.odk.collect.android.events;

/**
 * Created by victor on 16-Oct-15.
 */
public class SyncEvent {
    public static String SYNC_START = "start";
    public static String SYNC_END = "end";
    private String status ="";

    public SyncEvent(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
