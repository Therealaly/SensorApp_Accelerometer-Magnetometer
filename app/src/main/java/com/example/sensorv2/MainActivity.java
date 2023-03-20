package com.example.sensorv2;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Text;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager mSensorManager;

    private Sensor mSensorAccelerometer;
    private Sensor mSensorMagnetometer;

    private TextView mTextSensorAzimuth;
    private TextView mTextSensorPitch;
    private TextView mTextSensorRoll;

    private float[] mAccelerometerData = new float[3];
    private float[] mMagnetometerData = new float[3];

    private static final float VALUE_DRIFT = 0.05f;

    private ImageView nSpotTop;
    private ImageView nSpotBottom;
    private ImageView nSpotRight;
    private ImageView nSpotLeft;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT); // setting orientasi potrait secara deault.
        mTextSensorAzimuth = findViewById(R.id.value_azimuth);
        mTextSensorPitch = findViewById(R.id.value_pitch);
        mTextSensorRoll = findViewById(R.id.value_roll);

        // mendapatkan sensor accelerometer dan magnetometer untuk membaca ruangan 3D yg terjadi pada HP.
        // Karena akan membaca sumbu X, Y, dan Z
        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // assign variabel spot dengan objek spot
        nSpotTop = findViewById(R.id.spot_top);
        nSpotBottom = findViewById(R.id.spot_bottom);
        nSpotLeft = findViewById(R.id.spot_left);
        nSpotRight = findViewById(R.id.spot_right);


    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorAccelerometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        if (mSensorAccelerometer != null) {
            mSensorManager.registerListener(this, mSensorMagnetometer,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        mSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        int sensorType = sensorEvent.sensor.getType();
        switch (sensorType){
            case Sensor.TYPE_ACCELEROMETER:
                mAccelerometerData = sensorEvent.values.clone(); //secara otomatis akan mengisi array dgn value dari accelerometer
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                mMagnetometerData = sensorEvent.values.clone(); //secara otomatis akan mengisi array dgn value dari magnetometer
                break;
            default:
                return;
        }
        // fungsi untuk menggenerate angka dari accelerometer dan magnetometerdata menjadi rotation matrix
        // rotation matrix berfungsi sbg matrix 2D yg mentranslate koordinat relatif dari device
        // menjadi relative terhadap bumi. intinya deteksi position

        float[] rotationMatrix = new float[9];
        boolean rotationOK = SensorManager.getRotationMatrix(rotationMatrix, null,
                mAccelerometerData, mMagnetometerData);

        // dalam rotation matrix ada 3 sumbu yaitu X,Y,Z
        // X pointing ke arah kiri dan kanan HP
        // Y pointing ke arah north
        // Z pointing ke arah langit dan tegak lurus dgn perangkat

        float[] orientationValues = new float[3];
        if (rotationOK) {
            SensorManager.getOrientation(rotationMatrix, orientationValues);
        }
        // orientation dibagi mnjd azimuth, pitch, roll
        // azimuth represents -z axis hp dgn north pole
        // pitch represents x axis hp sejajar dgn tanah
        // roll reperesents y axis hp dengan dgn tanah


        // mendapat angka2 sensor utk ditampilkan dan menampilkan spot-spot
        float azimuth = orientationValues[0];
        float pitch = orientationValues[1];
        float roll = orientationValues[2];

        mTextSensorRoll.setText(getResources().getString(R.string.value_format, roll));
        mTextSensorPitch.setText(getResources().getString(R.string.value_format, pitch));
        mTextSensorAzimuth.setText(getResources().getString(R.string.value_format, azimuth));

        if (Math.abs(pitch) < VALUE_DRIFT){
            pitch = 0;
        }
        if (Math.abs(roll) < VALUE_DRIFT){
            roll = 0;
        }

        // .setAlpha() berfungsi untuk mengatur transparansi warna objek
        nSpotTop.setAlpha(0f);
        nSpotRight.setAlpha(0f);
        nSpotLeft.setAlpha(0f);
        nSpotBottom.setAlpha(0f);

        if (pitch > 0){
            nSpotTop.setAlpha(pitch); //ketika pitch up maka spot atas akan terang
        } else {
            nSpotBottom.setAlpha(Math.abs(pitch));
        }
        if (roll > 0){
            nSpotRight.setAlpha(roll); // ketika roll right maka spot right akan terang
        } else {
            nSpotLeft.setAlpha(Math.abs(roll));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}