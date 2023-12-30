package com.example.friendsnetwork.fragements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import com.example.friendsnetwork.R
import com.example.friendsnetwork.adapter.ProfileAdapter
import com.example.friendsnetwork.databinding.FragmentProfileBinding
import com.example.friendsnetwork.viewmodel.FriendsViewModel


class ProfileFragment : Fragment() {
    private lateinit var binding:FragmentProfileBinding
    private lateinit var mAdapter: ProfileAdapter
    private val viewModel:FriendsViewModel by activityViewModels()
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
    }

    private fun init(view:View) {

        mAdapter = ProfileAdapter()

        viewModel.mPersonalFeedList.observe(viewLifecycleOwner) {
            mAdapter.submitList(it)

        }
        binding.personlFeedRecyclerView.layoutManager = GridLayoutManager(requireContext(),3)
        binding.personlFeedRecyclerView.adapter = mAdapter


    }


}