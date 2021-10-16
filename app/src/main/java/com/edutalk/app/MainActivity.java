package com.edutalk.app;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import com.edutalk.app.iottalk.IoTTalkDAI;
import com.edutalk.app.sensor.streamsensor.StreamSensor;
import com.edutalk.app.sensor.streamsensor.StreamSensorType;
import com.edutalk.app.sa.IoTTalkSmartphoneSA;
import com.edutalk.app.utils.PermissionManager;

import java.util.ArrayList;
import java.util.HashSet;

public class MainActivity extends AppCompatActivity {
    final Integer[] SAMPLE_RATES = new Integer[]{1, 10, 25, 50, 100, 200, 300, 400};
    String CSMEndpoint = "https://iottalk2.tw/csm";//"https://publicedu.iottalk.tw/iottalk/csm";//"https://phyedu.iottalk.tw/iottalk/csm";//"https://iottalk2.tw/csm"; // default csm

    EditText csmEditText;
    Button startSAButton;
    CheckBox accCheckBox, gyrCheckBox, oriCheckBox;
    Spinner sampleRateSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setUI();
        PermissionManager.requestPermissions(this, PermissionManager.getLackingPermissions(this));
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
                ArrayList<String> lackingPermissions = PermissionManager.getLackingPermissions(MainActivity.this);
                if (lackingPermissions.size()>0){
                    PermissionManager.requestPermissionsInSetting(MainActivity.this, lackingPermissions);
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
            selectedSensors.add(StreamSensorType.Acceleration);
        }
        if(gyrCheckBox.isChecked()){
            selectedSensors.add(StreamSensorType.Gyroscope);
        }
        if(oriCheckBox.isChecked()){
            selectedSensors.add(StreamSensorType.Orientation);
        }
        return selectedSensors;
    }
}
