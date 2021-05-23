package com.example.hivehealthcare;

import androidx.appcompat.app.AppCompatActivity;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.auth.oauth2.ServiceAccountCredentials;
import com.google.cloud.dialogflow.v2beta1.*;
import com.google.cloud.translate.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;
import java.util.UUID;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.InputStream;


public class MainActivity extends AppCompatActivity  {

    private ImageView buttonSend;
    private ImageView buttonSpeech;
    private TextView textName;
    private EditText textMessage;
    private static TextView textLog;
    private GoogleCredentials credentials;
    private SessionName sessionName;
    private SessionsClient sessionsClient;
    private LinearLayout viewResponses;
    private LinearLayout buttonDiscussionOption;
    private LinearLayout buttonInformationOption;
    private LinearLayout layoutInformationPage;
    private Button buttonReturnToOptions;
    private LinearLayout layoutDiscussion;
    private LinearLayout layoutDisplayOptions;
    private final int VOICE_REQUEST_CODE = 200;
    private String knowledgeBaseName;
    private static Context context;
    TextToSpeech textToSpeech;
    private Translate translate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        textName = (TextView) findViewById(R.id.text_name);
        textName.setAlpha(0);
        textName.animate().alpha(1.0f).setDuration(3000).start();
        knowledgeBaseName =  getResources().getString(R.string.knowledge_base_id);
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                getOptionsLayout();
            }}, 2000);

        textToSpeech = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status == TextToSpeech.SUCCESS) {
                    int result = textToSpeech.setLanguage(Locale.UK);
                } else {
                    System.err.println("Failed to setup text to voice.");
                }
            }
        });
    }



    public void setupTranslator() {
        try  {
            StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
            TranslateOptions translateOptions = TranslateOptions.newBuilder().setCredentials(credentials).build();
            translate = translateOptions.getService();
        }catch (Exception e){
            System.err.println("Failed to configure translate." + e);
        }
    }


    public void translate() {
        System.out.println("test!!!");
        Translation translation = translate.translate("Hello Kim",
                Translate.TranslateOption.sourceLanguage("en"),
                Translate.TranslateOption.targetLanguage("fr"));
        System.out.println(translation.getTranslatedText());

    }

    private void startTextToSpeech(String str) {
        textToSpeech.speak(str, TextToSpeech.QUEUE_FLUSH, null, null);
    }


    public void createOutResponse(LinearLayout viewResponses, String str){
        System.out.println("createOutMessage");
        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
        linearLayout.setGravity(Gravity.RIGHT);
        linearLayout.setPadding(0,20,0,20);
        TextView textView = new TextView(MainActivity.this);
        textView.setText(str);

        textView.setBackgroundColor(0xfff00000);
        textView.setTextColor(Color.BLUE);
        textView.setPadding(40,20,40,20);
        textView.setBackgroundResource(R.color.white);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        viewResponses.addView(linearLayout);
        linearLayout.addView(textView);
    }

    public void createInResponse(LinearLayout viewResponses, String str){
        startTextToSpeech(str);
        System.out.println("createInResponse");
        LinearLayout linearLayout = new LinearLayout(MainActivity.this);
        linearLayout.setGravity(Gravity.LEFT);
        linearLayout.setPadding(0,20,0,20);
        TextView textView = new TextView(MainActivity.this);
        textView.setText(str);
        textView.setTextColor(Color.GRAY);
        textView.setPadding(40,20,40,20);
        textView.setBackgroundResource(R.color.white);
        textView.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        viewResponses.addView(linearLayout);
        linearLayout.addView(textView);
    }


    public void getOptionsLayout(){
        setContentView(R.layout.activity_display_options);
        buttonDiscussionOption = (LinearLayout) findViewById(R.id.layout_discussion);
        buttonInformationOption = (LinearLayout) findViewById(R.id.layout_information);
        layoutDisplayOptions = (LinearLayout) findViewById(R.id.layout_display_options);

        buttonDiscussionOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    //layout_discussion
                    setContentView(R.layout.activity_main);
                    layoutDiscussion = (LinearLayout) findViewById(R.id.layout_discussion);
                    layoutDiscussion.setAlpha(0);
                    layoutDiscussion.animate().alpha(1.0f).setDuration(3000).start();
                    buttonReturnToOptions = (Button) findViewById(R.id.button_return);
                    viewResponses = (LinearLayout) findViewById(R.id.view_responses);
                    textLog = (TextView) findViewById(R.id.textLog);

                    buttonSpeech = (ImageView) findViewById(R.id.buttonSpeech);
                    buttonSend = (ImageView) findViewById(R.id.buttonSend);
                    textMessage = (EditText) findViewById(R.id.textMessage);
                    setCredentials();
                    createInResponse(viewResponses, "Hello, ask questions about HIV/Aids and I will try my best to answer.");
                    createSession();
                    StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder().permitAll().build());
                    sendWhoRequest("USA");


                    setupTranslator();
                    translate();


                    buttonReturnToOptions.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                getOptionsLayout();
                            }catch (Exception e){
                                System.err.println("Failed to return to options page." + e);
                            }
                        }
                    });

                }catch (Exception e){
                    System.err.println("Failed to load 'discussions' option." + e);
                }

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

        buttonInformationOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    setContentView(R.layout.activity_information_sheet);
                    layoutInformationPage = (LinearLayout) findViewById(R.id.layout_information_page);
                    buttonReturnToOptions = (Button) findViewById(R.id.button_return);
                    layoutInformationPage.setAlpha(0);
                    layoutInformationPage.animate().alpha(1.0f).setDuration(3000).start();
                    //Speech recognition
                    buttonReturnToOptions.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            try {
                                getOptionsLayout();
                            }catch (Exception e){
                                System.err.println("Failed to return to options page." + e);
                            }
                        }
                    });


                }catch (Exception e){
                    System.err.println("Failed to load 'Information option'." + e);
                }
            }
        });
        layoutDisplayOptions.setAlpha(0);
        layoutDisplayOptions.animate().alpha(1.0f).setDuration(3000).start();
        buttonDiscussionOption.setZ(20);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK && null != intent && VOICE_REQUEST_CODE==requestCode) {
            ArrayList<String> result = intent.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            String results = result.get(0);
            results = results.replace(getResources().getString(R.string.resultRegex),getResources().getString(R.string.resultRegexUpdate));
            createOutResponse(viewResponses, results);
            try {
                sendMessage(results);
            } catch(Exception e){
                System.err.println("Failed to send message using voice recognition. " + e);
            }
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
                    System.out.println("Get Message: " + message);
                    createOutResponse(viewResponses,message);
                    sendMessage(message);

                    //Hide keys
                    InputMethodManager inputMethodManager =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
                }catch(Exception e){
                    System.err.println("Failed to send message. " + e);
                }

        }
    }


    public void sendMessage(String message) throws InterruptedException {
        DialogFlowThread dialogFlowThread = new DialogFlowThread(message, sessionsClient, sessionName, knowledgeBaseName);
        dialogFlowThread.start();
        dialogFlowThread.join();
        createInResponse(viewResponses, dialogFlowThread.getQueryResult());
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
    private String knowledgeBaseName;


    DialogFlowThread(String message, SessionsClient sessionClient, SessionName sessionName,  String knowledgeBaseName) {
        this.queryMessage= message;
        this.sessionClient = sessionClient;
        this.sessionName = sessionName;
        this.knowledgeBaseName = knowledgeBaseName;
    }

    public void run() {
        System.out.println("Running Thread and sending query.");
        sendQuery(this.queryMessage, this.sessionClient, this.sessionName, this.knowledgeBaseName);
    }

    String getQueryResult(){
        return result;
    }

    private void sendQuery(String message, SessionsClient sessionsClient, SessionName sessionName, String knowledgeBaseName) {
        System.out.println(knowledgeBaseName);
        try {
            QueryInput queryInput = QueryInput.newBuilder() .setText(TextInput.newBuilder().setText(message).setLanguageCode("en-US")).build();
            QueryParameters queryParameters = QueryParameters.newBuilder().addKnowledgeBaseNames(knowledgeBaseName).build();
            DetectIntentRequest detectIntentRequest =DetectIntentRequest.newBuilder().setSession(sessionName.toString()).setQueryInput(queryInput).setQueryParams(queryParameters).build();
            QueryResult queryResult = sessionsClient.detectIntent(detectIntentRequest).getQueryResult();
            this.result = queryResult.getFulfillmentText();
            System.out.println(this.result);
        } catch(Exception e){
            System.err.println("Failed to send query." + e);
        }
    }








}
