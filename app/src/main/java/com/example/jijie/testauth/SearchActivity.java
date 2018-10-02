package com.example.jijie.testauth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.com.google.gson.Gson;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Search;


public class SearchActivity extends AppCompatActivity {
    private TextView refid;
    private  DocumentDO doc;
    private TextView name;
    private TextView mail;
    private TextView tel;
    private TextView info;
    private String ref;
    private Button returntohome;
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.menulog,menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.signOut:{
                IdentityManager.getDefaultIdentityManager().signOut();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        String docJson=getIntent().getStringExtra("documents");
        doc = new Gson().fromJson(docJson,DocumentDO.class);
        IdentityManager.getDefaultIdentityManager().addSignInStateChangeListener(new SignInStateChangeListener() {
            @Override
            // Sign-in listener
            public void onUserSignedIn() {
                Log.d("Sign in", "User Signed In");
            }

            // Sign-out listener
            @Override
            public void onUserSignedOut() {

                // return to the sign-in screen upon sign-out
                Intent intent = new Intent();
                intent.setClass(SearchActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        Intent i =getIntent();
        refid = (TextView)findViewById(R.id.reference);
        refid.setText("Numéro de dossier:  "+doc.getUserId());

        name = (TextView)findViewById(R.id.username);
        name.setText(doc.getUsername());

        mail = (TextView)findViewById(R.id.email);
        mail.setText(doc.getEmail());

        tel = (TextView)findViewById(R.id.tel);
        tel.setText(doc.getPhone());

        info = (TextView)findViewById(R.id.infosupp);
        info.setText(doc.getNote());

        returntohome =(Button)findViewById(R.id.returntohome);
        returntohome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(SearchActivity.this, SCANActivity.class);
                startActivity(intent);
            }
        });


    }
}
