package com.example.firstaidfront.ui

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import com.bumptech.glide.Glide
import com.example.firstaidfront.R
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.databinding.LayoutParagraphBinding
import com.google.android.material.card.MaterialCardView

class ParagraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : MaterialCardView(context, attrs, defStyleAttr) {

    private val binding: LayoutParagraphBinding

    init {
        val view = LayoutInflater.from(context)
            .inflate(R.layout.layout_paragraph, this, true)
        binding = LayoutParagraphBinding.bind(view)
    }

    fun setTitle(title: String) {
        binding.paragraphTitle.text = title
    }

    fun setDescription(description: String) {
        binding.paragraphDescription.text = description
    }

    fun setImage(imageUrl: String) {
        binding.paragraphImage.visibility = View.VISIBLE
        Glide.with(context)
            .load("${ApiClient.BASE_URL}TRAINING-SERVICE/api/images/${imageUrl}")
            .into(binding.paragraphImage)
    }
}