package com.example.firstaidfront.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.firstaidfront.R
import com.example.firstaidfront.models.Test
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Locale

class TestAdapter(private val onItemClick: (Test) -> Unit) :
    RecyclerView.Adapter<TestAdapter.TestViewHolder>() {

    private var tests = listOf<Test>()
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    fun setTests(tests: List<Test>) {
        this.tests = tests
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_test, parent, false)
        return TestViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        holder.bind(tests[position])
    }

    override fun getItemCount() = tests.size

    class TestViewHolder(
        itemView: View,
        private val onItemClick: (Test) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val formationIcon: ImageView = itemView.findViewById(R.id.formationIcon)
        private val formationNameView: TextView = itemView.findViewById(R.id.formationName)
        private val testDateView: TextView = itemView.findViewById(R.id.testDate)
        private val statusChip: Chip = itemView.findViewById(R.id.statusChip)
        private val percentageView: TextView = itemView.findViewById(R.id.percentageText)

        fun bind(test: Test) {
            // Set formation name
            formationNameView.text = test.formationName

            // Format and set date
            testDateView.text = "Completed on ${test.testDate}"

            // Calculate and set percentage
            val percentage = test.score
            percentageView.text = "$percentage%"

            // Configure status chip
            statusChip.apply {
                text = if (test.isPassed) "PASS" else "FAIL"
                setChipBackgroundColorResource(
                    if (test.isPassed) R.color.green_success else R.color.red_fail
                )
                setTextColor(ContextCompat.getColor(context, R.color.white))
            }

            // Set formation icon based on type (you can customize this)
            formationIcon.setImageResource(
                when {
                    test.formationName.contains("First Aid", ignoreCase = true) ->
                        R.drawable.ic_healthtest
                    test.formationName.contains("CPR", ignoreCase = true) ->
                        R.drawable.ic_healthtest
                    test.formationName.contains("Emergency", ignoreCase = true) ->
                        R.drawable.ic_healthtest
                    else -> R.drawable.ic_formation
                }
            )

            // Set click listener
            itemView.setOnClickListener { onItemClick(test) }
        }
    }
}