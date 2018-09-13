package com.mispower.autoops.cloudera.initial;

import com.cloudera.api.model.ApiCommand;
import com.mispower.autoops.conf.Environments;
import com.cloudera.api.ApiRootResource;
import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.v12.ClustersResourceV12;
import com.cloudera.api.v12.RootResourceV12;

/**
 * 获取整个cloudera manager集群资源
 *
 * @author
 */

public class InitApiRootResource {

    /**
     * cloudera api 初始资源对象
     */
    private ApiRootResource apiRootResource;

    /**
     * 获取当前cloudera版本资源
     */
    private RootResourceV12 rootResourceV12;

    /**
     * 单例
     */
    private volatile static InitApiRootResource instance;

    /**
     * double checked locking
     *
     * @return
     */
    public static InitApiRootResource getInstance() {
        if (instance == null) {
            synchronized (InitApiRootResource.class) {
                if (instance == null) {
                    instance = new InitApiRootResource();
                }
            }
        }
        return instance;
    }

    public InitApiRootResource() {
        apiRootResource = new ClouderaManagerClientBuilder()
                .withHost(Environments.CDH_WEB_URL).withPort(Integer.parseInt(Environments.CDH_WEB_PORT))
                .withUsernamePassword(Environments.LOGIN_NAME, Environments.LOGIN_PWD).build();
        rootResourceV12 = apiRootResource.getRootV12();
    }

    /**
     * 获取当前cloudera版本资源
     *
     * @return RootResourceV12
     */
    private RootResourceV12 getRootResource() {

        return rootResourceV12;
    }

    /**
     * 获取cloudera manager 运行的所有集群资源
     *
     * @return ClustersResourceV12
     */
    public ClustersResourceV12 getClusterResource() {

        return getRootResource().getClustersResource();
    }


    /**
     * 回调ApiCommand
     *
     * @param commandId
     * @return
     */
    public ApiCommand readCommand(Long commandId) {
        return getRootResource().getCommandsResource().readCommand(commandId);
    }

    /**
     * 释放资源空闲资源
     */
    public void clearResources() {
        if (this.apiRootResource != null) {
            ClouderaManagerClientBuilder.clearCachedResources();
        }
    }

    /**
     * 关闭连接
     */
    public void closeClient() {
        if (this.apiRootResource != null) {
            clearResources();
            ClouderaManagerClientBuilder.closeClient(this.apiRootResource);
            if (instance != null) {
                synchronized (InitApiRootResource.class) {
                    if (instance != null) {
                        instance = null;
                    }
                }
            }
           // System.gc();
        }
    }
}
