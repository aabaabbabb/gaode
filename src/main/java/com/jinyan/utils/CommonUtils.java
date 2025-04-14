package com.jinyan.utils;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

public class CommonUtils {
     private static final Logger logger = Logger.getLogger(CommonUtils.class);
    //double 保留两位
    public static double decimal2(double f) {

        DecimalFormat df = new DecimalFormat("#.00");
        double four2 = Double.parseDouble(df.format(f));
        return four2;
    }

    /**
     * 时间戳转换日期
     *
     * @param time
     * @return
     */
    public static String transitionDate(String time) {
        Date date = new Date(Long.parseLong(time));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return simpleDateFormat.format(date);
    }

    /**
     * 日期转换时间戳
     *
     * @param time
     * @return
     */
    public static Long timestamp(String time) {
        try {
            Date parse = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(time);
            Long time1 = parse.getTime();
            return time1;
        } catch (ParseException e) {
            throw new RuntimeException("日期转换时间戳失败", e);
        }
    }
    public  static long timeStringToLong(String timeString) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = dateFormat.parse(timeString);
        long timestamp = date.getTime();
        long result = timestamp;
        return result;
    }

    public static boolean contains(int[] array, int target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i] == target) {
                return true;
            }
        }
        return false;
    }


    public static boolean contains(String[] array, String target) {
        for (int i = 0; i < array.length; i++) {
            if (array[i].equals(target) ) {
                return true;
            }
        }
        return false;
    }
    /**
     * 只要确保你的编码输入是正确的,就可以忽略掉 UnsupportedEncodingException
     */
    public static String asUrlParams(Map<String, Object> source){
        Iterator<String> it = source.keySet().iterator();
        StringBuilder paramStr = new StringBuilder();
        while (it.hasNext()){
            String key = it.next();
            Object value = source.get(key);
//            if (Strings.isNullOrEmpty(value)){
//                continue;
//            }
//            try {
//                // URL 编码
//                value = URLEncoder.encode(value, "utf-8");
//            } catch (UnsupportedEncodingException e) {
//                // do nothing
//            }
            paramStr.append("&").append(key).append("=").append(value);
        }
        // 去掉第一个&
        return paramStr.substring(1);
    }

    public static void errorException(Exception e) {

        StackTraceElement[] ste = e.getStackTrace();

        StringBuilder sb=new StringBuilder();
        for (int i = 0; i < ste.length; i++) {
            sb.append(ste[i].toString());
            sb.append("\r\n");
        }
        logger.error(sb);
    }

    public  static double FixCostPrice(double price){
        return price;
//        double one = Math.ceil(price*1.12) ;
//        String  str = String.format("%.2f",one);
//        double ret = Double.parseDouble(str);
//        return ret;
    }


}
