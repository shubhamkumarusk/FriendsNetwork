package com.example.friendsnetwork.fragements

import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.example.friendsnetwork.R
import com.example.friendsnetwork.USER_ID_FIRESTOREPATH
import com.example.friendsnetwork.adapter.ProfileAdapter
import com.example.friendsnetwork.buildDialog
import com.example.friendsnetwork.databinding.FragmentProfileBinding
import com.example.friendsnetwork.model.UserModel
import com.example.friendsnetwork.viewmodel.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class ProfileFragment : Fragment() {
    private lateinit var binding:FragmentProfileBinding
    private lateinit var mAdapter: ProfileAdapter
    private val viewModel:FriendsViewModel by activityViewModels()
    private lateinit var auth:FirebaseAuth
    private lateinit var firebaseReference:FirebaseFirestore
    private  var user:UserModel?=null
    private var isUserDataLoaded = false
    private var postNumber:Int=0



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        binding.profileEditButton.setOnClickListener {
            findNavController().navigate(R.id.action_homePage_to_profileSetUpFragment)
        }
        viewModel.mUser.observe(viewLifecycleOwner){
            user = it
            Glide.with(view)
                .load(user!!.userImage)
                .placeholder(R.drawable.profile)
                .into(binding.profileDp)
            binding.userName.text = user!!.name
            binding.caption.text = user!!.caption
            binding.postNumber.text = postNumber.toString()
        }
    }

//    private fun profileSetup(view: View) {
//        lifecycleScope.launch {
//            withContext(Dispatchers.Main) {
//
//            }
//        }
//
//    }

    private fun init(view:View) {
        auth = FirebaseAuth.getInstance()
        firebaseReference = FirebaseFirestore.getInstance()
        mAdapter = ProfileAdapter()
        viewModel.mPersonalFeedList.observe(viewLifecycleOwner) {
            mAdapter.submitList(it)
            postNumber = it.size
        }

        binding.personlFeedRecyclerView.layoutManager = GridLayoutManager(requireContext(),3)
        binding.personlFeedRecyclerView.adapter = mAdapter





    }


}