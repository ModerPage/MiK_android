package me.modernpage.ui.googlemap;

import android.location.Address;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.SavedStateHandle;
import androidx.lifecycle.ViewModel;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class GoogleMapViewModel extends ViewModel {
    private LiveData<Address> address;
    private SavedStateHandle mSavedStateHandle;

    @Inject
    public GoogleMapViewModel(SavedStateHandle savedStateHandle) {
        mSavedStateHandle = savedStateHandle;
        address = savedStateHandle.getLiveData("address");
    }

    public LiveData<Address> getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        mSavedStateHandle.set("address", address);
    }
}
