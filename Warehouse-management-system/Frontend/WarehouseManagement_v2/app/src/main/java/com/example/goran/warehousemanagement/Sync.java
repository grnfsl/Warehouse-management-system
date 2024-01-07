package com.example.goran.warehousemanagement;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Sync {

    DatabaseHelper db;

    private String android_id;
    private String access_token;
    private String timestamp;
    private Context context;
    private int num_rows;
    private int num_operations;

    private String[] data = new String[7];

    Sync(Context context, String access_token){
        this.context = context;

        this.android_id = Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ANDROID_ID);
        this.access_token = access_token;

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        this.timestamp = sharedPref.getString("TIMESTAMP", "0");
        this.num_operations = sharedPref.getInt("NUM_OPERATIONS", 0);

        db = new DatabaseHelper(context);
        this.num_rows = (int) db.getProfilesCount("delta_products");
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt("NUM_OPERATIONS", num_operations + num_rows);
        editor.commit();

        this.num_operations += num_rows;

        Cursor res = db.getData("delta_products", "*", "");
        JSONArray jsonArray = new JSONArray();

        if(res.getCount() != 0) {
            while (res.moveToNext()) {
                JSONObject jo = new JSONObject();
                try {
                    jo.put("manufacture", res.getString(1));
                    jo.put("model", res.getString(2));
                    jo.put("price", res.getString(3));
                    jo.put("quantity", res.getString(4));
                    jo.put("size", res.getString(5));
                    jo.put("operation", res.getString(6));
                    jo.put("warehouse", res.getString(7));
                    jsonArray.put(jo);

                } catch (JSONException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }
//        System.out.println("table: " + jsonArray.toString());;
        JSONObject studentsObj = new JSONObject();

        try {
            studentsObj.put("delta_products_table ", jsonArray);
        } catch (JSONException e) {
            e.printStackTrace();
        }

//        String jsonStr = studentsObj.toString();
        String jsonStr = jsonArray.toString();

//        System.out.println("jsonString: "+jsonStr);

        data[0] = access_token;
        data[1] = android_id;
        data[2] = timestamp;
        data[3] = Integer.toString(num_rows);
        data[4] = Integer.toString(num_operations);
        data[5] = jsonStr;
        data[6] = "v2";

        new PostData(data).execute();
    }

    private class PostData extends AsyncTask<String, String, String> {

        private String[] userInfo;
        private String isValid;

        PostData(String[] userInfo) {
            this.userInfo = userInfo;
            this.isValid = "";
        }

        protected void onPreExecute() {
            super.onPreExecute();
            // do stuff before posting data
        }

        @Override
        protected String doInBackground(String... strings) {
            try {
                isValid = postText(userInfo);
                return isValid;
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return isValid;
        }

        @Override
        protected void onPostExecute(String lenghtOfFile) {
            System.out.println("table JSON" + isValid);
            JSONObject jObject = null;
            try {
                jObject = new JSONObject(isValid);
            }
            catch (JSONException e) {
                e.printStackTrace();
            }

            if (jObject.optString("error").equals("1")) {
                Toast.makeText(context, jObject.optString("message"), Toast.LENGTH_LONG).show();
            }
            else if (jObject.optString("error").equals("0")) {
                int num_rows_updated = jObject.optInt("num_rows_updated");
                if(num_rows_updated != 0) {
                    JSONArray json_array;
                    try {
                        json_array = jObject.getJSONArray("new_data");
                        SQLiteDatabase dbL = db.getWritableDatabase();

                        for (int i = 0; i < num_rows_updated; i++) {
                            JSONObject ajo = json_array.getJSONObject(i);
                            String op = ajo.optString("operation");
                            String table = ajo.optString("warehouse");
                            if (table.equals("gdansk")) {
                                if (op.equals("add")) {
                                    ContentValues contentVal = new ContentValues();
                                    contentVal.put(FeedReaderContract.GdasnkProducts.MANUFACTURE, ajo.optString("manufacture"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.MODEL, ajo.optString("model"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.PRICE, ajo.optString("price"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.QUANTITY, ajo.optString("quantity"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.SIZE, ajo.optString("size"));
                                    System.out.println("table error size " + ajo.optString("size"));
                                    dbL.insert("gdansk_products", null, contentVal);
                                } else if (op.equals("rmv")) {
                                    int num_rmv = dbL.delete("gdansk_products", FeedReaderContract.GdasnkProducts.MANUFACTURE + "=? AND " + FeedReaderContract.GdasnkProducts.MODEL + "=?", new String[]{ajo.optString("manufacture"), ajo.optString("model")});
                                } else if (op.equals("inc")) {
                                    Cursor res = db.getData("gdansk_products", "quantity", "WHERE " + FeedReaderContract.GdasnkProducts.MANUFACTURE + "='" + ajo.optString("manufacture") + "' AND " + FeedReaderContract.GdasnkProducts.MODEL + "='" + ajo.optString("model") + "'");

                                    if (res.getCount() == 0) {
                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
                                        continue;
                                    }
                                    res.moveToNext();

                                    int inDb = Integer.parseInt(res.getString(0));
                                    int newQuantity = Integer.parseInt(ajo.optString("quantity")) + inDb;

                                    ContentValues contentVal = new ContentValues();
                                    contentVal.put(FeedReaderContract.GdasnkProducts.MANUFACTURE, ajo.optString("manufacture"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.MODEL, ajo.optString("model"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.PRICE, ajo.optString("price"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.QUANTITY, Integer.toString(newQuantity));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.SIZE, ajo.optString("size"));

                                    dbL.update("gdansk_products", contentVal, FeedReaderContract.GdasnkProducts.MANUFACTURE + "=? AND " + FeedReaderContract.GdasnkProducts.MODEL + "=?", new String[]{ajo.optString("manufacture"), ajo.optString("model")});


                                } else if (op.equals("dec")) {
                                    Cursor res = db.getData("gdansk_products", "quantity", "WHERE " + FeedReaderContract.GdasnkProducts.MANUFACTURE + "='" + ajo.optString("manufacture") + "' AND " + FeedReaderContract.GdasnkProducts.MODEL + "='" + ajo.optString("model") + "'");

                                    if (res.getCount() == 0) {
                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
                                        continue;
                                    }
                                    res.moveToNext();

                                    int inDb = Integer.parseInt(res.getString(0));
                                    int newQuantity = inDb - Integer.parseInt(ajo.optString("quantity"));

                                    ContentValues contentVal = new ContentValues();
                                    contentVal.put(FeedReaderContract.GdasnkProducts.MANUFACTURE, ajo.optString("manufacture"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.MODEL, ajo.optString("model"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.PRICE, ajo.optString("price"));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.QUANTITY, Integer.toString(newQuantity));
                                    contentVal.put(FeedReaderContract.GdasnkProducts.SIZE, ajo.optString("size"));

                                    dbL.update("gdansk_products", contentVal, FeedReaderContract.GdasnkProducts.MANUFACTURE + "=? AND " + FeedReaderContract.GdasnkProducts.MODEL + "=?", new String[]{ajo.optString("manufacture"), ajo.optString("model")});
                                }
                            } else if (table.equals("warsaw")) {
                                if (op.equals("add")) {
                                    ContentValues contentVal = new ContentValues();
                                    contentVal.put(FeedReaderContract.WarsawProducts.MANUFACTURE, ajo.optString("manufacture"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.MODEL, ajo.optString("model"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.PRICE, ajo.optString("price"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.QUANTITY, ajo.optString("quantity"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.SIZE, ajo.optString("size"));

                                    dbL.insert("warsaw_products", null, contentVal);
                                } else if (op.equals("rmv")) {
                                    int num_rmv = dbL.delete("warsaw_products", FeedReaderContract.WarsawProducts.MANUFACTURE + "=? AND " + FeedReaderContract.WarsawProducts.MODEL + "=?", new String[]{ajo.optString("manufacture"), ajo.optString("model")});
                                } else if (op.equals("inc")) {
                                    Cursor res = db.getData("warsaw_products", "quantity", "WHERE " + FeedReaderContract.WarsawProducts.MANUFACTURE + "='" + ajo.optString("manufacture") + "' AND " + FeedReaderContract.WarsawProducts.MODEL + "='" + ajo.optString("model") + "'");

                                    if (res.getCount() == 0) {
                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
                                        continue;
                                    }
                                    res.moveToNext();

                                    int inDb = Integer.parseInt(res.getString(0));
                                    int newQuantity = Integer.parseInt(ajo.optString("quantity")) + inDb;

                                    ContentValues contentVal = new ContentValues();
                                    contentVal.put(FeedReaderContract.WarsawProducts.MANUFACTURE, ajo.optString("manufacture"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.MODEL, ajo.optString("model"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.PRICE, ajo.optString("price"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.QUANTITY, Integer.toString(newQuantity));
                                    contentVal.put(FeedReaderContract.WarsawProducts.SIZE, ajo.optString("size"));

                                    dbL.update("warsaw_products", contentVal, FeedReaderContract.WarsawProducts.MANUFACTURE + "=? AND " + FeedReaderContract.WarsawProducts.MODEL + "=?", new String[]{ajo.optString("manufacture"), ajo.optString("model")});


                                } else if (op.equals("dec")) {
                                    Cursor res = db.getData("warsaw_products", "quantity", "WHERE " + FeedReaderContract.WarsawProducts.MANUFACTURE + "='" + ajo.optString("manufacture") + "' AND " + FeedReaderContract.WarsawProducts.MODEL + "='" + ajo.optString("model") + "'");

                                    if (res.getCount() == 0) {
                                        Toast.makeText(context, "Something went wrong", Toast.LENGTH_LONG).show();
                                        continue;
                                    }
                                    res.moveToNext();

                                    int inDb = Integer.parseInt(res.getString(0));
                                    int newQuantity = inDb - Integer.parseInt(ajo.optString("quantity"));

                                    ContentValues contentVal = new ContentValues();
                                    contentVal.put(FeedReaderContract.WarsawProducts.MANUFACTURE, ajo.optString("manufacture"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.MODEL, ajo.optString("model"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.PRICE, ajo.optString("price"));
                                    contentVal.put(FeedReaderContract.WarsawProducts.QUANTITY, Integer.toString(newQuantity));
                                    contentVal.put(FeedReaderContract.WarsawProducts.SIZE, ajo.optString("size"));

                                    dbL.update("warsaw_products", contentVal, FeedReaderContract.WarsawProducts.MANUFACTURE + "=? AND " + FeedReaderContract.WarsawProducts.MODEL + "=?", new String[]{ajo.optString("manufacture"), ajo.optString("model")});
                                }
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                timestamp = jObject.optString("server_timestamp");

                Toast.makeText(context, jObject.optString("message"), Toast.LENGTH_LONG).show();

                SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("TIMESTAMP", timestamp);
                editor.commit();

                SQLiteDatabase dbL = db.getWritableDatabase();
                int num_rows_affected = dbL.delete("delta_products", "", null);

//                Toast.makeText(context, num_rows_affected+" deleted from lacal db", Toast.LENGTH_LONG).show();

                //print local tables
                MainActivity.print_tables(context);
            }
            else {
                Toast.makeText(context, "Nothing happen!!!", Toast.LENGTH_LONG).show();
            }
        }

        // this will post our id token to backend server
        private String postText(String[] userInfo) {
            try {
                String postReceiverUrl = "http://172.18.0.1/sync.php";

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(postReceiverUrl);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("access_token", userInfo[0]));
                nameValuePairs.add(new BasicNameValuePair("android_id", userInfo[1]));
                nameValuePairs.add(new BasicNameValuePair("timestamp", userInfo[2]));
                nameValuePairs.add(new BasicNameValuePair("num_rows", userInfo[3]));
                nameValuePairs.add(new BasicNameValuePair("num_operations", userInfo[4]));
                nameValuePairs.add(new BasicNameValuePair("data", userInfo[5]));
                nameValuePairs.add(new BasicNameValuePair("version", userInfo[6]));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity resEntity = response.getEntity();

                if (resEntity != null) {
                    String responseStr = EntityUtils.toString(resEntity).trim();
                    Log.v("2", "table: " + responseStr);
                    //handleResponse(responseStr);
                    return responseStr;
                }

            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
