package com.example.Ex1

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.fragment.app.Fragment

class TopScoresFragment : Fragment() {

    interface OnScoreSelectedListener {
        fun onScoreSelected(lat: Double, lon: Double)
    }

    private var listener: OnScoreSelectedListener? = null
    private lateinit var listView: ListView


    fun setListener(listener: OnScoreSelectedListener) {
        this.listener = listener
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_top_scores, container, false)
        listView = view.findViewById(R.id.listViewScores)

        val prefs = requireContext().getSharedPreferences("high_scores", Context.MODE_PRIVATE)
        val scores = prefs.getStringSet("scores", setOf())?.map {
            val parts = it.split("#")
            Triple(parts[0].toInt(), parts[1].toDouble(), parts[2].toDouble())
        }?.sortedByDescending { it.first }?.take(10)

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_list_item_1,
            scores?.map { "Score: ${it.first}" } ?: listOf("No scores")
        )
        listView.adapter = adapter

        listView.setOnItemClickListener { _, _, position, _ ->
            scores?.get(position)?.let {
                listener?.onScoreSelected(it.second, it.third)
            }
        }

        return view
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }
}
