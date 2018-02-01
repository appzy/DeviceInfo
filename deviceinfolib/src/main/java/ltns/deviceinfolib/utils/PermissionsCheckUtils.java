package ltns.deviceinfolib.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;

/**
 * @date 创建时间：2018/1/8
 * @author appzy
 * @Description 权限
 * @version
 */

public class PermissionsCheckUtils {

    /**
     * 检查缺少的权限
     * @param mContext
     * @param permissions
     * @return
     */
    public static boolean lackPermissions(Context mContext, String... permissions) {
        for (String permission : permissions) {
            if (lackPermission(mContext, permission)) {
                return true;
            }
        }
        return false;
    }

    private static boolean lackPermission(Context mContext, String permission) {
        return ContextCompat.checkSelfPermission(mContext, permission) ==
                PackageManager.PERMISSION_DENIED;
    }
}