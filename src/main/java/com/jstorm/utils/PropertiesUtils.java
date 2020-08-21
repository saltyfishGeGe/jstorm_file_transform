package com.jstorm.utils;

import com.jstorm.common.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

public class PropertiesUtils {

    private static Logger LOG = LoggerBuilder.getLogger(PropertiesUtils.class);

    /**
     * 读取文档
     * @param propertiesName
     * @return
     */
    public static Properties readPropertie(String propertiesName) {
        Properties fileProperties = new Properties();
        try {
//            File inFile = new File("/config/" + propertiesName+".properties");
//            InputStream loadFile = new FileInputStream(inFile);
            InputStream resourceAsStream = PropertiesUtils.class.getClassLoader().getResourceAsStream("config/" + propertiesName + ".properties");
            fileProperties.load(resourceAsStream);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return fileProperties;
    }

    public static HashSet<String> getFtpServers(){
        Properties properties = PropertiesUtils.readPropertie(Constants.ftpServerProperties);
        HashSet<String> result = new HashSet<>();
        Set<Object> names = properties.keySet();
        for(Object o : names){
            if(((String)o).contains("ftp")){
                String ip = properties.getProperty(o.toString());
                result.add(ip);
                LOG.info("当前FTP连接IP：{}", ip);
            }
        }

        if(result.size() == 0){
            System.exit(0);
        }
        return result;
    }

    public static void main(String[] args){
        Properties ftpConf = PropertiesUtils.readPropertie("ftpServers");
        Set<Object> names = ftpConf.keySet();
        for(Object o : names){
            if(((String)o).contains("ftp")){
                String ip = ftpConf.getProperty(o.toString());
                System.out.println(ip);
            }
        }
    }
}
