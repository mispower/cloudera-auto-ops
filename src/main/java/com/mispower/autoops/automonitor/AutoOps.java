package com.mispower.autoops.automonitor;

import com.mispower.autoops.cloudera.IClusterManager;
import com.mispower.autoops.cloudera.IServiceManager;
import com.mispower.autoops.cloudera.imp.ClusterManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 自动运维入口
 *
 * @author
 */
public class AutoOps {

    private final static Logger LOGGER = LoggerFactory.getLogger(AutoOps.class);
    protected static final AtomicBoolean isClosed = new AtomicBoolean(false);

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
    public static void main(String[] args) throws Exception {

        while (!isClosed.get()) {
            IClusterManager iClusterManager = null;
            try {
                iClusterManager = new ClusterManager();
                final List<IServiceManager> clusterServices = iClusterManager.getClusterServices();
                for (IServiceManager cs : clusterServices) {
                    if (cs.getApiRoleListUnhealthy().size() > 0) {
                        cs.executorCommand();
                        cs.clean();
                    }
                }
            } catch (Throwable e) {
               LOGGER.info(e.getMessage());
            } finally {
                if (iClusterManager != null) {
                    iClusterManager.clean();
                }
            }
        }
    }
}