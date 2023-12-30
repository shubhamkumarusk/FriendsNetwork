package com.example.friendsnetwork.fragements

import android.app.Activity
import android.content.res.Configuration
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.friendsnetwork.R
import com.example.friendsnetwork.databinding.FragmentSignInPageBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit


class SignInPage : Fragment() {

    private lateinit var binding:FragmentSignInPageBinding
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentSignInPageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.notRegistered.setOnClickListener {
            findNavController().navigate(R.id.action_signInPage_to_loginPage)
        }
        binding.signInBackground.setOnClickListener {
            binding.emailEt.clearFocus()
            binding.passwordEt.clearFocus()
        }
        val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        if(isDarkTheme){
            binding.emailEt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.baseline_email_24_night,0,0,0)
            binding.passwordEt.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icons8_password_50_night,0,0,0)
            binding.googleSignInButton.setImageResource(R.drawable.googlelogo_night)

        }
        init(view)
        verifyEmailPass(view)

    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        if(auth.currentUser!=null){
            findNavController().navigate(R.id.action_signInPage_to_homePage)
        }
    }

    private fun verifyEmailPass(view: View) {
        binding.continueSignin.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passwordEt.text.toString().trim()
            if (email.isEmpty()) {
                binding.emailEt.error = "Enter Your e-mail"
            }
            if (pass.isEmpty()) {
                binding.passwordEt.error = "Enter your password"
            }
            if (email.isNotEmpty() && pass.isNotEmpty()) {
                auth.signInWithEmailAndPassword(email, pass)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // Sign in success, update UI with the signed-in user's information
                            findNavController().navigate(R.id.action_signInPage_to_homePage)

                        } else {
                            Toast.makeText(requireContext(), "${task.exception}", Toast.LENGTH_LONG)
                                .show()
                        }
                    }
            }
        }
    }
}