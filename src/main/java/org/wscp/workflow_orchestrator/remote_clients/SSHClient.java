package org.wscp.workflow_orchestrator.remote_clients;

import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.future.ConnectFuture;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory;
import org.apache.sshd.common.session.SessionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.EnumSet;
import java.util.concurrent.TimeUnit;


public class SSHClient {

    private SshClient sshClient;
    private ClientSession clientSession;
    private String username;
    private String host;
    private String password;
    private int port;
    private static final Logger logger = LoggerFactory.getLogger(SSHClient.class);

    private SSHClient(){}

    public SSHClient(String username, String password, String host, int port) {
        logger.info("Creating SSHClient for username: {} for {}:{}", username, host, port);
        sshClient = SshClient.setUpDefaultClient();
        sshClient.setFileSystemFactory(new VirtualFileSystemFactory() {
            @Override
            public Path getUserHomeDir(SessionContext session) {
                return Paths.get(System.getProperty("user.home"));
            }
        });
        this.username = username;
        this.password = password;
        this.host = host;
        this.port = port;
    }

    public ClientSession connect()  throws IOException {
        ConnectFuture connectFuture = sshClient.connect(username, host, port).verify();
        logger.info("SSHClient is connected: {}", connectFuture.isConnected());
        return connectFuture.getSession();
    }

    public void startClient() throws IOException {
        sshClient.start();
        clientSession = this.connect();
        clientSession.addPasswordIdentity(password);
        // Не удалять эту строчку, почему перестает нормально работать
        boolean isSucces = clientSession.auth().verify().isSuccess();
//        System.out.println("Is success ssh-connection: " + isSucces);
    }

    public void stopClient() {
        sshClient.stop();
        logger.info("SSHClient is stopped...");
    }

    public String exec(String command) throws IOException {
        ClientChannel execChannel = clientSession.createChannel(ClientChannel.CHANNEL_EXEC, command);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        execChannel.setOut(out);
        execChannel.setErr(err);
        execChannel.open().await(1, TimeUnit.SECONDS);
        Collection<ClientChannelEvent> waitMask = execChannel.waitFor(EnumSet.of(ClientChannelEvent.CLOSED), 10000);
//        waitMask.forEach(event -> System.out.println(event.name()));
//        System.out.println(execChannel.getExitStatus());
        byte[] errBytes = err.toByteArray();
        byte[] outBytes = out.toByteArray();
//        System.out.println(new String(outBytes, StandardCharsets.UTF_8));
        System.out.println(new String(errBytes, StandardCharsets.UTF_8));

        return new String(outBytes, StandardCharsets.UTF_8);
    }

    public static void main(String[] args) throws IOException {
        SSHClient sshClient = new SSHClient("sshuser", "123", "localhost", 2022);
        sshClient.startClient();
        sshClient.exec("cd calculations");
        sshClient.exec("ls");
    }
}
