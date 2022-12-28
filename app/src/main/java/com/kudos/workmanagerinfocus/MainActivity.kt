package com.kudos.workmanagerinfocus

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import com.kudos.workmanagerinfocus.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val blurViewModel: BlurViewModel by viewModels()

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.apply {
            goButton.setOnClickListener {
                blurViewModel.applyBlur(blurLevel)
            }
            seeFileButton.setOnClickListener {
                blurViewModel.outputUri?.let { currentUri ->
                    val actionView = Intent(Intent.ACTION_VIEW, currentUri)
                    actionView.resolveActivity(packageManager)?.run {
                        startActivity(actionView)
                    }
                }
            }
            binding.cancelButton.setOnClickListener { blurViewModel.cancelWork() }
        }
        blurViewModel.outputWorkInfos.observe(this, workInfosObserver())
    }

    // Define the observer function
    private fun workInfosObserver(): Observer<List<WorkInfo>> {
        return Observer { listOfWorkInfo ->

            // Note that these next few lines grab a single WorkInfo if it exists
            // This code could be in a Transformation in the ViewModel; they are included here
            // so that the entire process of displaying a WorkInfo is in one location.

            // If there are no matching work info, do nothing
            if (listOfWorkInfo.isNullOrEmpty()) {
                return@Observer
            }

            // We only care about the one output status.
            // Every continuation has only one worker tagged TAG_OUTPUT
            val workInfo = listOfWorkInfo[0]

            if (workInfo.state.isFinished) {
                showWorkFinished()

                // Normally this processing, which is not directly related to drawing views on
                // screen would be in the ViewModel. For simplicity we are keeping it here.
                val outputImageUri = workInfo.outputData.getString(KEY_IMAGE_URI)

                // If there is an output file show "See File" button
                if (!outputImageUri.isNullOrEmpty()) {
                    blurViewModel.setOutputUri(outputImageUri)
                    binding.seeFileButton.visibility = View.VISIBLE
                }
            } else {
                showWorkInProgress()
            }
        }
    }

    /**
     * Shows and hides views for when the Activity is processing an image
     */
    private fun showWorkInProgress() {
        with(binding) {
//            progressBar.visibility = View.VISIBLE
//            cancelButton.visibility = View.VISIBLE
            progressLayout.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }

    /**
     * Shows and hides views for when the Activity is done processing an image
     */
    private fun showWorkFinished() {
        with(binding) {
//            progressBar.visibility = View.GONE
//            cancelButton.visibility = View.GONE
            progressLayout.visibility = View.GONE
            goButton.visibility = View.VISIBLE
        }
    }

    private val blurLevel: Int
        get() =
            when (binding.blurAmountRadioGroup.checkedRadioButtonId) {
                R.id.level1RadioButton -> 1
                R.id.level2RadioButton -> 2
                R.id.level3RadioButton -> 3
                else -> 1
            }

}