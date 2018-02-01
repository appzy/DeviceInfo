package ltns.deviceinfolib.listener;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * @date 创建时间：2018/1/1
 * @author appzy
 * @Description
 * @version
 */

public interface CollectorStateObserver {
    void onCollectionSuccess(BaseDeviceInfoCollector mCollector);
    void onCollectionFailure(BaseDeviceInfoCollector mCollector,String mErrorInfo);

    void onManualCollectionSuccess(BaseDeviceInfoCollector mCollector,boolean startNext);
    void onManualCollectionFailure(BaseDeviceInfoCollector mCollector,String mErrorInfo,boolean startNext);

    void onNeedManualCollect(BaseDeviceInfoCollector mCollector);
}
