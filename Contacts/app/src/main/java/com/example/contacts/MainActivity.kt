package com.example.contacts

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var contactAdapter: ContactListRecyclerViewAdapter

    private var haveContactPermission = false

    companion object {
        const val sorry = "Can't show contacts without your permission"
        const val requestContacts = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        checkPermissions()
    }

    private fun checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_CONTACTS),
                requestContacts
            )
        } else {
            showContacts()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            requestContacts -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showContacts()
                } else {
                    showSorry()
                }
            }
        }
    }

    private fun showContacts() {
        val contacts = this.fetchAllContacts().sortedBy { contact -> contact.name }
        contactAdapter = ContactListRecyclerViewAdapter(contacts).apply {
            val message = resources.getQuantityString(
                R.plurals.numberOfContacts,
                contacts.size,
                contacts.size
            )
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
            onClickListener = {
                openCall(it)
            }
        }
        contact_list.adapter = contactAdapter
    }

    private fun showSorry() {
        Toast.makeText(this, sorry, Toast.LENGTH_LONG).show()

    }

    private fun openCall(contact: Contact) {
        val openCallIntent: Intent = Intent().apply {
            action = Intent.ACTION_DIAL
            data = Uri.parse("tel:${contact.phoneNumber}")
        }
        startActivity(openCallIntent)
    }
}
