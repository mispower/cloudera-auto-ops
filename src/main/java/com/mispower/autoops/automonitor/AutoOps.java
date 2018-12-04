package com.mispower.autoops.automonitor;

import com.mispower.autoops.cloudera.IClusterManager;
import com.mispower.autoops.cloudera.IServiceManager;
import com.mispower.autoops.cloudera.imp.ClusterManager;
import com.mispower.autoops.conf.Environments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自动运维入口
 *
 * @author wuguolin
 */
public class AutoOps {

    private final static Logger LOGGER = LoggerFactory.getLogger(AutoOps.class);
    private static final AtomicBoolean IS_CLOSED = new AtomicBoolean(false);

    /**
     * 检测异常，次数阈值
     */
    private static final long TIMES = 3;
    /**
     * 检测异常，时间阈值:15 Mins
     */
    private static final long DETECT_INTERVAL = 900000;

    private static long firstTime = System.currentTimeMillis();

    private static volatile Map<String, AtomicInteger> cache = new HashMap<>();

    private static final String ADDRESS = "sshpass -p%s ssh %s -oStrictHostKeyChecking=no /etc/init.d/cloudera-scm-agent restart";

    public AutoOps() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> IS_CLOSED.set(true)));
    }

    /**
     * 运行主函数入口
     *
     * @param args 参数
     */
    public static void main(String[] args) {


        while (!IS_CLOSED.get()) {
            IClusterManager iClusterManager = null;
            try {
                iClusterManager = new ClusterManager();
                final List<IServiceManager> clusterServices = iClusterManager.getClusterServices();
                for (IServiceManager cs : clusterServices) {
                    if (cs.getApiRoleListUnhealthy().size() > 0) {
                        cs.executorCommand();
                        if (cache.size() == 0) {
                            firstTime = System.currentTimeMillis();
                        }
                        for (String hName : cs.getApiHostNameListUnhealthy()) {

                            final String key = hName + "," + cs.getServiceName();
                            if (cache.containsKey(key)) {
                                cache.get(key).addAndGet(1);
                            } else {
                                cache.put(key, new AtomicInteger(1));
                            }
                        }
                        cs.clean();
                    }
                }
                iClusterManager.clean();

                long duration = System.currentTimeMillis() - firstTime;
                if (duration > DETECT_INTERVAL) {
                    cache.clear();
                    firstTime = System.currentTimeMillis();
                } else {
                    if (duration <= DETECT_INTERVAL) {
                        cache.forEach((s, atomicInteger) -> {
                            if (TIMES <= atomicInteger.get()) {
                                final String hostName = s.split(",")[0];

                                final String address = String.format(ADDRESS, Environments.getProperty(hostName), hostName);
                                cache.get(s).set(0);
                                try {
                                    int value = Runtime.getRuntime().exec(address).waitFor();
                                    LOGGER.info(String.format("Invoke shell return value:%s .Shell command: %s",
                                            value, address));
                                } catch (IOException | InterruptedException e) {
                                    e.printStackTrace();
                                }
                            }
                        });
                    }
                }
            } catch (Throwable e) {
                LOGGER.error("Error while running checking progress:", e);
            } finally {
                if (iClusterManager != null) {
                    iClusterManager.clean();
                }
            }
        }
    }
}