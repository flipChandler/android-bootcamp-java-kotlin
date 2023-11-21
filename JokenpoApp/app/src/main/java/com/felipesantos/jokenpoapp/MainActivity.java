package com.felipesantos.jokenpoapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private static final String STONE = "stone";
    private static final String PAPER = "paper";
    private static final String SCISSOR = "scissor";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void selectStone(View view) {
        verifyWinner("stone");
    }

    public void selectPaper(View view) {
        verifyWinner("paper");
    }

    public void selectScissor(View view) {
        verifyWinner("scissor");
    }

    private String generateAppRandomChoice() {
        String[] options = {"stone", "paper", "scissor"};
        int randomNumber = new Random().nextInt(3);
        String appChoice = options[randomNumber];
        ImageView imageApp = findViewById(R.id.image_app);
        switch(appChoice) {
            case "stone":
                imageApp.setImageResource(R.drawable.stone);
                break;
            case "paper":
                imageApp.setImageResource(R.drawable.paper);
                break;
            case "scissor":
                imageApp.setImageResource(R.drawable.scissor);
                break;
        }
        return appChoice;
    }

    private void verifyWinner(String userChoice) {
        String appChoice = generateAppRandomChoice();
        TextView txtResult = findViewById(R.id.txt_result);
        if (appChoice.equals(STONE) && userChoice.equals(SCISSOR)
                || appChoice.equals(PAPER) && userChoice.equals(STONE)
                || appChoice.equals(SCISSOR) && userChoice.equals(PAPER)) {
            txtResult.setText("You loose :(");
        } else if (userChoice.equals(STONE) && appChoice.equals(SCISSOR)
                || userChoice.equals(PAPER) && appChoice.equals(STONE)
                || userChoice.equals(SCISSOR) && appChoice.equals(PAPER)) {
            txtResult.setText("You win :)");
        } else {
            txtResult.setText("Draw");
        }
    }
}