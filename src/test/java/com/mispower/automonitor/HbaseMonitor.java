package com.mispower.automonitor;

import com.cloudera.api.ApiRootResource;
import com.cloudera.api.ClouderaManagerClientBuilder;
import com.cloudera.api.v11.RolesResourceV11;
import com.cloudera.api.v11.ServicesResourceV11;
import com.cloudera.api.v12.ClustersResourceV12;
import com.cloudera.api.v12.RootResourceV12;

import com.cloudera.api.model.*;

import java.lang.reflect.InvocationTargetException;

public class HbaseMonitor {
    private static String cdhApiPath = "10.10.11.11";
    private static String cdhApiPort = "7180";
    private static String cdhApiUserName = "admin";
    private static String cdhApiPassword = "!qaz@wsx";
    private static String CLUSTERNAME = "Cluster 1";

    public static void main(String[] args) throws InterruptedException, ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

        ApiRootResource root = new ClouderaManagerClientBuilder()
                .withHost(cdhApiPath).withPort(Integer.parseInt(cdhApiPort))
                .withUsernamePassword(cdhApiUserName, cdhApiPassword).build();


//        System.out.println(r1.getHostsResource().readHosts(DataView.SUMMARY));
        //获取当前cloudera版本信息
        RootResourceV12 r12 = root.getRootV12();
        //获取当前的集群资源
        ClustersResourceV12 cr12 = r12.getClustersResource();
        //根据集群名称获取集群上的资源操作类
        ServicesResourceV11 srv11 = cr12.getServicesResource(CLUSTERNAME);

        //  srv11.readService("",DataView.SUMMARY);

        //获取角色资源
        RolesResourceV11 rolesResourceV11 = srv11.getRolesResource("hbase");


        //重启指定服务：hbase ,hdfs,spark等
        //ApiCommand apiCommand = srv11.restartCommand("hbase");


        //RoleCommandsResource 启动指定role的程序
//        List<String> name = new ArrayList<String>();
//        name.add("hbase-REGIONSERVER-a6efcd83821a518db89352071724cf3b");
//        srv11.getRoleCommandsResource("hbase").restartCommand(new ApiRoleNameList(name));


        //获取Regionserver
        ApiRoleList apiRoleList = rolesResourceV11.readRoles();
        for (ApiRole ac : apiRoleList) {

            if ("regionserver".equals(ac.getType().toLowerCase())) {

                System.out.println(ac);
                System.out.println(ac.getHealthSummary() == ApiHealthSummary.CONCERNING);

                //重启异常角色
//                List<String> name = new ArrayList<String>();
//                name.add(ac.getName());
//                srv11.getRoleCommandsResource("hbase").restartCommand(new ApiRoleNameList(name));
            }
        }


//        ApiCommand ac = new ApiCommand(1L, "33333", new Date(), new Date(), true,
//                false, "", "", clusterRef, apiServiceRef,
//                null, apiHostRef, null, null, true);
//
//        while (ac.isActive()) {
//            Thread.sleep(100);
//            System.out.println(ac);
//            ac = r12.getCommandsResource().readCommand(ac.getId());
//            System.out.println(ac);
//        }
//        //集群所有的host信息
//        List<ApiHost> apiHosts = r12.getHostsResource().readHosts(DataView.SUMMARY).getHosts();
//        for (ApiHost ah : apiHosts) {
//            if (ah.getHostId().equals(hostId)) {
//                System.out.println(ah);
//            }
//        }


    }
}

