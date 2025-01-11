package com.example.firstaidfront.ui

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.example.firstaidfront.databinding.FragmentQuizBinding

import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.airbnb.lottie.LottieAnimationView
import com.example.firstaidfront.R
import com.example.firstaidfront.TestDetailActivity
import com.example.firstaidfront.data.ContentViewModel
import com.example.firstaidfront.data.QuizViewModel
import com.example.firstaidfront.databinding.FragmentContentBinding

import com.example.firstaidfront.models.ContentParagraph
import com.example.firstaidfront.models.Quiz
import com.example.firstaidfront.models.QuizResult
import com.google.android.material.button.MaterialButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class QuizFragment : Fragment() {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!

    private val viewModel: QuizViewModel by viewModels {
        QuizViewModel.Factory(requireContext())
    }

    private val quizViews = mutableMapOf<Int, QuizItemView>()
    private var resultDialog: Dialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val moduleId = arguments?.getInt(ARG_MODULE_ID)
            ?: throw IllegalArgumentException("Module ID is required")
        val enrollmentId = arguments?.getInt(ARG_ENROLLMENT_ID)
            ?: throw IllegalArgumentException("Enrollment ID is required")

        setupUI()
        setupObservers(moduleId, enrollmentId)
        viewModel.loadModule(moduleId)
        viewModel.loadQuizzes(moduleId)
    }

    private fun setupUI() {
        binding.submitAllButton.setOnClickListener {
            viewModel.calculateResult()
        }
    }

    private fun setupObservers(moduleId: Int, enrollmentId: Int) {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.quizzes.collect { quizzes ->
                        if (quizzes.isNotEmpty()) {
                            binding.progressBar.visibility = View.GONE
                            displayQuizzes(quizzes)
                        }
                    }
                }

                launch {
                    viewModel.quizResult.collect { result ->
                        result?.let {
                            showResultDialog(it, moduleId, enrollmentId)
                        }
                    }
                }

                launch {
                    viewModel.moduleCompleted.collect { completed ->
                        if (completed) {
                            resultDialog?.dismiss()
                            activity?.finish()
                        }
                    }
                }
            }
        }
    }

    private fun displayQuizzes(quizzes: List<Quiz>) {
        binding.quizzesContainer.removeAllViews()
        quizViews.clear()

        quizzes.forEach { quiz ->
            val quizView = QuizItemView(requireContext()).apply {
                layoutParams = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, resources.getDimensionPixelSize(R.dimen.spacing_normal))
                }
                onAnswerSelected = { selectedIndex ->
                    viewModel.submitAnswer(quiz.id, selectedIndex)
                }
                bindQuiz(quiz, viewModel.userAnswers[quiz.id])
            }
            binding.quizzesContainer.addView(quizView)
            quizViews[quiz.id] = quizView
        }
    }

    // QuizFragment.kt
    private fun showResultDialog(result: QuizResult, moduleId: Int, enrollmentId: Int) {
        binding.submitAllButton.visibility = View.GONE

        val dialogView = layoutInflater.inflate(R.layout.dialog_success, null)

        resultDialog = MaterialAlertDialogBuilder(requireContext(), R.style.AlertDialogTheme)
            .setView(dialogView)
            .setCancelable(false)
            .create()

        dialogView.apply {
            findViewById<LottieAnimationView>(R.id.successAnimation).apply {
                playAnimation()
                setAnimation(R.raw.success_animation)
            }

            findViewById<TextView>(R.id.dialogTitle).apply {
                text = "Quiz Completed!"
                setTextColor(ContextCompat.getColor(context, R.color.pink_dark))
                typeface = ResourcesCompat.getFont(context, R.font.poppins_bold)
            }

            findViewById<TextView>(R.id.dialogMessage).apply {
                text = buildString {
                    append("Score: ${String.format("%.1f", result.percentage)}%\n")
                    append("Correct Answers: ${result.correctAnswers}/${result.totalQuestions}")
                }
                setTextColor(ContextCompat.getColor(context, R.color.black))
                typeface = ResourcesCompat.getFont(context, R.font.poppins_regular)
            }

            findViewById<MaterialButton>(R.id.btnContinue).apply {
                setBackgroundColor(ContextCompat.getColor(context, R.color.pink_dark))
                setOnClickListener {
                    resultDialog?.dismiss()
                    val currentModule = viewModel.module.value

                    if (currentModule?.finished == true) {
                        viewModel.submitFinalTest(enrollmentId, moduleId)

                        viewLifecycleOwner.lifecycleScope.launch {
                            viewModel.testResult.collect { testResult ->
                                testResult?.let {
                                    Log.d("QuizFragment", "Test result received: $it")
                                    Log.d("QuizFragment", "User answers at submission: ${viewModel.userAnswers}")
                                    val intent = Intent(requireContext(), TestDetailActivity::class.java).apply {
                                        putExtra("test_id", it.id)
                                        putExtra("participantId", it.participantId)
                                        putExtra("formation_name", it.trainingDTO?.title ?: "")
                                        putExtra("scoreText", it.score.toInt())
                                        putExtra("estimatedDurationMinutes", it.trainingDTO?.estimatedDurationMinutes ?: 0)
                                        putExtra("submissionDate", it.submissionDate)
                                    }
                                    startActivity(intent)
                                    activity?.finish()
                                }
                            }
                        }
                    } else {
                        viewModel.markModuleComplete(enrollmentId, moduleId)
                    }
                }
            }
        }

        resultDialog?.window?.apply {
            setBackgroundDrawableResource(R.drawable.dialog_background)
            attributes?.windowAnimations = R.style.DialogAnimation
        }

        resultDialog?.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        resultDialog?.dismiss()
        resultDialog = null
        _binding = null
    }

    companion object {
        private const val ARG_MODULE_ID = "module_id"
        private const val ARG_ENROLLMENT_ID = "enrollment_id"

        fun newInstance(moduleId: Int, enrollmentId: Int) = QuizFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_MODULE_ID, moduleId)
                putInt(ARG_ENROLLMENT_ID, enrollmentId)
            }
        }
    }
}