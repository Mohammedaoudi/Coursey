package com.example.firstaidfront.ui

import android.content.Context
import android.graphics.Color
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.firstaidfront.R
import com.example.firstaidfront.databinding.LayoutParagraphBinding
import com.example.firstaidfront.databinding.ViewQuizItemBinding
import com.example.firstaidfront.models.Quiz
import com.google.android.material.card.MaterialCardView

class QuizItemView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private var questionText: TextView
    private var optionsGroup: RadioGroup
    var onAnswerSelected: ((Int) -> Unit)? = null

    init {
        // Inflate the layout
        val view = LayoutInflater.from(context).inflate(R.layout.view_quiz_item, this, true)

        // Initialize views
        questionText = view.findViewById(R.id.questionText)
        optionsGroup = view.findViewById(R.id.optionsGroup)

        // Setup card properties
        layoutParams = LayoutParams(
            LayoutParams.MATCH_PARENT,
            LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(0, 0, 0, 16)
        }

        radius = context.resources.getDimension(R.dimen.cardview_default_radius)
        elevation = context.resources.getDimension(R.dimen.cardview_default_elevation)
        setCardBackgroundColor(Color.WHITE)
    }

    fun bindQuiz(quiz: Quiz, selectedAnswer: Int? = null) {
        questionText.text = quiz.question
        optionsGroup.removeAllViews()

        quiz.options.forEachIndexed { index, option ->
            val radioButton = RadioButton(context).apply {
                id = View.generateViewId()
                text = option
                setTextColor(ContextCompat.getColor(context, android.R.color.black))
                layoutParams = RadioGroup.LayoutParams(
                    RadioGroup.LayoutParams.MATCH_PARENT,
                    RadioGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 8, 0, 8)
                }
                isChecked = selectedAnswer == index
            }
            optionsGroup.addView(radioButton)
        }

        optionsGroup.setOnCheckedChangeListener { _, _ ->
            val selectedIndex = optionsGroup.indexOfChild(
                optionsGroup.findViewById(optionsGroup.checkedRadioButtonId)
            )
            onAnswerSelected?.invoke(selectedIndex)
        }
    }
}