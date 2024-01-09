package com.example.friendsnetwork.fragements

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import com.example.friendsnetwork.R
import com.example.friendsnetwork.USER_ID_FIRESTOREPATH
import com.example.friendsnetwork.buildDialog
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
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class UploadFragment : Fragment() {

    private lateinit var binding: FragmentUploadBinding
    private lateinit var storageRef:StorageReference
    private lateinit var firebaseReference: FirebaseFirestore
    private lateinit var auth :FirebaseAuth
    private var mImageUri:Uri?=null
    private lateinit var mCameraUri: Uri
    private lateinit var dialog:ProgressDialog
    private  var currentPhotoPath: String?=null
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            Log.d("shubhamkumar", mImageUri.toString())
            binding.image.setImageURI(mImageUri)
        } else {
            // Handle failure
            Toast.makeText(requireActivity(), "Failed to capture image", Toast.LENGTH_LONG).show()
        }
    }
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
        binding.gallery.setOnClickListener {
            openGallery()
        }
        binding.camera.setOnClickListener{
            openCamera()
        }
        binding.uploadBtn.setOnClickListener {
            UploadFeed()
        }
    }

    private fun UploadFeed() {
        val currentUser = auth.currentUser!!
        val caption = binding.captionEt.text.toString()
        storageRef = storageRef.child(System.currentTimeMillis().toString())
        val progressBar = binding.progressBar
        progressBar.visibility = View.VISIBLE
        if (mImageUri != null) {
            val uploadTask = storageRef.putFile(mImageUri!!)
            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount).toInt()
                progressBar.progress = progress
            }
            uploadTask.addOnCompleteListener { uploadTask ->
                progressBar.visibility = View.GONE
                if (uploadTask.isSuccessful) {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        GlobalScope.launch(Dispatchers.Main) { // Switch to the main thread
                            try {
                                val user = getUserModelFromFirestore(currentUser.email!!)
                                user?.let { userModel ->
                                    val feed = FeedModel(currentUser.uid, uri, caption, userModel = userModel)
                                    uploadFeedToFirestore(currentUser.email!!, feed, "All")
                                    uploadFeedToFirestore(currentUser.email!!, feed, currentUser.email!!)
                                    navigateToFeedFragment()
                                } ?: run {
                                    null
                                }
                            } catch (e: Exception) {
                                Toast.makeText(requireActivity(), "Error: $e", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    GlobalScope.launch(Dispatchers.Main) {
                        Toast.makeText(requireActivity(), "${uploadTask.exception}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                Toast.makeText(requireActivity(), "Image not selected", Toast.LENGTH_LONG).show()
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
            Toast.makeText(requireActivity(), "Error uploading feed: $e", Toast.LENGTH_LONG).show()
        }
    }


    private fun init(view: View) {
        storageRef = FirebaseStorage.getInstance().reference.child("FeedImages")
        firebaseReference = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        dialog = buildDialog(requireContext(),"Uploading")

    }
    private fun openGallery(){
        val galleryIntent = Intent(Intent.ACTION_PICK,Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(galleryIntent,1)
    }
    private fun openCamera() {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val imageFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)

        mImageUri = Uri.fromFile(imageFile)
        val photoURI: Uri = FileProvider.getUriForFile(
            requireContext(),
            "com.example.friendsnetwork.fileprovider",
            imageFile
        )
        takePicture.launch(photoURI)
    }

    private fun createImageFile(): File? {
        return try {
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir: File? = requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES)

            // Ensure the storage directory exists
            storageDir?.mkdirs()

            val imageFile = File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )

            currentPhotoPath = imageFile.absolutePath
            Log.d("CurrentPath",currentPhotoPath.toString())
            imageFile
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == Activity.RESULT_OK && data != null) {

            mImageUri = data.data
            Log.d("shubhamkumar",mImageUri.toString())
            binding.image.setImageURI(mImageUri)
        }
        else if (requestCode == 2 && resultCode == Activity.RESULT_OK && data!=null) {
            // Use the image URI directly
            mImageUri = FileProvider.getUriForFile(
                requireContext(),
                "com.example.friends-network.provider",
                File(currentPhotoPath!!)
            )
            Log.d("shubhamkumar",mImageUri.toString())
            binding.image.setImageURI(mImageUri)
        }

    }
    private fun navigateToFeedFragment() {
        // Find the parent fragment (HomePage)
        val homePageFragment = parentFragment as HomePage
        // Replace FeedFragment in HomePage
        homePageFragment?.replaceFragment(FeedFragment())
    }




}