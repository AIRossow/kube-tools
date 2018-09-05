/**
 * 
 */
package com.flyover.kube.tools.connector.model;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author mramach
 *
 */
public class PodSpecModel extends Model {

	private String serviceAccount;
	private List<ContainerModel> containers = new LinkedList<>();
	private List<VolumeModel> volumes = new LinkedList<>();
	private List<ImagePullSecretModel> imagePullSecrets = new LinkedList<>();
	private Map<String, String> nodeSelector;

	public List<ContainerModel> getContainers() {
		return containers;
	}

	public void setContainers(List<ContainerModel> containers) {
		this.containers = containers;
	}
	
	public List<VolumeModel> getVolumes() {
		return volumes;
	}

	public void setVolumes(List<VolumeModel> volumes) {
		this.volumes = volumes;
	}

	public List<ImagePullSecretModel> getImagePullSecrets() {
		return imagePullSecrets;
	}

	public void setImagePullSecrets(List<ImagePullSecretModel> imagePullSecrets) {
		this.imagePullSecrets = imagePullSecrets;
	}

	public Map<String, String> getNodeSelector() {
		return nodeSelector;
	}

	public void setNodeSelector(Map<String, String> nodeSelector) {
		this.nodeSelector = nodeSelector;
	}

	public String getServiceAccount() {
		return serviceAccount;
	}

	public void setServiceAccount(String serviceAccount) {
		this.serviceAccount = serviceAccount;
	}

	public static class ImagePullSecretModel extends Model {
		
		private String name;

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}
		
	}
	
}
