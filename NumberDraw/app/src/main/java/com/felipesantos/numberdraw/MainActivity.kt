package com.felipesantos.numberdraw

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import java.util.Random

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun draw(view: View) {
        val txtResult = findViewById<TextView>(R.id.txt_result);
        val number = Random().nextInt(11); // 0 ... 10
        txtResult.setText("Number: $number");
    }
}