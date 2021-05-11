package com.example.gileadproject;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    private Button buttonSend;
    private EditText textMessage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        buttonSend = (Button) findViewById(R.id.buttonSend);
        textMessage = (EditText) findViewById(R.id.textMessage);
    }


    public void handleClick(View view){
        switch(view.getId()){
            case R.id.buttonSend:
                //Store message before sending request
                String message = textMessage.getText().toString();
                System.out.println("Get Message: " + message);
                System.out.println("Execute buttonSend Clicked");
                sendInputRequest(message);
                textMessage.setText("");
        }
    }


    public void sendInputRequest(String message){
        System.out.println("sendInputRequest is called.");
    }
}