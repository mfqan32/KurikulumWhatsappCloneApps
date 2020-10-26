package com.mfqan.creating.kurikulumwhatsappclone.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.mfqan.creating.kurikulumwhatsappclone.MainActivity
import com.mfqan.creating.kurikulumwhatsappclone.R
import kotlinx.android.synthetic.main.activity_sign_in.*

class SignInActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener {
        val user = firebaseAuth.currentUser?.uid
        if (user != null) {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sign_in)

        setTextChangedListener(edt_email,til_email)
        setTextChangedListener(edt_password,til_password)
        progress_layout.setOnTouchListener { _, _ -> true }

        btn_login.setOnClickListener {
            onLogin()
        }
        txt_signup.setOnClickListener {
            startActivity(Intent(this,RegisterActivity::class.java))
        }
    }

    private fun setTextChangedListener(edt: TextInputEditText?, til: TextInputLayout?) {
        edt?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til?.isErrorEnabled = false //saat editext berubah/di ubah til tidak menampilkan error
            }
        })
    }
    private fun onLogin() {
        var process = true
        if (edt_email.text.isNullOrEmpty()){        //cek edt email jika kosong
            til_email.error = "Membutuhkan Email"   //menampilkan pesan / toast
            til_email.isErrorEnabled = true         //mengubah statment til yg  sebelumnya tidak
            process = false
        }
        if (edt_password.text.isNullOrEmpty()){         //cek edt password jika kosong
            til_password.error = "Membutuhkan Email"    //menampilkan pesan / toast
            til_password.isErrorEnabled = true          //mengubah statment til yg  sebelumnya tidak
            process = false
        }
        if (process) {
            progress_layout.visibility = View.VISIBLE
            firebaseAuth.signInWithEmailAndPassword(
                edt_email.text.toString(),
                edt_password.text.toString()
            ).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    progress_layout.visibility = View.GONE
                    Toast.makeText(
                        this@SignInActivity,
                        "Login error: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }.addOnFailureListener { e ->
                    progress_layout.visibility = View.GONE
                    e.printStackTrace()
                }
        }
    }
    override fun onStart() {
        super.onStart()
        firebaseAuth.addAuthStateListener(firebaseAuthListener)
    }
    override fun onStop() {
        super.onStop()
        firebaseAuth.removeAuthStateListener(firebaseAuthListener)

    }
}