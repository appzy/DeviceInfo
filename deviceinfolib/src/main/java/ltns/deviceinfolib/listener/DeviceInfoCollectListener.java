package ltns.deviceinfolib.listener;

import ltns.deviceinfolib.DeviceInfoManager;
import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/20.
 * Email: gu.yuepeng@foxmail.com
 */

public interface DeviceInfoCollectListener {
    /**
     * manager启动收集
     */
    void onStart();
    /**
     * 有一个收集器完成收集任务
     * @param mCollector
     */
    void onSingleSuccess(BaseDeviceInfoCollector mCollector);

    /**
     * 有一个收集器收集过程中失败
     * @param mCollector
     * @param mErrorInfo
     */
    void onSingleFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo);

    /**
     * 所有的收集器完成收集
     * @param mDeviceInfoManager
     */
    void onAllDone(DeviceInfoManager mDeviceInfoManager);

    /**
     * 抛开需要手动收集的收集器，其他的收集器全部完成收集
     * @param mDeviceInfoManager
     */
    void onAutoAllDone(DeviceInfoManager mDeviceInfoManager);
}
