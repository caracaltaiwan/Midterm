package com.example.midterm;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.List;

/**
 * Created by 重夢 on 2017/3/23.
 */

public class SQLite {

    //<editor-fold desc="變數名稱">
    //資料庫表單名稱
    public static String[]
            /*最多創建10個表單*/
            TABLE_NAME = new String[10] ;//TABLE_NAME  = {"Student_Info"};

    //表單欄位名稱
    public static final String
            TABLE_COLUMN_ID = "_ID",
            TABLE_COLUMN_NAME = "NAME",
            TABLE_COLUMN_LOCATION = "LOCATION",
            TABLE_COLUMN_TYPE = "TYPE-SERVER",
            TABLE_COLUMN_DISTRICTSEL = "DISTRICTSEL",
            TABLE_COLUMN_ADDRESS = "ADDRESS",
            TABLE_COLUMN_CHARGE = "CHARGE",
            TABLE_COLUMN_PHONE_NUMBER = "PHONE_NUMBER",
            TABLE_COLUMN_PHONE_NUMBER_SECOND = "PHONE_NUMBER_SECOND";

    // 使用上面宣告的變數建立表格的SQL指令
    public String
            CREATE_TABLE =
            "Create Table " +//SQL指令 記得空格
                    "name_location" + "(" +//表單名稱
                    TABLE_COLUMN_ID + " INTEGER PRIMARY KEY autoincrement," +
                    TABLE_COLUMN_NAME + " CHAR(20)," +
                    TABLE_COLUMN_PHONE_NUMBER + " CHAR(20));",
            CREATE_TABLE_TEST =
            "Create Table " +//SQL指令 記得空格
                    "name_location" + "(" +//表單名稱
                    TABLE_COLUMN_ID + " INTEGER PRIMARY KEY autoincrement," +
                    TABLE_COLUMN_LOCATION + " CHAR(20)," +
                    TABLE_COLUMN_TYPE + " CHAR(20)," +
                    TABLE_COLUMN_DISTRICTSEL + " CHAR(20)," +
                    TABLE_COLUMN_ADDRESS + " CHAR(20)," +
                    TABLE_COLUMN_CHARGE + " CHAR(20)," +
                    TABLE_COLUMN_PHONE_NUMBER + " CHAR(20));" +
                    TABLE_COLUMN_PHONE_NUMBER_SECOND + " CHAR(20));";
    
    //表格名稱列表
    public List<String>
            Table_name;

    // 資料庫物件
    SQLiteDatabase db;

    //控制器暫存
    Context ct;

    //</editor-fold>

    // 建構子，一般的應用都不需要修改
    public SQLite(Context context, SQLiteOH sqLiteOH){
        db = sqLiteOH.getDatabase(context);
        ct=context;
        //Table_name.add("test");
        //String a= Table_name.get(0);
    }

    // 關閉資料庫，一般的應用都不需要修改
    public void close(){
        db.close();
    }

    //創建表單
    public boolean createTable(int table_number,String table_name){/*不安全*/
        TABLE_NAME[table_number] = table_name;
        String sql;
        try {
            rewrite(TABLE_NAME[table_number]);//呼叫重寫SQL的指令
            sql = CREATE_TABLE;
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    //重置表單
    public boolean dropTable(){
        String sql = "";
        try {
            sql = "drop TABLE "+TABLE_NAME[0];
            db.execSQL(sql);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    // 新增參數指定的物件
    public boolean insertTable(int table_number,String database_name) {/*不安全*/
        String sql = "";
        try {
            sql = "insert into " + TABLE_NAME[table_number] + " values(null,'" + database_name + "','TestData')";
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    public boolean insertTable(int table_number,String database_name,String database_phone) {/*不安全*/
        String sql = "";
        try {
            sql = "insert into " + TABLE_NAME[table_number] + " values(null,'" + database_name + "','" + database_phone +"')";
            db.execSQL(sql);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //呼叫重寫SQL的指令
    private void rewrite(String table_name) {//可以設為void 多設一個string 是為了防止出錯 /*不安全*/
        //"Create Table Student_info(_ID INTEGER PRIMARY KEY autoincrement,NAME CHAR(20),PHONE_NUMBER CHAR(20));)"
        try {//防止無法預期的錯誤
            CREATE_TABLE =
                    "Create Table " +//SQL指令 記得空格
                            table_name + "(" +//表單名稱
                            TABLE_COLUMN_ID + " INTEGER PRIMARY KEY autoincrement," +
                            TABLE_COLUMN_NAME + " CHAR(20)," +
                            TABLE_COLUMN_PHONE_NUMBER + " CHAR(20));";
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //輸出第一個表單(須改輸出格式)
    public String show1(){
        StringBuffer sf = new StringBuffer();
        Cursor cursor = null;//SELECT * from Student_Info
        try {
            cursor = db.rawQuery("SELECT * from "+TABLE_NAME[0], null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //<editor-fold desc="印出手機電話號碼 -- 如要引用記得刪掉">
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            try {
                sf.append(cursor.getInt(0)).append(":").append(cursor.getString(1)).append(":").append(cursor.getString(2));
            } catch (Exception e) {
                e.printStackTrace();
            }
            sf.append("\n");
            cursor.moveToNext();
        }
        //</editor-fold>
        return sf.toString();
    }
}
