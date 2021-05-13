package com.example.gileadproject;

import androidx.appcompat.app.AppCompatActivity;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.dialogflow.v2.*;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    private Button buttonSend;
    private EditText textMessage;
    private static TextView textLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        textMessage = (EditText) findViewById(R.id.textMessage);
        textLog = (TextView) findViewById(R.id.textLog);
    }

    public void handleClick(View view){
        switch(view.getId()){
            case R.id.buttonSend:
                //Store message before sending request
                String message = textMessage.getText().toString();
                textLog.append("\n" + message);
                System.out.println("Get Message: " + message);
                System.out.println("Execute buttonSend Clicked");
                textMessage.setText("");

        }
    }

    private void getCredentials() {
        try{
            InputStream in = this.getResources().openRawResource(R.raw.credentials);
            GoogleCredentials credentials = GoogleCredentials.fromStream(in);
        }catch(Exception e){
            e.printStackTrace();
            System.out.println(e);
        }
    }

    public void sendInputRequest(String message){
        System.out.println("sendInputRequest is called.");
    }
}