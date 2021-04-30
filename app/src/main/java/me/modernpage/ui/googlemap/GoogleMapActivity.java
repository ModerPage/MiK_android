package me.modernpage.ui.googlemap;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.provider.Settings;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.lifecycle.ViewModelProvider;

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
import com.google.android.material.snackbar.Snackbar;
import com.seatgeek.placesautocomplete.DetailsCallback;
import com.seatgeek.placesautocomplete.model.PlaceDetails;

import java.util.Locale;

import me.modernpage.activity.R;
import me.modernpage.activity.databinding.ActivityGoogleMapBinding;
import me.modernpage.ui.BaseActivity;
import me.modernpage.ui.common.GeocodeAddressService;

import static me.modernpage.ui.common.GeocodeAddressService.EXTRA_FETCH_TYPE;
import static me.modernpage.ui.common.GeocodeAddressService.EXTRA_LOCATION_LATLNG;
import static me.modernpage.ui.common.GeocodeAddressService.EXTRA_LOCATION_NAME_DATA;
import static me.modernpage.ui.common.GeocodeAddressService.EXTRA_RESULT_RECEIVER;
import static me.modernpage.ui.common.GeocodeAddressService.RESULT_ADDRESS;
import static me.modernpage.ui.common.GeocodeAddressService.SUCCESS_RESULT;
import static me.modernpage.ui.common.GeocodeAddressService.USE_ADDRESS_LOCATION;
import static me.modernpage.ui.common.GeocodeAddressService.USE_ADDRESS_NAME;


public class GoogleMapActivity extends BaseActivity<ActivityGoogleMapBinding> implements OnMapReadyCallback {
    private static final String TAG = "GoogleMapActivity";
    private static final float DEFAULT_ZOOM = 15F;
    private boolean permissionDenied = false;

    GoogleMapViewModel mGoogleMapViewModel;

    private GoogleMap mMap;
    // location provider to get current location of user
    private FusedLocationProviderClient mLocationProviderClient;

    private final ActivityResultLauncher<String[]> mGoogleMapRequestPermissionForResult = registerForActivityResult(
            new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                if (result.get(Manifest.permission.ACCESS_FINE_LOCATION) && result.get(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    // init map
                    init();
                } else {
                    permissionDenied = true;
                }
            }
    );

    public interface GoogleMapHandler {
        void onEditorAction(TextView textView, int actionId, KeyEvent keyEvent);

        void myLocationClicked();

        void submitLocationClicked();
    }

    @Override
    public int getLayoutRes() {
        return R.layout.activity_google_map;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mGoogleMapViewModel = new ViewModelProvider(this).get(GoogleMapViewModel.class);
        dataBinding.setLifecycleOwner(this);
        dataBinding.setHandler(mHandler);
        dataBinding.googleMapContent.setDetailsCallback(mDetailsCallback);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            init();
        } else {
            mGoogleMapRequestPermissionForResult.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
        }
    }

    private void init() {
        Log.d(TAG, "init: initializing map");
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        assert mapFragment != null;
        mapFragment.getMapAsync(this);
    }

    private DetailsCallback mDetailsCallback = new DetailsCallback() {
        @Override
        public void onSuccess(PlaceDetails placeDetails) {
            moveCamera(new LatLng(placeDetails.geometry.location.lat, placeDetails.geometry.location.lng), DEFAULT_ZOOM, placeDetails.formatted_address);
            Address address = new Address(Locale.getDefault());
            address.setLatitude(placeDetails.geometry.location.lat);
            address.setLongitude(placeDetails.geometry.location.lng);
            address.setAddressLine(0, placeDetails.formatted_address);
            address.setPhone(placeDetails.formatted_phone_number);
            address.setUrl(placeDetails.url);
            address.setLocality(placeDetails.name);
            mGoogleMapViewModel.setAddress(address);
        }

        @Override
        public void onFailure(Throwable throwable) {
            Log.d(TAG, "onFailure: placeDetails failed while getting");
        }
    };

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map is ready");
        mMap = googleMap;

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Intent findLocation = new Intent(GoogleMapActivity.this, GeocodeAddressService.class);
                findLocation.putExtra(EXTRA_RESULT_RECEIVER, new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == SUCCESS_RESULT) {
                            final Address address = resultData.getParcelable(RESULT_ADDRESS);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopProgress();
                                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
                                    mGoogleMapViewModel.setAddress(address);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopProgress();
                                    Toast.makeText(GoogleMapActivity.this, "Address Not Found", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                });
                findLocation.putExtra(EXTRA_FETCH_TYPE, USE_ADDRESS_LOCATION);
                findLocation.putExtra(EXTRA_LOCATION_LATLNG, latLng);
                startProgress();
                startService(findLocation);
            }
        });

        if (!permissionDenied) {
            Address address = mGoogleMapViewModel.getAddress().getValue();
            if (address != null) {
                moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
            } else {
                Log.d(TAG, "onMapReady: called to get deviceLocation");
                getDeviceLocation();
            }
            // add small blue dot on current location of device , centers it on the phone screen
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
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
                            Location currentLocation = (Location) task.getResult();
                            // move camera to the current location;
                            dataBinding.googleMapContent.inputSearch.setCurrentLocation(currentLocation);
                            moveCamera(new LatLng(currentLocation.getLatitude(), currentLocation.getLongitude()), DEFAULT_ZOOM, null);
                        } else {
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
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Snackbar.make(dataBinding.submitLocation, "Map is not available, give it permission to use", Snackbar.LENGTH_INDEFINITE)
                        .setAction("Grant Access", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if (ActivityCompat.shouldShowRequestPermissionRationale(GoogleMapActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)
                                        || ActivityCompat.shouldShowRequestPermissionRationale(GoogleMapActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                                    mGoogleMapRequestPermissionForResult.launch(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION});
                                } else {
                                    goToAppSettings();
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

    private void goToAppSettings() {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", GoogleMapActivity.this.getPackageName(), null);
        intent.setData(uri);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private GoogleMapHandler mHandler = new GoogleMapHandler() {
        @Override
        public void onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || actionId == EditorInfo.IME_ACTION_DONE
                    || keyEvent.getAction() == KeyEvent.ACTION_DOWN
                    || keyEvent.getAction() == KeyEvent.KEYCODE_ENTER) {

                // execute search action
                String searchQuery = textView.getText().toString();
                Log.d(TAG, "onEditorAction: searchQuery: " + searchQuery);
                Intent findLocation = new Intent(GoogleMapActivity.this, GeocodeAddressService.class);
                findLocation.putExtra(EXTRA_RESULT_RECEIVER, new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == SUCCESS_RESULT) {
                            final Address address = resultData.getParcelable(RESULT_ADDRESS);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopProgress();
                                    moveCamera(new LatLng(address.getLatitude(), address.getLongitude()), DEFAULT_ZOOM, address.getAddressLine(0));
                                    mGoogleMapViewModel.setAddress(address);
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopProgress();
                                    Toast.makeText(GoogleMapActivity.this, "Address Not Found", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                });
                findLocation.putExtra(EXTRA_FETCH_TYPE, USE_ADDRESS_NAME);
                findLocation.putExtra(EXTRA_LOCATION_NAME_DATA, searchQuery);

                hideKeyboard();
                startProgress();

                startService(findLocation);
            }
        }

        @Override
        public void myLocationClicked() {
            getDeviceLocation();
        }

        @Override
        public void submitLocationClicked() {
            final Intent data = new Intent();
            if (mGoogleMapViewModel.getAddress() == null) {
                Location location = dataBinding.googleMapContent.inputSearch.getCurrentLocation();
                Intent findLocation = new Intent(GoogleMapActivity.this, GeocodeAddressService.class);
                findLocation.putExtra(EXTRA_RESULT_RECEIVER, new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        if (resultCode == SUCCESS_RESULT) {
                            final Address address = resultData.getParcelable(RESULT_ADDRESS);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopProgress();
                                    data.putExtra(Address.class.getSimpleName(), address);
                                    setResult(RESULT_OK, data);
                                    finish();
                                }
                            });
                        } else {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    stopProgress();
                                    Toast.makeText(GoogleMapActivity.this, "Address Not Found", Toast.LENGTH_LONG).show();
                                }
                            });

                        }
                    }
                });
                findLocation.putExtra(EXTRA_FETCH_TYPE, USE_ADDRESS_LOCATION);
                findLocation.putExtra(EXTRA_LOCATION_LATLNG, new LatLng(location.getLatitude(), location.getLongitude()));
                startProgress();
                startService(findLocation);
            } else {
                data.putExtra(Address.class.getSimpleName(), mGoogleMapViewModel.getAddress().getValue());
                setResult(RESULT_OK, data);
                finish();
            }
        }
    };

    private void startProgress() {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
        dataBinding.googleMapContent.mapProgressBar.setVisibility(View.VISIBLE);
    }

    private void stopProgress() {
        dataBinding.googleMapContent.mapProgressBar.setVisibility(View.GONE);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);
    }
}

