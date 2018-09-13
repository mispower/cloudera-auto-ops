package com.mispower.autoops.cloudera;

import java.util.List;

/**
 * 集群管理器
 *
 * @author
 * @descibe 指定cluster下所有的服务管理
 */
public interface IClusterManager {

    /**
     * 获取集群名称
     *
     * @return
     */
    String getClusterName();

    /**
     * 获取当前集群下所有服务
     *
     * @return
     */
    List<IServiceManager> getClusterServices();

    /**
     * 释放资源
     */
    void clean();
}
