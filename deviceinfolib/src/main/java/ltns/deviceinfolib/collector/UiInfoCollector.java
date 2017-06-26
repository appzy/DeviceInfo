package ltns.deviceinfolib.collector;

import android.content.Context;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/26.
 * Email: gu.yuepeng@foxmail.com
 */

public class UiInfoCollector extends BaseDeviceInfoCollector {
    public UiInfoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

    private static final String HAS_NAVIGATION_BAR = "hasNavigationBar";

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
        put(HAS_NAVIGATION_BAR, checkDeviceHasNavigationBar(mContext));
    }

    @Override
    protected void doCollectManually() {

    }

    private boolean checkDeviceHasNavigationBar(Context activity) {
        //通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
        boolean hasMenuKey = ViewConfiguration.get(activity)
                .hasPermanentMenuKey();
        boolean hasBackKey = KeyCharacterMap
                .deviceHasKey(KeyEvent.KEYCODE_BACK);

        if (!hasMenuKey && !hasBackKey) {
            // 做任何你需要做的,这个设备有一个导航栏
            return true;
        }
        return false;
    }
}
