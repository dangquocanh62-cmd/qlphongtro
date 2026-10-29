package com.nhatro.app.utils;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DatePickerHelper {
    public interface OnDateSelectedListener {
        void onDateSelected(String isoDate);
    }

    public static void attachDatePicker(FragmentManager fragmentManager, EditText editText, TextInputLayout inputLayout, String title) {
        attachDatePicker(fragmentManager, editText, inputLayout, title, null);
    }

    public static void attachDatePicker(FragmentManager fragmentManager,
                                        EditText editText,
                                        TextInputLayout inputLayout,
                                        String title,
                                        OnDateSelectedListener listener) {
        if (editText == null || fragmentManager == null) return;

        editText.setFocusable(false);
        editText.setClickable(true);
        editText.setCursorVisible(false);

        Runnable showPicker = () -> {
            MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
            builder.setTitleText(title != null ? title : "Chọn ngày");

            String currentText = editText.getText() != null ? editText.getText().toString().trim() : "";
            if (!currentText.isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date parsed = sdf.parse(currentText);
                    if (parsed != null) {
                        builder.setSelection(parsed.getTime());
                    }
                } catch (Exception ignored) {}
            }

            MaterialDatePicker<Long> picker = builder.build();
            picker.addOnPositiveButtonClickListener(selection -> {
                if (selection != null) {
                    Calendar utcCal = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                    utcCal.setTimeInMillis(selection);
                    String formatted = String.format(Locale.US, "%04d-%02d-%02d",
                            utcCal.get(Calendar.YEAR),
                            utcCal.get(Calendar.MONTH) + 1,
                            utcCal.get(Calendar.DAY_OF_MONTH));
                    editText.setText(formatted);
                    if (listener != null) {
                        listener.onDateSelected(formatted);
                    }
                }
            });
            picker.show(fragmentManager, "MATERIAL_DATE_PICKER_" + editText.getId());
        };

        editText.setOnClickListener(v -> showPicker.run());
        if (inputLayout != null) {
            inputLayout.setEndIconOnClickListener(v -> showPicker.run());
        }
    }

    public static void setupMonthDropdown(Context context, AutoCompleteTextView view) {
        if (context == null || view == null) return;
        String[] months = new String[]{"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, months);
        view.setAdapter(adapter);
        view.setKeyListener(null);
        view.setOnClickListener(v -> view.showDropDown());
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) view.showDropDown();
        });
    }

    public static void setupYearDropdown(Context context, AutoCompleteTextView view) {
        if (context == null || view == null) return;
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[]{
                String.valueOf(currentYear - 2),
                String.valueOf(currentYear - 1),
                String.valueOf(currentYear),
                String.valueOf(currentYear + 1),
                String.valueOf(currentYear + 2),
                String.valueOf(currentYear + 3)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(context, android.R.layout.simple_dropdown_item_1line, years);
        view.setAdapter(adapter);
        view.setKeyListener(null);
        view.setOnClickListener(v -> view.showDropDown());
        view.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) view.showDropDown();
        });
    }
}
