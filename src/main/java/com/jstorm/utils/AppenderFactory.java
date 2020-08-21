package com.jstorm.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.filter.LevelFilter;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.util.OptionHelper;
import com.jstorm.common.Constants;
import com.sun.tools.javac.util.StringUtils;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import static ch.qos.logback.core.spi.FilterReply.ACCEPT;
import static ch.qos.logback.core.spi.FilterReply.DENY;

public class AppenderFactory {

    public FileAppender createFileAppender(Level level) {

        LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();

        FileAppender appender = new FileAppender();
        //这里设置级别过滤器
        appender.addFilter(createLevelFilter(level));

        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        appender.setContext(context);
        //appender的name属性
        appender.setName("file-" + level.levelStr.toLowerCase());
        //设置文件名
        appender.setFile(OptionHelper.substVars(Constants.logPath + level.levelStr.toLowerCase() + "_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".log", context));

        appender.setAppend(true);

        appender.setPrudent(true);

        appender.setEncoder(createEncoder(context));
        appender.start();
        return appender;
    }

    private PatternLayoutEncoder createEncoder(LoggerContext context) {
        PatternLayoutEncoder encoder = new PatternLayoutEncoder();
        //设置上下文，每个logger都关联到logger上下文，默认上下文名称为default。
        // 但可以使用<contextName>设置成其他名字，用于区分不同应用程序的记录。一旦设置，不能修改。
        encoder.setContext(context);
        //设置格式
        encoder.setPattern("%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} %msg%n");
        encoder.start();
        return encoder;
    }

    private LevelFilter createLevelFilter(Level level) {
        LevelFilter levelFilter = new LevelFilter();
        levelFilter.setLevel(level);
        levelFilter.setOnMatch(ACCEPT);
        levelFilter.setOnMismatch(DENY);
        levelFilter.start();
        return levelFilter;
    }
}
