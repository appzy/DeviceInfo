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
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.regex.Pattern;

import ltns.deviceinfolib.collector.base.BaseDeviceInfoCollector;

/**
 * Created by guyuepeng on 2017/6/23.
 * Email: gu.yuepeng@foxmail.com
 */

public class CpuInfoCollector extends BaseDeviceInfoCollector {
    private static final String CPU_HARDWARE = "cpuHardware";//cpu Hardware。示例：Qualcomm Technologies,Inc MSM8939
    private static final String CPU_FEATURE = "cpuFeature";//示例：swp half thumb fastmult vfp edsp neon vfpv3 tls vfpv4 idiva idivt vfpd32 evtstrm
    private static final String CPU_MODEL = "cpuModel";//cpu 类型
    private static final String CPU_ARCH = "cpu_arch";//cpu架构？ 示例：7     ？？？？？？？？FIXME：？这个我也不清楚是什么，原来代码中有的
    private static final String MAX_FREQ = "maxFreq";//最大频率
    private static final String CURRENT_FREQ = "currentFreq";//当前频率
    private static final String MIN_FREQ = "minFreq";//最小频率

    private static final String CORE_NUMBER = "coreNumber";//cpu核心数

    private static final String CPU_x = "cpuX";//cpu位数


    private static final String CPU_ABI="cpuAbi";//abi
    private static final String CPU_ABI_2="cpuAbi2";//abi2

    private static final String TOTAL_CPU_RUNNING_TIME = "totalCpuRunningTime";//cpu总运行时长
    private static final String PROCESS_CPU_RATE = "processCpuRate";//CPU占用率
    private static final String APP_CPU_TIME = "appCpuTime";//应用的cpu占用时长

    public CpuInfoCollector(Context context, String collectorName) {
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
        put(CORE_NUMBER, getCoreNumber());
        put(MAX_FREQ, getMaxCpuFreq());
        put(MIN_FREQ, getMinCpuFreq());
        put(CURRENT_FREQ, getCurCpuFreq());

        put(APP_CPU_TIME, getAppCpuTime());
        put(TOTAL_CPU_RUNNING_TIME, getTotalCpuRunningTime());
        put(PROCESS_CPU_RATE, getProcessCpuRate());

        put(CPU_ABI, CPU_ABI);
        put(CPU_ABI_2, Build.CPU_ABI2);
        put(CPU_x,getArchType(mContext));


        collectOtherCpuInfo();
    }

    @Override
    protected void doCollectManually() {

    }

    private static final String CPU_ARCHITECTURE_TYPE_32 = "32";
    private static final String CPU_ARCHITECTURE_TYPE_64 = "64";

    /** ELF文件头 e_indent[]数组文件类标识索引 */
    private static final int EI_CLASS = 4;
    /** ELF文件头 e_indent[EI_CLASS]的取值：ELFCLASS32表示32位目标 */
    private static final int ELFCLASS32 = 1;
    /** ELF文件头 e_indent[EI_CLASS]的取值：ELFCLASS64表示64位目标 */
    private static final int ELFCLASS64 = 2;

    /** The system property key of CPU arch type */
    private static final String CPU_ARCHITECTURE_KEY_64 = "ro.product.cpu.abilist64";

    /** The system libc.so file path */
    private static final String SYSTEM_LIB_C_PATH = "/system/lib/libc.so";
    private static final String SYSTEM_LIB_C_PATH_64 = "/system/lib64/libc.so";
    private static final String PROC_CPU_INFO_PATH = "/proc/cpuinfo";

    /**
     * Check if the CPU architecture is x86
     */
    public boolean checkIfCPUx86() {
        //1. Check CPU architecture: arm or x86
        if (getSystemProperty("ro.product.cpu.abi", "arm").contains("x86")) {
            //The CPU is x86
            return true;
        } else {
            return false;
        }
    }

    /**
     * cpu架构
     * Get the CPU arch type: x32 or x64
     */
    public String getArchType(Context context) {
        if (getSystemProperty(CPU_ARCHITECTURE_KEY_64, "").length() > 0) {

            return CPU_ARCHITECTURE_TYPE_64;
        } else if (isCPUInfo64()) {
            return CPU_ARCHITECTURE_TYPE_64;
        } else if (isLibc64()) {
            return CPU_ARCHITECTURE_TYPE_64;
        } else {
            return CPU_ARCHITECTURE_TYPE_32;
        }
    }

    private String getSystemProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> clazz= Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class, String.class);
            value = (String)(get.invoke(clazz, key, ""));
        } catch (Exception e) {

        }
        return value;
    }

    /**
     * Read the first line of "/proc/cpuinfo" file, and check if it is 64 bit.
     */
    private boolean isCPUInfo64() {
        File cpuInfo = new File(PROC_CPU_INFO_PATH);
        if (cpuInfo != null && cpuInfo.exists()) {
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = new FileInputStream(cpuInfo);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 512);
                String line = bufferedReader.readLine();
                if (line != null && line.length() > 0 && line.toLowerCase(Locale.US).contains("arch64")) {
                    return true;
                } else {

                }
            } catch (Throwable t) {

            } finally {
                try {
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    /**
     * Check if system libc.so is 32 bit or 64 bit
     */
    private boolean isLibc64() {
        File libcFile = new File(SYSTEM_LIB_C_PATH);
        if (libcFile != null && libcFile.exists()) {
            byte[] header = readELFHeadrIndentArray(libcFile);
            if (header != null && header[EI_CLASS] == ELFCLASS64) {

                return true;
            }
        }

        File libcFile64 = new File(SYSTEM_LIB_C_PATH_64);
        if (libcFile64 != null && libcFile64.exists()) {
            byte[] header = readELFHeadrIndentArray(libcFile64);
            if (header != null && header[EI_CLASS] == ELFCLASS64) {
                return true;
            }
        }

        return false;
    }

    /**
     * ELF文件头格式是固定的:文件开始是一个16字节的byte数组e_indent[16]
     * e_indent[4]的值可以判断ELF是32位还是64位
     */
    private byte[] readELFHeadrIndentArray(File libFile) {
        if (libFile != null && libFile.exists()) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(libFile);
                if (inputStream != null) {
                    byte[] tempBuffer = new byte[16];
                    int count = inputStream.read(tempBuffer, 0, 16);
                    if (count == 16) {
                        return tempBuffer;
                    } else {
                    }
                }
            } catch (Throwable t) {
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return null;
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
