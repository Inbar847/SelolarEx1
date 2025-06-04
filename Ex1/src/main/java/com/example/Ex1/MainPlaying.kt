package com.example.Ex1

import com.google.android.material.floatingactionbutton.FloatingActionButton
import android.widget.GridLayout
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaPlayer
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.appcompat.app.AlertDialog
import kotlin.random.Random

class MainPlaying : AppCompatActivity(), SensorEventListener {
    private lateinit var gameController: ControllerOfGame

    private lateinit var car: ImageView
    private lateinit var heart1: ImageView
    private lateinit var heart2: ImageView
    private lateinit var heart3: ImageView
    private lateinit var tvScore: TextView
    private lateinit var tvDistance: TextView
    private lateinit var crashSound: MediaPlayer
    private var initialSpeedFactor = 1.0f

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private var lastX: Float = 0f

    private var isSensorMode = true

    private var carLane = 2
    private var lives = 3
    private var score = 0
    private var distance = 0

    private lateinit var gameGrid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gameMode = intent.getStringExtra("GAME_MODE")
        isSensorMode = gameMode == "SENSOR"

        when (gameMode) {
            "BUTTON_FAST" -> {
                isSensorMode = false
                initialSpeedFactor = 1.5f
            }
            "BUTTON_SLOW" -> {
                isSensorMode = false
                initialSpeedFactor = 0.7f
            }
            "SENSOR" -> {
                isSensorMode = true
                initialSpeedFactor = 1.0f
            }
            else -> {
                isSensorMode = false
                initialSpeedFactor = 1.0f
            }
        }

        heart1 = findViewById(R.id.heart1)
        heart2 = findViewById(R.id.heart2)
        heart3 = findViewById(R.id.heart3)
        tvScore = findViewById(R.id.tvScore)
        tvDistance = findViewById(R.id.tvDistance)

        crashSound = MediaPlayer.create(this, R.raw.car_crash)

        val btnLeft: FloatingActionButton = findViewById(R.id.btnLeft)
        val btnRight: FloatingActionButton = findViewById(R.id.btnRight)

        btnLeft.setOnClickListener {
            if (carLane > 0) {
                carLane--
                moveCarToLane(car, carLane)
            }
        }

        btnRight.setOnClickListener {
            if (carLane < 4) {
                carLane++
                moveCarToLane(car, carLane)
            }
        }

        if (isSensorMode) {
            btnLeft.visibility = View.GONE
            btnRight.visibility = View.GONE
        }

        initGame()

        if (isSensorMode) {
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            val sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            if (sensor != null) {
                accelerometer = sensor
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
            } else {
                isSensorMode = false
            }
        }
    }

    private fun initGame() {
        gameGrid = findViewById(R.id.gameGrid)
        createGridMatrix(this, gameGrid)

        gameGrid.post {
            gameController = ControllerOfGame(
                context = this,
                gameGrid = gameGrid,
                onCollision = { onCarCollision() },
                onCoinCollected = { onCoinCollected() },
                onDistanceIncrease = { onDistanceIncrease() }
            )

            car = placeCarInGrid(this, gameGrid, carLane)
            gameController.setSpeedFactor(initialSpeedFactor)
            gameController.startGameLoop(car)
        }
    }

    private fun onCoinCollected() {
        score += 10
        tvScore.text = "Score: $score"
    }

    private fun onDistanceIncrease() {
        distance += 10
        tvDistance.text = "Distance: $distance"
    }

    private fun onCarCollision() {
        if (::crashSound.isInitialized) {
            if (crashSound.isPlaying) {
                crashSound.seekTo(0)
            } else {
                crashSound.start()
            }
        }

        lives--
        Log.d("DEBUG", "Lives left: $lives")

        when (lives) {
            2 -> heart3.visibility = View.GONE
            1 -> heart2.visibility = View.GONE
            0 -> {
                heart1.visibility = View.GONE
                gameController.stopGameLoop()

                // ⬇️ שמירת השיא עם מיקום רנדומלי
                val prefs = getSharedPreferences("high_scores", MODE_PRIVATE)
                val editor = prefs.edit()

                val latitude = 32.0853 + Math.random() * 0.08  // רנדום קל מסביב לת"א
                val longitude = 34.7818 + Math.random() * 0.08

                val scoreEntry = "$score#$latitude#$longitude"
                val scores = prefs.getStringSet("scores", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
                scores.add(scoreEntry)
                editor.putStringSet("scores", scores)
                editor.apply()

                showGameOverDialog()
            }
        }
    }


    private fun saveScoreAndShowDialog() {
        val prefs = getSharedPreferences("high_scores", MODE_PRIVATE)
        val scores = prefs.getStringSet("scores", setOf())?.toMutableSet() ?: mutableSetOf()

        val lat = Random.nextDouble(-90.0, 90.0)
        val lon = Random.nextDouble(-180.0, 180.0)

        scores.add("$score#$lat#$lon")
        prefs.edit().putStringSet("scores", scores).apply()

        showGameOverDialog()
    }

    private fun showGameOverDialog() {
        runOnUiThread {
            val builder = AlertDialog.Builder(this)
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

    override fun onResume() {
        super.onResume()
        if (isSensorMode) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)
        }
    }

    override fun onPause() {
        super.onPause()
        if (isSensorMode) {
            sensorManager.unregisterListener(this)
        }
    }

    private var lastMoveTime: Long = 0
    private val moveCooldown: Long = 500

    override fun onSensorChanged(event: SensorEvent?) {
        if (!isSensorMode || !::gameController.isInitialized) return

        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val x = event.values[0]
            val z = event.values[2]
            val currentTime = System.currentTimeMillis()

            if (currentTime - lastMoveTime >= moveCooldown) {
                if (x > 1.5 && carLane > 0) {
                    carLane--
                    moveCarToLane(car, carLane)
                    lastMoveTime = currentTime
                } else if (x < -1.5 && carLane < 4) {
                    carLane++
                    moveCarToLane(car, carLane)
                    lastMoveTime = currentTime
                }
            }

            val normalizedZ = z.coerceIn(-10f, 10f)
            val speedFactor = when {
                normalizedZ < -5 -> 2.0f
                normalizedZ < -2 -> 1.5f
                normalizedZ < 2 -> 1.0f
                normalizedZ < 5 -> 0.7f
                else -> 0.5f
            }

            gameController.setSpeedFactor(speedFactor)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // לא נדרש טיפול
    }
}
