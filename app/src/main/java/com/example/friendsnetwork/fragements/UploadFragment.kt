package com.example.friendsnetwork.fragements

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.friendsnetwork.R
import com.example.friendsnetwork.USER_ID_FIRESTOREPATH
import com.example.friendsnetwork.databinding.FragmentUploadBinding
import com.example.friendsnetwork.model.FeedModel
import com.example.friendsnetwork.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class UploadFragment : Fragment() {

    private lateinit var binding: FragmentUploadBinding
    private lateinit var storageRef:StorageReference
    private lateinit var firebaseReference: FirebaseFirestore
    private lateinit var auth :FirebaseAuth
    private var mImageUri:Uri?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentUploadBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        init(view)
        registerActivity()

    }

    private fun registerActivity() {
        binding.image.setOnClickListener {
            openGallery()
        }
        binding.uploadBtn.setOnClickListener {
            UploadFeed()
        }
    }

    private fun UploadFeed() {
        val currentUser = auth.currentUser!!
        val caption = binding.captionEt.text.toString()
        storageRef = storageRef.child(System.currentTimeMillis().toString())

        mImageUri?.let { imageUri ->
            storageRef.putFile(imageUri).addOnCompleteListener { uploadTask ->
                if (uploadTask.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        GlobalScope.launch(Dispatchers.Main) { // Switch to the main thread
                            try {
                                val user = getUserModelFromFirestore(currentUser.email!!)
                                user?.let { userModel ->
                                    val feed = FeedModel(currentUser.uid, uri, caption, userModel = userModel)
                                    uploadFeedToFirestore(currentUser.email!!, feed, "All")
                                    uploadFeedToFirestore(currentUser.email!!, feed, currentUser.email!!)
                                    Toast.makeText(requireContext(), "Uploaded", Toast.LENGTH_LONG).show()
                                } ?: run {
                                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_LONG).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(requireContext(), "Error: $e", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "${uploadTask.exception}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } ?: run {
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(requireContext(), "Image not selected", Toast.LENGTH_LONG).show()
            }
        }
    }


    private suspend fun getUserModelFromFirestore(userId: String): UserModel? {
        return try {
            firebaseReference.collection(USER_ID_FIRESTOREPATH).document(userId)
                .get().await().toObject(UserModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private suspend fun uploadFeedToFirestore(userId: String, feed: FeedModel, collectionPath: String) {
        try {
            firebaseReference.collection(collectionPath).add(feed).await()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error uploading feed: $e", Toast.LENGTH_LONG).show()
        }
    }


    private fun init(view: View) {
        storageRef = FirebaseStorage.getInstance().reference.child("FeedImages")
        firebaseReference = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

    }
    private fun openGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK,Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent,1)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {
            mImageUri = data.data
            binding.image.setImageURI(mImageUri)
        }
    }


}