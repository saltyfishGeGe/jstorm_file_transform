#!bin/sh
#java -Dstorm.jar=transform-1.0-SNAPSHOT.jar -cp transform-1.0-SNAPSHOT.jar:lib/* com.jstorm.Application
#这儿是不能使用主类提交topology
#1.若是主类提交，则需要将jstorm/storm的依赖包打包进来，但是在worker节点时会导致default.yaml文件造成冲突
#2.所以为了避免配置冲突，将jstorm依赖设置成provided，打包时不进行集成，所以通过主类启动时会报classNotFind异常，所以此处采用jstorm jar的方式进行提交
#java -Dstorm.jar=transform-1.0-SNAPSHOT-jar-with-dependencies.jar -cp transform-1.0-SNAPSHOT-jar-with-dependencies.jar com.jstorm.Application
jstorm jar transform-1.0-SNAPSHOT-jar-with-dependencies.jar com.jstorm.Application