package app.jam.jam.profile;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.States;
import app.jam.jam.data.User;
import de.hdodenhof.circleimageview.CircleImageView;

public class ViewProfileActivity extends AppCompatActivity {

    private static final String TAG = "View Profile";

    private DatabaseReference mUsersReference, mRequestReference, mContactReference;

    private CircleImageView mProfileImageView;
    private MaterialTextView mUsernameTextView, mAboutTextView, mWorkTextView, mAddressTextView;
    private MaterialButton mRequestButton, mCancelButton;

    private String mReceiverUserId, mCurrentUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        // getting references from server
        mUsersReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_USER);
        mRequestReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_REQUESTS);
        mContactReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_CONTACTS);

        // Checking whether user is logged in or not.
        FirebaseUser mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (mCurrentUser != null) {
            mCurrentUserId = mCurrentUser.getUid();
        } else {
            finishThisActivity();
        }
        mReceiverUserId = getIntent().getStringExtra(Constants.RECEIVER_USER_ID);

        // For initializing and setting listener for all views
        initializeViews();
        setListeners();
        retrieveUserData();

        // For managing request and setting buttons text accordingly
        manageRequests();

        if(mCurrentUserId.equals(mReceiverUserId)) {
            setButtonText(States.SELF);
        }
    }

    // Retrieves receiver user data from server and set it to the view
    private void retrieveUserData() {
        mUsersReference.child(mReceiverUserId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                User user = snapshot.getValue(User.class);
                if (user != null)
                    setData(user);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e(TAG, "retrieveUserData:onCancelled", error.toException());
            }
        });
    }

    /**
     * This method reads data from user object and set it to corresponding views.
     *
     * @param user the user object that represents the user information to display
     */
    private void setData(User user) {
        if (user.getUserName() != null)
            mUsernameTextView.setText(user.getUserName());
        if (user.getAbout() != null)
            mAboutTextView.setText(user.getAbout());
        if (user.getWork() != null)
            mWorkTextView.setText(user.getWork());
        if (user.getAddress() != null)
            mAddressTextView.setText(user.getAddress());
        if (user.getImageUri() != null)
            Picasso.get().load(user.getImageUri()).placeholder(R.drawable.profile_image).into(mProfileImageView);
    }

    // For initializing views from layout
    private void initializeViews() {
        mProfileImageView = findViewById(R.id.view_profile_circleImage);
        mUsernameTextView = findViewById(R.id.view_profile_username_textView);
        mAboutTextView = findViewById(R.id.view_profile_about_textView);
        mWorkTextView = findViewById(R.id.view_profile_work_textView);
        mAddressTextView = findViewById(R.id.view_profile_address_textView);

        mRequestButton = findViewById(R.id.view_profile_request_button);
        mCancelButton = findViewById(R.id.view_profile_cancel_button);
    }

    // For setting listeners to views
    private void setListeners() {
        mRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence text = mRequestButton.getText();
                if (text.equals(getString(R.string.button_text_send_request))) {
                    sendFriendRequest();
                } else if (text.equals(getString(R.string.button_text_accept_request))) {
                    acceptFriendRequest();
                }
            }
        });

        mCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CharSequence text = mCancelButton.getText();
                if (text.equals(getString(R.string.button_text_cancel_request)) ||
                        text.equals(getString(R.string.button_text_delete_request))) {
                    deleteFriendRequest();
                } else if (text.equals(getString(R.string.button_text_unfriend))) {
                    unFriendContact();
                }
            }
        });
    }

    /**
     * Called to finish this activity and return to the caller activity.
     */
    private void finishThisActivity() {
        finish();
    }

    /**
     * This method sends new friend request. After that, it makes cancel request button visible
     * and disables send friend request button.
     */
    private void sendFriendRequest() {
        mRequestReference.child(mCurrentUserId).child(mReceiverUserId)
                .child(Constants.CHILD_REQUEST_TYPE).setValue(Constants.CHILD_REQUEST_TYPE_SENT)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mRequestReference.child(mReceiverUserId).child(mCurrentUserId)
                                .child(Constants.CHILD_REQUEST_TYPE).setValue(Constants.CHILD_REQUEST_TYPE_RECEIVED)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ViewProfileActivity.this, "Successful", Toast.LENGTH_SHORT).show();
                                        setButtonText(States.REQUEST_SENT);
                                        // TODO: add notification
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "sendFriendRequest:addStage2", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "sendFriendRequest:addStage1", e);
                    }
                });
    }

    /**
     * This method accepts new friend request. After that, it makes un-friend button visible
     * and hides send accept friend request button.
     */
    private void acceptFriendRequest() {
        mContactReference.child(mCurrentUserId).child(mReceiverUserId)
                .child(Constants.CHILD_CONTACT).setValue(Constants.CHILD_CONTACT_SAVED)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mContactReference.child(mReceiverUserId).child(mCurrentUserId)
                                .child(Constants.CHILD_CONTACT).setValue(Constants.CHILD_CONTACT_SAVED)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        removeRequest(States.FRIEND);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "acceptFriendRequest:addContactStage2", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "acceptFriendRequest:addContactStage1", e);
                    }
                });
    }

    /**
     * This method removes sent friend request. After that, it makes send friend request button visible
     * and hides cancel request button.
     */
    private void deleteFriendRequest() {
        removeRequest(States.NEW);
        Toast.makeText(ViewProfileActivity.this, "Canceled request!", Toast.LENGTH_SHORT).show();
    }

    /**
     * This method removes {@code currentUserId} and {@code receiverUserId} from
     * {@code requestReference}.
     *
     * @param state the state to {@link ViewProfileActivity#setButtonText(States)} after success
     */
    private void removeRequest(final States state) {
        mRequestReference.child(mCurrentUserId).child(mReceiverUserId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mRequestReference.child(mReceiverUserId).child(mCurrentUserId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        setButtonText(state);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "removeFriendRequest:removeStage2", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "removeFriendRequest:removeStage1", e);
                    }
                });
    }

    /**
     * This method removes a contact from contact list. After that, it makes send friend request button visible
     * and hides un-friend button button.
     */
    private void unFriendContact() {
        mContactReference.child(mCurrentUserId).child(mReceiverUserId)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mContactReference.child(mReceiverUserId).child(mCurrentUserId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(ViewProfileActivity.this, "Contact removed!", Toast.LENGTH_SHORT).show();
                                        setButtonText(States.NEW);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "unFriendContact:removeStage2", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "unFriendContact:removeStage1", e);
                    }
                });
    }

    /**
     * This method checks relationship between {@code currentUser} and {@code receiverUser} and
     * sets button text and visibility accordingly
     */
    private void manageRequests() {
        mRequestReference.child(mCurrentUserId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.hasChild(mReceiverUserId)) {
                            String requestType = snapshot.child(mReceiverUserId).child(Constants.CHILD_REQUEST_TYPE).getValue().toString();
                            if (requestType.equals(Constants.CHILD_REQUEST_TYPE_SENT)) {
                                setButtonText(States.REQUEST_SENT);
                            } else if (requestType.equals(Constants.CHILD_REQUEST_TYPE_RECEIVED)) {
                                setButtonText(States.REQUEST_RECEIVED);
                            } else {
                                setButtonText(States.NEW);
                            }
                        } else {
                            mContactReference.child(mCurrentUserId)
                                    .addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if (snapshot.hasChild(mReceiverUserId)) {
                                                setButtonText(States.FRIEND);
                                            } else {
                                                setButtonText(States.NEW);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {
                                            Log.w(TAG, "manageRequests:onCancelled", error.toException());
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.w(TAG, "manageRequests:onCancelled", error.toException());
                    }
                });
    }

    /**
     * This method sets button text, visibility and enabled status according to given state.
     *
     * @param state The state to change buttons to
     */
    private void setButtonText(States state) {
        switch (state) {
            case NEW:
                mRequestButton.setText(R.string.button_text_send_request);
                mRequestButton.setVisibility(View.VISIBLE);
                mRequestButton.setEnabled(true);
                mCancelButton.setVisibility(View.GONE);
                break;
            case REQUEST_SENT:
                mRequestButton.setText(R.string.button_text_send_request);
                mRequestButton.setVisibility(View.VISIBLE);
                mRequestButton.setEnabled(false);
                mCancelButton.setText(R.string.button_text_cancel_request);
                mCancelButton.setVisibility(View.VISIBLE);
                break;
            case REQUEST_RECEIVED:
                mRequestButton.setText(R.string.button_text_accept_request);
                mRequestButton.setVisibility(View.VISIBLE);
                mRequestButton.setEnabled(true);
                mCancelButton.setText(R.string.button_text_delete_request);
                mCancelButton.setVisibility(View.VISIBLE);
                break;
            case FRIEND:
                mRequestButton.setVisibility(View.GONE);
                mCancelButton.setText(R.string.button_text_unfriend);
                mCancelButton.setVisibility(View.VISIBLE);
                break;
            case SELF:
                mRequestButton.setVisibility(View.GONE);
                mCancelButton.setVisibility(View.GONE);
                break;
        }
    }
}