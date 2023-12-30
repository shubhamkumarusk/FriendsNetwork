package com.example.friendsnetwork.fragements

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.friendsnetwork.R
import com.example.friendsnetwork.adapter.FeedAdapter
import com.example.friendsnetwork.databinding.FragmentFeedBinding
import com.example.friendsnetwork.model.FeedModel
import com.example.friendsnetwork.viewmodel.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.launch


class FeedFragment : Fragment() {

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
        mAdapter = FeedAdapter()
        binding.recyclerViewFeed.adapter = mAdapter
        binding.recyclerViewFeed.layoutManager = LinearLayoutManager(requireContext())
        viewModel.mFeedList.observe(viewLifecycleOwner, Observer {
            mAdapter.submitList(it)
        })

        if(auth.currentUser==null){
            findNavController().navigate(R.id.action_feedFragment_to_loginPage)
        }



    }




}