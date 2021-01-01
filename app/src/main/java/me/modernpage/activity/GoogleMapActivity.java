package me.modernpage.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import me.modernpage.PermissionUtils;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "GoogleMapActivity";
    private static String LAST_LOCATION_LATLNG_EXTRA = "last_selected_location_latlng";
    private static String LAST_LOCATION_TITLE_EXTRA = "last_selected_location_title";
    private static final int LOCATION_PERMESSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15F;
    private boolean permissionDenied = false;

    private GoogleMap mMap;
    // location provider to get current location of user
    private FusedLocationProviderClient mLocationProviderClient;
    private PlacesAutocompleteTextView mSearchText;
    private ImageView mMyLocation;

    private LatLng mLastLocationLatLng;
    private String mLastLocationTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        mSearchText = findViewById(R.id.input_search);
        mMyLocation = findViewById(R.id.my_location);

        if (savedInstanceState != null) {
            mLastLocationLatLng = savedInstanceState.getParcelable(LAST_LOCATION_LATLNG_EXTRA);
            mLastLocationTitle = savedInstanceState.getString(LAST_LOCATION_TITLE_EXTRA);
            Log.d(TAG, "onCreate : savedInstanceState: latlng: " + mLastLocationLatLng.longitude + ", " + mLastLocationLatLng.latitude + ", title: " + mLastLocationTitle);
        }

        if (PermissionUtils.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "onCreate: has permission");
            init();
        } else {
            Log.d(TAG, "onCreate: requesting permission");
            String[] requestPermission = {Manifest.permission.ACCESS_FINE_LOCATION};
            PermissionUtils.requestPermissions(this, requestPermission, LOCATION_PERMESSION_REQUEST_CODE);
            PermissionUtils.hasAskedForPermission(this, requestPermission[0]);
        }
    }

    private void init() {
        Log.d(TAG, "init: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mSearchText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                Log.d(TAG, "onEditorAction: called");
                if (actionId == EditorInfo.IME_ACTION_SEARCH
                        || actionId == EditorInfo.IME_ACTION_DONE
                        || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                        || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                    // execute search action
                    String searchQuery = textView.getText().toString();
                    geoLocate(searchQuery);
                }
                return false;
            }
        });

        mSearchText.setOnPlaceSelectedListener(new OnPlaceSelectedListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d(TAG, "onPlaceSelected: " + place.description);
                mSearchText.getDetailsFor(place, mDetailsCallback);

                hideKeyboard();
            }
        });

        mMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: myLocation pressed : ");
                getDeviceLocation();
            }
        });

        hideKeyboard();
    }

    private DetailsCallback mDetailsCallback = new DetailsCallback() {
        @Override
        public void onSuccess(PlaceDetails placeDetails) {
            Log.d(TAG, "onSuccess: placeDetails : " + placeDetails.formatted_address + ", latlng: " + placeDetails.geometry.location.lat + ", " + placeDetails.geometry.location.lng);
            moveCamera(new LatLng(placeDetails.geometry.location.lat, placeDetails.geometry.location.lng), DEFAULT_ZOOM, placeDetails.formatted_address);
        }

        @Override
        public void onFailure(Throwable throwable) {
            Log.d(TAG, "onFailure: placeDetails failed while getting");
        }
    };

    private void geoLocate(String searchQuery) {
        Log.d(TAG, "geoLocate: called");
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(searchQuery, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException" + e);
        }

        if (addressList.size() > 0) {
            Address address = addressList.get(0);
            Log.d(TAG, "geoLocate: found location: " + address.getLocality());
            moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
        }

        hideKeyboard();
    }

    private void geoLocate(LatLng latLng) {
        Log.d(TAG, "geoLocate: called by latlng");
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList = new ArrayList<>();

        try {
            addressList = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);
        } catch (IOException e) {
            Log.e(TAG, "geoLocate: IOException: " + e);
        }

        if (addressList.size() > 0) {
            Address address = addressList.get(0);
            Log.d(TAG, "geoLocate: found location by latlng: " + address.toString());
            moveCamera(latLng, DEFAULT_ZOOM, address.getAddressLine(0));
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != LOCATION_PERMESSION_REQUEST_CODE) {
            return;
        }
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // Enable the my location layer if the permission has been granted.
            init();
        } else {
            Toast.makeText(this, "Permission denied", Toast.LENGTH_LONG).show();
            // Permission was denied. Display an error message
            // [START_EXCLUDE]
            // Display the missing permission error dialog when the fragments resume.
            permissionDenied = true;
            // [END_EXCLUDE]
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Log.d(TAG, "onMapClick: latlng: " + latLng.latitude + " , " + latLng.longitude);
                geoLocate(latLng);
            }
        });

        if (!permissionDenied) {
            if (mLastLocationLatLng != null && mLastLocationTitle != null) {
                moveCamera(mLastLocationLatLng, DEFAULT_ZOOM, mLastLocationTitle);
            } else {
                getDeviceLocation();
            }
            // add small blue dot on current location of device , centers it on the phone screen
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
            mMap.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting current device location");
        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (!permissionDenied) {
                Task location = mLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: got current device location successfully");
                            Location currentLocation = (Location) task.getResult();
                            // move camera to the current location;
                            mSearchText.setCurrentLocation(currentLocation);
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, null);
                        } else {
                            Log.d(TAG, "onComplete: not found current location");
                            Toast.makeText(GoogleMapActivity.this, "unable to get current location", Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        } catch (SecurityException e) {
            Log.e(TAG, "getDeviceLocation: SecurityException: " + e);
        }
    }

    private void moveCamera(LatLng latLng, float zoom, String title) {
        Log.d(TAG, "moveCamera: moving to " + latLng.latitude + ", " + latLng.longitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, zoom));
        mMap.clear();
        if (title != null) {
            MarkerOptions markerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(title);
            mMap.addMarker(markerOptions);

            mLastLocationLatLng = latLng;
            mLastLocationTitle = title;
        }
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(this);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    @Override
    protected void onResumeFragments() {
        Log.d(TAG, "onResumeFragments: called");
        if (permissionDenied) {
            if (!PermissionUtils.hasPermission(GoogleMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Log.d(TAG, "onResumeFragments: go to settings");
                PermissionUtils.goToAppSettings(this);
            } else {
                Log.d(TAG, "onResumeFragments: got right permission");
                permissionDenied = false;
                init();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: called");
        if (mLastLocationLatLng != null && mLastLocationTitle != null) {
            Log.d(TAG, "onSaveInstanceState: latlng: " + mLastLocationLatLng.longitude + ", " + mLastLocationLatLng.latitude + " , " + mLastLocationTitle);
            outState.putParcelable(LAST_LOCATION_LATLNG_EXTRA, mLastLocationLatLng);
            outState.putString(LAST_LOCATION_TITLE_EXTRA, mLastLocationTitle);
        }
        super.onSaveInstanceState(outState);
    }
}
