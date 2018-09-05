/**
 * 
 */
package com.flyover.kube.tools.connector;

import com.flyover.kube.tools.connector.model.KubeMetadataModel;
import com.flyover.kube.tools.connector.model.RoleBindingModel;
import com.flyover.kube.tools.connector.model.RoleBindingModel.RoleRefModel;
import com.flyover.kube.tools.connector.model.RoleBindingModel.SubjectModel;

/**
 * @author mramach
 *
 */
public class RoleBinding {

	private Kubernetes kube;
	private RoleBindingModel model = new RoleBindingModel();
	
	public RoleBinding(Kubernetes kube) {
		this.kube = kube;
	}
	
	public KubeMetadataModel metadata() {
		return this.model.getMetadata();
	}
	
	public RoleBinding findOrCreate() {
		
		RoleBindingModel found = kube.find(this.model);
		
		this.model = found != null ? found : kube.create(this.model); 
		
		return this;
		
	}

	public RoleBinding roleRef(Role role) {
		
		RoleRefModel roleRef = new RoleRefModel();
		roleRef.setApiGroup("rbac.authorization.k8s.io");
		roleRef.setKind("Role");
		roleRef.setName(role.metadata().getName());
		
		this.model.setRoleRef(roleRef);
		
		return this;
		
	}
	
	public RoleBinding subject(ServiceAccount sa) {
		
		SubjectModel subject = new SubjectModel();
		subject.setKind("ServiceAccount");
		subject.setNamespace(sa.metadata().getNamespace());
		subject.setName(sa.metadata().getName());
		
		this.model.getSubjects().add(subject);
		
		return this;
		
	}

}
