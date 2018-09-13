package com.mispower.autoops.cloudera.imp;

import com.mispower.autoops.cloudera.IClusterManager;
import com.cloudera.api.v11.ServicesResourceV11;
import com.mispower.autoops.cloudera.IServiceManager;
import com.mispower.autoops.cloudera.initial.InitApiRootResource;
import com.mispower.autoops.conf.Environments;

import java.util.ArrayList;
import java.util.List;

/**
 * 集群管理器
 *
 * @author
 */
public class ClusterManager implements IClusterManager {

    /**
     * 集群名称
     */
    private String clusterName;
    /**
     * 当前集群资源管理器
     */
    private ServicesResourceV11 servicesResourceV11;

    /**
     * 获取当前集群下所有服务
     */
    private List<IServiceManager> clusterServices;

    public ClusterManager() {
        this(Environments.CLUSTER_NAME);
    }

    public ClusterManager(String clusterName) {
        this.clusterName = clusterName;
        initCluster();
    }

    /**
     * 初始化集群
     */
    private void initCluster() {
        servicesResourceV11 = InitApiRootResource.getInstance().getClusterResource().getServicesResource(clusterName);
        clusterServices = new ArrayList<>();
        for (String serviceName : Environments.SERVICE_NAME_LIST) {
            clusterServices.add(new ServiceManagerImp(serviceName, servicesResourceV11));
        }
    }

    @Override
    public String getClusterName() {
        return clusterName;
    }

    @Override
    public List<IServiceManager> getClusterServices() {

        return clusterServices;
    }

    @Override
    public void clean() {
        this.clusterServices = null;
        this.servicesResourceV11 = null;
        InitApiRootResource.getInstance().closeClient();
    }
}
