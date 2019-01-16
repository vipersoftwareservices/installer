/*
 * -----------------------------------------------------------------------------
 *               VIPER SOFTWARE SERVICES
 * -----------------------------------------------------------------------------
 *
 * @(#)filename.java	1.00 2012/01/15
 *
 * Copyright 1998-2012 by Viper Software Services
 * All rights reserved.
 *
 * This software is the confidential and proprietary information
 * of Viper Software Services. ("Confidential Information").  You
 * shall not disclose such Confidential Information and shall use
 * it only in accordance with the terms of the license agreement
 * you entered into with Viper Software Services.
 *
 * @author Tom Nevin (TomNevin@pacbell.net)
 *
 * @version 1.0, 01/15/2012 
 *
 * @note 
 *        
 * -----------------------------------------------------------------------------
 */

package com.viper.installer.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class CSVUtil {

    private static final Logger log = Logger.getLogger(CSVUtil.class.getName());

    public static List<Map<String, String>> toListOfMaps(String str, String lineDelimiter, String fieldDelimiter) {

        List<Map<String, String>> rows = new ArrayList<Map<String, String>>();
        String[] strs = str.split(lineDelimiter);
        String[] names = strs[0].split(fieldDelimiter);

        for (int i = 1; i < strs.length; i++) {
            String[] cells = strs[i].split(fieldDelimiter);
            Map<String, String> map = new HashMap<String, String>();
            for (int j = 0; j < cells.length; j++) {
                map.put(names[j], cells[j]);
            }
            rows.add(map);
        }
        return rows;
    }

    public static List<List<String>> toListOfList(String str, String lineDelimiter, String fieldDelimiter) {

        List<List<String>> rows = new ArrayList<List<String>>();
        String[] strs = str.split(lineDelimiter);

        for (int i = 0; i < strs.length; i++) {
            String[] cells = strs[i].split(fieldDelimiter);
            List<String> list = new ArrayList<String>();
            for (int j = 0; j < cells.length; j++) {
                list.add(cells[j]);
            }
            rows.add(list);
        }
        return rows;
    }

    public static <T> List<T> toListOfBeans(Class<T> cls, String str, String lineDelimiter, String fieldDelimiter)
            throws Exception {

        List<T> rows = new ArrayList<T>();
        String[] strs = str.split(lineDelimiter);
        String[] names = strs[0].split(fieldDelimiter);

        for (int i = 1; i < strs.length; i++) {
            String[] cells = strs[i].split(fieldDelimiter);
            T bean = cls.newInstance();
            for (int j = 0; j < cells.length; j++) {
                set(bean, names[j], cells[j]);
            }
            rows.add(bean);
        }
        return rows;
    }

    public static List<Map<String, String>> toListOfMapsFromFields(String str, String lineDelimiter, String fieldDelimiter) {

        List<Map<String, String>> items = new ArrayList<Map<String, String>>();
        String[] rows = str.split(lineDelimiter);
        String[] names = rows[0].trim().split(fieldDelimiter);

        for (int i = 1; i < rows.length; i++) {
            String[] cells = rows[i].trim().split(fieldDelimiter);
            Map<String, String> map = new HashMap<String, String>();
            for (int j = 0; j < cells.length; j++) {
                map.put(names[j], cells[j]);
            }
            items.add(map);
        }
        return items;
    }

    public static List<List<String>> toListOfListFromFields(String str, String lineDelimiter, String fieldDelimiter) {

        List<List<String>> rows = new ArrayList<List<String>>();
        String[] strs = str.split(lineDelimiter);

        List<Integer> fieldIndex = findFieldIndex(strs[0], fieldDelimiter);

        for (int i = 0; i < strs.length; i++) {
            List<String> cells = split(strs[i], fieldIndex);
            List<String> list = new ArrayList<String>();
            for (int j = 0; j < cells.size(); j++) {
                list.add(cells.get(j));
            }
            rows.add(list);
        }
        return rows;
    }

    public static <T> List<T> toListOfBeansFromFields(Class<T> cls, String str, String lineDelimiter, String fieldDelimiter)
            throws Exception {

        List<T> rows = new ArrayList<T>();
        String[] strs = str.split(lineDelimiter);

        List<Integer> fieldIndex = findFieldIndex(strs[0], fieldDelimiter);
        List<String> names = split(strs[0], fieldIndex);

        for (int i = 1; i < strs.length; i++) {
            List<String> cells = split(strs[i], fieldIndex);
            T bean = cls.newInstance();
            for (int j = 0; j < cells.size(); j++) {
                set(bean, names.get(j), cells.get(j));
            }
            rows.add(bean);
        }
        return rows;
    }

    public static <T> boolean set(T bean, String fieldname, Object fieldValue) {
        Class<?> clazz = bean.getClass();
        while (clazz != null && !clazz.getPackage().getName().startsWith("java.")) {
            try {
                Field field = clazz.getDeclaredField(fieldname);
                field.setAccessible(true);
                field.set(bean, fieldValue);
                return true;
            } catch (NoSuchFieldException e) {
                clazz = clazz.getSuperclass();
            } catch (Exception e) {
                log.severe("setValue failed for " + clazz + "." + fieldname + "=" + fieldValue + ": ERROR " + e);
                throw new IllegalStateException(e);
            }
        }
        return false;
    }

    private static List<Integer> findFieldIndex(String str, String delimiters) {
        List<Integer> indicies = new ArrayList<Integer>();
        boolean foundDelimiter = true;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (foundDelimiter) {
                if (delimiters.indexOf(c) == -1) {
                    foundDelimiter = false;
                    indicies.add(i);
                }
            } else {
                if (delimiters.indexOf(c) != -1) {
                    foundDelimiter = true;
                }
            }
        }

        return indicies;
    }

    private static List<String> split(String str, List<Integer> fieldIndex) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < fieldIndex.size(); i++) {
            Integer index1 = fieldIndex.get(i);
            if ((i + 1) >= fieldIndex.size()) {
                list.add(str.substring(index1).trim());
            } else {
                Integer index2 = fieldIndex.get(i + 1);
                list.add(str.substring(index1, index2).trim());
            }
        }
        return list;
    }
}
