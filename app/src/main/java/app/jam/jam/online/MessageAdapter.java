package app.jam.jam.online;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.List;

import app.jam.jam.R;
import app.jam.jam.data.Message;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> userMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersReference;


    public MessageAdapter(List<Message> userMessageList) {
        this.userMessageList = userMessageList;
    }


    public static class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView senderMessageTextView, receiverMessageTextView;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);

            senderMessageTextView = itemView.findViewById(R.id.sender_message_textView);
            receiverMessageTextView = itemView.findViewById(R.id.receiver_message_textView);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.messages_layout, parent, false);
        MessageViewHolder viewHolder = new MessageViewHolder(view);
        mAuth = FirebaseAuth.getInstance();
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull final MessageViewHolder holder, int position) {
        String messageSenderId = mAuth.getCurrentUser().getUid();
        Message message = userMessageList.get(position);

        String fromUserID = message.getFrom();
        String fromMessageType = message.getType();

        if (fromMessageType.equals("text")) {
            if (fromUserID.equals(messageSenderId)) {
                holder.senderMessageTextView.setVisibility(View.VISIBLE);
                holder.receiverMessageTextView.setVisibility(View.GONE);

//                holder.senderMessageText.setBackgroundResource(R.drawable.sender_messages_layout);
//                holder.senderMessageTextView.setBackgroundResource(0);
//                holder.senderMessageTextView.setTextColor(Color.BLACK);
                holder.senderMessageTextView.setText(String.format("%s\n\n%s - %s", message.getBody(), message.getTime(), message.getDate()));
            } else {
                holder.senderMessageTextView.setVisibility(View.GONE);
                holder.receiverMessageTextView.setVisibility(View.VISIBLE);

//                holder.receiverMessageText.setBackgroundResource(R.drawable.receiver_messages_layout);
//                holder.receiverMessageTextView.setBackgroundResource(0);
//                holder.receiverMessageTextView.setTextColor(Color.BLACK);
                holder.receiverMessageTextView.setText(String.format("%s\n\n%s - %s", message.getBody(), message.getTime(), message.getDate()));
            }
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

}
