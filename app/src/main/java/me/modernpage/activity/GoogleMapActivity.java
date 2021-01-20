package me.modernpage.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.OnPlaceSelectedListener;
import com.seatgeek.placesautocomplete.PlacesAutocompleteTextView;
import com.seatgeek.placesautocomplete.model.Place;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import java.util.Locale;

import me.modernpage.Constants;
import me.modernpage.PermissionUtils;
import me.modernpage.task.GeocodeAddressService;

public class GoogleMapActivity extends AppCompatActivity implements OnMapReadyCallback, ActivityCompat.OnRequestPermissionsResultCallback {
    private static final String TAG = "GoogleMapActivity";
    public static String LAST_ADDRESS_EXTRA = "last_address";
    private static final int LOCATION_PERMESSION_REQUEST_CODE = 1;
    private static final float DEFAULT_ZOOM = 15F;
    private boolean permissionDenied = false;

    private GoogleMap mMap;
    // location provider to get current location of user
    private FusedLocationProviderClient mLocationProviderClient;
    private PlacesAutocompleteTextView mSearchText;
    private ImageView mMyLocation;
    private ProgressBar mProgressBar;
    private FloatingActionButton mSubmit;

    private Address mLastAddress;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_google_map);
        mSearchText = findViewById(R.id.input_search);
        mMyLocation = findViewById(R.id.my_location);
        mProgressBar = findViewById(R.id.map_progress_bar);
        mSubmit = findViewById(R.id.submit_location);

        if (savedInstanceState != null) {
            mLastAddress = savedInstanceState.getParcelable(LAST_ADDRESS_EXTRA);
        }

        if (PermissionUtils.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            Log.d(TAG, "onCreate: has permission");
            init();
        } else {
            Log.d(TAG, "onCreate: requesting permission");
            String[] requestPermission = {Manifest.permission.ACCESS_FINE_LOCATION};
            PermissionUtils.requestPermissions(this, requestPermission, LOCATION_PERMESSION_REQUEST_CODE);
            PermissionUtils.markedPermissionAsAsked(this, requestPermission[0]);
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
                    Intent findLocation = new Intent(GoogleMapActivity.this, GeocodeAddressService.class);
                    findLocation.putExtra(Constants.Gecode.EXTRA_RESULT_RECEIVER, new ResultReceiver(null) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            if (resultCode == Constants.Gecode.SUCCESS_RESULT) {
                                final Address address = resultData.getParcelable(Constants.Gecode.RESULT_ADDRESS);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setVisibility(View.GONE);
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                        moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
                                        mLastAddress = address;
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setVisibility(View.GONE);
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                        Toast.makeText(GoogleMapActivity.this, "Address Not Found", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }
                    });
                    findLocation.putExtra(Constants.Gecode.EXTRA_FETCH_TYPE, Constants.Gecode.USE_ADDRESS_NAME);
                    findLocation.putExtra(Constants.Gecode.EXTRA_LOCATION_NAME_DATA, searchQuery);

                    Constants.hideKeyboard(GoogleMapActivity.this);
                    mProgressBar.setVisibility(View.VISIBLE);
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                    startService(findLocation);
                }
                return false;
            }
        });

        mSearchText.setOnPlaceSelectedListener(new OnPlaceSelectedListener() {
            @Override
            public void onPlaceSelected(@NonNull Place place) {
                Log.d(TAG, "onPlaceSelected: " + place.description);
                mSearchText.getDetailsFor(place, mDetailsCallback);

                Constants.hideKeyboard(GoogleMapActivity.this);
            }
        });

        mMyLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: myLocation pressed : ");
                getDeviceLocation();
            }
        });

        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Intent data = new Intent();
                if (mLastAddress == null) {
                    Location location = mSearchText.getCurrentLocation();
                    Intent findLocation = new Intent(GoogleMapActivity.this, GeocodeAddressService.class);
                    findLocation.putExtra(Constants.Gecode.EXTRA_RESULT_RECEIVER, new ResultReceiver(null) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            if (resultCode == Constants.Gecode.SUCCESS_RESULT) {
                                final Address address = resultData.getParcelable(Constants.Gecode.RESULT_ADDRESS);
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setVisibility(View.GONE);
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                        data.putExtra(LAST_ADDRESS_EXTRA, address);
                                        setResult(RESULT_OK, data);
                                        finish();
                                    }
                                });
                            } else {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        mProgressBar.setVisibility(View.GONE);
                                        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                        Toast.makeText(GoogleMapActivity.this, "Address Not Found", Toast.LENGTH_LONG).show();
                                    }
                                });

                            }
                        }
                    });
                    findLocation.putExtra(Constants.Gecode.EXTRA_FETCH_TYPE, Constants.Gecode.USE_ADDRESS_LOCATION);
                    findLocation.putExtra(Constants.Gecode.EXTRA_LOCATION_LATLNG, new LatLng(location.getLatitude(), location.getLongitude()));

                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                    mProgressBar.setVisibility(View.VISIBLE);
                    startService(findLocation);
                } else {
                    data.putExtra(LAST_ADDRESS_EXTRA, mLastAddress);
                    setResult(RESULT_OK, data);
                    finish();
                }
                Log.d(TAG, "submitLocation: lastAddress: " + mLastAddress);
            }
        });

        Constants.hideKeyboard(GoogleMapActivity.this);
    }

    private DetailsCallback mDetailsCallback = new DetailsCallback() {
        @Override
        public void onSuccess(PlaceDetails placeDetails) {
            Log.d(TAG, "onSuccess: placeDetails : " + placeDetails.formatted_address + ", latlng: " + placeDetails.geometry.location.lat + ", " + placeDetails.geometry.location.lng);
            moveCamera(new LatLng(placeDetails.geometry.location.lat, placeDetails.geometry.location.lng), DEFAULT_ZOOM, placeDetails.formatted_address);
            mLastAddress = new Address(Locale.getDefault());
            mLastAddress.setLatitude(placeDetails.geometry.location.lat);
            mLastAddress.setLongitude(placeDetails.geometry.location.lng);
            mLastAddress.setAddressLine(0, placeDetails.formatted_address);
            mLastAddress.setPhone(placeDetails.formatted_phone_number);
            mLastAddress.setUrl(placeDetails.url);
            mLastAddress.setLocality(placeDetails.name);
        }

        @Override
        public void onFailure(Throwable throwable) {
            Log.d(TAG, "onFailure: placeDetails failed while getting");
        }
    };

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

                Intent findLocation = new Intent(GoogleMapActivity.this, GeocodeAddressService.class);
                findLocation.putExtra(Constants.Gecode.EXTRA_RESULT_RECEIVER, new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == Constants.Gecode.SUCCESS_RESULT) {
                            final Address address = resultData.getParcelable(Constants.Gecode.RESULT_ADDRESS);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
                                    mLastAddress = address;
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    mProgressBar.setVisibility(View.GONE);
                                    getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    Toast.makeText(GoogleMapActivity.this, "Address Not Found", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                });
                findLocation.putExtra(Constants.Gecode.EXTRA_FETCH_TYPE, Constants.Gecode.USE_ADDRESS_LOCATION);
                findLocation.putExtra(Constants.Gecode.EXTRA_LOCATION_LATLNG, latLng);

                mProgressBar.setVisibility(View.VISIBLE);
                getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
                startService(findLocation);
            }
        });

        if (!permissionDenied) {
            if (mLastAddress != null) {
                moveCamera(new LatLng(mLastAddress.getLatitude(), mLastAddress.getLongitude()), DEFAULT_ZOOM, mLastAddress.getAddressLine(0));
            } else {
                getDeviceLocation();
            }
            // add small blue dot on current location of device , centers it on the phone screen
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(false);
        }
    }

    private void getDeviceLocation() {
        Log.d(TAG, "getDeviceLocation: getting current device location");
        mLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (!permissionDenied) {
                final Task location = mLocationProviderClient.getLastLocation();
                location.addOnCompleteListener(new OnCompleteListener() {
                    @Override
                    public void onComplete(@NonNull Task task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: got current device location successfully");
                            Location currentLocation = (Location) task.getResult();
                            // move camera to the current location;
                            mSearchText.setCurrentLocation(currentLocation);
                            Log.d(TAG, "onComplete: getDeviceLocation: " + currentLocation.toString());
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
        }
    }

    @Override
    protected void onResumeFragments() {
        Log.d(TAG, "onResumeFragments: called");
        if (permissionDenied) {
            if (!PermissionUtils.hasPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                Snackbar.make(mSubmit, "Map is not available, give it permission to use", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Grant Access", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (PermissionUtils.shouldAskForPermission(GoogleMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
                                    PermissionUtils.requestPermissions(GoogleMapActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMESSION_REQUEST_CODE);
                                    Log.d(TAG, "onClick: request permission");
                                } else {
                                    PermissionUtils.goToAppSettings(GoogleMapActivity.this);
                                    Log.d(TAG, "onClick: goto app settings");
                                }
                            }
                        }).show();
            } else {
                permissionDenied = false;
                init();
            }
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState: called");
        if (mLastAddress != null) {
            Log.d(TAG, "onSaveInstanceState: lastAddress: " + mLastAddress.toString());
            outState.putParcelable(LAST_ADDRESS_EXTRA, mLastAddress);
        }
        super.onSaveInstanceState(outState);
    }

}
