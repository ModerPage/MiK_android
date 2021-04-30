package me.modernpage.databinding;

import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;

import java.util.List;

import me.modernpage.activity.R;
import me.modernpage.data.local.entity.Group;

public class SpinnerBindingAdapter {
    @BindingAdapter("entries")
    public static void setEntries(Spinner spinner, List<Group> list) {
        if (list == null)
            return;
        ArrayAdapter<Group> adapter = new ArrayAdapter<>(spinner.getContext(), R.layout.spinner_item, list);
        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    @BindingAdapter("newValue")
    public static void setValue(Spinner spinner, Integer newValue) {
        spinner.setSelection(newValue == null ? 0 : newValue);
    }

    @InverseBindingAdapter(attribute = "newValue", event = "android:newValueAttrChanged")
    public static int getValue(Spinner spinner) {
        return spinner.getSelectedItemPosition();
    }

    @BindingAdapter(value = {"android:newValueAttrChanged"})
    public static void setOnItemSelectedListener(Spinner spinner, final InverseBindingListener newValueAttrChanged) {
        AdapterView.OnItemSelectedListener listener;
        if (newValueAttrChanged == null)
            listener = null;
        else {
            listener = new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    newValueAttrChanged.onChange();
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            };
        }
        spinner.setOnItemSelectedListener(listener);
    }

}
