package com.example.Ex1

import android.Manifest
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.GridLayout
import android.widget.ImageView
import androidx.annotation.RequiresPermission
import androidx.core.view.isVisible

data class Coin(var row: Int, var col: Int, val imageViewId: Int)

class ControllerOfGame(
    private val context: Context,
    private val gameGrid: GridLayout,
    private val onCollision: () -> Unit,
    private val onCoinCollected: () -> Unit,
    private val onDistanceIncrease: () -> Unit
)
{
    private val handler = Handler(Looper.getMainLooper())
    private val obstacles = mutableListOf<Obstacles>()
    private val coins = mutableListOf<Coin>()

    private var speedFactor = 1.0f

    private var obstacleId = 1000
    private var coinId = 2000
    private var tickCount = 0
    private var lastObstacleCol = -1
    private var isRunning = false

    fun startGameLoop(car: ImageView) {
        isRunning = true
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (!isRunning) return
                tick(car)
                handler.postDelayed(this, (1000 / speedFactor).toLong())
            }
        }, (1000 / speedFactor).toLong())
    }

    fun tick(car: ImageView) {
        moveObstacles()
        moveCoins()
        detectCollision(car)
        detectCoinCollection(car)
        onDistanceIncrease()

        tickCount++
        if (tickCount % 2 == 0) {
            spawnObstacle()
        }
        if (tickCount % 3 == 0) {
            spawnCoin()
        }
    }

    fun setSpeedFactor(factor: Float) {
        speedFactor = factor
    }


    private fun chooseColForObstacle(): Int {
        var col = (0 until gameGrid.columnCount).random()
        while (col == lastObstacleCol) {
            col = (0 until gameGrid.columnCount).random()
        }
        lastObstacleCol = col
        return col
    }

    fun spawnObstacle() {
        val col = chooseColForObstacle()
        val (cellWidth, cellHeight) = getCellSize(gameGrid)

        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.obstacle)

        val params = GridLayout.LayoutParams().apply {
            rowSpec = GridLayout.spec(0)
            columnSpec = GridLayout.spec(col)
            width = cellWidth
            height = cellHeight
            setGravity(android.view.Gravity.CENTER)
        }

        imageView.layoutParams = params
        imageView.id = obstacleId++
        gameGrid.addView(imageView)
        obstacles.add(Obstacles(0, col, imageView.id))
    }

    fun spawnCoin() {
        val col = (0 until gameGrid.columnCount).random()
        val (cellWidth, cellHeight) = getCellSize(gameGrid)

        val imageView = ImageView(context)
        imageView.setImageResource(R.drawable.coin)

        val params = GridLayout.LayoutParams().apply {
            rowSpec = GridLayout.spec(0)
            columnSpec = GridLayout.spec(col)
            width = cellWidth / 2
            height = cellHeight / 2
            setGravity(android.view.Gravity.CENTER)
        }

        imageView.layoutParams = params
        imageView.id = coinId++
        gameGrid.addView(imageView)
        coins.add(Coin(0, col, imageView.id))
    }

    @RequiresPermission(Manifest.permission.VIBRATE)
    private fun detectCollision(car: ImageView) {
        for (obstacle in obstacles) {
            val obstacleView = gameGrid.findViewById<ImageView>(obstacle.imageViewId)
            if (obstacleView.isVisible) {
                val carParams = car.layoutParams as GridLayout.LayoutParams
                val obstacleParams = obstacleView.layoutParams as GridLayout.LayoutParams
                if (carParams.columnSpec == obstacleParams.columnSpec && carParams.rowSpec == obstacleParams.rowSpec) {
                    FeedbackUtils.showToast(context, "Collision Detected!")
                    FeedbackUtils.vibrate(context)
                    onCollision()
                }
            }
        }
    }

    private fun detectCoinCollection(car: ImageView) {
        val iterator = coins.iterator()
        val carParams = car.layoutParams as GridLayout.LayoutParams

        while (iterator.hasNext()) {
            val coin = iterator.next()
            val coinView = gameGrid.findViewById<ImageView>(coin.imageViewId)
            val coinParams = coinView.layoutParams as GridLayout.LayoutParams

            if (carParams.columnSpec == coinParams.columnSpec && carParams.rowSpec == coinParams.rowSpec) {
                gameGrid.removeView(coinView)
                iterator.remove()
                onCoinCollected()
            }
        }
    }

    private fun moveObstacles() {
        val iterator = obstacles.iterator()

        while (iterator.hasNext()) {
            val obstacle = iterator.next()
            val view = gameGrid.findViewById<ImageView>(obstacle.imageViewId)

            if (obstacle.row >= gameGrid.rowCount - 1) {
                gameGrid.removeView(view)
                iterator.remove()
                continue
            }

            obstacle.row += 1
            val (cellWidth, cellHeight) = getCellSize(gameGrid)
            val params = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(obstacle.row)
                columnSpec = GridLayout.spec(obstacle.col)
                width = cellWidth
                height = cellHeight
                setGravity(android.view.Gravity.CENTER)
            }

            gameGrid.removeView(view)
            gameGrid.addView(view, params)
        }
    }

    private fun moveCoins() {
        val iterator = coins.iterator()

        while (iterator.hasNext()) {
            val coin = iterator.next()
            val view = gameGrid.findViewById<ImageView>(coin.imageViewId)

            if (coin.row >= gameGrid.rowCount - 1) {
                gameGrid.removeView(view)
                iterator.remove()
                continue
            }

            coin.row += 1
            val (cellWidth, cellHeight) = getCellSize(gameGrid)
            val params = GridLayout.LayoutParams().apply {
                rowSpec = GridLayout.spec(coin.row)
                columnSpec = GridLayout.spec(coin.col)
                width = cellWidth / 2
                height = cellHeight / 2
                setGravity(android.view.Gravity.CENTER)
            }

            gameGrid.removeView(view)
            gameGrid.addView(view, params)
        }
    }

    fun stopGameLoop() {
        isRunning = false
        handler.removeCallbacksAndMessages(null)
    }
}
