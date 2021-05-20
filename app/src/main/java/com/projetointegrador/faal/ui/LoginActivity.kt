package com.projetointegrador.faal.ui

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.projetointegrador.faal.R

class LoginActivity : AppCompatActivity() {

    private val providers = arrayListOf(
        AuthUI.IdpConfig.EmailBuilder().build(),
        AuthUI.IdpConfig.GoogleBuilder().build(),
        AuthUI.IdpConfig.FacebookBuilder().build(),
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startLoginAuth()
        } else {
            saveProfileInfo {
                startMainActivity()
            }
        }

    }

    private fun startLoginAuth() {
        val intent =
            AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setLogo(R.drawable.faal)
                .setLockOrientation(true)
                .setAvailableProviders(providers)
                .setTheme(R.style.Theme_FAAL_NoActionBar)
                .build()

        startActivityForResult(intent, RC_SIGN_IN)
    }

    private fun saveProfileInfo(onFinish: () -> Unit) {
        val currentUser = FirebaseAuth.getInstance().currentUser ?: return

        val db = Firebase.firestore
        db.collection("users").document(currentUser.uid)
            .set(hashMapOf(
                "name" to currentUser.displayName,
                "email" to currentUser.email,
                "photoUrl" to currentUser.photoUrl?.toString()
            ), SetOptions.merge())
            .addOnSuccessListener {
                onFinish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, e.toString(), Toast.LENGTH_SHORT).show()
            }
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_SIGN_IN) {
            if (resultCode == Activity.RESULT_OK) {
                saveProfileInfo {
                    startMainActivity()
                }
            } else {
                finish()
            }
        }
    }

    companion object {
        private const val RC_SIGN_IN = 200
    }
}