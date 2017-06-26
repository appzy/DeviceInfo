package ltns.deviceinfolib.collector;

import android.content.Context;
import android.os.Build;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/23.
 * Email: gu.yuepeng@foxmail.com
 */

public class CpuInfoCollector extends BaseDeviceInfoCollector {
    private static final String CPU_HARDWARE = "cpuHardware";
    private static final String CPU_FEATURE = "cpuFeature";
    private static final String CPU_MODEL = "cpuModel";
    private static final String CPU_ARCH = "cpuArch";//
    private static final String MAX_FREQ = "maxFreq";//最大频率
    private static final String CURRENT_FREQ = "currentFreq";//当前频率
    private static final String MIN_FREQ = "minFreq";//最小频率
    private static final String CORE_NUMBER = "coreNumber";//cpu核心数
    private static final String CPU_NAME = "cpuName";

    public CpuInfoCollector(Context context, String collectorName) {
        super(context, collectorName);
    }

    private static final String CPU_ABI="cpuAbi";
    private static final String CPU_ABI_2="cpuAbi2";

    private static final String TOTAL_CPU_RUNNING_TIME = "totalCpuRunningTime";//cpu总运行时长
    private static final String PROCESS_CPU_RATE = "processCpuRate";//CPU占用率
    private static final String APP_CPU_TIME = "appCpuTime";//应用的cpu占用时长

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
        put(CPU_NAME, getCpuName());
        put(CORE_NUMBER, getCoreNumber());
        put(MAX_FREQ, getMaxCpuFreq());
        put(MIN_FREQ, getMinCpuFreq());
        put(CURRENT_FREQ, getCurCpuFreq());

        put(APP_CPU_TIME, getAppCpuTime());
        put(TOTAL_CPU_RUNNING_TIME, getTotalCpuRunningTime());
        put(PROCESS_CPU_RATE, getProcessCpuRate());

        put(CPU_ABI, Build.CPU_ABI);
        put(CPU_ABI_2, Build.CPU_ABI2);

        collectOtherCpuInfo();
    }

    @Override
    protected void doCollectManually() {

    }


    /**
     * 获取手机CPU信息
     */
    private void collectOtherCpuInfo() {
        String str1 = "/proc/cpuinfo";
        BufferedReader localBufferedReader = null;
        try {
            FileReader fr = new FileReader(str1);
            localBufferedReader = new BufferedReader(fr);
            if (localBufferedReader != null) {
                String line = null;
                while ((line = localBufferedReader.readLine()) != null) {
                    String[] map = line.split(":");
                    if (map != null && map.length == 2) {
                        String key = map[0].trim();
                        String value = map[1].trim();
                        if (key != null && key.length() > 0 && value != null && value.length() > 0) {
                            filterKey(key, value);
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (localBufferedReader != null) {
                try {
                    localBufferedReader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void filterKey(String key, String value) {
        if (key.equalsIgnoreCase("Processor")) {
            try {
                Integer.parseInt(value);
            } catch (NumberFormatException e) {
                put(CPU_MODEL, value);
            }
        } else if (key.equalsIgnoreCase("Features")) {
            put(CPU_FEATURE, value);
        } else if (key.equalsIgnoreCase("Hardware")) {
            put(CPU_HARDWARE, value);
        } else if (key.contains("Architecture") || key.contains("architecture")) {
            put(CPU_ARCH, value);
        }

    }

    /**
     * 获取CPU最大频率（单位KHZ）
     * "/system/bin/cat" 命令行
     * "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq" 存储最大频率的文件的路径
     */
    private String getMaxCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_max_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    /**
     * 获取CPU最小频率（单位KHZ）
     */
    private String getMinCpuFreq() {
        String result = "";
        ProcessBuilder cmd;
        try {
            String[] args = {"/system/bin/cat",
                    "/sys/devices/system/cpu/cpu0/cpufreq/cpuinfo_min_freq"};
            cmd = new ProcessBuilder(args);
            Process process = cmd.start();
            InputStream in = process.getInputStream();
            byte[] re = new byte[24];
            while (in.read(re) != -1) {
                result = result + new String(re);
            }
            in.close();
        } catch (IOException ex) {
            ex.printStackTrace();
            result = "N/A";
        }
        return result.trim();
    }

    /**
     * 实时获取CPU当前频率（单位KHZ）
     *
     * @return
     */
    private String getCurCpuFreq() {
        String result = "N/A";
        try {
            FileReader fr = new FileReader(
                    "/sys/devices/system/cpu/cpu0/cpufreq/scaling_cur_freq");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 获取CPU名字
     */
    private String getCpuName() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
            }
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 如果返回-1，说明出现读数据异常
     *
     * @return
     */
    private int getCoreNumber() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }
        try {
            File dir = new File("/sys/devices/system/cpu/");
            File[] files = dir.listFiles(new CpuFilter());
            return files.length;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /**
     * @return cpu实时占用率
     */
    private int getProcessCpuRate() {
        int rate = 0;
        try {
            String Result;
            Process p;
            p = Runtime.getRuntime().exec("top -n 1");
            BufferedReader br = new BufferedReader(new InputStreamReader(p.getInputStream()));
            while ((Result = br.readLine()) != null) {
                if (Result.trim().length() < 1) {
                    continue;
                } else {
                    String[] CPUusr = Result.split("%");
                    String[] CPUusage = CPUusr[0].split("User");
                    String[] SYSusage = CPUusr[1].split("System");
                    rate = Integer.parseInt(CPUusage[1].trim()) + Integer.parseInt(SYSusage[1].trim());
                    break;
                }
            }

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(rate + "");
        return rate;
    }

    private long getTotalCpuRunningTime() { // 获取系统总CPU使用时间
        String[] cpuInfos = null;
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long totalCpu = Long.parseLong(cpuInfos[2])
                + Long.parseLong(cpuInfos[3]) + Long.parseLong(cpuInfos[4])
                + Long.parseLong(cpuInfos[6]) + Long.parseLong(cpuInfos[5])
                + Long.parseLong(cpuInfos[7]) + Long.parseLong(cpuInfos[8]);
        return totalCpu;
    }

    private long getAppCpuTime() { // 获取应用占用的CPU时间
        String[] cpuInfos = null;
        try {
            int pid = android.os.Process.myPid();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new FileInputStream("/proc/" + pid + "/stat")), 1000);
            String load = reader.readLine();
            reader.close();
            cpuInfos = load.split(" ");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long appCpuTime = Long.parseLong(cpuInfos[13])
                + Long.parseLong(cpuInfos[14]) + Long.parseLong(cpuInfos[15])
                + Long.parseLong(cpuInfos[16]);
        return appCpuTime;
    }


}
