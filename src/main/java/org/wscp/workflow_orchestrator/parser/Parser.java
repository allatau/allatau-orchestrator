package org.wscp.workflow_orchestrator.parser;

import java.io.InputStream;
import java.util.Map;

public interface Parser {
    public void read(String path);
    public void read(InputStream input);
    public void parse();
    public Map getData();
}
