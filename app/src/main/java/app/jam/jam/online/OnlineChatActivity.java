package app.jam.jam.online;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.Message;
import app.jam.jam.help.HelpActivity;
import app.jam.jam.methods.Cryptography;
import app.jam.jam.profile.ProfileActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class OnlineChatActivity extends AppCompatActivity {

    /**
     * Tag to use to {@link Log} messages
     */
    private String TAG = "Online Chat";

    // Layout Views
    private MaterialToolbar mToolbar;
    private CircleImageView mUserImage;
    private MaterialTextView mTitleTextView;
    private RecyclerView mConversationView;
    private EditText mOutEditText;
    private FloatingActionButton mSendButton, mSendImageButton;

    private DatabaseReference mMessagesReference;
    private StorageReference mImageStorageReference;
    private String mReceiverID, mReceiverName, mReceiverImage, mCurrentUserId;


    private FirebaseRecyclerAdapter<Message, MessageViewHolder> mFirebaseAdapter;

    private List<Message> messageList;
    private Map<String, Integer> messageIdPositionMap;
    private int count;
    private OnlineChatAdapter mOnlineChatAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_chat);

        if (getIntent().getExtras() != null) {
            mReceiverID = Objects
                    .requireNonNull(getIntent().getExtras().get(Constants.RECEIVER_USER_ID)).toString();
            mReceiverName = Objects
                    .requireNonNull(getIntent().getExtras().get(Constants.RECEIVER_USER_NAME)).toString();
            mReceiverImage = Objects
                    .requireNonNull(getIntent().getExtras().get(Constants.RECEIVER_USER_IMAGE)).toString();
        }

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finishThisActivity();
        }
        assert currentUser != null;
        mCurrentUserId = currentUser.getUid();
        mMessagesReference = FirebaseDatabase.getInstance().getReference()
                .child(Constants.ROOT_MESSAGES);
        mImageStorageReference = FirebaseStorage.getInstance().getReference()
                .child(Constants.ROOT_IMAGES).child(mCurrentUserId).child(mReceiverID);

        // For setting views from layout and listeners to the views
        initializeViews();
        setListeners();

        messageList = new ArrayList<>();
        messageIdPositionMap = new HashMap<>();
        count = 0;

        mTitleTextView.setText(mReceiverName);
        if (!mReceiverImage.equals(Constants.RECEIVER_USER_IMAGE)) {
            Picasso.get().load(mReceiverImage).placeholder(R.drawable.profile_image).into(mUserImage);
        }

        mOnlineChatAdapter = new OnlineChatAdapter(messageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mConversationView.setLayoutManager(linearLayoutManager);
        mConversationView.setAdapter(mOnlineChatAdapter);


    }

    @Override
    protected void onStart() {
        super.onStart();

        mMessagesReference.child(mCurrentUserId).child(mReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Message message = snapshot.getValue(Message.class);
                        assert message != null;
                        if (!messageIdPositionMap.containsKey(message.getMessageId())) {
                            messageIdPositionMap.put(message.getMessageId(), count);
                            count++;
                            messageList.add(message);
                            mOnlineChatAdapter.notifyDataSetChanged();
                            mConversationView.smoothScrollToPosition(
                                    Objects.requireNonNull(mConversationView.getAdapter())
                                            .getItemCount());
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        // Not needed
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        // Not needed
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        // Not needed
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "addChildEventListener:onCancelled", error.toException());
                    }
                });
    }

    /**
     * The view holder class that extends {@link RecyclerView.ViewHolder}
     * for {@link OnlineChatAdapter}.
     */
    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView senderMessageTextView, receiverMessageTextView;
        ImageView senderImageView, receiverImageView;
        ImageView senderTextSeen, senderImageSeen, receiverTextSeen, receiverImageSeen;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageTextView = itemView.findViewById(R.id.sender_message_textView);
            senderTextSeen = itemView.findViewById(R.id.sender_message_textView_seen);

            receiverMessageTextView = itemView.findViewById(R.id.receiver_message_textView);
            receiverTextSeen = itemView.findViewById(R.id.receiver_message_textView_seen);

            senderImageView = itemView.findViewById(R.id.sender_message_imageView);
            senderImageSeen = itemView.findViewById(R.id.sender_message_imageView_seen);

            receiverImageView = itemView.findViewById(R.id.receiver_message_imageView);
            receiverImageSeen = itemView.findViewById(R.id.receiver_message_imageView_seen);
        }

    }

    private void initializeViews() {
        mToolbar = findViewById(R.id.online_chat_topAppBar);
        mUserImage = findViewById(R.id.online_chat_imageView);
        mTitleTextView = findViewById(R.id.online_chat_title);

        mConversationView = findViewById(R.id.online_chat_messages);
        mOutEditText = findViewById(R.id.online_chat_editText);
        mSendButton = findViewById(R.id.online_chat_send_floatingButton);
        mSendImageButton = findViewById(R.id.online_chat_image_floatingButton);
    }

    private void setListeners() {
        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendTextMessage();
            }
        });

        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendImageMessage();
            }
        });

        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });

        mToolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.online_chat_profile:
                        goToProfileView();
                        break;
                    case R.id.online_chat_unFriend:
                        new MaterialAlertDialogBuilder(OnlineChatActivity.this)
                                .setTitle(R.string.title_text_confirmation)
                                .setMessage(R.string.message_remove_contact_confirmation)
                                .setIcon(R.drawable.ic_warning_24)
                                .setPositiveButton(R.string.button_text_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        unFriendContact();
                                    }
                                })
                                .setNeutralButton(R.string.button_text_cancel, null)
                                .show();

                        break;
                    case R.id.online_chat_help:
                        goToHelpActivity();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    /**
     * This method sends a text message to {@link FirebaseDatabase}.
     */
    private void sendTextMessage() {
        String text = mOutEditText.getText().toString();
        if (text.length() <= 0) {
            Toast.makeText(this, R.string.toast_chat_message_empty, Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference userMessageKeyRef =
                mMessagesReference.child(mCurrentUserId).child(mReceiverID)
                        .push();
        final String messageId = userMessageKeyRef.getKey();
        if (messageId == null) {
            Log.e(TAG, "sendTextMessage:messageId:null");
            return;
        }

        final Message message = new Message(Constants.MESSAGE_TYPE_TEXT);
        message.setFrom(mCurrentUserId);
        message.setTo(mReceiverID);
        message.setBody(text);
        message.setMessageId(messageId);

        sendMessageToDatabase(messageId, message, true);
    }

    /**
     * This method sends message to {@link FirebaseDatabase}.
     *
     * @param messageId the database id of this message
     * @param message   the message object
     */
    private void sendMessageToDatabase(final String messageId, final Message message, final boolean isText) {
        String encryptedText = Cryptography.encrypt(message.getBody());
        message.setBody(encryptedText);

        mMessagesReference.child(mCurrentUserId).child(mReceiverID).child(messageId)
                .setValue(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mMessagesReference.child(mReceiverID).child(mCurrentUserId).child(messageId)
                                .setValue(message)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        mOutEditText.setText("");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "sendMessage:failureStage2", e);
                                        Snackbar.make(mConversationView, R.string.snackbar_message_send_failed, Snackbar.LENGTH_LONG)
                                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        if (isText)
                                                            sendTextMessage();
                                                        else
                                                            sendImageMessage();
                                                    }
                                                })
                                                .show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "sendMessage:failureStage1", e);
                        Snackbar.make(mConversationView, R.string.snackbar_message_send_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        if (isText)
                                            sendTextMessage();
                                        else
                                            sendImageMessage();
                                    }
                                })
                                .show();
                    }
                });
    }

    /**
     * This method sends an image message to {@link FirebaseDatabase}.
     */
    private void sendImageMessage() {
        CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setActivityTitle(getString(R.string.title_text_select_picture))
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
                Uri imageUri = result.getUri();
                uploadAndSendImageToDatabase(imageUri);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Log.e(TAG, "CropImage:failure", result.getError());
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * This method uploads image to {@link FirebaseStorage}
     * and then calls {@link OnlineChatActivity#sendMessageToDatabase(String, Message, boolean)} using the
     * image uri.
     *
     * @param uri the uri of image file
     */
    private void uploadAndSendImageToDatabase(Uri uri) {
        final String messageName = new Date().toString();
        mImageStorageReference.child(messageName).putFile(uri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        mImageStorageReference.getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        DatabaseReference userMessageKeyRef =
                                                mMessagesReference.child(mCurrentUserId).child(mReceiverID)
                                                        .push();
                                        final String messageId = userMessageKeyRef.getKey();
                                        if (messageId == null) {
                                            Log.e(TAG, "uploadAndSendImageToDatabase:messageId:null");
                                            return;
                                        }

                                        // sending image message
                                        Message message = new Message(Constants.MESSAGE_TYPE_IMAGE);
                                        message.setFrom(mCurrentUserId);
                                        message.setTo(mReceiverID);
                                        message.setBody(uri.toString());
                                        message.setMessageId(messageId);
                                        message.setName(messageName);

                                        sendMessageToDatabase(messageId, message, false);
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "uploadAndSendImageToDatabase:getDownloadUrl:failure", e);
                                        Snackbar.make(mConversationView, R.string.snackbar_upload_image_failed, Snackbar.LENGTH_LONG)
                                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        sendImageMessage();
                                                    }
                                                })
                                                .show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "uploadImage:failure", e);
                        Snackbar.make(mConversationView, R.string.snackbar_upload_image_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        sendImageMessage();
                                    }
                                })
                                .show();
                    }
                });
    }

    /**
     * For going to {@link ProfileActivity} to vew contact's profile.
     */
    private void goToProfileView() {
        Intent profileIntent = new Intent(this, ProfileActivity.class);
        profileIntent.putExtra(Constants.RECEIVER_USER_ID, mReceiverID);
        startActivity(profileIntent);
    }

    /**
     * This method removes a contact from contact list. After that, it makes send friend request button visible
     * and hides un-friend button button.
     */
    private void unFriendContact() {
        final DatabaseReference contactsReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_CONTACTS);
        contactsReference.child(mCurrentUserId).child(mReceiverID)
                .removeValue()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        contactsReference.child(mReceiverID).child(mCurrentUserId)
                                .removeValue()
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(OnlineChatActivity.this, R.string.toast_unfriend_success, Toast.LENGTH_SHORT).show();
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "unFriendContact:removeStage2", e);
                                        Snackbar.make(mConversationView, R.string.snackbar_unfriend_failed, Snackbar.LENGTH_LONG)
                                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View v) {
                                                        unFriendContact();
                                                    }
                                                })
                                                .show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "unFriendContact:removeStage1", e);
                        Snackbar.make(mConversationView, R.string.snackbar_unfriend_failed, Snackbar.LENGTH_LONG)
                                .setAction(R.string.button_text_retry, new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        unFriendContact();
                                    }
                                })
                                .show();
                    }
                });
    }

    /**
     * For going to {@link HelpActivity}.
     */
    private void goToHelpActivity() {
        Intent intent = new Intent(this, HelpActivity.class);
        startActivity(intent);
    }

    /**
     * For finishing this activity and return to parent activity.
     */
    private void finishThisActivity() {
        finish();
    }

}