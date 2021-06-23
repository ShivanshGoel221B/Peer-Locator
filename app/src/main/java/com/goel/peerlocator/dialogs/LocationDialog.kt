package com.goel.peerlocator.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.goel.peerlocator.R

class LocationDialog(private val listener: ClickListeners): AppCompatDialogFragment() {

    lateinit var builder : AlertDialog.Builder
    private lateinit var dialogView : View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(requireContext())
        builder.setCancelable(false)
        dialogView = requireActivity().layoutInflater.inflate(R.layout.location_dialog, null, false)
        builder.setView(dialogView).setTitle("Location Permission")
        this.isCancelable = false
        setClickListeners()
        return builder.create()
    }

    private fun setClickListeners() {
        builder
            .setPositiveButton("Proceed") { dialog, _ ->
                dialog.dismiss()
                listener.proceed()
            }
            .setNegativeButton("Cancel") { _, _ -> listener.cancel()}
    }

    interface ClickListeners {
        fun proceed()
        fun cancel()
    }
}