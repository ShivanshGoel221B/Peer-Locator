package com.goel.peerlocator.dialogs

import android.annotation.SuppressLint
import android.app.Dialog
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDialogFragment
import com.goel.peerlocator.R

class CreateProfileDialog (private val listener: ClickListener): AppCompatDialogFragment() {

    private lateinit var builder : AlertDialog.Builder
    private lateinit var dialogView : View

    @SuppressLint("InflateParams")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        builder = AlertDialog.Builder(context!!)
        builder.setCancelable(false)
        dialogView = activity!!.layoutInflater.inflate(R.layout.create_profile_layout, null, false)
        builder.setView(dialogView)
        this.isCancelable = false
        setClickListeners ()
        return builder.create()
    }

    private fun setClickListeners () {
        dialogView.findViewById<ImageView>(R.id.close_button).setOnClickListener { listener.onCancelClicked() }
        dialogView.findViewById<Button>(R.id.create_profile_btn).setOnClickListener {
            val editText = dialogView.findViewById<EditText>(R.id.editTextName)
            listener.onCreateClicked(editText)
        }
    }

    interface ClickListener {
        fun onCreateClicked (editTextName: EditText)
        fun onCancelClicked ()
    }
}