package com.example.friendsnetwork.fragements

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.friendsnetwork.R
import com.example.friendsnetwork.USER_ID_FIRESTOREPATH
import com.example.friendsnetwork.adapter.FeedAdapter
import com.example.friendsnetwork.adapter.onClickHandel
import com.example.friendsnetwork.databinding.FragmentFeedBinding
import com.example.friendsnetwork.model.FeedModel
import com.example.friendsnetwork.model.UserModel
import com.example.friendsnetwork.viewmodel.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.withContext


class FeedFragment : Fragment(), onClickHandel {

    private lateinit var binding:FragmentFeedBinding
    private lateinit var mAdapter:FeedAdapter
    private  var mFeedList = mutableListOf<FeedModel>()
    private lateinit var firebaseReference: FirebaseFirestore
    private lateinit var auth : FirebaseAuth
    private lateinit var listener:FirebaseAuth.AuthStateListener
    private val viewModel:FriendsViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)

    }
    private fun init(view: View) {
        firebaseReference = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        mAdapter = FeedAdapter(this)
        binding.recyclerViewFeed.adapter = mAdapter
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())
        viewModel.mFeedList.observe(viewLifecycleOwner) {
            mAdapter.submitList(it)
        }

        if(auth.currentUser==null){
            findNavController().navigate(R.id.action_feedFragment_to_loginPage)
        }

    }

    override fun onLikeButtonClick(feed: FeedModel) {
        GlobalScope.launch{
            val currentFeed = getFeedModelFromFirestore(feed.feedId)
            Log.d("shubhamKumarFeed", currentFeed.toString())
            Log.d("feedIdLog", feed.feedId.toString())
            val currentUser = viewModel.mUser.value!!

            Log.d("shubhamKumarCurrentUser", currentUser.id.toString())
            currentFeed?.let { nonNullFeed ->
                val isLiked = nonNullFeed.liked_by.contains(currentUser.id)
                if (isLiked) {
                    nonNullFeed.liked_by.remove(currentUser.id)
                } else {
                    nonNullFeed.liked_by.add(currentUser.id)
                }
//                viewModel.updateFeedList(nonNullFeed)
                Log.d("size",nonNullFeed.liked_by.size.toString())
                uploadFeedToFirestore(nonNullFeed, "All")
            }
        }
    }


    private suspend fun getFeedModelFromFirestore(feedId: String): FeedModel? {
        return try {
            withContext(Dispatchers.IO) {
                firebaseReference.collection("All").document(feedId)
                    .get().await().toObject(FeedModel::class.java)
            }
        } catch (e: Exception) {
            Log.e("shubhamKumarFeed", "Error retrieving feed: $e")
            null
        }
    }

    private suspend fun uploadFeedToFirestore(feed: FeedModel, collectionPath: String) {
        try {
            withContext(Dispatchers.IO){
                firebaseReference.collection(collectionPath).document(feed.feedId).set(feed).await()
            }

        } catch (e: Exception) {
            Toast.makeText(requireActivity(), "Error uploading feed: $e", Toast.LENGTH_LONG).show()
        }
    }


}