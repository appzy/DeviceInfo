package ltns.deviceinfolib.collector;

import android.Manifest;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.telephony.TelephonyManager;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * Created by guyuepeng on 2017/6/23.
 * Email: gu.yuepeng@foxmail.com
 */

public class PhoneBasicInfoCollector extends BaseDeviceInfoCollector {
    private static final String VENDOR = "vendor";//供应商
    private static final String MODEL = "model";//版本
    private static final String SDK = "sdk";//sdk
    private static final String SDK_INT = "sdk_int";//sdk版本号，如andorid6.0-->23
    private static final String MAC = "mac";//mac地址
    private static final String IMEI = "imei";//IMEI
    private static final String HARDWARE_ID = "hardware_id";
    private static final String RADIO_VERSION = "radio_version";//无线电固件版本
    /*
     * 电话状态：
     * 1.tm.CALL_STATE_IDLE=0     无活动
     * 2.tm.CALL_STATE_RINGING=1  响铃
     * 3.tm.CALL_STATE_OFFHOOK=2  摘机
     */
    private static final String CALL_STATE = "call_state";


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
        return new String[0];
    }

    @Override
    protected void doCollectAutomatically() {
        put(VENDOR, Build.MANUFACTURER);
        put(CALL_STATE, getCallState());
        put(MODEL, Build.MODEL);
        put(SDK, Build.VERSION.RELEASE);
        put(SDK_INT, Build.VERSION.SDK_INT);
        put(MAC, getMacAddress());
        put(IMEI, getDeviceId());
        put(HARDWARE_ID, getHardware());
        put(RADIO_VERSION, getRadioVer());
    }

    private int getCallState() {
        TelephonyManager tm = (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getCallState();
    }

    private String getHardware() {
        String hardware = "UNKNOW";
        if (Build.VERSION.SDK_INT > 8) {
            try {
                Class<?> propertyClass = Class.forName("android.os.Build");
                Build build = (Build) propertyClass.newInstance();
                Field f = build.getClass().getField("HARDWARE");
                hardware = (String) f.get(hardware);
                if (null != hardware && hardware.toLowerCase().contains("mt")) {
                    return hardware;
                } else {
                    return getPlatform("ro.board.platform", "unknown");
                }
            } catch (SecurityException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
        }
        return hardware;
    }

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
