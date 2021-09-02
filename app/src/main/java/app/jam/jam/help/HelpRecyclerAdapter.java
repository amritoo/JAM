//package app.jam.jam.help;
//
//import android.animation.ObjectAnimator;
//import android.content.Context;
//import android.util.SparseBooleanArray;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.RelativeLayout;
//import android.widget.TextView;
//
//import androidx.annotation.NonNull;
//import androidx.recyclerview.widget.RecyclerView;
//
//import com.github.aakira.expandablelayout.ExpandableLayout;
//import com.github.aakira.expandablelayout.ExpandableLayoutListenerAdapter;
//import com.github.aakira.expandablelayout.ExpandableLinearLayout;
//import com.github.aakira.expandablelayout.Utils;
//
//import java.util.List;
//
//import app.jam.jam.R;
//import app.jam.jam.data.Constants;
//import app.jam.jam.data.HelpItem;
//
//// TODO: don't use expandable layout
//
//public class HelpRecyclerAdapter extends RecyclerView.Adapter<HelpRecyclerAdapter.HelpViewHolder> {
//
//    private final List<HelpItem> data;
//    private SparseBooleanArray expandState = new SparseBooleanArray();
//
//    public HelpRecyclerAdapter(final List<HelpItem> data) {
//        this.data = data;
//        for (int i = 0; i < data.size(); i++) {
//            expandState.append(i, false);
//        }
//    }
//
//    @NonNull
//    @Override
//    public HelpViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
//        Context context = parent.getContext();
//        return new HelpViewHolder(LayoutInflater.from(context)
//                .inflate(R.layout.list_help_format, parent, false));
//    }
//
//    @Override
//    public void onBindViewHolder(final HelpViewHolder holder, final int position) {
//        final HelpItem item = data.get(position);
//        holder.setIsRecyclable(false);
//        // Setting data
//        holder.question_textView.setText(item.getQuestion());
//        holder.question_textView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                onClickButton(holder.expandableLayout);
//            }
//        });
//        holder.answer_textView.setText(item.getAnswer());
//        // Setting expandable view
//        holder.expandableLayout.setInRecyclerView(true);
//        holder.expandableLayout.setExpanded(expandState.get(position));
//        holder.expandableLayout.setListener(new ExpandableLayoutListenerAdapter() {
//            @Override
//            public void onPreOpen() {
//                createRotateAnimator(holder.buttonLayout, 0f, 180f).start();
//                expandState.put(position, true);
//            }
//
//            @Override
//            public void onPreClose() {
//                createRotateAnimator(holder.buttonLayout, 180f, 0f).start();
//                expandState.put(position, false);
//            }
//        });
//
//        holder.buttonLayout.setRotation(expandState.get(position) ? 180f : 0f);
//        holder.buttonLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(final View v) {
//                onClickButton(holder.expandableLayout);
//            }
//        });
//    }
//
//    private void onClickButton(final ExpandableLayout expandableLayout) {
//        expandableLayout.toggle();
//    }
//
//    @Override
//    public int getItemCount() {
//        return data.size();
//    }
//
//    public ObjectAnimator createRotateAnimator(final View target, final float from, final float to) {
//        ObjectAnimator animator = ObjectAnimator.ofFloat(target, "rotation", from, to);
//        animator.setDuration(Constants.HELP_TOGGLE_ANIMATION_DURATION);  // animation duration
//        animator.setInterpolator(Utils.createInterpolator(Utils.LINEAR_INTERPOLATOR));
//        return animator;
//    }
//
//    public static class HelpViewHolder extends RecyclerView.ViewHolder {
//
//        public TextView question_textView, answer_textView;
//        public RelativeLayout buttonLayout;
//        public ExpandableLinearLayout expandableLayout;
//
//        public HelpViewHolder(@NonNull View itemView) {
//            super(itemView);
//
//            question_textView = itemView.findViewById(R.id.question_textView);
//            answer_textView = itemView.findViewById(R.id.answer_textView);
//            buttonLayout = itemView.findViewById(R.id.help_expand_button);
//            expandableLayout = itemView.findViewById(R.id.help_expandableLayout);
//        }
//    }
//
//}