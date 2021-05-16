package com.example.gileadproject;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2.DetectIntentRequest;
import com.google.cloud.dialogflow.v2.QueryInput;
import com.google.cloud.dialogflow.v2.QueryResult;
import com.google.cloud.dialogflow.v2.SessionName;
import com.google.cloud.dialogflow.v2.SessionsClient;
import com.google.cloud.dialogflow.v2.SessionsSettings;
import com.google.cloud.dialogflow.v2.TextInput;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.UUID;

import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Button buttonSend;
    private EditText textMessage;
    private static TextView textLog;
    private GoogleCredentials credentials;
    private SessionName sessionName;
    private SessionsClient sessionsClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        textMessage = (EditText) findViewById(R.id.textMessage);
        textLog = (TextView) findViewById(R.id.textLog);
        setCredentials();
        createSession();
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
        sendWhoRequest("USA");


    }

    public void handleClick(View view){
        switch(view.getId()){
            case R.id.buttonSend:
                //Store message before sending request
                String message = textMessage.getText().toString();
                textMessage.setText("");
                textLog.append(System.lineSeparator());
                textLog.append(message);
                System.out.println("Get Message: " + message);
                sendQuery(message);
                //Hide soft keyboard
                InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void setCredentials() {
        try{
            InputStream in = this.getResources().openRawResource(R.raw.credentials);
            credentials = GoogleCredentials.fromStream(in);
        }catch(Exception e){
            System.err.println("Failed to set credentials. " + e);
        }
    }

    private void createSession() {
        try{
            SessionsSettings.Builder settingsBuilder = SessionsSettings.newBuilder();
            SessionsSettings sessionsSettings = settingsBuilder.setCredentialsProvider(FixedCredentialsProvider.create(credentials)).build();
            String projectId = ((ServiceAccountCredentials) credentials).getProjectId();
            String sessionId = UUID.randomUUID().toString();
            //Define endpoint
            sessionsClient = SessionsClient.create(sessionsSettings);
            sessionName = SessionName.of(projectId, sessionId);
        }catch(Exception e){
            System.err.println("Failed to create session. " + e);
        }
    }

    private void sendQuery(String message) {
        try {
            QueryInput queryInput = QueryInput.newBuilder() .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
            DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder().setSession(sessionName.toString()).setQueryInput(queryInput).build();
            QueryResult queryResult = sessionsClient.detectIntent(detectIntentRequest).getQueryResult();
            textLog.append(System.lineSeparator());
            textLog.append(queryResult.getFulfillmentText());
        } catch(Exception e){
            System.err.println("Failed to send query." + e);
        }
    }



    public void sendWhoRequest (String country) {
        try {
            URL url = new URL("https://apps.who.int/gho/athena/api/GHO/HIV_0000000001.xml?filter=COUNTRY:" + country + "&profile=simple");
            URLConnection urlConnection = (URLConnection) url.openConnection();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            String string;
            while ((string = bufferedReader.readLine()) != null) {
                System.out.println(string);
                System.out.println();
            }
            bufferedReader.close();
        }catch (Exception e){
            System.err.println("Failed to open URL." + e);
        }
    }

}