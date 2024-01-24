package org.wscp.workflow_orchestrator.remote_clients.old;

import java.io.InputStream;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SSHRemoteClientSecond {
    private JSch jsch = new JSch();
    private String username;
    private String password;
    private String hostname;
    private int port;
    public SSHRemoteClientSecond(String hostname, int port)  {

        this.hostname = hostname;
        this.port = port;
    }

//    protected void finalize() throws IOException
//    {
//        this.ssh.disconnect();
//    }

    public void exec(String command) {
        try{
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");

            Session session=jsch.getSession(this.username, this.hostname, this.port);
            session.setPassword(password);
            session.setConfig(config);

            session.connect();
            System.out.println("Connected");

            Channel channel=session.openChannel("exec");
            ((ChannelExec)channel).setCommand(command);
            channel.setInputStream(null);
            ((ChannelExec)channel).setErrStream(System.err);

            InputStream in=channel.getInputStream();
            channel.connect();
            byte[] tmp=new byte[1024];
            while(true){
                while(in.available()>0){
                    int i=in.read(tmp, 0, 1024);
                    if(i<0)break;
                    System.out.print(new String(tmp, 0, i));
                }
                if(channel.isClosed()){
                    System.out.println("exit-status: "+channel.getExitStatus());
                    break;
                }
                try{Thread.sleep(1000);}catch(Exception ee){}
            }
            channel.disconnect();
            session.disconnect();
            System.out.println("DONE");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void setAuth(String username, String password) {
        this.password = password;
        this.username = username;
    }


}
