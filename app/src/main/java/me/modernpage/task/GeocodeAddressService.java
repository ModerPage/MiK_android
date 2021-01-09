package me.modernpage.task;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import me.modernpage.Constants;

public class GeocodeAddressService extends IntentService {
    private static final String TAG = "GeocodeAddressService";

    private ResultReceiver mResultReceiver;

    public GeocodeAddressService() {
        super("GeocodeAddressService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses = null;

        mResultReceiver = intent.getParcelableExtra(Constants.Gecode.EXTRA_RESULT_RECEIVER);
        int fetchType = intent.getIntExtra(Constants.Gecode.EXTRA_FETCH_TYPE, 0);
        String errorMessage = null;

        if (fetchType == Constants.Gecode.USE_ADDRESS_NAME) {
            String locationName = intent.getStringExtra(Constants.Gecode.EXTRA_LOCATION_NAME_DATA);
            Log.d(TAG, "onHandleIntent: location name: " + locationName);
            try {
                addresses = geocoder.getFromLocationName(locationName, 1);
            } catch (IOException e) {
                errorMessage = "Service not available";
                Log.e(TAG, errorMessage, e);
            }

        } else if (fetchType == Constants.Gecode.USE_ADDRESS_LOCATION) {
            LatLng latLng = intent.getParcelableExtra(
                    Constants.Gecode.EXTRA_LOCATION_LATLNG);

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
            if (errorMessage.isEmpty()) {
                errorMessage = "Not Found";
            }
            deliverResultToReceiver(Constants.Gecode.FAILURE_RESULT, errorMessage, null);
        } else {

            Address address = addresses.get(0);
            String successMessage = "Address found";
            deliverResultToReceiver(Constants.Gecode.SUCCESS_RESULT, successMessage, address);
        }
    }

    private void deliverResultToReceiver(int resultCode, String message, Address address) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.Gecode.RESULT_ADDRESS, address);
        bundle.putString(Constants.Gecode.RESULT_MESSAGE, message);
        mResultReceiver.send(resultCode, bundle);
    }
}
