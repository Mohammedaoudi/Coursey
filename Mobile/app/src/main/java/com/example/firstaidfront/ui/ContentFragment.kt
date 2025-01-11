package com.example.firstaidfront.ui

import com.example.firstaidfront.models.Content
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.example.firstaidfront.data.ContentViewModel
import com.example.firstaidfront.databinding.FragmentContentBinding

import com.example.firstaidfront.models.ContentParagraph
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import kotlinx.coroutines.launch
import java.util.regex.Pattern

class ContentFragment : Fragment() {
    private var _binding: FragmentContentBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ContentViewModel by viewModels {
        ContentViewModel.Factory(requireContext())
    }
    private var youTubePlayer: YouTubePlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentContentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val content = arguments?.getParcelable<Content>(ARG_CONTENT)
            ?: return

        Log.d("Content", "Loaded content: $content")

        setupUI(content)
        setupObservers()
        setupYouTubePlayer(content)

        viewModel.loadParagraphs(content.id)
    }

    private fun setupYouTubePlayer(content: Content) {
        if (content.url != null) {
            binding.youtubePlayerContainer.visibility = View.VISIBLE

            lifecycle.addObserver(binding.youtubePlayer)

            binding.youtubePlayer.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(player: YouTubePlayer) {
                    youTubePlayer = player
                    // Extract video ID from URL and load it
                    val videoId = extractYouTubeVideoId(content.url)
                    videoId?.let {
                        player.loadVideo(it, 0f)
                    }
                }
            })
        } else {
            binding.youtubePlayerContainer.visibility = View.GONE
        }
    }

    private fun extractYouTubeVideoId(url: String): String? {
        val pattern = "(?<=watch\\?v=|/videos/|embed\\/|youtu.be\\/|\\/v\\/|\\/e\\/|watch\\?v%3D|watch\\?feature=player_embedded&v=|%2Fvideos%2F|embed%\u201C|youtu.be%2F|%2Fv%2F)[^#\\&\\?\\n]*"
        val compiledPattern = Pattern.compile(pattern)
        val matcher = compiledPattern.matcher(url)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }

    private fun setupUI(content: Content) {
        binding.titleText.text = content.title
        binding.descriptionText.text = content.description
    }

    private fun setupObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.paragraphs.collect { paragraphs ->
                        Log.d("Parg", "Loaded paragraphs: $paragraphs")
                        updateParagraphs(paragraphs)
                    }
                }
            }
        }
    }

    private fun updateParagraphs(paragraphs: List<ContentParagraph>) {
        binding.paragraphsContainer.removeAllViews()
        paragraphs.forEach { paragraph ->
            val paragraphView = ParagraphView(requireContext()).apply {
                setTitle(paragraph.title)
                setDescription(paragraph.description)
                if (paragraph.image != null) {
                    setImage(paragraph.image)
                }
            }
            binding.paragraphsContainer.addView(paragraphView)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        youTubePlayer = null
        _binding = null
    }

    companion object {
        private const val ARG_CONTENT = "content"

        fun newInstance(content: Content) = ContentFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_CONTENT, content)
            }
        }
    }
}
