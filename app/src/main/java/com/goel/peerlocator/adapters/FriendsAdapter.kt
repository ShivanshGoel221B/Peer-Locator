package com.goel.peerlocator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goel.peerlocator.R
import com.goel.peerlocator.models.FriendModel

class FriendsAdapter (private val friendsList: ArrayList<FriendModel>, private val context: Context,
                      private val clickListener: FriendClickListener)
                    : RecyclerView.Adapter<FriendsAdapter.FriendsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendsViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.details_card, parent, false)
        val viewHolder = FriendsViewHolder(view)
        view.setOnClickListener {clickListener.onFriendClicked(viewHolder.adapterPosition)}
        viewHolder.friendImage.setOnClickListener {clickListener.onFriendPhotoClicked(viewHolder.adapterPosition)}
        viewHolder.infoButton.setOnClickListener {clickListener.onFriendInfoClicked(viewHolder.adapterPosition)}
        return viewHolder
    }

    override fun onBindViewHolder(holder: FriendsViewHolder, position: Int) {
        val friend = friendsList[position]
        val friendName = friend.name
        val profileUrl = friend.imageUrl
        val commonCircleCount = holder.itemView.resources
                                .getQuantityString(R.plurals.common_circles_count, friend.commonCirclesCount, friend.commonCirclesCount)

        holder.friendName.text = friendName
        holder.commonCirclesCount.text = commonCircleCount
        Glide.with(context).load(profileUrl).placeholder(R.drawable.ic_placeholder_user)
            .circleCrop()
            .into(holder.friendImage)
    }

    override fun getItemCount() = friendsList.size

    inner class FriendsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendImage : ImageView = itemView.findViewById(R.id.card_profile_image)
        val friendName : TextView = itemView.findViewById(R.id.card_profile_name)
        val commonCirclesCount : TextView = itemView.findViewById(R.id.card_additional_detail)
        private val controlsBar : LinearLayout = itemView.findViewById(R.id.controls_bar)
        val infoButton : ImageView = itemView.findViewById(R.id.card_info)
        init {
            controlsBar.visibility = View.GONE
        }
    }

    interface FriendClickListener {
        fun onFriendClicked (position: Int)
        fun onFriendPhotoClicked (position: Int)
        fun onFriendInfoClicked (position: Int)
    }
}