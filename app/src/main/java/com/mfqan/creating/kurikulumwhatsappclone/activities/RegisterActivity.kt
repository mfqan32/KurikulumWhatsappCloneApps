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
import com.google.firebase.firestore.FirebaseFirestore
import com.mfqan.creating.kurikulumwhatsappclone.model.User
import com.mfqan.creating.kurikulumwhatsappclone.MainActivity
import com.mfqan.creating.kurikulumwhatsappclone.R
import com.mfqan.creating.kurikulumwhatsappclone.util.DATA_USERS
import kotlinx.android.synthetic.main.activity_register.*

class RegisterActivity : AppCompatActivity() {

    private val firebaseAuth = FirebaseAuth.getInstance() //untuk akun
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val firebaseAuthListener = FirebaseAuth.AuthStateListener{
        //mengecek userid yang ada/aktif dan jika ada akan langsung intent ke mainActivity
        val user = firebaseAuth.currentUser?.uid
        if (user != null){
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        setTextChangedListener(edt_email_signup,til_email_signup)
        setTextChangedListener(edt_password_signup,til_password_signup)
        setTextChangedListener(edt_name,til_name)
        setTextChangedListener(edt_phone,til_phone)
        progress_layout_register.setOnTouchListener { _, _ -> true }

        btn_signup.setOnClickListener {
            onRegister()
        }
    }

    private fun setTextChangedListener(
        edt: TextInputEditText?,
        til: TextInputLayout?
    ) {
        edt?.addTextChangedListener(object : TextWatcher{
            override fun afterTextChanged(p0: Editable?) {}

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                til?.isErrorEnabled = false
            }
        })
    }

    private fun onRegister() {
        var proses = true
        if (edt_email_signup.text.isNullOrEmpty()){        //cek edt email jika kosong
            til_email_signup.error = "Membutuhkan Email"   //menampilkan pesan / toast
            til_email_signup.isErrorEnabled = true         //mengubah statment til yg  sebelumnya tidak
            proses = false
        }
        if (edt_password_signup.text.isNullOrEmpty()){        //cek edt email jika kosong
            til_password_signup.error = "Membutuhkan Email"   //menampilkan pesan / toast
            til_password_signup.isErrorEnabled = true         //mengubah statment til yg  sebelumnya tidak
            proses = false
        }
        if (edt_name.text.isNullOrEmpty()){        //cek edt email jika kosong
            til_name.error = "Membutuhkan Email"   //menampilkan pesan / toast
            til_name.isErrorEnabled = true         //mengubah statment til yg  sebelumnya tidak
            proses = false
        }
        if (edt_phone.text.isNullOrEmpty()){        //cek edt email jika kosong
            til_phone.error = "Membutuhkan Email"   //menampilkan pesan / toast
            til_phone.isErrorEnabled = true         //mengubah statment til yg  sebelumnya tidak
            proses = false
        }
        if (proses) {
            progress_layout_register.visibility = View.VISIBLE
            firebaseAuth.createUserWithEmailAndPassword(
                edt_email_signup.text.toString(),
                edt_password_signup.text.toString()
            ).addOnCompleteListener { task ->
                if (!task.isSuccessful) {
                    progress_layout_register.visibility = View.GONE
                    Toast.makeText(
                        this,
                        "SignUp error: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (firebaseAuth.uid != null) {
                    val email = edt_email_signup.text.toString()
                    val phone = edt_phone.text.toString()
                    val name = edt_name.text.toString()
                    val user = User(
                        email,
                        phone,
                        name,
                        "",
                        "Hello i'm new",
                        "",
                        ""
                    )
                    firebaseDb.collection(DATA_USERS)
                        .document(firebaseAuth.uid!!).set(user)
                }
                progress_layout_register.visibility = View.GONE
            }
                .addOnFailureListener {
                    progress_layout_register.visibility = View.GONE
                    it.printStackTrace()
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