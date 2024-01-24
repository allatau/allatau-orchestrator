package org.wscp.workflow_orchestrator.remote_clients.old;


import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.sftp.SFTPClient;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.IOException;

public class SFTPRemoteClient {
    public static void main(String[] args) throws IOException {
        SSHClient client = new SSHClient();
        client.addHostKeyVerifier(new PromiscuousVerifier());
        client.connect("localhost", 2022);
        client.authPassword("sshuser", "123");

        SFTPClient sftpClient = client.newSFTPClient();

        sftpClient.put("C:\\dev\\wcsp-test-worker\\samples\\cavity\\wcspfile.yml", "wcspfile.yml");

        sftpClient.close();
        client.disconnect();
    }
}
