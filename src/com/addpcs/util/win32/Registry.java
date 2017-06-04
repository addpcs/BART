/*
 * Registry.java
 *
 * Created on 17. August 2007, 15:07
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.addpcs.util.win32;

import com.addpcs.jna.*;
import com.addpcs.jna.Advapi32b.LUID;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinNT.HANDLEByReference;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.platform.win32.WinReg.HKEYByReference;
import com.sun.jna.ptr.IntByReference;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Methods for accessing the Windows Registry. Only String and DWORD values supported at the moment.
 */
public class Registry {

    private static Advapi32 advapi32 = Advapi32.INSTANCE;
    private static Advapi32b advapi32b = Advapi32b.INSTANCE;


    /**
     * Testing.
     *
     * @param args arguments
     * @throws java.lang.Exception on error
     */
    public static void main(String[] args) throws Exception {
        try {
            Registry.setStringValue(WinReg.HKEY_CURRENT_USER, "software\\microsoft\\windows\\currentversion\\run", "ctfmon.exe", "Horray");

        } catch (UnsupportedEncodingException uee) {
            uee.printStackTrace();
        }
    }

    /**
     * Gets one of the root keys.
     *
     * @param key key type
     * @return root key
     */
    private static HKEY getRegistryRootKey(HKEY key) {
        HKEYByReference pHandle;
        HKEY handle = new HKEY(0);

        pHandle = new HKEYByReference();

        if (advapi32.RegOpenKeyEx(key, null, 0, 0, pHandle) == WINERROR.ERROR_SUCCESS) {
            handle = pHandle.getValue();
        }
        return (handle);
    }

    /**
     * Opens a key.
     *
     * @param rootKey root key
     * @param subKeyName name of the key
     * @param access access mode
     * @return handle to the key or 0
     */
    private static HKEY openKey(HKEY rootKey, String subKeyName, int access) {
        HKEYByReference pHandle = new HKEYByReference();

        if (advapi32.RegOpenKeyEx(rootKey, subKeyName, 0, access, pHandle) == WINERROR.ERROR_SUCCESS) {
            return (pHandle.getValue());
        } else {
            return null;
        }
    }

    /**
     * Converts a Windows buffer to a Java String.
     *
     * @param buf buffer
     * @throws java.io.UnsupportedEncodingException on error
     * @return String
     */
    private static String convertBufferToString(byte[] buf) throws UnsupportedEncodingException {
        return (new String(buf, 0, buf.length - 2, "UTF-16LE"));
    }

    /**
     * Converts a Windows buffer to an int.
     *
     * @param buf buffer
     * @return int
     */
    private static int convertBufferToInt(byte[] buf) {
        return (((int) (buf[0] & 0xff)) + (((int) (buf[1] & 0xff)) << 8) + (((int) (buf[2] & 0xff)) << 16) + (((int) (buf[3] & 0xff)) << 24));
    }

    /**
     * Read a String value.
     *
     * @param rootKey root key
     * @param subKeyName key name
     * @param name value name
     * @throws java.io.UnsupportedEncodingException on error
     * @return String or null
     */
    public static String getStringValue(HKEY rootKey, String subKeyName, String name) throws UnsupportedEncodingException {
        IntByReference pType, lpcbData;
        byte[] lpData = new byte[1];
        HKEY handle;
        String ret = null;

        pType = new IntByReference();
        lpcbData = new IntByReference();
        handle = openKey(rootKey, subKeyName, WinNT.KEY_READ);

        if (handle != null) {

            if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_MORE_DATA) {
                lpData = new byte[lpcbData.getValue()];

                if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_SUCCESS) {
                    ret = convertBufferToString(lpData);
                }
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Read an int value.
     *
     *
     * @return int or 0
     * @param rootKey root key
     * @param subKeyName key name
     * @param name value name
     */
    public static int getIntValue(HKEY rootKey, String subKeyName, String name) {
        IntByReference pType, lpcbData;
        byte[] lpData = new byte[1];
        HKEY handle;
        int ret = 0;

        pType = new IntByReference();
        lpcbData = new IntByReference();
        handle = openKey(rootKey, subKeyName, WinNT.KEY_READ);

        if (handle != null) {

            if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_MORE_DATA) {
                lpData = new byte[lpcbData.getValue()];

                if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_SUCCESS) {
                    ret = convertBufferToInt(lpData);
                }
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    public static byte[] getBinValue(HKEY rootKey, String subKeyName, String name) {
        IntByReference pType, lpcbData;
        byte[] lpData = new byte[1];
        HKEY handle;
        byte[] ret = new byte[1];

        pType = new IntByReference();
        lpcbData = new IntByReference();
        handle = openKey(rootKey, subKeyName, WinNT.KEY_READ);

        if (handle != null) {

            if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_MORE_DATA) {
                lpData = new byte[lpcbData.getValue()];

                if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) == WINERROR.ERROR_SUCCESS) {
                    ret = lpData;
                }
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Delete a value.
     *
     * @param rootKey root key
     * @param subKeyName key name
     * @param name value name
     * @return true on success
     */
    public static boolean deleteValue(HKEY rootKey, String subKeyName, String name) {
        HKEY handle;
        boolean ret = false;

        handle = openKey(rootKey, subKeyName, WinNT.KEY_READ | WinNT.KEY_WRITE);

        if (handle != null) {
            if (advapi32.RegDeleteValue(handle, name) == WINERROR.ERROR_SUCCESS) {
                ret = true;
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Writes a String value.
     *
     * @param rootKey root key
     * @param subKeyName key name
     * @param name value name
     * @param value value
     * @throws java.io.UnsupportedEncodingException on error
     * @return true on success
     */
    public static boolean setStringValue(HKEY rootKey, String subKeyName, String name, String value) throws UnsupportedEncodingException {
        HKEY handle;
        byte[] data;
        boolean ret = false;

        data = Arrays.copyOf(value.getBytes("UTF-16LE"), value.length() * 2 + 2);
        handle = openKey(rootKey, subKeyName, WinNT.KEY_READ | WinNT.KEY_WRITE);

        if (handle != null) {
            if (advapi32.RegSetValueEx(handle, name, 0, WinNT.REG_SZ, data, data.length) == WINERROR.ERROR_SUCCESS) {
                ret = true;
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Writes a String value.
     *
     * @param rootKey root key
     * @param subKeyName key name
     * @param name value name
     * @param value value
     * @throws java.io.UnsupportedEncodingException on error
     * @return true on success
     */
    public static boolean setMultiSzValue(HKEY rootKey, String subKeyName, String name, String value) throws UnsupportedEncodingException {
        HKEY handle;
        byte[] data;
        boolean ret = false;

        data = Arrays.copyOf(value.getBytes("UTF-16LE"), value.length() * 2 + 4); //plus 4 for 2 null chars
        handle = openKey(rootKey, subKeyName, WinNT.KEY_READ | WinNT.KEY_WRITE);

        if (handle != null) {
            if (advapi32.RegSetValueEx(handle, name, 0, WinNT.REG_MULTI_SZ, data, data.length) == WINERROR.ERROR_SUCCESS) {
                ret = true;
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Writes an int value.
     *
     *
     * @return true on success
     * @param rootKey root key
     * @param subKeyName key name
     * @param name value name
     * @param value value
     */
    public static boolean setIntValue(HKEY rootKey, String subKeyName, String name, int value) {
        HKEY handle;
        byte[] data;
        boolean ret = false;

        data = new byte[4];
        data[0] = (byte) (value & 0xff);
        data[1] = (byte) ((value >> 8) & 0xff);
        data[2] = (byte) ((value >> 16) & 0xff);
        data[3] = (byte) ((value >> 24) & 0xff);
        handle = openKey(rootKey, subKeyName, WinNT.KEY_READ | WinNT.KEY_WRITE);

        if (handle != null) {

            if (advapi32.RegSetValueEx(handle, name, 0, WinNT.REG_DWORD, data, data.length) == WINERROR.ERROR_SUCCESS) {
                ret = true;
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Check for existence of a value.
     *
     * @param rootKey root key
     * @param subKeyName key name
     * @param name value name
     * @return true if exists
     */
    public static boolean valueExists(HKEY rootKey, String subKeyName, String name) {
        IntByReference pType, lpcbData;
        byte[] lpData = new byte[1];
        HKEY handle;
        boolean ret = false;

        pType = new IntByReference();
        lpcbData = new IntByReference();
        handle = openKey(rootKey, subKeyName, WinNT.KEY_READ);

        if (handle != null) {

            if (advapi32.RegQueryValueEx(handle, name, 0, pType, lpData, lpcbData) != WINERROR.ERROR_FILE_NOT_FOUND) {
                ret = true;

            } else {
                ret = false;
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Create a new key.
     *
     * @param rootKey root key
     * @param parent name of parent key
     * @param name key name
     * @return true on success
     */
    public static boolean createKey(HKEY rootKey, String parent, String name) {
        IntByReference dwDisposition = new IntByReference();
        HKEYByReference hkResult = new HKEYByReference();
        HKEY handle;
        boolean ret = false;

        handle = openKey(rootKey, parent, WinNT.KEY_READ);
        if (handle != null) {
            if (advapi32.RegCreateKeyEx(handle, name, 0, null, WinNT.REG_OPTION_NON_VOLATILE, WinNT.KEY_READ, null,
                    hkResult, dwDisposition) == WINERROR.ERROR_SUCCESS) {
                ret = true;
                advapi32.RegCloseKey(hkResult.getValue());

            } else {
                ret = false;
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Delete a key.
     *
     * @param rootKey root key
     * @param parent name of parent key
     * @param name key name
     * @return true on success
     */
    public static boolean deleteKey(HKEY rootKey, String parent, String name) {
        HKEY handle;
        boolean ret = false;

        handle = openKey(rootKey, parent, WinNT.KEY_READ);
        if (handle != null) {
            if (advapi32.RegDeleteKey(handle, name) == WINERROR.ERROR_SUCCESS) {
                ret = true;

            } else {
                ret = false;
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Delete a key and its subtree.  Requires Vista or later
     *
     * @param rootKey root key
     * @param parent name of parent key
     * @param name key name
     * @return true on success
     */
    public static boolean deleteTree(HKEY rootKey, String parent, String name) {
        HKEY handle;
        boolean ret = false;

        handle = openKey(rootKey, parent, WinNT.KEY_READ);

        if (handle != null) {

            if (advapi32b.RegDeleteTree(handle, name) == WINERROR.ERROR_SUCCESS) {
                ret = true;

            } else {
                ret = false;
            }
            advapi32.RegCloseKey(handle);
        }
        return (ret);
    }

    /**
     * Get all sub keys of a key.
     *
     * @param rootKey root key
     * @param parent key name
     * @return array with all sub key names
     */
    public static String[] getSubKeys(HKEY rootKey, String parent) {
        HKEY handle;
        int dwIndex;
        char[] lpName;
        IntByReference lpcName;
        WinBase.FILETIME lpftLastWriteTime = new WinBase.FILETIME();
        TreeSet<String> subKeys = new TreeSet<String>();

        handle = openKey(rootKey, parent, WinNT.KEY_READ);
        lpName = new char[256];
        lpcName = new IntByReference(256);

        if (handle != null) {
            dwIndex = 0;

            while (advapi32.RegEnumKeyEx(handle, dwIndex, lpName, lpcName, null,
                    null, null, lpftLastWriteTime) == WINERROR.ERROR_SUCCESS) {
                subKeys.add(new String(lpName, 0, lpcName.getValue()));
                lpcName.setValue(256);
                dwIndex++;
            }
            advapi32.RegCloseKey(handle);
        }

        return (subKeys.toArray(new String[]{}));
    }

    /**
     * Get all values under a key.
     *
     * @param rootKey root key
     * @param key key name
     * @throws java.io.UnsupportedEncodingException on error
     * @return TreeMap with name and value pairs
     */
    public static TreeMap<String, Object> getValuesAsTreeMap(HKEY rootKey, String key) throws UnsupportedEncodingException {
        HKEY handle;
        int dwIndex, result = 0;
        char[] lpValueName;
        byte[] lpData;
        IntByReference lpcchValueName, lpType, lpcbData;
        String name;
        TreeMap<String, Object> values = new TreeMap<String, Object>(String.CASE_INSENSITIVE_ORDER);

        handle = openKey(rootKey, key, WinNT.KEY_READ);
        lpValueName = new char[16384];
        lpcchValueName = new IntByReference(16384);
        lpType = new IntByReference();
        lpData = new byte[1];
        lpcbData = new IntByReference();

        if (handle != null) {
            dwIndex = 0;

            do {
                lpcbData.setValue(0);
                result = advapi32.RegEnumValue(handle, dwIndex, lpValueName, lpcchValueName, null,
                        lpType, lpData, lpcbData);

                if (result == WINERROR.ERROR_MORE_DATA) {
                    lpData = new byte[lpcbData.getValue()];
                    lpcchValueName = new IntByReference(16384);
                    result = advapi32.RegEnumValue(handle, dwIndex, lpValueName, lpcchValueName, null,
                            lpType, lpData, lpcbData);

                    if (result == WINERROR.ERROR_SUCCESS) {
                        name = new String(lpValueName, 0, lpcchValueName.getValue());

                        switch (lpType.getValue()) {
                            case WinNT.REG_SZ:
                                values.put(name, convertBufferToString(lpData));
                                break;
                            case WinNT.REG_DWORD:
                                values.put(name, convertBufferToInt(lpData));
                                break;
                            default:
                                break;
                        }
                    }
                }
                dwIndex++;
            } while (result == WINERROR.ERROR_SUCCESS);

            advapi32.RegCloseKey(handle);
        }
        return (values);
    }

    public static boolean loadKey(HKEY rootKey, String subKeyName, java.io.File hiveFile) {
        if(hiveFile.exists()) {
            return loadKey(rootKey, subKeyName, hiveFile.getAbsolutePath());
        }
        return false;
    }

    public static boolean loadKey(HKEY rootKey, String subKeyName, String fileName) {

        /* Set the permissions for the process to load the key */
        //Get process token
        HANDLEByReference token = new HANDLEByReference();
        try {
            advapi32.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(), Advapi32b.TOKEN_ADJUST_PRIVILEGES | Advapi32b.TOKEN_QUERY, token);
        } catch(IllegalArgumentException e) { return false; } //Failed due to lack of admin rights

        //get LUID for desired Privs
        LUID RestoreLuid = new LUID();
        advapi32b.LookupPrivilegeValue(null, "SeRestorePrivilege", RestoreLuid);
        LUID BackupLuid = new LUID();
        advapi32b.LookupPrivilegeValue(null, "SeBackupPrivilege", BackupLuid);

        //Fill in the crazy struct
        Advapi32b.TOKEN_PRIVILEGES TP = new Advapi32b.TOKEN_PRIVILEGES();
        TP.PrivilegeCount = 2;
        TP.Privileges = new Advapi32b.LUID_AND_ATTRIBUTES[2];
        TP.Privileges[0] = new Advapi32b.LUID_AND_ATTRIBUTES();
        TP.Privileges[0].Luid = RestoreLuid;
        TP.Privileges[0].Attributes = Advapi32b.SE_PRIVILEGE_ENABLED;
        TP.Privileges[1] = new Advapi32b.LUID_AND_ATTRIBUTES();
        TP.Privileges[1].Luid = BackupLuid;
        TP.Privileges[1].Attributes = Advapi32b.SE_PRIVILEGE_ENABLED;

        //Adjust the privs
        advapi32b.AdjustTokenPrivileges(token.getValue(), false, TP, 28, null, null);

        //Load the registry hive and return success
        return advapi32b.RegLoadKey(rootKey, subKeyName, fileName) == WINERROR.ERROR_SUCCESS;
    }

    public static boolean unloadKey(HKEY rootKey, String subKeyName) {
        /* Set the permissions for the process to unload the key */
        //Get process token
        HANDLEByReference token = new HANDLEByReference();
        try {
            advapi32.OpenProcessToken(Kernel32.INSTANCE.GetCurrentProcess(), Advapi32b.TOKEN_ADJUST_PRIVILEGES | Advapi32b.TOKEN_QUERY, token);
        } catch(IllegalArgumentException e) { return false; } //Failed due to lack of admin rights

        //get LUID for desired Privs
        LUID RestoreLuid = new LUID();
        advapi32b.LookupPrivilegeValue(null, "SeRestorePrivilege", RestoreLuid);
        LUID BackupLuid = new LUID();
        advapi32b.LookupPrivilegeValue(null, "SeBackupPrivilege", BackupLuid);

        //Fill in the crazy struct
        Advapi32b.TOKEN_PRIVILEGES TP = new Advapi32b.TOKEN_PRIVILEGES();
        TP.PrivilegeCount = 2;
        TP.Privileges = new Advapi32b.LUID_AND_ATTRIBUTES[2];
        TP.Privileges[0] = new Advapi32b.LUID_AND_ATTRIBUTES();
        TP.Privileges[0].Luid = RestoreLuid;
        TP.Privileges[0].Attributes = Advapi32b.SE_PRIVILEGE_ENABLED;
        TP.Privileges[1] = new Advapi32b.LUID_AND_ATTRIBUTES();
        TP.Privileges[1].Luid = BackupLuid;
        TP.Privileges[1].Attributes = Advapi32b.SE_PRIVILEGE_ENABLED;

        //Adjust the privs
        advapi32b.AdjustTokenPrivileges(token.getValue(), false, TP, 28, null, null);

        //unLoad the registry hive and return success
        return advapi32b.RegUnLoadKey(rootKey, subKeyName) == WINERROR.ERROR_SUCCESS;
    }

    
}
