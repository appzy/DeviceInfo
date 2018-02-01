package ltns.deviceinfolib.collector;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * @date 创建时间：2018/1/8
 * @author appzy
 * @Description 系统相关信息
 * @version
 */

public class SystemInfoCollector extends BaseDeviceInfoCollector {
    public SystemInfoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

    private static final String BUILD_PROP = "build.prop";
    private final String BUILD_PROP_PATH = "system/build.prop";
    private final String DEFAULT_PROP_PATH = "system/default.prop";//恢复出厂后的build.prop

    private final String SYSTEM_APP="systemApp";
    private final String SYSTEM_APP_PATH ="system/app";
    private final String SYSTEM_APP_PATH_1 ="system/pri-app";

    private final String SETTINGS_DEFAULT_PATH_23plus = "/data/system/users/0/settings_system.xml";//6.0以上
    private final String SETTINGS_DEFAULT_PATH = "data/data/com.android.providers.settings/databases/settings.db";
    private final String SETTINGS_DEFAULT_PATH2 = "systen/etc/motorola/com.android.providers.settings/databases/settings.db";

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
        {
            String buildPropPath = DEFAULT_PROP_PATH;
            if (new File(BUILD_PROP_PATH).exists())
                buildPropPath = BUILD_PROP_PATH;
            Log.i("--->", buildPropPath);
            put(BUILD_PROP, readFileByLines(buildPropPath));
        }
//        {
//           //没有权限读 naive
//            String settingsDbPath = SETTINGS_DEFAULT_PATH_23plus;
//            if (new File(SETTINGS_DEFAULT_PATH).exists())
//                settingsDbPath = SETTINGS_DEFAULT_PATH;
//            SQLiteDatabase db = SQLiteDatabase.openOrCreateDatabase(settingsDbPath, null);
//            find(db);
//        }
        {
            String systemApp = SYSTEM_APP_PATH;
            if (new File(SYSTEM_APP_PATH_1).exists())
                systemApp = SYSTEM_APP_PATH_1;
            put(SYSTEM_APP,getFiles(systemApp));
        }

    }
    /**
     * 以行为单位读取文件，常用于读面向行的格式化文件
     */
    public String readFileByLines(String fileName) {
        File file = new File(fileName);

        BufferedReader reader = null;
        StringBuffer sb = new StringBuffer();
        try {
            reader = new BufferedReader(new FileReader(file));
            String tempString = null;
            int line = 1;
            // 一次读入一行，直到读入null为文件结束
            while ((tempString = reader.readLine()) != null) {
                // 显示行号
                Log.i("--->", "line " + line + ": " + tempString);
                sb.append(tempString);
                line++;
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e1) {
                }
            }
        }
        return sb.toString();
    }
    public List<String> find(SQLiteDatabase db) {
        List<String> all = new ArrayList<String>(); //此时只是String
        String sql = "SELECT * FROM *";
        Cursor result = db.rawQuery(sql, null);    //执行查询语句
        for (result.moveToFirst(); !result.isAfterLast(); result.moveToNext())   //采用循环的方式查询数据
        {
            all.add(result.toString());
            Log.i("--->", result.toString());
        }
        db.close();
        return all;
    }

    private File[] getFiles(String path) {
        return new File(path).listFiles();
    }

    private String exec(String i) {
        StringBuffer sb = new StringBuffer();
        try {
            String temp;
            Process p;
            p = Runtime.getRuntime().exec(i);
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((temp = br.readLine()) != null) {
                sb.append(temp);
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return "N/A";
        }
        return sb.toString();
    }

    @Override
    protected void doCollectManually() {

    }
}
