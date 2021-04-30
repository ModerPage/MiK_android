package me.modernpage.ui.common;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class GeocodeAddressService extends IntentService {
    private static final String TAG = "GeocodeAddressService";
    public static final String EXTRA_RESULT_RECEIVER = "result_receiver";
    public static final String EXTRA_FETCH_TYPE = "fetch_type";
    public static final int USE_ADDRESS_NAME = 100;
    public static final int USE_ADDRESS_LOCATION = 101;
    public static final String EXTRA_LOCATION_NAME_DATA = "location_name_data";
    public static final String EXTRA_LOCATION_LATLNG = "location_data";

    public static final int FAILURE_RESULT = 200;
    public static final int SUCCESS_RESULT = 201;

    public static final String RESULT_ADDRESS = "result_address";
    public static final String RESULT_MESSAGE = "result_message";
    private ResultReceiver mResultReceiver;

    public GeocodeAddressService() {
        super("GeocodeAddressService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;

        mResultReceiver = intent.getParcelableExtra(EXTRA_RESULT_RECEIVER);
        int fetchType = intent.getIntExtra(EXTRA_FETCH_TYPE, 0);
        String errorMessage = null;

        if (fetchType == USE_ADDRESS_NAME) {
            String locationName = intent.getStringExtra(EXTRA_LOCATION_NAME_DATA);
            Log.d(TAG, "onHandleIntent: location name: " + locationName);
            try {
                addresses = geocoder.getFromLocationName(locationName, 1);
            } catch (IOException e) {
                errorMessage = "Service not available";
                Log.e(TAG, errorMessage, e);
            }

        } else if (fetchType == USE_ADDRESS_LOCATION) {
            LatLng latLng = intent.getParcelableExtra(EXTRA_LOCATION_LATLNG);

            try {
                addresses = geocoder.getFromLocation(
                        latLng.latitude, latLng.longitude, 1);
            } catch (IOException ioException) {
                errorMessage = "Service Not Available";
                Log.e(TAG, errorMessage, ioException);
            } catch (IllegalArgumentException illegalArgumentException) {
                errorMessage = "Invalid Latitude or Longitude Used";
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + latLng.latitude + ", Longitude = " +
                        latLng.longitude, illegalArgumentException);
            }
        } else {
            errorMessage = "Unknown Type";
        }

        if (addresses == null || addresses.size() == 0) {
            if (errorMessage == null) {
                errorMessage = "Not Found";
            }
            deliverResultToReceiver(FAILURE_RESULT, errorMessage, null);
        } else {

            Address address = addresses.get(0);
            String successMessage = "Address found";
            deliverResultToReceiver(SUCCESS_RESULT, successMessage, address);
        }
    }

    private void deliverResultToReceiver(int resultCode, String message, Address address) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(RESULT_ADDRESS, address);
        bundle.putString(RESULT_MESSAGE, message);
        mResultReceiver.send(resultCode, bundle);
    }
}
