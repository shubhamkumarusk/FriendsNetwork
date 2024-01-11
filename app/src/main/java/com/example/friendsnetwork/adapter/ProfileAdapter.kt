package com.example.friendsnetwork.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsnetwork.R
import com.example.friendsnetwork.databinding.FeedListBinding
import com.example.friendsnetwork.databinding.PersonalFeedListBinding
import com.example.friendsnetwork.model.FeedModel

class ProfileAdapter: ListAdapter<FeedModel, ProfileAdapter.ProfileViewHolder>(DiffCallBack) {


    class ProfileViewHolder(private val binding: PersonalFeedListBinding): RecyclerView.ViewHolder(binding.root){


        fun bind(feed:FeedModel){
            binding.feedImage.let {
                Glide.with(itemView.context)
                    .load(feed.image)
                    .centerCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.connection_error)
                    .into(binding.feedImage)
            }

        }




    }

    companion object{
        val DiffCallBack = object: DiffUtil.ItemCallback<FeedModel>(){

            override fun areItemsTheSame(oldItem: FeedModel, newItem: FeedModel): Boolean {
                return oldItem.feedId==newItem.feedId
            }

            override fun areContentsTheSame(oldItem: FeedModel, newItem: FeedModel): Boolean {
                return oldItem==newItem
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProfileViewHolder {
        return ProfileViewHolder(
            PersonalFeedListBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        )
    }


    override fun onBindViewHolder(holder: ProfileViewHolder, position: Int) {
        val curr = getItem(position)
        holder.bind(curr)
    }


}