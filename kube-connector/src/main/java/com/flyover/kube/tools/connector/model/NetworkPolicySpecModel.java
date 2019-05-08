package com.flyover.kube.tools.connector.model;

import java.util.List;

public class NetworkPolicySpecModel extends Model {

    private SelectorModel podSelector;
    private List<NetworkPolicyIngressRuleModel> ingress;
    private List<NetworkPolicyEgressRuleModel> egress;

    public SelectorModel getPodSelector() {
        return podSelector;
    }

    public void setPodSelector(SelectorModel podSelector) {
        this.podSelector = podSelector;
    }

    public List<NetworkPolicyIngressRuleModel> getIngress() {
        return ingress;
    }

    public void setIngress(List<NetworkPolicyIngressRuleModel> ingress) {
        this.ingress = ingress;
    }

	public List<NetworkPolicyEgressRuleModel> getEgress() {
		return egress;
	}

	public void setEgress(List<NetworkPolicyEgressRuleModel> egress) {
		this.egress = egress;
	}

}
