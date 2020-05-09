package com.example.memorycanvas;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    TilesView view;
    Button button;
    boolean start;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view = findViewById(R.id.view);
        button = findViewById(R.id.button2);
        start = true;
    }

    public void turnOnGame(View v){
        if(start) {
            view.turnOnGame();
            button.setText("Play again");
            start = false;
        } else{
            view.newGame();
            Toast.makeText(this, "New game is started", Toast.LENGTH_SHORT).show();
        }
    }
}
