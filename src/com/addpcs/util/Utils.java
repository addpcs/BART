/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addpcs.util;

import com.addpcs.util.win32.Kernel32w;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Justin
 */
public class Utils {

    private static File tempDir;

    public static String byteToString(long bytes) //Convert bytes to a user friendly string
    {
        if (bytes > 1024 * 1024 * 1024) {
            return "" + round((double) bytes / (1024 * 1024 * 1024), 3) + " GB";
        } else if (bytes > 1024 * 1024) {
            return "" + round((double) bytes / (1024 * 1024), 3) + " MB";
        } else if (bytes > 1024) {
            return "" + round((double) bytes / 1024, 3) + " KB";
        } else {
            return "" + bytes + " Bytes";
        }
    }

    public static int hexToInt(String hex) {
        String ch = "0123456789abcdef";
        hex = hex.toLowerCase();
        if (hex.length() > 2 && hex.substring(0, 2).equals("0x")) {
            hex = hex.substring(2);
        }
        int p = 0;
        int val = 0;
        for (int i = hex.length() - 1; i >= 0; i--) {
            if (ch.indexOf(hex.charAt(i)) == -1) {
                return -1;
            }
            val += ch.indexOf(hex.charAt(i)) * Math.pow(16, p);
            p++;
        }
        return val;
    }

    public static double round(double num, int digits) {
        return (double) Math.round(num * Math.pow(10, digits)) / Math.pow(10, digits);
    }

    public static File[] getValidDrives() {
        File[] temp = File.listRoots();
        File[] temp2 = null;
        int num = 0;
        int num2 = 0;
        if (temp != null) {
            for (int i = 0; i < temp.length; i++) {
                if (temp[i].getTotalSpace() > 0) {
                    num++;
                }
            }
        }

        if (isLinux()) {
            temp2 = new File("/media/").listFiles();
            if (temp2 != null) {
                for (int i = 0; i < temp2.length; i++) {
                    if (temp2[i].isDirectory() && temp2[i].list() != null && temp2[i].list().length > 0 && temp2[i].getTotalSpace() > 0) {
                        num2++;
                    }
                }
            }
        }

        File[] ret = new File[(num + num2)];
        num = 0;
        if (temp != null) {
            for (int i = 0; i < temp.length; i++) {
                if (temp[i].getTotalSpace() > 0) {
                    ret[num] = temp[i];
                    num++;
                }
            }
        }
        if (temp2 != null) {
            for (int i = 0; i < temp2.length; i++) {
                if (temp2[i].isDirectory() && temp2[i].list() != null && temp2[i].list().length > 0 && temp2[i].getTotalSpace() > 0) {
                    ret[num] = temp2[i];
                    num++;
                }
            }
        }
        return ret;
    }

    public static boolean isVista() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("vista");
    }

    public static boolean isXP() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("xp");
    }

    public static boolean isWin() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("windows");
    }

    public static boolean isLinux() {
        String os = System.getProperty("os.name");
        return os.toLowerCase().contains("linux");
    }

    public static String fileExt(File src) {
        return fileExt(src.getName());
    }

    public static String fileExt(String filename) {
        int dot = filename.lastIndexOf(".");
        if (dot > 0) {
            return filename.toLowerCase().substring(dot);
        } else {
            return "~";
        }
    }

    public static void filePerms(File f) {
        // subinacl /subdirectories "full path to dir\*.*" /setowner=Administrators
        // xcacls "full path to dir" /G Administrators:F /C /T /Y
        if (f == null) {
            return;
        }
        if (isWin()) {
            File sa = extract("/com/addpcs/util/setACL/SetACL.exe");
            if (sa != null && sa.exists()) {
                runWait(sa.getPath(), "-on", f.getAbsolutePath(), "-ot", "file", "-actn", "setOwner", "-ownr", "n:S-1-5-32-544", "-rec", "cont_obj");
                runWait(sa.getPath(), "-on", f.getAbsolutePath(), "-ot", "file", "-actn", "ace", "-ace", "n:S-1-5-32-544;p:full;s:y", "-rec", "cont_obj");
            }
        } else {
            if (f.isDirectory()) {
                f.setExecutable(true, false);
                f.setReadable(true, false);
                f.setWritable(true, false);
                File children[] = f.listFiles();
                if (children != null) {
                    for (File child : children) {
                        filePerms(child);
                    }
                }
            } else {
                f.setReadable(true, false);
                f.setWritable(true, false);
            }
        }
    }

    public static File extract(String res) {
        try {
            DataInputStream in = new DataInputStream(new BufferedInputStream(Utils.class.getResource(res).openStream()));
            File dest = new File(temp(), res.substring(res.lastIndexOf("/")));
            //System.out.println(dest.getAbsolutePath());
            if (!dest.exists()) {
                DataOutputStream out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(dest)));
                byte[] buffer = new byte[1024];
                int len;
                while ((len = in.read(buffer)) >= 0) {
                    out.write(buffer, 0, len);
                }
                out.close();
            }
            in.close();
            dest.deleteOnExit();
            return dest;
        } catch (IOException e) {
            return null;
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public static File root(File path) {
        while (path.getParentFile() != null) {
            path = path.getParentFile();
        }
        return path;
    }

    public static File root() {
        return root(new File(System.getProperty("user.home")));
    }

    public static File temp(long minSpace) {
        if (tempDir != null && tempDir.exists() && (tempDir.getUsableSpace() >= minSpace) && !detectRO(tempDir)) {
            return tempDir;
        }

        // Use %temp%
        String env = Kernel32w.GetLongPathName(Kernel32w.GetEnvironmentVariable("temp"));
        if (env != null) {
            tempDir = new File(env);
            if (tempDir.exists() && (tempDir.getUsableSpace() >= minSpace) && !detectRO(tempDir)) {
                return tempDir;
            }
        }

        tempDir = new File("c:\\temp");
        tempDir.mkdirs();
        if (tempDir.exists() && (tempDir.getUsableSpace() >= minSpace) && !detectRO(tempDir)) {
            return tempDir;
        }

        tempDir = new File(System.getProperty("user.home"), "\\Temp");
        if (tempDir.exists() && (tempDir.getUsableSpace() >= minSpace) && !detectRO(tempDir)) {
            return tempDir;
        }
        return tempDir;
    }

    public static File temp() {
        return temp(100 * 1024 * 1024);
    }

    public static int runWait(File wkDir, String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(wkDir);
            Process p = pb.start();
            BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
            String line;
            while ((line = input.readLine()) != null) {
                //writeDetail(line);
            }
            input.close();
            p.waitFor();
            return p.exitValue();
        } catch (Exception ex) {
            //Error of sorts
        }
        return -1;
    }

    public static int runWait(String... cmd) {
        return runWait(null, cmd);
    }

    public static Process run(File wkDir, String... cmd) {
        try {
            ProcessBuilder pb = new ProcessBuilder(cmd);
            pb.directory(wkDir);
            pb.redirectErrorStream(true);
            Process p = pb.start();
            return p;
        } catch (Exception ex) {
            //Error of sorts
        }
        return null;
    }

    public static Process run(String... cmd) {
        return run(null, cmd);
    }

    public static boolean detectRO(File destDir) {
        boolean writable = false;
        File test = new File(destDir, "test.txt");
        try {
            if (!test.exists()) {
                test.createNewFile();
            }
            writable = test.canWrite();
            writable = test.delete();
        } catch (IOException ex) {
            //ex.printStackTrace();
            writable = false;
        }
        return !writable;
    }

    public static boolean detectRO() { //current directory
        return detectRO(new File(System.getProperty("user.dir")));
    }
    static final String HEXES = "0123456789abcdef";

    public static String byte2Hex(byte[] data) {
        if (data == null) {
            return null;
        }
        final StringBuilder hex = new StringBuilder(2 * data.length);
        for (final byte b : data) {
            hex.append(HEXES.charAt((b & 0xF0) >> 4)).append(HEXES.charAt((b & 0x0F)));
        }
        return hex.toString();
    }

    public static String SHA1(String text) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        return SHA1(text.getBytes("iso-8859-1"));
    }

    public static String SHA1(byte[] data) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        md.update(data);
        return byte2Hex(md.digest());
    }

    public static boolean isReparsePoint(File f) {
        int attrib = Kernel32w.GetFileAttributes(f);
        if (attrib == Kernel32w.INVALID_FILE_ATTRIBUTES) {
            return false;
        }
        return (attrib & Kernel32w.FILE_ATTRIBUTE_REPARSE_POINT) != 0;
    }
    
    public static boolean parentIsReparsePoint(File f) {
        while(f.getParentFile() != null) {
            if(isReparsePoint(f)) {
                return true;
            } else {
                f = f.getParentFile();
            }
        }
        return false;
    }

    public static boolean isSubfolder(File root, File sub) {
        File temp = new File(sub.getAbsolutePath());
        while((temp = temp.getParentFile()) != null) {
            if(root.equals(temp)) {
                return true;
            }
        }
        return false;
    }

    public static String spacedString(String s, int width) {
        if(s != null && s.length() >= width) {
            return s;
        } else {
            StringBuffer out;
            int spaces;
            if(s == null) {
                out = new StringBuffer();
                spaces = width;
            } else {
                out = new StringBuffer(s);
                spaces = width - s.length();
            }
            for(int i=0; i<spaces; i++) {
                out.append(" ");
            }
            return out.toString();
        }
    }

    public static void CenterJFrame(javax.swing.JFrame frame) {
        //Center in the window
        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension frameSize = frame.getSize();
        if (frameSize.height > screenSize.height) {
            frameSize.height = screenSize.height;
        }
        if (frameSize.width > screenSize.width) {
            frameSize.width = screenSize.width;
        }
        frame.setLocation((screenSize.width - frameSize.width) / 2, (screenSize.height - frameSize.height) / 2);
    }
}
