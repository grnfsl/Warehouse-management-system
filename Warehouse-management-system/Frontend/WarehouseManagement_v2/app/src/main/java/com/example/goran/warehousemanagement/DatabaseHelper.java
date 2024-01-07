package com.example.goran.warehousemanagement;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "warehouse.db";

    private static Context contextDB;

    private static final String SQL_CREATE_ENTRIES_DELTA_PRODUCTS =
            "CREATE TABLE " + FeedReaderContract.DeltaProducts.TABLE_NAME + " (" +
                    FeedReaderContract.DeltaProducts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedReaderContract.DeltaProducts.MANUFACTURE + " TEXT," +
                    FeedReaderContract.DeltaProducts.MODEL + " TEXT," +
                    FeedReaderContract.DeltaProducts.PRICE + " DOUBLE," +
                    FeedReaderContract.DeltaProducts.QUANTITY + " INTEGER," +
                    FeedReaderContract.DeltaProducts.SIZE + " TEXT," +
                    FeedReaderContract.DeltaProducts.OPERATION + " TEXT," +
                    FeedReaderContract.DeltaProducts.WAREHOUSE + " TEXT)";

    private static final String SQL_CREATE_ENTRIES_DELTA_PRODUCTS_TEMP =
            "CREATE TABLE " + FeedReaderContract.DeltaProductsTemp.TABLE_NAME + " (" +
                    FeedReaderContract.DeltaProductsTemp._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedReaderContract.DeltaProductsTemp.MANUFACTURE + " TEXT," +
                    FeedReaderContract.DeltaProductsTemp.MODEL + " TEXT," +
                    FeedReaderContract.DeltaProductsTemp.PRICE + " DOUBLE," +
                    FeedReaderContract.DeltaProductsTemp.QUANTITY + " INTEGER," +
                    FeedReaderContract.DeltaProductsTemp.SIZE + " TEXT," +
                    FeedReaderContract.DeltaProductsTemp.OPERATION + " TEXT," +
                    FeedReaderContract.DeltaProductsTemp.WAREHOUSE + " TEXT)";

//    private static final String SQL_CREATE_ENTRIES_DELTA_PRODUCTS =
//            "CREATE TABLE " + FeedReaderContract.DeltaProducts.TABLE_NAME + " (" +
//                    FeedReaderContract.DeltaProducts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
//                    FeedReaderContract.DeltaProducts.MANUFACTURE + " TEXT," +
//                    FeedReaderContract.DeltaProducts.MODEL + " TEXT," +
//                    FeedReaderContract.DeltaProducts.PRICE + " DOUBLE," +
//                    FeedReaderContract.DeltaProducts.QUANTITY + " INTEGER," +
//                    FeedReaderContract.DeltaProducts.OPERATION + " TEXT)";

    private static final String SQL_CREATE_ENTRIES_PRODUCTS =
            "CREATE TABLE " + FeedReaderContract.Products.TABLE_NAME + " (" +
                    FeedReaderContract.Products._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedReaderContract.Products.MANUFACTURE + " TEXT," +
                    FeedReaderContract.Products.MODEL + " TEXT," +
                    FeedReaderContract.Products.PRICE + " DOUBLE," +
                    FeedReaderContract.Products.QUANTITY + " INTEGER)";

    private static final String SQL_CREATE_ENTRIES_GDANSK_PRODUCTS =
            "CREATE TABLE " + FeedReaderContract.GdasnkProducts.TABLE_NAME + " (" +
                    FeedReaderContract.GdasnkProducts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedReaderContract.GdasnkProducts.MANUFACTURE + " TEXT," +
                    FeedReaderContract.GdasnkProducts.MODEL + " TEXT," +
                    FeedReaderContract.GdasnkProducts.PRICE + " DOUBLE," +
                    FeedReaderContract.GdasnkProducts.QUANTITY + " INTEGER," +
                    FeedReaderContract.GdasnkProducts.SIZE + " TEXT)";

    private static final String SQL_CREATE_ENTRIES_WARSAW_PRODUCTS =
            "CREATE TABLE " + FeedReaderContract.WarsawProducts.TABLE_NAME + " (" +
                    FeedReaderContract.WarsawProducts._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    FeedReaderContract.WarsawProducts.MANUFACTURE + " TEXT," +
                    FeedReaderContract.WarsawProducts.MODEL + " TEXT," +
                    FeedReaderContract.WarsawProducts.PRICE + " DOUBLE," +
                    FeedReaderContract.WarsawProducts.QUANTITY + " INTEGER," +
                    FeedReaderContract.WarsawProducts.SIZE + " TEXT)";

    private static final String SQL_DELETE_ENTRIES_DELTA_PRODUCTS =
            "DROP TABLE IF EXISTS " + FeedReaderContract.DeltaProducts.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES_DELTA_PRODUCTS_TEMP =
            "DROP TABLE IF EXISTS " + FeedReaderContract.DeltaProductsTemp.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES_GDANSK_PRODUCTS_OLD =
            "DROP TABLE IF EXISTS products";

    private static final String SQL_DELETE_ENTRIES_GDANSK_PRODUCTS =
            "DROP TABLE IF EXISTS " + FeedReaderContract.GdasnkProducts.TABLE_NAME;

    private static final String SQL_DELETE_ENTRIES_WARSAW_PRODUCTS =
            "DROP TABLE IF EXISTS " + FeedReaderContract.WarsawProducts.TABLE_NAME;

    private static final String SQL_MIGRATE_OLD_DELTA_TO_TEMP =
        "INSERT INTO " + FeedReaderContract.DeltaProductsTemp.TABLE_NAME +
                " SELECT t1._id, t1.manufacturer_name, t1.model_name, t1.price, t1.quantity, 'none', t1.op, 'gdansk' FROM delta_products t1";

    private static final String SQL_MIGRATE_TEMP_TO_DELTA =
            "INSERT INTO " + FeedReaderContract.DeltaProducts.TABLE_NAME +
                    " SELECT t1.* FROM "+ FeedReaderContract.DeltaProductsTemp.TABLE_NAME +" t1";

    private static final String SQL_MIGRATE_PRODUCTS_TO_GDANSK_PRODUCTS =
            "INSERT INTO " + FeedReaderContract.GdasnkProducts.TABLE_NAME +
                    " SELECT t1._id, t1.manufacturer_name, t1.model_name, t1.price, t1.quantity, 'none' FROM products t1";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        contextDB = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db){
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(contextDB);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("TIMESTAMP", "0");
        editor.putInt("NUM_OPERATIONS", 0);
        editor.commit();

        db.execSQL(SQL_DELETE_ENTRIES_DELTA_PRODUCTS);
        db.execSQL(SQL_DELETE_ENTRIES_GDANSK_PRODUCTS);
        db.execSQL(SQL_DELETE_ENTRIES_WARSAW_PRODUCTS);
        db.execSQL(SQL_DELETE_ENTRIES_GDANSK_PRODUCTS_OLD);

        db.execSQL(SQL_CREATE_ENTRIES_DELTA_PRODUCTS);
        db.execSQL(SQL_CREATE_ENTRIES_GDANSK_PRODUCTS);
        db.execSQL(SQL_CREATE_ENTRIES_WARSAW_PRODUCTS);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over

        //Migrate delta_products table
        db.execSQL(SQL_DELETE_ENTRIES_DELTA_PRODUCTS_TEMP);
        db.execSQL(SQL_CREATE_ENTRIES_DELTA_PRODUCTS_TEMP);
        db.execSQL(SQL_MIGRATE_OLD_DELTA_TO_TEMP);
        db.execSQL(SQL_DELETE_ENTRIES_DELTA_PRODUCTS);
        db.execSQL(SQL_CREATE_ENTRIES_DELTA_PRODUCTS);
        db.execSQL(SQL_MIGRATE_TEMP_TO_DELTA);
        db.execSQL(SQL_DELETE_ENTRIES_DELTA_PRODUCTS_TEMP);

        db.execSQL(SQL_DELETE_ENTRIES_GDANSK_PRODUCTS);
        db.execSQL(SQL_DELETE_ENTRIES_WARSAW_PRODUCTS);
        db.execSQL(SQL_CREATE_ENTRIES_GDANSK_PRODUCTS);
        db.execSQL(SQL_CREATE_ENTRIES_WARSAW_PRODUCTS);

        //migrate products table to gdansk_products
        db.execSQL(SQL_MIGRATE_PRODUCTS_TO_GDANSK_PRODUCTS);
        db.execSQL(SQL_DELETE_ENTRIES_GDANSK_PRODUCTS_OLD);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public boolean insertData(String[] values, String tableName){
        SQLiteDatabase db = this.getWritableDatabase();

        if(!values[6].equals("add")){
            Cursor res1;
            if(values[7].equals("gdansk"))
                res1 = getData("gdansk_products", FeedReaderContract.GdasnkProducts._ID, "WHERE "+FeedReaderContract.GdasnkProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.GdasnkProducts.MODEL + "='"+ values[3]+"'");
            else if(values[7].equals("warsaw"))
                res1 = getData("warsaw_products", FeedReaderContract.WarsawProducts._ID, "WHERE "+FeedReaderContract.WarsawProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.WarsawProducts.MODEL + "='"+ values[3]+"'");
            else return false;

            if(res1.getCount() == 0){
                System.out.println("table what");
                if(values[7].equals("gdansk"))
                    res1 = getData("delta_products", FeedReaderContract.GdasnkProducts._ID, "WHERE "+FeedReaderContract.DeltaProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.DeltaProducts.MODEL + "='"+ values[3]+"'AND "+FeedReaderContract.DeltaProducts.WAREHOUSE+"='"+values[7]+"'");
                else if(values[7].equals("warsaw"))
                    res1 = getData("delta_products", FeedReaderContract.WarsawProducts._ID, "WHERE "+FeedReaderContract.DeltaProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.DeltaProducts.MODEL + "='"+ values[3]+"'AND "+FeedReaderContract.DeltaProducts.WAREHOUSE+"='"+values[7]+"'");
                else return false;
//                res1 = getData("delta_products", FeedReaderContract.GdasnkProducts._ID, "WHERE "+FeedReaderContract.DeltaProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.DeltaProducts.MODEL + "='"+ values[3]+"'");
                if(res1.getCount()==0){
                    return false;
                }
                else {
                    if(values[6].equals("rmv")){
                        deleteData(values, tableName);
                        return true;
                    }
                    else {
                        //for increase and decrease, operation should be 'add' because the item does not exist in warehouses
                        return updateData( values, tableName, values[6], "add");
                    }
                }
            }
        }

        System.out.println("table what1");

//        if(values[6].equals("inc") || values[6].equals("dec")){
//            Cursor res;
//            if(values[7].equals("gdansk"))
//                res = getData("delta_products", FeedReaderContract.GdasnkProducts._ID, "WHERE "+FeedReaderContract.DeltaProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.DeltaProducts.MODEL + "='"+ values[3]+"'AND "+FeedReaderContract.DeltaProducts.WAREHOUSE+"='"+values[7]+"' AND "+FeedReaderContract.DeltaProducts.OPERATION+"<>'rmv'");
//            else if(values[7].equals("warsaw"))
//                res = getData("delta_products", FeedReaderContract.WarsawProducts._ID, "WHERE "+FeedReaderContract.DeltaProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.DeltaProducts.MODEL + "='"+ values[3]+"'AND "+FeedReaderContract.DeltaProducts.WAREHOUSE+"='"+values[7]+"' AND "+FeedReaderContract.DeltaProducts.OPERATION+"<>'rmv'");
//            else return false;
//
//            if(res.getCount()==0){
//                return false;
//            }
//        }

        if(values[4]==null || values[5]==null || values[6]==null || values[8]==null){
            Cursor res;
            if(values[7].equals("gdansk"))
                res = getData("gdansk_products", "price, quantity, size", "WHERE "+FeedReaderContract.GdasnkProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.GdasnkProducts.MODEL + "='"+ values[3]+"'");
            else if(values[7].equals("warsaw"))
                res = getData("warsaw_products", "price, quantity, size", "WHERE "+FeedReaderContract.WarsawProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.WarsawProducts.MODEL + "='"+ values[3]+"'");
            else return false;

            if(res.getCount()==0) return false;
            res.moveToNext();

            values[4] = res.getString(0);
            if(values[6].equals("rmv"))
                values[5] = res.getString(1);
            values[8] = res.getString(2);
        }

        ContentValues contentVal = new ContentValues();
        contentVal.put(FeedReaderContract.DeltaProducts.MANUFACTURE, values[2]);
        contentVal.put(FeedReaderContract.DeltaProducts.MODEL, values[3]);
        contentVal.put(FeedReaderContract.DeltaProducts.PRICE, values[4]);
        contentVal.put(FeedReaderContract.DeltaProducts.QUANTITY, values[5]);
        contentVal.put(FeedReaderContract.DeltaProducts.OPERATION, values[6]);
        contentVal.put(FeedReaderContract.DeltaProducts.WAREHOUSE, values[7]);
        contentVal.put(FeedReaderContract.DeltaProducts.SIZE, values[8]);

        long newRowId = db.insert(tableName, null, contentVal);

        if (newRowId != -1)
            return true;
        else
            return false;
    }

    public Cursor getData(String tableName, String select, String condition){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = db.rawQuery("SELECT "+select+" FROM " + tableName +" "+ condition, null);
        return res;
    }

    public boolean updateData(String[] values, String tableName, String op, String op1){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = getData(tableName, "price, quantity, op, size", "WHERE "+FeedReaderContract.DeltaProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.DeltaProducts.MODEL + "='"+ values[3]+"' AND "+FeedReaderContract.DeltaProducts.WAREHOUSE+"='"+values[7]+"'");

        if(res.getCount()==0) return false;
        res.moveToNext();

        int inDb = Integer.parseInt(res.getString(1));
        int reqI = Integer.parseInt(values[5]);

        if (op == "inc")
            reqI += inDb;
        else if (op == "dec") {
            reqI = inDb - reqI;
            if(reqI<0)
                return false;
        }
        else return false;

        values[4] = res.getString(0);
        values[5] = Integer.toString(reqI);
        values[8] = res.getString(3);

        ContentValues contentVal = new ContentValues();
        contentVal.put(FeedReaderContract.DeltaProducts.MANUFACTURE, values[2]);
        contentVal.put(FeedReaderContract.DeltaProducts.MODEL, values[3]);
        contentVal.put(FeedReaderContract.DeltaProducts.PRICE, values[4]);
        contentVal.put(FeedReaderContract.DeltaProducts.QUANTITY, values[5]);
        contentVal.put(FeedReaderContract.DeltaProducts.OPERATION, op1);
        contentVal.put(FeedReaderContract.DeltaProducts.WAREHOUSE, values[7]);

        db.update(tableName, contentVal, FeedReaderContract.DeltaProducts.MANUFACTURE+"=? AND "+FeedReaderContract.DeltaProducts.MODEL+"=?", new String[] {values[2], values[3]} );
        return true;
    }

    public int deleteData(String[] values, String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(tableName, FeedReaderContract.DeltaProducts.MANUFACTURE+"=? AND "+FeedReaderContract.DeltaProducts.MODEL+"=?", new String[] {values[2], values[3]});
    }

    public boolean markRemove(String[] values, String tableName){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor res = getData(tableName, "price, quantity, op", "WHERE "+FeedReaderContract.DeltaProducts.MANUFACTURE+"='"+ values[2] +"' AND "+FeedReaderContract.DeltaProducts.MODEL + "='"+ values[3]+"'");

        if(res.getCount()==0) return false;
        res.moveToNext();

        values[4] = res.getString(0);
        values[5] = res.getString(1);

//        if(res.getString(2).equals("0") && res.getString(3).equals("remove"))
//        deleteData(values, "delta_products");

        ContentValues contentVal = new ContentValues();
        contentVal.put(FeedReaderContract.DeltaProducts.MANUFACTURE, values[2]);
        contentVal.put(FeedReaderContract.DeltaProducts.MODEL, values[3]);
        contentVal.put(FeedReaderContract.DeltaProducts.PRICE, values[4]);
        contentVal.put(FeedReaderContract.DeltaProducts.QUANTITY, values[5]);
        contentVal.put(FeedReaderContract.DeltaProducts.OPERATION, values[6]);
        contentVal.put(FeedReaderContract.DeltaProducts.WAREHOUSE, values[7]);
        contentVal.put(FeedReaderContract.DeltaProducts.SIZE, values[8]);

//        db.update(tableName, contentVal, FeedReaderContract.DeltaProducts.MANUFACTURE+"=? AND "+FeedReaderContract.DeltaProducts.MODEL+"=?", new String[] {values[2], values[3]} );
        return true;
    }

    public long getProfilesCount(String tablename) {
        SQLiteDatabase db = this.getReadableDatabase();
        long count = DatabaseUtils.queryNumEntries(db, tablename);
        db.close();
        return count;
    }
}
