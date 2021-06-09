package com.goel.peerlocator.activities

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivityProfileBinding
import com.goel.peerlocator.fragments.BlockListFragment
import com.goel.peerlocator.fragments.ImageViewFragment
import com.goel.peerlocator.listeners.ProfileDataListener
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.Database
import com.goel.peerlocator.utils.firebase.storage.Storage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class ProfileActivity : AppCompatActivity(), ProfileDataListener {

    private lateinit var binding : ActivityProfileBinding
    private lateinit var model : UserModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClickListeners ()
        setData()
    }

    private fun setData () {
        model = Database.currentUser

        binding.profileName.text = model.name

        Database.getMyData(this)

        Picasso.with(this).load(model.imageUrl).placeholder(R.drawable.ic_placeholder_user)
                .transform(CropCircleTransformation()).into(binding.profilePhoto)
        binding.profilePhotoProgress.visibility = View.GONE
    }

    private fun setClickListeners () {
        // Profile Photo
        binding.profilePhoto.setOnClickListener {
            val imageViewFragment = ImageViewFragment.newInstance(url = model.imageUrl, isCircle = false)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.addToBackStack(Constants.DP)
            transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
            transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
            transaction.commit()
        }

        //Block List
        binding.blockListButton.setOnClickListener {
            val blockListFragment = BlockListFragment.newInstance()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.addToBackStack(Constants.BLOCKS)
            transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
            transaction.replace(R.id.profile_photo_container, blockListFragment, Constants.BLOCKS)
            transaction.commit()
        }

        //Edit
        binding.editName.setOnClickListener { editName() }
        binding.editNameDone.setOnClickListener { editNameSubmit(binding.editNameInput) }
        binding.editNameCancel.setOnClickListener { editNameDone() }
        binding.camera.setOnClickListener { checkStoragePermission () }
        binding.onlineSwitch.setOnClickListener { changeOnlineStatus() }
        binding.visibleSwitch.setOnClickListener { changeVisibilityStatus() }
        binding.logoutButton.setOnClickListener { showLogoutWarning() }
    }

    private fun editName () {
        binding.profileName.visibility = View.GONE
        binding.editName.visibility = View.GONE
        binding.editNameInput.setText(binding.profileName.text, TextView.BufferType.EDITABLE)
        binding.editNameInput.visibility = View.VISIBLE
        binding.editNameDone.visibility = View.VISIBLE
        binding.editNameCancel.visibility = View.VISIBLE
        binding.editNameInput.requestFocus()
        binding.editNameInput.showKeyboard()
    }

    private fun editNameDone () {
        binding.editNameInput.setText("", TextView.BufferType.EDITABLE)
        binding.profileName.visibility = View.VISIBLE
        binding.editName.visibility = View.VISIBLE
        binding.editNameInput.visibility = View.GONE
        binding.editNameDone.visibility = View.GONE
        binding.editNameCancel.visibility = View.GONE
        binding.editNameInput.hideKeyboard()
    }

    private fun editNameSubmit(input: EditText) {
        val newName = input.text.toString()
        val validity = Constants.isNameValid(newName)
        if (validity.contains(true)) {
            if (newName != model.name)
                Database.changeName(model.documentReference, newName, this)
            editNameDone()
        }
        else {
            input.error = validity[false]
        }
    }

    private fun View.showKeyboard () {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(applicationWindowToken, InputMethodManager.SHOW_FORCED, 0)
    }

    private fun View.hideKeyboard() {
        val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    // change profile pictures
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
                val inputStream = contentResolver.openInputStream(it.data!!)
                val type = contentResolver.getType(it.data!!)
                val size = contentResolver.openInputStream(it.data!!)!!.readBytes().size
                when {
                    type !in Constants.IMAGE_FILE_TYPES ->
                        Toast.makeText(this, getString(R.string.image_type_warning), Toast.LENGTH_LONG).show()
                    size > Constants.MAX_IMAGE_SIZE ->
                        Toast.makeText(this, getString(R.string.image_size_warning), Toast.LENGTH_LONG).show()
                    else -> {
                        binding.profilePhotoProgress.visibility = View.VISIBLE
                        Storage.uploadProfileImage(model, inputStream!!, this)
                    }
                }
            }
        }
    }

    // Change online status and visibility
    private fun changeOnlineStatus () {
        Database.changeOnlineStatus(binding.onlineSwitch.isChecked, this)
    }
    private fun changeVisibilityStatus () {
        Database.changeVisibilityStatus(binding.visibleSwitch.isChecked, this)
    }

    //Log out
    private fun showLogoutWarning () {
        AlertDialog.Builder(this).setTitle(R.string.log_out)
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton(R.string.ok) { dialog, _ ->
                    FirebaseAuth.getInstance().signOut()
                    dialog.dismiss()
                    userLogout()
                }
                .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
                .show()
    }

    private fun userLogout () {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

        GoogleSignIn.getClient(this, gso).signOut()
                .addOnFailureListener { Toast.makeText(applicationContext, "Failed to sign out", Toast.LENGTH_SHORT).show() }
                .addOnSuccessListener {
                    Toast.makeText(applicationContext, "Signed out successfully", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, SplashActivity::class.java))
                    finishAffinity()
                }
    }

    // Data Listeners

    override fun friendsCountComplete(count: Long) {
        binding.friendsCounter.text = resources.getQuantityString(R.plurals.friends_count, count.toInt(), count)
    }

    override fun circlesCountComplete(count: Long) {
        binding.circlesCounter.text = resources.getQuantityString(R.plurals.circles_count, count.toInt(), count)
    }

    override fun onlineStatusFetched(online: Boolean) {
        binding.onlineSwitch.isChecked = online
    }

    override fun visibilityStatusFetched(visible: Boolean) {
        binding.visibleSwitch.isChecked = visible
    }

    override fun onEmailFound(exist: Boolean, email: String) {
        if (!exist) {
            binding.profileEmailHolder.visibility = View.GONE
        } else {
            binding.profileEmail.text = email
        }
    }

    override fun onPhoneFound(exist: Boolean, phone: String) {
        if (!exist) {
            binding.profilePhoneHolder.visibility = View.GONE
        } else {
            binding.profilePhone.text = phone
        }
    }

    override fun onPhotoChanged(url: String) {
        model.imageUrl = url
        Picasso.with(this).load(url).placeholder(R.drawable.ic_placeholder_user)
                .transform(CropCircleTransformation()).into(binding.profilePhoto)
        binding.profilePhotoProgress.visibility = View.GONE
    }

    override fun onNameChanged(name: String) {
        Database.currentUser.name = name
        binding.profileName.text = name
        Toast.makeText(this, "Name Changed", Toast.LENGTH_SHORT).show()
    }

    override fun onOnlineStatusChanged(online: Boolean) {
        val message = if (online) "Went Online" else "Went Offline"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onVisibilityStatusChanged(visible: Boolean) {
        val message = if (visible) "Became Visible" else "Became Invisible"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun networkError() {
        Toast.makeText(this, "Failed to Connect", Toast.LENGTH_SHORT).show()
    }
}