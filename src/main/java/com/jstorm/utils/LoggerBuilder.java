package com.jstorm.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentHashMap;

public class LoggerBuilder {
    private static ConcurrentHashMap<String, Logger> container = new ConcurrentHashMap<>();

    public static Logger getLogger(Class<?> clazz) {
        String name = clazz.getName();
        Logger logger = container.get(name);
        if (logger != null) {
            return logger;
        }
        synchronized (LoggerBuilder.class) {
            logger = container.get(name);
            if (logger != null) {
                return logger;
            }
            logger = build(name,clazz);
            container.put(name, logger);
        }
        return logger;
    }

    private static Logger build(String name, Class<?> clazz) {
        FileAppender errorAppender = new AppenderFactory().createFileAppender(Level.ERROR);
        FileAppender infoAppender = new AppenderFactory().createFileAppender(Level.INFO);
        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
        Logger logger = context.getLogger(clazz + " [" + name + "]");
        //设置不向上级打印信息
        logger.setAdditive(false);
        logger.addAppender(errorAppender);
        logger.addAppender(infoAppender);

        return logger;
    }
}
