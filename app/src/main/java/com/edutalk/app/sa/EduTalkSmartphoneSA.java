package com.edutalk.app.sa;

import android.content.Context;
import android.content.DialogInterface;
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
import com.edutalk.app.customUI.SeekBarWithLabel;
import com.edutalk.app.edutalk.EduTalkDAI;
import com.edutalk.app.R;
import com.edutalk.app.edutalk.EduTalkRCConfig;
import com.edutalk.app.sensor.BaseSensor;
import com.edutalk.app.sensor.BaseSensorType;
import com.edutalk.app.sensor.streamsensor.StreamSensor;
import com.edutalk.app.sensor.streamsensor.StreamSensorType;
import com.edutalk.app.sensor.triggersensor.RangeSensor;
import com.edutalk.app.sensor.triggersensor.TriggerSensorType;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.stream.IntStream;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EduTalkSmartphoneSA extends AppCompatActivity {
    EduTalkRCConfig eduTalkRCConfig;
    int sampleRate;

    HashMap<String, BaseSensorType> selectedSensorTypes = new HashMap<>();
    BaseSensor[] selectedSensors;
    EduTalkDAI dai;
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
        eduTalkRCConfig = (EduTalkRCConfig) bundle.getSerializable("eduTalkRCConfig");
        selectedSensorTypes = (HashMap<String, BaseSensorType>) bundle.getSerializable("selectedSensors");
        sampleRate = bundle.getInt("sampleRate", 1);
    }

    private void setUI(){
        exitButton = findViewById(R.id.exitButton);
        deviceModelTexView = findViewById(R.id.deviceModelTextView);
        deviceModelTexView.setText(eduTalkRCConfig.device_model);
        deviceNameTexView = findViewById(R.id.deviceNameTextView);
        deviceNameTexView.setText(eduTalkRCConfig.device_name);
        sensorDataView = findViewById(R.id.scrollView);
        sensorDataLayout =findViewById(R.id.dataLayout);
    }

    private void setDAI(){
        try {
            selectedSensors = getSensors(selectedSensorTypes);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dai = new EduTalkDAI(this, eduTalkRCConfig.csm_url, eduTalkRCConfig.rc_bind, eduTalkRCConfig.device_name, eduTalkRCConfig.device_model, selectedSensors);
    }

    private BaseSensor[] getSensors(HashMap<String, BaseSensorType> selectedSensorTypes) throws JSONException {
        ArrayList<BaseSensor> sensors = new ArrayList<>();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        JSONArray joins = new JSONArray(eduTalkRCConfig.joins);
        for(int i=0; i<joins.length();i++){
            JSONObject join = joins.getJSONObject(i);
            String idfName = join.getString("idf");
            String odfName = join.getString("odf").replace("-O", "");
            BaseSensorType baseSensorType = (BaseSensorType) selectedSensorTypes.get(idfName);
            BaseSensor sensor = null;

            if (baseSensorType instanceof StreamSensorType){
                StreamSensorType streamSensorType = (StreamSensorType) baseSensorType;
                //build ui
                MutliDimesionDataText dataText = CustomUIFactory.buildMutlDimesionDataText(this, idfName+
                        " ( "+streamSensorType.getAcceptUnit()+" )", streamSensorType.getDataDimensions());
                sensorDataLayout.addView(dataText);

                //create sensor and update ui method
                sensor = new StreamSensor(idfName, sensorManager, streamSensorType, sampleRate);
                sensor.setOnSensorSignalCallBack((float[] data) -> runOnUiThread(() ->
                        dataText.setValues(IntStream.range(0, data.length)
                                .mapToObj(index -> String.format("%.2f", data[index]))
                                .toArray(String[]::new))));

            }else if (baseSensorType instanceof TriggerSensorType){
                TriggerSensorType triggerSensorType = (TriggerSensorType) baseSensorType;
                if (triggerSensorType == TriggerSensorType.RangeSlider){
                    //build ui
                    SeekBarWithLabel seekBarWithLabel = CustomUIFactory.buildSeekBarWithLabel(this, odfName);
                    sensorDataLayout.addView(seekBarWithLabel);

                    //create sensor and update ui method
                    float step=0.01f; //need to change !
                    int stepPrecision = (""+step).length()-1-(""+step).indexOf(".");
                    sensor = new RangeSensor(idfName, seekBarWithLabel, triggerSensorType,
                            join.getInt("min"), join.getInt("max"), step, join.getInt("default"));
                    sensor.setOnSensorSignalCallBack((float[] data) -> runOnUiThread(()-> seekBarWithLabel.setValue(String.format("%."+stepPrecision+"f", data[0]))));
                }else{
                    //add new TriggerSensor here ...
                }
            }else{
                //add new sensor type here...
            }
            if (sensor!=null)sensors.add(sensor);
        }

        return sensors.toArray(new BaseSensor[0]);
    }

    private void setListeners(){
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
        for (BaseSensor sensor : selectedSensors) {
            sensor.stopSensing();
        }
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        for (BaseSensor sensor : selectedSensors) {
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

    private boolean hasDestroy;

    @Override
    protected void onDestroy() {
        if (!hasDestroy){
            dai.stop();
            //clear connectivityManager
            connectivityManager.unregisterNetworkCallback(onNetworkStateChangeCallback);
            hasDestroy=true;
        }
        super.onDestroy();
    }

    void exit(){
        if (!hasDestroy){
            dai.stop();
            //clear connectivityManager
            connectivityManager.unregisterNetworkCallback(onNetworkStateChangeCallback);
            hasDestroy=true;
        }
        finishAndRemoveTask();
    }
}
