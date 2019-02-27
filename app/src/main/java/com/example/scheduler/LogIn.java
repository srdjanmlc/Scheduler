package com.example.scheduler;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.shobhitpuri.custombuttons.GoogleSignInButton;

public class LogIn extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AndroidClarified";
    private GoogleSignInButton signInButton;
    public static GoogleSignInClient googleSignInClient;
    private AsyncTasks newAsyncTask;
    //List of permission that app need to be granted in order to work as intended.
    private String[] perms = {"android.permission.READ_CONTACTS", "android.permission.SEND_SMS", "android.permission.READ_PHONE_STATE", "android.permission.READ_CALENDAR", "android.permission.WRITE_CALENDAR"};

    /*As the app is having the local SQLite I am using the AsyncTasks task to create db and tables,
     and to collect contacts from contact list and put them in the created tables.
     so I am making copies of contact names in the database. */

    public class AsyncTasks extends AsyncTask<Void, Void, Void> {

       /* DatabaseHelper is the my custom helper class to make tasks of
         creating bd and tables easier and code reusable
         See the class for implementation details. */

        private DatabaseHelper dbHelper;

        @Override
        protected Void doInBackground(Void... voids) {

            //create the database
            dbHelper = new DatabaseHelper(getApplicationContext(), "Contacts");
            //creating the users table
            dbHelper.dbOperations("CREATE TABLE IF NOT EXISTS users (id INTEGER PRIMARY KEY AUTOINCREMENT , name VARCHAR, birthdate VARCHAR, UNIQUE (name))");
            // creating table deleted_users
            dbHelper.dbOperations("CREATE TABLE IF NOT EXISTS deleted_users (id INTEGER PRIMARY KEY AUTOINCREMENT, name VARCHAR, UNIQUE (name))");
            //creating appointments table
            dbHelper.dbOperations("CREATE TABLE IF NOT EXISTS appointments (id INTEGER PRIMARY KEY AUTOINCREMENT , nameid INTEGER, appointmentdate VARCHAR)");

            /*Contacts is the my custom helper class to make tasks of
             reading contact and transferring them to local db easier and code reusable.
             See the class for implementation details. */
            Contacts readContacts = new Contacts(getApplicationContext(), dbHelper);
            readContacts.readAllContactsWrite2Db();

            return null;
        }

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_log_in);

        //Collection all needed user permission at the first startup.
        // At each following app start this is checking if in the mean time some permission have been revoked
        checkPermissions(perms);

        signInButton = findViewById(R.id.sign_in_button);

        //Building google default sing in dialog as per google docs.
        GoogleSignInOptions googleSignInOptions = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions);

        signInButton.setOnClickListener(this);
    }

    //Google signin button is clicked
    @Override
    public void onClick(View v) {
        Intent signInIntent = googleSignInClient.getSignInIntent();
        //App has been granted the credentials to access google calendar through Google Calendar API.
        startActivityForResult(signInIntent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case 101:
                    try {
                        // The Task returned from this call is always completed, no need to attach
                        // a listener.
                        Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
                        GoogleSignInAccount account = task.getResult(ApiException.class);
                        onLoggedIn(account);
                    } catch (ApiException e) {
                        // The ApiException status code indicates the detailed failure reason.
                        Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
                    }
                    break;
            }
    }

    /*If the login was successful new intent is started and user is transferred to Main activity.
    beside that I am sending the googleSignIn account details to Main activity I need info form it there.*/
    private void onLoggedIn(GoogleSignInAccount googleSignInAccount) {
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra(MainActivity.GOOGLE_ACCOUNT, googleSignInAccount);
        startActivity(intent);
        finish();
    }

    /* In onStart lifecycle of the activity I am checking if the current user is still
      logged in and if that is the case he is transferred to MainActivity */

    @Override
    protected void onStart() {
        super.onStart();
        GoogleSignInAccount alreadyloggedAccount = GoogleSignIn.getLastSignedInAccount(this);
        if (alreadyloggedAccount != null) {
          //  progressBar.setVisibility(View.VISIBLE);
            Toast.makeText(this, "Logging In....", Toast.LENGTH_LONG).show();
            onLoggedIn(alreadyloggedAccount);
        } else {
            Log.d(TAG, "Not logged in");
        }
    }


    // permission checks methods  +++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++

    public void checkPermissions(String[] permissions) {

        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, permissions, 1);
            } else {
                //doing the db creation, table creation, reading contacts and  writing to db off the main thread (in background)
                newAsyncTask = new LogIn.AsyncTasks();
                newAsyncTask.execute();
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {

            case 1:
                if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
                        /*permission granted
                        doing the db creation, table creation, reading contacts and  writing to db off the main thread (in background).
                        */
                        newAsyncTask = new LogIn.AsyncTasks();
                        newAsyncTask.execute();
                    }

                    if (Build.VERSION.SDK_INT >= 23) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
                            //permission granted
                        }
                    }

                    if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
                            //permission granted
                        }

                    }

                    if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                            //permission granted
                        }

                    }

                    if (permissions.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED) {
                            //permission granted
                        }

                    }

                }
        }

    }
}
