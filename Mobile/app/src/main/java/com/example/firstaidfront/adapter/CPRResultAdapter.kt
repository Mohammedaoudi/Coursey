package com.example.firstaidfront.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.firstaidfront.R
import com.example.firstaidfront.cprDL.CPRResult


class CPRResultAdapter : RecyclerView.Adapter<CPRResultAdapter.ResultViewHolder>() {
    private val results = mutableListOf<CPRResult>()

    class ResultViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val timeText: TextView = view.findViewById(R.id.timeText)
        val rateText: TextView = view.findViewById(R.id.rateText)
        val depthText: TextView = view.findViewById(R.id.depthText)
        val handText: TextView = view.findViewById(R.id.handText)
        val releaseText: TextView = view.findViewById(R.id.releaseText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResultViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cpr_result, parent, false)
        return ResultViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResultViewHolder, position: Int) {
        val result = results[position]
        val timeSeconds = result.timestamp / 1000

        // Format time as "start-end" (e.g., "0-5s" instead of "-5-0s")
        val startTime = timeSeconds
        val endTime = timeSeconds + 5
        holder.timeText.text = "${startTime}-${endTime}s"

        holder.rateText.text = "${result.compressionRate ?: 0} BPM"
        holder.depthText.text = "%.1f mm".format(result.depth)
        holder.handText.text = result.handPosition
        holder.releaseText.text = result.release
    }

    override fun getItemCount() = results.size

    fun addResult(result: CPRResult) {
        results.add(result)
        notifyItemInserted(results.size - 1)
    }

    fun clear() {
        val size = results.size
        results.clear()
        notifyItemRangeRemoved(0, size)
    }
}