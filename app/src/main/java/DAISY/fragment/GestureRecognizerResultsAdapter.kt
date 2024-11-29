package com.google.mediapipe.examples.DAISY.fragment

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.google.mediapipe.examples.gesturerecognizer.R
import com.google.mediapipe.examples.gesturerecognizer.databinding.ItemGestureRecognizerResultBinding
import com.google.mediapipe.tasks.components.containers.Category
import java.util.Locale
import kotlin.math.min

class GestureRecognizerResultsAdapter : RecyclerView.Adapter<GestureRecognizerResultsAdapter.ViewHolder>() {
    companion object {
        private const val NO_VALUE = "--"
        private const val PAUSE_DELAY = 1500L  // 1.5 seconds pause between updates
        private const val CLEAR_DELAY = 180000L  // 180 seconds delay before clearing text
    }

    private var adapterCategories: MutableList<Category?> = mutableListOf()
    private var adapterSize: Int = 1
    private var resultSentence: String = ""  // Holds the sentence to display
    private val handler = Handler(Looper.getMainLooper())  // Handler to manage delays
    private var isUpdating = false  // Flag to prevent updating while already in progress

    @SuppressLint("NotifyDataSetChanged")
    fun updateResults(categories: List<Category>?) {
        categories?.let {
            val sortedCategories = categories.sortedByDescending { it.score() }
            val min = min(sortedCategories.size, adapterSize)

            if (!isUpdating) {
                isUpdating = true
                handler.postDelayed({
                    // Gradually append categories with a pause
                    for (i in 0 until min) {
                        val category = sortedCategories[i]
                        val label = category.categoryName()

                        // Combine gestures for both hands if needed
                        resultSentence += "$label "

                        // After each append, notify the adapter and delay the next update
                        notifyDataSetChanged()

                        // Add a pause for the next result, but avoid extra delay after the last result
                        if (i < min - 1) {
                            handler.postDelayed({}, PAUSE_DELAY)
                        }
                    }
                    isUpdating = false

                    // Clear the sentence after a certain delay
                    handler.postDelayed({
                        resultSentence = ""  // Clear the sentence
                        notifyDataSetChanged()  // Refresh the RecyclerView
                    }, CLEAR_DELAY)  // Clear after 60 seconds
                }, PAUSE_DELAY)
            }
        }
    }

    fun updateAdapterSize(size: Int) {
        adapterSize = size
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemGestureRecognizerResultBinding.inflate(
            LayoutInflater.from(parent.context), parent, false)

        // Set up the button for clearing text
        val clearButton: Button = binding.root.findViewById(R.id.btnClearText)
        clearButton.setOnClickListener {
            resultSentence = ""
            notifyDataSetChanged()
        }

        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(resultSentence)
    }

    override fun getItemCount(): Int = 1  // Only 1 item to display (the full sentence)

    inner class ViewHolder(private val binding: ItemGestureRecognizerResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(sentence: String?) {
            binding.tvLabel.text = sentence ?: NO_VALUE
        }
    }
}