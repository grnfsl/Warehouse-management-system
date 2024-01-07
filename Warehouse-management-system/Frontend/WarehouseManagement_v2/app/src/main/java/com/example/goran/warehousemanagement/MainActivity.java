package com.example.goran.warehousemanagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.EditText;
import android.view.View;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

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
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    DatabaseHelper db;

    private static final String TAG = "MainActivity";
    private static final int RC_GET_TOKEN = 9002;

    private CharSequence textMain = "Enter username and password";
    private String accessToken;
    private String scope;
    private TextView userText;
    private TextView passwordText;

    private EditText userEdit;
    private EditText passwordEdit;

    private TextView mStatusTextView;
    private GoogleSignInClient mGoogleSignInClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        db = new DatabaseHelper(this);
        SQLiteDatabase db1 = db.getWritableDatabase();

        //fresh installing
//        db.onCreate(db1);

        //upgrade the app
//        db.onUpgrade(db1, 1, 2);

//        System.out.println("table: " + FeedReaderContract.GdasnkProducts._ID);
        //print local tables


//        db1.delete("delta_products", "", null);
//        db1.delete("gdansk_products", "", null);
//        db1.delete("warsaw_products", "", null);
//
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
//        SharedPreferences.Editor editor = sharedPreferences.edit();
//        editor.putString("TIMESTAMP", "0");
//        editor.putInt("NUM_OPERATIONS", 0);
//        editor.commit();

        MainActivity.print_tables(getApplicationContext());

//        LoadPreferences();

//        File dbFile = this.getDatabasePath("warehouse.db");
//        if(dbFile.exists()){
//            System.out.println("hello "+ true + " " + dbFile.getAbsolutePath().toString());
//        }
//        else {
//            System.out.println("hello "+ false);
//        }

        // Configure sign-in to request the user's ID, email address, and basic
        // profile. ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestIdToken(getString(R.string.server_client_id))
                .build();

        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        userEdit = (EditText) findViewById(R.id.userEditID);
        passwordEdit = (EditText) findViewById(R.id.passwordEditID);

        findViewById(R.id.loginID).setOnClickListener(this);
        findViewById(R.id.sign_in_button).setOnClickListener(this);
        findViewById(R.id.sign_out_button).setOnClickListener(this);
        findViewById(R.id.disconnect_button).setOnClickListener(this);
        findViewById(R.id.refresh).setOnClickListener(this);

        Context context = getApplicationContext();

        int duration = Toast.LENGTH_SHORT;

        Toast toast = Toast.makeText(context, textMain, duration);
        toast.show();
        //String s = getIntent().getStringExtra("state");
        //if(s.equals("logout"))
          //  signOut();
        //i.putExtra("state", "logout");
    }

    @Override
    protected void onStart(){
        super.onStart();

    }
    @Override
     public void onBackPressed() {
        super.onBackPressed();
        finish();

    }
    private void getIdToken() {
        // Show an account picker to let the user choose a Google account from the device.
        // If the GoogleSignInOptions only asks for IDToken and/or profile and/or email then no
        // consent screen will be shown here.
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GET_TOKEN);
    }
    private void refreshIdToken() {
        // Attempt to silently refresh the GoogleSignInAccount. If the GoogleSignInAccount
        // already has a valid token this method may complete immediately.
        //
        // If the user has not previously signed in on this device or the sign-in has expired,
        // this asynchronous branch will attempt to sign in the user silently and get a valid
        // ID token. Cross-device single sign on will occur in this branch.
        mGoogleSignInClient.silentSignIn()
                .addOnCompleteListener(this, new OnCompleteListener<GoogleSignInAccount>() {
                    @Override
                    public void onComplete(@NonNull Task<GoogleSignInAccount> task) {
                        handleSignInResult(task);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.loginID:
                login();
                break;
            case R.id.sign_in_button:
                getIdToken();
                break;
            case R.id.sign_out_button:
                signOut();
                break;
            case R.id.disconnect_button:
                revokeAccess();
                break;
            case R.id.refresh:
                refreshIdToken();
                break;

        }
    }

    private void login(){
        String[] userInfo = new String[2];
        userInfo[0] = userEdit.getText().toString();
        userInfo[1] = passwordEdit.getText().toString();
        Log.v(TAG, "warehouse user name: "+userInfo[0]);
        Log.v(TAG, "warehouse user pass: "+userInfo[1]);
        new PostIDToken1(userInfo).execute();
    }

    private void SavePreferences(String key, String value, String type) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.commit();
    }

    private void LoadPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String timestamp = sharedPreferences.getString("TIMESTAMP", "");
        System.out.println("timestamp: " + timestamp);
    }

    private class PostIDToken1 extends AsyncTask<String, String, String> {

        private String[] userInfo;
        private String isValid;

        PostIDToken1(String[] userInfo){
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
                isValid = postText1(userInfo);
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
            if(isValid.equals("user_does_not_exist") || isValid.equals("failed_validation") || isValid.equals("failed_token_request")) {
                Toast.makeText(getApplicationContext(), isValid, Toast.LENGTH_LONG).show();
                textMain = "Username or/and password wrong";
                updateUI(false);
                signOut();
            }
            else {
                JSONObject jObject = null;
                try {
                    jObject = new JSONObject(isValid);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                accessToken=jObject.optString("accessToken");
                scope=jObject.optString("scope");

                if(scope.equals("manager")) {
                    Intent i = new Intent(MainActivity.this, product_utilities.class);
                    i.putExtra("access_token", accessToken);
                    startActivity(i);
                }
                else if(scope.equals("employee")){
                    Intent i = new Intent(MainActivity.this, e_product_utilities.class);
                    i.putExtra("access_token", accessToken);
                    startActivity(i);
                }
                updateUI(true);
            }
            // do stuff after posting data
        }
    }

    // this will post our id token to backend server
    private String postText1(String[] userInfo){
        try{
            String postReceiverUrl = "http://172.18.0.1/tokensignin1.php";
            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(postReceiverUrl);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("client_id", userInfo[0]));
            nameValuePairs.add(new BasicNameValuePair("client_secret", userInfo[1]));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {
                String responseStr = EntityUtils.toString(resEntity).trim();
                Log.v(TAG, "Warehouse Response: " +  responseStr);
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

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_GET_TOKEN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
    }


    // [START handleSignInResult]
    private void handleSignInResult(@NonNull Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            String[] userInfo = new String[4];

            userInfo[0] = account.getEmail();
            userInfo[1] = account.getGivenName();
            userInfo[2] = account.getFamilyName();
            userInfo[3] = account.getIdToken();

            PostIDToken pp = new PostIDToken(userInfo);
            pp.execute();

            //updateUI(account);
        } catch (ApiException e) {
            Log.w(TAG, "handleSignInResult:error", e);
            updateUI(false);
        }
    }

    private class PostIDToken extends AsyncTask<String, String, String> {

        private String[] userInfo;
        private String isValid;

        PostIDToken(String[] userInfo){
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
            if(isValid.equals("user_does_not_exist") || isValid.equals("invalid_id_token") || isValid.equals("failed_validation") || isValid.equals("failed_token_request")) {
                textMain = "Username or/and password wrong";
                updateUI(false);
                signOut();
            }
            else {
                JSONObject jObject = null;
                try {
                    jObject = new JSONObject(isValid);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }

                accessToken=jObject.optString("accessToken");
                scope=jObject.optString("scope");

                if(scope.equals("manager")) {
                    Intent i = new Intent(MainActivity.this, product_utilities.class);
                    i.putExtra("access_token", accessToken);
                    startActivity(i);
                }
                else if(scope.equals("employee")){
                    Intent i = new Intent(MainActivity.this, e_product_utilities.class);
                    i.putExtra("access_token", accessToken);
                    startActivity(i);
                }

                updateUI(true);
            }
            // do stuff after posting data
        }
    }

    // this will post our id token to backend server
    private String postText(String[] userInfo){
        try{
            String postReceiverUrl = "http://172.18.0.1/tokensignin.php";

            HttpClient httpClient = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(postReceiverUrl);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(1);
            nameValuePairs.add(new BasicNameValuePair("email", userInfo[0]));
            nameValuePairs.add(new BasicNameValuePair("givenName", userInfo[1]));
            nameValuePairs.add(new BasicNameValuePair("familyName", userInfo[2]));
            nameValuePairs.add(new BasicNameValuePair("idToken", userInfo[3]));

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse response = httpClient.execute(httpPost);
            HttpEntity resEntity = response.getEntity();

            if (resEntity != null) {

                String responseStr = EntityUtils.toString(resEntity).trim();
                Log.v(TAG, "Response: " +  responseStr);
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

    private void signOut() {
        mGoogleSignInClient.signOut()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(false);
                    }
                });
    }

    private void revokeAccess() {
        mGoogleSignInClient.revokeAccess()
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        updateUI(false);
                    }
                });
    }

    private void updateUI(boolean account) {
        if (account != false) {
            findViewById(R.id.userT).setVisibility(View.INVISIBLE);
            findViewById(R.id.passT).setVisibility(View.INVISIBLE);
            findViewById(R.id.loginID).setVisibility(View.INVISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.INVISIBLE);
            findViewById(R.id.userEditID).setVisibility(View.INVISIBLE);
            findViewById(R.id.passwordEditID).setVisibility(View.INVISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.VISIBLE);
            findViewById(R.id.refresh).setVisibility(View.VISIBLE);
        } else {
            findViewById(R.id.userT).setVisibility(View.VISIBLE);
            findViewById(R.id.passT).setVisibility(View.VISIBLE);
            findViewById(R.id.loginID).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_in_button).setVisibility(View.VISIBLE);
            findViewById(R.id.userEditID).setVisibility(View.VISIBLE);
            findViewById(R.id.passwordEditID).setVisibility(View.VISIBLE);
            findViewById(R.id.sign_out_button).setVisibility(View.INVISIBLE);
            findViewById(R.id.refresh).setVisibility(View.INVISIBLE);
        }
    }

    public static void print_tables(Context context){
        DatabaseHelper db = new DatabaseHelper(context);
        SQLiteDatabase db1 = db.getWritableDatabase();

        Cursor res = db.getData("delta_products","*", "");

        System.out.println("table delta_products");
        System.out.printf("table %15s %15s %15s %15s %15s %15s %15s %15s%n", "ID", "Manufacture", "Model", "Price", "Quantity", "size", "Operation", "Warehouse");
        System.out.printf("table %15s %15s %15s %15s %15s %15s %15s %15s%n", "---", "------------", "------", "------", "---------","---------", "-----------", "-----------");
        if(res.getCount() != 0)
            while (res.moveToNext())
                System.out.printf("table %15s %15s %15s %15s %15s %15s %15s %15s%n", res.getString(0), res.getString(1), res.getString(2), res.getString(3), res.getString(4), res.getString(5), res.getString(6), res.getString(7));
        System.out.println("table ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        res = db.getData("gdansk_products","*", "");

        System.out.println("table gdansk products");
        System.out.printf("table %15s %15s %15s %15s %15s %15s%n", "ID", "Manufacture", "Model", "Price", "Quantity", "size");
        System.out.printf("table %15s %15s %15s %15s %15s %15s%n", "---", "------------", "------", "------", "---------", "---------");
        if(res.getCount() != 0)
            while (res.moveToNext())
                System.out.printf("table %15s %15s %15s %15s %15s %15s%n", res.getString(0), res.getString(1), res.getString(2), res.getString(3), res.getString(4), res.getString(5));
        System.out.println("table ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        res = db.getData("warsaw_products","*", "");

        System.out.println("table warsaw products");
        System.out.printf("table %15s %15s %15s %15s %15s %15s%n", "ID", "Manufacture", "Model", "Price", "Quantity", "size");
        System.out.printf("table %15s %15s %15s %15s %15s %15s%n", "---", "------------", "------", "------", "---------", "---------");
        if(res.getCount() != 0)
            while (res.moveToNext())
                System.out.printf("table %15s %15s %15s %15s %15s %15s%n", res.getString(0), res.getString(1), res.getString(2), res.getString(3), res.getString(4),  res.getString(5));
        System.out.println("table ++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");

        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        System.out.println("table TIMESTAMP: " + sharedPref.getString("TIMESTAMP", "0"));
        System.out.println("table NUM_OPERATIONS: " + sharedPref.getInt("NUM_OPERATIONS", 0));
    }
}