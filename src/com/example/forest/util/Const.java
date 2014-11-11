package com.example.forest.util;

import com.example.forest.bean.Location;

public class Const {

    public static int NET_AVILIBALE = 0x111;
    public static int NET_UNAVILIBALE = 0x222;
    public static int NEW_MSG = 0x333;
    public static int RECEIVE_SUCCESS = 0x999;
    public static int NEW_LOCATION = 0x888;
    public static int LOGIN_SUC = 0x666;
    public static int LOGIN_FAIL = 0x444;
    public static int FIRESENDSUC = 0x555;
    public static int FIRESENDFAIL = 0x777;
    public static final int NEWVERSION = 0x123;
    public static final int DOWNFINISH = 0x321;
    public static final int UPDATE = 0x322;
    public static int NO_SERVER = 0x987;

    public static int SENDED = 0x567;
    public static int SHOWDIALOG = 0x678;
    public static int FINISH = 0x789;

    public static int CYCLE_CHANGED = 1;
    public static int CYCLE_NOCHANGED = 2;
    public static int CYCLE_INIT = 0;

    public static Location cur_location = new Location(0, 0);

    public static int FIREREPORT = 1; // 火情报告
    public static int PESTSREPORT = 2; // 病虫害报告
    public static int DEFORESTREPORT = 3; // 滥砍滥伐报告

    public static String FOOT = "P";
    public static String SOS = "S";
    public static String FIRE = "A";
    public static String PESTS = "B";
    public static String CUT = "C";

    public static String GPS_AVILIBALE = "A";
    public static String GPS_UNAVILIBALE = "V";
    public static String GPS_STATE = GPS_UNAVILIBALE;

}
