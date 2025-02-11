package be.kuleuven.gt.nodenest.model;

import android.content.Context;
import android.devicelock.DeviceId;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// class stores all the information about the iot device
public class IotDevice implements Parcelable {

    private String measurement;
    private int deviceId;
    private String mqttTopic;
    private String deviceType;
    private String deviceName;
    private int userId;
    private String unit;
    private boolean unitFetched = false;
    private final Object unitLock = new Object();
    private int status;

    public IotDevice(int deviceId, String mqttTopic, String deviceType, int userId, String deviceName, int status, String measurement, String unit) {
        this.deviceId = deviceId;
        this.mqttTopic = mqttTopic;
        this.deviceType = deviceType;
        this.userId = userId;
        this.deviceName = deviceName;
        this.status = status;
        this.measurement = measurement;
        this.unit = unit;
    }
    public void setUnit(String unit) {
        this.unit = unit;
    }
    public static final Creator<IotDevice> CREATOR = new Creator<IotDevice>() {
        @Override
        public IotDevice createFromParcel(Parcel in) {
            return new IotDevice(in);
        }

        @Override
        public IotDevice[] newArray(int size) {
            return new IotDevice[size];
        }
    };
    public int getDeviceId() {
        return deviceId;
    }
    public String getDeviceType() {
        return deviceType;
    }
    public int getUserId() {
        return userId;
    }
    public String getUnit() {
        return unit;
    }
    public void fetchAndSetUnit(Context context) {
        if (!unitFetched) {
            String url = "https://studev.groept.be/api/a23PT103/getUnit/" + deviceId;
            RequestQueue requestQueue = Volley.newRequestQueue(context);
            JsonArrayRequest getRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            try {
                                if (response.length() > 0) {
                                    JSONObject unitObject = response.getJSONObject(0);
                                    String fetchedUnit = unitObject.getString("unit").replace("\\u00b0", "°");
                                    unit = fetchedUnit;
                                    unitFetched = true;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            error.printStackTrace();
                        }
                    });
            requestQueue.add(getRequest);
        }
    }
    public void setStatus(int status) {
        this.status = status;
    }
    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }
    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
    public String getMeasurement() {
        return measurement;
    }
    public int getStatus() {
        return status;
    }
    public IotDevice(JSONObject o) {
        try {
            deviceId = o.getInt("deviceId");
            mqttTopic = o.getString("mqttTopic");
            deviceType = o.getString("deviceType");
            userId = o.getInt("userId");
            deviceName = o.getString("deviceName");
            status = o.getInt("status");
            unit = o.getString("unit");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
    public String getDeviceName() {
        return deviceName;
    }
    protected IotDevice(Parcel in) {
        measurement = in.readString();
        deviceId = in.readInt();
        mqttTopic = in.readString();
        deviceType = in.readString();
        deviceName = in.readString();
        userId = in.readInt();
        status = in.readInt();
        unit = in.readString();
    }
    public String getMqttTopic() {
        return mqttTopic;
    }
    public void setMeasurement(String measurement) {
        this.measurement = measurement;
    }
    public void setMqttTopic(String mqttTopic) {
        this.mqttTopic = mqttTopic;
    }
    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(measurement);
        dest.writeInt(deviceId);
        dest.writeString(mqttTopic);
        dest.writeString(deviceType);
        dest.writeString(deviceName);
        dest.writeInt(userId);
        dest.writeInt(status);
        dest.writeString(unit);
    }
}
