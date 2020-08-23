package app.jam.jam.online;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import app.jam.jam.AboutActivity;
import app.jam.jam.R;
import app.jam.jam.auth.LoginActivity;
import app.jam.jam.data.Constants;
import app.jam.jam.help.HelpActivity;
import app.jam.jam.offline.OfflineActivity;
import app.jam.jam.profile.ProfileActivity;
import app.jam.jam.settings.SettingsActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class OnlineActivity extends AppCompatActivity {

    /**
     * Tag to use to {@link Log} messages
     */
    private static final String TAG = "Online";

    FirebaseAuth mAuth;
    FirebaseUser mCurrentUser;

    private TabLayout mTabLayout;
    private MaterialToolbar mToolbar;
    private CircleImageView mCurrentUserImage;
    private MaterialTextView mTitleTextView;

    private Fragment mContactsFragment, mExploreFragment;
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

        // for initializing views and setting actions
        initializeViews();
        setActions();

        getUserNameAndImage();

        mContactsFragment = ContactsFragment.newInstance();
        mExploreFragment = ExploreFragment.newInstance();
        replaceFragment(mContactsFragment);
    }

    /**
     * This method retrieves user name and image uri from {@link FirebaseDatabase}
     * and sets it to title and user image.
     */
    private void getUserNameAndImage() {
        FirebaseDatabase.getInstance().getReference()
                .child(Constants.ROOT_USERS).child(mCurrentUser.getUid())
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            mUserName = Objects
                                    .requireNonNull(snapshot.child(Constants.CHILD_USERNAME)
                                            .getValue()).toString();
                            mTitleTextView.setText(mUserName);
                            Toast.makeText(OnlineActivity.this, getString(R.string.toast_welcome_user, mUserName), Toast.LENGTH_SHORT).show();

                            if (snapshot.hasChild(Constants.CHILD_USER_IMAGE)) {
                                mUserImage = Objects
                                        .requireNonNull(snapshot.child(Constants.CHILD_USER_IMAGE)
                                                .getValue()).toString();
                                Picasso.get().load(mUserImage).placeholder(R.drawable.profile_image).into(mCurrentUserImage);
                            }
                            Log.i(TAG, "getUserNameAndImage:onDataChange");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "getUserNameAndImage:onCancelled", error.toException());
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
        transaction.replace(R.id.fragment_placeholder, fragment);
        transaction.commit();
    }

    // Initializes views from layout
    private void initializeViews() {
        // Initializing AppBar
        mToolbar = findViewById(R.id.online_topAppBar);
        mCurrentUserImage = findViewById(R.id.online_imageView);
        mTitleTextView = findViewById(R.id.online_title);

        mTabLayout = findViewById(R.id.online_tabLayout);
    }

    // Sets actions for tab layout and toolbar menu
    private void setActions() {
        mCurrentUserImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToProfileActivity();
            }
        });

        mTabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                int position = tab.getPosition();
                if (position == 0) {
                    replaceFragment(mContactsFragment);
                } else if (position == 1) {
                    replaceFragment(mExploreFragment);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.online_requests:
                        goToRequests();
                        break;
                    case R.id.online_my_profile:
                        goToProfileActivity();
                        break;
                    case R.id.online_go_offline:
                        goToOfflineActivity();
                        break;
                    case R.id.online_settings:
                        goToSettingsActivity();
                        break;
                    case R.id.online_help:
                        goToHelpActivity();
                        break;
                    case R.id.online_about:
                        goToAboutActivity();
                        break;
                    case R.id.online_logout:
                        new MaterialAlertDialogBuilder(OnlineActivity.this)
                                .setTitle(R.string.title_text_confirmation)
                                .setMessage(R.string.message_logout_confirmation)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(R.string.button_text_yes, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        logoutUser();
                                    }
                                })
                                .setNeutralButton(R.string.button_text_no, null)
                                .show();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    /**
     * For going from this activity, {@link OnlineActivity}, to {@link RequestsActivity}.
     */
    private void goToRequests() {
        Intent intent = new Intent(this, RequestsActivity.class);
        startActivity(intent);
    }

    /**
     * For going from this activity, {@link OnlineActivity}, to {@link ProfileActivity}.
     * It passes current user id as string extra.
     */
    private void goToProfileActivity() {
        Intent intent = new Intent(this, ProfileActivity.class);
        intent.putExtra(Constants.RECEIVER_USER_ID, mCurrentUser.getUid());
        startActivity(intent);
    }

    /**
     * For going from this activity, {@link OnlineActivity}, to {@link OfflineActivity}.
     */
    private void goToOfflineActivity() {
        Intent intent = new Intent(this, OfflineActivity.class);
        startActivity(intent);
    }

    /**
     * For going from this activity, {@link OnlineActivity}, to {@link SettingsActivity}.
     */
    private void goToSettingsActivity() {
        Intent intent = new Intent(this, SettingsActivity.class);
        intent.putExtra(Constants.CONNECTION_FLAG, Constants.CONNECTION_ONLINE);
        startActivity(intent);
    }

    /**
     * For going from this activity, {@link OnlineActivity}, to {@link HelpActivity}.
     */
    private void goToHelpActivity() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    /**
     * For going from this activity, {@link OnlineActivity}, to {@link AboutActivity}.
     */
    private void goToAboutActivity() {
        Intent intent = new Intent(this, AboutActivity.class);
        startActivity(intent);
    }

    /**
     * This methods logs out user and clears any login info if saved.
     * Then it calls {@link OnlineActivity#goToLogin()}.
     */
    private void logoutUser() {
        // Clear login info
        SharedPreferences preferences = getSharedPreferences(Constants.PREFERENCE_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean(Constants.REMEMBER_ME, false);
        editor.remove(Constants.PREFERENCE_EMAIL);
        editor.remove(Constants.PREFERENCE_PASSWORD);
        editor.apply();
        mAuth.signOut();

        Log.i(TAG, "logoutUser:success");
        goToLogin();
    }

    /**
     * For going from this activity, {@link OnlineActivity}, to {@link LoginActivity}.
     */
    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

}