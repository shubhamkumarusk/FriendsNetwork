package com.example.friendsnetwork.viewmodel

import android.net.Uri
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.friendsnetwork.USER_ID_FIRESTOREPATH
import com.example.friendsnetwork.model.FeedModel
import com.example.friendsnetwork.model.UserModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.getField
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class FriendsViewModel:ViewModel() {
    val mSelectedMenuId=MutableLiveData<Fragment>()
    val mFeedList = MutableLiveData<List<FeedModel>>()
    val mauth:FirebaseAuth = FirebaseAuth.getInstance()
    val mPersonalFeedList = MutableLiveData<List<FeedModel>>()
    private var mfirebaseReference = FirebaseFirestore.getInstance()
    val mUser=MutableLiveData<UserModel>()

    fun setSelectedMenuId(id:Fragment){
        mSelectedMenuId.postValue(id)
    }

    init {
        if(mauth.currentUser!=null){
            showProfile()
        }
        showFeeds()
        showPersonalFeed()

    }

    private fun showProfile() {
        viewModelScope.launch {
            val user = getUserModelFromFirestore(mauth.currentUser!!.email!!)
            if (user!=null ) mUser.postValue(user!!)
        }
    }



    private fun showFeeds(){
        mfirebaseReference.collection("All")
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }
                GlobalScope.launch{
                if (snapshot != null) {
                    val FeedList = mutableListOf<FeedModel>()
                    for (document in snapshot.documents) {
                        val userId = document.getString("userId")!!
                        val image = Uri.parse(document.getString("image")!!)
                        val caption = document.getString("caption")!!
                        val user_Id= document.getField<UserModel>("userModel")!!.id
                        val user = getUserModelFromFirestore(user_Id)

                        val feed = FeedModel(userId, image, caption, userModel = user)
                        FeedList.add(feed)

                    }
                    mFeedList.postValue(FeedList)
                }



                }
            }

    }
    fun refresh(){
        showFeeds()
    }
    private fun showPersonalFeed() {
        mfirebaseReference.collection(mauth.currentUser!!.email!!)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    // Handle error
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val FeedList = mutableListOf<FeedModel>()
                        for (document in snapshot.documents) {
                            val userId = document.getString("userId")!!
                            val image = Uri.parse(document.getString("image")!!)
                            val caption = document.getString("caption")!!

                            val feed = FeedModel(userId, image, caption)
                            FeedList.add(feed)
                        }
                        mPersonalFeedList.postValue(FeedList)
                    }
                }
    }

    private suspend fun getUserModelFromFirestore(userId: String): UserModel? {
        return try {
            mfirebaseReference.collection(USER_ID_FIRESTOREPATH).document(userId)
                .get().await().toObject(UserModel::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun updateProfile(user:UserModel){
        val data = HashMap<String, Any>()

    }




}