package app.jam.jam.data;


import android.app.DatePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.DatePicker;
import android.widget.EditText;

import java.util.Calendar;
import java.util.TimeZone;

/**
 * This class is made to show a {@link DatePickerDialog} when clicking on the textView.
 */
public class EditTextDatePicker implements OnClickListener, OnDateSetListener {

    private EditText mEditText;
    private int mDay;
    private int mMonth;
    private int mYear;
    private Context context;

    public EditTextDatePicker(Context context, EditText editText) {
        this.mEditText = editText;
        this.mEditText.setOnClickListener(this);
        this.context = context;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        mYear = year;
        mMonth = monthOfYear;
        mDay = dayOfMonth;
        updateDisplay();
    }

    @Override
    public void onClick(View v) {
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
        new DatePickerDialog(context,
                this,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH))
                .show();
    }

    /**
     * This method updates the date in the EditText.
     */
    private void updateDisplay() {
        mEditText.setText(new StringBuilder()
                .append(mDay).append("/")
                .append(mMonth + 1).append("/")
                .append(mYear).append(" "));
    }
}