package org.wscp.workflow_orchestrator.remote_clients;


import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;

public class SFTPClient {

    // Печать журнала
    private static final Log logger = LogFactory.getLog(SFTPClient.class);

    private static Date last_push_date = null;

    private Session sshSession;

    private ChannelSftp channel;

    private static ThreadLocal<SFTPClient> sftpLocal = new ThreadLocal<SFTPClient>();

    public SFTPClient(String host, int port, String username, String password) throws Exception {
        JSch jsch = new JSch();
        jsch.getSession(username, host, port);
        // Получение сеанса на основе имени пользователя, пароля, номера порта
        sshSession = jsch.getSession(username, host, port);
        sshSession.setPassword(password);
        // Change значение yes в GSSAPIAuthentication на сервере / etc / ssh / sshd_config на no, чтобы решить проблему, из-за которой пользователи не могут войти удаленно
        sshSession.setConfig("userauth.gssapi-with-mic", "no");

        // Установить свойства для объекта сеанса, не нужно вводить yes при первом обращении к серверу
        sshSession.setConfig("StrictHostKeyChecking", "no");
        sshSession.connect();
        // Получить канал sftp
        channel = (ChannelSftp) sshSession.openChannel("sftp");
        channel.connect();
        logger.info("Подключите FTP успешно!");
    }

    /**
     * Связано ли это
     *
     * @return
     */
    private boolean isConnected() {
        return null != channel && channel.isConnected();
    }

    /**
     * Получить клиент sftp для локального хранилища потоков
     *
     * @return
     * @throws Exception
     */
    public static SFTPClient getSftpUtil(String host, int port, String username, String password) throws Exception {
        // Получить локальный поток
        SFTPClient sftpUtil = sftpLocal.get();
        if (null == sftpUtil || !sftpUtil.isConnected()) {
            // Предотвращаем новые подключения из локальных потоков и реализуем параллельную обработку
            sftpLocal.set(new SFTPClient(host, port, username, password));
        }
        return sftpLocal.get();
    }

    /**
     * Бесплатный клиент sftp для локального хранилища потоков
     */
    public static void release() {
        if (null != sftpLocal.get()) {
            sftpLocal.get().closeChannel();
            logger.info("Закрыть соединение" + sftpLocal.get().sshSession);
            sftpLocal.set(null);

        }
    }

    /**
     * Закрыть канал
     *
     * @throws Exception
     */
    public void closeChannel() {
        if (null != channel) {
            try {
                channel.disconnect();
            } catch (Exception e) {
                logger.error("Возникла исключительная ситуация при закрытии канала SFTP:", e);
            }
        }
        if (null != sshSession) {
            try {
                sshSession.disconnect();
            } catch (Exception e) {
                logger.error("Iсключение закрытого сеанса SFTP:", e);
            }
        }
    }

    /**
     * @param directory  загрузить каталог ftp
     * @param uploadFile локальный файловый каталог
     * @param isDel      удалить оригинальный файл
     */
    public void upload(String directory, String uploadFile, boolean isDel) throws Exception {
        try {// Список выполнения показывает команду ls
            channel.ls(directory); // Выполнить команду cd переключения букв дисков
            channel.cd(directory);
            List<File> files = getFiles(uploadFile, new ArrayList<File>());
            for (int i = 0; i < files.size(); i++) {
                File file = files.get(i);
                InputStream input = new BufferedInputStream(new FileInputStream(file));
                channel.put(input, file.getName());
                try {
                    if (input != null) input.close();
                } catch (Exception e) {
                    e.printStackTrace();
                    logger.error(file.getName() + "При закрытии файла ... исключение!" + e.getMessage());
                }
                if (file.exists() && isDel) {
                    boolean b = file.delete();
                    logger.info(file.getName() + "Загрузка файла завершена! Удалить логотип:" + b);
                }
            }
        } catch (Exception e) {
            logger.error("[Создание подкаталога]:", e);
            // Создать подкаталог
            channel.mkdir(directory);
        }

    }

    public void uploadFromStream(String directory, InputStream in, String filename, boolean isDel) throws Exception {
        try {// Список выполнения показывает команду ls
            System.out.println("directory: "  + directory);
            channel.ls(directory); // Выполнить команду cd переключения букв дисков
            channel.cd(directory);


            InputStream input = in;
            channel.put(input, filename);
            try {
                if (input != null) input.close();
            } catch (Exception e) {
                e.printStackTrace();
                logger.error(filename + "При закрытии файла ... исключение!" + e.getMessage());
            }
        } catch (Exception e) {
            logger.error("[Создание подкаталога]:", e);
            // Создать подкаталог
            channel.mkdir(directory);
        }

    }

    //Получить файл
    public List<File> getFiles(String realpath, List<File> files) {
        File realFile = new File(realpath);
        if (realFile.isDirectory()) {
            File[] subfiles = realFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File file) {
                    if (null == last_push_date) {
                        return true;
                    } else {
                        long modifyDate = file.lastModified();
                        return modifyDate > last_push_date.getTime();
                    }
                }
            });
            for (File file : subfiles) {
                if (file.isDirectory()) {
                    getFiles(file.getAbsolutePath(), files);
                } else {
                    files.add(file);
                }
                if (null == last_push_date) {
                    last_push_date = new Date(file.lastModified());
                } else {
                    long modifyDate = file.lastModified();
                    if (modifyDate > last_push_date.getTime()) {
                        last_push_date = new Date(modifyDate);
                    }
                }
            }
        } else {
            files.add(realFile);
        }
        return files;
    }

    public static void main(String[] args) throws Exception {
        InputStream input = new BufferedInputStream(new FileInputStream("C:\\dev\\wcsp-test-worker\\samples\\cavity\\wcspfile.yml"));

        SFTPClient ftp = new SFTPClient("localhost", 2022, "sshuser", "123");
        ftp.uploadFromStream("/home/sshuser", input, "wcspfile.yml",  false);
//        ftp.upload("/home/sshuser", "C:\\dev\\wcsp-test-worker\\samples\\cavity\\wcspfile.yml", false);
        ftp.closeChannel();
    }
}
