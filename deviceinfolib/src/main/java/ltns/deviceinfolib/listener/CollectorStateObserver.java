package ltns.deviceinfolib.listener;

import ltns.deviceinfolib.collector.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/22.
 * Email: gu.yuepeng@foxmail.com
 */

public interface CollectorStateObserver {
    void onCollectionSuccess(BaseDeviceInfoCollector mCollector);
    void onCollectionFailure(BaseDeviceInfoCollector mCollector,String mErrorInfo);

    void onManualCollectionSuccess(BaseDeviceInfoCollector mCollector,boolean startNext);
    void onManualCollectionFailure(BaseDeviceInfoCollector mCollector,String mErrorInfo,boolean startNext);

    void onNeedManualCollect(BaseDeviceInfoCollector mCollector);
}
