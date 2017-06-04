/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.addpcs.util.win32;

import com.addpcs.jna.Kernel32b;
import java.io.File;

/**
 *
 * @author justin
 */
public class Kernel32w {
    public static final int FILE_ATTRIBUTE_ARCHIVE = 0x20;
    public static final int FILE_ATTRIBUTE_COMPRESSED = 0x800;
    public static final int FILE_ATTRIBUTE_DEVICE = 0x40;
    public static final int FILE_ATTRIBUTE_DIRECTORY = 0x10;
    public static final int FILE_ATTRIBUTE_ENCRYPTED = 0x4000;
    public static final int FILE_ATTRIBUTE_HIDDEN = 0x2;
    public static final int FILE_ATTRIBUTE_NORMAL = 0x80;
    public static final int FILE_ATTRIBUTE_NOT_CONTENT_INDEXED = 0x2000;
    public static final int FILE_ATTRIBUTE_OFFLINE = 0x1000;
    public static final int FILE_ATTRIBUTE_READONLY = 0x1;
    public static final int FILE_ATTRIBUTE_REPARSE_POINT = 0x400;
    public static final int FILE_ATTRIBUTE_SPARSE_FILE = 0x200;
    public static final int FILE_ATTRIBUTE_SYSTEM = 0x4;
    public static final int FILE_ATTRIBUTE_TEMPORARY = 0x100;
    public static final int FILE_ATTRIBUTE_VIRTUAL = 0x10000;
    public static final int INVALID_FILE_ATTRIBUTES = 0xFFFFFFFF;

    private static Kernel32b kernel32b = Kernel32b.INSTANCE;

    public static String ExpandEnvironmentString(String name) {
        byte[] buffer = new byte[16384];

        if(kernel32b.ExpandEnvironmentStrings(name, buffer, buffer.length) != 0) {
            try {
                return (new String(buffer,"UTF-16LE")).trim();
            } catch(java.io.UnsupportedEncodingException ex) { }
        }
        return null;
    }

    public static String GetEnvironmentVariable(String name) {
        byte[] buffer = new byte[16384];

        if(kernel32b.GetEnvironmentVariable(name, buffer, buffer.length) != 0) {
            try {
                return (new String(buffer,"UTF-16LE")).trim();
            } catch(java.io.UnsupportedEncodingException ex) { }
        }
        return null;
    }

    public static String GetLongPathName(String shortName) {
        byte[] buffer = new byte[16384];
        
        if(kernel32b.GetLongPathName(shortName, buffer, buffer.length) != 0) {
            try {
                return (new String(buffer,"UTF-16LE")).trim();
            } catch(java.io.UnsupportedEncodingException ex) { }
        }
        return null;
    }

    public static int GetFileAttributes(File f) {
        return kernel32b.GetFileAttributes(f.getAbsolutePath());
    }
}
