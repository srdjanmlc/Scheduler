package com.example.scheduler;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.scheduler.fragments.AddMeetingFragment;
import com.example.scheduler.fragments.ContactListFragment;
import com.example.scheduler.fragments.FirstFragment;
import com.example.scheduler.fragments.ScheduleFragment;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

// I am implementing NavigationView.OnNavigationItemSelectedListener in the mainActivity class.
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    public static final String GOOGLE_ACCOUNT = "google_account";
    private GoogleSignInAccount googleSignInAccount;
    private DrawerLayout drawer;
    private TextView username, email;
    private FirstFragment firstFragment;
    private NavigationView navigationView;
    private AddMeetingFragment addMeetingFragment;
    private ContactListFragment contactListFragment;
    private ScheduleFragment scheduleFragment;
    private ImageView profilePicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

//      Setting toolbar as action bar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        Finding Java objects and linking them to JAVA code
        drawer = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);

       /* I need this view to be able to access the xml element of the Navigation header part.
        There I am setting the username and the mane of the Current logged in user. */
        View headerView = navigationView.getHeaderView(0);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);


//        setting navigation drawer listener and toggle state
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

//        Getting the mane og the account (in this case email)
//        that will need me to get the users calendar info and present the email in the navigation drawer heading
//        this was sent though intent form LogIn Activity
        googleSignInAccount = getIntent().getParcelableExtra(GOOGLE_ACCOUNT);

        //making an instance of Contact fragment
        contactListFragment = new ContactListFragment();

        //making an instance of Contact fragment
        scheduleFragment = new ScheduleFragment();

      /*  Forming Bundle to put the the account email in it. I need this
        Bundle to use it to set argument for the fragment activities and in that way
        I am transferring data between fragment (the same way I use intent putExtra when I operate with activities. */
        Bundle data = new Bundle();
        data.putString("Account_Name", googleSignInAccount.getEmail());

        //loading main fragment
        firstFragment = new FirstFragment();
        firstFragment.setArguments(data);

      /*  When I am in any other fragment except the first one, when the device is rotated the app is returned to the first fragment.
        This is because on rotate the onCreate method of the main activity will execute and set fragment object to the first fragment.
        To avoid this and to retain the fragment I am currently viewing after the rotation I check if the savedInstanceState bundle
         is null or not. savedInstanceState bundle is null only at the fist start of the app. */
        if (null == savedInstanceState) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, firstFragment).commit();
        }

        // Transferring the Bundle data to addMeeting Fragment class. I need the email to initialize
        // CalendarContractHelper object there.
        addMeetingFragment = new AddMeetingFragment();
        addMeetingFragment.setArguments(data);

        username = headerView.findViewById(R.id.usernameTextView);
        email = headerView.findViewById(R.id.emailTextView);
        profilePicture = headerView.findViewById(R.id.profileImageView);

        username.setText(googleSignInAccount.getDisplayName());
        email.setText(googleSignInAccount.getEmail());

    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

         /* I don't want to add this first fragment on the backstack,
                so that when I press back button I will exit the app basically.
                On rest of the fragments I will put add to back stack, without that I will have the situation
                that no matter in which fragment I am currently
                I will exit the app, and I don't want that.I want to return to previous fragment I was in.
                I use setChecked method of the navigationView to select (color it) the appropriate menu item in the
                drawer.*/

        switch (item.getItemId()) {
            case R.id.nav_calendar:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,firstFragment).commit();
                break;
            case R.id.nav_contact:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, contactListFragment).addToBackStack(null).commit();
                navigationView.getMenu().getItem(1).setChecked(true);
                break;
            case R.id.nav_meeting:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,addMeetingFragment).addToBackStack(null).commit();
                navigationView.getMenu().getItem(2).setChecked(true);
                break;
            case R.id.nav_schedule:
                getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container,scheduleFragment).addToBackStack(null).commit();
                navigationView.getMenu().getItem(3).setChecked(true);
                break;
            case R.id.nav_signout:
                singOut();
                navigationView.getMenu().getItem(4).setChecked(true);
                break;

        }

        //This is closing the navigation drawer after the selection has been made.
        drawer.closeDrawer(GravityCompat.START);

        return true;

    }

    public void singOut() {

         /*
          Sign-out is initiated by simply calling the googleSignInClient.signOut API. I add a
          listener which will be invoked once the sign out is the successful.
           */
        LogIn.googleSignInClient.signOut().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                //On Succesful signout I navigate back to the Login Activity
                Intent intent = new Intent(getApplicationContext(), LogIn.class);
                startActivity(intent);
            }
        });
    }


    @Override
    public void onBackPressed() {
        //This is to check if the navigation drawer is opened and if it opened close it on back press.
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}
