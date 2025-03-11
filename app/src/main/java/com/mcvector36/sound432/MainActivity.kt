package com.mcvector36.sound432

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val myButton: MaterialButton = findViewById(R.id.myButton)
        myButton.setOnClickListener {
            Toast.makeText(this, "Buton apăsat!", Toast.LENGTH_SHORT).show()
        }
    }
}