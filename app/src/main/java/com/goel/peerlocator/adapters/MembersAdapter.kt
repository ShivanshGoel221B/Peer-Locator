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
import com.goel.peerlocator.models.MemberModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class MembersAdapter (private val membersList: ArrayList<MemberModel>, private val context: Context,
                      private val clickListener: MemberClickedListener)
    : RecyclerView.Adapter<MembersAdapter.MembersViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MembersAdapter.MembersViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.details_card, parent, false)
        val viewHolder = MembersViewHolder(view)
        view.setOnClickListener {clickListener.onMemberClicked(viewHolder.adapterPosition)}
        viewHolder.memberImage.setOnClickListener {clickListener.onMemberPhotoClicked(viewHolder.adapterPosition)}
        viewHolder.infoButton.setOnClickListener {clickListener.onMemberInfoClicked(viewHolder.adapterPosition)}
        return viewHolder
    }

    override fun onBindViewHolder(holder: MembersViewHolder, position: Int) {
        val member = membersList[position]
        val name = member.name
        val url = member.imageUrl

        holder.memberName.text = name

        Picasso.with(context).load(url).placeholder(R.drawable.ic_placeholder_user)
            .transform(CropCircleTransformation())
            .into(holder.memberImage)
    }

    override fun getItemCount() = membersList.size

    inner class MembersViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val memberImage : ImageView = itemView.findViewById(R.id.card_profile_image)
        val memberName : TextView = itemView.findViewById(R.id.card_profile_name)
        private val commonCirclesCount : TextView = itemView.findViewById(R.id.card_additional_detail)
        private val controlsBar : LinearLayout = itemView.findViewById(R.id.controls_bar)
        val infoButton : ImageView = itemView.findViewById(R.id.card_info)
        init {
            controlsBar.visibility = View.GONE
            commonCirclesCount.visibility = View.GONE
        }
    }

    interface MemberClickedListener {
        fun onMemberClicked (position: Int)
        fun onMemberPhotoClicked (position: Int)
        fun onMemberInfoClicked (position: Int)
    }
}