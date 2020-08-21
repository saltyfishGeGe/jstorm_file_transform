package com.jstorm.bolt;

import backtype.storm.task.OutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichBolt;
import backtype.storm.tuple.Fields;
import backtype.storm.tuple.Tuple;
import com.jstorm.common.Constants;
import com.jstorm.ftpreader.FtpHelper;
import com.jstorm.ftpreader.StandardFtpHelper;
import com.jstorm.hdfsWriter.HdfsHelper;
import com.jstorm.spout.FtpSpout;
import com.jstorm.utils.LogBackConfigLoader;
import com.jstorm.utils.LoggerBuilder;
import com.jstorm.utils.PropertiesUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.net.ftp.FTP;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class HdfsBolt extends BaseRichBolt {

    private Properties ftpConf;

    private Properties hdfsConf;

    private Logger LOG = null;

    private Map<String, FtpHelper> helperMap = new HashMap<>();

    private HdfsHelper hdfsHelper = null;

    private OutputCollector collector;

    private Integer num = 0;

    public HdfsBolt(Properties ftpConf,Properties hdfsConf) {
        this.ftpConf = ftpConf;
        this.hdfsConf = hdfsConf;

    }

    @Override
    public void prepare(Map stormConf, TopologyContext context, OutputCollector collector) {
        // 每次调度独立生成一套日志
        LOG = LoggerBuilder.getLogger(HdfsBolt.class);

        for(String ftpInfo : PropertiesUtils.getFtpServers()){
            StandardFtpHelper ftp = new StandardFtpHelper(ftpInfo.split(",")[0],
                    ftpConf.getProperty("username"),
                    ftpConf.getProperty("password"),
                    Integer.valueOf(ftpConf.getProperty("port")),
                    Integer.valueOf(ftpConf.getProperty("timeout")),
                    ftpConf.getProperty("connectMode"));
            ftp.loginFtpServer();
            helperMap.put(ftpInfo.split(",")[0], ftp);
        }

        this.hdfsHelper = new HdfsHelper();
        this.hdfsHelper.init(hdfsConf);

        this.collector = collector;

        if(!new File(Constants.localPath).exists()){
            new File(Constants.localPath).mkdirs();
        }

        if(!new File(Constants.localBakPath).exists()){
            new File(Constants.localBakPath).mkdirs();
        }

        if(!new File(Constants.localErrorPath).exists()){
            new File(Constants.localErrorPath).mkdirs();
        }
    }

    @Override
    public void execute(Tuple input) {
//        collector.ack(input);
        String ip = input.getString(0);
        String currentFile = input.getString(2);
        LOG.info("接收到新数据，本次处理记录：{},{}", ip, currentFile);
        LOG.info("处理第{}条数据", ++num);

        FtpHelper ftpHelper = this.helperMap.get(ip);
//        if(!ftpHelper.getFtpClient().isConnected()){
//            LOG.info("bolt 登录ftp...");
//            ftpHelper.loginFtpServer();
//        }

        FileOutputStream fos = null;
        String fileName = FilenameUtils.getName(currentFile);
        // 下载时先生成本地临时文件
        String localTmpPath = Constants.localPath + fileName + ".tmp";
        String localPath = Constants.localPath + fileName;

        // 本地文件
        File ffTmp = new File(localTmpPath);
        File ff = new File(localPath);
        try {
            LocalDateTime start = LocalDateTime.now();
            LOG.info("{}开始下载文件：{}", Thread.currentThread().getName(), currentFile);

            if (!ffTmp.exists()) {
                ffTmp.createNewFile();
            } else {
                LOG.info("本地已存在同名文件，本次下载跳过...");
                return;
            }
//
            // 每次数据连接之前，ftp client告诉ftp server开通一个端口来传输数据
            // 因为ftp server可能每次开启不同的端口来传输数据，但是在linux上，由于安全限制，可能某些端口没有开启，所以就出现阻塞。
            ftpHelper.getFtpClient().enterLocalPassiveMode();
            ftpHelper.getFtpClient().setFileType(FTP.BINARY_FILE_TYPE);
            fos = new FileOutputStream(ffTmp);
            if (ftpHelper.getFtpClient().retrieveFile(currentFile, fos)) {

                fos.flush();
                LocalDateTime end = LocalDateTime.now();
                LOG.info("文件下载完成:{}，本次下载消耗时间为{}毫秒", fileName, Duration.between(start, end).toMillis());

                ffTmp.renameTo(ff);
                LOG.info("文件更名成功..{}", Constants.localPath + fileName);

            } else {
                LOG.error("文件下载失败:{}", fileName);
                // 这种错误一般是网络问题，重新跑多一次就可以了
            }

        } catch (FileNotFoundException e) {
            LOG.error("FTP文件{}不存在，下载到本地失败，理由：{}", currentFile, e);
        } catch (IOException e) {
            // 在此处偶尔会出现 IOException caught while copying. Read timed out等异常，貌似是因为网络问题
            // 若是出现频繁可以考虑加入异常重连机制来解决网络短时间不稳定的问题
            LOG.error("文件:{}, 下载到本地出错,错误信息：{}", currentFile, e);
        }

        // 4. 上传文件到HDFS
        File source = new File(localPath);
        if (!hdfsHelper.isFileExist(Constants.hdfsPath + fileName)) {
            try {
                hdfsHelper.upload2HDFS(localPath, Constants.hdfsPath + fileName);
            } catch (Exception e) {
                LOG.error("本地文件:{}, 上传至HDFS出错,错误信息：{}", localPath, e);
            }

            // 5. 移动本地文件至备份目录
            try {
                File bak = new File(Constants.localBakPath + fileName);
                // 文件移动
                source.renameTo(bak);
                LOG.info("HDFS文件上传成功，移动本地文件至备份目录:{}", bak);
            } catch (Exception e) {
                LOG.error("本地文件:{}, 移动至备份目录出错,错误信息：{}", localPath, e);
            }
        } else {
            // 若是HDFS已存在文件，则存放在本地错误目录
            try {
                File error = new File(Constants.localErrorPath + fileName);
                // 文件移动
                source.renameTo(error);
                LOG.info("HDFS文件已存在：{}，移动本地文件至错误目录:{}", Constants.hdfsPath + fileName, error);
            } catch (Exception e) {
                LOG.error("本地文件:{}, 移动至错误目录出错,错误信息：{}", localPath, e);
            }
        }
    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("ip","scanPath","ftpFile"));
    }

}
