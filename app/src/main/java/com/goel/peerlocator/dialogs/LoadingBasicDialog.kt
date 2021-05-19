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


class LoadingBasicDialog(private val message: String) : AppCompatDialogFragment() {
    private lateinit var builder : AlertDialog.Builder
    private lateinit var dialogView : View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(context!!)
        builder.setCancelable(false)
        dialogView = activity!!.layoutInflater.inflate(R.layout.loading_basic, null, false)
        builder.setView(dialogView).setTitle(R.string.please_wait)
        this.isCancelable = false
        setMessage(message)
        return builder.create()
    }

    fun setMessage(message: String) {
        dialogView.findViewById<TextView>(R.id.loading_message).text = message
    }

}