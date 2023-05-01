package com.neowise.tracko.view.login

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.maps.MapsInitializer
import com.neowise.tracko.R
import com.neowise.tracko.view.GlobalOptions
import com.neowise.tracko.view.map.MapActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        MapsInitializer.initialize(applicationContext)

        if (GlobalOptions.isLocationServiceAlive) {
            startActivity(Intent(this, MapActivity::class.java))
        }

        setContentView(R.layout.activity_login)

        showSignIn()
    }

    private fun showSignIn() {
        val signInFragment = SignInFragment.getInstance()

        val transaction = supportFragmentManager.beginTransaction()
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        transaction.replace(R.id.frameLayout, signInFragment)
        transaction.commit()
    }
}