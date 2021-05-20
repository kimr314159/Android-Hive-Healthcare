package com.example.hivehealthcare;

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
import java.util.ArrayList;
import java.util.Locale;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private ImageView buttonSend;
    private ImageView buttonSpeech;
    private EditText textMessage;
    private static TextView textLog;
    private GoogleCredentials credentials;
    private SessionName sessionName;
    private SessionsClient sessionsClient;
    private Button buttonDiscussionOption;
    private Button buttonInformationOption;
    private final int VOICE_REQUEST_CODE = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getOptionsLayout();
            }}, 2000);
    }


    public void getOptionsLayout(){
        setContentView(R.layout.activity_display_options);
        buttonInformationOption = (Button) findViewById(R.id.button_info_option);
        buttonDiscussionOption = (Button) findViewById(R.id.button_discussion_option);

        buttonDiscussionOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setContentView(R.layout.activity_main);
                buttonSpeech = (ImageView) findViewById(R.id.buttonSpeech);
                buttonSend = (ImageView) findViewById(R.id.buttonSend);
                textMessage = (EditText) findViewById(R.id.textMessage);
                textLog = (TextView) findViewById(R.id.textLog);
                setCredentials();
                createSession();
                StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
                sendWhoRequest("USA");

                //Speech recognition
                buttonSpeech.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                            intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Detecting speech.");
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault().toString());
                            intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 500);
                            intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
                            startActivityForResult(intent, VOICE_REQUEST_CODE);
                        }catch (Exception e){
                            System.err.println("Failed to detect speech." + e);
                        }
                    }
                });
            }
        });

        buttonDiscussionOption.setAlpha(0);
        buttonDiscussionOption.animate().alpha(1.0f).setDuration(1500).start();
        buttonInformationOption.setAlpha(0);
        buttonInformationOption.animate().alpha(1.0f).setDuration(1500).start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
            if (resultCode == RESULT_OK && null != intent && VOICE_REQUEST_CODE==requestCode) {
                ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                textLog.append(System.lineSeparator());
                textLog.append(result.get(0));
            }
        super.onActivityResult(requestCode, resultCode, intent);
    }



    public void handleClick(View view) throws InterruptedException {
        switch(view.getId()){
            case R.id.buttonSend:
                //Store message before sending request
                try{
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
                }catch(Exception e){
                    System.err.println("Failed to send message. " + e);
                }

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