package com.geekyint.login.utils;

/**
 * Created by geekyint on 1/7/16.
 */
public class Constants {

    private static final int MILI_SECONDS_PER_SECOND = 10000;

    private static final int UPDATE_INTERVAL_IN_SECONDS = 60;

    public static final long UPDATE_INTERVAL = MILI_SECONDS_PER_SECOND; //MILI_SECONDS_PER_SECOND * UPDATE_INTERVAL_IN_SECONDS;

    private static final int FAST_INTERVAL_IN_SECONDS = 60;

    public static final long FAST_INTERVAL = MILI_SECONDS_PER_SECOND / 2; //MILI_SECONDS_PER_SECOND / FAST_INTERVAL_IN_SECONDS;

    public static final String LOCATION_FILE = "sdcard/location.txt";

    public static final String LOG_FILE = "sdcard/log.txt";

    public static final String RUNNING = "runningInBackground"; // Recording data in background

    public static final String APP_PACKAGE_NAME = "com.blackcj.locationtracker";

    /**
     * Suppress default constructor for noninstantiability
     */
    private Constants() {
        throw new AssertionError();
    }
}
