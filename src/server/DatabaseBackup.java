/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import constants.ServerConstants;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import server.Timer.WorldTimer;
import tools.FileoutputUtil;

/**
 *
 * @author Fairyms
 */
public class DatabaseBackup {

    public static DatabaseBackup instance = null;

    public static DatabaseBackup getInstance() {
        if (instance == null) {
            instance = new DatabaseBackup();
        }
        return instance;
    }

    public void startTasking() {
        File file = new File(".\\DBbackup");
        //如果文件夹不存在则创建    
        if (!file.exists() && !file.isDirectory()) {
            file.mkdir();
        } 
        WorldTimer tMan = WorldTimer.getInstance();
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                    String name = sdf.format(Calendar.getInstance().getTime());
                    Process p;
                    if (ServerConstants.isLinux) {
                        p = Runtime.getRuntime().exec(new String[]{"/bin/sh", "-c", ServerConstants.linuxDumpPath + "mysqldump -u" + ServerConstants.SQL_USER + " -p" + ServerConstants.SQL_PASSWORD + " mapleonline | gzip -9 > DBbackup/" + name + ".sql.gz"});
                    } else {
                        p = Runtime.getRuntime().exec("cmd /C \"" + ServerConstants.windowsDumpPath + "mysqldump\" -u" + ServerConstants.SQL_USER + " -p" + ServerConstants.SQL_PASSWORD + " " + ServerConstants.SQL_DATABASE + " > DBbackup\\" + name + ".sql");
                    }
                    p.getInputStream().read();
                    try {
                        p.waitFor();
                    } finally {
                        p.destroy();
                    }
                    FileoutputUtil.log("[数据库] 数据库自动完成备份.");
                } catch (IOException e) {
                    System.err.println("[数据库] 数据库自动备份失败.");
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    System.err.println("[数据库] 数据库备份发生未知的错误.");
                    e.printStackTrace();
                }
            }
        };
        tMan.register(r, ServerConstants.SQL_SAVETIME * 60 * 60 * 1000);
    }
}
