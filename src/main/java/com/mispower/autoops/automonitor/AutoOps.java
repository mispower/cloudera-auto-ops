package com.mispower.autoops.automonitor;

import com.mispower.autoops.cloudera.IClusterManager;
import com.mispower.autoops.cloudera.IServiceManager;
import com.mispower.autoops.cloudera.imp.ClusterManager;
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
 * @author
 */
public class AutoOps {

    private final static Logger LOGGER = LoggerFactory.getLogger(AutoOps.class);
    protected static final AtomicBoolean isClosed = new AtomicBoolean(false);

    /**
     * 检测异常，次数阈值
     */
    private final long times = 3;
    /**
     * 检测异常，时间阈值:15 Mins
     */
    private static final long detectInterval = 900000;

    private static long firstTime = System.currentTimeMillis();

    private static volatile Map<String, AtomicInteger> cache = new HashMap<>();

    private static final String ADDRESS = "sshpass -p1qaz2wsx ssh %s -oStrictHostKeyChecking=no /etc/init.d/cloudera-scm-agent restart";

    public AutoOps() {
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                isClosed.set(true);
            }
        }));
    }

    /**
     * 运行主函数入口
     *
     * @param args
     */
    public void main(String[] args) throws Exception {

        while (!isClosed.get()) {
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
                if (duration > detectInterval) {
                    cache.clear();
                    firstTime = System.currentTimeMillis();
                } else {
                    if (duration <= detectInterval) {
                        cache.forEach((s, atomicInteger) -> {
                            if (times <= atomicInteger.get()) {
                                final String address = String.format(ADDRESS, s.split(",")[0]);
                                cache.get(s).set(0);
                                try {
                                    int value = Runtime.getRuntime().exec(address).waitFor();
                                    LOGGER.info(String.format("Invoke shell return value:%s .Shell command: %s",
                                            value, address));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                } catch (InterruptedException e) {
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