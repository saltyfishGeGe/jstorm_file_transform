package com.jstorm.utils;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

public class LogBackConfigLoader {

    public static void load (String externalConfigFileLocation) {
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            InputStream resourceAsStream = LogBackConfigLoader.class.getClassLoader().getResourceAsStream(externalConfigFileLocation);
            JoranConfigurator configurator = new JoranConfigurator();
            configurator.setContext(lc);
            lc.reset();
            configurator.doConfigure(resourceAsStream);
            StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        String path = LogBackConfigLoader.class.getClassLoader().getResource("config/logback.xml").getPath();
        System.out.println(path);
        if(new File(path).exists()){
            System.out.println("读取到了1");
        }else{
            System.out.println("读取不到1");
        }

        System.out.println(System.getProperty("user.dir"));
    }
}
