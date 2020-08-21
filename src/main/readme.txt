JSTORM测试程序
    主要功能：从FTP服务器将文件下载至本地，在从本地将文件上传至HDFS

1. 打包命令
    mvn clean assembly:assembly
    线上目录结构如下：
    -- start.sh
    -- stop.sh
    -- log
        -- info.log
        -- error.log
    -- transform-1.0-SNAPSHOT-jar-with-dependencies.jar
    更新时只需替换jar包即可

2. 依赖包Jstorm-core包scope需设置成provided，否则worker节点会报出重复配置文件异常
    原因：jstorm-core下有一份默认配置文件default.yaml。若是打包进去，与worker的jstorm包会有两份配置文件从而报错
         而不打包jstorm-core包，将导致上线时无法直接启动主类提交：java -cp com.jstorm.Application
         用jstorm -jar xxx.jar com.jstorm.Application

3. 本地测试可以使用
        com.jstorm.StartTopologyTest类使用本地模式进行业务逻辑测试，pom中需删除jstorm依赖的<scope>provided</scope>
   线上启动
        com.jstorm.Application类，采用分布式运行模式，打包时需将jstorm依赖去除，详情参照第2点

