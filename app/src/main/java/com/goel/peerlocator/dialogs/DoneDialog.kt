package com.goel.peerlocator.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.goel.peerlocator.R

class DoneDialog (private val message: String, private val clickListener: ClickListener)
                : AppCompatDialogFragment() {

    private lateinit var builder : AlertDialog.Builder
    private lateinit var dialogView : View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        dialogView = requireActivity().layoutInflater.inflate(R.layout.dialog_done, null, false)
        builder.setView(dialogView).setTitle(R.string.please_wait)
        this.isCancelable = false
        setMessage(message)
        dialogView.findViewById<Button>(R.id.ok_button).setOnClickListener {
            dismiss()
            clickListener.onOkClicked()
        }
        return builder.create()
    }

    private fun setMessage(message: String) {
        dialogView.findViewById<TextView>(R.id.loading_message).text = message
    }

    interface ClickListener {
        fun onOkClicked ()
    }
}