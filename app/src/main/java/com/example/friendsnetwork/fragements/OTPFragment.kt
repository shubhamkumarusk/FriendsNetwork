package com.example.friendsnetwork.fragements

import android.content.res.Configuration
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.friendsnetwork.R
import com.example.friendsnetwork.databinding.FragmentOTPBinding
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import papaya.`in`.sendmail.SendMail
import kotlin.random.Random
import kotlin.random.nextInt

class OTPFragment : Fragment() {
    private lateinit var binding:FragmentOTPBinding
    private lateinit var email:String
    private lateinit var password:String
    private lateinit var auth: FirebaseAuth
    private  var randomPass=0
    private val args:OTPFragmentArgs by navArgs()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding= FragmentOTPBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        init(view)
        random()
        Log.d("email",email.toString())
        binding.verificationBtn.setOnClickListener {
            val OTP = binding.otp.text.toString()
            if(OTP==randomPass.toString()){
                register(view)
            }
            else{
                binding.otp.error = "Wrong OTP"

            }
        }



    }

    fun random(){
        randomPass = Random.nextInt(100000..999999)
        val email = SendMail("friendsnetworksocialapp@gmail.com","nsfcmjtxkgujcmoc",
            email,
        "Friends Network's OTP Verification",
        "Dear User,\n" +
                "\n" +
                "Thank you for choosing Friends Network! To complete your account setup, please use the following one-time passcode (OTP):\n" +
                "\n" +
                "Your OTP:${randomPass} \n" +
                "\n" +
                "Please enter this code on the verification page to confirm your email address. Note that this code is valid for a single use only and will expire shortly.\n" +
                "\n" +
                "If you didn't request this code, or if you have any concerns about your account security, please contact our support team immediately at friendsnetworksocialapp@gmail.com.\n" +
                "\n" +
                "Welcome aboard, and thank you for trusting Friends Network!\n" +
                "\n" +
                "Best regards,\n" +
                "The Friends Network Team\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n" +
                "\n")
        email.execute()
    }

    private fun init(view: View) {
        auth = FirebaseAuth.getInstance()
        if(auth.currentUser!=null){
            findNavController().navigate(R.id.action_loginPage_to_homePage)
        }
        val isDarkTheme = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES

        if (isDarkTheme) {
            // Set the alternative drawable for dark theme
            binding.imgOtp.setImageResource(R.drawable.password_svgrepo_com_night)


        }
        email = args.email
        password = args.password

    }

    private fun register(view:View){
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { it ->
                if(it.isSuccessful){
                    findNavController().navigate(R.id.action_OTPFragment_to_profileSetUpFragment)
                }
                else{
                    Snackbar.make(view, "${it.exception}", Snackbar.LENGTH_SHORT).show()
                }
            }

    }


}