package com.goel.peerlocator.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.goel.peerlocator.R

class InvitationLoadingDialog (private val message: String) : AppCompatDialogFragment() {

    private lateinit var builder : AlertDialog.Builder
    private lateinit var dialogView : View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        dialogView = requireActivity().layoutInflater.inflate(R.layout.loading_invitation, null, false)
        builder.setView(dialogView).setTitle(R.string.please_wait)
        dialogView.findViewById<TextView>(R.id.loading_message).text = message
        this.isCancelable = false
        return builder.create()
    }
}