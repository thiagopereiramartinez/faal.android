package com.projetointegrador.faal.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseAuth
import com.projetointegrador.faal.R
import com.projetointegrador.faal.databinding.ActivityMainBinding
import com.projetointegrador.faal.databinding.HeaderNavigationViewBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        setSupportActionBar(binding.topAppBar)

        setupUI()

    }

    private fun setupUI() = with(binding) {

        val user = FirebaseAuth.getInstance().currentUser

        topAppBar.setNavigationOnClickListener {
            drawerLayout.open()
        }

        val header = navigationView.getHeaderView(0)

        user?.photoUrl?.toString()?.let { url ->
            Glide
                .with(this@MainActivity)
                .load(url)
                .centerCrop()
                .into(header.findViewById(R.id.ivProfile))
        }

        (header.findViewById(R.id.tvName) as? TextView)?.let {
            it.text = user?.displayName
        }

        (header.findViewById(R.id.tvEmail) as? TextView)?.let {
            it.text = user?.email
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        MenuInflater(this).inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) =
        when (item.itemId) {
            R.id.menuSair -> {
                AuthUI
                    .getInstance()
                    .signOut(this)
                    .addOnCompleteListener {
                        startActivity(Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        })
                    }
                true
            }
            else -> false
        }

}