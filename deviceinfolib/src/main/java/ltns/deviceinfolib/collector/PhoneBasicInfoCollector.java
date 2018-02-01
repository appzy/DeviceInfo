package ltns.deviceinfolib.collector;

import android.Manifest;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import com.google.gson.annotations.SerializedName;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;


/**
 * @date 创建时间：2018/1/8
 * @author appzy
 * @Description Andorid设备基本信息
 * @version
 */

public class PhoneBasicInfoCollector extends BaseDeviceInfoCollector {
    private static final String PRODUCT = "product";//产品型号
    private static final String MODEL = "model_m";//手机型号
    private static final String SDK = "sdk";//sdk
    private static final String SDK_INT = "sdkInt";//sdk版本号，如andorid6.0-->23
    private static final String MAC = "mac";//mac地址
    private static final String IMEI = "imei";//IMEI
    private static final String HARDWARE = "hardware";
    private static final String RADIO_VERSION = "radioVersion";//无线电固件版本

    private static final String BRAND="brand";//系统供应商
    private static final String ROM="rom";//rom供应商???
    private static final String VERSION="systemVersion";//系统版本
    private static final String DISPLAY="display";//版本显示
    private static final String HOST="host";//主机
    /*
    /*
     * 电话状态：
     * 1.tm.CALL_STATE_IDLE=0     无活动
     * 2.tm.CALL_STATE_RINGING=1  响铃
     * 3.tm.CALL_STATE_OFFHOOK=2  摘机
     */
    private static final String CALL_STATE = "callState";


    public PhoneBasicInfoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

    private String[] permissions = {Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.READ_PHONE_STATE};

    @Override
    public boolean needCollectManually() {
        return false;
    }

    @Override
    public String[] getRequiredPermissions() {
        return permissions;
    }

    @Override
    protected void doCollectAutomatically() {
        put(ROM, Build.MANUFACTURER);
        put(CALL_STATE, getCallState());
        put(MODEL, Build.MODEL);
        put(SDK, Build.VERSION.RELEASE);
        put(SDK_INT, Build.VERSION.SDK_INT);
        put(MAC, getMacAddress());
        put(IMEI, getDeviceId());
        put(HARDWARE, Build.HARDWARE);
        put(RADIO_VERSION, getRadioVer());
        put(PRODUCT,Build.PRODUCT);
        put(VERSION,Build.VERSION.RELEASE);
        put(HOST,Build.HOST);
        put(BRAND,Build.BRAND);
        put(DISPLAY,Build.DISPLAY);

    }

    private int getCallState() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getCallState();
    }


    @Deprecated
    private String getPlatform(String key, String def) {
        Class<?> mClassType = null;
        String v = "UNKNOW";
        try {
            if (null == mClassType) {
                mClassType = Class.forName("android.os.SystemProperties");
                try {
                    Method mGetIntMethod = mClassType.getDeclaredMethod("get", String.class, String.class);
                    try {
                        v = (String) mGetIntMethod.invoke(mClassType, key, def);
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    } catch (InvocationTargetException e) {
                        e.printStackTrace();
                    }
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return v;
    }

    private String getDeviceId() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getDeviceId();
    }

    private String getMacAddress() {
        WifiManager wifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        String macAddress = info.getMacAddress();
        return macAddress == null ? "" : macAddress;
    }

    private String getRadioVer() {
        return Build.getRadioVersion();
    }

    @Override
    protected void doCollectManually() {

    }
}
