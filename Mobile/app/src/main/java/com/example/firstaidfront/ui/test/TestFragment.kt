package com.example.firstaidfront.ui.test

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.firstaidfront.R
import com.example.firstaidfront.TestDetailActivity
import com.example.firstaidfront.adapter.TestAdapter
import com.example.firstaidfront.api.ParticipantService
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.config.TokenManager
import com.example.firstaidfront.databinding.FragmentTestBinding
import com.example.firstaidfront.models.Test
import com.example.firstaidfront.models.TestResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class TestFragment : Fragment() {
    private lateinit var binding: FragmentTestBinding
    private lateinit var testAdapter: TestAdapter
    private lateinit var participantService: ParticipantService
    private var testResults = listOf<TestResult>()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTestBinding.inflate(inflater, container, false)
        participantService = ApiClient.create(ParticipantService::class.java, requireContext())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        startLoadingAnimation()
    }

    private fun setupRecyclerView() {
        val participantId = TokenManager.getParticipantId(requireContext())
        testAdapter = TestAdapter { testObj ->
            val testResult = testResults.find { it.id == testObj.id }
            val intent = Intent(requireContext(), TestDetailActivity::class.java).apply {
                putExtra("test_id", testObj.id)
                putExtra("formation_name", testObj.formationName)
                putExtra("scoreText", testObj.score)
                putExtra("estimatedDurationMinutes", testResult?.trainingDTO?.estimatedDurationMinutes)
                putExtra("submissionDate", testResult?.submissionDate)
                putExtra("participantId",participantId)


            }
            startActivity(intent)
        }

        binding.testsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = testAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun startLoadingAnimation() {
        binding.loadingAnimation.visibility = View.VISIBLE
        binding.testsRecyclerView.visibility = View.GONE

        lifecycleScope.launch {
            kotlinx.coroutines.delay(1400)
            loadTestData()
        }
    }

    private fun loadTestData() {
        // Créer une variable pour suivre si les données ont été chargées
        var dataLoaded = false

        // Timer pour arrêter l'animation après 10 secondes
        lifecycleScope.launch {
            delay(10000) // 10 secondes
            if (!dataLoaded) {
                binding.loadingAnimation.visibility = View.GONE
                binding.testsRecyclerView.visibility = View.VISIBLE
                // Afficher un message d'erreur ou une vue vide
                Log.e(TAG, "Timeout: Aucune donnée reçue après 10 secondes")
            }
        }

        lifecycleScope.launch {
            try {
                val participantId = TokenManager.getParticipantId(requireContext())

                if (participantId != null) {
                    Log.d(TAG, "Participant ID: $participantId")

                    testResults = participantService.getTestResultsByParticipantId(participantId)

                    if (testResults.isEmpty()) {
                        dataLoaded = true
                        binding.loadingAnimation.visibility = View.GONE
                        binding.testsRecyclerView.visibility = View.VISIBLE
                        binding.testsCompletedCount.text = "0"
                        binding.averageScoreText.text = "0%"
                        return@launch
                    }

                    val tests = testResults.map { testResult ->
                        Test(
                            id = testResult.id,
                            formationName = testResult.trainingDTO?.title ?: "Formation inconnue",
                            testDate = testResult.submissionDate,
                            isPassed = testResult.passed,
                            score = testResult.score.toInt().coerceIn(0, 100),
                            totalQuestions = 20
                        )
                    }

                    Log.d(TAG, "Fetched test results: $tests")
                    dataLoaded = true

                    binding.loadingAnimation.animate()
                        .alpha(0f)
                        .setDuration(500)
                        .withEndAction {
                            binding.loadingAnimation.visibility = View.GONE
                            binding.testsRecyclerView.apply {
                                alpha = 0f
                                visibility = View.VISIBLE
                                animate()
                                    .alpha(1f)
                                    .setDuration(500)
                                    .start()
                            }
                            binding.testsCompletedCount.text = tests.count { it.isPassed }.toString()
                            binding.averageScoreText.text = "${tests.map { it.score }.average().toInt()}%"
                            testAdapter.setTests(tests)
                        }
                        .start()
                } else {
                    dataLoaded = true
                    Log.e(TAG, "Participant ID is null")
                    binding.loadingAnimation.visibility = View.GONE
                }
            } catch (e: Exception) {
                dataLoaded = true
                Log.e(TAG, "Erreur lors de la récupération des résultats des tests", e)
                binding.loadingAnimation.visibility = View.GONE
                binding.testsRecyclerView.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding.loadingAnimation.cancelAnimation()
    }

    companion object {
        private const val TAG = "TestFragment"
    }
}