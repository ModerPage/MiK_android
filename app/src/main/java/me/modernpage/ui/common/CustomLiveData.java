package me.modernpage.ui.common;

import android.util.Pair;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

public class CustomLiveData extends MediatorLiveData<Pair<String, Long>> {
    public CustomLiveData(LiveData<String> stringLiveData, LiveData<Long> longLiveData) {
        addSource(stringLiveData, new Observer<String>() {
            @Override
            public void onChanged(String s) {
                setValue(Pair.create(s, longLiveData.getValue()));
            }
        });
        addSource(longLiveData, new Observer<Long>() {
            @Override
            public void onChanged(Long aLong) {
                setValue(Pair.create(stringLiveData.getValue(), aLong));
            }
        });
    }
}
