package com.example.friendsnetwork.adapter

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.friendsnetwork.R
import com.example.friendsnetwork.databinding.FeedListBinding
import com.example.friendsnetwork.model.FeedModel
import com.example.friendsnetwork.viewmodel.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth

class FeedAdapter(private val mlistner:onClickHandel) :ListAdapter<FeedModel,FeedAdapter.FeedViewHolder>(DiffCallBack) {
    class FeedViewHolder(private val binding: FeedListBinding,val listner: onClickHandel):RecyclerView.ViewHolder(binding.root){
        private val caption = binding.captionFeed
        private val likedNumer = binding.likeNum
        fun bind(feed:FeedModel){
            val userModel = feed.userModel
            if (userModel != null) {
                binding.username.text = userModel.name
                Glide.with(itemView.context)
                    .load(Uri.parse(userModel.userImage))
                    .centerCrop()
                    .placeholder(R.drawable.profile)
                    .into(binding.dpImage)


            }
            likedNumer.text = feed.liked_by.size.toString()
            binding.captionFeed.text = feed.caption
            binding.imageFeed.let {
                Glide.with(itemView.context)
                    .load(feed.image)
                    .centerCrop()
                    .placeholder(R.drawable.loading_animation)
                    .error(R.drawable.connection_error)
                    .into(binding.imageFeed)
            }
            caption.setOnClickListener{
                readMore()
            }
            binding.likeBtn.setOnClickListener {
               listner.onLikeButtonClick(feed)
            }
            val liked = feed.liked_by.contains(FirebaseAuth.getInstance().currentUser!!.email)
            if(liked){
                binding.likeBtn.setImageDrawable(ContextCompat.getDrawable(binding.likeBtn.context,R.drawable.like_btn))
            }else{
                binding.likeBtn.setImageDrawable(ContextCompat.getDrawable(binding.likeBtn.context,R.drawable.liked_btn_outlined))
            }


        }


        private fun readMore(){
            if(caption.maxLines==2){
                caption.maxLines = Int.MAX_VALUE
            }
            else{
                caption.maxLines =2
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
                return oldItem.feedId==newItem.feedId
            }


            override fun areContentsTheSame(oldItem: FeedModel, newItem: FeedModel): Boolean {
                return oldItem==newItem
            }

        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        return FeedViewHolder(
            FeedListBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            mlistner
        )
    }


    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val curr = getItem(position)
        holder.bind(curr)
    }



}

interface onClickHandel{
    fun onLikeButtonClick(feed:FeedModel)
}