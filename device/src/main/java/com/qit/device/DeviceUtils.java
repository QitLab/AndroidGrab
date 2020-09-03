package com.qit.device;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.ContentResolver;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.NetworkInfo;
import android.net.Proxy;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Locale;

import static android.content.Context.WIFI_SERVICE;

/**
 * Created by zhonglq on 2016/6/3.
 */
public class DeviceUtils {

    private static final String TAG = DeviceUtils.class.getSimpleName();

    private static final String FILE_MEMORY = "/proc/meminfo";

    private static final String FILE_CPU = "/proc/cpuinfo";

    /**
     * 获取app屏幕尺寸
     */
    public static DisplayMetrics getDisplayMetrics(Context context) {
        return context.getResources().getDisplayMetrics();
    }

    /**
     * 获取屏幕亮度
     *
     * @return
     */
    public static int getScreenBrightness(Context context) {
        int lightValue = 0;
        ContentResolver contentResolver = context.getContentResolver();
        try {
            lightValue = Settings.System.getInt(contentResolver, Settings.System.SCREEN_BRIGHTNESS);
        } catch (Settings.SettingNotFoundException e) {
            e.printStackTrace();
        }
        return lightValue;
    }

    /**
     * 获取开机到现在的毫秒数(包括睡眠时间)
     */
    public static long getElapsedRealtime() {
        return SystemClock.elapsedRealtime();
    }

    /**
     * 获取开机到现在的毫秒数(不包括睡眠时间)
     *
     * @return
     */
    public static long getUpdateMills() {
        return SystemClock.uptimeMillis();
    }

    public static int getPhoneType(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Activity.TELEPHONY_SERVICE);
        return manager.getPhoneType();
    }

    public static int getSysVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static String getNetWorkOperator(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        String networkOperator = "";
        if (manager != null) {
            networkOperator = manager.getNetworkOperator();
        }
        return networkOperator;
    }

    public static String getNetWorkOperatorName(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        String operatorName = "";
        if (manager != null) {
            operatorName = manager.getNetworkOperatorName();
        }
        return operatorName;
    }

    public static int getNetworkType(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        int networkType = -1;
        if (manager != null) {
            networkType = manager.getNetworkType();
        }
        return networkType;
    }

    /**
     * 获取imsi
     *
     * @return
     */
    public static String getIMEI(Context context) {
        TelephonyManager manager = (TelephonyManager) context
                .getSystemService(Activity.TELEPHONY_SERVICE);
        String deviceId = "";
        if (manager != null) {
            deviceId = manager.getDeviceId();
        }
        return deviceId;
    }

    /**
     * 获取imsi
     *
     * @return
     */
    public static String getIMSI(Context context) {
        TelephonyManager manager = (TelephonyManager) context.getSystemService(Activity.TELEPHONY_SERVICE);
        String subscriberId = "";
        if (manager != null) {
            subscriberId = manager.getSubscriberId();
        }
        return subscriberId;
    }

    private static String getAdressMacByInterface() {
        try {
            List<NetworkInterface> all = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface nif : all) {
                if (nif.getName().equalsIgnoreCase("wlan0")) {
                    byte[] macBytes = nif.getHardwareAddress();
                    if (macBytes == null) {
                        return "";
                    }

                    StringBuilder res1 = new StringBuilder();
                    for (byte b : macBytes) {
                        res1.append(String.format("%02X:", b));
                    }

                    if (res1.length() > 0) {
                        res1.deleteCharAt(res1.length() - 1);
                    }
                    return res1.toString();
                }
            }

        } catch (Exception e) {
            Log.e("MobileAcces", "Erreur lecture propriete Adresse MAC ");
        }
        return null;
    }

    private static String crunchifyGetStringFromStream(InputStream crunchifyStream) throws IOException {
        if (crunchifyStream != null) {
            Writer crunchifyWriter = new StringWriter();

            char[] crunchifyBuffer = new char[2048];
            try {
                Reader crunchifyReader = new BufferedReader(new InputStreamReader(crunchifyStream, StandardCharsets.UTF_8));
                int counter;
                while ((counter = crunchifyReader.read(crunchifyBuffer)) != -1) {
                    crunchifyWriter.write(crunchifyBuffer, 0, counter);
                }
            } finally {
                crunchifyStream.close();
            }
            return crunchifyWriter.toString();
        } else {
            return "No Contents";
        }
    }

    /**
     * 获取是否开启USB调试
     *
     * @return
     */
    public static boolean isOpenUSBDebug(Context context) {
        return (Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.ADB_ENABLED, 0) > 0);
    }
    /**
     * 获取cpu信息
     *
     * @return
     */
    public static String getCpuInfo() {
        try {
            FileReader fr = new FileReader(FILE_CPU);
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            for (int i = 0; i < array.length; i++) {
                Log.w(TAG, " .....   " + array[i]);
            }
            Log.w(TAG, text);
            return array[1];
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 获取手机制造商
     *
     * @return
     */
    public static String getProductName() {
        return Build.PRODUCT;
    }

    /**
     * 获取手机信息 MI 4LTE
     *
     * @return
     */
    public static String getModelName() {
        return Build.MODEL;
    }

    /**
     * 获取硬件制造商 Xiaomi
     *
     * @return
     */
    public static String getManufacturerName() {
        return Build.MANUFACTURER;
    }

    /**
     * 获取硬件名称
     *
     * @return
     */
    public static String getFingeprint() {
        return Build.FINGERPRINT;
    }

    /**
     * 获取android系统定制商
     *
     * @return
     */
    public static String getBrand() {
        return Build.BRAND;
    }

    /**
     * 获取主板信息
     *
     * @return
     */
    public static String getBoard() {
        return Build.BOARD;
    }

    /**
     * The name of the industrial design
     *
     * @return
     */
    public static String getDevice() {
        return Build.DEVICE;
    }

    /**
     * A build ID string meant for displaying to the user
     *
     * @return
     */
    public static String getDisplay() {
        return Build.DISPLAY;
    }

    /**
     * 获取serial信息
     *
     * @return
     */
    public static String getSerial() {
        return Build.SERIAL;
    }

    /**
     * 获取手机是否root
     *
     * @return
     */
    public static boolean isRoot() {
        boolean bool = false;
        try {
            bool = (new File("/system/bin/su").exists()) || (new File("/system/xbin/su").exists());
            Log.d(TAG, "bool = " + bool);
        } catch (Exception e) {
        }
        return bool;
    }

    /**
     * 是否使用vpn
     *
     * @return
     */
    public static boolean isUsingVPN() {
        if (DeviceUtils.getSysVersion() > 14) {
            String defaultHost = Proxy.getDefaultHost();
            return !TextUtils.isEmpty(defaultHost);
        }
        return false;
    }

    /**
     * 是否使用vpn
     *
     * @return
     */
    public static boolean isUsingProxyPort() {
        if (DeviceUtils.getSysVersion() > 14) {
            int defaultPort = Proxy.getDefaultPort();
            return defaultPort != -1;
        }
        return false;
    }

    public static String getDiaplay() {
        return Build.DISPLAY;
    }

    public static String getId() {
        return Build.ID;
    }

    /**
     * 获取传感器信息
     *
     * @return
     */
    public static String getSensorList(Context context) {
        JSONArray jsonArray = new JSONArray();
        // 获取传感器管理器
        SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        // 获取全部传感器列表
        List<Sensor> sensors = sensorManager.getSensorList(Sensor.TYPE_ALL);
        try {
            for (Sensor item : sensors) {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("type", String.valueOf(item.getType()));
                jsonObject.put("name", item.getName());
                jsonObject.put("version", String.valueOf(item.getVersion()));
                jsonObject.put("vendor", item.getVendor());
                jsonObject.put("maxRange", String.valueOf(item.getMaximumRange()));
                jsonObject.put("minDelay", String.valueOf(item.getMinDelay()));
                jsonObject.put("power", String.valueOf(item.getPower()));
                jsonObject.put("resolution", String.valueOf(item.getResolution()));
                jsonArray.put(jsonObject);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return jsonArray.toString();
    }

    /**
     * 获取网关IP
     *
     * @return
     */
    public static String getGateWayIp(Context context) {
        WifiManager wm = (WifiManager) context.getSystemService(WIFI_SERVICE);
        DhcpInfo di = wm.getDhcpInfo();
        return intToIp(di.gateway);
    }

    /**
     * 转换成normal ip
     *
     * @param ipInt
     * @return
     */
    private static String intToIp(int ipInt) {
        StringBuilder sb = new StringBuilder();
        sb.append(ipInt & 0xFF).append(".");
        sb.append((ipInt >> 8) & 0xFF).append(".");
        sb.append((ipInt >> 16) & 0xFF).append(".");
        sb.append((ipInt >> 24) & 0xFF);
        return sb.toString();
    }

    /**
     * 获取可用大小
     *
     * @return
     */
    public static long getSDFreeSize() {
        // 取得SD卡文件路径
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) return 0;
        try {
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());
            long blockSize = sf.getBlockSize();
            long freeBlocks = sf.getAvailableBlocks();
            // 单位Byte
            return freeBlocks * blockSize;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取sd卡总大小
     *
     * @return
     */
    public static long getSDAllSize() {
        if (!Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
            return 0;
        }
        try {
            File path = Environment.getExternalStorageDirectory();
            StatFs sf = new StatFs(path.getPath());
            long blockSize = sf.getBlockSize();
            long allBlocks = sf.getBlockCount();
            // 单位Byte
            return allBlocks * blockSize;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 获取可用内存大小
     *
     * @return
     */
    public static long getFreeMem(Context context) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Activity.ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo info = new ActivityManager.MemoryInfo();
        manager.getMemoryInfo(info);
        // 单位Byte
        return info.availMem;
    }

    /**
     * 获取总内存大小
     *
     * @return
     */
    public static long getTotalMem() {
        try {
            FileReader fr = new FileReader(FILE_MEMORY);
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split("\\s+");
            Log.w(TAG, text);
            // 单位为Byte
            return Long.valueOf(array[1]) * 1024;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return -1;
    }

    /**
     * 获取IP地址
     *
     * @return
     */
    public static String getIpAddress(Context context) {
        if (isOnline(context)) {
            if (getMobileTypeNetwork(context).equals("wifi")) {
                return getWifiIpAddress(context);
            } else {
                return getLocalIpAddress();
            }
        } else {
            return "";
        }
    }

    private static String getMobileTypeNetwork(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo == null) {
            return "other";
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return "wifi";
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_WIMAX) {
            return "wimax";
        } else if (networkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
            return networkInfo.getSubtypeName();
        } else {
            return "other";
        }
    }

    private static boolean isOnline(Context context) {
        ConnectivityManager manager = (ConnectivityManager) context
                .getSystemService(Activity.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        return info != null && info.isConnected();
    }

    /**
     * 获取local ip
     *
     * @return
     */
    private static String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf
                        .getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress() && !inetAddress.isLinkLocalAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
        }
        return null;
    }

    /**
     * 获取无线网络下的ip地址
     *
     * @return
     */
    private static String getWifiIpAddress(Context context) {
        //获取wifi服务
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }
        WifiInfo wifiInfo = wifiManager.getConnectionInfo();
        int ipAddress = wifiInfo.getIpAddress();
        return intToIp(ipAddress);
    }

    /**
     * 获取无线网络下的ip地址
     *
     * @return
     */
    public static String getWifiName(Context context) {
        if (isOnline(context) && getMobileTypeNetwork(context).equals("wifi")) {
            //获取wifi服务
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String ssid = wifiInfo.getSSID();
            if (!TextUtils.isEmpty(ssid) && ssid.contains("\"")) {
                ssid = ssid.replaceAll("\"", "");
            }
            return ssid;
        }
        return "";
    }

    /**
     * 获取默认国家/地区
     *
     * @return eg.CN或US
     */
    public static String getDefaultCountry() {
        return Locale.getDefault().getCountry();
    }

    /**
     * 获取默认语言
     *
     * @return eg.es/zh
     */
    public static String getDefaultLanguage() {
        return Locale.getDefault().getLanguage();
    }

    /**
     * 获取显示语言
     *
     * @return eg.es/zh
     */
    public static String getDefaultDisplayLanguage() {
        return Locale.getDefault().getDisplayLanguage();
    }

    /**
     * 获取显示国家
     *
     * @return eg.es/zh
     */
    public static String getDefaultDisplayCountry() {
        return Locale.getDefault().getDisplayCountry();
    }

    public static String getHost() {
        return Build.HOST;
    }

    public static String getTags() {
        return Build.TAGS;
    }
    public static String getType() {
        return Build.TYPE;
    }
    public static String getUser() {
        return Build.USER;
    }
}
