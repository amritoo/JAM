package app.jam.jam.offline;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textview.MaterialTextView;

import java.util.List;

import app.jam.jam.R;
import app.jam.jam.data.Message;
import app.jam.jam.methods.Cryptography;
import app.jam.jam.online.OnlineChatAdapter;

public class OfflineChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Message> userMessageList;
    private String receiverAddress;

    public static final int MESSAGE_TYPE_IN = 1;
    public static final int MESSAGE_TYPE_OUT = 2;

    public OfflineChatAdapter(List<Message> userMessageList, String receiverAddress) {
        this.userMessageList = userMessageList;
        this.receiverAddress = receiverAddress;
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
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = userMessageList.get(position);
        String text = Cryptography.decrypt(message.getBody());  // decrypting message
        message.setBody(text);
        if (receiverAddress.equals(message.getFrom())) {
            ((MessageInViewHolder) holder).bind(message);
        } else {
            ((MessageOutViewHolder) holder).bind(message);
        }
    }

    @Override
    public int getItemCount() {
        return userMessageList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (userMessageList.get(position).getFrom().equals(receiverAddress)) {
            return MESSAGE_TYPE_IN;
        } else {
            return MESSAGE_TYPE_OUT;
        }
    }

    /**
     * The view holder class that extends {@link RecyclerView.ViewHolder}
     * for {@link OnlineChatAdapter}. It's for receiver message.
     */
    public static class MessageInViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView receiverMessageTextView;

        public MessageInViewHolder(@NonNull View itemView) {
            super(itemView);

            receiverMessageTextView = itemView.findViewById(R.id.receiver_message_textView1);
        }

        void bind(Message message) {
            receiverMessageTextView.setVisibility(View.VISIBLE);
            String text = Cryptography.decrypt(message.getBody());  // decrypting message
            receiverMessageTextView.setText(String.format("%s\n\n%s - %s", text, message.getTime(), message.getDate()));
        }

    }

    /**
     * The view holder class that extends {@link RecyclerView.ViewHolder}
     * for {@link OfflineChatAdapter}. It's for sender message.
     */
    public static class MessageOutViewHolder extends RecyclerView.ViewHolder {

        MaterialTextView senderMessageTextView;

        public MessageOutViewHolder(@NonNull View itemView) {
            super(itemView);
            senderMessageTextView = itemView.findViewById(R.id.sender_message_textView2);
        }

        void bind(Message message) {
            senderMessageTextView.setVisibility(View.VISIBLE);
            String text = Cryptography.decrypt(message.getBody());  // decrypting message
            senderMessageTextView.setText(String.format("%s\n\n%s - %s", text, message.getTime(), message.getDate()));
        }

    }

}
