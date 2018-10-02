package com.example.jijie.testauth;

import android.content.ClipData;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;


import com.amazonaws.ClientConfiguration;
import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.com.google.gson.Gson;
import com.amazonaws.mobile.auth.core.IdentityManager;
import com.amazonaws.mobile.auth.core.SignInStateChangeListener;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.dynamodbv2.document.Table;
import com.amazonaws.mobileconnectors.dynamodbv2.document.QueryFilter;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;

import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.sql.Ref;
import java.util.List;



public class SCANActivity extends AppCompatActivity {
    private int codeResult;
    private String refNumber;
    private DynamoDBMapper mapper;
    private Thread queryThread;
    private ImageView scan;


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

    /**
     * this is a comment
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan);

        Toolbar toolbar =findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle("ScanApp");
        }

        AWSConfiguration configuration = AWSMobileClient.getInstance().getConfiguration();
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                getApplicationContext(),
                "eu-central-1:ce81813a-b5f9-4509-932f-9d90e01abc2c", // Identity pool ID
                Regions.EU_CENTRAL_1 // Region
        );
        AmazonDynamoDBClient dynamoDBClient = Region.getRegion(Regions.EU_CENTRAL_1) // CRUCIAL
                .createClient(
                        AmazonDynamoDBClient.class,
                        credentialsProvider,
                        new ClientConfiguration()
                );
         this.mapper= DynamoDBMapper.builder()
                .dynamoDBClient(dynamoDBClient)
                .awsConfiguration(configuration)
                .build();
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
                intent.setClass(SCANActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        ImageView scan = findViewById(R.id.scanIcon);
         scan.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View v) {
                 IntentIntegrator integrator = new IntentIntegrator(SCANActivity.this);
                 integrator.setCaptureActivity(CustomCaptureActivity.class);
                 integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE);
                 integrator.setCameraId(0);
                 integrator.setBeepEnabled(false);
                 integrator.setOrientationLocked(false);
                 integrator.setPrompt("Scan");
                 integrator.initiateScan();
             }
         });


    }
   /* public void onScanQrcode(View v){
        IntentIntegrator integrator = new IntentIntegrator(this);
        integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE_TYPES);
       // integrator.setPrompt("Scan");
        integrator.setCameraId(0);
        integrator.setBeepEnabled(false);
        integrator.initiateScan();
    }*/


    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Toast.makeText(this, "Scan error", Toast.LENGTH_LONG).show();
            } else {
                refNumber = result.getContents().toString();
                QueryThread querythread = new QueryThread();
                querythread.setMapper(mapper);
                querythread.setRef(refNumber);
                querythread.start();
                try {
                    querythread.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                Log.d("Réussi","Result in thread:"+ querythread.getResult());
                if(querythread.getResult().isEmpty()) {
                    Toast.makeText(this, "Scan réussi, le numéro de référence:" + result.getContents(), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    intent.setClass(SCANActivity.this, PageActivity.class);
                    intent.putExtra("reference", refNumber);
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Scan réussi," + result.getContents()+"found in database", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent();
                    List<DocumentDO> exist = querythread.getResult();
                    Log.d("have","items:"+ exist.get(0));
                    intent.setClass(SCANActivity.this, SearchActivity.class);

                    intent.putExtra("documents", new Gson().toJson(exist.get(0)));
                    startActivity(intent);
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Gets news.
     */
/*  //private boolean existRef(int codeResult)

   private List getDoc(DynamoDBMapper mapper, String ref) {
         queryThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    com.amazonaws.models.nosql.DocumentDO doc = new DocumentDO();
                    doc.setUserId(ref);
                    DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                            .withHashKeyValues(doc)
                            .withConsistentRead(false);

                    List<DocumentDO> result = mapper.query(DocumentDO.class, queryExpression);
                    Log.d("réussi", "this is the result" + doc);
                    return result;
                } catch (Throwable t) {
                    System.err.println("Error running the app: " + t);
                    Log.e("Error running the app: ", Log.getStackTraceString(t));
                    return null;
                }
            }
        });


        /*try {
            System.out.println("GetDoc: Get document Id= " + ref);
            com.amazonaws.models.nosql.DocumentDO doc = mapper.load(com.amazonaws.models.nosql.DocumentDO.class,"lol");
            System.out.println(doc);
            Log.d("réussi","this is the result"+doc);
            return doc;
        }  catch (Throwable t) {
            System.err.println("Error running the app: " + t);
            Log.e("Error running the app: ",Log.getStackTraceString(t));
            return null;
        }

    }*/
    public void queryNews() {



              /*  Gson gson = new Gson();
                StringBuilder stringBuilder = new StringBuilder();

                // Loop through query results
                for (int i = 0; i < result.size(); i++) {
                    String jsonFormOfItem = gson.toJson(result.get(i));
                    stringBuilder.append(jsonFormOfItem + "\n\n");
                }*/

                // Add your code here to deal with the data result
               // Log.d("Query result: ", stringBuilder.toString());



    }
}
