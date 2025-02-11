package be.kuleuven.gt.nodenest.model;

import android.graphics.Color;
import android.widget.TextView;

//class contains all information concerning visiuals, and stored fields on chart activities
public class ChartActivityModel {
    private TextView sensorNameView; // display of sensor name
    private TextView statusView; // display of device status
    private TextView ValueView; // display of measurement value
    private TextView UnitsView; // display of units of measurement
    private String sensorName; // device name
    private String status; // device status
    private String Value; // measurement value
    private String Units; // measurement units

    public ChartActivityModel() {
    }

    // setting device name display
    public void setSensorName(String sensorName) {
        this.sensorName = sensorName;
        this.sensorNameView.setText(sensorName);
    }

    // setting device status display
    public void setStatus(String status) {
        this.status = status;
        this.statusView.setText(status);
        if (status.equals("Active"))
        {
            this.statusView.setTextColor(Color.GREEN);
        } else if (status.equals("Not Active")) {
            this.statusView.setTextColor(Color.RED);
        }
        else {
            this.statusView.setTextColor(Color.YELLOW);
        }
    }
    // setting device status display
    public void setStatus(SensorStatus status) {
        if (status == SensorStatus.ACTIVE)
        {
            this.status = "Active";
            this.statusView.setText(this.status);
            this.statusView.setTextColor(Color.GREEN);
        } else if (status == SensorStatus.NOTACTIVE) {
            this.status = "Not Active";
            this.statusView.setText(this.status);
            this.statusView.setTextColor(Color.RED);
        }
        else {
            this.statusView.setTextColor(Color.YELLOW);
        }
    }

    // setting display of measurement value
    public void setValue(String value) {
        Value = value;
        this.ValueView.setText(value);
    }
    // setting display of measurement value
    public void setValue(int value) {
        Value = String.valueOf(value);
        this.ValueView.setText(String.valueOf(value));
    }
    // setting display of measurement value
    public void setValue(float value) {
        Value = String.valueOf(value);
        this.ValueView.setText(String.valueOf(value));
    }

    // setting display of measurement units
    public void setUnits(String units) {
        Units = units;
        this.UnitsView.setText(units);
    }


    // setting device name display
    public void setSensorNameView(TextView sensorNameView) {
        this.sensorNameView = sensorNameView;
    }


    // setting device status display
    public void setStatusView(TextView statusView) {
        this.statusView = statusView;
    }


    // setting display of measurement value
    public void setValueView(TextView valueView) {
        ValueView = valueView;
    }


    // setting display of measurement units
    public void setUnitsView(TextView unitsView) {
        UnitsView = unitsView;
    }
}
