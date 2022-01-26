package com.example.hub.task;

import com.example.hub.models.InfectedSKManager;

import java.sql.SQLException;
import java.util.Calendar;

public class CleanSkTask implements Runnable{

    private static final int SECONDS_IN_DAY = 86400;
    private static final int SK_EXPIRATION_DAYS = 14;

    @Override
    public void run() {
        try {
            int expirationEpochDay = getCurrentEpochDay() - SK_EXPIRATION_DAYS;
            InfectedSKManager.removeExpiredSks(expirationEpochDay);
            System.out.println("Removed old sks");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getCurrentEpochDay() {
        long now = Calendar.getInstance().getTimeInMillis() / 1000;
        return (int) now / SECONDS_IN_DAY;
    }
}
