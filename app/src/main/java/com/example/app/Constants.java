package com.example.app;

import java.util.UUID;

public class Constants {
    public static final UUID SERVICE_UUID = UUID.fromString("0000b81d-0000-1000-8000-00805f9b34fb");

    public static final UUID MESSAGE_UUID = UUID.fromString("7db3e235-3608-41f3-a03c-955fcbd2ea4b");

    public static final int BLE_SCAN_TIME = 1000;
    public static final int BLS_CONNECTION_TIME = 1000;

    public static final int SECONDS_IN_INTERVAL = 300; // 5min
    public static final int SECONDS_IN_DAY = 86400;
    public static final int NUM_OF_INTERVALS_IN_DAY = SECONDS_IN_DAY / SECONDS_IN_INTERVAL;
    public static final int SK_DELETED_AFTER_DAYS = 14;
    public static final int MSG_DELETED_AFTER_DAYS = 14;
    public static final int MSG_DELETED_AFTER_INTERVALS = NUM_OF_INTERVALS_IN_DAY * MSG_DELETED_AFTER_DAYS;
}
