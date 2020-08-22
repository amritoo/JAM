package app.jam.jam.online;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.Message;
import app.jam.jam.profile.ViewProfileActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class OnlineChatActivity extends AppCompatActivity {

    private String TAG = "Online Chat";

    // Layout Views
    private MaterialToolbar mToolbar;
    private CircleImageView mUserImage;
    private MaterialTextView mTitleTextView;
    private RecyclerView mConversationView;
    private EditText mOutEditText;
    private FloatingActionButton mSendButton, mSendImageButton;

    private DatabaseReference mMessagesReference;
    private String mReceiverID, mReceiverName, mReceiverImage, mCurrentUserId;

    private final List<Message> messageList = new ArrayList<>();
    private MessageAdapter mMessageAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_online_chat);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finishThisActivity();
        }
        mCurrentUserId = currentUser.getUid();
        mMessagesReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_MESSAGES);

        if (getIntent().getExtras() != null) {
            mReceiverID = getIntent().getExtras().get(Constants.RECEIVER_USER_ID).toString();
            mReceiverName = getIntent().getExtras().get(Constants.RECEIVER_USER_NAME).toString();
            mReceiverImage = getIntent().getExtras().get(Constants.RECEIVER_USER_IMAGE).toString();
        }

        initializeViews();
        setListeners();

        mTitleTextView.setText(mReceiverName);
        if (!mReceiverImage.equals(Constants.RECEIVER_USER_IMAGE)) {
            Picasso.get().load(mReceiverImage).placeholder(R.drawable.profile_image).into(mUserImage);
        }

        mMessageAdapter = new MessageAdapter(messageList);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mConversationView.setLayoutManager(linearLayoutManager);
        mConversationView.setAdapter(mMessageAdapter);
    }

    @Override
    protected void onStart() {
        super.onStart();

        mMessagesReference.child(mCurrentUserId).child(mReceiverID)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                        Message message = snapshot.getValue(Message.class);

                        messageList.add(message);
                        mMessageAdapter.notifyDataSetChanged();
                        mConversationView.smoothScrollToPosition(mConversationView.getAdapter().getItemCount());
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "addChildEventListener:onCancelled", error.toException());
                    }
                });
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
                String text = mOutEditText.getText().toString();
                if (text.length() > 0) {
                    sendMessage(text);
                } else {
                    Toast.makeText(OnlineChatActivity.this, "first write your message...", Toast.LENGTH_SHORT).show();
                }
            }
        });

        mSendImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO
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
                    case R.id.menu_chat_profile:
                        goToProfileView();
                        break;
                    case R.id.menu_chat_unFriend:
                        new MaterialAlertDialogBuilder(OnlineChatActivity.this)
                                .setTitle(R.string.title_text_confirmation)
                                .setMessage(R.string.message_remove_contact_confirmation)
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .setPositiveButton(R.string.button_text_ok, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        unFriendContact();
                                    }
                                })
                                .setNeutralButton(R.string.button_text_cancel, null)
                                .show();

                        break;
                    case R.id.menu_chat_help:
                        goToHelpActivity();
                        break;
                    default:
                        return false;
                }
                return true;
            }
        });
    }

    private void sendMessage(String messageText) {
        DatabaseReference userMessageKeyRef =
                mMessagesReference.child(mCurrentUserId).child(mReceiverID)
                        .push();
        final String messageID = userMessageKeyRef.getKey();

        final Message message = new Message(Constants.MESSAGE_TYPE_TEXT);
        message.setFrom(mCurrentUserId);
        message.setTo(mReceiverID);
        message.setBody(messageText);

        if (messageID == null) {
            Log.e(TAG, "messageId:null");
            return;
        }
        mMessagesReference.child(mCurrentUserId).child(mReceiverID).child(messageID)
                .setValue(message)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mMessagesReference.child(mReceiverID).child(mCurrentUserId).child(messageID)
                                .setValue(message)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(OnlineChatActivity.this, "Message sent...", Toast.LENGTH_SHORT).show();
                                        mOutEditText.setText("");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "sendMessage:failureStage2", e);
                                        Toast.makeText(OnlineChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "sendMessage:failureStage1", e);
                        Toast.makeText(OnlineChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void finishThisActivity() {
        finish();
    }

    private void goToProfileView() {
        Intent profileIntent = new Intent(this, ViewProfileActivity.class);
        profileIntent.putExtra(Constants.RECEIVER_USER_ID, mReceiverID);
        startActivity(profileIntent);
    }

    private void goToHelpActivity() {
//        Intent intent = new Intent(this, OfflineActivity.class);
//        startActivity(intent);
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
                                        Toast.makeText(OnlineChatActivity.this, "Contact removed!", Toast.LENGTH_SHORT).show();
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

}