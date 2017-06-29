package ltns.deviceinfolib.collector;

import android.Manifest;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/21.
 * Email: gu.yuepeng@foxmail.com
 * <p>
 * 具体每个方法获取的值的含义以及返回值区间见内部类SimCard.java中的注释
 */

public class SimInfoCollector extends BaseDeviceInfoCollector {

    private static final String SIM_COUNT = "simCount";
    private static final String SIMS = "sims";

    private class SimCard {
        /**
         * 按照字母次序的current registered operator(当前已注册的用户)的名字<br/>
         * 注意：仅当用户已在网络注册时有效。<br/>
         * 在CDMA网络中结果也许不可靠。
         */
        private String networkOperatorName;
        /**
         * 当前使用的网络类型：<br/>
         * NETWORK_TYPE_UNKNOWN 网络类型未知 0<br/>
         * NETWORK_TYPE_GPRS GPRS网络 1<br/>
         * NETWORK_TYPE_EDGE EDGE网络 2<br/>
         * NETWORK_TYPE_UMTS UMTS网络 3<br/>
         * NETWORK_TYPE_HSDPA HSDPA网络 8<br/>
         * NETWORK_TYPE_HSUPA HSUPA网络 9<br/>
         * NETWORK_TYPE_HSPA HSPA网络 10<br/>
         * NETWORK_TYPE_CDMA CDMA网络,IS95A 或 IS95B. 4<br/>
         * NETWORK_TYPE_EVDO_0 EVDO网络, revision 0. 5<br/>
         * NETWORK_TYPE_EVDO_A EVDO网络, revision A. 6<br/>
         * NETWORK_TYPE_1xRTT 1xRTT网络 7<br/>
         * 在中国，联通的3G为UMTS或HSDPA，移动和联通的2G为GPRS或EGDE，电信的2G为CDMA，电信的3G为EVDO<br/>
         */
        private String networkType;
        /**
         * 是否漫游:(在GSM用途下)
         */
        private String isNetworkRoaming;
        /**
         * 获取数据连接状态<br/>
         * DATA_CONNECTED 数据连接状态：已连接<br/>
         * DATA_CONNECTING 数据连接状态：正在连接<br/>
         * DATA_DISCONNECTED 数据连接状态：断开<br/>
         * DATA_SUSPENDED 数据连接状态：暂停<br/>
         */
        private String dataState;
        /**
         * 返回移动终端的类型：<br/>
         * PHONE_TYPE_CDMA 手机制式为CDMA，电信<br/>
         * PHONE_TYPE_GSM 手机制式为GSM，移动和联通<br/>
         * PHONE_TYPE_NONE 手机制式未知<br/>
         */
        private String phoneType;
        private String simState;//sim卡状态
        private String simSerialNumber;//手机卡序列号:898xxxxxxxxxxxxxxx
        private String simCountryIso;//手机卡国家简称：cn
        private String simOperator;//手机卡运营商：46001
        private String simOperatorName;//手机卡运营商名称：中国联通
        private String imsi;//手机卡的IMSI：4600179xxxxxxxx

        private String imei;//sim卡槽对应的设备号IMEI

        public String getImei() {
            return imei;
        }

        public void setImei(String imei) {
            this.imei = imei;
        }


        public String getNetworkOperatorName() {
            return networkOperatorName;
        }

        public void setNetworkOperatorName(String networkOperatorName) {
            this.networkOperatorName = networkOperatorName;
        }

        public String getNetworkType() {
            return networkType;
        }

        public void setNetworkType(String networkType) {
            this.networkType = networkType;
        }

        public String getIsNetworkRoaming() {
            return isNetworkRoaming;
        }

        public void setIsNetworkRoaming(String isNetworkRoaming) {
            this.isNetworkRoaming = isNetworkRoaming;
        }

        public String getDataState() {
            return dataState;
        }

        public void setDataState(String dataState) {
            this.dataState = dataState;
        }

        public String getPhoneType() {
            return phoneType;
        }

        public void setPhoneType(String phoneType) {
            this.phoneType = phoneType;
        }

        public String getSimState() {
            return simState;
        }

        public void setSimState(String simState) {
            this.simState = simState;
        }

        public String getSimSerialNumber() {
            return simSerialNumber;
        }

        public void setSimSerialNumber(String simSerialNumber) {
            this.simSerialNumber = simSerialNumber;
        }

        public String getSimCountryIso() {
            return simCountryIso;
        }

        public void setSimCountryIso(String simCountryIso) {
            this.simCountryIso = simCountryIso;
        }

        public String getSimOperator() {
            return simOperator;
        }

        public void setSimOperator(String simOperator) {
            this.simOperator = simOperator;
        }

        public String getSimOperatorName() {
            return simOperatorName;
        }

        public void setSimOperatorName(String simOperatorName) {
            this.simOperatorName = simOperatorName;
        }

        public String getImsi() {
            return imsi;
        }

        public void setImsi(String imsi) {
            this.imsi = imsi;
        }

    }

    private static final String FUN_SIM_STATE = "getSimState";//sim卡状态
    private static final String FUN_SIM_SERIAL_NUMBER = "getSimSerialNumber";//手机卡序列号:898xxxxxxxxxxxxxxx
    private static final String FUN_SIM_COUNTRY_ISO = "getSimCountryIso";//手机卡国家简称：cn
    private static final String FUN_SIM_OPERATOR = "getSimOperator";//手机卡运营商：46001
    private static final String FUN_SIM_IMSI = "getSubscriberId";//手机卡的IMSI：4600179xxxxxxxx
//    private static final String FUN_IMEI = "getDeviceIdGemini";//设备IMEI：4600179xxxxxxxx---MTX
//    private static final String FUN_IMEI2 = "getDeviceId";//和上面的方法一样的功能，不同平台自己定制的内容不一样

    private static final String FUN_NETWORK_TYPE = "getNetworkType";//当前使用的网络类型
    private static final String FUN_NETWORK_OPERATOR_NAME = "getNetworkOperatorName";//(当前已注册的用户)运营商名字:
    private static final String FUN_PHONE_TYPE = "getPhoneType";//手机类型
    private static final String FUN_DATA_STATE = "getDataState";//获取数据连接状态
    private static final String FUN_IS_NETWORK_ROAMING = "isNetworkRoaming";//是否漫游

    public SimInfoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

    private String[] permissions = {
            Manifest.permission.READ_PHONE_STATE};
    private List<String> mSimIDs = new ArrayList<>();
    private List<SimCard> mSimCards = new ArrayList<>();

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
        initSimCount();
        for (String id :
                mSimIDs) {
            SimCard simCard = new SimCard();
            simCard.setSimState(getOperatorBySlot(mContext, FUN_SIM_STATE, Integer.valueOf(id)));
            simCard.setSimSerialNumber(getOperatorBySlot(mContext, FUN_SIM_SERIAL_NUMBER, Integer.valueOf(id)));
            simCard.setSimCountryIso(getOperatorBySlot(mContext, FUN_SIM_COUNTRY_ISO, Integer.valueOf(id)));
            simCard.setSimOperator(getOperatorBySlot(mContext, FUN_SIM_OPERATOR, Integer.valueOf(id)));
            simCard.setNetworkOperatorName(getOperatorBySlot(mContext, FUN_NETWORK_OPERATOR_NAME, Integer.valueOf(id)));
            simCard.setImsi(getOperatorBySlot(mContext, FUN_SIM_IMSI, Integer.valueOf(id)));
            simCard.setNetworkType(getOperatorBySlot(mContext, FUN_NETWORK_TYPE, Integer.valueOf(id)));
            simCard.setNetworkOperatorName(getOperatorBySlot(mContext, FUN_NETWORK_OPERATOR_NAME, Integer.valueOf(id)));
            simCard.setPhoneType(getOperatorBySlot(mContext, FUN_PHONE_TYPE, Integer.valueOf(id)));
            simCard.setDataState(getOperatorBySlot(mContext, FUN_DATA_STATE, Integer.valueOf(id)));
            simCard.setIsNetworkRoaming(getOperatorBySlot(mContext, FUN_IS_NETWORK_ROAMING, Integer.valueOf(id)));
//            simCard.setImei(getOperatorBySlot(mContext, FUN_IMEI, Integer.valueOf(id)) != null
//                    ? getOperatorBySlot(mContext, FUN_IMEI, Integer.valueOf(id))
//                    : getOperatorBySlot(mContext, FUN_IMEI2, Integer.valueOf(id)));
            mSimCards.add(simCard);
        }
        put(SIM_COUNT,mSimCards.size());
        put(SIMS,mSimCards);
    }

    @Override
    protected void doCollectManually() {
    }

    private void initSimCount() {
        mSimIDs = new ArrayList<>();
        SubscriptionManager mSubscriptionManager = (SubscriptionManager) mContext.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            mSubscriptionManager.getActiveSubscriptionInfoCountMax();//手机SIM卡数
            mSubscriptionManager.getActiveSubscriptionInfoCount();//手机使用的SIM卡数
            List<SubscriptionInfo> activeSubscriptionInfoList = mSubscriptionManager.getActiveSubscriptionInfoList();//手机SIM卡信息
            for (SubscriptionInfo subscriptionInfo : activeSubscriptionInfoList
                    ) {
                mSimIDs.add(subscriptionInfo.getSubscriptionId() + "");
            }
        } else {
            initSimIDsByReadFile();
        }

    }

    private void initSimIDsByReadFile() {
        Uri uri = Uri.parse("content://telephony/siminfo"); //访问raw_contacts表
        ContentResolver resolver = mContext.getContentResolver();
        Cursor cursor = resolver.query(uri, new String[]{"_id", "icc_id", "sim_id", "display_name", "carrier_name", "name_source", "color", "number", "display_number_format", "data_roaming", "mcc", "mnc"}, null, null, null);
        if (cursor != null) {
            mSimIDs = new ArrayList<>();
            while (cursor.moveToNext()) {
                //当simId==-1时说明卡槽没有插入sim卡，其他情况下说明有卡
                String id = cursor.getString(cursor.getColumnIndex("_id"));
                String sim_id = cursor.getString(cursor.getColumnIndex("sim_id"));
                if (Integer.valueOf(sim_id) >= 0) {
                    mSimIDs.add(id);
                }
            }
            cursor.close();
        }
    }


    private String getOperatorBySlot(Context context, String predictedMethodName, int slotID) {
        String inumeric = null;
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        try {
            Class<?> telephonyClass = Class.forName(telephony.getClass().getName());
            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimID = telephonyClass.getMethod(predictedMethodName, parameter);
            Object[] obParameter = new Object[1];
            obParameter[0] = slotID;
            Object ob_phone = getSimID.invoke(telephony, obParameter);
            if (ob_phone != null) {
                inumeric = ob_phone.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return inumeric;
    }


}
