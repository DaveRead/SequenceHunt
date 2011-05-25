package com.monead.games.android.sequence.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;

/**
 * Provide formatting services.
 * 
 * @author David Read
 * 
 */
public final class Formatter {
    /**
     * Singleton instance.
     */
    private static Formatter formatter;

    /**
     * Class name used for logging.
     */
    private String className = this.getClass().getName();

    /**
     * A formatter for time.
     */
    private SimpleDateFormat timerFormat;

    /**
     * Create a formatter - private since Singleton.
     */
    private Formatter() {
        timerFormat = new SimpleDateFormat("HH:mm:ss");
        timerFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * Get an instance of the formatter.
     * 
     * @return The formatter instance
     */
    public static synchronized Formatter getInstance() {
        if (formatter == null) {
            formatter = new Formatter();
        }
        return formatter;
    }

    /**
     * Obtain MS in hour:minute:second format.
     * 
     * @param milliseconds
     *            The number of milliseconds
     * 
     * @return The converted value
     */
    public String formatTimer(final long milliseconds) {
        Date tempDate;

        tempDate = new Date(milliseconds);

        Log.d(className, "milliseconds [" + milliseconds + "] tempDate ["
                + tempDate + "] result[" + timerFormat.format(tempDate) + "]");

        return timerFormat.format(tempDate);
    }
}
