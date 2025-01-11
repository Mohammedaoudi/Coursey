package com.example.firstaidfront

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.firstaidfront.databinding.ActivityArBinding
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.math.Position

class ArActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArBinding
    private lateinit var sceneView: ArSceneView
    private lateinit var modelNode: ArModelNode

    private val cprSteps = listOf(
        "Étape 1 : Position des mains",
        "Étape 2 : Placement sur la victime",
        "Étape 3 : Mouvement de compression"
    )

    private val cprModels = listOf(
        "models/step1.glb",
        "models/step2.glb",
        "models/step3.glb"
    )

    private var currentStepIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val trainingTitle = intent.getStringExtra("training_title") ?: "No title provided"
        Log.d("TrainingDetailActivity", "Training Title: $trainingTitle")
        binding.trainingTitleTv.text = trainingTitle

        setupAr()
    }

    private fun setupAr() {
        sceneView = binding.sceneView
        sceneView.lightEstimationMode = Config.LightEstimationMode.DISABLED

        val trainingTitle = intent.getStringExtra("training_title") ?: "No title provided"

        when {
            trainingTitle == "CPR Training" -> {
                binding.nextStepButton.visibility = View.VISIBLE
                setupCprTraining()
            }
            trainingTitle.contains("dental", ignoreCase = true) -> {
                binding.nextStepButton.visibility = View.GONE
                loadDentModel()
            }
            else -> {
                binding.nextStepButton.visibility = View.GONE
                loadAnatomyModel()
            }
        }
    }

    private fun setupCprTraining() {
        binding.instructionText.text = cprSteps[currentStepIndex]
        loadModelForCurrentStep()

        binding.nextStepButton.setOnClickListener {
            goToNextStep()
        }
    }

    private fun loadModelForCurrentStep() {
        if (::modelNode.isInitialized) {
            sceneView.removeChild(modelNode)
        }

        modelNode = ArModelNode(sceneView.engine).apply {
            loadModelGlbAsync(
                glbFileLocation = cprModels[currentStepIndex],
                scaleToUnits = 2.0f
            ) {
                sceneView.planeRenderer.isVisible = false
                modelNode.position = Position(0f, -1f, -2f)
                anchor()
            }
        }
        sceneView.addChild(modelNode)
    }

    private fun loadDentModel() {
        modelNode = ArModelNode(sceneView.engine).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/mouth.glb",
                scaleToUnits = 2.5f,
                onError = { error ->
                    Log.e("loadDentModel", "Erreur: ${error.message}")
                }
            ) {
                sceneView.planeRenderer.isVisible = false
                modelNode.position = Position(0f, -1f, -2f)
                anchor()
            }
        }
        sceneView.addChild(modelNode)
    }

    private fun loadAnatomyModel() {
        modelNode = ArModelNode(sceneView.engine).apply {
            loadModelGlbAsync(
                glbFileLocation = "models/anatomy.glb",
                scaleToUnits = 2.5f,
                onError = { error ->
                    Log.e("loadAnatomyModel", "Erreur: ${error.message}")
                }
            ) {
                Log.d("loadAnatomyModel", "Chargement du modèle anatomy.glb en cours...")
                sceneView.planeRenderer.isVisible = false
                Log.d("loadAnatomyModel", "Modèle anatomy.glb chargé avec succès")
                modelNode.position = Position(0f, -1f, -2f)
                anchor()
                Log.d("loadAnatomyModel", "Le modèle anatomy.glb a été ancré avec succès.")
            }
        }
        sceneView.addChild(modelNode)
        Log.d("loadAnatomyModel", "Le modèle anatomy.glb a été ajouté à la scène.")
    }

    private fun goToNextStep() {
        if (currentStepIndex < cprSteps.size - 1) {
            currentStepIndex++
            binding.instructionText.text = cprSteps[currentStepIndex]
            loadModelForCurrentStep()
        } else {
            binding.instructionText.text = "RCP terminée !"
            binding.nextStepButton.isEnabled = false
        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}