package app.jam.jam.online;


import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.List;

import app.jam.jam.R;
import app.jam.jam.data.Constants;
import app.jam.jam.data.Message;
import app.jam.jam.methods.Cryptography;

public class OnlineChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    /**
     * Tag to use to {@link Log} messages
     */
    private static String TAG = "Message Adapter";

    private FirebaseUser mCurrentUser;
    List<Message> userMessageList;

    public static final int MESSAGE_TYPE_IN = 1;
    public static final int MESSAGE_TYPE_OUT = 2;

    public OnlineChatAdapter(List<Message> userMessageList) {
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.userMessageList = userMessageList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == MESSAGE_TYPE_IN) {
            return new MessageInViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message_in, parent, false));
        } else {
            return new MessageOutViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.list_message_out, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull final RecyclerView.ViewHolder holder, int position) {
        String currentUserId = mCurrentUser.getUid();
        Message message = userMessageList.get(position);
        if (message.getFrom().equals(currentUserId)) {
            ((MessageOutViewHolder) holder).bind(message);
        } else {
            ((MessageInViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (userMessageList.get(position).getFrom().equals(mCurrentUser.getUid())) {
            return MESSAGE_TYPE_OUT;
        } else {
            return MESSAGE_TYPE_IN;
        }
    }

    /**
     * This method updates an existing message in {@link FirebaseDatabase}.
     *
     * @param message the message object
     */
    private static void updateSeenToDatabase(final Message message) {
        final DatabaseReference mMessagesReference = FirebaseDatabase.getInstance().getReference().child(Constants.ROOT_MESSAGES);
        mMessagesReference.child(message.getFrom()).child(message.getTo())
                .child(message.getMessageId())
                .child(Constants.CHILD_MESSAGE_SEEN).setValue(Constants.CHILD_MESSAGE_SEEN)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        mMessagesReference.child(message.getTo()).child(message.getFrom())
                                .child(message.getMessageId())
                                .child(Constants.CHILD_MESSAGE_SEEN).setValue(Constants.CHILD_MESSAGE_SEEN)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.i(TAG, "updateSeenToDatabase:success");
                                    }
                                })
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Log.e(TAG, "updateSeenToDatabase:failureStage2", e);
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "updateSeenToDatabase:failureStage1", e);
                    }
                });
    }

    /**
     * The view holder class that extends {@link RecyclerView.ViewHolder}
     * for {@link OnlineChatAdapter}. It's for receiver message.
     */
    public static class MessageInViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView receiverMessageTextView;
        ImageView receiverImageView;
        ImageView receiverTextSeen, receiverImageSeen;

        public MessageInViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMessageTextView = itemView.findViewById(R.id.receiver_message_textView1);
            receiverTextSeen = itemView.findViewById(R.id.receiver_message_textView_seen1);

            receiverImageView = itemView.findViewById(R.id.receiver_message_imageView1);
            receiverImageSeen = itemView.findViewById(R.id.receiver_message_imageView_seen1);
        }

        void bind(Message message) {
            if (message.getType().equals(Constants.MESSAGE_TYPE_TEXT)) {
                receiverMessageTextView.setVisibility(View.VISIBLE);
                String text = Cryptography.decrypt(message.getBody());  // decrypting message
                receiverMessageTextView.setText(String.format("%s\n\n%s - %s", text, message.getTime(), message.getDate()));
                // update seen status
                if (message.getSeen().equals(Constants.MESSAGE_SEEN_DEFAULT)) {
                    message.setSeen(Constants.MESSAGE_SEEN);
                    updateSeenToDatabase(message);
                }
                receiverTextSeen.setVisibility(View.VISIBLE);
            } else {
                receiverImageView.setVisibility(View.VISIBLE);
                String uri = Cryptography.decrypt(message.getBody());   // decrypting message
                Picasso.get().load(uri).placeholder(R.drawable.ic_photo_24).into(receiverImageView);
                // update seen status
                if (message.getSeen().equals(Constants.MESSAGE_SEEN_DEFAULT)) {
                    message.setSeen(Constants.MESSAGE_SEEN);
                    updateSeenToDatabase(message);
                }
                receiverImageSeen.setVisibility(View.VISIBLE);
            }
        }

    }

    /**
     * The view holder class that extends {@link RecyclerView.ViewHolder}
     * for {@link OnlineChatAdapter}. It's for sender message.
     */
    public static class MessageOutViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView senderMessageTextView;
        ImageView senderImageView;
        ImageView senderTextSeen, senderImageSeen;

        public MessageOutViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageTextView = itemView.findViewById(R.id.sender_message_textView2);
            senderTextSeen = itemView.findViewById(R.id.sender_message_textView_seen2);

            senderImageView = itemView.findViewById(R.id.sender_message_imageView2);
            senderImageSeen = itemView.findViewById(R.id.sender_message_imageView_seen2);
        }


        void bind(Message message) {
            if (message.getType().equals(Constants.MESSAGE_TYPE_TEXT)) {
                senderMessageTextView.setVisibility(View.VISIBLE);
                String text = Cryptography.decrypt(message.getBody());  // decrypting message
                senderMessageTextView.setText(String.format("%s\n\n%s - %s", text, message.getTime(), message.getDate()));
                if (!message.getSeen().equals(Constants.MESSAGE_SEEN_DEFAULT))
                    senderTextSeen.setVisibility(View.VISIBLE);
            } else {
                senderImageView.setVisibility(View.VISIBLE);
                String uri = Cryptography.decrypt(message.getBody());   // decrypting message
                Picasso.get().load(uri).placeholder(R.drawable.ic_photo_24).into(senderImageView);
                if (!message.getSeen().equals(Constants.MESSAGE_SEEN_DEFAULT))
                    senderImageSeen.setVisibility(View.VISIBLE);
            }
        }

    }

}
