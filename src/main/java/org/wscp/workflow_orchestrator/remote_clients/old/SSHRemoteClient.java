package org.wscp.workflow_orchestrator.remote_clients.old;

import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.Session;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class SSHRemoteClient {
    private String username;
    private String password;
    private String hostname;
    private int port;
    public SSHRemoteClient(String hostname, int port) throws IOException {

        this.hostname = hostname;
        this.port = port;
    }

    public void exec(String command) throws IOException {
        SSHClient ssh = new SSHClient();
        ssh.loadKnownHosts();
        ssh.addHostKeyVerifier(new PromiscuousVerifier());
        ssh.connect(this.hostname, this.port);
        Session session = null;
        try {
            ssh.authPassword(this.username, this.password);
            session = ssh.startSession();
            BufferedReader bf = null;
            final Session.Command cmd = session.exec(command);
            bf = new BufferedReader(new InputStreamReader(cmd.getInputStream()));
            String line;
            // Imprime saida, se exister
            while ((line = bf.readLine()) != null) {
                System.out.println(line);
            }
            // Aguarda
//            cmd.join(1, TimeUnit.SECONDS);
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
            } catch (IOException e) {
                // Do Nothing
            }

            ssh.disconnect();
        }
    }

    public void setAuth(String username, String password) {
        this.password = password;
        this.username = username;
    }


}
