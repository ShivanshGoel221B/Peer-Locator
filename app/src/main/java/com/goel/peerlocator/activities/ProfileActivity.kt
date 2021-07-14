package com.goel.peerlocator.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivityProfileBinding
import com.goel.peerlocator.fragments.BlockListFragment
import com.goel.peerlocator.fragments.ImageViewFragment
import com.goel.peerlocator.listeners.ProfileDataListener
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.services.ServicesHandler
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.Database
import com.goel.peerlocator.utils.firebase.storage.Storage
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

class ProfileActivity : AppCompatActivity(), ProfileDataListener {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var model: UserModel
    private val imageResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val intent = result.data
                intent?.let {
                    val inputStream = contentResolver.openInputStream(it.data!!)
                    val size = contentResolver.openInputStream(it.data!!)!!.readBytes().size
                    when {
                        size > Constants.MAX_IMAGE_SIZE ->
                            Toast.makeText(
                                this,
                                getString(R.string.image_size_warning),
                                Toast.LENGTH_LONG
                            ).show()
                        else -> {
                            binding.profilePhotoProgress.visibility = View.VISIBLE
                            Storage.uploadProfileImage(model, inputStream!!, this)
                        }
                    }
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setClickListeners()
        setData()
    }

    private fun setData() {
        model = Database.currentUser

        binding.profileName.text = model.name

        Database.getMyData(this)

        Glide.with(this).load(model.imageUrl).placeholder(R.drawable.ic_placeholder_user)
            .circleCrop().into(binding.profilePhoto)
        binding.profilePhotoProgress.visibility = View.GONE
    }

    private fun setClickListeners() {
        // Profile Photo
        binding.profilePhoto.setOnClickListener {
            val imageViewFragment =
                ImageViewFragment.newInstance(url = model.imageUrl, isCircle = false)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.addToBackStack(Constants.DP)
            transaction.setCustomAnimations(
                R.anim.enter_from_bottom,
                R.anim.exit_to_bottom,
                R.anim.enter_from_bottom,
                R.anim.exit_to_bottom
            )
            transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
            transaction.commit()
        }

        //Block List
        binding.blockListButton.setOnClickListener {
            val blockListFragment = BlockListFragment.newInstance()
            val transaction = supportFragmentManager.beginTransaction()
            transaction.addToBackStack(Constants.BLOCKS)
            transaction.setCustomAnimations(
                R.anim.enter_from_bottom,
                R.anim.exit_to_bottom,
                R.anim.enter_from_bottom,
                R.anim.exit_to_bottom
            )
            transaction.replace(R.id.profile_photo_container, blockListFragment, Constants.BLOCKS)
            transaction.commit()
        }

        //Edit
        binding.editName.setOnClickListener { editName() }
        binding.editNameDone.setOnClickListener { editNameSubmit(binding.editNameInput) }
        binding.editNameCancel.setOnClickListener { editNameDone() }
        binding.camera.setOnClickListener { pickImage() }
        binding.onlineSwitch.setOnClickListener { changeOnlineStatus() }
        binding.visibleSwitch.setOnClickListener { changeVisibilityStatus() }
        binding.logoutButton.setOnClickListener { showLogoutWarning() }
    }

    private fun editName() {
        binding.profileName.visibility = View.GONE
        binding.editName.visibility = View.GONE
        binding.editNameInput.setText(binding.profileName.text, TextView.BufferType.EDITABLE)
        binding.editNameInput.visibility = View.VISIBLE
        binding.editNameDone.visibility = View.VISIBLE
        binding.editNameCancel.visibility = View.VISIBLE
        binding.editNameInput.requestFocus()
        binding.editNameInput.showKeyboard()
    }

    private fun editNameDone() {
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
        } else {
            input.error = validity[false]
        }
    }

    private fun View.showKeyboard() {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.toggleSoftInputFromWindow(
            applicationWindowToken,
            InputMethodManager.SHOW_FORCED,
            0
        )
    }

    private fun View.hideKeyboard() {
        val inputMethodManager =
            context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(windowToken, 0)
    }

    private fun pickImage() {
        ImagePicker.with(this)
            .cropSquare()
            .compress(1024)
            .maxResultSize(1080, 1080)
            .galleryMimeTypes(Constants.IMAGE_FILE_TYPES)
            .createIntent {
                imageResult.launch(it)
            }
    }

    // Change online status and visibility
    private fun changeOnlineStatus() {
        Database.changeOnlineStatus(binding.onlineSwitch.isChecked, this)
    }

    private fun changeVisibilityStatus() {
        Database.changeVisibilityStatus(binding.visibleSwitch.isChecked, this)
    }

    //Log out
    private fun showLogoutWarning() {
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

    private fun userLogout() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        GoogleSignIn.getClient(this, gso).signOut()
            .addOnFailureListener {
                Toast.makeText(
                    applicationContext,
                    "Failed to sign out",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .addOnSuccessListener {
                Toast.makeText(applicationContext, "Signed out successfully", Toast.LENGTH_SHORT)
                    .show()
                ServicesHandler.stopBackgroundLocation(this)
                startActivity(Intent(this, SplashActivity::class.java))
                finishAffinity()
            }
    }

    // Data Listeners

    override fun friendsCountComplete(count: Long) {
        binding.friendsCounter.text =
            resources.getQuantityString(R.plurals.friends_count, count.toInt(), count)
    }

    override fun circlesCountComplete(count: Long) {
        binding.circlesCounter.text =
            resources.getQuantityString(R.plurals.circles_count, count.toInt(), count)
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
        Glide.with(this).load(url).placeholder(R.drawable.ic_placeholder_user)
            .circleCrop().into(binding.profilePhoto)
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