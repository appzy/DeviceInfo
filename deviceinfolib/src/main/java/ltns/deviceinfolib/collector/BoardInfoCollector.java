package ltns.deviceinfolib.collector;

import android.content.Context;
import android.os.Build;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * @date 创建时间：2018/1/8
 * @author appzy
 * @Description 主板信息
 * @version
 */

public class BoardInfoCollector extends BaseDeviceInfoCollector {
    private static final String BOARD_NAME = "boardName";//主板名

    public BoardInfoCollector(Context context, String collectorName) {
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
        collectDataMap.put(BOARD_NAME,getBoardName());

    }

    private String getBoardName() {
        return Build.BOARD;
    }

    @Override
    protected void doCollectManually() {
    }
}
