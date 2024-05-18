package com.example.labsensor;

import android.os.Bundle;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements SensorEventListener, LocationListener {

    private SensorManager sensorManager;
    private Sensor accelerometer;
    private boolean fallDetected = false;
    private LocationManager locationManager;
    private static final int MIN_TIME_BW_UPDATES = 1000 * 60 * 1; // 1 minute
    private static final int MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters
    private double latitude;
    private double longitude;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            } else {
                Toast.makeText(this, "Accelerometer not available", Toast.LENGTH_SHORT).show();
            }
        }

        // Initialiser le LocationManager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (locationManager != null) {
            try {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
                Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnownLocation != null) {
                    latitude = lastKnownLocation.getLatitude();
                    longitude = lastKnownLocation.getLongitude();
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        // Mettre à jour les coordonnées GPS
        latitude = location.getLatitude();
        longitude = location.getLongitude();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float gravityX = event.values[0];
        float gravityY = event.values[1];
        float gravityZ = event.values[2];

        // Calculer l'accélération résultante
        double acceleration = Math.sqrt(gravityX * gravityX + gravityY * gravityY + gravityZ * gravityZ);

        // Détection de chute
        if (acceleration < 2) { // Valeur seuil de détection de chute, peut nécessiter un ajustement
            if (!fallDetected) {
                fallDetected = true;
                Toast.makeText(this, "Fall detected! Sending SMS.", Toast.LENGTH_SHORT).show();
                sendSMS("0650851911", "Help! I have fallen. My location: Latitude " + latitude + ", Longitude " + longitude);
            }
        } else {
            fallDetected = false;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private void sendSMS(String phoneNumber, String message) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("smsto:" + phoneNumber));
        intent.putExtra("sms_body", message);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Toast.makeText(getApplicationContext(), "No messaging app found.", Toast.LENGTH_SHORT).show();
        }
    }
}
