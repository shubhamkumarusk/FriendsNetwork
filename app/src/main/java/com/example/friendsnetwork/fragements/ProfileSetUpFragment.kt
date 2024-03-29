package com.example.friendsnetwork.fragements

import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Camera
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.registerForActivityResult
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.friendsnetwork.R
import com.example.friendsnetwork.USER_ID_FIRESTOREPATH
import com.example.friendsnetwork.buildDialog
import com.example.friendsnetwork.databinding.FragmentProfileSetUpBinding
import com.example.friendsnetwork.model.UserModel
import com.example.friendsnetwork.viewmodel.FriendsViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ProfileSetUpFragment : Fragment() {
    private lateinit var binding:FragmentProfileSetUpBinding
    private lateinit var auth:FirebaseAuth
    private lateinit var firebaseReference: FirebaseFirestore
    private lateinit var storageRef: StorageReference
    private  var mImageUri: Uri?=null
    private var mUser:UserModel?=null
    val viewModel:FriendsViewModel by activityViewModels()
    private lateinit var dialog: ProgressDialog
    private  var isProfilePicChanged:Boolean = false
    var message = "Creating Profile"
    private var takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()){success->
        if(success){
            Glide.with(requireContext())
                .load(mImageUri)
                .placeholder(R.drawable.profile)
                .into(binding.profilePic)
        }
        else{

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
        binding= FragmentProfileSetUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)

            binding.continueProfile.setOnClickListener {
                AddUsers()
            }
        binding.updateDp.setOnClickListener {
            val options = arrayOf<CharSequence>("Camera","Gallery","Remove Profile Pic")
            val builder = AlertDialog.Builder(requireContext())
            builder.setTitle("Choose")
            builder.setItems(options){_,item->
                when{
                    options[item]=="Camera"->{
                        openCamera()
                    }
                    options[item]=="Gallery"->{
                        openGallery()
                    }
                    options[item]=="Remove Profile Pic"->{
                        mImageUri = null
                        binding.profilePic.setBackgroundResource(R.drawable.profile)
                    }
                }

            }
            builder.show()

        }
    }



    private fun AddUsers() {
        val curruser = auth.currentUser!!
        val name = binding.userNameProfile.text.toString().trim()
        val caption = binding.Caption.text.toString()
        if(name.isEmpty()){
            binding.userNameProfile.error = "Enter Your name please!!"
        }
        dialog.show()
        if(name.isNotEmpty()) {
            storageRef = storageRef.child(curruser.uid)
               if(mImageUri!=null && isProfilePicChanged) {
                   mImageUri?.let {
                       storageRef.putFile(it).addOnCompleteListener {
                           if (it.isSuccessful) {
                               storageRef.downloadUrl.addOnSuccessListener { uri ->
                                   val user = UserModel(
                                       curruser.email!!,
                                       name,
                                       uri.toString(),
                                       caption,
                                       true
                                   )
                                   viewModel.mUser.postValue(user)
                                   firebaseReference.collection(USER_ID_FIRESTOREPATH)
                                       .document(curruser.email!!).set(user)
                                       .addOnCompleteListener {
                                           if (it.isSuccessful) {
                                               dialog.dismiss()
                                               findNavController().navigate(R.id.action_profileSetUpFragment_to_homePage)
                                           } else {
                                               dialog.dismiss()

                                           }
                                       }
                               }
                           } else {
                               dialog.dismiss()
                               Toast.makeText(
                                   requireContext(),
                                   "Failed To create Account+${it.exception}",
                                   Toast.LENGTH_LONG
                               ).show()
                           }
                       }
                   }
               }
            else{
                val user = UserModel(curruser.email!!,name,mImageUri.toString(),caption,true)
                   viewModel.mUser.postValue(user)
                   firebaseReference.collection(USER_ID_FIRESTOREPATH)
                       .document(curruser.email!!).set(user)
                       .addOnCompleteListener {
                           if (it.isSuccessful) {
                               dialog.dismiss()
                               findNavController().navigate(R.id.action_profileSetUpFragment_to_homePage)
                           } else {
                               dialog.dismiss()
                           }
                       }
               }

            }

    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        firebaseReference = FirebaseFirestore.getInstance()
        storageRef = FirebaseStorage.getInstance().reference.child("UserDP")

        viewModel.mUser.observe(viewLifecycleOwner){
            if(it!=null){
                updateProfile(it,view)
                message = "Updating Profile"
            }
        }
        dialog = buildDialog(requireContext(), message)
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
            isProfilePicChanged = true
        }
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
    private fun updateProfile(user: UserModel, view: View){
        Glide.with(view)
            .load(user.userImage)
            .placeholder(R.drawable.profile)
            .into(binding.profilePic)

        mImageUri = Uri.parse(user.userImage)
        Log.d("ImageUri",mImageUri.toString())
        binding.userNameProfile.setText(user.name)
        binding.Caption.setText(user.caption)
    }




}