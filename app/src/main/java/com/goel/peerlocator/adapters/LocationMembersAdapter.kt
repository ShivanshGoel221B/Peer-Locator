package com.goel.peerlocator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.models.LocationMemberModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class LocationMembersAdapter (private val membersList: ArrayList<LocationMemberModel>,
                              private val context: Context,
                              private val clickListener: ClickListener):
    RecyclerView.Adapter<LocationMembersAdapter.MembersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.location_members_card, parent, false)
        val viewHolder = MembersViewHolder(view)
        view.setOnClickListener {
            clickListener.onMemberClicked(viewHolder.adapterPosition)
        }
        return viewHolder
    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        val model = membersList[position]
        holder.nameView.text = model.name
        Picasso.with(context).load(model.imageUrl)
            .transform(CropCircleTransformation())
            .placeholder(R.drawable.ic_placeholder_user)
            .into(holder.photoView)
    }

    override fun getItemCount(): Int = membersList.size


    inner class MembersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameView : TextView = itemView.findViewById(R.id.card_profile_name)
        val photoView : ImageView = itemView.findViewById(R.id.card_profile_image)
    }

    interface ClickListener {
        fun onMemberClicked(position: Int)
    }
}