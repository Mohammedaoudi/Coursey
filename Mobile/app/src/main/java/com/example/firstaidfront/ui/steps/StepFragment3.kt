package com.example.firstaidfront.ui.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import android.widget.Toast
import androidx.core.view.children
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.firstaidfront.R
import com.example.firstaidfront.api.ParticipantService
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.config.TokenManager
import com.example.firstaidfront.databinding.FragmentStep3Binding
import com.example.firstaidfront.models.ParticipantAnswer
import com.example.firstaidfront.models.Quiz
import com.example.firstaidfront.models.TestResult
import com.google.android.material.card.MaterialCardView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.launch

class StepFragment3 : Fragment() {
    private var _binding: FragmentStep3Binding? = null
    private val binding get() = _binding!!

    private var currentQuestionIndex = 0
    private var quizzes: List<Quiz> = emptyList()
    private val userAnswers = mutableMapOf<Int, Int>()
    private var trainingId: Int = -1
    private lateinit var participantService: ParticipantService// QuizId to selected answer index
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        participantService = ApiClient.create(ParticipantService::class.java, requireContext())
        activity?.intent?.let { intent ->
            trainingId = intent.getIntExtra("training_id", -1)
//            intent.getParcelableArrayListExtra<Quiz>("quizzes")?.let {
//                quizzes = it.toList()
//            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentStep3Binding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAnimation()
        if (quizzes.isNotEmpty()) {
            displayQuestion(currentQuestionIndex)
            setupSubmitButton()
        } else {
            showNoQuizzesMessage()
        }
    }

    private fun setupAnimation() {
        binding.testAnimation.setAnimation(R.raw.quiz)
        binding.testAnimation.playAnimation()
    }

    private fun showNoQuizzesMessage() {
        binding.questionCard.visibility = View.GONE
        binding.optionsGroup.visibility = View.GONE
        binding.submitButton.visibility = View.GONE
        // You might want to add a TextView to show this message in your layout
    }

    private fun displayQuestion(index: Int) {
        val quiz = quizzes[index]
        binding.questionNumber.text = "Question ${index + 1}/${quizzes.size}"
        binding.questionText.text = quiz.question

        binding.optionsGroup.removeAllViews()

        quiz.options.forEachIndexed { optionIndex, optionText ->
            val optionView = layoutInflater.inflate(
                R.layout.item_test_option,
                binding.optionsGroup,
                false
            ) as MaterialCardView

            val radioButton = optionView.findViewById<RadioButton>(R.id.radioButton)
            radioButton.text = optionText
            radioButton.isChecked = userAnswers[quiz.id] == optionIndex

            val clickListener = View.OnClickListener {
                handleOptionSelection(quiz.id, optionIndex)

                // Update radio button states
                binding.optionsGroup.children.forEach { child ->
                    (child as? MaterialCardView)?.findViewById<RadioButton>(R.id.radioButton)?.isChecked = false
                }
                radioButton.isChecked = true
            }

            optionView.setOnClickListener(clickListener)
            radioButton.setOnClickListener(clickListener)

            binding.optionsGroup.addView(optionView)
        }

        binding.submitButton.visibility =
            if (index == quizzes.size - 1) View.VISIBLE else View.GONE
    }

    private fun handleOptionSelection(quizId: Int, selectedIndex: Int) {
        userAnswers[quizId] = selectedIndex

        view?.postDelayed({
            if (currentQuestionIndex < quizzes.size - 1) {
                currentQuestionIndex++
                displayQuestion(currentQuestionIndex)
            }
        }, 300)
    }

    private fun setupSubmitButton() {
        binding.submitButton.setOnClickListener {
            if (userAnswers.size < quizzes.size) {
                Toast.makeText(
                    context,
                    "Please answer all questions before submitting",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            submitTest()
        }
    }

    private fun submitTest() {
        val participantId = TokenManager.getParticipantId(requireContext())
        if (participantId == null || trainingId == -1) {
            Toast.makeText(context, "Error: Invalid session", Toast.LENGTH_SHORT).show()
            return
        }

        // Show loading state
        binding.submitButton.isEnabled = false
        binding.testAnimation.pauseAnimation()

        // Convert answers to API format
        val answers = userAnswers.map { (quizId, selectedAnswer) ->
            ParticipantAnswer(quizId, selectedAnswer)
        }

        // Submit test
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = participantService.submitTest(participantId, trainingId, answers)
                showResultDialog(result)
            } catch (e: Exception) {
                Toast.makeText(
                    context,
                    "Error submitting test: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
                binding.submitButton.isEnabled = true
                binding.testAnimation.resumeAnimation()
            }
        }
    }

    private fun calculateScore(): Int {
        return quizzes.count { quiz ->
            userAnswers[quiz.id] == quiz.correctAnswerIndex
        }
    }

    private fun showResultDialog(result: TestResult) {
        val message = buildString {
            append("Score: ${String.format("%.1f", result.score)}%\n\n")

            if (result.passed) {
                append("Congratulations! You've passed the test!\n")
                append("A certificate has been generated for you.")
            } else {
                append("You didn't pass this time.\n")
                append("Keep practicing and try again!")
            }
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(if (result.passed) "Certification Achieved!" else "Test Results")
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                if (result.passed) {
                    // Navigate back or to certificate view
                    activity?.finish()
                }
            }
            .setCancelable(false)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(): StepFragment3 = StepFragment3()
    }
}