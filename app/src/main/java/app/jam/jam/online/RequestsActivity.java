package app.jam.jam.online;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.Objects;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.Contact;
import app.jam.jam.profile.ViewProfileActivity;
import de.hdodenhof.circleimageview.CircleImageView;

public class RequestsActivity extends AppCompatActivity {

    /**
     * Tag to use to {@link Log} messages
     */
    private String TAG = "Friend Requests";

    private DatabaseReference mRequestsReference, mUsersReference;

    private RecyclerView mRequestsRecyclerView;
    private String currentUserID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_requests);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            finishThisActivity();
        }
        assert currentUser != null;
        currentUserID = currentUser.getUid();

        mUsersReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_USERS);
        mRequestsReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_REQUESTS);

        // For initializing and setting listener to views from layout
        MaterialToolbar mToolbar = findViewById(R.id.requests_toolbar);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finishThisActivity();
            }
        });
        mRequestsRecyclerView = findViewById(R.id.requests_recyclerView);
        mRequestsRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        setAdapterToRecyclerView();
    }

    /**
     * This method creates and adds adapter to the recycler view
     */
    private void setAdapterToRecyclerView() {
        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(mRequestsReference.child(currentUserID), Contact.class)
                        .build();

        FirebaseRecyclerAdapter<Contact, RequestsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, RequestsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final RequestsViewHolder holder, int position, @NonNull Contact model) {
                        final String userId = getRef(position).getKey();

                        getRef(position).child(Constants.CHILD_REQUEST_TYPE)
                                .addValueEventListener(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if (snapshot.exists()) {
                                            String type = Objects.requireNonNull(snapshot.getValue()).toString();

                                            if (type.equals(Constants.CHILD_REQUEST_TYPE_RECEIVED)) {
                                                setHolderData(holder, userId, true);
                                            } else if (type.equals(Constants.CHILD_REQUEST_TYPE_SENT)) {
                                                setHolderData(holder, userId, false);
                                            }
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        Log.e(TAG, "setAdapterToRecyclerView:addValueEventListener:onCancelled", error.toException());
                                    }
                                });

                        // Setting listener to show profile and send request
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                goToProfileView(userId);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public RequestsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_user_layout, parent, false);
                        RequestsViewHolder holder;
                        holder = new RequestsViewHolder(view);
                        return holder;
                    }
                };

        mRequestsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    /**
     * This method gets data from {@link FirebaseDatabase} and sets them to corresponding views.
     *
     * @param holder     the holder object to set data
     * @param userId     id of the user to display
     * @param isReceived if true, then shows receive message in about, otherwise shows sent message
     */
    private void setHolderData(final RequestsViewHolder holder, final String userId, final boolean isReceived) {
        mUsersReference.child(userId)
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.hasChild(Constants.CHILD_USER_IMAGE)) {
                            final String profileImageUri = Objects
                                    .requireNonNull(dataSnapshot.child(Constants.CHILD_USER_IMAGE)
                                            .getValue()).toString();
                            Picasso.get().load(profileImageUri).placeholder(R.drawable.profile_image).into(holder.profileImage);
                        }

                        final String userName = Objects
                                .requireNonNull(dataSnapshot.child(Constants.CHILD_USERNAME).getValue()).toString();

                        holder.userName.setText(userName);
                        if (isReceived)
                            holder.userAbout.setText(R.string.friend_request_received_text);
                        else
                            holder.userAbout.setText(getString(R.string.friend_request_sent_text, userName));
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e(TAG, "setHolderData:onCancelled", error.toException());
                    }
                });
    }

    /**
     * This method starts {@link ViewProfileActivity} to show user information of the passed user id.
     *
     * @param visitUserId the id of visited user
     */
    private void goToProfileView(String visitUserId) {
        Intent profileIntent = new Intent(this, ViewProfileActivity.class);
        profileIntent.putExtra(Constants.RECEIVER_USER_ID, visitUserId);
        startActivity(profileIntent);
    }

    /**
     * Finishes this activity and return to parent activity
     */
    private void finishThisActivity() {
        finish();
    }

    /**
     * The view holder class that extends {@link RecyclerView.ViewHolder}
     * for {@link FirebaseRecyclerAdapter} adapter.
     */
    public static class RequestsViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView userName, userAbout;
        CircleImageView profileImage;

        public RequestsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userAbout = itemView.findViewById(R.id.user_about);
            profileImage = itemView.findViewById(R.id.user_picture);
        }
    }

}