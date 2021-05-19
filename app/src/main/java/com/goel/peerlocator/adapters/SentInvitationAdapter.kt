package com.goel.peerlocator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goel.peerlocator.R
import com.goel.peerlocator.models.UnknownUserModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class SentInvitationAdapter (private val context: Context,
            private val list : ArrayList<UnknownUserModel>, private val clickListener: ClickListener)
            : RecyclerView.Adapter<SentInvitationAdapter.SentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SentViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.invitation_card, parent, false)
        val holder = SentViewHolder(view)
        holder.removeButton.setOnClickListener { clickListener.onRemoveClicked(holder.adapterPosition) }
        return holder
    }

    override fun onBindViewHolder(holder: SentViewHolder, position: Int) {
        val model = list[position]
        val url = model.imageUrl
        val name = model.name
        holder.nameView.text = name
        Picasso.with(context).load(url)
            .placeholder(R.drawable.ic_placeholder_user)
            .transform(CropCircleTransformation())
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = list.size

    inner class SentViewHolder (itemView: View) : RecyclerView.ViewHolder (itemView) {
        val imageView : ImageView = itemView.findViewById(R.id.profile_picture)
        val nameView : TextView = itemView.findViewById(R.id.member_name)
        val removeButton : ImageView = itemView.findViewById(R.id.remove_button)
    }

    interface ClickListener {
        fun onRemoveClicked (position: Int)
    }
}