package org.wscp.workflow_orchestrator.models;

import java.util.List;

public class Solver {
    private String software;

    private String version;
    private List<String> script;

    public String getSoftware() {
        return software;
    }

    public void setSoftware(String software) {
        this.software = software;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public List<String> getScript() {
        return script;
    }
    public String getScriptData() {
        String delim = " \n";

        String scriptData = String.join(delim, this.script);

        return scriptData;
    }

    public void setScript(List<String> script) {
        this.script = script;
    }
}
