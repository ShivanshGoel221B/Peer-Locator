package com.goel.peerlocator.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.NewCircleAdapter
import com.goel.peerlocator.databinding.ActivityNewCircleBinding
import com.goel.peerlocator.dialogs.DoneDialog
import com.goel.peerlocator.dialogs.LoadingBasicDialog
import com.goel.peerlocator.fragments.AddMembersFragment
import com.goel.peerlocator.listeners.EditCircleListener
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.Database
import com.goel.peerlocator.viewmodels.NewCircleViewModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation
import java.io.InputStream

class NewCircleActivity : AppCompatActivity(), NewCircleAdapter.NewCircleClickListener,
    EditCircleListener {

    private lateinit var binding : ActivityNewCircleBinding
    private lateinit var viewModel: NewCircleViewModel
    private lateinit var adapter : NewCircleAdapter
    private lateinit var membersCounter : TextView
    private var membersCount : Int = 1
    private var imageStream: InputStream? = null
    private lateinit var loadingDialogBox: LoadingBasicDialog
    private lateinit var doneDialog: DoneDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewCircleBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setViews()
        setRecyclerView()
        initializeDialogs()
        setClickListeners()
    }

    private fun setViews () {
        membersCounter = binding.circleMembersCounter
        Picasso.with(this)
            .load(Database.currentUser?.imageUrl)
            .placeholder(R.drawable.ic_placeholder_user)
            .transform(CropCircleTransformation())
            .into(binding.myProfilePicture)
        membersCount = 1
        updateCounter(membersCount)
    }

    private fun updateCounter (number : Int) {
        membersCounter.text = resources.getQuantityString(R.plurals.members_count, number, number)
    }

    private fun setRecyclerView() {
        viewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(NewCircleViewModel::class.java)
        adapter = NewCircleAdapter(this, viewModel.membersList, this)
        binding.membersRecyclerView.adapter = adapter

        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.membersRecyclerView.layoutManager = manager
    }

    private fun initializeDialogs () {
        loadingDialogBox = LoadingBasicDialog(getString(R.string.creating_circle))
        doneDialog = DoneDialog(getString(R.string.circle_created), object : DoneDialog.ClickListener {
            override fun onOkClicked() {
                doneDialog.dismiss()
                finish()
            }
        })
    }

    private fun setClickListeners() {
        binding.addMembersButton.setOnClickListener {
            if (membersCount < Constants.MAX_CIRCLE_SIZE) {
                val addMembersFragment = AddMembersFragment.newInstance(viewModel.membersList)
                val transaction = supportFragmentManager.beginTransaction()
                transaction.addToBackStack(getString(R.string.add_members))
                transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
                transaction.replace(R.id.add_members_fragment_container, addMembersFragment, getString(R.string.add_members))
                transaction.commit()
            }
            else {
                Toast.makeText(this, R.string.circle_size_warning, Toast.LENGTH_LONG).show()
            }
        }
        binding.editNameDone.setOnClickListener {
            it.hideKeyboard()
            binding.editNameInput.clearFocus()
        }

        binding.camera.setOnClickListener { checkStoragePermission() }

        binding.cancelButton.setOnClickListener { finish() }
        binding.submitButton.setOnClickListener {
            createCircle ()
        }
    }

    private fun createCircle() {
        val name = binding.editNameInput.text.toString()
        val nameValidity = Constants.isNameValid(name)
        if (true in nameValidity.keys) {
            loadingDialogBox.show(supportFragmentManager, "loading dialog")
            viewModel.createCircle(name, imageStream, this)
        }
        else
        {
            val message = nameValidity[false]
            binding.editNameInput.error = message
        }
    }

    // Profile Picture

    private fun checkStoragePermission () {
        if (ContextCompat.checkSelfPermission(applicationContext, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            == PackageManager.PERMISSION_GRANTED) {
            uploadImage()
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                Constants.READ_STORAGE_PERMISSION_REQUEST_CODE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == Constants.READ_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED)
                uploadImage()
        }
    }

    private fun uploadImage () {
        val imageIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(imageIntent, Constants.IMAGE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == Constants.IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            data?.let {
                val type = contentResolver.getType(it.data!!)
                val size = contentResolver.openInputStream(it.data!!)!!.readBytes().size
                when {
                    type !in Constants.IMAGE_FILE_TYPES ->
                        Toast.makeText(this, getString(R.string.image_type_warning), Toast.LENGTH_LONG).show()
                    size > Constants.MAX_IMAGE_SIZE ->
                        Toast.makeText(this, getString(R.string.image_size_warning), Toast.LENGTH_LONG).show()
                    else -> {
                        imageStream = contentResolver.openInputStream(it.data!!)
                        Picasso.with(this).load(it.data)
                            .transform(CropCircleTransformation()).into(binding.circleProfilePhoto)
                    }
                }
            }
        }
    }

    ////////////////////////////////////////////////

    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    override fun onBackPressed() {
        super.onBackPressed()
        adapter.notifyDataSetChanged()
        updateCounter(viewModel.membersList.size)
    }

    override fun onRemoveClick(position: Int) {
        adapter.notifyItemRemoved(position)
        viewModel.membersList.removeAt(position)
    }

    override fun onCreationSuccessful() {
        loadingDialogBox.setMessage(getString(R.string.inviting_members))
    }

    override fun membersAdditionSuccessful() {
        loadingDialogBox.setMessage(getString(R.string.finishing_up))
        loadingDialogBox.dismiss()
        doneDialog.show(supportFragmentManager, "done loading")
    }

    override fun onError() {
        loadingDialogBox.dismiss()
        doneDialog.dismiss()
        Toast.makeText(applicationContext, R.string.error_message, Toast.LENGTH_SHORT).show()
    }
}