package app.jam.jam.online;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import app.jam.jam.R;
import app.jam.jam.auth.LoginActivity;
import app.jam.jam.data.Constants;
import app.jam.jam.offline.OfflineActivity;
import app.jam.jam.settings.SettingsActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class OnlineActivity extends AppCompatActivity {

    private static final String TAG = "Online";

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    private TabLayout mTabLayout;
    private MaterialToolbar mToolbar;
    private CircleImageView mCurrentUserImage;
    private MaterialTextView mTitleTextView;
    private LinearLayout mContainer;

    private ContactsFragment mContactsFragment;
    private ExploreFragment mExploreFragment;

    private String mUserName, mUserImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        if (mCurrentUser == null) {
            goToLogin();
        }

        initializeViews();
        setTabLayoutAction();
        setToolbarAction();


        mContactsFragment = ContactsFragment.newInstance();
        mExploreFragment = ExploreFragment.newInstance();

        replaceFragment(mContactsFragment);

        FirebaseDatabase.getInstance().getReference()
                .child(Constants.ROOT_USERS).child(mCurrentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            mUserName = snapshot.child(Constants.CHILD_USERNAME).getValue().toString();
                            mTitleTextView.setText(mUserName);
                            if(snapshot.hasChild(Constants.CHILD_USER_IMAGE)) {
                                mUserImage = snapshot.child(Constants.CHILD_USER_IMAGE).getValue().toString();
                                Picasso.get().load(mUserImage).placeholder(R.drawable.profile_image).into(mCurrentUserImage);
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "addValueEventListener:onCancelled", error.toException());
                    }
                });
    }

    /**
     * This method changes current fragment with the given fragment
     *
     * @param fragment the fragment to transition to
     */
    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    private void initializeViews() {
        mTabLayout = findViewById(R.id.online_tabLayout);
        mContainer = findViewById(R.id.fragment_container);

        // Setting AppBar
        mToolbar = findViewById(R.id.online_topAppBar);
        mToolbar.getMenu().add(Menu.NONE, Constants.OFFLINE_MENU_ID, 1, R.string.menu_title_offline);
        mCurrentUserImage = findViewById(R.id.online_imageView);
        mTitleTextView = findViewById(R.id.online_title);
    }


    private void setToolbarAction() {
        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case Constants.OFFLINE_MENU_ID:
                        Toast.makeText(OnlineActivity.this, "Go offline selected", Toast.LENGTH_SHORT).show();
                        goToOfflineActivity();
                        break;
                    case R.id.item_settings:
                        Toast.makeText(OnlineActivity.this, "Settings selected", Toast.LENGTH_SHORT).show();
                        goToSettingsActivity();
                        break;
                    case R.id.item_help:
                        Toast.makeText(OnlineActivity.this, "Help selected", Toast.LENGTH_SHORT).show();
                        goToHelpActivity();
                        break;
                    case R.id.item_logout:
                        Toast.makeText(OnlineActivity.this, "Logout selected", Toast.LENGTH_SHORT).show();
                        logoutUser();
                        break;
                    case R.id.item_about:
                        Toast.makeText(OnlineActivity.this, "About us selected", Toast.LENGTH_SHORT).show();
                        goToAboutActivity();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    private void setTabLayoutAction() {
        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    replaceFragment(mContactsFragment);
                    System.out.println("Selected my contacts");
                } else if (position == 1) {
                    replaceFragment(mExploreFragment);
                    System.out.println("Selected explore");
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    System.out.println("Unselected my contacts");
                } else if (position == 1) {
                    System.out.println("Unselected explore");
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    System.out.println("Reselected my contacts");
                } else if (position == 1) {
                    System.out.println("Reselected explore");
                }
            }
        });
    }

    // For going back to Sign In
    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void logoutUser() {
        // forget login info
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.REMEMBER_ME, false);
        editor.remove(Constants.PREFERENCE_EMAIL);
        editor.remove(Constants.PREFERENCE_PASSWORD);
        editor.apply();
        mAuth.signOut();

        Log.i(TAG, "Login data cleared");
        goToLogin();
    }

    private void goToOfflineActivity() {
        Intent intent = new Intent(this, OfflineActivity.class);
        startActivity(intent);
    }

    private void goToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

    private void goToHelpActivity() {
//        Intent intent = new Intent(this, OfflineActivity.class);
//        startActivity(intent);
    }

    private void goToAboutActivity() {
//        Intent intent = new Intent(this, OfflineActivity.class);
//        startActivity(intent);
    }

}