package app.jam.jam.online;

import android.content.Intent;
import android.os.Bundle;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.Contact;
import app.jam.jam.profile.ViewProfileActivity;
import de.hdodenhof.circleimageview.CircleImageView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ExploreFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ExploreFragment extends Fragment {

    private DatabaseReference mUsersReference;

    private RecyclerView mExploreRecyclerView;

    public ExploreFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ExploreFragment.
     */
    public static ExploreFragment newInstance() {
        return new ExploreFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View mExploreView = inflater.inflate(R.layout.fragment_explore, container, false);

        mUsersReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_USERS);

        mExploreRecyclerView = mExploreView.findViewById(R.id.explore_recyclerView);
        mExploreRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        return mExploreView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerOptions<Contact> options =
                new FirebaseRecyclerOptions.Builder<Contact>()
                        .setQuery(mUsersReference, Contact.class)
                        .build();

        // Creating adapter
        FirebaseRecyclerAdapter<Contact, ExploreViewHolder> adapter =
                new FirebaseRecyclerAdapter<Contact, ExploreViewHolder>(options) {
                    @Override
                    protected void onBindViewHolder(@NonNull ExploreViewHolder holder, final int position, @NonNull Contact model) {
                        holder.userName.setText(model.getUserName());
                        holder.userAbout.setText(model.getAbout());
                        Picasso.get().load(model.getImageUri()).placeholder(R.drawable.profile_image).into(holder.userPicture);

                        // Setting listener to show profile and send request
                        holder.itemView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                String visitUserId = getRef(position).getKey();
                                goToProfileView(visitUserId);
                            }
                        });
                    }

                    @NonNull
                    @Override
                    public ExploreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
                        View view = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.list_user_layout, parent, false);
                        ExploreViewHolder viewHolder;
                        viewHolder = new ExploreViewHolder(view);
                        return viewHolder;
                    }
                };

        mExploreRecyclerView.setAdapter(adapter);
        adapter.startListening();
    }

    /**
     * This method starts {@link ViewProfileActivity} to show user information of the passed user id.
     *
     * @param visitUserId the id of visited user
     */
    private void goToProfileView(String visitUserId) {
        Intent profileIntent = new Intent(getContext(), ViewProfileActivity.class);
        profileIntent.putExtra(Constants.RECEIVER_USER_ID, visitUserId);
        startActivity(profileIntent);
    }

    /**
     * The view holder class that extends {@link RecyclerView.ViewHolder}
     * for {@link ExploreFragment} adapter.
     */
    public static class ExploreViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView userName, userAbout;
        CircleImageView userPicture;

        public ExploreViewHolder(@NonNull View itemView) {
            super(itemView);

            userName = itemView.findViewById(R.id.user_name);
            userAbout = itemView.findViewById(R.id.user_about);
            userPicture = itemView.findViewById(R.id.user_picture);
        }
    }

}