package com.mispower.autoops.cloudera;

import com.cloudera.api.model.ApiRoleList;
import com.cloudera.api.model.ApiRoleNameList;
import com.cloudera.api.v10.RoleCommandsResourceV10;
import com.cloudera.api.v11.RolesResourceV11;

import java.util.List;

/**
 * 集群服务管理器
 *
 * @author
 * @describe 主要负责的是指定服务内的所有资源管理
 */
public interface IServiceManager {

    /**
     * 获取服务名称
     *
     * @return
     */
    String getServiceName();

    /**
     * 获取指定服务的角色管理器
     *
     * @return
     */
    RolesResourceV11 getRolesResourceV11();

    /**
     * 获取角色指令执行器
     *
     * @return
     */
    RoleCommandsResourceV10 getRoleCommandsResourceV10();

    /**
     * 获取指定服务中运行的所有角色
     *
     * @return
     */
    ApiRoleList getApiRoleList();


    /**
     * 获取不健康的角色列表
     *
     * @return
     */
    ApiRoleNameList getApiRoleListUnhealthy();

    /**
     * 获取不健康的角色对应的主机列表
     *
     * @return
     */
    List<String> getApiHostNameListUnhealthy();

    /**
     * 当前服务下需要重启的角色
     */
    void executorCommand();

    /**
     * 释放资源
     */
    void clean();
}
