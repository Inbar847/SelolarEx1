package com.example.Ex1

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainMenuActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_menu)  // ודא שזה השם של הקובץ שלך

        findViewById<Button>(R.id.btnFastMode).setOnClickListener {
            startGame("BUTTON_FAST")
        }

        findViewById<Button>(R.id.btnSlowMode).setOnClickListener {
            startGame("BUTTON_SLOW")
        }

        findViewById<Button>(R.id.btnSensorMode).setOnClickListener {
            startGame("SENSOR")
        }

        findViewById<Button>(R.id.btnViewScores).setOnClickListener {
            val intent = Intent(this, HighScoresActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startGame(mode: String) {
        val intent = Intent(this, MainPlaying::class.java)
        intent.putExtra("GAME_MODE", mode)
        startActivity(intent)
    }
}
