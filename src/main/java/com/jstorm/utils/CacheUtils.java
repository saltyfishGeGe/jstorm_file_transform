package com.jstorm.utils;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

/**
* @Description: 缓存工具
* @Author: xianyu
* @Date: 10:30
*/
public class CacheUtils {

    private static CacheManager cacheManager;

    static{
        cacheManager = CacheManager.create(CacheUtils.class.getClassLoader().getResourceAsStream("config/ehcache.xml"));
        Element element = new Element("fileTime", 1587099566775l); // 2020-04-17 12:59:26
        // 附上初始下载时间
        cacheManager.getCache("downloadTimestampCache").put(element);
    }

    public static Cache getDownloadCache(){
        return cacheManager.getCache("downloadTimestampCache");
    }

    public static void main(String[] args) {
        Element lastModifyTimeElem = CacheUtils.getDownloadCache().get("fileTime");
        long lastModifyTime = (long) lastModifyTimeElem.getObjectValue();
        System.out.println(lastModifyTime);
    }

}
