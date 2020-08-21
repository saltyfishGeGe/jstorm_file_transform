package com.jstorm.spout;

import backtype.storm.spout.SpoutOutputCollector;
import backtype.storm.task.TopologyContext;
import backtype.storm.topology.OutputFieldsDeclarer;
import backtype.storm.topology.base.BaseRichSpout;
import backtype.storm.tuple.Fields;
import com.jstorm.ftpreader.FtpHelper;
import com.jstorm.ftpreader.StandardFtpHelper;
import com.jstorm.task.FtpScanTask;
import com.jstorm.utils.LogBackConfigLoader;
import com.jstorm.utils.LoggerBuilder;
import com.jstorm.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FtpSpout extends BaseRichSpout {

    private static Logger LOG = null;

    private Properties ftpConf;

    public FtpSpout(Properties ftpConf) {
        this.ftpConf = ftpConf;
    }

    @Override
    public void open(Map conf, TopologyContext context, SpoutOutputCollector collector) {

//      此处不使用LogBackConfigLoader.load加载worker节点的日志路径，使用该方法会导致spout/bolt的初始化方法被重复调用两次？
        // 此处替换成手动指定日志生成路径

        LOG = LoggerBuilder.getLogger(FtpSpout.class);

        HashSet<String> ftpServers = PropertiesUtils.getFtpServers();

        ExecutorService executorService = Executors.newFixedThreadPool(ftpServers.size());

        LOG.info("当前系统配置的FTP连接个数：{}", ftpServers.size());

        for(String ftp : ftpServers){
            LOG.info("创建新FTP连接：{}", ftp);
            String ip = ftp.split(",")[0];
            String path = ftp.split(",")[1];
            FtpHelper ftpHelper = new StandardFtpHelper(ip,
                    ftpConf.getProperty("username"),
                    ftpConf.getProperty("password"),
                    Integer.valueOf(ftpConf.getProperty("port")),
                    Integer.valueOf(ftpConf.getProperty("timeout")),
                    ftpConf.getProperty("connectMode"));

            // 设置当前线程扫描路径
            ftpHelper.setScanPath(path);

            FtpScanTask ftpTask = new FtpScanTask(ftpHelper, collector);

            executorService.submit(ftpTask);
        }
    }

    @Override
    public void nextTuple() {

    }

    @Override
    public void declareOutputFields(OutputFieldsDeclarer declarer) {
        declarer.declare(new Fields("ip", "scanPath", "ftpFile"));
    }
}
