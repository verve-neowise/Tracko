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
import com.neowise.tracko.data.model.RegisterParams
import com.neowise.tracko.view.map.MapActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SignUpFragment() : Fragment() {

    companion object {
        fun getInstance() : SignUpFragment {
            return SignUpFragment()
        }
    }

    private lateinit var nameTextEdit: EditText
    private lateinit var lastNameTextEdit: EditText
    private lateinit var emailTextEdit: EditText
    private lateinit var passwordTextEdit: EditText
    private lateinit var confirmPasswordTextEdit: EditText
    private lateinit var signUpButton: Button
    private lateinit var backToSignInButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_sign_up, container, false)

        nameTextEdit = view.findViewById(R.id.name_edit)
        lastNameTextEdit = view.findViewById(R.id.last_name_edit)
        emailTextEdit = view.findViewById(R.id.email_edit)
        passwordTextEdit = view.findViewById(R.id.password_edit)
        confirmPasswordTextEdit = view.findViewById(R.id.confirm_password_edit)

        signUpButton = view.findViewById(R.id.sign_up_btn)
        backToSignInButton = view.findViewById(R.id.back_to_sign_in_btn)

        signUpButton.setOnClickListener {
            signUp()
        }

        backToSignInButton.setOnClickListener {
            showSignIn()
        }

        return view
    }

    private fun signUp() {

        // Get login data
        val name = nameTextEdit.text.toString().trim()
        val lastName = lastNameTextEdit.text.toString().trim()
        val email = emailTextEdit.text.toString().trim()
        val password = passwordTextEdit.text.toString().trim()
        val confirmPassword = confirmPasswordTextEdit.text.toString().trim()

        // Empty fields, cant do request :(
        if (name == "" || lastName == "" || email == "" || password == "" || confirmPassword == "") {
            Toast.makeText(context, "Fields should be filled", Toast.LENGTH_SHORT).show()
            return
        }

        // Password will be confirmed
        if (password != confirmPassword) {
            Toast.makeText(context, "Password and confirmation do not match", Toast.LENGTH_SHORT).show()
            return
        }

        val signParams = RegisterParams(name, lastName, email, password)

        disableButtons()

        GlobalScope.launch(Dispatchers.IO) {
            try {

                // Take token from API
                val token = RemoteApi.getInstance(context!!).registration(signParams)
                Preferences.getInstance(context!!).saveLogin(LoginParams(email, password), token)

                // Successful login, start Main view
                launch(Dispatchers.Main) {
                    Toast.makeText(context, "Successful login as $email", Toast.LENGTH_SHORT).show()
                    showMain()
                }
            }
            catch (e: Exception) {
                // Oops! Error, show message
                launch(Dispatchers.Main) {

                    enableButtons()
                    Toast.makeText(context, "Cannot Sign Up. Try again later", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showMain() {
        startActivity(Intent(context, MapActivity::class.java))
        activity!!.finish()
    }

    private fun showSignIn() {
        val transaction = fragmentManager!!.beginTransaction()
        transaction.setCustomAnimations(android.R.animator.fade_in, android.R.animator.fade_out);
        transaction.replace(R.id.frameLayout, SignInFragment.getInstance())
        transaction.commit()
    }

    private fun disableButtons() {
        signUpButton.isEnabled = false
    }

    private fun enableButtons() {
        signUpButton.isEnabled = true
    }
}