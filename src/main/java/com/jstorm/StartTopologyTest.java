package com.jstorm;

import backtype.storm.Config;
import backtype.storm.LocalCluster;
import backtype.storm.topology.TopologyBuilder;
import com.jstorm.bolt.HdfsBolt;
import com.jstorm.spout.FtpSpout;
import com.jstorm.utils.LoggerBuilder;
import com.jstorm.utils.PropertiesUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
* @Description: IDEA本地调试topology测试类
 *              测试时，需要将pom中关于jstorm的依赖加进来（即删去<scope>provided</scope>）,本地模式运行需依赖，线上分布式运行无需打包依赖
* @Author: xianyu
* @Date: 10:10
*/
public class StartTopologyTest {

    private static Logger LOG = LoggerBuilder.getLogger(StartTopologyTest.class);

    public static void main(String[] args) throws InterruptedException {
        // 配置文件
        Properties ftpConf = PropertiesUtils.readPropertie("ftpConf");
        Properties hdfsConf = PropertiesUtils.readPropertie("hdfsConf");

        //创建topology的生成器
        TopologyBuilder builder = new TopologyBuilder();

        //创建Spout，注意名字中不要含有空格
        builder.setSpout("ftpCDR",new FtpSpout(ftpConf));

        builder.setBolt("hdfsCDR", new HdfsBolt(ftpConf, hdfsConf)).localOrShuffleGrouping("ftpCDR");


        Map conf = new HashMap();

        //建议加上这行，使得每个bolt/spout的并发度都为1
//        conf.put(Config.TOPOLOGY_MAX_TASK_PARALLELISM, 1);

        LOG.info("111");

        LocalCluster cluster = new LocalCluster();

        LOG.info("妈耶");
        //提交拓扑
        cluster.submitTopology("SequenceTest", conf, builder.createTopology());

        //等待1分钟， 1分钟后会停止拓扑和集群， 视调试情况可增大该数值
        Thread.sleep(60000);

        //结束拓扑
        cluster.killTopology("SequenceTest");

        cluster.shutdown();
    }

}
