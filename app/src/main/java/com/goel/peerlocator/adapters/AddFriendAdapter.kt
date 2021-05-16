package com.goel.peerlocator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.models.UnknownUserModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class AddFriendAdapter (private val context: Context, private val usersList : ArrayList<UnknownUserModel>,
                        private val clickListener: ClickListeners) : RecyclerView.Adapter<AddFriendAdapter.AddFriendViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddFriendViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.details_card, parent, false)
        val viewHolder = AddFriendViewHolder(view)
        view.setOnClickListener { clickListener.onUserClicked(viewHolder.adapterPosition) }
        viewHolder.inviteButton.setOnClickListener { clickListener.onInviteClicked(viewHolder.adapterPosition) }
        viewHolder.photoView.setOnClickListener { clickListener.onPhotoClicked(viewHolder.adapterPosition) }
        return viewHolder
    }

    override fun onBindViewHolder(holder: AddFriendViewHolder, position: Int) {
        val model = usersList[position]
        val name = model.name
        val url = model.imageUrl
        holder.nameView.text = name
        Picasso.with(context).load(url).placeholder(R.drawable.ic_placeholder_user)
            .transform(CropCircleTransformation())
            .into(holder.photoView)
    }

    override fun getItemCount(): Int = usersList.size

    inner class AddFriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView : TextView = itemView.findViewById(R.id.card_profile_name)
        private val additional : TextView = itemView.findViewById(R.id.card_additional_detail)
        val photoView : ImageView = itemView.findViewById(R.id.card_profile_image)
        val controlsBar : LinearLayout = itemView.findViewById(R.id.controls_bar)
        private val acceptButton : TextView = itemView.findViewById(R.id.accept_request)
        private val rejectButton : TextView = itemView.findViewById(R.id.reject_request)
        private val infoButton : ImageView = itemView.findViewById(R.id.card_info)
        val inviteButton : TextView = itemView.findViewById(R.id.send_friend_request)
        init {
            additional.visibility = View.GONE
            controlsBar.visibility = View.VISIBLE
            acceptButton.visibility = View.GONE
            rejectButton.visibility = View.GONE
            infoButton.visibility = View.GONE
            inviteButton.visibility = View.VISIBLE
        }
    }

    interface ClickListeners {
        fun onInviteClicked (position: Int)
        fun onUserClicked (position: Int)
        fun onPhotoClicked (position: Int)
    }
}