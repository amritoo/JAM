package app.jam.jam.online;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
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
import app.jam.jam.data.Contact;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ContactsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ContactsFragment extends Fragment {

    private String TAG = "Contacts";

    private View mContactsView;
    private RecyclerView mContactsRecyclerView;

    private DatabaseReference mContactsReference, mUsersReference;
    private FirebaseAuth mAuth;
    private String currentUserID;

    public ContactsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ContactsFragment.
     */
    public static ContactsFragment newInstance() {
        return new ContactsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mContactsView = inflater.inflate(R.layout.fragment_contacts, container, false);

        mContactsRecyclerView = mContactsView.findViewById(R.id.contacts_recyclerView);
        mContactsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            currentUserID = mAuth.getCurrentUser().getUid();
        }

        mContactsReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_CONTACTS).child(currentUserID);
        mUsersReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_USER);

        return mContactsView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(mContactsReference, Contact.class)
                        .build();

        FirebaseRecyclerAdapter<Contact, ContactsViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, ContactsViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull final ContactsViewHolder holder, int position, @NonNull final Contact model) {
                        final String userId = getRef(position).getKey();
                        mUsersReference.child(userId).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists()) {
                                    // TODO active status
                                    String userName = snapshot.child(Constants.CHILD_USERNAME).getValue().toString();
                                    String userAbout = getString(R.string.placeholder_text_not_given);
                                    if (snapshot.hasChild(Constants.CHILD_USER_ABOUT)) {
                                        userAbout = snapshot.child(Constants.CHILD_USER_ABOUT).getValue().toString();
                                    }
                                    if (snapshot.hasChild(Constants.CHILD_USER_IMAGE)) {
                                        String userImage = snapshot.child(Constants.CHILD_USER_IMAGE).getValue().toString();
                                        Picasso.get().load(userImage).placeholder(R.drawable.profile_image).into(holder.userPicture);
                                    }
                                    holder.userName.setText(userName);
                                    holder.userAbout.setText(userAbout);

                                    holder.itemView.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            startChatActivity(userId);
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                Log.e(TAG, "addValueEventListener:onCancelled", error.toException());
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ContactsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_user_layout, parent, false);
                        ContactsViewHolder viewHolder = new ContactsViewHolder(view);
                        return viewHolder;
                    }
                };

        mContactsRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    private void startChatActivity(String visitUserId) {
        Intent chatIntent = new Intent(getContext(), OnlineChatActivity.class);
        chatIntent.putExtra(Constants.RECEIVER_USER_ID, visitUserId);
        startActivity(chatIntent);
    }

    public static class ContactsViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView userName, userAbout;
        CircleImageView userPicture;
//        ImageView statusIcon;

        public ContactsViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userAbout = itemView.findViewById(R.id.user_about);
            userPicture = itemView.findViewById(R.id.user_picture);
//            statusIcon = itemView.findViewById(R.id.user_status);
        }
    }

}