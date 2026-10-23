package com.example.experiment10

import android.Manifest
import android.content.ContentProviderOperation
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.os.Bundle
import android.provider.ContactsContract
import android.text.InputType
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.doAfterTextChanged

/**
 * Experiment 10: Data sharing using the Contacts Content Provider.
 * The phone's Contacts app owns the contacts data. Our app does not touch its
 * database directly; it asks the system through a ContentResolver, which talks
 * to the Contacts ContentProvider (ContactsContract).
 *   - READ  : contentResolver.query(...)       -> list + search contacts
 *   - WRITE : contentResolver.applyBatch(...)  -> add a new contact
 * The UI is built in code, so no XML layout files are needed.
 */
class MainActivity : AppCompatActivity() {

    private lateinit var tvCount: TextView
    private lateinit var etName: EditText
    private lateinit var etNumber: EditText
    private lateinit var etSearch: EditText
    private lateinit var adapter: ArrayAdapter<String>

    // Asks the user for READ and WRITE contacts permission at runtime
    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
            if (result[Manifest.permission.READ_CONTACTS] == true) {
                toast("Permission granted")
                loadContacts()
            } else {
                tvCount.text = "Permission denied. Tap 'Grant Permission' to allow access."
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildUi()
        if (hasPermission(Manifest.permission.READ_CONTACTS)) {
            loadContacts()
        } else {
            requestPermissions()
        }
    }

    private fun buildUi() {
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(
                WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.ime()
            )
            v.setPadding(40 + bars.left, 30 + bars.top, 40 + bars.right, 30 + bars.bottom)
            insets
        }

        root.addView(text("Experiment 10: Contacts Content Provider", 20f, bold = true))
        root.addView(text("Name: Nimitha BC  |  USN: 25MCAR0132", 14f))

        root.addView(Button(this).apply {
            text = "Grant Permission"
            setOnClickListener { requestPermissions() }
        })

        // ----- Add contact -----
        root.addView(text("Add a contact", 16f, bold = true))
        etName = EditText(this).apply { hint = "Contact name" }
        etNumber = EditText(this).apply {
            hint = "Phone number"
            inputType = InputType.TYPE_CLASS_PHONE
        }
        root.addView(etName)
        root.addView(etNumber)
        root.addView(Button(this).apply {
            text = "Add Contact"
            setOnClickListener { addContact() }
        })

        // ----- Search + list -----
        root.addView(text("Contacts from the phone", 16f, bold = true))
        etSearch = EditText(this).apply { hint = "Search by name" }
        etSearch.doAfterTextChanged { loadContacts(it?.toString().orEmpty()) }
        root.addView(etSearch)

        tvCount = text("", 14f)
        root.addView(tvCount)

        adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, mutableListOf<String>())
        val listView = ListView(this).apply { this.adapter = this@MainActivity.adapter }
        root.addView(listView, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 0, 1f
        ))

        setContentView(root)
    }

    // ---------- READ: query the Contacts provider ----------
    private fun loadContacts(filter: String = "") {
        if (!hasPermission(Manifest.permission.READ_CONTACTS)) {
            tvCount.text = "Permission needed to read contacts"
            return
        }
        val nameCol = ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
        val numberCol = ContactsContract.CommonDataKinds.Phone.NUMBER

        val selection = if (filter.isBlank()) null else "$nameCol LIKE ?"
        val args = if (filter.isBlank()) null else arrayOf("%$filter%")

        val rows = mutableListOf<String>()
        contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(nameCol, numberCol),
            selection,
            args,
            "$nameCol ASC"
        )?.use { cursor ->
            val nameIdx = cursor.getColumnIndexOrThrow(nameCol)
            val numIdx = cursor.getColumnIndexOrThrow(numberCol)
            while (cursor.moveToNext()) {
                rows.add("${cursor.getString(nameIdx)}\n${cursor.getString(numIdx)}")
            }
        }

        adapter.clear()
        adapter.addAll(rows)
        adapter.notifyDataSetChanged()
        tvCount.text = "Contacts found: ${rows.size}"
    }

    // ---------- WRITE: insert a contact through the provider ----------
    private fun addContact() {
        val name = etName.text.toString().trim()
        val number = etNumber.text.toString().trim()
        if (name.isEmpty() || number.isEmpty()) {
            toast("Enter name and number")
            return
        }
        if (!hasPermission(Manifest.permission.WRITE_CONTACTS)) {
            toast("Grant permission first")
            requestPermissions()
            return
        }

        val ops = ArrayList<ContentProviderOperation>()
        // 1) create an empty raw contact
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, null)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, null)
                .build()
        )
        // 2) add the name to it
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name)
                .build()
        )
        // 3) add the phone number to it
        ops.add(
            ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(
                    ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                )
                .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, number)
                .withValue(
                    ContactsContract.CommonDataKinds.Phone.TYPE,
                    ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE
                )
                .build()
        )

        try {
            contentResolver.applyBatch(ContactsContract.AUTHORITY, ops)
            toast("Contact added")
            etName.setText("")
            etNumber.setText("")
            etSearch.setText("")
            loadContacts()
        } catch (e: Exception) {
            toast("Could not add contact: ${e.message}")
        }
    }

    // ---------- Helpers ----------
    private fun hasPermission(p: String) =
        ContextCompat.checkSelfPermission(this, p) == PackageManager.PERMISSION_GRANTED

    private fun requestPermissions() {
        permissionLauncher.launch(
            arrayOf(Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)
        )
    }

    private fun text(t: String, size: Float, bold: Boolean = false) = TextView(this).apply {
        text = t
        textSize = size
        if (bold) typeface = Typeface.DEFAULT_BOLD
        setPadding(0, 12, 0, 8)
    }

    private fun toast(msg: String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
}
