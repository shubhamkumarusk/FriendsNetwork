package com.example.friendsnetwork.fragements

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.friendsnetwork.R
import com.example.friendsnetwork.USER_ID_FIRESTOREPATH
import com.example.friendsnetwork.databinding.FragmentProfileSetUpBinding
import com.example.friendsnetwork.model.UserModel
import com.example.friendsnetwork.viewmodel.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference


class ProfileSetUpFragment : Fragment() {
    private lateinit var binding:FragmentProfileSetUpBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var firebaseReference: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private  var mImageUri: Uri?=null
    val viewModel:FriendsViewModel by activityViewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentProfileSetUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        binding.continueProfile.setOnClickListener{
            AddUsers()
        }
        binding.updateDp.setOnClickListener {
            openGallery()
        }
    }

    private fun AddUsers() {
        val curruser = auth.currentUser!!
        val name = binding.userNameProfile.text.toString().trim()
        val caption = binding.Caption.text.toString()
        if(name.isEmpty()){
            binding.userNameProfile.error = "Enter Your name please!!"
        }
        if(name.isNotEmpty()) {
            storageRef = storageRef.child(curruser.uid)
            mImageUri?.let {
                storageRef.putFile(it).addOnCompleteListener {
                    if (it.isSuccessful) {
                        storageRef.downloadUrl.addOnSuccessListener { uri ->
                            val user = UserModel(curruser.email!!, name, uri.toString(), caption,true)
                            firebaseReference.collection(USER_ID_FIRESTOREPATH).document(curruser.email!!).set(user)
                                .addOnCompleteListener {
                                    if(it.isSuccessful){

                                        findNavController().navigate(R.id.action_profileSetUpFragment_to_homePage)
                                    }
                                    else{

                                    }
                                }
                        }
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Failed To create Account+${it.exception}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }


    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        firebaseReference = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("UserDP")
    }
    private fun openGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent,1)

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            mImageUri = data.data
            binding.profilePic.setImageURI(mImageUri)
        }
    }



}