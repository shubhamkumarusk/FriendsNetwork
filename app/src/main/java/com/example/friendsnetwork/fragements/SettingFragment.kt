package com.example.friendsnetwork.fragements

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.friendsnetwork.R
import com.example.friendsnetwork.databinding.FragmentSettingBinding


class SettingFragment : Fragment() {
    private lateinit var binding:FragmentSettingBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding =  FragmentSettingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.tofeedBtn.setOnClickListener {
            navigateToFeedFragment()
        }
    }

    private fun navigateToFeedFragment() {
        // Find the parent fragment (HomePage)
        val homePageFragment = parentFragment as HomePage
        // Replace FeedFragment in HomePage
        homePageFragment?.replaceFragment(FeedFragment())
    }


}