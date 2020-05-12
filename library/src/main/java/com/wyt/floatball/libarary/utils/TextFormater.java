package com.wyt.floatball.libarary.utils;

/**
 * @description 内存大小单位的自动转换
 * @date: 2020/4/22
 * @author:
 */
public class TextFormater {
    public static String dataSizeFormat(long size) {
        long kb = 1024;
        long mb = kb * 1024;
        long gb = mb * 1024;

        if (size >= gb) {
            return String.format("%.2f GB", (float) size / gb);
        }
        else if (size >= mb) {
            float f = (float) size / mb;
            return String.format(f > 100 ? "%.0f MB" : "%.1f MB", f);
        }
        else if (size >= kb) {
            float f = (float) size / kb;
            return String.format(f > 100 ? "%.0f KB" : "%.1f KB", f);
        }
        else {
            return String.format("%d B", size);
        }
    }
}
