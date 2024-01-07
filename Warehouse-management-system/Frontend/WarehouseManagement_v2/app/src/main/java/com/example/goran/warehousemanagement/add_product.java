package com.example.goran.warehousemanagement;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class add_product extends AppCompatActivity implements View.OnClickListener{

    DatabaseHelper db;

    private String accessToken;
    final static private String operation = "add";
    private String[] userInfo = new String[9];

    Spinner spinner;
    private EditText manufacturer;
    private EditText model;
    private EditText price;
    private EditText quantity;
    private EditText size;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product_activity);

        spinner = (Spinner) findViewById(R.id.warehouses);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.warehouses, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);

        SpinnerActivity spinnerListener = new SpinnerActivity();
        spinner.setOnItemSelectedListener(spinnerListener);

        manufacturer = (EditText) findViewById(R.id.manufacturer);
        model = (EditText) findViewById(R.id.model);
        price = (EditText) findViewById(R.id.price);
        quantity = (EditText) findViewById(R.id.quantity);
        size = (EditText) findViewById(R.id.size);

        accessToken = getIntent().getStringExtra("access_token");

        findViewById(R.id.addB).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.addB:
                submit();
                break;
        }
    }

    private void submit(){
        userInfo[0] = accessToken;
        userInfo[1] = operation;
        userInfo[2] = manufacturer.getText().toString();
        userInfo[3] = model.getText().toString();
        userInfo[4] = price.getText().toString();
        userInfo[5] = quantity.getText().toString();
        userInfo[6] = "add";
        userInfo[7] = spinner.getSelectedItem().toString().toLowerCase();
        userInfo[8] = size.getText().toString();

        //offline
        db = new DatabaseHelper(this);
        boolean isAdded = db.insertData(userInfo, "delta_products");

        if(isAdded) {
            Toast.makeText(this, "Product add added successfully", Toast.LENGTH_LONG).show();
        }
        else
            Toast.makeText(this, "Product add add failed", Toast.LENGTH_LONG).show();

        //print local tables
        MainActivity.print_tables(getApplicationContext());

        finish();
        //online
//        new PostIDToken(userInfo).execute();
    }

    private class PostIDToken extends AsyncTask<String, String, String> {

        private String[] userInfo;
        private String isValid;

        PostIDToken(String[] userInfo) {
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
        }

        // this will post our id token to backend server
        private String postText(String[] userInfo) {
            try {
                String postReceiverUrl = "http://172.18.0.1/product_operations.php";

                HttpClient httpClient = new DefaultHttpClient();
                HttpPost httpPost = new HttpPost(postReceiverUrl);

                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
                nameValuePairs.add(new BasicNameValuePair("access_token", userInfo[0]));
                nameValuePairs.add(new BasicNameValuePair("operation", userInfo[1]));
                nameValuePairs.add(new BasicNameValuePair("manufacturer", userInfo[2]));
                nameValuePairs.add(new BasicNameValuePair("model", userInfo[3]));
                nameValuePairs.add(new BasicNameValuePair("price", userInfo[4]));
                nameValuePairs.add(new BasicNameValuePair("quantity", userInfo[5]));

                httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

                HttpResponse response = httpClient.execute(httpPost);
                HttpEntity resEntity = response.getEntity();

                if (resEntity != null) {

                    String responseStr = EntityUtils.toString(resEntity).trim();
                    Log.v("2", "Response: " + responseStr);
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
    public class SpinnerActivity extends Activity implements AdapterView.OnItemSelectedListener {
        public void onItemSelected(AdapterView<?> parent, View view,
                                   int pos, long id) {
            System.out.println("table " + parent.getItemAtPosition(pos));
            // An item was selected. You can retrieve the selected item using
            // parent.getItemAtPosition(pos)
        }

        public void onNothingSelected(AdapterView<?> parent) {
            // Another interface callback
        }
    }
}
