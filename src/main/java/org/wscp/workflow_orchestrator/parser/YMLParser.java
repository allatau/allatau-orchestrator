package org.wscp.workflow_orchestrator.parser;

import org.yaml.snakeyaml.Yaml;

import java.io.*;
import java.util.Map;

public class YMLParser implements Parser {
    Map dataParsed = null;
    InputStream input = null;

    public YMLParser() {

    }
    @Override
    public void read(String path) {

        try {
            this.input = new FileInputStream(new File(path));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void read(InputStream input) {
        this.input = input;
    }

    @Override
    public void parse() {
        Yaml yaml = new Yaml();
        this.dataParsed = (Map) yaml.load(input);
    }

    @Override
    public Map getData() {
        return this.dataParsed;
    }

    public static void main(String[] args) throws FileNotFoundException {
        String computingResourceStringYML = "computing_resource:\n" +
                "  hostname: \"localhost\"\n" +
                "  port: 2022\n" +
                "  username: \"sshuser\"\n" +
                "  password: \"123\"";

        Parser parser = new YMLParser();
        parser.read(new ByteArrayInputStream(computingResourceStringYML.getBytes()));
        parser.parse();

        Map dataMap = parser.getData();
        System.out.println(dataMap.get("computing_resource"));
        Map computingResource = (Map) dataMap.get("computing_resource");
        System.out.println(computingResource.get("hostname"));
    }
}
