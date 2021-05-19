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
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView buttonSend;
    private EditText textMessage;
    private static TextView textLog;
    private GoogleCredentials credentials;
    private SessionName sessionName;
    private SessionsClient sessionsClient;
    private Button buttonDiscussionOption;
    private Button buttonInformationOption;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getOptionsLayout();
            }
        }, 2000);
    }


    public void getOptionsLayout(){
        setContentView(R.layout.activity_display_options);
        buttonInformationOption = (Button) findViewById(R.id.button_info_option);
        buttonDiscussionOption = (Button) findViewById(R.id.button_discussion_option);

        buttonDiscussionOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_main);
                buttonSend = (ImageView) findViewById(R.id.buttonSend);
                textMessage = (EditText) findViewById(R.id.textMessage);
                textLog = (TextView) findViewById(R.id.textLog);
                setCredentials();
                createSession();
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
                sendWhoRequest("USA");
            }
        });

        buttonDiscussionOption.setAlpha(0);
        buttonDiscussionOption.animate().alpha(1.0f).setDuration(1500).start();
        buttonInformationOption.setAlpha(0);
        buttonInformationOption.animate().alpha(1.0f).setDuration(1500).start();
    }


    public void handleClick(View view) throws InterruptedException {
        switch(view.getId()){
            case R.id.buttonSend:
                //Store message before sending request
                String message = textMessage.getText().toString();
                textMessage.setText("");
                textLog.append(System.lineSeparator());
                textLog.append(message);
                System.out.println("Get Message: " + message);
                DialogFlowThread dialogFlowThread = new DialogFlowThread(message, sessionsClient, sessionName);
                dialogFlowThread.start();
                dialogFlowThread.join();
                textLog.append(System.lineSeparator());
                textLog.append(dialogFlowThread.getQueryResult());

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


class DialogFlowThread extends Thread {
    private String queryMessage;
    private SessionsClient sessionClient;
    private SessionName sessionName;
    private String result;

    DialogFlowThread(String message, SessionsClient sessionClient, SessionName sessionName) {
        this.queryMessage= message;
        this.sessionClient = sessionClient;
        this.sessionName = sessionName;
    }

    public void run() {
        System.out.println("Running Thread and sending query.");
        sendQuery(this.queryMessage, this.sessionClient, this.sessionName);
    }

    String getQueryResult(){
        return result;
    }

    private void sendQuery(String message, SessionsClient sessionsClient, SessionName sessionName) {
        try {
            QueryInput queryInput = QueryInput.newBuilder() .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
            DetectIntentRequest detectIntentRequest = DetectIntentRequest.newBuilder().setSession(sessionName.toString()).setQueryInput(queryInput).build();
            QueryResult queryResult = sessionsClient.detectIntent(detectIntentRequest).getQueryResult();
            this.result = queryResult.getFulfillmentText();
            System.out.println(this.result);
        } catch(Exception e){
            System.err.println("Failed to send query." + e);
        }
    }
}
