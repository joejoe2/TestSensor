package com.edutalk.app.sa;

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

import com.edutalk.app.customUI.CustomUIFactory;
import com.edutalk.app.customUI.DataText;
import com.edutalk.app.customUI.MultiDimensionDataText;
import com.edutalk.app.customUI.SeekBar;
import com.edutalk.app.edutalk.EduTalkDAI;
import com.edutalk.app.R;
import com.edutalk.app.edutalk.EduTalkRCConfig;
import com.edutalk.app.sensor.BaseSensor;
import com.edutalk.app.sensor.streamsensor.StreamSensor;
import com.edutalk.app.sensor.streamsensor.StreamSensorType;
import com.edutalk.app.sensor.triggersensor.RangeSensor;
import com.edutalk.app.sensor.triggersensor.TriggerSensorType;
import com.edutalk.app.sensor.unsupportsensor.UnSupportSensor;
import com.edutalk.app.sensor.unsupportsensor.UnSupportSensorType;
import com.edutalk.app.utils.DialogQueue;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.stream.IntStream;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class EduTalkSmartphoneSA extends AppCompatActivity {
    EduTalkRCConfig eduTalkRCConfig;
    int sampleRate;

    HashMap<String, BaseSensor> selectedSensors;
    EduTalkDAI dai;
    Network originalNetwork;
    ConnectivityManager connectivityManager;
    ConnectivityManager.NetworkCallback onNetworkStateChangeCallback;

    Button exitButton;
    ScrollView sensorDataView;
    LinearLayout dataLayout, sensorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.edutalk_smartphone_sa);
        CustomUIFactory.setFontColor(Color.BLACK);
        getBundle();
        setUI();
        setListeners();
        setDAI();
        try {
            dai.start();
        } catch (Exception e) {
            e.printStackTrace();
            terminateWithError();
        }
    }

    /**
     * receive params from bundle
     */
    void getBundle(){
        Bundle bundle = this.getIntent().getExtras();
        eduTalkRCConfig = (EduTalkRCConfig) bundle.getSerializable("eduTalkRCConfig");
        sampleRate = bundle.getInt("sampleRate", 1);
    }

    private void setUI(){
        exitButton = findViewById(R.id.exitButton);
        sensorDataView = findViewById(R.id.scrollView);
        dataLayout = findViewById(R.id.dataLayout);
        sensorLayout = findViewById(R.id.sensorLayout);
    }

    private void setDAI(){
        try {
            selectedSensors = getSensors(eduTalkRCConfig);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        dai = new EduTalkDAI(this, eduTalkRCConfig.csm_url, eduTalkRCConfig.rc_bind, eduTalkRCConfig.bindCallbacks, eduTalkRCConfig.deviceName, eduTalkRCConfig.deviceModel, selectedSensors);
    }

    private HashMap<String, BaseSensor> getSensors(EduTalkRCConfig rcConfig) throws JSONException {
        JSONArray idfs = rcConfig.getIdfs();
        JSONArray iv_list = rcConfig.getIv_list();

        HashSet<StreamSensorType> selectStreamSensor = new HashSet<>();
        HashMap<String, BaseSensor> sensors = new HashMap<>();
        HashMap<String, List<DataText>> dataTexts=new HashMap<>();
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        for (int i=0;i<iv_list.length();i++){
            String displayName = iv_list.getJSONObject(i).getString("giv_name")+iv_list.getJSONObject(i).get("index");
            JSONArray params=iv_list.getJSONObject(i).getJSONArray("params");

            for (int j=0;j<params.length();j++){
                JSONObject param = params.getJSONObject(j);
                String idfName = getIdfName(idfs, iv_list, i, j);
                BaseSensor sensor = null;

                //build label for odf
                if (j==0)dataLayout.addView(CustomUIFactory.buildLabel(this, displayName));

                String device = param.optString("device", "");
                //build sensor
                if ("Smartphone".equals(device)){
                    StreamSensorType sensorType=null;
                    for (StreamSensorType streamSensorType:StreamSensorType.values()){
                        if(streamSensorType.getAlias().equals(param.getString("sensor"))){
                            sensorType = streamSensorType;
                            sensorType.setNeedTimestamp(true);
                            break;
                        }
                    }
                    if(sensorType==null)continue;
                    //build ui
                    String function = param.getString("function");
                    DataText dataText = CustomUIFactory.buildDataText(this);
                    dataText.setValues("Using "+sensorType.getAlias()+" Sensor with function "+function);
                    List<DataText> texts = dataTexts.getOrDefault(idfName, new ArrayList<>());
                    texts.add(dataText);
                    dataTexts.put(idfName, texts);
                    dataLayout.addView(dataText);
                    //build sensor
                    sensor = sensors.getOrDefault(idfName, new StreamSensor(idfName, sensorManager, sensorType, sampleRate));
                    //build additional ui
                    if (!selectStreamSensor.contains(sensorType)){
                        selectStreamSensor.add(sensorType);

                        MultiDimensionDataText mutliDimesionDataText = CustomUIFactory.buildMutlDimesionDataText(this,
                                sensorType.getAlias()+" ( "+sensorType.getAcceptUnit()+" )", sensorType.getDataDimensionNames());
                        sensorLayout.addView(mutliDimesionDataText);
                        sensor.setOnSensorSignalCallBack((float[] data) -> runOnUiThread(() ->{
                            mutliDimesionDataText.setValues(IntStream.range(0, data.length)
                                    .mapToObj(index -> String.format("%.2f", data[index])).toArray(String[]::new));
                        }));
                    }
                }else if(TriggerSensorType.RangeSlider.getAlias().equals(device)){
                    float step=0.01f; // configurable ?
                    int stepPrecision = (""+step).length()-1-(""+step).indexOf(".");
                    //build ui
                    SeekBar seekBar = CustomUIFactory.buildSeekBar(this);
                    seekBar.setValue(String.format("%."+stepPrecision+"f", (float)param.getDouble("default")));
                    dataLayout.addView(seekBar);
                    TriggerSensorType sensorType = TriggerSensorType.RangeSlider;
                    sensorType.setNeedTimestamp(true);
                    sensorType.setNeedDfName(true);
                    //build sensor
                    sensor = new RangeSensor(idfName, seekBar, sensorType,
                            (float)param.getDouble("min"), (float)param.getDouble("max"), step, (float)param.getDouble("default"));
                    sensor.setOnSensorSignalCallBack((float[] data) -> runOnUiThread(()-> seekBar.setValue(String.format("%."+stepPrecision+"f", data[0]))));
                }else {
                    UnSupportSensorType sensorType;
                    if(UnSupportSensorType.InputBox.getAlias().equals(device)){
                        //build sensor
                        sensorType=UnSupportSensorType.InputBox;
                        sensorType.setNeedTimestamp(true);
                        sensorType.setNeedDfName(true);
                        sensor = sensors.getOrDefault(idfName, new UnSupportSensor(idfName, sensorType));
                        ((UnSupportSensor)sensor).getDefaultVals().add(param.get("default"));
                        //build ui
                        DataText dataText = CustomUIFactory.buildDataText(this);
                        dataText.setValues(sensorType.getAlias()+" is not supported now");
                        dataLayout.addView(dataText);
                    }else if (UnSupportSensorType.Morsensor.getAlias().equals(device)){
                        //build sensor
                        sensorType=UnSupportSensorType.Morsensor;
                        sensorType.setNeedTimestamp(true);
                        sensor = sensors.getOrDefault(idfName, new UnSupportSensor(idfName, sensorType));
                        ((UnSupportSensor)sensor).getDefaultVals().add((float) param.getDouble("default"));
                        //build ui
                        DataText dataText = CustomUIFactory.buildDataText(this);
                        dataText.setValues(sensorType.getAlias()+" is not supported now");
                        dataLayout.addView(dataText);
                    }else{
                        //build ui
                        DataText dataText = CustomUIFactory.buildDataText(this);
                        dataText.setValues("Using "+device+" "+param.optString("sensor", "sensor")+" Sensor on EduTalk");
                        dataLayout.addView(dataText);
                    }
                }

                if(sensor!=null)sensors.put(idfName, sensor);
            }
        }

        return sensors;
    }

    private String getIdfName(JSONArray idfs, JSONArray iv_list, int ivIndex, int paramIndex) throws JSONException {
        int idfIndex = paramIndex;
        for (int i = 0; i < ivIndex; i += 1) {
            idfIndex += iv_list.getJSONObject(i).getJSONArray("params").length();
        }
        return idfs.getJSONArray(idfIndex).getString(0).replace("_", "-");
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
        for (BaseSensor sensor : selectedSensors.values()) {
            sensor.stopSensing();
        }
        super.onStop();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        for (BaseSensor sensor : selectedSensors.values()) {
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

    private void terminateWithError(){
        android.app.AlertDialog.Builder builder=new android.app.AlertDialog.Builder(this);
        builder.setTitle("there are some errors occur !");
        builder.setPositiveButton("exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                exit();
            }
        });
        builder.setCancelable(false);
        DialogQueue.showDialog(builder.create());
    }
}
