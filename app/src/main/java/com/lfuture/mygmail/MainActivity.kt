package com.lfuture.mygmail

import android.Manifest
import android.accounts.AccountManager
import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.gmail.Gmail
import com.google.api.services.gmail.GmailScopes
import com.lfuture.mygmail.util.CheckServices
import com.lfuture.mygmail.util.MailService
import com.lfuture.mygmail.util.Utils
import kotlinx.android.synthetic.main.activity_main.*
import javax.mail.MessagingException
import javax.mail.internet.MimeMessage


class MainActivity : AppCompatActivity() {


    private val mailService = MailService()
    private val checkServices = CheckServices(this)
    private var mService: Gmail? = null
    private lateinit var mCredential: GoogleAccountCredential

    //these are gMail scopes to do action automatically for us
    //we do not need commented scopes cuz we only want to compose and send
    private val SCOPES = arrayListOf(
        GmailScopes.GMAIL_SEND,
        GmailScopes.GMAIL_COMPOSE
//        GmailScopes.GMAIL_INSERT,
//        GmailScopes.GMAIL_MODIFY,
//        GmailScopes.GMAIL_READONLY,
//        GmailScopes.MAIL_GOOGLE_COM
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mCredential = GoogleAccountCredential.usingOAuth2(applicationContext, SCOPES)
            .setBackOff(ExponentialBackOff())

        btnSendMail.setOnClickListener {
            chooseAccount()
        }
        //change gMail account
        btnChange.setOnClickListener {
            startActivityForResult(
                mCredential.newChooseAccountIntent(),
                Utils.REQUEST_ACCOUNT_PICKER
            )

        }
    }


    private fun getResultsFromApi() {
        if (!checkServices.isGooglePlayServicesAvailable()) {
            checkServices.acquireGooglePlayServices()
        } else if (mCredential.selectedAccountName == null) {
            chooseAccount()
        } else {
            MakeRequestTask(this, mCredential).execute()
        }
    }

    @Suppress("PrivatePropertyName")
    private val PREF_ACCOUNT_NAME = "accountName"

    private fun chooseAccount() {
        if (Utils.checkPermission(this, Manifest.permission.GET_ACCOUNTS)) {
            //to check if account is chosen from gmail dialog
            val accName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null)
            if (accName != null) {
                //if exist we send email
                Log.e("ACC", "EXIST $accName")
                mCredential.selectedAccountName = accName
                getResultsFromApi()
            } else {
                //if accName does not exist we request accName(gmail) again
                startActivityForResult(
                    mCredential.newChooseAccountIntent(),
                    Utils.REQUEST_ACCOUNT_PICKER
                )
            }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.GET_ACCOUNTS),
                Utils.REQUEST_PERMISSION_GET_ACCOUNTS
            )
        }
    }

    //permission results are returned here from requestPermission method(function)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Utils.REQUEST_PERMISSION_GET_ACCOUNTS -> {
                chooseAccount()
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            Utils.REQUEST_ACCOUNT_PICKER -> {
                if (resultCode == RESULT_OK && data != null && data.extras != null) {
                    val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                    if (accountName != null) {
                        val settings = getPreferences(Context.MODE_PRIVATE)
                        val editor = settings.edit()
                        editor.putString(PREF_ACCOUNT_NAME, accountName)
                        editor.apply()
                        mCredential.selectedAccountName = accountName
                        getResultsFromApi()
                    }
                }
            }
            Utils.REQUEST_AUTHORIZATION -> {
                if (resultCode == RESULT_OK) {
                    getResultsFromApi()
                }
            }
        }
    }

    //async task class for Network Request
    private inner class MakeRequestTask(
        _activity: MainActivity,
        credential: GoogleAccountCredential
    ) :
        AsyncTask<Void, Void, String>() {
        private val activity: MainActivity
        private var mLastError: Exception? = null

        init {
            val transport = AndroidHttp.newCompatibleTransport()
            val jsonFactory = JacksonFactory.getDefaultInstance()
            mService = Gmail.Builder(
                transport, jsonFactory, credential
            )
                .setApplicationName(resources.getString(R.string.app_name))
                .build()

            this.activity = _activity
        }

        override fun doInBackground(vararg params: Void?): String {
            return try {
                getData()
            } catch (e: Exception) {
                mLastError = e
                cancel(true)
                ""
            }
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            if (result == null || result.isEmpty()) {
                Log.e("PostExecute", "No results returned.")
            } else {
                Log.e("result", result)
            }
        }


        override fun onCancelled() {
            super.onCancelled()
            if (mLastError is UserRecoverableAuthIOException) {
                startActivityForResult(
                    (mLastError as UserRecoverableAuthIOException).intent,
                    Utils.REQUEST_AUTHORIZATION
                )
            } else {
                Log.e("Cancel", "The following error occurred:\n$mLastError")
                Log.v("Error", "" + mLastError)
            }

        }

    }

    private fun getData(): String {
        val user = "me" //this is google gMail default user id for yourself
        val to = "yuya5995@gmail.com"
        val from = mCredential.selectedAccountName  //we get account name from gMail dialog
        val subject = "Hi"
        val body = "hello from mandalay"
        val mimeMessage: MimeMessage
        var response = ""
        try {
            mimeMessage = mailService.createEmail(to, from, subject, body)
            response = mailService.sendMessage(mService!!, user, mimeMessage)

        } catch (e: MessagingException) {
            e.printStackTrace()
        }
        return response
    }

}

