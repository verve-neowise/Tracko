package com.neowise.tracko.view.login

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.neowise.tracko.R
import com.neowise.tracko.data.Preferences
import com.neowise.tracko.data.RemoteApi
import com.neowise.tracko.data.model.LoginParams
import com.neowise.tracko.view.map.MapActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SignInFragment() : Fragment() {

    companion object {
        fun getInstance() : SignInFragment {
            return SignInFragment()
        }
    }

    private lateinit var emailTextEdit: EditText
    private lateinit var passwordTextEdit: EditText
    private lateinit var signInButton: Button
    private lateinit var gotoSignUpButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_in, container, false)

        emailTextEdit = view.findViewById(R.id.email_edit)
        passwordTextEdit = view.findViewById(R.id.password_edit)
        signInButton = view.findViewById(R.id.sign_in_btn)
        gotoSignUpButton = view.findViewById(R.id.goto_sign_up_btn)

        signInButton.setOnClickListener {
            signIn()
        }

        gotoSignUpButton.setOnClickListener {
            showSignUp()
        }

        // Fill login fields with saved data
        val login = Preferences.getInstance(requireContext()).getLogin()

        emailTextEdit.setText(login.email)
        passwordTextEdit.setText(login.password)

        return view
    }

    private fun signIn() {
        // Get login data

        val email = emailTextEdit.text.toString().trim()
        val password = emailTextEdit.text.toString().trim()

        if (email == "" || password == "") {
            Toast.makeText(context, "Fields should be filled!", Toast.LENGTH_SHORT).show()
            return
        }

        val loginParams = LoginParams(email, password)

        disableButtons()

        GlobalScope.launch(Dispatchers.IO) {
            try {

                // Take token from API
                val token = RemoteApi.getInstance(requireContext()).login(loginParams)
                Preferences.getInstance(requireContext()).saveLogin(loginParams, token)

                // Successful login, start Main view
                launch(Dispatchers.Main) {
                    showMain()
                    Toast.makeText(context, "Successful login as $email", Toast.LENGTH_SHORT).show()
                }
            }
            catch (e: Exception) {
                // Oops! Error, show message
                launch(Dispatchers.Main) {
                    enableButtons()
                    Toast.makeText(context, "Cannot login. Try again later", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showMain() {
        startActivity(Intent(context, MapActivity::class.java))
        requireActivity().finish()
    }

    private fun showSignUp() {
        val transaction = parentFragmentManager.beginTransaction()
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        transaction.replace(R.id.frameLayout, SignUpFragment.getInstance())
        transaction.commit()
    }

    private fun disableButtons() {
        signInButton.isEnabled = false
    }

    private fun enableButtons() {
        signInButton.isEnabled = true
    }
}