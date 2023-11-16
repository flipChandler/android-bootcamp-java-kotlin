package com.felipesantos.quotesofthedayapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    String[] quotes = {
            "A vida é bela!",
            "Socorram-me subi no onibus em Marrocos",
            "It's better to be a warrior in the garden than to be a gardener in a war",
            "Give it away now",
            "Can't stutter when you're talking with your eyes",
            "Mountains all around",
            "As of now I bet you got me wrong",
            "Mas o carcará foi dizer a rosa"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void createQuote(View view) {
        TextView text = findViewById(R.id.txt_quote);
        int randomNumber = new Random().nextInt(9); // 0.. 8
        String quote = quotes[randomNumber];
        text.setText(quote);
    }

    public void showAll(View view) {
        TextView text = findViewById(R.id.txt_quote);
        String result = "";
        for (String quote : quotes) {
            result = result + quote + "\n";
        }
        text.setText(result);
    }
}