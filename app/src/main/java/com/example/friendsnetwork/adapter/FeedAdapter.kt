package com.example.friendsnetwork.adapter

import android.graphics.Color
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.net.toUri
import androidx.lifecycle.VIEW_MODEL_STORE_OWNER_KEY
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsnetwork.R
import com.example.friendsnetwork.USER_ID_FIRESTOREPATH
import com.example.friendsnetwork.databinding.FeedListBinding
import com.example.friendsnetwork.model.FeedModel
import com.google.firebase.firestore.FirebaseFirestore

class FeedAdapter:ListAdapter<FeedModel,FeedAdapter.FeedViewHolder>(DiffCallBack) {


    class FeedViewHolder(private val binding: FeedListBinding ):RecyclerView.ViewHolder(binding.root){
        private val caption = binding.captionFeed
        private val readmore = binding.readmore

        fun bind(feed:FeedModel){
            val userModel = feed.userModel
            if (userModel != null) {
                binding.username.text = userModel.name

                Glide.with(itemView.context)
                    .load(Uri.parse(userModel.userImage))
                    .centerCrop()
                    .placeholder(R.color.black)
                    .into(binding.dpImage)

                // Other bindings...
            } else {
                // Handle the case where userModel is null
            }
            binding.likeNum.text = feed.liked_by.size.toString()
            binding.captionFeed.text = feed.caption
            binding.imageFeed.let {
                Glide.with(itemView.context)
                    .load(feed.image)
                    .centerCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.connection_error)
                    .into(binding.imageFeed)
            }
            binding.readmore.setOnClickListener{
                readMore()
            }


            Log.d("imageUri", userModel!!.userImage)
            Log.d("name",userModel.name)




        }

        private fun readMore(){
            if(caption.maxLines==2){
                caption.maxLines = Int.MAX_VALUE
                readmore.text = "Show Less"
            }
            else{
                caption.maxLines =2
                readmore.text = "...Read more"
            }
        }

        private fun numberofLines(textView: TextView):Int{
            val height = textView.height
            val lines = textView.lineHeight
            val totalLines = height/lines
            if(totalLines>0) return totalLines
            return 1

        }
    }

    companion object{
        val DiffCallBack = object:DiffUtil.ItemCallback<FeedModel>(){

            override fun areItemsTheSame(oldItem: FeedModel, newItem: FeedModel): Boolean {
                return oldItem.userId==newItem.userId
            }


            override fun areContentsTheSame(oldItem: FeedModel, newItem: FeedModel): Boolean {
                return oldItem==newItem
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        return FeedViewHolder(
            FeedListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        )
    }


    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val curr = getItem(position)
        holder.bind(curr)
    }

}