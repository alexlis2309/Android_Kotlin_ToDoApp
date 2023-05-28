package com.example.laba5_6

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DetailActivity : AppCompatActivity() {
    private lateinit var selectedItemTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)

        selectedItemTextView = findViewById(R.id.selectedItemTextView)

        val selectedItem = intent.getStringExtra("selectedItem")
        selectedItemTextView.text = selectedItem
    }
}
