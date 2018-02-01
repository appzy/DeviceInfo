package ltns.deviceinfolib.collector;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * @date 创建时间：2018/1/8
 * @author appzy
 * @Description 屏幕信息
 * @version
 */

public class ScreenInfoCollector extends BaseDeviceInfoCollector {
    private static final String REAL_RESOLUTION = "realResolution";//实际像素
    private static final String RESOLUTION = "resolution";//正常像素
    private static final String DENSITY_DPI = "densityDpi";// 屏幕密度（每寸像素：120/160/240/320）

//    private static final String SCREEN_WIDTH = "screenWidth";//屏幕宽度
//    private static final String SCREEN_HEIGHT = "screenHeight";//屏幕高度

    private static final String MULTI_TOUCH="multiTouch";//多点触控
    private static final int GET_MULTI_TOUCH_INFO = 99;

    public ScreenInfoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

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
        put(RESOLUTION, getResolution());
        put(REAL_RESOLUTION,getRealResolution());
        put(DENSITY_DPI, getDensityDpi());
    }

    @Override
    protected void doCollectManually() {
    }

    @Deprecated
    private int[] getScreenSize() {
        int[] size = new int[2];
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        size[0] = wm.getDefaultDisplay().getHeight();
        size[1] = wm.getDefaultDisplay().getWidth();
        return size;
    }

    /**
     * 获取屏幕正常分辨率
     */
    private String getResolution() {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        int screenHeight = display.getHeight();
        int screenWidth = display.getWidth();
        return screenWidth + " * " + screenHeight;
    }

    private String getRealResolution() {
        DisplayMetrics metrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            ((WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE))
                    .getDefaultDisplay().getRealMetrics(metrics);
        }
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return width + "*" + height;
    }

    private int getDensityDpi() {
        DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
        return metrics.densityDpi;
    }


}
