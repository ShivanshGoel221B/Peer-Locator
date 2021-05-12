package com.goel.peerlocator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.models.FriendModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class AddMembersAdapter (private val context : Context, private val friendsList : ArrayList<FriendModel>,
                         private val addedList : ArrayList<FriendModel>, private val listener : AddMembersClickListeners)
    : RecyclerView.Adapter<AddMembersAdapter.MembersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.add_members_card, parent, false)
        val viewHolder = MembersViewHolder(view)
        viewHolder.addButton.setOnClickListener { listener.onAddClicked(viewHolder.adapterPosition) }
        viewHolder.removeButton.setOnClickListener { listener.onRemoveClicked(viewHolder.adapterPosition) }
        return viewHolder
    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        val model = friendsList[position]
        val name = model.friendName
        val url = model.imageUrl
        holder.profileName.text = name
        Picasso.with(context)
            .load(url)
            .transform(CropCircleTransformation())
            .placeholder(R.drawable.ic_placeholder_user)
            .into(holder.profileImage)
        if (model in addedList) {
            holder.addButton.visibility = View.GONE
            holder.removeButton.visibility = View.VISIBLE
        }
        else {
            holder.addButton.visibility = View.VISIBLE
            holder.removeButton.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int = friendsList.size


    inner class MembersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage : ImageView = itemView.findViewById(R.id.friend_profile_picture)
        val profileName : TextView = itemView.findViewById(R.id.friend_name)
        val addButton : ImageView = itemView.findViewById(R.id.add_member_button)
        val removeButton : ImageView = itemView.findViewById(R.id.remove_member_button)
    }

    interface AddMembersClickListeners {
        fun onAddClicked (position: Int)
        fun onRemoveClicked (position: Int)
    }
}