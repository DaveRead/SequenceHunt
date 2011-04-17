package com.monead.games.android.sequence.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.util.Log;

public class Formatter {
	private static Formatter formatter;

	/**
	 * Class name used for logging
	 */
	private String className = this.getClass().getName();

	SimpleDateFormat timerFormat;
	
	private Formatter() {
		timerFormat = new SimpleDateFormat("HH:mm:ss");
		timerFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}
	
	public static final synchronized Formatter getInstance() {
		if (formatter == null) {
			formatter = new Formatter();
		}
		return formatter;
	}
	
	public String formatTimer(long milliseconds) {
		Date tempDate;
		
		tempDate = new Date(milliseconds);

		Log.d(className, "milliseconds [" + milliseconds + "] tempDate [" + tempDate + "] result[" + timerFormat.format(tempDate) + "]");
		
		return timerFormat.format(tempDate);
	}
}
