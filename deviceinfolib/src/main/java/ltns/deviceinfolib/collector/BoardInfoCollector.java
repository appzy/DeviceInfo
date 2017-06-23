package ltns.deviceinfolib.collector;

import android.content.Context;
import android.os.Build;

/**
 * Created by guyuepeng on 2017/6/21.
 * Email: gu.yuepeng@foxmail.com
 */

public class BoardInfoCollector extends BaseDeviceInfoCollector {
    private static final String BOARD_NAME = "board_name";

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
