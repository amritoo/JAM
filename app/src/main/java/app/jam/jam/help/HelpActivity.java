package app.jam.jam.help;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.appbar.MaterialToolbar;

import java.util.ArrayList;
import java.util.List;

import app.jam.jam.R;
import app.jam.jam.data.HelpItem;

public class HelpActivity extends AppCompatActivity {

    private List<HelpItem> mData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        MaterialToolbar toolbar = findViewById(R.id.help_toolbar);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        RecyclerView mRecyclerView = findViewById(R.id.help_recyclerView);
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        initializeData();
        HelpRecyclerAdapter adapter = new HelpRecyclerAdapter(mData);
        mRecyclerView.setAdapter(adapter);
    }

    /**
     * For adding all help string resources.
     */
    private void initializeData() {
        mData = new ArrayList<>();

        // Online help
        mData.add(new HelpItem(getString(R.string.question_1), getString(R.string.answer_1)));
        mData.add(new HelpItem(getString(R.string.question_2), getString(R.string.answer_2)));
        mData.add(new HelpItem(getString(R.string.question_3), getString(R.string.answer_3)));
        mData.add(new HelpItem(getString(R.string.question_4), getString(R.string.answer_4)));

        // Offline help
        mData.add(new HelpItem(getString(R.string.question_20), getString(R.string.answer_20)));
        mData.add(new HelpItem(getString(R.string.question_21), getString(R.string.answer_21)));
        mData.add(new HelpItem(getString(R.string.question_22), getString(R.string.answer_22)));

        // Others
        mData.add(new HelpItem(getString(R.string.question_40), getString(R.string.answer_40)));
    }

}