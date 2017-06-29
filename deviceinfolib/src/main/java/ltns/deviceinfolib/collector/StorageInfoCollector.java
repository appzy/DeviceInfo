package ltns.deviceinfolib.collector;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Environment;
import android.os.StatFs;
import android.text.format.Formatter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/23.
 * Email: gu.yuepeng@foxmail.com
 */

public class StorageInfoCollector extends BaseDeviceInfoCollector {
    public StorageInfoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

    private String[] permissions = {
            Manifest.permission.READ_EXTERNAL_STORAGE
    };


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
        Ram mRam = new Ram();
        mRam.setRamAvail(getRamAvail());
        mRam.setRamTotal(getRamTotal());
        Sdcard mSd = new Sdcard();
        String[] sd = getSDCardMemory();
        mSd.setUsedSize(sd[2]);
        mSd.setAvailSize(sd[1]);
        mSd.setMaxSize(sd[0]);
        put("ram", mRam);
        put("sd", mSd);
    }

    public String[] getSDCardMemory() {
        String[] sdCardInfo = new String[3];
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            File sdcardDir = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(sdcardDir.getPath());
            long bSize = sf.getBlockSize();
            long bCount = sf.getBlockCount();
            long availBlocks = sf.getAvailableBlocks();

            sdCardInfo[0] = bSize * bCount / 1024 / 1024 + "MB";//总大小
            sdCardInfo[1] = bSize * availBlocks / 1024 / 1024 + "MB";//可用大小
            sdCardInfo[2] = bSize * (bCount - availBlocks) / 1024 / 1024 + "MB";//已用大小
        }
        return sdCardInfo;
    }

    @Override
    protected void doCollectManually() {
    }

    /**
     * 返回最大可用Ram
     */
    @SuppressLint("DefaultLocale")
    private String getRamTotal() {
        String str1 = "/proc/meminfo";// 系统内存信息文件
        String str2;
        String[] arrayOfString;
        try {
            FileReader localFileReader = new FileReader(str1);
            BufferedReader localBufferedReader = new BufferedReader(localFileReader, 8192);
            str2 = localBufferedReader.readLine();// 读取meminfo第一行，系统总内存大小
            arrayOfString = str2.split(":");
            String mem = arrayOfString[1];
            localBufferedReader.close();
            if (mem != null) {
                mem = mem.toLowerCase();
                int length = mem.length();
                mem = mem.substring(0, length - 2);
                mem = mem.trim();
                return String.valueOf(Integer.valueOf(mem) / 1024) + "MB";
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "N/A";
    }

    private String getRamAvail() {// 获取android当前可用内存大小

        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(mi);
        //mi.availMem; 当前系统的可用内存
        return Formatter.formatFileSize(mContext, mi.availMem);// 将获取的内存大小规格化
    }

    /**
     * Ram类
     */
    class Ram {
        public String getRamAvail() {
            return ramAvail;
        }

        public void setRamAvail(String ramAvail) {
            this.ramAvail = ramAvail;
        }

        public String getRamTotal() {
            return ramTotal;
        }

        public void setRamTotal(String ramTotal) {
            this.ramTotal = ramTotal;
        }

        private String ramAvail;//可用ram
        private String ramTotal;//总ram

    }

    class Sdcard {
        public String getMaxSize() {
            return maxSize;
        }

        public void setMaxSize(String maxSize) {
            this.maxSize = maxSize;
        }

        public String getUsedSize() {
            return usedSize;
        }

        public void setUsedSize(String usedSize) {
            this.usedSize = usedSize;
        }
        public String getAvailSize() {
            return availSize;
        }

        public void setAvailSize(String availSize) {
            this.availSize = availSize;
        }

        //这个总大小是：总空间-系统内存占用的空间。如总空间16G，系统内存占用4.56G，则maxSize=11.44G
        private String maxSize;
        private String usedSize;
        private String availSize;


    }

}
