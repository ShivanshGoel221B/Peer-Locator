package com.goel.peerlocator.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.goel.peerlocator.R
import com.goel.peerlocator.models.UnknownUserModel

class BlockListAdapter (private val blockList : ArrayList<UnknownUserModel>, private val listener : BlockListClickListener,
                        private val context: Context) : RecyclerView.Adapter<BlockListAdapter.BlockViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlockViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.block_card, parent, false)
        val holder = BlockViewHolder(view)
        holder.check.setOnClickListener{listener.onChecked(holder.adapterPosition)}
        return holder
    }

    override fun onBindViewHolder(holder: BlockViewHolder, position: Int) {
        val user = blockList[position]
        val url = user.imageUrl
        val name = user.name
        Glide.with(context)
            .load(url)
            .placeholder(R.drawable.ic_placeholder_user)
            .circleCrop()
            .into(holder.blockImage)
        holder.blockName.text = name
    }

    override fun getItemCount(): Int = blockList.size

    inner class BlockViewHolder (itemView: View) : RecyclerView.ViewHolder(itemView) {
        val blockImage : ImageView = itemView.findViewById(R.id.block_profile_picture)
        val blockName : TextView = itemView.findViewById(R.id.block_name)
        val check : CheckBox = itemView.findViewById(R.id.unblock_check_box)
    }

    interface BlockListClickListener {
        fun onChecked (position: Int)
    }
}