package com.example.friendsnetwork.fragements

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.friendsnetwork.R
import com.example.friendsnetwork.adapter.StatusAdapter
import com.example.friendsnetwork.databinding.FragmentHomePageBinding
import com.example.friendsnetwork.viewmodel.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class HomePage : Fragment() {
    private lateinit var binding:FragmentHomePageBinding
    private val viewModel:FriendsViewModel by activityViewModels()
    private val fragmentList: MutableList<Fragment> = mutableListOf()
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if(savedInstanceState==null){
            replaceFragment(FeedFragment())
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= FragmentHomePageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(savedInstanceState==null) {
            replaceFragment(FeedFragment())
        }
        val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if(!isDarkTheme){
            binding.bottomNav.setBackgroundResource(R.color.white)
            binding.bottomNav.itemTextColor= ColorStateList.valueOf(Color.BLACK)
            binding.bottomNav.itemIconTintList = ColorStateList.valueOf(Color.BLACK)
            binding.drawerNav.itemTextColor= ColorStateList.valueOf(Color.BLACK)
            binding.drawerNav.itemIconTintList = ColorStateList.valueOf(Color.BLACK)

        }
        else{
            binding.drawerNav.itemTextColor= ColorStateList.valueOf(Color.WHITE)
            binding.drawerNav.itemIconTintList = ColorStateList.valueOf(Color.WHITE)
        }

        handelDrawer(view)

        init(view)
        viewModel.mSelectedMenuId.observe(viewLifecycleOwner) {
            replaceFragment(it)
        }
        binding.bottomNav.setOnItemSelectedListener {
            when(it.itemId){
                R.id.home_bottom_nav-> {
                    viewModel.setSelectedMenuId(FeedFragment())
                }
                R.id.upload_bottom_nav->{
                    viewModel.setSelectedMenuId(UploadFragment())
                }
                R.id.setting_bottom_nav->{
                    viewModel.setSelectedMenuId(SettingFragment())
                }
                else->{
                    viewModel.setSelectedMenuId(ProfileFragment())
                }
            }


            it.isChecked = true
            true
        }


    }

    private fun handelDrawer(view: View) {
        val header = binding.drawerNav.getHeaderView(0)
        val headerName = header.findViewById<TextView>(R.id.header_name)
        val headerDp = header.findViewById<ImageView>(R.id.header_dp)
        viewModel.mUser.observe(viewLifecycleOwner){
            Glide.with(view)
                .load(it.userImage)
                .centerCrop()
                .into(headerDp)
            headerName.text = it!!.name
        }

        val drawerMenu = binding.drawerNav.menu
        val logout = drawerMenu.findItem(R.id.signout)
        logout.setOnMenuItemClickListener{
            auth.signOut()
            findNavController().navigate(R.id.action_homePage_to_loginPage)
            true
        }
    }

    fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = childFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragments, fragment)
        fragmentTransaction.commit()
        fragmentList.add(fragment)
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        if (auth.currentUser == null) {
            findNavController().navigate(R.id.action_homePage_to_loginPage)
        } else {
            // Check if the user exists in Firebase Authentication
            auth.currentUser!!.reload()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // User exists, continue with the app
                    } else {
                        // User does not exist (account deleted), navigate to login
                        auth.signOut()
                        findNavController().navigate(R.id.action_homePage_to_loginPage)
                    }
                }
        }
    }







}