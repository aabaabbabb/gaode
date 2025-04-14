package com.jinyan.utils;



import com.jfinal.core.Controller;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.Ret;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;


import java.util.List;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class ControllerUtils {


    /**
     * 把model转为map，驼峰命名
     *
     * @param model
     * @return
     */
    public static Map<String, Object> modelToCamelCaseMap(Model model) {
        if (null == model) {
            return null;
        }
        String[] keys = model._getAttrNames();
        Map<String, Object> map = new HashMap<>();
        for (String key : keys) {
            Object value = model.get(key);
            key = StrKit.toCamelCase(key.toLowerCase());
//避免输出null的json，过滤掉空值
            if(null != value){
                map.put(key, value);
            }

        }
        return map;
    }

    /**
     * Record转为Map，驼峰命名
     *
     * @param record
     * @return
     */
    public static Map<String, Object> recordToCamelCaseMap(Record record) {
        if (null == record) {
            return null;
        }
        String[] keys = record.getColumnNames();
        Map<String, Object> map = new HashMap<>();
        for (String key : keys) {
            Object value = record.get(key);
            key = StrKit.toCamelCase(key.toLowerCase());
//避免输出null的json，过滤掉空值
            if(null != value){
                map.put(key, value);
            }
        }
        return map;
    }

    /**
     * List<Record>转为List<Map<String, Object>>，驼峰命名
     *
     * @param records
     * @return
     */
    public static List<Map<String, Object>> recordsToCamelCaseMaps(List<Record> records) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Record record : records) {
            maps.add(recordToCamelCaseMap(record));
        }
        return maps;
    }

    /**
     * List<? extends Model>转为List<Map<String, Object>>，驼峰命名
     *
     * @param models
     * @return
     */
    public static List<Map<String, Object>> modelsToCamelCaseMaps(List<? extends Model> models) {
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Model model : models) {
            maps.add(modelToCamelCaseMap(model));
        }
        return maps;
    }

    /**
     * Page<Record>转为Page<Map<String, Object>>，驼峰命名
     *
     * @param records
     * @return
     */
    public static Page<Map<String, Object>> recordsToCamelCaseMaps(Page<Record> records) {
        List<Record> recordList = records.getList();
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Record record : recordList) {
            maps.add(recordToCamelCaseMap(record));
        }
        return new Page<Map<String, Object>>(maps, records.getPageNumber(), records.getPageSize(),
                records.getTotalPage(), records.getTotalRow());
    }

    /**
     * Page<Page<? extends Model>转为Page<Map<String, Object>>，驼峰命名
     *
     * @param models
     * @return
     */
    public static Page<Map<String, Object>> modelsToCamelCaseMaps(Page<? extends Model> models) {
        List<? extends Model> modelList = models.getList();
        List<Map<String, Object>> maps = new ArrayList<>();
        for (Model model : modelList) {
            maps.add(modelToCamelCaseMap(model));
        }
        return new Page<Map<String, Object>>(maps, models.getPageNumber(), models.getPageSize(), models.getTotalPage(),
                models.getTotalRow());
    }

}
