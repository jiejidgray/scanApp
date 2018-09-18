package com.example.jijie.testauth;

import android.util.Log;

import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBQueryExpression;

import java.util.List;

public class QueryThread extends Thread implements Runnable{
    private String ref;
    private DynamoDBMapper mapper;
    private List<DocumentDO> result;

    public List<DocumentDO> getResult() {
        return result;
    }

    public void setResult(List<DocumentDO> result) {
        this.result = result;
    }

    public DynamoDBMapper getMapper() {
        return mapper;
    }

    public void setMapper(DynamoDBMapper mapper) {
        this.mapper = mapper;
    }

    public String getRef() {
        return ref;
    }

    public void setRef(String ref) {
        this.ref = ref;
    }

    public void run(){
       try {
            DocumentDO doc = new DocumentDO();
            doc.setUserId(getRef());
            DynamoDBQueryExpression queryExpression = new DynamoDBQueryExpression()
                    .withHashKeyValues(doc)
                    .withConsistentRead(false);

            result = mapper.query(DocumentDO.class, queryExpression);
            setResult(result);
            Log.d("réussi", "this is the result" + this.result);
            doc.setUserId(null);
        } catch (Throwable t) {
            System.err.println("Error running the app: " + this.result);
            Log.e("Error running the app: ", Log.getStackTraceString(t));
        }
    }
}
