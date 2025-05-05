package com.example.Ex1
import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.GridLayout
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity


class MainPlaying : AppCompatActivity() {

    private lateinit var car: ImageView
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView

    private var carLane = 1

    private var lives = 3

    private lateinit var gameController: ControllerOfGame

    private lateinit var gameGrid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)

        val btnLeft: FloatingActionButton = findViewById(R.id.btnLeft)
        val btnRight: FloatingActionButton = findViewById(R.id.btnRight)

        btnLeft.setOnClickListener {
            if (carLane > 0) {
                carLane--
                moveCarToLane(car, carLane)
            }
        }

        btnRight.setOnClickListener {
            if (carLane < 2) {
                carLane++
                moveCarToLane(car, carLane)
            }
        }

        Log.d("DEBUG", "initGameGrid called")
        initGame()


    }


    private fun initGame() {
        gameGrid = findViewById(R.id.gameGrid)
        createGridMatrix(this, gameGrid)

        gameGrid.post {
            gameController = ControllerOfGame(this, gameGrid) {onCarCollision()}
            car = placeCarInGrid(this, gameGrid, carLane)
            gameController.startGameLoop(car)
        }
    }





    private fun onCarCollision() {
        lives--
        Log.d("DEBUG", "Lives left: $lives")

        when (lives) {
            2 -> heart3.visibility = View.GONE
            1 -> heart2.visibility = View.GONE
            0 -> {
                heart1.visibility = View.GONE
                gameController.stopGameLoop()
                showGameOverDialog()
            }
        }
    }
    private fun showGameOverDialog() {
        runOnUiThread {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("Game Over")
            builder.setMessage("You lost all lives! Play again?")
            builder.setCancelable(false)
            builder.setPositiveButton("Restart") { _, _ ->
                restartGame()
            }
            builder.setNegativeButton("Exit") { _, _ ->
                finish()
            }
            builder.show()
        }
    }
    private fun restartGame() {
        val intent = intent
        finish()
        startActivity(intent)
    }





}

