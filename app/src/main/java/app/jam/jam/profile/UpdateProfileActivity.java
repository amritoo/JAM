package app.jam.jam.profile;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.yalantis.ucrop.UCrop;

import java.io.File;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class UpdateProfileActivity extends AppCompatActivity {

    private static final String TAG = "Update Profile";

    private FirebaseUser mCurrentUser;
    private DatabaseReference mUserReference;
    private StorageReference mStorageRef;

    private MaterialToolbar mToolbar;
    private CircleImageView mProfileImageView;
    private TextInputLayout mUsernameTextInputLayout, mAboutTextInputLayout, mWorkTextInputLayout, mAddressTextInputLayout, mBirthDateTextInputLayout;
    private MaterialButton mUpdateProfileButton, mUploadPictureButton;

    private User mUser;
    private Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();

        initializeViews();
        setListeners();

        mUser = new User();

        String currentUserId = mCurrentUser.getUid();

        // Setting storage reference to store profile picture
        mStorageRef = FirebaseStorage.getInstance()
                .getReference()
                .child(Constants.ROOT_PROFILE_IMAGE)
                .child(currentUserId);

        // Setting database reference to retrieve and update user data
        mUserReference = FirebaseDatabase.getInstance()
                .getReference()
                .child(Constants.ROOT_USER)
                .child(currentUserId);
        mUserReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null)
                    setData(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.w(TAG, "loadUser:onCancelled", error.toException());
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.SELECT_PICTURE_CODE && resultCode == RESULT_OK && data != null) {
            mImageUri = data.getData();
            mProfileImageView.setImageURI(mImageUri);

            // TODO Crop image
//            Uri sourceUri = data.getData();
//            File file = getImageFile();
//            Uri destinationUri = Uri.fromFile(file);
//            openCropActivity(sourceUri, destinationUri);
        }
//        else if (requestCode == UCrop.REQUEST_CROP && resultCode == RESULT_OK && data != null) {
//            mImageUri = UCrop.getOutput(data);  // path to the image selected by the user
//            mProfileImageView.setImageURI(mImageUri);
//        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private File getImageFile() {
        // TODO
        String path = Environment.getDataDirectory().toString();
        File file = new File(path + "/" + getString(R.string.app_package));
        file.mkdir();
//        return null;

        String imageFileName = "JPEG_" + System.currentTimeMillis() + "_";
        File storageDir = new File(
                Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_DCIM
                ), "Camera"
        );
        System.out.println(storageDir.getAbsolutePath());
        if (storageDir.exists())
            System.out.println("File exists");
        else
            System.out.println("File not exists");
//        File file = File.createTempFile(imageFileName, ".jpg", storageDir);
//        String currentPhotoPath = "file:" + file.getAbsolutePath();
        return file;
    }

    private void openCropActivity(Uri sourceUri, Uri destinationUri) {
        UCrop.Options options = new UCrop.Options();
        options.setCircleDimmedLayer(true);
        options.setCropFrameColor(ContextCompat.getColor(this, R.color.colorAccent));
        UCrop.of(sourceUri, destinationUri)
                .withAspectRatio(1, 1)
                .withMaxResultSize(R.integer.profile_picture_max_size, R.integer.profile_picture_max_size)
                .start(this);
    }

    /**
     * This method uploads a picture to {@link FirebaseStorage} and set downloadable uri to user object
     *
     * @param uri the local file path to the image
     */
    private void uploadImageAndData(Uri uri) {
        mStorageRef.putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        Toast.makeText(UpdateProfileActivity.this, R.string.toast_upload_image_success, Toast.LENGTH_SHORT).show();
                        // Get a URL to the uploaded content
                        mStorageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                mUser.setImageUri(uri.toString());
                                // updating user info
                                mUserReference.setValue(mUser)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.i(TAG, "Update successful");
                                                Toast.makeText(UpdateProfileActivity.this, R.string.toast_update_profile_success, Toast.LENGTH_SHORT).show();
                                                finishThisActivity();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w(TAG, "update:failure", e);
                                                Toast.makeText(UpdateProfileActivity.this, R.string.toast_update_profile_failed, Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        Log.w(TAG, "uploadImage:failure", exception);
                        Toast.makeText(UpdateProfileActivity.this, R.string.toast_upload_image_failed, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    /**
     * This method updates user data after updating profile picture (if changed)
     */
    private void update() {
        // TODO show progress dialog
        if (mImageUri != null) {
            uploadImageAndData(mImageUri);
        } else {
            mUserReference.setValue(mUser)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.i(TAG, "Update successful");
                            Toast.makeText(UpdateProfileActivity.this, R.string.toast_update_profile_success, Toast.LENGTH_SHORT).show();
                            finishThisActivity();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w(TAG, "Update:failure", e);
                            Toast.makeText(UpdateProfileActivity.this, R.string.toast_update_profile_failed, Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    private void setData(User user) {
        if (user.getUserName() != null)
            mUsernameTextInputLayout.getEditText().setText(user.getUserName());
        if (user.getAbout() != null)
            mAboutTextInputLayout.getEditText().setText(user.getAbout());
        if (user.getWork() != null)
            mWorkTextInputLayout.getEditText().setText(user.getWork());
        if (user.getAddress() != null)
            mAddressTextInputLayout.getEditText().setText(user.getAddress());
        if (user.getBirthDate() != null)
            mBirthDateTextInputLayout.getEditText().setText(user.getBirthDate());

        //TODO set picture
        mProfileImageView.setImageDrawable(getDrawable(R.drawable.ic_user_108));
    }

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

    private void setListeners() {
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });

        mUpdateProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = mUsernameTextInputLayout.getEditText().getText().toString();
                String about = mAboutTextInputLayout.getEditText().getText().toString();
                String work = mWorkTextInputLayout.getEditText().getText().toString();
                String address = mAddressTextInputLayout.getEditText().getText().toString();
                String birthDate = mBirthDateTextInputLayout.getEditText().getText().toString();

                mUser.setUserName(username);
                mUser.setAbout(about);
                mUser.setWork(work);
                mUser.setAddress(address);
                mUser.setBirthDate(birthDate);

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

    private void selectPicture() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(galleryIntent, Constants.SELECT_PICTURE_CODE);
    }

    private void finishThisActivity() {
        finish();
    }

}