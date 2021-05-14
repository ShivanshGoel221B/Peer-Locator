package com.goel.peerlocator.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import com.goel.peerlocator.R


class LoadingDialogHorizontal(private val clickListener: ClickListener) : AppCompatDialogFragment() {
    private lateinit var builder : AlertDialog.Builder
    private lateinit var dialogview : View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(context!!)
        builder.setCancelable(false)
        dialogview = activity!!.layoutInflater.inflate(R.layout.loading_dialog_box_horizontal, null, false)
        builder.setView(dialogview).setTitle(R.string.please_wait)
        this.isCancelable = false
        setProgress(0, 30)
        setMessage(R.string.creating_circle)
        return builder.create()
    }

    fun setProgress(progress: Int, secondaryProgress : Int) {
        val progressBar : ProgressBar = dialogview.findViewById(R.id.loading_progress_bar)
        progressBar.progress = progress
        progressBar.secondaryProgress = secondaryProgress
        if (progress >= 100)
            doneLoading()
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
            clickListener.onOkClicked()
        }
    }

    interface ClickListener {
        fun onOkClicked ()
    }
}