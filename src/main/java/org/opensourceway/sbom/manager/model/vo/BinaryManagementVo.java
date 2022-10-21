package org.opensourceway.sbom.manager.model.vo;

import org.opensourceway.sbom.manager.model.ExternalPurlRef;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

public class BinaryManagementVo implements Serializable {

    private List<ExternalPurlRef> packageList = Collections.emptyList();

    private List<ExternalPurlRef> provideList = Collections.emptyList();

    private List<ExternalPurlRef> externalList = Collections.emptyList();

    private List<ExternalPurlRef> relationshipList = Collections.emptyList();

    public List<ExternalPurlRef> getPackageList() {
        return packageList;
    }

    public void setPackageList(List<ExternalPurlRef> packageList) {
        this.packageList = packageList;
    }

    public List<ExternalPurlRef> getProvideList() {
        return provideList;
    }

    public void setProvideList(List<ExternalPurlRef> provideList) {
        this.provideList = provideList;
    }

    public List<ExternalPurlRef> getExternalList() {
        return externalList;
    }

    public void setExternalList(List<ExternalPurlRef> externalList) {
        this.externalList = externalList;
    }

    public List<ExternalPurlRef> getRelationshipList() {
        return relationshipList;
    }

    public void setRelationshipList(List<ExternalPurlRef> relationshipList) {
        this.relationshipList = relationshipList;
    }
}
