package com.mispower.autoops.conf;

import com.mispower.autoops.utils.FileUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * load environment parameters
 *
 * @author wgl
 */
public class Environments {
    public static String CDH_WEB_URL = "10.10.1";
    public static String CDH_WEB_PORT = "7180";
    public static String LOGIN_NAME = "admin";
    public static String LOGIN_PWD = "admin";
    public static String CLUSTER_NAME = "Cluster 1";
    public static List<String> SERVICE_NAME_LIST = new ArrayList<String>();
    public static String DEFAULT_SERVICE_NAME = "hbase";

    /**
     * 加载默认参数
     */
    static {
        Properties properties = null;
        try {
            properties = FileUtil.autoReadProperties("cloudera.properties");
            CDH_WEB_URL = properties.getProperty("webUrl", CDH_WEB_URL);
            CDH_WEB_PORT = properties.getProperty("webPort", CDH_WEB_PORT);
            LOGIN_NAME = properties.getProperty("loginName", LOGIN_NAME);
            LOGIN_PWD = properties.getProperty("loginPwd", LOGIN_PWD);
            CLUSTER_NAME = properties.getProperty("clusterName", CLUSTER_NAME);
            String serviceNames = properties.getProperty("serviceName", DEFAULT_SERVICE_NAME);
            String[] serviceArray = serviceNames.split(",");
            for (String s : serviceArray) {
                SERVICE_NAME_LIST.add(s);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
