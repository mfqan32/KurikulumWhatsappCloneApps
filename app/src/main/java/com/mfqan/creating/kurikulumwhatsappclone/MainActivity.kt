package com.mfqan.creating.kurikulumwhatsappclone

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.*
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.mfqan.creating.kurikulumwhatsappclone.activities.ContactActivity
import com.mfqan.creating.kurikulumwhatsappclone.activities.ProfileActivity
import com.mfqan.creating.kurikulumwhatsappclone.activities.SignInActivity
import com.mfqan.creating.kurikulumwhatsappclone.adapter.SectionPagerAdapter
import com.mfqan.creating.kurikulumwhatsappclone.fragments.ChatsFragment
import com.mfqan.creating.kurikulumwhatsappclone.listener.FailureCallback
import com.mfqan.creating.kurikulumwhatsappclone.util.DATA_USERS
import com.mfqan.creating.kurikulumwhatsappclone.util.DATA_USER_PHONE
import com.mfqan.creating.kurikulumwhatsappclone.util.PERMISSION_REQUEST_READ_CONTACT
import com.mfqan.creating.kurikulumwhatsappclone.util.REQUEST_NEW_CHATS
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity(), FailureCallback {

    private var mySectionPagerAdapter: SectionPagerAdapter? = null
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDb = FirebaseFirestore.getInstance()
    private val chatsFragment = ChatsFragment()

    companion object {
        const val PARAM_NAME = "name"
        const val PARAM_PHONE = "phone"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        chatsFragment.setFailureCallbackListener(this)
        setSupportActionBar(toolbar_main)
        mySectionPagerAdapter = SectionPagerAdapter(supportFragmentManager)

        container_viewpager.addOnPageChangeListener(TabLayout.TabLayoutOnPageChangeListener(tabs))
        tabs.addOnTabSelectedListener(
            TabLayout.ViewPagerOnTabSelectedListener(
                container_viewpager
            )
        )
        resizeTabs()
        tabs.getTabAt(1)?.select()
        tabs.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> fab.hide()
                    1 -> fab.show()
                    2 -> fab.hide()

                }
            }
        })
        fab.setOnClickListener {
            onNewChat()
        }
        container_viewpager.adapter = mySectionPagerAdapter
    }

    private fun onNewChat() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) { // pengecheckan izin dari aplikasi // izin tidak diberikan
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_CONTACTS
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Contacts Permission")
                    .setMessage("This App Requires Access to Your Contacts to Initiation A Concersation")
                    .setPositiveButton("Yes") { dialogInterface, i ->
                        izinmintaKontak()
                    }
                    .setNegativeButton("No") { dialogInterface, i ->

                    }
                    .show()
            } else {
                izinmintaKontak()
            }
        } else {
            startNewActivity()
        }
    }

    private fun izinmintaKontak() {
        ActivityCompat.requestPermissions(
            this, arrayOf(Manifest.permission.READ_CONTACTS), PERMISSION_REQUEST_READ_CONTACT
            // meminta izin untuk membaca // kontak dalam ponsel
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when(requestCode){
            PERMISSION_REQUEST_READ_CONTACT -> {
                if (grantResults.isNotEmpty() && grantResults[0] ==
                    PackageManager.PERMISSION_GRANTED){
                    startNewActivity() // memulai activity yang lain dengan memakai intent
                }
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    private fun startNewActivity() {
        startActivityForResult(Intent(this, ContactActivity::class.java), REQUEST_NEW_CHATS)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                REQUEST_NEW_CHATS -> {
                    val name = data?.getStringExtra(PARAM_NAME)  ?: ""
                    val phone = data?.getStringExtra(PARAM_PHONE)?: ""
                    checkNewChat(name,phone)
                }
            }
        }
    }

    private fun checkNewChat(name: String, phone: String) {
        if (name.isNotEmpty() && phone.isNotEmpty()) {
            firebaseDb.collection(DATA_USERS) // mengakses table User di Firebase
                    .whereEqualTo(DATA_USER_PHONE, phone) // ketika data phone = phone di kontak
                    .get()
                    .addOnSuccessListener {
                        if (it.documents.size > 0) {
                            chatsFragment.newChat(it.documents[0].id)
                        } else {
                            AlertDialog.Builder(this)
                                    .setTitle("Judul")
                                    .setMessage("Pesan")
                                    .setPositiveButton("Iya") {dialog, i ->
                                        val intent = Intent(Intent(ACTION_VIEW))
                                        intent.data = Uri.parse("sms:$phone") // query untuk mengirim pesan intent
                                        intent.putExtra("sms_body", "Hi I'm using this new cool WhatsAppClone app")
                                        startActivity(intent)
                                    }
                                    .setNegativeButton("Tidak", null)
                                    .setCancelable(false)
                                    .show()
                        }
                    }
        }
    }


    private fun resizeTabs() {
        val layout = (tabs.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val layoutParams = layout.layoutParams as LinearLayout.LayoutParams
        layoutParams.weight = 0.5f
        layout.layoutParams = layoutParams
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> onLogout()
            R.id.action_profile -> onProfile()

        }
        return super.onOptionsItemSelected(item)
    }

    override fun onUserError() {
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }


    private fun onProfile() {
        startActivity(Intent(this, ProfileActivity::class.java))
    }

    private fun onLogout() {
        firebaseAuth.signOut()
        startActivity(Intent(this, SignInActivity::class.java))
        finish()
    }
}

























//class PlaceHolderFragment : Fragment() {
//        companion object {
//            private const val ARG_SECTION_NUMBER = "section_number"
//            fun newIntent(sectionNumber: Int): PlaceHolderFragment {
//                val fragment = PlaceHolderFragment()
//                val args = Bundle() //mengikat data untuk di kirim secara bersama
//                args.putInt(ARG_SECTION_NUMBER, sectionNumber) //mengirimkan datanya
//                fragment.arguments = args
//                return fragment
//            }
//        }
//
//        //memasangkan tampilan dari layout dengan menggunakan LayoutInflater
//        override fun onCreateView(
//            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
//        ): View? {
//            val rootView = inflater.inflate(R.layout.fragment_main, container, false)
//            rootView.section_label.text = "Test ${arguments?.getInt(ARG_SECTION_NUMBER)}"
//            return rootView
//        }
//    }