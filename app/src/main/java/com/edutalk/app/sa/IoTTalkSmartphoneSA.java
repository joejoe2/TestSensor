package com.edutalk.app.sa;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.edutalk.app.customUI.CustomUIFactory;
import com.edutalk.app.customUI.MutliDimesionDataText;
import com.edutalk.app.iottalk.IoTTalkDAI;
import com.edutalk.app.MainActivity;
import com.edutalk.app.R;
import com.edutalk.app.sensor.streamsensor.StreamSensorType;
import com.edutalk.app.sensor.streamsensor.StreamSensor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.UUID;
import java.util.stream.IntStream;

public class IoTTalkSmartphoneSA extends AppCompatActivity {
    String CSMEndpoint;
    final String DEVICE_MODEL = "Smartphone";
    final String DEVICE_NAME = "Android_"+UUID.randomUUID().toString().substring(0, 8);
    int sampleRate;
    HashSet<StreamSensorType> selectedSensorTypes;
    StreamSensor[] selectedSensors;
    IoTTalkDAI ioTTalkDai;

    Network originalNetwork;
    ConnectivityManager connectivityManager;
    ConnectivityManager.NetworkCallback onNetworkStateChangeCallback;

    Button exitButton;
    TextView deviceModelTexView, deviceNameTexView;
    ScrollView sensorDataView;
    LinearLayout sensorDataLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartphone_sa);
        getBundle();
        setUI();
        setListeners();
        setDAI();
        try {
            ioTTalkDai.start();
        } catch (Exception e) {
            e.printStackTrace();
            finish();
        }
    }

    /**
     * receive params from bundle
     */
    void getBundle(){
        Bundle bundle = this.getIntent().getExtras();
        CSMEndpoint = bundle.getString("CSMEndpoint", "http://iottalk2.tw/csm");

        selectedSensorTypes = (HashSet<StreamSensorType>) bundle.getSerializable("selectedSensors");
        if (selectedSensorTypes == null) selectedSensorTypes = new HashSet<StreamSensorType>();

        sampleRate = bundle.getInt("sampleRate", 0);
    }

    private void setUI(){
        exitButton = findViewById(R.id.exitButton);
        deviceModelTexView = findViewById(R.id.deviceModelTextView);
        deviceModelTexView.setText(DEVICE_MODEL);
        deviceNameTexView = findViewById(R.id.deviceNameTextView);
        deviceNameTexView.setText(DEVICE_NAME);
        sensorDataView = findViewById(R.id.scrollView);
        sensorDataLayout =findViewById(R.id.dataLayout);
    }

    private void setDAI(){
        selectedSensors = getSensors(selectedSensorTypes);
        ioTTalkDai = new IoTTalkDAI(this, CSMEndpoint, DEVICE_NAME, DEVICE_MODEL, selectedSensors);
    }

    private StreamSensor[] getSensors(HashSet<StreamSensorType> selectedSensors){
        ArrayList<StreamSensor> sensors = new ArrayList<>();

        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        for (StreamSensorType streamSensorType: selectedSensors) {
            //build ui
            MutliDimesionDataText dataText= CustomUIFactory.buildMutlDimesionDataText(this,
                    streamSensorType.getAlias()+" ( "+streamSensorType.getAcceptUnit()+" )", streamSensorType.getDataDimensionNames());
            sensorDataLayout.addView(dataText);

            //create sensor and update method
            StreamSensor sensor = new StreamSensor(streamSensorType.getAlias()+"-I", sensorManager, streamSensorType, sampleRate);
            sensor.setOnSensorSignalCallBack((float[] data)->{
                runOnUiThread(()->{
                    dataText.setValues(IntStream.range(0, data.length).mapToObj(i -> String.format("%.2f", data[i])).toArray(String[]::new));
                });
            });

            sensors.add(sensor);
        }

        return sensors.toArray(new StreamSensor[0]);
    }

    private void setListeners(){
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ioTTalkDai.stop();
                goBackToMainActivity();
            }
        });

        // if network state change, due to we has not use persistent binding
        // the register of dan will lost, so we cannot continue to push data
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        onNetworkStateChangeCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                if (originalNetwork==null){
                    originalNetwork = network;
                }else {
                    doOnStateChange();
                }
            }
            @Override
            public void onLost(Network network) {
                doOnStateChange();
            }

            private void doOnStateChange(){
                AlertDialog.Builder builder = new AlertDialog.Builder(IoTTalkSmartphoneSA.this);
                builder.setTitle("ERROR: network state change !");
                builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        goBackToMainActivity();
                    }
                });
                builder.setCancelable(false);
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        };
        connectivityManager.registerDefaultNetworkCallback(onNetworkStateChangeCallback);
    }

    @Override
    public void onStop() {
        for (StreamSensor sensor : selectedSensors) {
            sensor.stopSensing();
        }
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        for (StreamSensor sensor : selectedSensors) {
            sensor.startSensing();
        }
    }

    @Override
    public void onBackPressed() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("exit ?");
        builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                goBackToMainActivity();
            }
        });
        builder.setNegativeButton("no", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    protected void onDestroy() {
        ioTTalkDai.stop();
        super.onDestroy();
    }

    /**
     * go back to MainActivity
     */
    void goBackToMainActivity(){
        ioTTalkDai.stop();
        //clear connectivityManager
        connectivityManager.unregisterNetworkCallback(onNetworkStateChangeCallback);
        //back to MainActivity and clear history
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP );
        startActivity(intent);
    }
}
