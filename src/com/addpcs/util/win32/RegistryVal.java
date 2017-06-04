/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.addpcs.util.win32;

import com.sun.jna.platform.win32.WinNT;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;


/**
 *
 * @author justin
 */
public class RegistryVal {
    protected final String name;
    private byte[] data;
    private int type;

    public RegistryVal(String Name, int Type, byte[] value) {
        name = Name;
        type = Type;
        data = Arrays.copyOf(value, value.length);
    }

    public RegistryVal(String Name, String value, boolean multiSZ) throws UnsupportedEncodingException {
        name = Name;
        if(multiSZ) {
            type = WinNT.REG_MULTI_SZ;
            data = Arrays.copyOf(value.getBytes("UTF-16LE"), value.length() * 2 + 4);
        } else {
            type = WinNT.REG_SZ;
            data = Arrays.copyOf(value.getBytes("UTF-16LE"), value.length() * 2 + 2);
        }
    }

    public RegistryVal(String Name, String value) throws UnsupportedEncodingException {
        this(Name,value,false);
    }

    public RegistryVal(String Name, int value) {
        name = Name;
        type = WinNT.REG_DWORD;
        data = new byte[4];
        data[0] = (byte) (value & 0xff);
        data[1] = (byte) ((value >> 8) & 0xff);
        data[2] = (byte) ((value >> 16) & 0xff);
        data[3] = (byte) ((value >> 24) & 0xff);
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public boolean setType(int t) {
        if(t == WinNT.REG_BINARY || t == WinNT.REG_DWORD || t == WinNT.REG_DWORD_LITTLE_ENDIAN || t == WinNT.REG_DWORD_BIG_ENDIAN || t == WinNT.REG_EXPAND_SZ || t == WinNT.REG_LINK || t == WinNT.REG_MULTI_SZ || t == WinNT.REG_NONE || t == WinNT.REG_SZ) {
            type = t;
            return true;
        }
        return false;
    }

    public String getTypeString() {
        if(type == WinNT.REG_BINARY) {
            return "REG_BINARY";
        } else if(type == WinNT.REG_DWORD) {
            return "REG_DWORD";
        } else if(type == WinNT.REG_DWORD_LITTLE_ENDIAN) {
            return "REG_DWORD_LITTLE_ENDIAN";
        } else if(type == WinNT.REG_DWORD_BIG_ENDIAN) {
            return "REG_DWORD_BIG_ENDIAN";
        } else if(type == WinNT.REG_EXPAND_SZ) {
            return "REG_EXPAND_SZ";
        } else if(type == WinNT.REG_LINK) {
            return "REG_LINK";
        } else if(type == WinNT.REG_MULTI_SZ) {
            return "REG_MULTI_SZ";
        } else if(type == WinNT.REG_NONE) {
            return "REG_NONE";
        } else if(type == WinNT.REG_SZ) {
            return "REG_SZ";
        } else {
            return "";
        }
    }

    public byte[] getBinData() {
        return Arrays.copyOf(data, data.length);
    }

    public int getBinDataLen() {
        return data.length;
    }

    public String getStringData() {
        if(data.length >= 2) {
            try {
                return new String(data, 0, data.length - 2, "UTF-16LE");
            } catch (UnsupportedEncodingException ex) {
                return null;
            }
        }
        return null;
    }

    public int getIntData() {
        if(data.length >= 4) {
            return (((int) (data[0] & 0xff)) + (((int) (data[1] & 0xff)) << 8) + (((int) (data[2] & 0xff)) << 16) + (((int) (data[3] & 0xff)) << 24));
        }
        return -1;
    }

    public boolean setData(String value) {
        try {
            data = Arrays.copyOf(value.getBytes("UTF-16LE"), value.length() * 2 + 2);
            return true;
        } catch (UnsupportedEncodingException ex) {
            return false;
        }
    }

    public boolean setData(int value) {
        data = new byte[4];
        data[0] = (byte) (value & 0xff);
        data[1] = (byte) ((value >> 8) & 0xff);
        data[2] = (byte) ((value >> 16) & 0xff);
        data[3] = (byte) ((value >> 24) & 0xff);
        return true;
    }

    public boolean setData(byte[] value) {
        data = Arrays.copyOf(value, value.length);
        return true;
    }
}
