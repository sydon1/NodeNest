package be.kuleuven.gt.nodenest.controller;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import java.util.List;

import be.kuleuven.gt.nodenest.R;
import be.kuleuven.gt.nodenest.model.IotDevice;
import be.kuleuven.gt.nodenest.view.AddingDeviceActivity;
import be.kuleuven.gt.nodenest.view.LoginActivity;
import be.kuleuven.gt.nodenest.view.SensorLightActivity;
import be.kuleuven.gt.nodenest.view.SensorPressureActivity;
import be.kuleuven.gt.nodenest.view.SensorTempActivity;

public class IotDeviceAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_FOOTER = 1;
    private final List<IotDevice> deviceList;
    private final Context context;

    public IotDeviceAdapter(Context context, List<IotDevice> deviceList) {
        this.deviceList = deviceList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == deviceList.size()) {
            return VIEW_TYPE_FOOTER;
        } else {
            return VIEW_TYPE_ITEM;
        }
    }

    @Override
    public int getItemCount() {
        return deviceList.size() + 1;  // Plus one for the footer
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_FOOTER) {
            View footerView = LayoutInflater.from(parent.getContext()).inflate(R.layout.footer_button, parent, false);
            return new FooterViewHolder(footerView);
        } else {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.devices_view, parent, false);
            return new ItemViewHolder(itemView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof ItemViewHolder) {
            IotDevice iotDevice = deviceList.get(position);
            ItemViewHolder itemViewHolder = (ItemViewHolder) holder;
            itemViewHolder.deviceName.setText(iotDevice.getDeviceName());
            iotDevice.fetchAndSetUnit(context);
            itemViewHolder.latestMeasurement.setText(iotDevice.getMeasurement() + " " + iotDevice.getUnit());

            itemViewHolder.deviceStatus.setText(iotDevice.getStatus() == 1 ? "Active" : "Not Active");
            itemViewHolder.actionButton.setOnClickListener(v -> {
                Intent intent;
                switch (iotDevice.getDeviceType()) {
                    case "Temperature Sensor":
                        intent = new Intent(context, SensorTempActivity.class);
                        break;
                    case "Light Sensor":
                        intent = new Intent(context, SensorLightActivity.class);
                        break;
                    case "Pressure Sensor":
                        intent = new Intent(context, SensorPressureActivity.class);
                        break;
                    default:
                        intent = null;
                }
                if (intent != null) {
                    intent.putExtra("iotDevice", iotDevice);
                    context.startActivity(intent);
                }
            });
        } else if (holder instanceof FooterViewHolder) {
            FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
            footerViewHolder.addButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, AddingDeviceActivity.class);
                intent.putExtra("userId", deviceList.get(0).getUserId());  // Pass userId of the first device
                context.startActivity(intent);

            });
            footerViewHolder.logoutButton.setOnClickListener(v -> {
                Intent intent = new Intent(context, LoginActivity.class);
                context.startActivity(intent);
            });
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView latestMeasurement;
        TextView deviceStatus;
        Button actionButton;

        ItemViewHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.deviceName);
            latestMeasurement = itemView.findViewById(R.id.latestMeasurement);
            deviceStatus = itemView.findViewById(R.id.deviceStatus);
            actionButton = itemView.findViewById(R.id.actionButton);
        }
    }

    static class FooterViewHolder extends RecyclerView.ViewHolder {
        Button addButton;
        Button logoutButton;

        FooterViewHolder(View footerView) {
            super(footerView);
            addButton = footerView.findViewById(R.id.btnAddDevice);
            logoutButton = footerView.findViewById(R.id.btnLogout);
        }
    }
}
