package com.example.app.helpers;

import com.example.app.Constants;

import java.util.Calendar;

public class EpochHelper {

	private EpochHelper() { /* Empty */ }

	public static long getCurrentEpochTime() {
		return Calendar.getInstance().getTimeInMillis()/1000;
	}

	public static long getCurrentEpochDay() {
		return getCurrentEpochTime() / Constants.SECONDS_IN_DAY;
	}

	public static long getCurrentInterval() {
		return getCurrentEpochTime() / Constants.SECONDS_IN_INTERVAL;
	}

	public static long getFirstIntervalOfDay(long epochDay) {
		return epochDay * Constants.SECONDS_IN_DAY / Constants.SECONDS_IN_INTERVAL;
	}

	public static long getLastIntervalOfDay(long epochDay) {
		return getFirstIntervalOfDay(epochDay) + Constants.NUM_OF_INTERVALS_IN_DAY - 1;
	}
}
