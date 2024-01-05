package com.example.friendsnetwork.fragements

import android.app.ProgressDialog
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.friendsnetwork.R
import com.example.friendsnetwork.USER_ID_FIRESTOREPATH
import com.example.friendsnetwork.buildDialog
import com.example.friendsnetwork.databinding.FragmentLoginPageBinding
import com.example.friendsnetwork.model.UserModel
import com.example.friendsnetwork.viewmodel.FriendsViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit


class LoginPage : Fragment() {

    private lateinit var binding:FragmentLoginPageBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var googleSignInClient: GoogleSignInClient
    private lateinit var firebaseReference: FirebaseFirestore
    private val RC_SIGN_IN = 9001
    private val viewModel:FriendsViewModel by activityViewModels()
    private lateinit var dialog: ProgressDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentLoginPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        init(view)
        binding.alreadySignin.setOnClickListener {
            findNavController().navigate(R.id.action_loginPage_to_signInPage)
        }
        binding.signupBackground.setOnClickListener {
            binding.emailIdEt.clearFocus()
            binding.confirmPasswordEt.clearFocus()
            binding.setPasswordEt.clearFocus()
        }
        binding.googleSignInButton.setOnClickListener {
            signInWithGoogle()
        }
        verifyEmailPass(view)



    }

    private fun verifyEmailPass(view: View) {
        binding.verificationBtn.setOnClickListener {
            val email = binding.emailIdEt.text.toString().trim()
            val pass = binding.setPasswordEt.text.toString().trim()
            val confirmPass = binding.confirmPasswordEt.text.toString().trim()
            dialog.show()
            if (email.isEmpty()) {
                binding.emailIdEt.error = "Enter Your e-mail"
                return@setOnClickListener
            }
            if (pass.isEmpty()) {
                binding.setPasswordEt.error = "Enter Your Password"
                return@setOnClickListener
            }
            if (confirmPass.isEmpty()) {
                binding.confirmPasswordEt.error = "Confirm Your Password"
                return@setOnClickListener
            }
            if (confirmPass != pass) {
                binding.confirmPasswordEt.error = "Enter the same password to confirm"
                return@setOnClickListener
            }

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty() && pass == confirmPass) {
                val userDocumentRef = firebaseReference.collection(USER_ID_FIRESTOREPATH).document(email)

                userDocumentRef.get().addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val doc = task.result

                        if (doc != null && doc.exists()) {
                            dialog.dismiss()
                            showToast("User already exists")

                        } else {
                            // User doesn't exist, navigate to OTPFragment
                            dialog.dismiss()
                            val action = LoginPageDirections.actionLoginPageToOTPFragment(email, pass)
                            findNavController().navigate(action)
                        }
                    } else {
                        dialog.dismiss()
                        showToast("Error: ${task.exception?.message}")
                    }
                }
            }

        }
    }


    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        firebaseReference = FirebaseFirestore.getInstance()
        binding.progressBar.visibility = View.GONE
        binding.viewBackground.visibility = View.GONE
        if(auth.currentUser!=null) {
            lifecycleScope.launch {
                binding.progressBar.visibility = View.VISIBLE
                binding.viewBackground.visibility = View.VISIBLE
                binding.verificationBtn.visibility = View.GONE
                val user = withContext(Dispatchers.IO) {
                    getUserModelFromFirestore(auth.currentUser!!.email!!)
                }
                if (user == null) {
                    findNavController().navigate(R.id.action_loginPage_to_profileSetUpFragment)
                } else if (!user.profileSetup) {
                    findNavController().navigate(R.id.action_loginPage_to_profileSetUpFragment)
                } else {
                    findNavController().navigate(R.id.action_loginPage_to_homePage)
                }
                binding.progressBar.visibility = View.GONE
                binding.viewBackground.visibility = View.GONE
                binding.verificationBtn.visibility = View.VISIBLE
            }
        }
        //Changing Color of icons according to theme
        val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (isDarkTheme) {
            // Set the alternative drawable for dark theme
            binding.googleSignInButton.setImageResource(R.drawable.googlelogo_night)
            binding.emailIdEt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_email_24_night,0,0,0)
            binding.setPasswordEt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icons8_password_50_night,0,0,0)
            binding.confirmPasswordEt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icons8_password_50_night,0,0,0)

        }
        dialog = buildDialog(requireContext(),"Sending OTP")
        initGoogleSignIn(view)




    }
    private suspend fun getUserModelFromFirestore(userId: String): UserModel? {
        return try {
            firebaseReference.collection(USER_ID_FIRESTOREPATH).document(userId)
                .get().await().toObject(UserModel::class.java)
        } catch (e: Exception) {
            null
        }
    }
    private fun initGoogleSignIn(view: View) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireContext(), gso)

    }

    private fun signInWithGoogle() {
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
        try {
            val account = completedTask.getResult(ApiException::class.java)
            firebaseAuthWithGoogle(account!!)
        } catch (e: ApiException) {
            // Print the error message
            Log.e("GoogleSignIn", "Google Sign-In failed with error: ${e.message}")
            Toast.makeText(requireContext(), "Google Sign-In failed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun firebaseAuthWithGoogle(account: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(account.idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    //Checking if user already have account!!
                    val newUser = task.result!!.additionalUserInfo!!.isNewUser
                    if(newUser){
                        findNavController().navigate(R.id.action_loginPage_to_profileSetUpFragment)
                    }
                    else{
                        lifecycleScope.launch {
                            val user = withContext(Dispatchers.IO) {
                                getUserModelFromFirestore(auth.currentUser!!.email!!)
                            }

                            if (user == null) {
                                findNavController().navigate(R.id.action_loginPage_to_profileSetUpFragment)
                            } else if (!user.profileSetup) {
                                findNavController().navigate(R.id.action_loginPage_to_profileSetUpFragment)
                            } else {
                                findNavController().navigate(R.id.action_loginPage_to_homePage)
                            }
                        }
                    }



                } else {
                    // If sign-in fails, display a message to the user.
                    Toast.makeText(
                        requireContext(),
                        "Google Sign-In failed: ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun showToast(message:String){
        Toast.makeText(requireContext(),message,Toast.LENGTH_LONG).show()
    }






}