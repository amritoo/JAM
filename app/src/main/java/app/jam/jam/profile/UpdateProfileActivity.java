package app.jam.jam.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.Objects;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.EditTextDatePicker;
import app.jam.jam.data.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {

    /**
     * Tag to use to {@link Log} messages
     */
    private static final String TAG = "Update Profile";

    private FirebaseUser mCurrentUser;
    private DatabaseReference mCurrentUserReference;
    private StorageReference mProfileImageStorageReference;

    private MaterialToolbar mToolbar;
    private CircleImageView mProfileImageView;
    private TextInputLayout mUsernameTextInputLayout, mAboutTextInputLayout, mWorkTextInputLayout, mAddressTextInputLayout, mBirthDateTextInputLayout;
    private MaterialButton mUpdateProfileButton, mUploadPictureButton;

    private User mOldUser;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        // For initializing and setting listeners to all views
        initializeViews();
        setListeners();

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser == null)
            finishThisActivity();
        String currentUserId = mCurrentUser.getUid();

        // Setting storage reference to store profile picture
        mProfileImageStorageReference = FirebaseStorage.getInstance()
                .getReference()
                .child(Constants.ROOT_PROFILE_IMAGES)
                .child(currentUserId);

        // Setting database reference to retrieve and update user data
        mCurrentUserReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.ROOT_USERS).child(currentUserId);
        mCurrentUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    mOldUser = snapshot.getValue(User.class);
                    assert mOldUser != null;
                    setData(mOldUser);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "onCreate:addListenerForSingleValueEvent:onCancelled", error.toException());
            }
        });
    }

    // For initializing views from layout
    private void initializeViews() {
        mToolbar = findViewById(R.id.update_profile_toolbar);

        mProfileImageView = findViewById(R.id.update_profile_circleImage);
        mUsernameTextInputLayout = findViewById(R.id.update_profile_username_textInputLayout);
        mAboutTextInputLayout = findViewById(R.id.update_profile_about_textInputLayout);
        mWorkTextInputLayout = findViewById(R.id.update_profile_work_textInputLayout);
        mAddressTextInputLayout = findViewById(R.id.update_profile_address_textInputLayout);
        mBirthDateTextInputLayout = findViewById(R.id.update_profile_birth_date_textInputLayout);

        mUpdateProfileButton = findViewById(R.id.update_profile_update_button);
        mUploadPictureButton = findViewById(R.id.update_picture_button);
    }

    // Listeners for all views
    private void setListeners() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });

        new EditTextDatePicker(this, mBirthDateTextInputLayout.getEditText());

        mUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                update();
            }
        });

        mUploadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectPicture();
            }
        });
    }

    /**
     * This method reads user input and updates profile data accordingly
     */
    private void update() {
        String username = Objects
                .requireNonNull(mUsernameTextInputLayout.getEditText())
                .getText().toString();
        String about = Objects
                .requireNonNull(mAboutTextInputLayout.getEditText())
                .getText().toString();
        String work = Objects
                .requireNonNull(mWorkTextInputLayout.getEditText())
                .getText().toString();
        String address = Objects
                .requireNonNull(mAddressTextInputLayout.getEditText())
                .getText().toString();
        String birthDate = Objects
                .requireNonNull(mBirthDateTextInputLayout.getEditText())
                .getText().toString();

        User user = new User();
        user.setUserName(username);
        user.setAbout(about);
        user.setWork(work);
        user.setAddress(address);
        user.setBirthDate(birthDate);

        if (mImageUri != null) {
            uploadImageAndData(mImageUri, user);
        } else {
            user.setImageUri(mOldUser.getImageUri());
            updateData(user);
        }
    }

    /**
     * This method uploads a picture to {@link FirebaseStorage} and set downloadable uri to user object.
     * After that it calls {@link UpdateProfileActivity#updateData(User)}.
     *
     * @param uri the local file path to the image
     */
    private void uploadImageAndData(Uri uri, final User user) {
        mProfileImageStorageReference.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        mProfileImageStorageReference.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        // updating user info
                                        user.setImageUri(uri.toString());
                                        updateData(user);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "uploadImageAndData:getDownloadUrl:failure", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "uploadImage:failure", e);
                        Snackbar.make(mUsernameTextInputLayout, R.string.snackbar_upload_image_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        update();
                                    }
                                })
                                .show();
                    }
                });
    }

    /**
     * This method updates user data in {@link FirebaseDatabase}.
     */
    private void updateData(User user) {
        updateUsername(mCurrentUser, user.getUserName());
        mCurrentUserReference.setValue(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "updateData:success");
                        Toast.makeText(UpdateProfileActivity.this, R.string.toast_update_profile_success, Toast.LENGTH_SHORT).show();
                        finishThisActivity();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "updateData:failure", e);
                        Snackbar.make(mUsernameTextInputLayout, R.string.snackbar_update_profile_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        update();
                                    }
                                })
                                .show();
                    }
                });

    }

    /**
     * Updates firebase user's username
     *
     * @param user     the user instance of {@link FirebaseUser}
     * @param username the name to set to user
     */
    private void updateUsername(FirebaseUser user, String username) {
        UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileChangeRequest)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "updateUsername:success");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "updateUsername:failure", e);
                    }
                });
    }

    /**
     * This method sets all user information to corresponding views.
     *
     * @param user the object containing user information
     */
    private void setData(User user) {
        if (user.getUserName() != null)
            Objects.requireNonNull(mUsernameTextInputLayout.getEditText())
                    .setText(user.getUserName());
        if (user.getAbout() != null)
            Objects.requireNonNull(mAboutTextInputLayout.getEditText())
                    .setText(user.getAbout());
        if (user.getWork() != null)
            Objects.requireNonNull(mWorkTextInputLayout.getEditText())
                    .setText(user.getWork());
        if (user.getAddress() != null)
            Objects.requireNonNull(mAddressTextInputLayout.getEditText())
                    .setText(user.getAddress());
        if (user.getBirthDate() != null)
            Objects.requireNonNull(mBirthDateTextInputLayout.getEditText())
                    .setText(user.getBirthDate());
        // Setting profile picture
        if (user.getImageUri() != null)
            Picasso.get().load(user.getImageUri()).placeholder(R.drawable.profile_image).into(mProfileImageView);
    }

    /**
     * This method starts a {@link CropImage#activity()} to get selected image from user and crop it.
     */
    private void selectPicture() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(getString(R.string.title_text_select_profile_picture))
                .setCropShape(CropImageView.CropShape.OVAL)
                .setAspectRatio(1, 1)
                .setCropMenuCropButtonTitle(getString(R.string.title_text_done))
                .setRequestedSize(R.integer.upload_picture_size, R.integer.upload_picture_size)
                .setCropMenuCropButtonIcon(R.drawable.ic_check_24)
                .start(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            assert result != null;
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                mProfileImageView.setImageURI(mImageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e(TAG, "CropImage:failure", result.getError());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * For finishing this activity and return to parent activity.
     */
    private void finishThisActivity() {
        finish();
    }

}