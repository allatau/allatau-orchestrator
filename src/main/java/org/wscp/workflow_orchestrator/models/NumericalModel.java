package org.wscp.workflow_orchestrator.models;

import java.util.Map;

public class NumericalModel {
    private Map payload;

    private Geometry geometry;

    public Map getPayload() {
        return payload;
    }

    public void setPayload(Map payload) {
        this.payload = payload;
    }

    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
}
