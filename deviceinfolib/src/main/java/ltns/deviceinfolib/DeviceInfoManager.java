package ltns.deviceinfolib;

import android.content.Context;
import android.support.annotation.NonNull;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import ltns.deviceinfolib.collector.BaseDeviceInfoCollector;
import ltns.deviceinfolib.listener.CollectorStateObserver;
import ltns.deviceinfolib.listener.DeviceInfoCollectListener;
import ltns.deviceinfolib.utils.PermissionsCheckUtils;

/**
 * Created by guyuepeng on 2017/6/20.
 * Email: gu.yuepeng@foxmail.com
 */

public class DeviceInfoManager implements CollectorStateObserver {

    //记录所有的收集器
    private List<BaseDeviceInfoCollector> mAllCollectors = new ArrayList<>();
    //记录正在运行的收集器
    private List<BaseDeviceInfoCollector> mRunningCollectors;
    private int runningCollectorCount = 0;

    //记录需要手动操作的收集器
    private List<BaseDeviceInfoCollector> mManualCollectors = new ArrayList<>();
    private boolean hasManualCollector;

    //对外提供的监听接口，反馈收集器的状态
    private DeviceInfoCollectListener mDeviceInfoCollectListener;
    private boolean hasDeviceInfoCollectListener;

    //当自动收集完成后是否自动启动手动收集
    private boolean isManualCollectionStartAutomatically = true;

    private Context mContext;

    private DeviceInfoManager(Context mContext) {
        this.mContext = mContext;
    }

    public static DeviceInfoManager NewInstance(Context mContext) {
        return new DeviceInfoManager(mContext);
    }

    public DeviceInfoManager addCollector(BaseDeviceInfoCollector mCollector) {
        mCollector.bindObserver(this);
        mAllCollectors.add(mCollector);
        return this;
    }

    public DeviceInfoManager bindListener(DeviceInfoCollectListener mListener) {
        mDeviceInfoCollectListener = mListener;
        hasDeviceInfoCollectListener = true;
        return this;
    }

    /**
     * 是否自动启动手动收集
     *
     * @return
     */
    public DeviceInfoManager autoStartManualCollection(boolean autoDo) {
        isManualCollectionStartAutomatically = autoDo;
        return this;
    }

    /**
     * 若不开启自动进行手动采集，则需要在所有的自动收集部分完成后，调用该方法开始收集
     */
    public void startCollectByHand() {
        doStartManualCollector();
    }

    /**
     * 循环开启自动收集
     */
    public void start() {
        String[] permissions = getAllPermissions();
        if (!PermissionsCheckUtils.lackPermissions(mContext, permissions)) {
            //若不缺少权限，直接开始循环开启收集
            realStartAutomaticCollection();
        }
        //先申请权限，然后执行循环开启收集
        AndPermission.with(mContext)
                .requestCode(100)
                .permission(permissions)
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                        //真正的循环开始收集
                        if (!realStartAutomatedCollect)
                            realStartAutomaticCollection();
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        /*
                            FIXME:存在逻辑漏洞，可能出现要申请的权限部分已经授权了，其余的（真正申请的）全部驳回，此时没有调用onSucceed,也不会走到if内
                            FIXME:暂时使用一个标志位使realStartCollect()方法只能被调用一次
                        */
                        //此时所有的权限都被驳回，onSucceed没有被调用，因此在此处需要调用启动收集
                        if (!realStartAutomatedCollect)
                            realStartAutomaticCollection();
                    }
                })
                .start();

    }

    private boolean realStartAutomatedCollect = false;//见startCollect方法的 FIXME

    private void realStartAutomaticCollection() {
        realStartAutomatedCollect = true;
        mRunningCollectors = new ArrayList<>();
        if (hasDeviceInfoCollectListener)
            mDeviceInfoCollectListener.onStart();
        for (BaseDeviceInfoCollector mCollector :
                mAllCollectors) {
            runningCollectorCount++;
            mRunningCollectors.add(mCollector);
            //启动
            mCollector.startCollectAutomatically();
        }
    }

    /**
     * 在Collector子线程中收集器完成信息收集后调用，线程同步
     *
     * @param mCollector
     */
    @Override
    public void onCollectionSuccess(BaseDeviceInfoCollector mCollector) {
        if (hasDeviceInfoCollectListener)
            mDeviceInfoCollectListener.onSingleSuccess(mCollector);
        doSingleCollectorStopRunning(mCollector);
    }

    @Override
    public void onCollectionFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo) {
        if (hasDeviceInfoCollectListener)
            mDeviceInfoCollectListener.onSingleFailure(mCollector, mErrorInfo);
        doSingleCollectorStopRunning(mCollector);
    }

    @Override
    public void onManualCollectionSuccess(BaseDeviceInfoCollector mCollector, boolean startNext) {
        if (startNext)
            startNext(mCollector);
    }

    @Override
    public void onManualCollectionFailure(BaseDeviceInfoCollector mCollector, String mErrorInfo, boolean startNext) {
        if (startNext)
            startNext(mCollector);
    }

    /**
     * 当添加需要手动收集的collector时回调
     *
     * @param mCollector
     */
    @Override
    public void onNeedManualCollect(BaseDeviceInfoCollector mCollector) {
        hasManualCollector = true;
        mManualCollectors.add(mCollector);
        checkAutomatedAllDone();
    }

    /**
     * 启动下一个手动采集器
     *
     * @param mCollector
     */
    private void startNext(BaseDeviceInfoCollector mCollector) {
        int currentIndex = mManualCollectors.indexOf(mCollector);
        if (currentIndex == mManualCollectors.size() - 1)
            return;
        mManualCollectors.get(currentIndex + 1).startCollectManually();
    }

    /**
     * 当有一个之前处于运行态的收集器停止运行时，均要调用该方法，以维护runningCollectors，同时检查是否全部收集器运行结束
     *
     * @param mCollector
     */
    private synchronized void doSingleCollectorStopRunning(BaseDeviceInfoCollector mCollector) {
        runningCollectorCount--;
        mRunningCollectors.remove(mCollector);
        checkAllDone();
        checkAutomatedAllDone();
    }

    /**
     * 检查所有的采集器是否完成
     */
    private void checkAllDone() {
        if (!isAllDone())
            return;
        if (hasDeviceInfoCollectListener)
            mDeviceInfoCollectListener.onAllDone(this);
    }

    //当设置了所有自动采集工作完成后立即进行手动采集，且所有自动采集完成后，开启手动采集的标志位
    private boolean isManualCollectionStart = false;

    /**
     * 检查所有的自动采集器是否完成采集工作（无论有无出错），同时做回调
     */
    private synchronized void checkAutomatedAllDone() {
        if (!isAutomatedAllDone())
            return;
        if (isManualCollectionStartAutomatically && !isManualCollectionStart) {
            isManualCollectionStart = true;
            doStartManualCollector();
        }
        if (hasDeviceInfoCollectListener)
            mDeviceInfoCollectListener.onAutoAllDone(this);
    }

    /**
     * 真正的开启手动采集器的方法
     */
    private void doStartManualCollector() {
        if (mManualCollectors.size() == 0)
            return;
        mManualCollectors.get(0).startCollectManually();
    }

    /**
     * 通过判断是否还有运行的采集器，检查是否所有采集器完成采集工作
     *
     * @return
     */
    private boolean isAllDone() {
        return runningCollectorCount == 0;
    }

    /**
     * 通过比对运行中的数量和需要手动采集而被挂起的采集器数量，判断是否所有自动采集工作完成
     *
     * @return
     */
    private boolean isAutomatedAllDone() {
        return runningCollectorCount == mManualCollectors.size();
    }

    private String[] getAllPermissions() {
        Set<String> mPermissionSet = new HashSet<>();
        for (BaseDeviceInfoCollector mCollector :
                mAllCollectors) {
            for (String permission :
                    mCollector.getRequiredPermissions()) {
                mPermissionSet.add(permission);
            }
        }
        return mPermissionSet.toArray(new String[0]);
    }


    /**
     * 每个单元的json信息组成完整的json
     *
     * @return
     */
    public String getDeviceJsonInfo() {
        StringBuffer sb = new StringBuffer();
        sb.append("{");
        BaseDeviceInfoCollector mCollector;
        for (int i = 0; i < mAllCollectors.size(); i++) {
            mCollector = mAllCollectors.get(i);
            if (i > 0)
                sb.append(",");
            sb.append("\"" + mCollector.collectorName + "\":" + mCollector.getJsonInfo());
        }
        sb.append("}");
        return sb.toString();
    }

}
