package com.jstorm.task;

import backtype.storm.spout.SpoutOutputCollector;
import com.jstorm.ftpreader.FtpHelper;
import com.jstorm.ftpreader.StandardFtpHelper;
import com.jstorm.utils.LoggerBuilder;
import com.jstorm.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;

public class FtpScanTask implements Runnable {

    private FtpHelper ftpHelper = null;

    private SpoutOutputCollector collector = null;

    private Logger LOG = LoggerBuilder.getLogger(FtpScanTask.class);

    public FtpScanTask(FtpHelper ftpHelper, SpoutOutputCollector collector) {
        this.ftpHelper = ftpHelper;
        this.collector = collector;
    }

    @Override
    public void run() {
        LOG.info("开始扫描FTP数据...");
        LOG.info("连接ftp:{}", this.ftpHelper.currentIP());
        ftpHelper.loginFtpServer();

        while(true){
            //TODO 已整合ehcache, 后续可以增加扫描FTP文件时，过滤已处理文件的功能
            //TODO 可以在每次扫描前后，设定文件数量，或者无新文件时断开FTP连接等功能

            LOG.info("扫描FTP:{}文件列表", ftpHelper.currentIP());
            HashSet<String> ftpFiles = this.ftpHelper.getAllFiles(Arrays.asList(this.ftpHelper.getScanPath()), 0, 100, Arrays.asList("tmp"));
            LOG.info("本次FTP：{}，扫描文件总数:{}", ftpHelper.currentIP(), ftpFiles.size());

            if(ftpFiles != null && ftpFiles.size() > 0){
//                downloadFiles.addAll(ftpFiles);
                for(String ff : ftpFiles){
                    collector.emit(Arrays.asList(this.ftpHelper.currentIP(), this.ftpHelper.getScanPath(), ff));
                    LOG.info("发送消息..send fileInfo: {}", ff);
                }

                LOG.info("ftp:{}线程休息100s", ftpHelper.currentIP());
                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }else{
                // 如果扫描不到文件
                try {
                    LOG.info("ftp:{} 未存在新文件，线程休息20s", ftpHelper.currentIP());
//                    ftpHelper.logoutFtpServer();
                    Thread.sleep(20000);
                } catch (InterruptedException e) {
                    LOG.error("线程休息中断异常{}", e);
                }
            }
        }
    }

    public static void main(String[] args) {
//        Properties ftpConf = PropertiesUtils.readPropertie("ftpConf");
//        String ftp = "172.17.8.229,/aaa/xx";
//        System.out.println("创建新FTP连接：" + ftp);
//        String ip = ftp.split(",")[0];
//        String path = ftp.split(",")[1];
//        FtpHelper ftpHelper = new StandardFtpHelper(ip,
//                ftpConf.getProperty("username"),
//                ftpConf.getProperty("password"),
//                Integer.valueOf(ftpConf.getProperty("port")),
//                Integer.valueOf(ftpConf.getProperty("timeout")),
//                ftpConf.getProperty("connectMode"));
//
//        // 设置当前线程扫描路径
//        ftpHelper.setScanPath(path);
//        new Thread(new FtpScanTask(ftpHelper, null)).start();
    }
}
