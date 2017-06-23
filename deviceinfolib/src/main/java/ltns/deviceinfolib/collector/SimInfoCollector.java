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

import com.google.gson.Gson;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by guyuepeng on 2017/6/21.
 * Email: gu.yuepeng@foxmail.com
 */

public class SimInfoCollector extends BaseDeviceInfoCollector {
//    private static final String NETWORK_OPERATOR_NAME = "networkOperatorName";
//    private static final String NETWORK_TYPE = "networkType";
//    private static final String IS_NETWORK_ROAMING = "isNetworkRoaming";
//    private static final String DATA_STATE = "dataState";
//    private static final String PHONE_TYPE = "phoneType";
//    private static final String SIM_STATE = "SimState";//sim卡状态
//    private static final String SIM_SERIAL_NUMBER = "SimSerialNumber";//手机卡序列号:898xxxxxxxxxxxxxxx
//    private static final String SIM_COUNTRY_ISO = "SimCountryIso";//手机卡国家简称：cn
//    private static final String SIM_OPERATOR = "SimOperator";//手机卡运营商：46001
//    private static final String SIM_OPERATOR_NAME = "SimOperatorName";//手机卡运营商名称：中国联通
//    private static final String SIM_IMSI = "imsi";//手机卡的IMSI：4600179xxxxxxxx

    private static final String FUN_SIM_STATE = "getSimState";
    private static final String FUN_SIM_SERIAL_NUMBER = "getSimSerialNumber";
    private static final String FUN_SIM_COUNTRY_ISO = "getSimCountryIso";
    private static final String FUN_SIM_OPERATOR = "getSimOperator";
    private static final String FUN_SIM_IMSI = "getSubscriberId";
    private static final String FUN_NETWORK_TYPE = "getNetworkType";
    private static final String FUN_NETWORK_OPERATOR_NAME = "getNetworkOperatorName";
    private static final String FUN_PHONE_TYPE = "getPhoneType";
    private static final String FUN_DATA_STATE = "getDataState";
    private static final String FUN_IS_NETWORK_ROAMING = "isNetworkRoaming";

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
            mSimCards.add(simCard);
        }
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

    @Override
    public String getJsonInfo() {
        return new Gson().toJson(mSimCards);
    }

    private class SimCard {
        private String networkOperatorName;
        private String networkType;
        private String isNetworkRoaming;
        private String dataState;
        private String phoneType;
        private String simState;//sim卡状态
        private String simSerialNumber;//手机卡序列号:898xxxxxxxxxxxxxxx
        private String simCountryIso;//手机卡国家简称：cn
        private String simOperator;//手机卡运营商：46001
        private String simOperatorName;//手机卡运营商名称：中国联通
        private String imsi;//手机卡的IMSI：4600179xxxxxxxx


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


}
