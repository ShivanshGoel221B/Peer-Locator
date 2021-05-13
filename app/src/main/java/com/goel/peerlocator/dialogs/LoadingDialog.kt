package com.goel.peerlocator.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import com.goel.peerlocator.R
import com.goel.peerlocator.listeners.EditCircleListener


class LoadingDialog (private val listener : EditCircleListener) : AppCompatDialogFragment() {
    private lateinit var builder : AlertDialog.Builder
    private lateinit var dialogview : View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(activity!!.applicationContext)
        builder.setCancelable(false)
        dialogview = layoutInflater.inflate(R.layout.loading_dialog_box_horizontal, null)
        builder.setView(view)
        setProgress(0, 30)
        setMessage(R.string.creating_circle)
        return builder.create()
    }

    fun setProgress(progress: Int, secondaryProgress : Int) {
        val progressBar : ProgressBar = dialogview.findViewById(R.id.loading_progress_bar)
        progressBar.secondaryProgress = secondaryProgress
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
            progressBar.setProgress(progress, true)
        else
            progressBar.progress = progress
    }

    fun setMessage(resId : Int) {
        val message = resources.getString(resId)
        dialogview.findViewById<TextView>(R.id.loading_message).text = message
    }

    fun doneLoading () {
        dialogview.findViewById<ConstraintLayout>(R.id.progress_container).visibility = View.GONE
        dialogview.findViewById<ConstraintLayout>(R.id.done_layout).visibility = View.VISIBLE
        dialogview.findViewById<Button>(R.id.ok_button).setOnClickListener {
            dismiss()
            listener.onFinalCompletion()
        }
    }
}