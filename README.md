# jstorm_file_transform
基于jstorm框架实现文件从FTP传输至HDFS
  1. 自定义Spout，连接FTP服务进行文件扫描，将扫描到的文件信息发送去worker
  2. 自定义bolt，bolt的操作是基于spout传递来的文件信息去进行下载和上传至HDFS
