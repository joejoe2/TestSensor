package com.joejoe2.testsensor.sa;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.joejoe2.testsensor.customUI.MutliDimesionDataText;
import com.joejoe2.testsensor.customUI.SeekBarWithLabel;
import com.joejoe2.testsensor.edutalk.EduTalkDAI;
import com.joejoe2.testsensor.R;
import com.joejoe2.testsensor.sensor.SensorBase;
import com.joejoe2.testsensor.sensor.SensorType;
import com.joejoe2.testsensor.sensor.streamsensor.StreamSensor;
import com.joejoe2.testsensor.sensor.triggersensor.RangeSensor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EduTalkSmartphoneSA extends AppCompatActivity {
    String CSMEndpoint;
    String deviceName, deviceModel;
    String BIND_RC_URL;
    int sampleRate;

    HashMap<String, SensorType> selectedSensorTypes = new HashMap<>();
    SensorBase[] selectedSensors;
    EduTalkDAI dai;
    Network originalNetwork;
    ConnectivityManager connectivityManager;
    ConnectivityManager.NetworkCallback onNetworkStateChangeCallback;

    Button exitButton;
    TextView deviceModelTexView, deviceNameTexView;
    ScrollView dataView;
    LinearLayout dataLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.smartphone_sa);
        getBundle();
        setUI();
        setListeners();
        setDAI();
        try {
            dai.start();
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
        CSMEndpoint = bundle.getString("CSMEndpoint");
        deviceModel = bundle.getString("deviceModel");
        deviceName = bundle.getString("deviceName");
        selectedSensorTypes = (HashMap<String, SensorType>) bundle.getSerializable("selectedSensors");
        BIND_RC_URL = bundle.getString("BIND_RC_URL");
        sampleRate = bundle.getInt("sampleRate", 1);
    }

    private void setUI(){
        exitButton = findViewById(R.id.exitButton);
        deviceModelTexView = findViewById(R.id.deviceModelTextView);
        deviceModelTexView.setText(deviceModel);
        deviceNameTexView = findViewById(R.id.deviceNameTextView);
        deviceNameTexView.setText(deviceName);
        dataView = findViewById(R.id.scrollView);
        dataLayout=findViewById(R.id.dataLayout);
    }

    private void setDAI(){
        selectedSensors = getSensors(selectedSensorTypes);
        dai = new EduTalkDAI(CSMEndpoint, BIND_RC_URL, deviceName, deviceModel, selectedSensors);
    }

    private SensorBase[] getSensors(HashMap<String, SensorType> selectedSensorTypes){
        ArrayList<SensorBase> sensors = new ArrayList<>();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        selectedSensorTypes.forEach((String id, SensorType sensorType) -> {
            SensorBase sensor=null;
            if (sensorType.isStreamSensor()){
                MutliDimesionDataText dataText = buildMutlDimesionDataText(id+
                        " ( "+sensorType.getStreamSensorType().getUnit()+" )", sensorType.getStreamSensorType().getDataDimensions());
                sensor = new StreamSensor(id, sensorManager, sensorType, sampleRate);
                sensor.setOnSensorSignalCallBack((float[] data) -> {
                    runOnUiThread(()->{
                        dataText.setValues(IntStream.range(0, data.length)
                                .mapToObj(i -> String.format("%.2f", data[i]))
                                .toArray(String[]::new));
                    });
                });
            }else if (sensorType.isRangeSensor()){
                SeekBarWithLabel seekBarWithLabel = buildSeekBarWithLabel(id);
                float step=0.01f;
                int stepPrecision = (""+step).length()-1-(""+step).indexOf(".");
                sensor = new RangeSensor(id, seekBarWithLabel, sensorType, 0, 10, step, 5);
                sensor.setOnSensorSignalCallBack((float[] data) -> {
                    runOnUiThread(()->{
                        seekBarWithLabel.setValue(String.format("%."+stepPrecision+"f", data[0]));
                    });
                });
            }

            if (sensor!=null)sensors.add(sensor);
        });

        return sensors.toArray(new SensorBase[0]);
    }

    private MutliDimesionDataText buildMutlDimesionDataText(String title, String[] dimensionNames){
        MutliDimesionDataText dataText= new MutliDimesionDataText(this, title, dimensionNames, 18, 22, Color.WHITE);
        dataLayout.addView(dataText);
        return dataText;
    }

    private SeekBarWithLabel buildSeekBarWithLabel(String title){
        SeekBarWithLabel seekBar = new SeekBarWithLabel(this, title, 18, 22, Color.WHITE);
        dataLayout.addView(seekBar);
        return seekBar;
    }

    private void setListeners(){
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dai.stop();
                exit();
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
                AlertDialog.Builder builder = new AlertDialog.Builder(EduTalkSmartphoneSA.this);
                builder.setTitle("ERROR: network state change !");
                builder.setPositiveButton("yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.dismiss();
                        exit();
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
        for (SensorBase sensor : selectedSensors) {
            sensor.stopSensing();
        }
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        for (SensorBase sensor : selectedSensors) {
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
                dai.stop();
                exit();
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
        dai.stop();
        //clear connectivityManager
        connectivityManager.unregisterNetworkCallback(onNetworkStateChangeCallback);
        super.onDestroy();
    }

    /**
     * exit app
     */
    void exit(){
        //finishAffinity will not call on destroy onDestroy is for sys to call
        dai.stop();
        //clear connectivityManager
        connectivityManager.unregisterNetworkCallback(onNetworkStateChangeCallback);
        //exit
        finishAffinity();
    }
}
