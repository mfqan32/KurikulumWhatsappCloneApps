package com.mfqan.creating.kurikulumwhatsappclone.activities

import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.mfqan.creating.kurikulumwhatsappclone.R
import com.mfqan.creating.kurikulumwhatsappclone.model.User
import com.mfqan.creating.kurikulumwhatsappclone.util.*
import kotlinx.android.synthetic.main.activity_profile.btn_apply
import kotlinx.android.synthetic.main.activity_profile.btn_delete_account
import kotlinx.android.synthetic.main.activity_profile.edt_email_profile
import kotlinx.android.synthetic.main.activity_profile.edt_name_profile
import kotlinx.android.synthetic.main.activity_profile.edt_phone_profile
import kotlinx.android.synthetic.main.activity_profile.progress_layout_screen_profile
import kotlinx.android.synthetic.main.activity_screen_profile.*

class ProfileActivity : AppCompatActivity() {

    private var firebaseDB = FirebaseFirestore.getInstance()
    private var userID = FirebaseAuth.getInstance().currentUser?.uid
    private var firebaseAuth = FirebaseAuth.getInstance()
    private var firebaseStorage = FirebaseStorage.getInstance().reference
    private var imageUrl: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        if (userID.isNullOrEmpty()) {
            finish()
        }
        imbtn_profile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, REQUEST_CODE_PHOTO)
        }
        btn_delete_account.setOnClickListener {
            onDelete()
        }
        btn_apply.setOnClickListener {
            onApply()
        }
        getUserInfo()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_PHOTO) {
            storeImage(data?.data) // method storeImage dijalankan setelah pengguna memilih gambar
        }
    }

    private fun storeImage(data: Uri?) {
        if (data != null) {
            Toast.makeText(this, "Uploading...", Toast.LENGTH_SHORT).show()
            progress_layout_screen_profile.visibility = View.VISIBLE
            val filePath = firebaseStorage.child(DATA_IMAGES).child(userID!!)

            filePath.putFile(data)
                .addOnSuccessListener {
                    filePath.downloadUrl
                        .addOnSuccessListener {
                            val url = it.toString() // mengubah uri menjadi String
                            firebaseDB.collection(DATA_USERS)
                                .document(userID!!)
                                .update(DATA_USER_IMAGE_URL, url)
                                .addOnSuccessListener {
                                    imageUrl = url
                                    populateImage(this, imageUrl, img_profile, R.drawable.ic_user)
                                }
                            progress_layout_screen_profile.visibility = View.GONE
                        }
                        .addOnFailureListener {
                            onUploadFailure()
                        }
                }
                .addOnFailureListener {
                    onUploadFailure()
                }
        }
    }

    private fun onUploadFailure() {
        Toast.makeText(this, "Image upload failed. Please try again later.", Toast.LENGTH_SHORT)
            .show()
        progress_layout_screen_profile.visibility = View.GONE
    }

    private fun onApply() {
        progress_layout_screen_profile.visibility = View.VISIBLE
        val name = edt_name_profile.text.toString()
        val email = edt_email_profile.text.toString()
        val phone = edt_phone_profile.text.toString()

        val profileMap = HashMap<String, Any>()
        profileMap[DATA_USER_NAME] = name
        profileMap[DATA_USER_EMAIL] = email
        profileMap[DATA_USER_PHONE] = phone

        firebaseAuth.currentUser?.updateEmail(userID!!)
        firebaseDB.collection(DATA_USERS).document(userID!!).update(profileMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Update Success", Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener {
                it.printStackTrace()
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show()
                progress_layout_screen_profile.visibility = View.GONE
            }
    }

    private fun onDelete() {
        progress_layout_screen_profile.visibility = View.VISIBLE
        AlertDialog.Builder(this)
            .setTitle("Delete Akun")
            .setMessage("yakin mau hapus")
            .setPositiveButton("Yes") { _, _ ->
                firebaseDB.collection(DATA_USERS).document(userID!!).delete()
                firebaseStorage.child(DATA_IMAGES).child(userID!!).delete()
                firebaseAuth.currentUser?.delete()
                    ?.addOnSuccessListener {
                        finish()
                    }
                    ?.addOnFailureListener {
                        Toast.makeText(this, "failed", Toast.LENGTH_SHORT).show()
                    }
                progress_layout_screen_profile.visibility = View.GONE
                Toast.makeText(this, "Delete Success", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this,SignInActivity::class.java))
                finish()
            }
            .setNegativeButton("No") { _, _ ->
                progress_layout_screen_profile.visibility = View.GONE
            }
            .setCancelable(false)  //alert tidak akan hilang tanpa menekan butoon no/yes
            .show()
    }
    private fun getUserInfo() {
        progress_layout_screen_profile.visibility = View.VISIBLE
        firebaseDB.collection(DATA_USERS).document(userID!!).get()
            .addOnSuccessListener {
                val user = it.toObject(User::class.java)
                imageUrl = user?.imageUrl
                edt_name_profile.setText(user?.name, TextView.BufferType.EDITABLE)
                edt_email_profile.setText(user?.email, TextView.BufferType.EDITABLE)
                edt_phone_profile.setText(user?.phone, TextView.BufferType.EDITABLE)
                if (imageUrl != null) {
                    populateImage(this, user?.imageUrl, img_profile, R.drawable.ic_user)
                }
                progress_layout_screen_profile.visibility = View.GONE
            }.addOnFailureListener {
                it.printStackTrace()
                finish()
            }
    }
}