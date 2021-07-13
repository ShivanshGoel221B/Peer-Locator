package com.goel.peerlocator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goel.peerlocator.R
import com.goel.peerlocator.models.FriendModel

class NewCircleAdapter (private val context : Context, private val membersList : ArrayList<FriendModel>,
                        private val listener : NewCircleClickListener)
    : RecyclerView.Adapter<NewCircleAdapter.NewMembersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewMembersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.invitation_card, parent, false)
        val viewHolder = NewMembersViewHolder(view)
        viewHolder.removeButton.setOnClickListener {
            if (viewHolder.adapterPosition != RecyclerView.NO_POSITION)
                listener.onRemoveClick(viewHolder.adapterPosition)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: NewMembersViewHolder, position: Int) {
        val model = membersList[position]
        val name = model.name
        val url = model.imageUrl
        holder.nameHolder.text = name
        Glide.with(context).load(url)
            .circleCrop()
            .placeholder(R.drawable.ic_placeholder_user)
            .into(holder.imageHolder)
    }

    override fun getItemCount(): Int = membersList.size

    inner class NewMembersViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageHolder : ImageView = itemView.findViewById(R.id.profile_picture)
        val nameHolder : TextView = itemView.findViewById(R.id.member_name)
        val removeButton : ImageView = itemView.findViewById(R.id.remove_button)
    }

    interface NewCircleClickListener {
        fun onRemoveClick (position: Int)
    }

}