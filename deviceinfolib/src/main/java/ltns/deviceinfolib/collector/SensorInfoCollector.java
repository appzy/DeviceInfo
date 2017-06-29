package ltns.deviceinfolib.collector;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;

import java.util.ArrayList;
import java.util.List;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/26.
 * Email: gu.yuepeng@foxmail.com
 */

public class SensorInfoCollector extends BaseDeviceInfoCollector {

    public SensorInfoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

    //传感器名列表
    private List<String> mSensorNames = new ArrayList<>();

    @Override
    public boolean needCollectManually() {
        return false;
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[0];
    }

    @Override
    protected void doCollectAutomatically() {
startCollectSensor();
    }

    private void startCollectSensor() {
        SensorManager mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensors = mSensorManager.getSensorList(Sensor.TYPE_ALL);
            for (int i = 0; i < sensors.size(); i++) {
                mSensorNames.add(sensors.get(i).getName());
            }
    }

    @Override
    protected void doCollectManually() {

    }

    @Override
    public String getJsonInfo() {
        return getGson().toJson(mSensorNames);
    }
}
