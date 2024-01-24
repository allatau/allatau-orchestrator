package org.wscp.clients;


import io.micronaut.configuration.picocli.PicocliRunner;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(name = "demo", description = "...",
        mixinStandardHelpOptions = true)
public class TestMicronautClient implements Runnable {

    @Option(names = {"-v", "--verbose"}, description = "...")
    boolean verbose;

    public static void main(String[] args) throws Exception {
        PicocliRunner.run(TestMicronautClient.class, args);
    }

    public void run() {
        // business logic here
//        if (verbose) {
            System.out.println("Hi1!");
//        }
    }
}

