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
import com.goel.peerlocator.models.InviteModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class InvitesAdapter (private val invitesList: ArrayList<InviteModel>, private val context: Context,
                      private val clickListener: InviteClickListener) : RecyclerView.Adapter<InvitesAdapter.InvitesViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InvitesViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.details_card, parent, false)
        val viewHolder = InvitesViewHolder(view)
        view.setOnClickListener {clickListener.onInviteClicked(viewHolder.adapterPosition)}
        viewHolder.photoView.setOnClickListener {clickListener.onInvitePhotoClicked(viewHolder.adapterPosition)}
        viewHolder.acceptButton.setOnClickListener {clickListener.onAcceptClicked(viewHolder.adapterPosition)}
        viewHolder.rejectButton.setOnClickListener {clickListener.onRejectClicked(viewHolder.adapterPosition)}
        return viewHolder
    }

    override fun onBindViewHolder(holder: InvitesViewHolder, position: Int) {
        val invite = invitesList[position]
        val reference = invite.documentReference.path
        val placeHolder = if ("circles/" in reference)
            R.drawable.ic_placeholder_circle
        else
            R.drawable.ic_placeholder_user

        val name = invite.name
        val profileUrl = invite.imageUrl
        val timeStamp = invite.timeStamp

        holder.nameView.text = name
        holder.additional.text = timeStamp
        Picasso.with(context).load(profileUrl).placeholder(placeHolder)
            .transform(CropCircleTransformation())
            .into(holder.photoView)
    }

    override fun getItemCount(): Int = invitesList.size

    inner class InvitesViewHolder (itemView: View) : RecyclerView.ViewHolder (itemView) {
        val nameView : TextView = itemView.findViewById(R.id.card_profile_name)
        val additional : TextView = itemView.findViewById(R.id.card_additional_detail)
        val photoView : ImageView = itemView.findViewById(R.id.card_profile_image)
        val controlsBar : LinearLayout = itemView.findViewById(R.id.controls_bar)
        val acceptButton : TextView = itemView.findViewById(R.id.accept_request)
        val rejectButton : TextView = itemView.findViewById(R.id.reject_request)
        private val inviteButton : TextView = itemView.findViewById(R.id.send_friend_request)
        private val infoButton : ImageView = itemView.findViewById(R.id.card_info)
        init {
            controlsBar.visibility = View.VISIBLE
            infoButton.visibility = View.GONE
            inviteButton.visibility = View.GONE
        }
    }

    interface InviteClickListener {
        fun onInviteClicked (position: Int)
        fun onInvitePhotoClicked (position: Int)
        fun onAcceptClicked (position: Int)
        fun onRejectClicked (position: Int)
    }
}