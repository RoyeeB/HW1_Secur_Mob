package com.example.hw1_secur_mob;


import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.location.Location;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import androidx.activity.EdgeToEdge;

import android.Manifest;


public class SignInActivity extends AppCompatActivity {

    private Button signin_BTN;
    private TextView place1;
    private TextView place2;
    private TextView place3;
    private TextView place4;
    private TextView place5;
    private int clickCounter = 0;
    private FusedLocationProviderClient fusedLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_sign_in);

        findview();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        } else {
            getCurrentLocation(this::handleLocationResult);
        }
        handler.post(runnable);
        clickedSignIn();
    }

    private void findview() {
        signin_BTN = findViewById(R.id.signin_BTN);
        place1 = findViewById(R.id.place1);
        place2 = findViewById(R.id.place2);
        place3 = findViewById(R.id.place3);
        place4 = findViewById(R.id.place4);
        place5 = findViewById(R.id.place5);

    }

    private boolean checkAll() {
        return (isBluetoothEnabled() && clickCounter >= 3 && getScreenBrightness(this) >= 255 && !isConnectedToWiFi(this) && isBackgroundGreen(place4));
    }

    private void clickedSignIn() {
        signin_BTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (checkAll()) {
                    startActivity(new Intent(SignInActivity.this, SuccessActivity.class));
                } else
                    clickCounter++;
            }
        });
    }


    public boolean isBluetoothEnabled() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            return false;
        }
        return bluetoothAdapter.isEnabled();
    }

    public void setFirstColors() {
        if (isBluetoothEnabled())
            place1.setBackgroundColor(Color.GREEN);
        else
            place1.setBackgroundColor(Color.RED);

        if (clickCounter >= 3)
            place2.setBackgroundColor(Color.GREEN);
        else
            place2.setBackgroundColor(Color.RED);

        if (getScreenBrightness(this) >= 255)
            place3.setBackgroundColor(Color.GREEN);
        else
            place3.setBackgroundColor(Color.RED);

        getCurrentLocation(this::handleLocationResult);


        if (!isConnectedToWiFi(this))
            place5.setBackgroundColor(Color.GREEN);
        else
            place5.setBackgroundColor(Color.RED);
    }


    Handler handler = new Handler(Looper.getMainLooper());
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            updateUI();
            handler.postDelayed(this, 1000);
        }
    };

    private void updateUI() {
        setFirstColors();
    }


    public int getScreenBrightness(Context context) {
        try {
            ContentResolver contentResolver = context.getContentResolver();
            return Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }



    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation(this::handleLocationResult);
            } else {
                Toast.makeText(this, "הרשאה למיקום לא ניתנה", Toast.LENGTH_SHORT).show();
            }
        }
    }

    //
    @SuppressLint("MissingPermission")
    private void getCurrentLocation(LocationCallback callback) {
        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        String locationMessage = "Latitude: " + latitude + ", Longitude: " + longitude;
                        Toast.makeText(this, locationMessage, Toast.LENGTH_SHORT).show();
                        boolean isInIsrael = isInIsrael(latitude, longitude);
                        callback.onLocationReceived(isInIsrael);
                    } else {
                        callback.onLocationReceived(false);
                    }

                });
    }

    private boolean isInIsrael(double latitude, double longitude) {
        return latitude >=  30.361894177754692 && latitude <= 32.80416247784227 && longitude >=  34.472174728244966 && longitude <= 35.52502205204172;

    }


    private void handleLocationResult(boolean isInIsrael) {

        if (isInIsrael) {
            place4.setBackgroundColor(Color.GREEN);
        } else {
            place4.setBackgroundColor(Color.RED);
        }
    }


    public interface LocationCallback {
        void onLocationReceived(boolean isInIsrael);
    }

    public boolean isConnectedToWiFi(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();

            if (activeNetwork != null && activeNetwork.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

    public boolean isBackgroundGreen(View place4) {
        if (place4.getBackground() instanceof ColorDrawable) {
            ColorDrawable colorDrawable = (ColorDrawable) place4.getBackground();
            int backgroundColor = colorDrawable.getColor();

            return backgroundColor == Color.GREEN;
        }
        return false;
    }


}