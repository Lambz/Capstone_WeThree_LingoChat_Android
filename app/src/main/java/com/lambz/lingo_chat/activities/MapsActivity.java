package com.lambz.lingo_chat.activities;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.lambz.lingo_chat.R;
import com.lambz.lingo_chat.Utils;
import com.lambz.lingo_chat.models.UserLocation;

import java.io.Serializable;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback
{

    private static final int REQUEST_CODE = 1111;
    private static final float DEFAULT_ZOOM_LEVEL = 13.25f;
    private static final String TAG = "MapsActivity";
    private GoogleMap mMap;
    private UserLocation mUserLocation;
    private LocationManager mLocationManager;
    private Marker mMarker;
    private Button mSaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        checkPermissions();
        Serializable serializable = getIntent().getSerializableExtra("location");
        mSaveButton = findViewById(R.id.save_btn);
        if (serializable != null)
        {
            mUserLocation = (UserLocation) serializable;
            mSaveButton.setVisibility(View.GONE);
        }
        mLocationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onMapReady(GoogleMap googleMap)
    {
        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        setUISettings();
        if (mUserLocation == null)
        {
            setOnClickListeners();
        } else
        {
            addUserMarker();
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            requestLocationPermission();
            return;
        }
        enableUserLocationAndZoom();
    }

    private void addUserMarker()
    {
        addMarker(mUserLocation.getLatLng());
    }

    private void setUISettings()
    {
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setMyLocationButtonEnabled(false);
        mMap.getUiSettings().setCompassEnabled(false);
    }

    private void setOnClickListeners()
    {
        mMap.setOnMapClickListener(mOnMapClickListener);
    }

    private void checkPermissions()
    {
        if (!hasLocationPermission())
        {
            requestLocationPermission();
        }
    }

    /**
     * Method to request Location Permission
     */
    private void requestLocationPermission()
    {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_CODE);
    }

    /**
     * Method to check if the app has User Location Permission
     *
     * @return - True if the app has User Location Permission
     */
    private boolean hasLocationPermission()
    {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        if (requestCode == REQUEST_CODE)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {
                if (mMap != null)
                {
                    enableUserLocationAndZoom();
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private Location getCurrentLocation()
    {
        return mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
    }

    @SuppressLint("MissingPermission")
    private void enableUserLocationAndZoom()
    {
        mMap.setMyLocationEnabled(true);
        Location location = getCurrentLocation();
        if (location != null)
        {
            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, DEFAULT_ZOOM_LEVEL));
        }
    }

    GoogleMap.OnMapClickListener mOnMapClickListener = latLng -> addMarker(latLng);

    public void saveClicked(View view)
    {
        if (mUserLocation != null)
        {
            Intent intent = new Intent();
            intent.putExtra("location", mUserLocation);
            setResult(RESULT_OK, intent);
            finish();
        }
        else
        {
            // Code for location not selected
        }
    }

    private void addMarker(LatLng latLng)
    {
        if (mMarker != null)
        {
            mMarker.remove();
        }
        MarkerOptions markerOptions = new MarkerOptions().position(latLng);
        mMarker = mMap.addMarker(markerOptions);
        mUserLocation = new UserLocation(latLng.latitude,latLng.longitude);
        zoomToMarker(mMarker);
        setInfoAsync(latLng, mMarker);
    }

    private void zoomToMarker(Marker marker)
    {
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.getPosition(), DEFAULT_ZOOM_LEVEL));
    }


    public void backClicked(View view)
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    private void setInfoAsync(LatLng latLng, Marker mMarker)
    {
        new Thread(() ->
        {
            try
            {
                Geocoder geocoder = new Geocoder(MapsActivity.this);
                Address address = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1).get(0);
                String title = Utils.getFormattedAddress(address);
                if (!title.isEmpty())
                {
                    MapsActivity.this.runOnUiThread(() ->
                    {
                        setInfo(title, mMarker);
                    });
                } else
                {
                    MapsActivity.this.runOnUiThread(() -> setInfo(getString(R.string.unknown), mMarker));
                }
            } catch (Exception e)
            {
                e.printStackTrace();
                MapsActivity.this.runOnUiThread(() -> setInfo(getString(R.string.unknown), mMarker));
            }
        }).start();
    }

    private void setInfo(String title, Marker mMarker)
    {
        mUserLocation.setTitle(title);
        mMarker.setTitle(title);
        mMarker.showInfoWindow();
    }
}