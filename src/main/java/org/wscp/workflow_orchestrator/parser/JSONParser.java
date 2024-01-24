package org.wscp.workflow_orchestrator.parser;

import com.google.gson.Gson;

import java.io.*;
import java.util.Map;
public class JSONParser implements Parser {
    Map dataParsed = null;
    InputStream input = null;

    public JSONParser() {

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
        Gson gson = new Gson();
        Reader reader = null;
        try {
            reader = new InputStreamReader(this.input, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        this.dataParsed = (Map) gson.fromJson(reader, Map.class);
    }

    @Override
    public Map getData() {
        return this.dataParsed;
    }

    public static void main(String[] args) throws FileNotFoundException {
//        String computingResourceStringJSON = "{\n" +
//                "  \"computing_resource\": {\n" +
//                "    \"hostname\": \"localhost\",\n" +
//                "    \"port\": 2022,\n" +
//                "    \"username\": \"sshuser\",\n" +
//                "    \"password\": \"123\"\n" +
//                "  }\n" +
//                "}";

        String computingResourceStringJSON = "{\n" +
                "  \"computing_resource\": null\n" +
                "}";

        Parser parser = new JSONParser();
        parser.read(new ByteArrayInputStream(computingResourceStringJSON.getBytes()));
        parser.parse();

        Map dataMap = parser.getData();
        System.out.println(dataMap.get("computing_resource"));
        Map computingResource = (Map) dataMap.get("computing_resource");
//        System.out.println(computingResource);
        System.out.println(computingResource.getClass().getSimpleName());
        int port = (Integer) ((Double) computingResource.get("port")).intValue();

        System.out.println(port);
    }
}
