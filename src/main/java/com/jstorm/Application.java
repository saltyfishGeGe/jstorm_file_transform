package com.jstorm;

import backtype.storm.Config;
import backtype.storm.StormSubmitter;
import backtype.storm.generated.AlreadyAliveException;
import backtype.storm.generated.InvalidTopologyException;
import backtype.storm.topology.TopologyBuilder;
import com.jstorm.bolt.HdfsBolt;
import com.jstorm.spout.FtpSpout;
import com.jstorm.utils.LogBackConfigLoader;
import com.jstorm.utils.PropertiesUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
* @Description:  Jstorm 程序启动主类
* @Author: xianyu
* @Date: 15:01
*/
public class Application
{
    public static void main( String[] args ) throws AlreadyAliveException, InvalidTopologyException {
        System.out.println( "Hello Jstorm!" );

         LogBackConfigLoader.load("config/logback.xml");

        // 配置文件
        Properties ftpConf = PropertiesUtils.readPropertie("ftpConf");
        Properties hdfsConf = PropertiesUtils.readPropertie("hdfsConf");

        //创建topology的生成器
        TopologyBuilder builder = new TopologyBuilder();

        //创建Spout，注意名字中不要含有空格
        builder.setSpout("ftpCDR",new FtpSpout(ftpConf), 1).setNumTasks(1);

        builder.setBolt("hdfsCDR", new HdfsBolt(ftpConf, hdfsConf), 1).setNumTasks(1).localOrShuffleGrouping("ftpCDR");

        Map conf = new HashMap();

        //建议加上这行，使得每个bolt/spout的并发度都为1
        conf.put(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 1);
        // 运行模式
        conf.put(Config.STORM_CLUSTER_MODE, "distributed"); // 本地模式用于测试，参照StartTopologyTest

        // worker数量
        int workerNum = 1;
        conf.put(Config.TOPOLOGY_WORKERS, workerNum);

        // 指定运行的机器
        conf.put(Config.ISOLATION_SCHEDULER_MACHINES, Arrays.asList("172.17.8.163"));

        //设置表示acker的并发数
        Config.setNumAckers(conf, 0);

        // 指定zk
        conf.put(Config.STORM_ZOOKEEPER_ROOT, "/canal_test"); // 默认/jstorm，需替换成自己的zk rootPath
        conf.put(Config.STORM_ZOOKEEPER_SERVERS, Arrays.asList("172.17.8.155")); // 默认 localhost
        conf.put(Config.STORM_ZOOKEEPER_PORT, "2181");
        conf.put(Config.NIMBUS_HOST, "172.17.8.155"); // nimbus节点

        conf.put(Config.TOPOLOGY_WORKER_CHILDOPTS, "-server -Xmx160g -Xms160g -Xmn96g -Xss256k -XX:PermSize=128m -XX:MaxPermSize=128m  -XX:ReservedCodeCacheSize=128m -XX:+UseParNewGC -XX:SurvivorRatio=6 -XX:+UseConcMarkSweepGC -XX:+UseCMSInitiatingOccupancyOnly -XX:CMSInitiatingOccupancyFraction=50 -XX:ParallelGCThreads=32 -XX:+UseCMSCompactAtFullCollection -XX:CMSFullGCsBeforeCompaction=5 -XX:SoftRefLRUPolicyMSPerMB=0 -XX:+CMSParallelRemarkEnabled -XX:+UseFastAccessorMethods -XX:+UseBiasedLocking -XX:+AggressiveOpts -XX:+UseCompressedOops -XX:+ExplicitGCInvokesConcurrent -XX:+DisableExplicitGC -Djava.security.auth.login.config=/app/bighead/terrace/kdc/kafka_client_jaas.conf -Djava.security.krb5.conf=/app/bighead/terrace/kdc/krb5.conf -Djavax.security.auth.useSubjectCredsOnly=true -Dzookeeper.sasl.client=false");

        // 停止脚本通过该名称杀死topo，若替换需同步更改CDR_TEST
        StormSubmitter.submitTopology("CDR_TEST", conf,builder.createTopology());

    }
}
