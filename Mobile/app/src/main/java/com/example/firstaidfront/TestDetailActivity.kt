package com.example.firstaidfront

import android.Manifest
import android.graphics.Path

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext

import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.lifecycleScope
import com.example.firstaidfront.api.ParticipantService
import com.example.firstaidfront.config.ApiClient
import com.example.firstaidfront.databinding.ActivityTestDetailBinding
import com.example.firstaidfront.models.Participant
import com.google.android.material.elevation.SurfaceColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.io.path.Path

class TestDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityTestDetailBinding
    private lateinit var participantService: ParticipantService
    private var testId: Int = 0
    private lateinit var formationName: String
    private var score: Int = 0

    companion object {
        private const val STORAGE_PERMISSION_CODE = 1001
        private val TAG = TestDetailActivity::class.java.simpleName
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityTestDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        participantService = ApiClient.create(ParticipantService::class.java, applicationContext)

        setupToolbar("Tests")
        setupWindowDecoration()
        setupTestDetails()
        setupCertificateButton()
    }

    private fun setupToolbar(name: String) {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = name
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowHomeEnabled(true)
        }
    }

    private fun setupWindowDecoration() {
        window.statusBarColor = ContextCompat.getColor(this, R.color.pink_dark)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)
    }

    private fun setupCertificateButton() {
        binding.downloadCertificateButton.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                checkAndRequestPermissions()
            } else {
                Toast.makeText(this, "Cette fonctionnalité nécessite Android 8.0 ou supérieur", Toast.LENGTH_LONG).show()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun setupTestDetails() {
        try {
            testId = intent.getIntExtra("test_id", -1)
            val participantId = intent.getIntExtra("participantId", -1)

            if (testId == -1 || participantId == -1) {
                throw IllegalArgumentException("Invalid test or participant ID")
            }

            formationName = intent.getStringExtra("formation_name") ?: throw IllegalArgumentException("Formation name required")
            score = intent.getIntExtra("scoreText", 0)
            val duration = intent.getIntExtra("estimatedDurationMinutes", 0)
            val submissionDate = parseSubmissionDate()

            updateUI(duration, submissionDate)
        } catch (e: Exception) {
            Log.e(TAG, "Error in setupTestDetails", e)
            Toast.makeText(this, "Error setting up test details: ${e.message}", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun parseSubmissionDate(): String {
        return intent.getStringExtra("submissionDate")?.let {
            try {
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
                val parsed = LocalDateTime.parse(it, formatter)
                parsed.format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH))
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing date", e)
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH))
            }
        } ?: LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMMM d, yyyy", Locale.ENGLISH))
    }

    private fun updateUI(duration: Int, submissionDate: String) {
        binding.apply {
            formationName.text = this@TestDetailActivity.formationName
            scoreText.text = "$score%"
            scoreProgress.progress = score
            estimatedDurationMinutes.text = "$duration minutes"
            completionDate.text = submissionDate
            certificationCard.visibility = if (score >= 70) View.VISIBLE else View.GONE
        }
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            generateAndDownloadCertificate()
        } else {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            } else {
                generateAndDownloadCertificate()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)

    private fun generateCertificate(participant: Participant): Bitmap {
        val pageWidth = 3508 // Horizontal A4
        val pageHeight = 2480

        return Bitmap.createBitmap(pageWidth, pageHeight, Bitmap.Config.ARGB_8888).apply {
            Canvas(this).apply {
                // Background
                drawRect(0f, 0f, pageWidth.toFloat(), pageHeight.toFloat(), Paint().apply {
                    color = Color.WHITE
                })

                // Border avec plus de marge
                val borderPaint = Paint().apply {
                    color = Color.rgb(44, 62, 80)
                    style = Paint.Style.STROKE
                    strokeWidth = 3f
                }
                drawRect(100f, 100f, pageWidth - 100f, pageHeight - 100f, borderPaint)

                // Logo centré en haut
                val appLogo = ResourcesCompat.getDrawable(resources, R.drawable.logo_app, null)
                val logoSize = 400f
                appLogo?.let {
                    it.setBounds(
                        (pageWidth/2 - logoSize).toInt(),
                        150,
                        (pageWidth/2 + logoSize).toInt(),
                        550
                    )
                    it.draw(this)
                }

                // Titre
                val titlePaint = Paint().apply {
                    color = Color.rgb(44, 62, 80)
                    textAlign = Paint.Align.CENTER
                    textSize = 150f
                    typeface = Typeface.create("serif", Typeface.BOLD)
                }
                drawText("CERTIFICATE OF COMPLETION", pageWidth/2f, 800f, titlePaint)

                // Date alignée à gauche
                val datePaint = Paint().apply {
                    color = Color.rgb(100, 100, 100)
                    textAlign = Paint.Align.LEFT
                    textSize = 60f
                    typeface = Typeface.create("serif", Typeface.NORMAL)
                }
                drawText(
                    LocalDateTime.now().format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                    300f,
                    1000f,
                    datePaint
                )

                // Nom centré
                val namePaint = Paint().apply {
                    color = Color.rgb(44, 62, 80)
                    textAlign = Paint.Align.CENTER
                    textSize = 120f
                    typeface = Typeface.create("serif", Typeface.BOLD)
                }
                drawText("${participant.firstName} ${participant.lastName}", pageWidth/2f, 1200f, namePaint)

                // Texte de réussite
                val completedPaint = Paint().apply {
                    color = Color.rgb(44, 62, 80)
                    textAlign = Paint.Align.CENTER
                    textSize = 80f
                    typeface = Typeface.create("serif", Typeface.NORMAL)
                }
                drawText("a réussi avec succès", pageWidth/2f, 1400f, completedPaint)

                // Nom de la formation
                val coursePaint = Paint().apply {
                    color = Color.rgb(44, 62, 80)
                    textAlign = Paint.Align.CENTER
                    textSize = 100f
                    typeface = Typeface.create("serif", Typeface.BOLD)
                }
                drawText(formationName, pageWidth/2f, 1600f, coursePaint)

                // Score
                if (score >= 70) {
                    drawText("Score: $score%", pageWidth/2f, 1800f, completedPaint)
                }

                // Signature et vérification alignées aux extrémités
                drawLine(300f, pageHeight - 300f, 900f, pageHeight - 300f, Paint().apply {
                    color = Color.rgb(44, 62, 80)
                    strokeWidth = 2f
                })

                val footerPaint = Paint().apply {
                    textAlign = Paint.Align.LEFT
                    textSize = 50f
                    typeface = Typeface.create("serif", Typeface.NORMAL)
                }

                drawText("Instructeur, FirstAid", 300f, pageHeight - 200f, footerPaint)
                drawText("Verify at: https://firstaid.com/verify/$testId", pageWidth - 900f, pageHeight - 200f, footerPaint)
            }
        }
    }

    @SuppressLint("NewApi")
    private fun generateAndDownloadCertificate() {
        val participantId = intent.getIntExtra("participantId", -1)
        if (participantId == -1) {
            Toast.makeText(this, "Invalid participant ID", Toast.LENGTH_SHORT).show()
            return
        }

        binding.downloadCertificateButton.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val participant = participantService.getParticipantById(participantId)
                val certificate = generateCertificate(participant)
                downloadCertificate(certificate)

                withContext(Dispatchers.Main) {
                    binding.downloadCertificateButton.isEnabled = true
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error generating certificate", e)
                withContext(Dispatchers.Main) {
                    binding.downloadCertificateButton.isEnabled = true
                    Toast.makeText(this@TestDetailActivity, "Error generating certificate: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }


    @SuppressLint("NewApi")
    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun downloadCertificate(certificate: Bitmap) {
        try {
            val sanitizedFormationName = formationName.replace(Regex("[^a-zA-Z0-9]"), "_")
            val fileName = "FirstAid_Certificate_${sanitizedFormationName}_${LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))}.pdf"

            val contentValues = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Downloads.IS_PENDING, 1)
                }
            }

            val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                ?: throw Exception("Failed to create new MediaStore record")

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                val pdfDocument = PdfDocument().apply {
                    val pageInfo = PdfDocument.PageInfo.Builder(certificate.width, certificate.height, 1).create()
                    startPage(pageInfo).apply {
                        canvas.drawBitmap(certificate, 0f, 0f, null)
                        finishPage(this)
                    }
                    writeTo(outputStream)
                    close()
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Downloads.IS_PENDING, 0)
                    contentResolver.update(uri, contentValues, null, null)
                }

                withContext(Dispatchers.Main) {
                    Toast.makeText(this@TestDetailActivity, "Certificate saved as:\n$fileName", Toast.LENGTH_LONG).show()

                    // Ouvrir le PDF directement après la sauvegarde
                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
                        setDataAndType(uri, "application/pdf")
                    }

                    try {
                        startActivity(intent)
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(
                            this@TestDetailActivity,
                            "Aucune application trouvée pour ouvrir le PDF. Veuillez installer un lecteur PDF.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e(TAG, "Error saving certificate", e)
                Toast.makeText(this@TestDetailActivity, "Error saving certificate: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            STORAGE_PERMISSION_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    generateAndDownloadCertificate()
                } else {
                    Toast.makeText(this, "Storage permission required to save certificate", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}