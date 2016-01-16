package com.example.werner_jensgrunert.jgcompass;

// http://android-developers.blogspot.co.uk/2011/06/deep-dive-into-location.html
// http://www.onlineconversion.com/map_greatcircle_bearings.htm
// http://stackoverflow.com/questions/3856340/what-scale-is-initial-bearing-in-returned-from-location-distancebetween
// http://stackoverflow.com/questions/5479753/using-orientation-sensor-to-point-towards-a-specific-location
// https://newcircle.com/s/post/98/compass_application_in_android
// http://www.vogella.com/tutorials/AndroidSensor/article.html

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.List;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;


public class MainActivity extends AppCompatActivity {
    private TextView Status;
    private TextView LatitudeGPS;
    private TextView LongitudeGPS;
    private TextView SpeedGPS;
    private TextView LatitudeDEST;
    private TextView LongitudeDEST;
    private TextView DistanceToDest;
    private TextView Bearing1;
    private TextView Bearing2;

    private SensorManager sensorManager;
    private SensorEventListener sensorListener;

    private LocationManager locationManager;
    private LocationListener locationListener;
    private String provider;
    private double LatitudeDestination = 45.8f;
    private double LongitudeDestination = 9.58f;
    float [] Distance = new float[10];

    static boolean jens = false;

    public boolean isPackageExisted(String targetPackage) {
        PackageManager pm = getPackageManager();
        try {
            PackageInfo info = pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
        return true;
    }

    private static final int USER_PERMISSION_REQUEST = 10;

    public void listPackages(View view) {
        TextView textView;
        textView = (TextView) findViewById(R.id.textViewPackages);
        textView.setMovementMethod(new ScrollingMovementMethod());
        List<PackageInfo> packages = getPackageManager().getInstalledPackages(0);

        for (PackageInfo pi : packages) {
            String packagename = pi.packageName;
            textView.append("" + packagename + "\n");
        }

        if (isPackageExisted("net.osmand.plus")) {
            Intent launchIntent = getPackageManager().getLaunchIntentForPackage("net.osmand.plus");
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(launchIntent);
        }
        // finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);


        Status = (TextView) findViewById(R.id.textViewStatus);
        LatitudeGPS = (TextView) findViewById(R.id.textViewLatGps);
        LongitudeGPS = (TextView) findViewById(R.id.textViewLongGps);
        SpeedGPS = (TextView) findViewById(R.id.textViewSpeedGps);
        LatitudeDEST = (TextView) findViewById(R.id.textViewLatDEST);
        LongitudeDEST = (TextView) findViewById(R.id.textViewLongDEST);
        DistanceToDest = (TextView) findViewById(R.id.textViewDIST);
        Bearing1 = (TextView) findViewById(R.id.textViewB1);
        Bearing2 = (TextView) findViewById(R.id.textViewB2);

        Intent intent = getIntent();
        String action = intent.getAction();
        String type = intent.getType();
        String data = intent.getDataString();

        if (action.equals("android.intent.action.VIEW") && data.matches("geo:.*")) {
            String s[] = data.split(":|,|\\?");
            LatitudeDestination = Float.valueOf(s[1].trim()).floatValue();
            LongitudeDestination = Float.valueOf(s[2].trim()).floatValue();
        }

        LatitudeDEST.setText(String.format("%.8f",LatitudeDestination));
        LongitudeDEST.setText(String.format("%.8f",LongitudeDestination));

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {

            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        provider = LocationManager.NETWORK_PROVIDER;

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                LatitudeGPS.setText(String.format("%.8f", location.getLatitude()));
                LongitudeGPS.setText(String.format("%.8f", location.getLongitude()));
                Status.setText(String.format("State: onLocationChanged, Position from : %s", location.getProvider()));
                if (location.hasSpeed()) {
                    SpeedGPS.setText(String.format("%.2f",location.getSpeed() * 3.6));
                } else {
                    SpeedGPS.setText("No Speed available.");
                }
                Location.distanceBetween(location.getLatitude(), location.getLongitude(), LatitudeDestination, LongitudeDestination, Distance);
                DistanceToDest.setText(String.format("%.2f", Distance[0] / 1000.0));
                Bearing1.setText(String.format("%.2f", Distance[1]));
                Bearing2.setText(String.format("%.2f", Distance[2]));
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {
                Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(intent);
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION,
                                Manifest.permission.INTERNET},
                        USER_PERMISSION_REQUEST);
                return;
            } else {
                jens = true;
            }
        }
        if (jens) {
            Location fastlocation = locationManager.getLastKnownLocation(provider);
            LatitudeGPS.setText(String.format("%.8f",fastlocation.getLatitude()));
            LongitudeGPS.setText(String.format("%.8f",fastlocation.getLongitude()));
            Status.setText(String.format("State: onCreate, Position from : %s",fastlocation.getProvider()));
            Location.distanceBetween(fastlocation.getLatitude(), fastlocation.getLongitude(), LatitudeDestination, LongitudeDestination, Distance);
            DistanceToDest.setText(String.format("%.2f",Distance[0]/1000.0));
            Bearing1.setText(String.format("%.2f", Distance[1]));
            Bearing2.setText(String.format("%.2f", Distance[2]));

            locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (jens) {
            locationManager.removeUpdates(locationListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (jens) {
            Location fastlocation = locationManager.getLastKnownLocation(provider);
            LatitudeGPS.setText(String.format("%.8f", fastlocation.getLatitude()));
            LongitudeGPS.setText(String.format("%.8f", fastlocation.getLongitude()));
            Status.setText(String.format("State: onResume, Position from : %s", fastlocation.getProvider()));
            Location.distanceBetween(fastlocation.getLatitude(), fastlocation.getLongitude(), LatitudeDestination, LongitudeDestination, Distance);
            DistanceToDest.setText(String.format("%.2f",Distance[0]/1000.0));
            Bearing1.setText(String.format("%.2f", Distance[1]));
            Bearing2.setText(String.format("%.2f", Distance[2]));

            locationManager.requestLocationUpdates("gps", 1000, 0, locationListener);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case USER_PERMISSION_REQUEST: {
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Status.append("Permission granted");
                    jens = true;
                } else {
                    Status.append("Permission denied");
                }
                return;
            }
        }
    }
}
