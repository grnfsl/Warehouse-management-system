package com.example.goran.warehousemanagement;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;


public class product_utilities extends AppCompatActivity implements View.OnClickListener{

    private Button add;
    private Button increase;
    private Button decrease;
    private Button remove;
    private Button sync;
    private Button logout;
    private String accessToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product);

        findViewById(R.id.add).setOnClickListener(this);
        findViewById(R.id.increase).setOnClickListener(this);
        findViewById(R.id.decrease).setOnClickListener(this);
        findViewById(R.id.remove).setOnClickListener(this);
        findViewById(R.id.sync).setOnClickListener(this);
        findViewById(R.id.logout).setOnClickListener(this);

        accessToken = getIntent().getStringExtra("access_token");
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add:
                add();
                break;
            case R.id.increase:
                increase();
                break;
            case R.id.decrease:
                decrease();
                break;
            case R.id.remove:
                remove();
                break;
            case R.id.sync:
                sync();
                break;
            case R.id.logout:
                logout();
                break;
        }
    }

  //  @Override
   // public void onBackPressed() {

    //}

    private void add(){
        Intent i = new Intent(product_utilities.this, add_product.class);
        i.putExtra("access_token", accessToken);
        startActivity(i);
    }
    private void increase(){
        Intent i = new Intent(product_utilities.this, increase_product.class);
        i.putExtra("access_token", accessToken);
        startActivity(i);
    }
    private void decrease(){
        Intent i = new Intent(product_utilities.this, decrease_product.class);
        i.putExtra("access_token", accessToken);
        startActivity(i);
    }
    private void remove(){
        Intent i = new Intent(product_utilities.this, remove_product.class);
        i.putExtra("access_token", accessToken);
        startActivity(i);
    }
    private void logout(){
        signOut();
        Intent i = new Intent(product_utilities.this, MainActivity.class);
        finish();
        startActivity(i);
    }
    private void signOut(){
        GoogleSignInClient mGoogleSignInClient;
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestIdToken(getString(R.string.server_client_id))
                .build();
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        mGoogleSignInClient.signOut();
    }
    private void sync(){
        new Sync(getApplicationContext(), accessToken);
    }

}
