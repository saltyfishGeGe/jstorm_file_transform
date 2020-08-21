package com.jstorm.common;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
* @Description: 全局变量
* @Author: xianyu
* @Date: 16:09
*/
public class Constants {

    public static String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));

    public static final String ftpBakPath = "/home/storm/CDR_TEST/bak/";

    public static final String localPath = "/app/bighead/transform/data/input/";

    public static final String localBakPath = "/app/bighead/transform/data/bak/" + today + "/";

    public static final String hdfsRootPath = "hdfs://clusterb/tmp/ODM2/";

    // 当前是测试，hdfs路径暂时自定义
    public static String hdfsPath = hdfsRootPath + "CDR_TEST2/" + today + "/";

    public static final String localErrorPath = "/app/bighead/transform/data/errorPath/" + today + "/";

    public static final Integer theadNum = 1;

    // ftp服务器ip地址资源文件
    public static final String ftpServerProperties = "ftpServers";

//    public static final String logPath = "E:/study/projects/transform/log/";
    public static final String logPath = "/app/bighead/transform/log/";
}
