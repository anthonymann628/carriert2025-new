package com.agilegeodata.carriertrack.android.utils;

import android.content.pm.PackageInfo;

import com.agilegeodata.carriertrack.android.BuildConfig;
import com.agilegeodata.carriertrack.android.activities.CTApp;
import com.agilegeodata.carriertrack.android.constants.GlobalConstants;

import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.android.LogcatAppender;
import ch.qos.logback.classic.encoder.PatternLayoutEncoder;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.rolling.FixedWindowRollingPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.SizeBasedTriggeringPolicy;
import ch.qos.logback.core.util.FileSize;
import ch.qos.logback.core.util.FileUtil;
import ch.qos.logback.core.util.StatusPrinter;

public class LoggerFileUtils{
	public static final String CARRIERTRACK_LOGGER = "AGD";
	private static boolean initialized = false;

	public static void init(){
		if(!initialized){
			configureLogbackDirectly();
		}
	}

	public static void reset(){
		initialized = false;
		configureLogbackDirectly();
	}

	static private void configureLogbackDirectly(){
		int appVersionNumber = -1;
		String appFlavor = "";
		try{
			PackageInfo pinfo = CTApp.appContext.getPackageManager().getPackageInfo(CTApp.appContext.getPackageName(), 0);
			appVersionNumber = pinfo.versionCode;
			appFlavor = BuildConfig.FLAVOR;
		}
		catch(Exception e){
			System.out.print(e.getMessage());
			//=== its ok just use -1 we know what that means here
			//logger.error("EXCEPTION : could not get app version", nnf);
		}

		//=== CHECK FOR A LOG FILE, IF NOT PRESENT CREATE AN EMPTY LOG FILE
		//=== THIS BECAME A BUG IN ONE OF THE UPDATES TO LOGJ4
		String logFileDir = FileUtils.getAppDirectoryForLogFiles() + GlobalConstants.LOGGER_FILENAME + ".txt";
		File logFile = new File(logFileDir);
		FileUtil.createMissingParentDirectories(logFile);
		try{
			if(!logFile.exists()){
				logFile.createNewFile();
				logFile.setReadable(true);
				logFile.setWritable(true);
			}
		}
		catch(IOException ioe){
			System.out.print("EXCEPTION with creating log file.");    //???
		}

		String logfilePath = "";
		try{
			logfilePath = FileUtils.getAppDirectoryForLogFiles() + GlobalConstants.LOGGER_FILENAME;
		}
		catch(Exception e){
			System.out.println("EXCEPTION : " + e.getMessage());
		}

		//=== reset the default context (which may already have been initialized)
		//=== since we want to reconfigure it
		LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
//		List<Logger> loggers = lc.getLoggerList();
		lc.reset();

		//=== SET UP THE ROLLING FILE APPENDER
		RollingFileAppender<ILoggingEvent> rollingFileAppender = new RollingFileAppender<ILoggingEvent>();
		rollingFileAppender.setContext(lc);
		rollingFileAppender.setFile(logfilePath + ".txt");

		FixedWindowRollingPolicy fixedWindowRollingPolicy = new FixedWindowRollingPolicy();
		fixedWindowRollingPolicy.setContext(lc);
		fixedWindowRollingPolicy.setMinIndex(1);
		fixedWindowRollingPolicy.setMaxIndex(3);
		fixedWindowRollingPolicy.setParent(rollingFileAppender);
		fixedWindowRollingPolicy.setFileNamePattern(logfilePath + ".%i.txt.zip");
		fixedWindowRollingPolicy.start();

		SizeBasedTriggeringPolicy<ILoggingEvent> rollingFileTriggeringPolicy = new SizeBasedTriggeringPolicy<ILoggingEvent>();
		rollingFileTriggeringPolicy.setContext(lc);

		rollingFileTriggeringPolicy.setMaxFileSize(FileSize.valueOf("5MB"));
		rollingFileTriggeringPolicy.start();

		PatternLayoutEncoder rollingFileEncoder = new PatternLayoutEncoder();
		rollingFileEncoder.setContext(lc);
		rollingFileEncoder.setPattern("%d{ddMMMyy-HH:mm:ss} " + appFlavor + " " + appVersionNumber + " %-5level %class{0}.%method : %line => %msg%n%n");
		rollingFileEncoder.start();

		//=== ADD ALL THE COMPONENTS TO THE ROLLING FILE SETUP
		rollingFileAppender.setEncoder(rollingFileEncoder);
		rollingFileAppender.setRollingPolicy(fixedWindowRollingPolicy);
		rollingFileAppender.setTriggeringPolicy(rollingFileTriggeringPolicy);
		rollingFileAppender.start();

		//=== setup LogcatAppender
		PatternLayoutEncoder logcatEncoder = new PatternLayoutEncoder();
		logcatEncoder.setContext(lc);
		logcatEncoder.setPattern("LOGCAT %d{ddMMMyy-HH:mm:ss} " + appFlavor + " " + appVersionNumber + " %-5level %class{0}.%method : %line => %msg%n%n");
		logcatEncoder.start();

		LogcatAppender logcatAppender = new LogcatAppender();
		logcatAppender.setContext(lc);
		logcatAppender.setEncoder(logcatEncoder);
		logcatAppender.start();

		//=== add the newly created appenders to the root logger;
		//=== qualify Logger to disambiguate from org.slf4j.Logger
		//ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(CARRIERTRACK_LOGGER);
		ch.qos.logback.classic.Logger root = lc.getLogger(Logger.ROOT_LOGGER_NAME);
		root.detachAndStopAllAppenders();
		root.setLevel(Level.DEBUG);//ALL);//DEBUG);
		root.addAppender(rollingFileAppender);
		root.addAppender(logcatAppender);

		StatusPrinter.print(lc);

		initialized = true;
	}
}
