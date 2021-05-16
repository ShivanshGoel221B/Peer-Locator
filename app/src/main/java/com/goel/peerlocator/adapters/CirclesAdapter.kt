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
import com.goel.peerlocator.models.CircleModel
import com.squareup.picasso.Picasso
import jp.wasabeef.picasso.transformations.CropCircleTransformation

class CirclesAdapter(private val circleList: ArrayList<CircleModel>, private val context: Context,
                     private val clickListener: CircleClickListener)
                    : RecyclerView.Adapter<CirclesAdapter.CircleViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CircleViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.details_card, parent, false)
        val viewHolder = CircleViewHolder(view)
        view.setOnClickListener { clickListener.onCircleClicked(viewHolder.adapterPosition) }
        view.setOnLongClickListener {clickListener.onCircleLongClicked(viewHolder.adapterPosition)}
        viewHolder.circleImage.setOnClickListener {clickListener.onCirclePhotoClicked(viewHolder.adapterPosition)}
        viewHolder.infoButton.setOnClickListener {clickListener.onInfoClicked(viewHolder.adapterPosition)}
        return viewHolder
    }

    override fun onBindViewHolder(holder: CircleViewHolder, position: Int) {
        val circle = circleList[position]
        val imageUri = circle.imageUrl
        val circleName = circle.name
        val membersCount = holder.itemView.resources.getQuantityString(R.plurals.members_count, circle.memberCount, circle.memberCount)

        holder.circleName.text = circleName
        holder.membersCount.text = membersCount
        Picasso.with(context).load(imageUri).placeholder(R.drawable.ic_placeholder_circle)
            .transform(CropCircleTransformation())
            .into(holder.circleImage)
    }

    override fun getItemCount(): Int = circleList.size

    inner class CircleViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val circleImage : ImageView = itemView.findViewById(R.id.card_profile_image)
        val circleName : TextView = itemView.findViewById(R.id.card_profile_name)
        val membersCount : TextView = itemView.findViewById(R.id.card_additional_detail)
        private val controlsBar : LinearLayout = itemView.findViewById(R.id.controls_bar)
        val infoButton : ImageView = itemView.findViewById(R.id.card_info)
        init {
            controlsBar.visibility = View.GONE
        }
    }

    interface CircleClickListener {
        fun onCircleClicked (position: Int)
        fun onCircleLongClicked (position: Int) : Boolean
        fun onCirclePhotoClicked (position: Int)
        fun onInfoClicked (position: Int)
    }
}