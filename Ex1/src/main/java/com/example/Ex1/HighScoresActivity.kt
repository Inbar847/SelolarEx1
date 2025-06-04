package com.example.Ex1

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class HighScoresActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_high_scores)

        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HighScoresTabsFragment())
            .commit()
    }
}
