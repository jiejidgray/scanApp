package com.example.jijie.testauth;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobile.auth.core.IdentityHandler;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.AWSStartupHandler;
import com.amazonaws.mobile.client.AWSStartupResult;
import com.amazonaws.mobile.config.AWSConfiguration;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;

public class PageActivity extends AppCompatActivity {
    // Declare a DynamoDBMapper object
    DynamoDBMapper dynamoDBMapper;
    private TextView refid;
    private EditText name;
    private EditText email;
    private EditText phone;
    private EditText note;
    private Button submit;
    private Button reset;
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
        setContentView(R.layout.activity_page);
        Intent i =getIntent();
        refid = (TextView)findViewById(R.id.ref);
        refid.setText("Numéro de dossier:  "+i.getStringExtra("reference"));
        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Créer un nouveau dossier");

        AWSMobileClient.getInstance().initialize(this, new AWSStartupHandler() {
            @Override

            public void onComplete(AWSStartupResult awsStartupResult) {

                // Obtain the reference to the AWSCredentialsProvider and AWSConfiguration objects



                // Use IdentityManager#getUserID to fetch the identity id.
                IdentityManager.getDefaultIdentityManager().getUserID(new IdentityHandler() {
                    @Override
                    public void onIdentityId(String identityId) {
                        Log.d("YourMainActivity", "Identity ID = " + identityId);

                        // Use IdentityManager#getCachedUserID to
                        //  fetch the locally cached identity id.
                        final String cachedIdentityId =
                                IdentityManager.getDefaultIdentityManager().getCachedUserID();
                    }

                    @Override
                    public void handleError(Exception exception) {
                        Log.d("YourMainActivity", "Error in retrieving the identity" + exception);
                    }
                });
            }
        }).execute();

        AWSConfiguration  configuration = AWSMobileClient.getInstance().getConfiguration();
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "eu-central-1:ce81813a-b5f9-4509-932f-9d90e01abc2c", // Identity pool ID
                Regions.EU_CENTRAL_1 // Region
        );

        // Add code to instantiate a AmazonDynamoDBClient
        AmazonDynamoDBClient dynamoDBClient = Region.getRegion(Regions.EU_CENTRAL_1) // CRUCIAL
                .createClient(
                        AmazonDynamoDBClient.class,
                        credentialsProvider,
                        new ClientConfiguration()
                );
        this.dynamoDBMapper = DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();

        // other activity code ...
        name = findViewById(R.id.name);
        email = findViewById(R.id.mail);
        phone = findViewById(R.id.phone);
        note = findViewById(R.id.note);
        submit = (Button) findViewById(R.id.submit);
        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNews();
                Toast.makeText(PageActivity.this,"Submit Réussi",Toast.LENGTH_LONG).show();
                Intent intent = new Intent();
                intent.setClass(PageActivity.this, SCANActivity.class);
                startActivity(intent);
            }
        });
        reset = (Button) findViewById(R.id.reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reset();
            }
        });
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
                intent.setClass(PageActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

    public void createNews() {
        final DocumentDO newsItem = new DocumentDO();

        newsItem.setUserId(refid.getText().toString());
        newsItem.setUsername(name.getText().toString());
        newsItem.setEmail(email.getText().toString());
        newsItem.setPhone(phone.getText().toString());
        newsItem.setNote(note.getText().toString());


       // newsItem.setArticleId("Article1");
      //  newsItem.setContent("This is the article content");

        Runnable runnable = new Runnable() {
            public void run() {
                //DynamoDB calls go here
                dynamoDBMapper.save(newsItem);
            }
        };
        Thread mythread = new Thread(runnable);
        mythread.start();

    }

    public void reset(){
        name.setText("");
        email.setText("");
        phone.setText("");
        note.setText("");
    }

}
