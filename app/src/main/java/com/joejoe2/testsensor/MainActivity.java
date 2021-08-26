package com.joejoe2.testsensor;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.joejoe2.testsensor.iottalk.IoTTalkDAI;
import com.joejoe2.testsensor.sensor.streamsensor.StreamSensor;
import com.joejoe2.testsensor.sensor.streamsensor.StreamSensorType;
import com.joejoe2.testsensor.sa.IoTTalkSmartphoneSA;
import com.joejoe2.testsensor.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    SensorManager sensorManager;
    StreamSensor accelerationSensor, gyroscopeSensor, orientationSensor;
    final Integer[] SAMPLE_RATES = new Integer[]{1, 5, 10, 100, 200};
    String CSMEndpoint = "https://iottalk2.tw/csm";//"https://publicedu.iottalk.tw/iottalk/csm";//"https://phyedu.iottalk.tw/iottalk/csm";//"https://iottalk2.tw/csm"; // default csm
    IoTTalkDAI ioTTalkDai;

    EditText csmEditText;
    Button startSAButton;
    CheckBox accCheckBox, gyrCheckBox, oriCheckBox;
    Spinner sampleRateSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUI();
        PermissionUtils.requestPermissions(this, PermissionUtils.getLackingPermissions(this));
        setListeners();
    }

    private void setUI(){
        startSAButton = findViewById(R.id.startSAButton);
        accCheckBox = findViewById(R.id.accCheckBox);
        gyrCheckBox = findViewById(R.id.gyrCheckBox);
        oriCheckBox = findViewById(R.id.oriCheckBox);
        sampleRateSpinner = findViewById(R.id.sampleRateSpinner);
        sampleRateSpinner.setAdapter(new ArrayAdapter<Integer>(MainActivity.this,
                android.R.layout.simple_spinner_dropdown_item, SAMPLE_RATES));
        sampleRateSpinner.setSelection(0);
        csmEditText = findViewById(R.id.csmEditText);
        csmEditText.setText(CSMEndpoint);
    }

    private void setListeners(){
        startSAButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ArrayList<String> lackingPermissions = PermissionUtils.getLackingPermissions(MainActivity.this);
                if (lackingPermissions.size()>0){
                    PermissionUtils.requestPermissionsInSetting(MainActivity.this, lackingPermissions);
                    return;
                }

                HashSet<StreamSensorType> selectedSensors = getSelectedSensors();
                if (selectedSensors.size()>0){
                    CSMEndpoint = csmEditText.getText().toString().trim();

                    Intent saActivity=new Intent().setClass(MainActivity.this, IoTTalkSmartphoneSA.class);
                    Bundle bundle = new Bundle();
                    bundle.putString("CSMEndpoint", CSMEndpoint);
                    bundle.putSerializable("selectedSensors", selectedSensors);
                    bundle.putInt("sampleRate", (Integer) sampleRateSpinner.getSelectedItem());
                    saActivity.putExtras(bundle);
                    startActivity(saActivity);
                }
            }
        });
    }

    private HashSet<StreamSensorType> getSelectedSensors(){
        HashSet<StreamSensorType> selectedSensors=new HashSet<>();
        if(accCheckBox.isChecked()){
            selectedSensors.add(StreamSensorType.Acceleration_I);
        }
        if(gyrCheckBox.isChecked()){
            selectedSensors.add(StreamSensorType.Gyroscope_I);
        }
        if(oriCheckBox.isChecked()){
            selectedSensors.add(StreamSensorType.Orientation_I);
        }
        return selectedSensors;
    }
}
