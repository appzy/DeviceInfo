package ltns.deviceinfo.utils;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * @date 创建时间：2018/1/8
 * @author appzy
 * @Description 保存到手机DeviceInfo文件目录
 * @version
 */

public class FileUtils {
    public static void saveJsonAsFile(String jsonStr, String path,String fileName) {
        File mFile=new File(path);
        if (!mFile.exists())
            mFile.mkdirs();
        FileWriter mFileWriter = null;
        try {
            mFileWriter = new FileWriter(path+fileName);
            mFileWriter.write(jsonStr);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (mFileWriter != null)
                try {
                    mFileWriter.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }

    }
}
