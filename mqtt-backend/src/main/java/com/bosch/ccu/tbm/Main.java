package com.bosch.ccu.tbm;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Optional;
import java.util.Properties;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.openthinks.libs.utilities.lookup.Lookups;

public class Main {
  final static Logger LOGGER = LoggerFactory.getLogger(Main.class);
  public static final Optional<String> port = Optional.ofNullable(System.getenv("TMB-HTTP-PORT"));

  public static void main(String[] args) throws Exception {
    AppConfig appConf = loadAppConfig();
    if (args != null && args.length > 0) {
      if (args[0] != null && args[0].startsWith("-C")) {
        String configFilePath = args[0].substring(2);
        File file = new File(configFilePath);
        if (file.exists()) {
          appConf = loadAppConfig(new FileInputStream(file));
        }
      }
    }
    String contextPath = "/";
    String appBase = ".";
    Tomcat tomcat = new Tomcat();
    tomcat.setPort(Integer.valueOf(port.orElse(appConf.getHttpPort())));
    tomcat.getHost().setAppBase(appBase);
    tomcat.addWebapp(contextPath, appBase);
    addJVMSHutdownHook(tomcat);
    tomcat.start();
    tomcat.getServer().await();
  }

  private static void addJVMSHutdownHook(final Tomcat tomcat) {
    Runtime.getRuntime().addShutdownHook(new Thread("ShutdownHookThread") {
      @Override
      public void run() {
        LOGGER.info("Go to stop and destory tomcat...");
        if (tomcat != null) {
          try {
            tomcat.stop();
            tomcat.destroy();
          } catch (LifecycleException e) {
            LOGGER.error("Failed to stop and destory tomcat.", e);
          }
        }
      }
    });

  }

  private static AppConfig loadAppConfig() {
    return loadAppConfig(Main.class.getResourceAsStream("/META-INF/config/config.properties"));
  }

  private static AppConfig loadAppConfig(InputStream ins) {
    Properties props = new Properties();
    AppConfig appConf = null;
    try {
      props.load(ins);
      LOGGER.info("Load configure properties success.");
      appConf = new AppConfig(props);
      Lookups.global().register(AppConfig.class, appConf);
      LOGGER.info("Read configure properties success.");
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    return appConf;
  }
}
