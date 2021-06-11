package com.goel.peerlocator.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.adapters.MembersAdapter
import com.goel.peerlocator.databinding.ActivityCircleInfoBinding
import com.goel.peerlocator.dialogs.LoadingBasicDialog
import com.goel.peerlocator.fragments.ImageViewFragment
import com.goel.peerlocator.listeners.CircleDataListener
import com.goel.peerlocator.listeners.RemoveMemberListener
import com.goel.peerlocator.models.CircleModel
import com.goel.peerlocator.models.FriendModel
import com.goel.peerlocator.models.MemberModel
import com.goel.peerlocator.models.UnknownUserModel
import com.goel.peerlocator.repositories.CirclesRepository
import com.goel.peerlocator.utils.Constants
import com.goel.peerlocator.utils.firebase.database.Database
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class CircleInfoActivity : AppCompatActivity(), CircleDataListener, MembersAdapter.MemberClickedListener {

    companion object {
        lateinit var model : CircleModel
    }
    private lateinit var binding: ActivityCircleInfoBinding
    private lateinit var membersList : ArrayList<MemberModel>
    private lateinit var adapter : MembersAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCircleInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        createToolBar()
    }

    override fun onResume() {
        super.onResume()
        setData()
        setButtons()
    }

    private fun createToolBar() {
        val toolbar : androidx.appcompat.widget.Toolbar = binding.infoToolbar.root
        setSupportActionBar(toolbar)
        binding.infoToolbar.backButton.setOnClickListener {onBackPressed()}
        supportActionBar?.title = ""
    }

    private fun setButtons () {
        if (Database.currentUser.uid == model.adminReference.id) {
            binding.addMembersButton.visibility = View.VISIBLE
            binding.addMembersButton.setOnClickListener {

            }
        }
        else
            binding.addMembersButton.visibility = View.GONE

        binding.leaveCircleButton.setOnClickListener {
            showLeaveWarning()
        }
    }

    private fun setData () {
        val photoUrl = model.imageUrl
        val name = model.name
        binding.infoToolbar.profileName.text = name
        Picasso.with(this).load(photoUrl).placeholder(R.drawable.ic_placeholder_circle_big).into(binding.profileImageHolder)
        createRecyclerView()
    }

    private fun createRecyclerView () {
        membersList = ArrayList()
        adapter = MembersAdapter(membersList, this, this)
        binding.infoMembersRecyclerView.adapter = adapter
        val lm = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
        binding.infoMembersRecyclerView.layoutManager = lm
        CirclesRepository.instance.getAllMembers(model.documentReference, this)
    }

    private fun showLeaveWarning () {
        AlertDialog.Builder(this)
            .setTitle(R.string.leave_circle)
            .setMessage("Are you sure you want to leave this circle?")
            .setNegativeButton(R.string.no) {dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.yes) {dialog, _ ->
                dialog.dismiss()
                val loadingDialog = LoadingBasicDialog("Leaving Circle")
                leaveCircle(loadingDialog)
            }.show()
    }

    private fun leaveCircle(loadingDialog: LoadingBasicDialog) {
        loadingDialog.show(supportFragmentManager, "Removing Member")
        CirclesRepository.instance.leaveCircle(model.documentReference,
            model.adminReference.id == Database.currentUser.uid,
            object : RemoveMemberListener {
                override fun memberRemoved(member: MemberModel) {
                    loadingDialog.dismiss()
                    finish()
                    Toast.makeText(this@CircleInfoActivity, "Left ${model.name}", Toast.LENGTH_SHORT).show()
                }

                override fun onError() {
                    loadingDialog.dismiss()
                    Toast.makeText(this@CircleInfoActivity, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                }
            })
    }


    // Circle Listeners
    override fun onMemberCountComplete(members: Long) {
        binding.infoMembersCount.text = resources.getQuantityString(R.plurals.members_count, members.toInt(), members)
    }


    private fun openMember (member: MemberModel) {
        when (member.flag) {
            Constants.FRIEND -> {
                FriendActivity.friend = FriendModel(documentReference = member.documentReference, name = member.name, imageUrl = member.imageUrl)
                startActivity(Intent(this, FriendActivity::class.java))
            }
            Constants.UNKNOWN -> {
                UserInfoActivity.model = UnknownUserModel(documentReference = member.documentReference,
                    uid = member.uid, name = member.name, imageUrl = member.imageUrl)
                startActivity(Intent(this, UserInfoActivity::class.java))
                Toast.makeText(this, "You must be friend with this user to access location", Toast.LENGTH_SHORT).show()
            }
            Constants.INACCESSIBLE -> {
                Toast.makeText(this, R.string.inaccessible_user_warning, Toast.LENGTH_SHORT).show()
            }
            Constants.ME -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }
    }

    private fun openMemberInfo (member: MemberModel) {
        when (member.flag) {
            Constants.FRIEND -> {
                FriendInfoActivity.model = FriendModel(documentReference = member.documentReference, name = member.name, imageUrl = member.imageUrl)
                startActivity(Intent(this, FriendInfoActivity::class.java))
            }
            Constants.UNKNOWN -> {
                UserInfoActivity.model = UnknownUserModel(documentReference = member.documentReference,
                                        uid = member.uid, name = member.name, imageUrl = member.imageUrl)
                startActivity(Intent(this, UserInfoActivity::class.java))
            }
            Constants.INACCESSIBLE -> {
                Toast.makeText(this, R.string.inaccessible_user_warning, Toast.LENGTH_SHORT).show()
            }
            Constants.ME -> {
                startActivity(Intent(this, ProfileActivity::class.java))
            }
        }
    }

    private fun loadPhoto (member: MemberModel) {
        val imageViewFragment = ImageViewFragment.newInstance(url = member.imageUrl, isCircle = false)
        val transaction = supportFragmentManager.beginTransaction()
        transaction.addToBackStack(Constants.DP)
        transaction.setCustomAnimations(R.anim.enter_from_bottom, R.anim.exit_to_bottom, R.anim.enter_from_bottom, R.anim.exit_to_bottom)
        transaction.replace(R.id.profile_photo_container, imageViewFragment, Constants.DP)
        transaction.commit()
    }

    override fun onMemberRetrieved(member: MemberModel) {
        if(member.documentReference.id == model.adminReference.id) {
            binding.infoAdminCard.cardProfileName.text = member.name
            Picasso.with(this).load(member.imageUrl)
                .placeholder(R.drawable.ic_placeholder_user)
                .transform(CropCircleTransformation())
                .into(binding.infoAdminCard.cardProfileImage)
            binding.infoAdminCard.cardAdditionalDetail.visibility = View.GONE
            binding.infoAdminCard.root.setOnClickListener { openMember(member) }
            binding.infoAdminCard.cardInfo.setOnClickListener { openMemberInfo(member) }
            binding.infoAdminCard.cardProfileImage.setOnClickListener { loadPhoto(member) }
        }
        else {
            membersList.add(member)
            adapter.notifyDataSetChanged()
        }
        onMemberCountComplete((membersList.size + 1).toLong())
    }

    private fun showRemoveWarning (member: MemberModel) {
        AlertDialog.Builder(this).setTitle(R.string.remove_member_title)
            .setMessage("Are you sure you want to remove ${member.name} from ${model.name}?")
            .setNegativeButton(R.string.no) {dialog, _ ->
                dialog.dismiss()
            }
            .setPositiveButton(R.string.yes) {dialog, _ ->
                dialog.dismiss()
                val loadingDialog = LoadingBasicDialog("Removing member")
                removeMember (member, loadingDialog)
            }.show()
    }

    private fun removeMember(member: MemberModel, loadingDialog: LoadingBasicDialog) {
        loadingDialog.show(supportFragmentManager, "Removing Member")
        CirclesRepository.instance.removeMember(model.documentReference, member,
            object : RemoveMemberListener {
                override fun memberRemoved(member: MemberModel) {
                    membersList.remove(member)
                    onMemberCountComplete((membersList.size + 1).toLong())
                    adapter.notifyDataSetChanged()
                    loadingDialog.dismiss()
                    Toast.makeText(this@CircleInfoActivity, "${member.name} Removed", Toast.LENGTH_SHORT).show()
                }

                override fun onError() {
                    loadingDialog.dismiss()
                    Toast.makeText(this@CircleInfoActivity, "Some Error Occurred", Toast.LENGTH_SHORT).show()
                }
            })
    }

    override fun onError() {
        Toast.makeText(this, R.string.error_message, Toast.LENGTH_SHORT).show()
        finish()
    }

    //Friends Click Listeners
    override fun onMemberClicked(position: Int) {
        val member = membersList[position]
        openMember(member)
    }

    override fun onMemberPhotoClicked(position: Int) {
        val member = membersList[position]
        loadPhoto(member)
    }

    override fun onMemberInfoClicked(position: Int) {
        val member = membersList[position]
        openMemberInfo(member)
    }

    override fun onLongClicked(position: Int): Boolean {
        if (Database.currentUser.uid != model.adminReference.id)
            return false
        val member = membersList[position]
        if (member.flag == Constants.ME)
            return false
        showRemoveWarning (member)
        return true
    }
}