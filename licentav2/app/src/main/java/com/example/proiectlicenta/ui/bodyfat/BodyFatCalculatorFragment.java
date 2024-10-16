package com.example.proiectlicenta.ui.bodyfat;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;

import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.proiectlicenta.R;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.net.PlacesClient;

public class BodyFatCalculatorFragment extends Fragment implements OnMapReadyCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private BodyFatCalculatorViewModel viewModel;
    private EditText editTextAbdomen;
    private EditText editTextWeight;
    private TextView textViewResult;
    private ProgressBar progressBar;
    private SeekBar seekBarBodyFat;
    private TextView textViewBodyFatInfo;
    private GoogleMap mMap;
    private PlacesClient placesClient;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private BroadcastReceiver locationReceiver;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bodyfat_calculator, container, false);

        Toolbar toolbar = view.findViewById(R.id.toolbar);
        toolbar.inflateMenu(R.menu.menu_refresh);
        toolbar.setOnMenuItemClickListener(this::onOptionsItemSelected);

        editTextAbdomen = view.findViewById(R.id.editTextAbdomen);
        editTextWeight = view.findViewById(R.id.editTextWeight);
        textViewResult = view.findViewById(R.id.textViewResult);
        progressBar = view.findViewById(R.id.progressBar);
        seekBarBodyFat = view.findViewById(R.id.seekBarBodyFat);
        textViewBodyFatInfo = view.findViewById(R.id.textViewBodyFatInfo);
        Button buttonCalculate = view.findViewById(R.id.buttonCalculate);

        viewModel = new ViewModelProvider(this).get(BodyFatCalculatorViewModel.class);
        viewModel.getBodyFatResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Log.d("BodyFatCalculatorFragment", "Observed body fat result: " + result);
                progressBar.setVisibility(View.GONE);
                textViewResult.setText(String.format("Body Fat: %.2f%%", result));
                textViewResult.setVisibility(View.VISIBLE);
                seekBarBodyFat.setProgress((int) result.doubleValue());
                seekBarBodyFat.setVisibility(View.VISIBLE);
                textViewBodyFatInfo.setVisibility(View.VISIBLE);

                if (result < 15) {
                    seekBarBodyFat.setProgressTintList(ContextCompat.getColorStateList(requireContext(), R.color.low_body_fat));
                    textViewBodyFatInfo.setText("Low Body Fat");
                    textViewBodyFatInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.low_body_fat));
                } else if (result < 25) {
                    seekBarBodyFat.setProgressTintList(ContextCompat.getColorStateList(requireContext(), R.color.normal_body_fat));
                    textViewBodyFatInfo.setText("Normal Body Fat");
                    textViewBodyFatInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
                } else {
                    seekBarBodyFat.setProgressTintList(ContextCompat.getColorStateList(requireContext(), R.color.high_body_fat));
                    textViewBodyFatInfo.setText("High Body Fat");
                    textViewBodyFatInfo.setTextColor(ContextCompat.getColor(requireContext(), R.color.high_body_fat));
                }
            }
        });

        buttonCalculate.setOnClickListener(v -> {
            String abdomenStr = editTextAbdomen.getText().toString();
            String weightStr = editTextWeight.getText().toString();
            if (!TextUtils.isEmpty(abdomenStr) && !TextUtils.isEmpty(weightStr)) {
                double abdomen = Double.parseDouble(abdomenStr);
                double weight = Double.parseDouble(weightStr);
                Log.d("BodyFatCalculatorFragment", "Calculating body fat with abdomen: " + abdomen + " and weight: " + weight);
                textViewResult.setVisibility(View.GONE);
                progressBar.setVisibility(View.VISIBLE);
                seekBarBodyFat.setVisibility(View.GONE);
                textViewBodyFatInfo.setVisibility(View.GONE);
                viewModel.calculateBodyFat(abdomen, weight);
            }
        });

        // initializare google maps si places
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        Places.initialize(requireContext(), getString(R.string.google_maps_key));
        placesClient = Places.createClient(requireContext());
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // broadcast receiver pentru momentul cand schimbi locatia
        locationReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().matches("android.location.PROVIDERS_CHANGED")) {
                    LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                    if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                        // refresh cand deschidem locatia
                        if (mMap != null) {
                            mMap.clear();
                            onMapReady(mMap);
                        }
                    }
                }
            }
        };

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter filter = new IntentFilter("android.location.PROVIDERS_CHANGED");
        requireContext().registerReceiver(locationReceiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        requireContext().unregisterReceiver(locationReceiver);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        // verificam permisiunile de locatie
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(requireActivity(), location -> {
            if (location != null) {
                LatLng currentLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15));

                // cautam sali de fitness in apropiere
                findNearbyGyms(currentLocation);
            }
        });
    }

    private void findNearbyGyms(LatLng location) {


        String apiKey = getString(R.string.google_maps_key);
        String url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="
                + location.latitude + "," + location.longitude
                + "&radius=3000&type=gym&key=" + apiKey; // raza de 5 km

        new FetchNearbyGymsTask(mMap).fetchGyms(url);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onMapReady(mMap);
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_refresh, menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            refreshContent();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshContent() {
        if (mMap != null) {
            mMap.clear();
            onMapReady(mMap);
            resetFields();
        }
    }

    private void resetFields() {
        editTextAbdomen.setText("");
        editTextWeight.setText("");
        textViewResult.setVisibility(View.GONE);
        progressBar.setVisibility(View.GONE);
        seekBarBodyFat.setVisibility(View.GONE);
        textViewBodyFatInfo.setVisibility(View.GONE);
    }
}
