package com.mispower.autoops.cloudera.imp;

import com.cloudera.api.model.*;
import com.cloudera.api.v10.HostsResourceV10;
import com.cloudera.api.v10.RoleCommandsResourceV10;
import com.cloudera.api.v11.RolesResourceV11;
import com.cloudera.api.v11.ServicesResourceV11;
import com.mispower.autoops.cloudera.IServiceManager;
import com.mispower.autoops.cloudera.initial.InitApiRootResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Consumer;

/**
 * 集群上运行服务包含的信息
 *
 * @author
 */
public class ServiceManagerImp implements IServiceManager {


    private final static Logger LOGGER = LoggerFactory.getLogger(ServiceManagerImp.class);

    private String serviceName;
    private HostsResourceV10 hostResource;
    private RolesResourceV11 rolesResourceV11;
    private RoleCommandsResourceV10 roleCommandsResourceV10;
    private Tuple<List<String>> listTuple;


    /**
     * 构造函数
     *
     * @param serviceName
     */
    public ServiceManagerImp(String serviceName, String clusterName) {

        final InitApiRootResource root = InitApiRootResource.getInstance();
        final ServicesResourceV11 servicesResourceV11 = root.getClusterResource().getServicesResource(clusterName);
        this.hostResource = root.getHostsResource();
        this.serviceName = serviceName;
        this.rolesResourceV11 = servicesResourceV11.getRolesResource(serviceName);
        this.roleCommandsResourceV10 = servicesResourceV11.getRoleCommandsResource(serviceName);
        processData();
    }

    /**
     * 获取服务名
     *
     * @return
     */
    @Override
    public String getServiceName() {
        return serviceName;
    }

    /**
     * 获取角色管理器
     *
     * @return
     */
    @Override
    public RolesResourceV11 getRolesResourceV11() {
        return this.rolesResourceV11;
    }


    /**
     * 获取角色指令执行器
     *
     * @return
     */
    @Override
    public RoleCommandsResourceV10 getRoleCommandsResourceV10() {
        return this.roleCommandsResourceV10;
    }

    /**
     * 获取指定服务中运行的所有角色
     *
     * @return
     */
    @Override
    public ApiRoleList getApiRoleList() {
        return this.rolesResourceV11.readRoles();
    }


    /**
     * 获取不健康的角色列表
     *
     * @return
     */
    @Override
    public ApiRoleNameList getApiRoleListUnhealthy() {

        ApiRoleNameList apiRoleNameList = new ApiRoleNameList(listTuple.get(0));
        return apiRoleNameList;
    }

    /**
     * 数据处理
     */
    private void processData() {
        List<String> roles = new ArrayList<>();
        List<String> hostIds = new ArrayList<>();
        ApiRoleState apiRoleState;
        ApiHealthSummary apiHealthSummary;
        List<ApiRole> apiRoleList = getApiRoleList().getRoles();
        for (ApiRole ar : apiRoleList) {
            apiRoleState = ar.getRoleState();
            apiHealthSummary = ar.getHealthSummary();
            boolean healthy = (apiRoleState == ApiRoleState.STARTING || apiRoleState == ApiRoleState.STOPPING || apiRoleState == ApiRoleState.STARTED
                    || apiRoleState == ApiRoleState.BUSY) && (apiHealthSummary == ApiHealthSummary.CONCERNING
                    || apiHealthSummary == ApiHealthSummary.GOOD);
            if (!healthy) {
                roles.add(ar.getName());
                hostIds.add(ar.getHostRef().getHostId());
            }
        }
        listTuple = new Tuple<>(roles, hostIds);
    }

    @Override
    public List<String> getApiHostNameListUnhealthy() {

        List<String> hostNames = new ArrayList<>();
        List<String> hostIds = listTuple.get(1);
        for (String id : hostIds) {
            final String name = hostResource.readHost(id).getHostname();
            hostNames.add(name.substring(0, name.indexOf(".")));
        }
        return hostNames;
    }

    /**
     * 当前服务下需要重启的角色
     */
    @Override
    public synchronized void executorCommand() {
        ApiBulkCommandList apiBulkCommandList = this.roleCommandsResourceV10.restartCommand(getApiRoleListUnhealthy());
        List<ApiCommand> apiCommands = apiBulkCommandList.getCommands();
        //回调函数,检测command状态
        apiCommands.forEach(apiCommand -> {
            try {
                while (apiCommand.isActive()) {
                    apiCommand = InitApiRootResource.getInstance().readCommand(apiCommand.getId());
                    Thread.sleep(50);
                }
                LOGGER.info(apiCommand.toString());
            } catch (Throwable ex) {
                LOGGER.info(ex.getMessage());
            }
        });
    }


    /**
     * 获取当前服务下的所有角色类型
     *
     * @return
     */
    private Set<String> getRoleType() {
        Set<String> roleTypes = new TreeSet<>();
        for (ApiRole ar : getApiRoleList().getRoles()) {
            roleTypes.add(ar.getType());
        }
        return roleTypes;
    }

    /**
     * 根据获取指定服务中指定role类型获取指定的role信息
     *
     * @param type：like {regionserver,master,hdfs ...}
     * @return
     */
    private List<ApiRole> getApiRoleByType(String type) {
        List<ApiRole> roleList = new ArrayList<ApiRole>();
        for (ApiRole ar : getApiRoleList().getRoles()) {
            if (ar.getType().toLowerCase().equals(type.toLowerCase())) {
                roleList.add(ar);
            }
        }
        return roleList;
    }

    @Override
    public void clean() {
        listTuple = null;
        hostResource = null;
        rolesResourceV11 = null;
        roleCommandsResourceV10 = null;
    }

    class Tuple<T> {
        private T[] ars;

        Tuple(T... ars) {
            this.ars = ars;
        }

        public T get(int index) {
            assert index < 0 || index > ars.length;
            return ars[index];
        }
    }
}
