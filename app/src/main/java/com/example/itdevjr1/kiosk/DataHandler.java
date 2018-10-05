package com.example.itdevjr1.kiosk;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;


public class DataHandler {

    ////====== CREATING A user account =====//

    private static final String DATABASE_NAME           = "kiosk.db";
    private static final int DATABASE_VERSION           = BuildConfig.VERSION_CODE;
//    BuildConfig.VERSION_CODE

    public static final String TABLE_USER_ACCOUNT       = "usertbl";
    public static final String COLUMN_DATA              = "branch_supervisor_data";


    public static final String TABLE_IP_ADDRESS         = "ip_address_tbl";
    public static final String COLUMN_IP_ADDRESS        = "flag";

    public static final String TABLE_TOKEN              = "token_tbl";
    public static final String COLUMN_TOKEN             = "token";

    public static final String TABLE_BRANCH             = "branch_tbl";
    public static final String COLUMN_BRANCH_ARRAY      = "branches";

    public static final String TABLE_SERVICES           = "services_tbl";
    public static final String COLUMN_SERVICE_ARRAY     = "services";
    public static final String COLUMN_SERVICE_VERSION   = "services_version";

    public static final String TABLE_PRODUCTS           = "products_tbl";
    public static final String COLUMN_PRODUCTS_ARRAY    = "products";
    public static final String COLUMN_PRODUCTS_VERSION  = "products_version";

    public static final String TABLE_WAIVER             = "waiver_tbl";
    public static final String COLUMN_WAIVER_ARRAY      = "waiver_array";

    public static final String TABLE_ALARM              = "tbl_alarm";
    public static final String COLUMN_KIOSK_ALARM       = "alarm_status";

    public static final String TABLE_SCHEDULE           = "tbl_branch_schedule";
    public static final String COLUMN_SCHEDULE_ARRAY    = "array_schedule";

    public static final String TABLE_DEVICE                     = "tbl_device";
    public static final String COLUMN_DEVICE_CODE               = "device_code";
    public static final String COLUMN_DEVICE_IF_REGISTERED      = "device_if_registered";


    private static final String DATABASE_CREATE  = "CREATE TABLE IF NOT EXISTS " +
            TABLE_USER_ACCOUNT
            +"(" +
                COLUMN_DATA     + " text not null "
             +"); ";

    private static final String DATABASE_CREATE_IP = " CREATE TABLE IF NOT EXISTS " +
            TABLE_IP_ADDRESS
            +"("+
                COLUMN_IP_ADDRESS + " text not null"
            +"); ";

    private static final String DATABASE_CREATE_TOKEN  = "CREATE TABLE IF NOT EXISTS " +
            TABLE_TOKEN
            +"(" +
            COLUMN_TOKEN + " text not null "
            +"); ";

    private static final String DATABASE_CREATE_BRANCH = " CREATE TABLE IF NOT EXISTS " +
            TABLE_BRANCH
            +"("+
            COLUMN_BRANCH_ARRAY + " text not null"
            +"); ";

    private static final String DATABASE_CREATE_SERVICES = " CREATE TABLE IF NOT EXISTS " +
            TABLE_SERVICES
            +"("+
            COLUMN_SERVICE_ARRAY + " text not null"
            +"); ";

    private static final String DATABASE_CREATE_PRODUCTS = " CREATE TABLE IF NOT EXISTS " +
            TABLE_PRODUCTS
            +"("+
            COLUMN_PRODUCTS_ARRAY + " text not null"
            +"); ";

    private static final String DATABASE_CREATE_WAIVER = " CREATE TABLE IF NOT EXISTS " +
            TABLE_WAIVER
            +"("+
            COLUMN_WAIVER_ARRAY + " text not null"
            +"); ";

    private static final String DATABASE_CREATE_ALARM = " CREATE TABLE IF NOT EXISTS " +
            TABLE_ALARM
            +"("+
            COLUMN_KIOSK_ALARM + " text not null"
            +"); ";


    private static final String DATABASE_CREATE_SCHEDULE = " CREATE TABLE IF NOT EXISTS " +
            TABLE_SCHEDULE
            +"("+
            COLUMN_SCHEDULE_ARRAY + " text not null"
            +"); ";

    private static final String DATABASE_CREATE_DEVICE = " CREATE TABLE IF NOT EXISTS " +
            TABLE_DEVICE
            +"("+
            COLUMN_DEVICE_CODE          + " text not null, "+
            COLUMN_DEVICE_IF_REGISTERED + " text not null "
            +"); ";


    DataBaseHelper dbhelper;
    Context ctx;
    SQLiteDatabase db;

    public DataHandler(Context ctx) {
        this.ctx = ctx;
        dbhelper = new DataBaseHelper(ctx);
    }

    private static class DataBaseHelper extends SQLiteOpenHelper {

        public DataBaseHelper(Context ctx)
        {
            super(ctx,DATABASE_NAME,null,DATABASE_VERSION);
        }
        public void onCreate(SQLiteDatabase db) {
            Log.v("INFO1", "creating db");
            db.execSQL(DATABASE_CREATE);
            db.execSQL(DATABASE_CREATE_IP);
            db.execSQL(DATABASE_CREATE_TOKEN);
            db.execSQL(DATABASE_CREATE_BRANCH);
            db.execSQL(DATABASE_CREATE_SERVICES);
            db.execSQL(DATABASE_CREATE_PRODUCTS);
            db.execSQL(DATABASE_CREATE_WAIVER);
            db.execSQL(DATABASE_CREATE_ALARM);
            db.execSQL(DATABASE_CREATE_SCHEDULE);
            db.execSQL(DATABASE_CREATE_DEVICE);
        }

        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(DataHandler.class.getName(),
                    "Upgrading database from version " + oldVersion + " to "
                            + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_ACCOUNT);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_IP_ADDRESS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_TOKEN);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_BRANCH);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_PRODUCTS);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_WAIVER);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_ALARM);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVICES);
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICE);
            onCreate(db);
        }
    }

    public DataHandler open() {
        db = dbhelper.getWritableDatabase();
        return this;
    }

    public void close()
    {
        dbhelper.close();
    }

    //=======METHODS OF ACCOUNT==========//

    public long insertUserAccount(String arrayData) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DATA,arrayData);
        return db.insertOrThrow(TABLE_USER_ACCOUNT,null,values);
    }

    public Cursor returnUserAccount() {
        Cursor c = db.query(TABLE_USER_ACCOUNT,new String[] {
                COLUMN_DATA
        },null,null,null,null,null);
        return c;
    }

    public int deleteUserAccount() {
        return db.delete(TABLE_USER_ACCOUNT, null, null);
    }

    public long insertIpAddress(String ip_address) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_IP_ADDRESS,ip_address);
        return db.insertOrThrow(TABLE_IP_ADDRESS,null,values);
    }

    public Cursor returnIPAddress() {
        Cursor c = db.query(TABLE_IP_ADDRESS,new String[] {
                COLUMN_IP_ADDRESS
        },null,null,null,null,null);
        return c;
    }

    public int deleteIpAddress() {
        return db.delete(TABLE_IP_ADDRESS, null, null);
    }

    public long insertToken(String token) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_TOKEN,token);
        return db.insertOrThrow(TABLE_TOKEN,null,values);
    }

    public Cursor returnToken() {
        Cursor c = db.query(TABLE_TOKEN,new String[] {
                COLUMN_TOKEN
        },null,null,null,null,null);
        return c;
    }

    public int deleteToken () {
        return db.delete(TABLE_TOKEN, null, null);
    }


    public long insertBranch(String branchArray) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_BRANCH_ARRAY,branchArray);
        return db.insertOrThrow(TABLE_BRANCH,null,values);
    }

    public long updateBranch(String branchArray) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_BRANCH_ARRAY,branchArray);
        return db.insertOrThrow(TABLE_BRANCH,null,values);
    }

    public Cursor returnBranch() {
        Cursor c = db.query(TABLE_BRANCH,new String[] {
                COLUMN_BRANCH_ARRAY
        },null,null,null,null,null);
        return c;
    }

    public int deleteBranch() {
        return db.delete(TABLE_BRANCH, null, null);
    }


    public long insertService(String serviceArray) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SERVICE_ARRAY,serviceArray);
        return db.insertOrThrow(TABLE_SERVICES,null,values);
    }

    public Cursor returnService() {
        Cursor c = db.query(TABLE_SERVICES,new String[] {
                COLUMN_SERVICE_ARRAY
        },null,null,null,null,null);
        return c;
    }


    public int deleteService() {
        return db.delete(TABLE_SERVICES, null, null);
    }

    public long insertProduct(String productArray) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_PRODUCTS_ARRAY,productArray);
        return db.insertOrThrow(TABLE_PRODUCTS,null,values);
    }

    public Cursor returnProduct() {
        Cursor c = db.query(TABLE_PRODUCTS,new String[] {
                COLUMN_PRODUCTS_ARRAY
        },null,null,null,null,null);
        return c;
    }

    public int deleteProduct() {
        return db.delete(TABLE_PRODUCTS, null, null);
    }


    public long insertWaiver(String waiverArray) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_WAIVER_ARRAY,waiverArray);
        return db.insertOrThrow(TABLE_WAIVER,null,values);
    }

    public Cursor returnWaiver() {
        Cursor c = db.query(TABLE_WAIVER,new String[] {
                COLUMN_WAIVER_ARRAY
        },null,null,null,null,null);
        return c;
    }

    public int deleteWaiver() {
        return db.delete(TABLE_WAIVER, null, null);
    }


    public long insertAlarm(String alarmStatus) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_KIOSK_ALARM,alarmStatus);
        return db.insertOrThrow(TABLE_ALARM,null,values);
    }

    public long setAlarm(String alarmStatus) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_KIOSK_ALARM,alarmStatus);
        return db.update(TABLE_ALARM,values,null,null);
    }

    public Cursor getAlarmStatus() {
        Cursor c = db.query(TABLE_ALARM,new String[] {
                COLUMN_KIOSK_ALARM
        },null,null,null,null,null);
        return c;
    }

    public int deleteAlarm() {
        return db.delete(TABLE_ALARM, null, null);
    }


    public long insertSchedule(String arraySchedule) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDULE_ARRAY,arraySchedule);
        return db.insertOrThrow(TABLE_SCHEDULE,null,values);
    }

    public long updateSchedule(String arraySchedule) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_SCHEDULE_ARRAY,arraySchedule);
        return db.update(TABLE_SCHEDULE,values,null,null);
    }

    public Cursor returnSchedule() {
        Cursor c = db.query(TABLE_SCHEDULE,new String[] {
                COLUMN_SCHEDULE_ARRAY
        },null,null,null,null,null);
        return c;
    }

    public int deleteSchedule() {
        return db.delete(TABLE_SCHEDULE, null, null);
    }


    public long insertDeviceStatus(String deviceCode,String ifRegistered) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_CODE,deviceCode);
        values.put(COLUMN_DEVICE_IF_REGISTERED,ifRegistered);

        return db.insertOrThrow(TABLE_DEVICE,null,values);
    }

    public long updateDeviceStatus(String deviceCode,String ifRegistered) {
        ContentValues values = new ContentValues();
        values.put(COLUMN_DEVICE_CODE,deviceCode);
        values.put(COLUMN_DEVICE_IF_REGISTERED,ifRegistered);
        return db.update(TABLE_DEVICE,values,null,null);
    }

    public Cursor returnDeviceStatus() {
        Cursor c = db.query(TABLE_DEVICE,new String[] {
                COLUMN_DEVICE_CODE,COLUMN_DEVICE_IF_REGISTERED
        },null,null,null,null,null);
        return c;
    }

    public int deleteDeviceStatus() {
        return db.delete(TABLE_DEVICE, null, null);
    }




}
