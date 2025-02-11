package be.kuleuven.gt.nodenest.model;

import java.sql.Timestamp;

// class containing measurement information
public class Measurement {
    private int deviceId; // id of the device measurement was taken from
    private float measurement; // getting measurement value
    private Timestamp dateTimme; // time of obtaining measurement

    public Measurement(int deviceId, float measurement, Timestamp dateTimme) {
        this.deviceId = deviceId;
        this.measurement = measurement;
        this.dateTimme = dateTimme;
    }

    // getting measurement value
    public float getMeasurement() {
        return measurement;
    }
}
