/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.addpcs.util.win32;

import com.addpcs.jna.Advapi32b;
import com.addpcs.jna.Kernel32b;
import com.addpcs.jna.WINERROR;
import com.sun.jna.platform.win32.Advapi32;
import com.sun.jna.platform.win32.Advapi32Util;
import com.sun.jna.platform.win32.Advapi32Util.Account;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT.*;
import com.sun.jna.ptr.IntByReference;
import java.io.File;

/**
 *
 * @author justin
 */
public class Chown {

    private static Advapi32 advapi32 = Advapi32.INSTANCE;
    private static Advapi32b advapi32b = Advapi32b.INSTANCE;
    private static Kernel32b kernel32b = Kernel32b.INSTANCE;
    private static Kernel32 kernel32 = Kernel32.INSTANCE;

    // Sets the privileges of the given process, requires Admin rights
    // token handle (NULL: current process), Privilege to enable/disable,  TRUE to enable.  FALSE to disable
    public static boolean SetPrivilege(HANDLE hToken, String Privilege, boolean bEnablePrivilege) {
        Advapi32b.TOKEN_PRIVILEGES tp = new Advapi32b.TOKEN_PRIVILEGES();
        Advapi32b.LUID luid = new Advapi32b.LUID();
        Advapi32b.TOKEN_PRIVILEGES tpPrevious = new Advapi32b.TOKEN_PRIVILEGES();
        IntByReference cbPrevious = new IntByReference(Advapi32b.TOKEN_PRIVILEGES_SIZE);
        HANDLEByReference token;

        //
        // Retrieve a handle of the access token
        //
        if (hToken == null) {
            token = new HANDLEByReference();
            try {
                if (!advapi32.OpenProcessToken(kernel32.GetCurrentProcess(), Advapi32b.TOKEN_ADJUST_PRIVILEGES | Advapi32b.TOKEN_QUERY, token)) {
                    return false;
                }
            } catch(IllegalArgumentException e) { return false; } //Failed due to lack of admin rights
        } else {
            token = new HANDLEByReference(hToken);
        }

        if (!advapi32b.LookupPrivilegeValue(null, Privilege, luid)) {
            return false;
        }

        //
        // first pass.  get current privilege setting
        //
        tp.PrivilegeCount = 1;
        tp.Privileges = new Advapi32b.LUID_AND_ATTRIBUTES[1];
        tp.Privileges[0] = new Advapi32b.LUID_AND_ATTRIBUTES();
        tp.Privileges[0].Luid = luid;
        tp.Privileges[0].Attributes = 0;

        tpPrevious.Privileges = new Advapi32b.LUID_AND_ATTRIBUTES[1];
        //tp.Privileges[0] = new Advapi32.LUID_AND_ATTRIBUTES();

        advapi32b.AdjustTokenPrivileges(token.getValue(), false, tp, Advapi32b.TOKEN_PRIVILEGES_SIZE, tpPrevious, cbPrevious);

        if (kernel32.GetLastError() != WINERROR.ERROR_SUCCESS) {
            return false;
        }

        //
        // second pass.  set privilege based on previous setting
        //
        tpPrevious.PrivilegeCount = 1;
        tpPrevious.Privileges[0].Luid = luid;

        if (bEnablePrivilege) {
            tpPrevious.Privileges[0].Attributes |= (Advapi32b.SE_PRIVILEGE_ENABLED);
        } else {
            tpPrevious.Privileges[0].Attributes ^= (Advapi32b.SE_PRIVILEGE_ENABLED & tpPrevious.Privileges[0].Attributes);
        }

        advapi32b.AdjustTokenPrivileges(token.getValue(), false, tpPrevious, cbPrevious.getValue(), null, null);

        if (kernel32.GetLastError() != WINERROR.ERROR_SUCCESS) {
            return false;
        }

        return true;
    }

    public static boolean chown(File f, String owner, boolean recurse) {
        //Enabling privileges...
        if (!SetPrivilege(null,Advapi32b.SE_TAKE_OWNERSHIP_NAME,true)) { // needed if you don't have full control
            //sysTools.writeDetail("Could not enable Take Ownership privilege, trying without.");
        }
        if (!SetPrivilege(null,Advapi32b.SE_RESTORE_NAME,true)) {
            //sysTools.writeDetail("Could not enable Restore Files privilege, trying without.");
        }
        if (!SetPrivilege(null,Advapi32b.SE_BACKUP_NAME,true)) {
            //sysTools.writeDetail("Could not enable Backup Files privilege, trying without.");
        }
        if (!SetPrivilege(null,Advapi32b.SE_CHANGE_NOTIFY_NAME,true)) {
            //sysTools.writeDetail("Could not enable Bypass Traverse Checking privilege, trying without.");
        }
        if (!kernel32b.SetCurrentDirectory(f.getAbsoluteFile().getParent())) {
            //sysTools.writeDetail("Warning: SetCurrentDirectory failed.");
        }

        Account user = Advapi32Util.getAccountByName(owner);
        byte[] sid = user.sid;
        if (sid == null) {
            return false;
        }

        System.out.print("SID: ");
        for(int i=0; i<sid.length; i++) {
            System.out.print((int)(char)sid[i] + " ");
        }
        System.out.println("");

        byte[] psdFileSDrel = new byte[100];
        
        for(int i=0; i<20; i++) {
            System.out.print((int)(char)psdFileSDrel[i] + " ");
        }
        System.out.println("");
        if (!advapi32b.InitializeSecurityDescriptor(psdFileSDrel, Advapi32b.SECURITY_DESCRIPTOR_REVISION)) {
            //sysTools.writeDetail("Error: InitializeSecurityDescriptor, code " + kernel32.GetLastError());
            return false;
        }
        System.out.println(user.sidString);
        for(int i=0; i<20; i++) {
            System.out.print((int)(char)psdFileSDrel[i] + " ");
        }
        System.out.println("");

        if (!advapi32b.SetSecurityDescriptorOwner(psdFileSDrel, sid, false)) {
            //sysTools.writeDetail("Error: SetSecurityDescriptorOwner for " + f.getName() + ", code " + kernel32.GetLastError());
            return false;
        }

        //psdFileSDrel[4] = Byte.parseByte("78", 16);
        //psdFileSDrel[5] = Byte.parseByte("61", 16);
        //psdFileSDrel[6] = Byte.parseByte("15", 16);
        //psdFileSDrel[7] = Byte.parseByte("0", 16);

        for(int i=0; i<20; i++) {
            System.out.print(Integer.toHexString(psdFileSDrel[i]) + " ");
        }
        System.out.println("");
        if (!advapi32b.IsValidSecurityDescriptor(psdFileSDrel)) {
            //sysTools.writeDetail("Invalid SD. " + kernel32.GetLastError());
            return false;
        }
        if (!advapi32b.SetFileSecurity(f.getAbsolutePath(), Advapi32b.OWNER_SECURITY_INFORMATION, psdFileSDrel)) {
            //sysTools.writeDetail("Error: SetFileSecurity for " + f.getName() + ", code " + kernel32.GetLastError());
            return false;
        }
        
        if (recurse && f.isDirectory()) {
            //Apply chown to children
            for(File child : f.listFiles()) {
                chown(child,owner,recurse);
            }
        }

        return true;
    }

    public static void main(String[] args) {
        if(args.length < 1) {
            System.out.println("Specify path.");
        } else {
            chown(new File(args[0]),"Administrator",true);
        }
    }
}
