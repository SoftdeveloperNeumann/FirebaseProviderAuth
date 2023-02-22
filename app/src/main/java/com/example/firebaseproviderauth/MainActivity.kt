package com.example.firebaseproviderauth

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import com.example.firebaseproviderauth.databinding.ActivityMainBinding
import com.facebook.FacebookSdk
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private var user: FirebaseUser? = null

    private val authProviderList: MutableList<AuthUI.IdpConfig>
        get() {
            val providers = ArrayList<AuthUI.IdpConfig>().apply {
                add(
                    AuthUI.IdpConfig.EmailBuilder().setActionCodeSettings(
                        ActionCodeSettings.newBuilder()
                            .setUrl("/__/auth/action?mode=<action>&oobCode=<code>")
                            .build()
                    ).build()
                )
                add(AuthUI.IdpConfig.PhoneBuilder().build())
                add(AuthUI.IdpConfig.GoogleBuilder().build())
//                add(AuthUI.IdpConfig.FacebookBuilder().build())
            }
            return providers
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()
        user = auth.currentUser

        activityResultLauncher = registerForActivityResult(FirebaseAuthUIActivityResultContract()){result ->
            this.onSignInResult(result)
        }

        if((user != null && user!!.isEmailVerified) || (user != null && user!!.phoneNumber != null)){
            Toast.makeText(this, "angemeldet und bestätigt", Toast.LENGTH_SHORT).show()
        }else{
            authenticate()
        }
    }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult?) {
        val response = result!!.idpResponse
        if(result.resultCode == RESULT_OK){
            val user = auth.currentUser
            if(user!!.isEmailVerified || user!!.phoneNumber != null){
                Toast.makeText(this, "angemeldet und bestätigt", Toast.LENGTH_SHORT).show()
            }else{
                user.sendEmailVerification().addOnCompleteListener { task ->
                    if(task.isSuccessful){
                        Toast.makeText(this, "Mail wurde versendet", Toast.LENGTH_SHORT).show()
                        authenticate()
                    }
                }
            }
        }
    }

    private fun authenticate() {
        activityResultLauncher.launch(
            AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(authProviderList)
                .setLogo(R.drawable.ic_launcher_background)
                .setTheme(com.firebase.ui.auth.R.style.FirebaseUI_DefaultMaterialTheme)
                .setIsSmartLockEnabled(false)
                .build()
        )
    }
}