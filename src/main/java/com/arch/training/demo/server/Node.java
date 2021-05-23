package com.arch.training.demo.server;

import com.google.common.primitives.Longs;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.*;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;

/**
 * @Desctiption
 * @Author wallace
 * @Date 2021/5/18
 */
public class Node {

	public static final Node INSTANCE = new Node();
	private volatile String role = "slave";

	private CuratorFramework curatorFramework;

	private Node(){

	}

	public void start(String connectString) throws Exception{
		if(connectString == null || connectString.isEmpty()){
			throw new Exception("connectString is null or empty");
		}

		CuratorFramework client = CuratorFrameworkFactory.builder()
				.connectString(connectString).sessionTimeoutMs(5000).connectionTimeoutMs(3000)
				.retryPolicy(new ExponentialBackoffRetry(1000, 3))
				.build();

		String groupNodePath = "/com/taobao/book/operating";
		String masterNodePath = groupNodePath + "/master";
		String slaveNodePath = groupNodePath + "/slave";

		//watch master node
		PathChildrenCache pathChildrenCache = new PathChildrenCache(client, groupNodePath, true);
		pathChildrenCache.getListenable().addListener(new PathChildrenCacheListener() {
			@Override
			public void childEvent(CuratorFramework client, PathChildrenCacheEvent event) throws Exception {
				if(event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)){
					String childPath = event.getData().getPath();
					System.out.println("child removed: " + childPath);

					if(masterNodePath.equals(childPath)){
						switchMaster(client, masterNodePath, slaveNodePath);
					}
				} else if(event.getType().equals(PathChildrenCacheEvent.Type.CONNECTION_LOST)) {
					System.out.println("connection lost, become slave");
					role = "slave";
				} else if(event.getType().equals(PathChildrenCacheEvent.Type.CONNECTION_RECONNECTED)) {
					System.out.println("connection reconnected……");
					if(!becomeMaster(client, masterNodePath)){
						becomeSlave(client, slaveNodePath);
					}
				}
				else{
					System.out.println("path changed: " + event.getData().getPath() + ", event: " + event.toString());
				}

			}
		});

		client.start();
		pathChildrenCache.start();
	}

	public String getRole(){
		return role;
	}

	private boolean becomeMaster(CuratorFramework client, String masterNodePath){
		//try to become master
		try {
			client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL_SEQUENTIAL)
					.forPath(masterNodePath, Longs.toByteArray(System.currentTimeMillis()));

			System.out.println("succeeded to become master");
			role = "master";

			return true;
		} catch (Exception e) {
			System.out.println("failed to become master: " + e.getMessage());
			return false;
		}
	}

	private boolean becomeSlave(CuratorFramework client, String slaveNodePath) throws Exception {
		//try to become master
		try {
			client.create().creatingParentContainersIfNeeded().withMode(CreateMode.EPHEMERAL)
					.forPath(slaveNodePath, Longs.toByteArray(System.currentTimeMillis()));

			System.out.println("succeeded to become slave");
			role = "slave";

			return true;
		} catch (Exception e) {
			System.out.println("failed to become slave: " + e.getMessage());
			throw e;
		}
	}

	private void switchMaster(CuratorFramework client, String masterNodePath, String slaveNodePath){
		if(becomeMaster(client, masterNodePath)){
			try {
				client.delete().forPath(slaveNodePath);
			} catch (Exception e) {
				System.out.println("failed to delete slave node when switch master: " + slaveNodePath);
			}
		}
	}
}
