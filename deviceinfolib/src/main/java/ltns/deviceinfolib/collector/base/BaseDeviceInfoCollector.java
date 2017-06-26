package ltns.deviceinfolib.collector.base;

import android.content.Context;

import com.google.gson.Gson;

import java.util.HashMap;

import ltns.deviceinfolib.api.Constant;
import ltns.deviceinfolib.listener.CollectorStateObserver;
import ltns.deviceinfolib.utils.PermissionsCheckUtils;

/**
 * Created by guyuepeng on 2017/6/20.
 * Email: gu.yuepeng@foxmail.com
 */

public abstract class BaseDeviceInfoCollector {

    protected Context mContext;
    public String collectorName = "baseDeviceCollector";
    protected HashMap<String, Object> collectDataMap = new HashMap<>();

    private CollectorStateObserver mStateObserver;
    private boolean hasObserver;

    public BaseDeviceInfoCollector(Context context, String collectorName) {
        mContext = context;
        this.collectorName = collectorName;
    }

    /**
     * @return 返回是否需要手动收集，若不需要，自动收集完成后会直接回调收集完成
     */
    public abstract boolean needCollectManually();


    public BaseDeviceInfoCollector bindObserver(CollectorStateObserver mStateObserver) {
        hasObserver = true;
        this.mStateObserver = mStateObserver;
        return this;
    }

    /**
     * 声明当前收集器所需的权限
     *
     * @return
     */
    public abstract String[] getRequiredPermissions();

    /**
     * 真正的自动收集方法
     */
    protected abstract void doCollectAutomatically();

    /**
     * 真正的手动收集的部分，在手动收集完成后，收集成功应在实现的方法内调用onCollectByHandDone
     */
    protected abstract void doCollectManually();

    /**
     * 启动收集
     */
    public final void startCollectAutomatically() {
        //TODO:检查逻辑——>
        new Thread(new Runnable() {
            @Override
            public void run() {
                //若缺少权限则终止收集
                if (PermissionsCheckUtils.lackPermissions(mContext, getRequiredPermissions())) {
                    onCollectionFailure(Constant.Error.PERMISSION_APPLY_REFUSED);
                    return;
                }
                doCollectAutomatically();
                //若需要手动收集，添加到集合中
                if (needCollectManually()) {
                    if (hasObserver)
                        mStateObserver.onNeedManualCollect(BaseDeviceInfoCollector.this);
                    return;
                }
                //若不需要手动采集，自动采集之后自动回调通知收集完成；
                //若需要手动采集信息，手动采集后需要手动调用对应方法通知manager完成收集
                onCollectionSuccess();
            }
        }).start();
    }

    /**
     * 启动手动收集
     */
    public final void startCollectManually() {
        doCollectManually();
    }

    /**
     * 当手动参与的信息收集完成时调用
     *
     * @param startNextManualCollector 是否启动队列中下一个手动采集器
     */
    protected final void onManualCollectionSuccess(boolean startNextManualCollector) {
        if (hasObserver)
            mStateObserver.onManualCollectionSuccess(this, startNextManualCollector);
        onCollectionSuccess();//因为手动采集是在自动采集之后进行的，因此手动采集完成即该采集器的所有的采集过程均完成
    }

    /**
     * 当手动参与的信息收集失败时调用
     * 提供默认的出错信息
     *
     * @param startNextManualCollector 是否启动队列中下一个手动采集器
     */
    protected final void onManualCollectionFailure(boolean startNextManualCollector) {
        onManualCollectionFailure(Constant.Error.COLLECT_BY_HAND_ERROR, startNextManualCollector);
    }

    protected final void onManualCollectionFailure(String errorInfo, boolean startNextManualCollector) {
        if (hasObserver)
            mStateObserver.onManualCollectionFailure(this, errorInfo, startNextManualCollector);
        onCollectionFailure(errorInfo);
    }

    /**
     * 当手动采集和自动收集均成功完成采集时调用
     */
    protected final void onCollectionSuccess() {
        if (hasObserver)
            mStateObserver.onCollectionSuccess(this);
    }
    /**
     * 当手动采集和自动收集均成功完成采集时调用
     */
    protected final void onCollectionFailure(String mErrorInfo) {
        if (hasObserver)
            mStateObserver.onCollectionFailure(this, mErrorInfo);
    }

    protected void put(String key, Object value) {
        collectDataMap.put(key, value);
    }

    /**
     * 如果具体子类构成json的方式不同，可覆盖（SimInfoCollector中就做了覆盖）
     *
     * @return
     */
    public String getJsonInfo() {
        return new Gson().toJson(collectDataMap);
    }


}
