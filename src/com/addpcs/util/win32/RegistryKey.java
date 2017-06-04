/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addpcs.util.win32;

import com.addpcs.jna.Advapi32b;
import com.addpcs.jna.WINERROR;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.WinBase;
import com.sun.jna.platform.win32.WinNT;
import com.sun.jna.platform.win32.WinReg;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.platform.win32.WinReg.HKEYByReference;
import com.sun.jna.ptr.IntByReference;
import java.util.ArrayList;

/**
 *
 * @author justin
 */
public class RegistryKey {

    private final HKEY root;
    private final String parentPath;
    private final String name;
    private HKEY handle;
    private boolean open;
    private Advapi32 advapi32 = Advapi32.INSTANCE;
    private Advapi32b advapi32b = Advapi32b.INSTANCE;

    private ArrayList<RegistryKey> subKeys;
    private ArrayList<RegistryVal>  values;
    private boolean subTreeSaved;

    private boolean is64;

    public RegistryKey(HKEY rk, String fullpath) {
        this(rk,fullpath.substring(0, fullpath.lastIndexOf("\\")),fullpath.substring(fullpath.lastIndexOf("\\") + 1),false);
    }

    public RegistryKey(HKEY rk, String parentKey, String Name) {
        this(rk,parentKey,Name,false);
    }

    public RegistryKey(HKEY rk, String parentKey, String Name, boolean x64) {
        root = rk;
        parentPath = parentKey;
        name = Name;
        open = false;
        handle = new HKEY(0);
        is64 = x64;

        subKeys = new ArrayList<RegistryKey>();
        values = new ArrayList<RegistryVal>();
        subTreeSaved = false;
    }

    public RegistryKey(RegistryKey toClone) {
        this(toClone.root, toClone.parentPath, toClone.name);
    }

    public String getParentPath() {
        return parentPath;
    }

    public RegistryKey getParentKey() {
        String p = getFullPath();
        p = p.substring(0,p.lastIndexOf("\\"));
        return new RegistryKey(root,p);
    }

    public String getFullPath() {
        return (parentPath + "\\" + name);
    }

    public String getName() {
        return name;
    }

    public HKEY getRoot() {
        return root;
    }

    public boolean create(int access) {
        IntByReference dwDisposition;
        HKEYByReference hkResult, pHandle;
        HKEY hParent;

        hkResult = new HKEYByReference();
        dwDisposition = new IntByReference();
        pHandle = new HKEYByReference();

        if (advapi32.RegOpenKeyEx(root, parentPath, 0, ((WinNT.KEY_READ | WinNT.KEY_WRITE) | access), pHandle) != WINERROR.ERROR_SUCCESS) {
            return false;
        }
        if ((hParent = pHandle.getValue()) == null) {
            return false;
        }

        if (advapi32.RegCreateKeyEx(hParent, name, 0, null, WinNT.REG_OPTION_NON_VOLATILE, access, null, hkResult, dwDisposition) == WINERROR.ERROR_SUCCESS) {
            advapi32.RegCloseKey(hParent);
            handle = hkResult.getValue();
            return open = true;
        } else {
            advapi32.RegCloseKey(hParent);
            return open = false;
        }
    }

    public boolean create32() {
        is64 = false;
        return create(WinNT.KEY_READ | WinNT.KEY_WRITE);
    }

    public boolean create64() {
        is64 = true;
        return create(WinNT.KEY_READ | WinNT.KEY_WRITE | WinNT.KEY_WOW64_64KEY);
    }

    public boolean create() {
        if(is64) {
            return create64();
        } else {
            return create32();
        }
    }

    public boolean delete(int samDesired) {
        HKEYByReference pHandle = new HKEYByReference();
        HKEY hParent;

        if (advapi32.RegOpenKeyEx(root, parentPath, 0, ((WinNT.KEY_READ | WinNT.KEY_WRITE) | samDesired), pHandle) != WINERROR.ERROR_SUCCESS) {
            return false;
        }
        if ((hParent = pHandle.getValue()) == null) {
            return false;
        }
        try {
            if (advapi32b.RegDeleteKeyEx(hParent, name, samDesired, 0) == WINERROR.ERROR_SUCCESS) {
                advapi32.RegCloseKey(hParent);
                handle = null;
                open = false;
                return true;
            } else {
                advapi32.RegCloseKey(hParent);
                return false;
            }
        } catch(java.lang.UnsatisfiedLinkError err) {
            //No RegDeleteKeyEx
            if (advapi32.RegDeleteKey(hParent, name) == WINERROR.ERROR_SUCCESS) {
                advapi32.RegCloseKey(hParent);
                handle = null;
                open = false;
                return true;
            } else {
                advapi32.RegCloseKey(hParent);
                return false;
            }
        }
    }

    public boolean delete() {
        if(is64) {
            return delete(WinNT.KEY_WOW64_64KEY);
        } else {
            return delete(0);
        }
    }

    // Delete this key and all subkeys recursively
    // Must already be open
    public boolean deleteTree() {
        if (open) {
            for(RegistryKey s : getSubKeys()) {
                if(is64) {
                    s.open64();
                } else {
                    s.open();
                }
                s.deleteTree(); //This also closes s
            }
            return delete(); //This also closes this
        }
        return false;
    }

    public boolean open(int access) {
        HKEYByReference pHandle = new HKEYByReference();
        if (!open) {
            if (advapi32.RegOpenKeyEx(root, (parentPath + "\\" + name), 0, access, pHandle) == WINERROR.ERROR_SUCCESS) {
                handle = pHandle.getValue();
                if (handle != null) {
                    return open = true;
                }
            }
            return open = false;
        } else {
            return true;
        }
    }

    public boolean open32() {
        is64 = false;
        return open(WinNT.KEY_READ | WinNT.KEY_WRITE);
    }

    public boolean open64() {
        is64 = true;
        return open(WinNT.KEY_READ | WinNT.KEY_WRITE | WinNT.KEY_WOW64_64KEY);
    }

    public boolean open() {
        if(is64) {
            return open64();
        } else {
            return open32();
        }
    }

    public boolean close() {
        if (open) {
            if (advapi32.RegCloseKey(handle) == WINERROR.ERROR_SUCCESS) {
                open = false;
                return true;
            }
            return false;
        }
        return true;
    }

    public RegistryVal getValue(String valName) {
        IntByReference pType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        byte[] lpData = new byte[1];


        if (open) {
            if (advapi32.RegQueryValueEx(handle, valName, 0, pType, lpData, lpcbData) == WINERROR.ERROR_MORE_DATA) {
                lpData = new byte[lpcbData.getValue()];

                if (advapi32.RegQueryValueEx(handle, valName, 0, pType, lpData, lpcbData) == WINERROR.ERROR_SUCCESS) {
                    return new RegistryVal(valName, pType.getValue(), lpData);
                }
            }
        }
        return null;
    }

    public boolean setValue(RegistryVal rv) {
        if(open) {
            int ret;
            if ((ret = advapi32.RegSetValueEx(handle, rv.getName(), 0, rv.getType(), rv.getBinData(), rv.getBinDataLen())) == WINERROR.ERROR_SUCCESS) {
                return true;
            } else {
                System.out.println("RegSetValueEx Error: " + ret);
            }
        }
        return false;
    }

    public boolean hasValue(String valName) {
        if(open) {
            IntByReference pType = new IntByReference();
            IntByReference lpcbData = new IntByReference();
            byte[] lpData = new byte[1];
            if(advapi32.RegQueryValueEx(handle, valName, 0, pType, lpData, lpcbData) != WINERROR.ERROR_FILE_NOT_FOUND) {
                return true;
            }
        }
        return false;
    }

    public boolean hasValue(RegistryVal rv) {
        return hasValue(rv.getName());
    }

    public boolean deleteValue(RegistryVal rv) {
        if (open) {
            if (advapi32.RegDeleteValue(handle, rv.getName()) == WINERROR.ERROR_SUCCESS) {
                return true;
            }
        }
        return false;
    }

    public RegistryKey getSubKey(String skName) {
        return new RegistryKey(root, (parentPath + "\\" + name), skName);
    }

    public ArrayList<RegistryKey> getSubKeys() {
        if(open) {
            int dwIndex = 0;
            char[] lpName = new char[256];
            IntByReference lpcName = new IntByReference(256);
            WinBase.FILETIME lpftLastWriteTime = new WinBase.FILETIME();
            RegistryKey temp;
            //subKeys = new ArrayList<RegistryKey>();

            while (advapi32.RegEnumKeyEx(handle, dwIndex, lpName, lpcName, null, null, null, lpftLastWriteTime) == WINERROR.ERROR_SUCCESS) {
                temp = new RegistryKey(root,(parentPath + "\\" + name), new String(lpName, 0, lpcName.getValue()));
                if(!subKeys.contains(temp)) {
                    subKeys.add(temp);
                }
                lpcName.setValue(256);
                dwIndex++;
            }
            return subKeys;
        }
        return null;
    }

    public ArrayList<RegistryVal> getValues() {
        int dwIndex, result = 0;
        char[] lpValueName = new char[16384];
        byte[] lpData = new byte[1];
        IntByReference lpcchValueName = new IntByReference(16384);
        IntByReference lpType = new IntByReference();
        IntByReference lpcbData = new IntByReference();
        values = new ArrayList<RegistryVal>();


        if (open) {
            dwIndex = 0;
            do {
                lpcbData.setValue(0);
                result = advapi32.RegEnumValue(handle, dwIndex, lpValueName, lpcchValueName, null, lpType, lpData, lpcbData);

                if (result == WINERROR.ERROR_MORE_DATA) {
                    lpData = new byte[lpcbData.getValue()];
                    lpcchValueName = new IntByReference(16384);
                    result = advapi32.RegEnumValue(handle, dwIndex, lpValueName, lpcchValueName, null, lpType, lpData, lpcbData);

                    if (result == WINERROR.ERROR_SUCCESS) {
                        values.add(new RegistryVal(new String(lpValueName, 0, lpcchValueName.getValue()), lpType.getValue(), lpData));
                    }
                }
                dwIndex++;
            } while (result == WINERROR.ERROR_SUCCESS);
            return values;
        }
        return null;
    }

    public boolean readSubTree() {
        boolean ret = true;
        if(open) {
            getValues();
            for(RegistryKey key : getSubKeys()) {
                if(is64) {
                    key.open64();
                } else {
                    key.open();
                }
                if(!key.readSubTree()) {
                    ret = false;
                }
                key.close();
            }
        } else {
            ret = false;
        }
        return subTreeSaved = ret;
    }

    public boolean subTreeIsSaved() {
        return subTreeSaved;
    }

    public boolean writeSubTree() {
        boolean ret = true;
        if(open) {
            for(RegistryVal val : values) {
                if(!setValue(val)) {
                    ret = false;
                }
            }
            for(RegistryKey key : subKeys) {
                if(is64) {
                    key.create64();
                } else {
                    key.create();
                }
                if(!key.writeSubTree()) {
                    ret = false;
                }
                key.close();
            }
        } else {
            ret = false;
        }
        return ret;
    }

    /*
    public static void main(String[] args) {
        RegistryKey tst = new RegistryKey(WinReg.HKEY_CURRENT_USER, "Software", "KasperskyLab");
        System.out.println("Open: " + tst.open());
        System.out.println("ReadTree: " + tst.readSubTree());
        System.out.println("DeleteTree: " + tst.deleteTree());
        
        System.out.println();
        System.out.println();
        tst.printTree();
        System.out.println();
        System.out.println();
        System.out.println("Create: " + tst.create());
        System.out.println("WriteTree: " + tst.writeSubTree());
        System.out.println("Close: " + tst.close());

    }*/

    public void printTree() {
        System.out.println(toString());
        printTree(0);
    }

    public void printTree(int ind) {
        indent(ind);
        System.out.println(name);
        for(RegistryVal val : values) {
            indent(ind);
            System.out.println(" - " + val.name);
        }
        for(RegistryKey key : subKeys) {
            key.printTree(ind+5);
        }
    }

    private void indent(int n) {
        for(int i=0; i<n; i++) {
            System.out.print(" ");
        }
    }

    @Override
    public String toString() {
        if(root == WinReg.HKEY_LOCAL_MACHINE) {
            return "hklm\\" + getFullPath();
        } else if(root == WinReg.HKEY_CURRENT_USER) {
            return "hkcu\\" + getFullPath();
        } else if(root == WinReg.HKEY_USERS) {
            return "hku\\" + getFullPath();
        } else if(root == WinReg.HKEY_CLASSES_ROOT) {
            return "hkcr\\" + getFullPath();
        } else {
            return getFullPath();
        }
    }

    @Override
    public boolean equals(Object o) {
        if(o.getClass().equals(this.getClass())) {
            return (hashCode() == ((RegistryKey)o).hashCode());
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.root != null ? this.root.hashCode() : 0);
        hash = 79 * hash + (this.parentPath != null ? this.parentPath.hashCode() : 0);
        hash = 79 * hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }
}
