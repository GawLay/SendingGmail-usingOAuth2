package com.lfuture.mygmail.javaVersion;

import android.Manifest;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.ExponentialBackOff;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.GmailScopes;
import com.lfuture.mygmail.R;
import com.lfuture.mygmail.util.Permission;
import com.lfuture.mygmail.util.javaVersion.CheckVersionJava;
import com.lfuture.mygmail.util.javaVersion.MailServiceJava;
import com.lfuture.mygmail.util.javaVersion.UtilPermission;

import java.io.IOException;
import java.util.ArrayList;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

public class MainJavaActivity extends AppCompatActivity {
    private MailServiceJava mailService = new MailServiceJava();
    private CheckVersionJava checkServices = new CheckVersionJava(this);
    private GoogleAccountCredential mCredential;
    private ArrayList<String> gmailScopesArrayList = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //these are gMail scopes to do action automatically for us
        //we do not need commented scopes cuz we only want to compose and send
        gmailScopesArrayList.add(GmailScopes.GMAIL_SEND);
        gmailScopesArrayList.add(GmailScopes.GMAIL_COMPOSE);

        mCredential = GoogleAccountCredential.usingOAuth2(getApplicationContext(), gmailScopesArrayList)
                .setBackOff(new ExponentialBackOff());

        Button btnSendMail = findViewById(R.id.btnSendMail);
        Button btnChange = findViewById(R.id.btnChange);
        btnSendMail.setOnClickListener(view -> chooseAccount());
        btnChange.setOnClickListener(view -> startActivityForResult(
                mCredential.newChooseAccountIntent(), Permission.REQUEST_ACCOUNT_PICKER
        ));

    }

    private void getResultsFromApi() {
        if (!checkServices.isGooglePlayServicesAvailable()) {
            checkServices.acquireGooglePlayServices();
        } else if (mCredential.getSelectedAccount() == null) {
            chooseAccount();
        } else {
            new MakeRequestTask(this, mCredential).execute();
        }
    }

    private String PREF_ACCOUNT_NAME = "accountName";

    private void chooseAccount() {
        if (UtilPermission.checkPermission(this, Manifest.permission.GET_ACCOUNTS)) {
            //to check if account is chosen from gmail dialog
            String accName = getPreferences(Context.MODE_PRIVATE).getString(PREF_ACCOUNT_NAME, null);
            if (accName != null) {
                //if exist we send email
                Log.e("ACC", "EXIST $accName");
                mCredential.setSelectedAccountName(accName);
                getResultsFromApi();
            } else {
                //if accName does not exist we request accName(gmail) again
                startActivityForResult(
                        mCredential.newChooseAccountIntent(),
                        Permission.REQUEST_ACCOUNT_PICKER
                );
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.GET_ACCOUNTS}, UtilPermission.REQUEST_PERMISSION_GET_ACCOUNTS);

        }
    }

    //permission results are returned here from requestPermission method(function)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case UtilPermission.REQUEST_PERMISSION_GET_ACCOUNTS:
                chooseAccount();
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case UtilPermission.REQUEST_ACCOUNT_PICKER:
                if (resultCode == RESULT_OK && data != null && data.getExtras() != null) {
                    String accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {
                        SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.apply();
                        mCredential.setSelectedAccountName(accountName);
                        getResultsFromApi();
                    }
                }
                break;
            case UtilPermission.REQUEST_AUTHORIZATION:
                if (resultCode == RESULT_OK) {
                    getResultsFromApi();
                }
                break;
        }
    }

    //async task class for Network Request
    private class MakeRequestTask extends AsyncTask<Void, Void, String> {

        private Gmail mService = null;
        private Exception mLastError = null;
        private MainJavaActivity activity;

        MakeRequestTask(MainJavaActivity activity, GoogleAccountCredential credential) {
            HttpTransport transport = AndroidHttp.newCompatibleTransport();
            JsonFactory jsonFactory = JacksonFactory.getDefaultInstance();

            mService = new com.google.api.services.gmail.Gmail.Builder(
                    transport, jsonFactory, credential)
                    .setApplicationName(getResources().getString(R.string.app_name))
                    .build();
            this.activity = activity;
        }

        @Override
        protected String doInBackground(Void... params) {
            try {
                return getData();
            } catch (Exception e) {
                mLastError = e;
                cancel(true);
                return null;
            }
        }

        private String getData() {
            // getting Values for to Address, from Address, Subject and Body
            String user = "me"; //this is google gMail default user id for yourself
            String to = "yuya5995@gmail.com"; //what can i say this is to #my Little# :P <3
            String from = mCredential.getSelectedAccountName();//we get account name from gMail dialog
            String subject = "hereComeSubject";
            String body = "here come body Text";
            MimeMessage mimeMessage;
            String response = "";
            try {
                mimeMessage = mailService.createEmail(to, from, subject, body);
                response = mailService.sendMessage(mService, user, mimeMessage);
            } catch (MessagingException | IOException e) {
                e.printStackTrace();
            }
            return response;
        }


        @Override
        protected void onPostExecute(String output) {
            if (output == null || output.length() == 0) {
                Log.e("PostExecute", "No results returned.");
            } else {
                Log.e("result", output);
            }
        }

        @Override
        protected void onCancelled() {
            super.onCancelled();
            if (mLastError != null) {
                if (mLastError instanceof UserRecoverableAuthIOException) {
                    startActivityForResult(
                            ((UserRecoverableAuthIOException) mLastError).getIntent(),
                            UtilPermission.REQUEST_AUTHORIZATION);
                } else {
                    Log.e("Error", "The following error occurred:\n$mLastError");
                    Log.e("Error", mLastError.toString() + "");
                }
            } else {
                Log.v("Error", "" + mLastError);
            }

        }
    }


}
