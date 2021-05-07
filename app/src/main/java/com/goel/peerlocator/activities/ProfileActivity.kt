package com.goel.peerlocator.activities

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.goel.peerlocator.R
import com.goel.peerlocator.databinding.ActivityProfileBinding
import com.goel.peerlocator.fragments.ImageViewFragment
import com.goel.peerlocator.listeners.ProfileDataListener
import com.goel.peerlocator.models.UserModel
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.Database
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
    }

    override fun onResume() {
        super.onResume()
        setData()
    }

    private fun setData () {
        model = Database.currentUser!!

        binding.profileName.text = model.displayName

        Database.getMyData(this)

        Picasso.with(this).load(model.photoUrl).placeholder(R.drawable.ic_placeholder_user)
                .transform(CropCircleTransformation()).into(binding.profilePhoto)
    }

    private fun setClickListeners () {
        // Profile Photo
        binding.profilePhoto.setOnClickListener {
            val imageViewFragment = ImageViewFragment.newInstance(url = model.photoUrl, editable = true,
                    isCircle = false, reference = model.documentReference)
            val transaction = supportFragmentManager.beginTransaction()
            transaction.addToBackStack(Constants.DP)
            transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
            transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
            transaction.commit()
        }

        //Edit Name
        binding.editName.setOnClickListener { editName() }
        binding.editNameDone.setOnClickListener { editNameSubmit(binding.editNameInput) }
        binding.editNameCancel.setOnClickListener { editNameDone() }
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
            if (newName != model.displayName)
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
        TODO("Not yet implemented")
    }

    override fun onNameChanged(name: String) {
        Database.currentUser?.displayName = name
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