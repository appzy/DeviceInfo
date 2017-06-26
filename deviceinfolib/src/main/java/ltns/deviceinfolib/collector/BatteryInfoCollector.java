package ltns.deviceinfolib.collector;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/23.
 * Email: gu.yuepeng@foxmail.com
 */

/**
 * 由于电池需要通过等待接收广播，无法保证代码执行完一定能接到广播，
 * 因此采用手动调用方式，在onReceiver()中手动调用onManualCollectionSuccess(true);
 */
public class BatteryInfoCollector extends BaseDeviceInfoCollector {
    public BatteryInfoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

    private final String LEVEL = "level";//电池剩余电量:int
    private final String SCALE = "scale";//电池总电量：int
    private final String VOLTAGE = "voltage";//电压：int
    private final String TEMPERATURE = "temperature";//电池温度：0.1度为单位（int）即197表示19.7度
    private final String TECHNOLOGY = "technology";//电池类型 str
    /*
     * 充电方式：int
     * BatteryManager.BATTERY_PLUGGED_AC：AC充电。
     * BatteryManager.BATTERY_PLUGGED_USB：USB充电。
     */
    private final String PLUGGED = "plugged";
    /*
     * 电池状态：int
     * BatteryManager.BATTERY_STATUS_CHARGING：充电状态。
     * BatteryManager.BATTERY_STATUS_DISCHARGING：放电状态。
     * BatteryManager.BATTERY_STATUS_NOT_CHARGING：未充满。
     * BatteryManager.BATTERY_STATUS_FULL：充满电。
     * BatteryManager.BATTERY_STATUS_UNKNOWN：未知状态。
     */
    private final String STATUS = "status";
    /*
     * 健康状态：int
     * BatteryManager.BATTERY_HEALTH_GOOD：状态良好。
     * BatteryManager.BATTERY_HEALTH_DEAD：电池没有电。
     * BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE：电池电压过高。
     * BatteryManager.BATTERY_HEALTH_OVERHEAT：电池过热。
     * BatteryManager.BATTERY_HEALTH_UNKNOWN：未知状态。
     */
    private final String HEALTH = "health";

    @Override
    public boolean needCollectManually() {
        return true;
    }

    @Override
    public String[] getRequiredPermissions() {
        return new String[0];
    }

    @Override
    protected void doCollectAutomatically() {
    }

    @Override
    protected void doCollectManually() {
        IntentFilter batteryFilter = new IntentFilter();
        batteryFilter.addAction(Intent.ACTION_BATTERY_CHANGED);
        mContext.registerReceiver(new BatteryReceiver(), batteryFilter);
    }

    class BatteryReceiver extends BroadcastReceiver {
        private Intent mIntent;

        @Override
        public void onReceive(Context context, Intent intent) {
            mIntent = intent;
            put(LEVEL, getIntValue(LEVEL));
            put(SCALE, getIntValue(SCALE));
            put(VOLTAGE, getIntValue(VOLTAGE));
            put(TEMPERATURE, getIntValue(TEMPERATURE));
            put(TECHNOLOGY, getStrValue(TECHNOLOGY));
            put(PLUGGED, getIntValue(PLUGGED));
            put(STATUS, getIntValue(STATUS));
            put(HEALTH, getIntValue(HEALTH));
            onManualCollectionSuccess(true);
            context.unregisterReceiver(this);
        }

        private int getIntValue(String key) {
            return mIntent.getIntExtra(key, 0);
        }

        private String getStrValue(String key) {
            return mIntent.getStringExtra(key);
        }

    }
}
