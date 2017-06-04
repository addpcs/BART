/*
 * Advapi32.java
 *
 * Created on 6. August 2007, 11:24
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.addpcs.jna;

import com.sun.jna.*;
import com.sun.jna.platform.win32.WinNT.HANDLE;
import com.sun.jna.platform.win32.WinReg.HKEY;
import com.sun.jna.ptr.*;
import com.sun.jna.win32.*;

/**
 *
 * @author TB
 */
public interface Advapi32b extends StdCallLibrary {

    public static final int TOKEN_ADJUST_PRIVILEGES = 0x00000020;
    public static final int TOKEN_QUERY = 0x00000008;
    public static final int SE_PRIVILEGE_ENABLED = 0x00000002;
    public static final int SECURITY_DESCRIPTOR_REVISION = 0x1;
    public static final int OWNER_SECURITY_INFORMATION = 0x1;
    public static final String SE_CREATE_TOKEN_NAME = "SeCreateTokenPrivilege";
    public static final String SE_ASSIGNPRIMARYTOKEN_NAME = "SeAssignPrimaryTokenPrivilege";
    public static final String SE_LOCK_MEMORY_NAME = "SeLockMemoryPrivilege";
    public static final String SE_INCREASE_QUOTA_NAME = "SeIncreaseQuotaPrivilege";
    public static final String SE_UNSOLICITED_INPUT_NAME = "SeUnsolicitedInputPrivilege";
    public static final String SE_MACHINE_ACCOUNT_NAME = "SeMachineAccountPrivilege";
    public static final String SE_TCB_NAME = "SeTcbPrivilege";
    public static final String SE_SECURITY_NAME = "SeSecurityPrivilege";
    public static final String SE_TAKE_OWNERSHIP_NAME = "SeTakeOwnershipPrivilege";
    public static final String SE_LOAD_DRIVER_NAME = "SeLoadDriverPrivilege";
    public static final String SE_SYSTEM_PROFILE_NAME = "SeSystemProfilePrivilege";
    public static final String SE_SYSTEMTIME_NAME = "SeSystemtimePrivilege";
    public static final String SE_PROF_SINGLE_PROCESS_NAME = "SeProfileSingleProcessPrivilege";
    public static final String SE_INC_BASE_PRIORITY_NAME = "SeIncreaseBasePriorityPrivilege";
    public static final String SE_CREATE_PAGEFILE_NAME = "SeCreatePagefilePrivilege";
    public static final String SE_CREATE_PERMANENT_NAME = "SeCreatePermanentPrivilege";
    public static final String SE_BACKUP_NAME = "SeBackupPrivilege";
    public static final String SE_RESTORE_NAME = "SeRestorePrivilege";
    public static final String SE_SHUTDOWN_NAME = "SeShutdownPrivilege";
    public static final String SE_DEBUG_NAME = "SeDebugPrivilege";
    public static final String SE_AUDIT_NAME = "SeAuditPrivilege";
    public static final String SE_SYSTEM_ENVIRONMENT_NAME = "SeSystemEnvironmentPrivilege";
    public static final String SE_CHANGE_NOTIFY_NAME = "SeChangeNotifyPrivilege";
    public static final String SE_REMOTE_SHUTDOWN_NAME = "SeRemoteShutdownPrivilege";
    public static final String SE_UNDOCK_NAME = "SeUndockPrivilege";
    public static final String SE_SYNC_AGENT_NAME = "SeSyncAgentPrivilege";
    public static final String SE_ENABLE_DELEGATION_NAME = "SeEnableDelegationPrivilege";
    Advapi32b INSTANCE = (Advapi32b) Native.loadLibrary("Advapi32", Advapi32b.class, Options.UNICODE_OPTIONS);

    /*
    SC_HANDLE WINAPI OpenSCManager(
    LPCTSTR lpMachineName,
    LPCTSTR lpDatabaseName,
    DWORD dwDesiredAccess
    );*/
    public Pointer OpenSCManager(String lpMachineName, WString lpDatabaseName, int dwDesiredAccess);

    /*
    BOOL WINAPI CloseServiceHandle(
    SC_HANDLE hSCObject
    );*/
    public boolean CloseServiceHandle(Pointer hSCObject);

    /*
    SC_HANDLE WINAPI OpenService(
    SC_HANDLE hSCManager,
    LPCTSTR lpServiceName,
    DWORD dwDesiredAccess
    );*/
    public Pointer OpenService(Pointer hSCManager, String lpServiceName, int dwDesiredAccess);

    /*
    BOOL WINAPI StartService(
    SC_HANDLE hService,
    DWORD dwNumServiceArgs,
    LPCTSTR* lpServiceArgVectors
    );*/
    public boolean StartService(Pointer hService, int dwNumServiceArgs, char[] lpServiceArgVectors);

    /*
    BOOL WINAPI ControlService(
    SC_HANDLE hService,
    DWORD dwControl,
    LPSERVICE_STATUS lpServiceStatus
    );*/
    public boolean ControlService(Pointer hService, int dwControl, SERVICE_STATUS lpServiceStatus);

    /*
    BOOL WINAPI StartServiceCtrlDispatcher(
    const SERVICE_TABLE_ENTRY* lpServiceTable
    );*/
    public boolean StartServiceCtrlDispatcher(Structure[] lpServiceTable);

    /*
    SERVICE_STATUS_HANDLE WINAPI RegisterServiceCtrlHandler(
    LPCTSTR lpServiceName,
    LPHANDLER_FUNCTION lpHandlerProc
    );*/
    public Pointer RegisterServiceCtrlHandler(String lpServiceName, Handler lpHandlerProc);

    /*
    SERVICE_STATUS_HANDLE WINAPI RegisterServiceCtrlHandlerEx(
    LPCTSTR lpServiceName,
    LPHANDLER_FUNCTION_EX lpHandlerProc,
    LPVOID lpContext
    );*/
    public Pointer RegisterServiceCtrlHandlerEx(String lpServiceName, HandlerEx lpHandlerProc, Pointer lpContext);

    /*
    BOOL WINAPI SetServiceStatus(
    SERVICE_STATUS_HANDLE hServiceStatus,
    LPSERVICE_STATUS lpServiceStatus
    );*/
    public boolean SetServiceStatus(Pointer hServiceStatus, SERVICE_STATUS lpServiceStatus);

    /*
    SC_HANDLE WINAPI CreateService(
    SC_HANDLE hSCManager,
    LPCTSTR lpServiceName,
    LPCTSTR lpDisplayName,
    DWORD dwDesiredAccess,
    DWORD dwServiceType,
    DWORD dwStartType,
    DWORD dwErrorControl,
    LPCTSTR lpBinaryPathName,
    LPCTSTR lpLoadOrderGroup,
    LPDWORD lpdwTagId,
    LPCTSTR lpDependencies,
    LPCTSTR lpServiceStartName,
    LPCTSTR lpPassword
    );*/
    public Pointer CreateService(Pointer hSCManager, String lpServiceName, String lpDisplayName,
            int dwDesiredAccess, int dwServiceType, int dwStartType, int dwErrorControl,
            String lpBinaryPathName, String lpLoadOrderGroup, IntByReference lpdwTagId,
            String lpDependencies, String lpServiceStartName, String lpPassword);

    /*
    BOOL WINAPI DeleteService(
    SC_HANDLE hService
    );*/
    public boolean DeleteService(Pointer hService);

    /*
    BOOL WINAPI ChangeServiceConfig2(
    SC_HANDLE hService,
    DWORD dwInfoLevel,
    LPVOID lpInfo
    );*/
    public boolean ChangeServiceConfig2(Pointer hService, int dwInfoLevel, ChangeServiceConfig2Info lpInfo);

    /*
    LONG WINAPI RegDeleteKeyEx(
    HKEY hKey,
    LPCTSTR lpSubKey
    REGSAM samDesired
    DWORD Reserved //must be 0
    );*/
    public int RegDeleteKeyEx(HKEY hKey, String lpSubKey, int samDesired, int Reserved);

    /*LONG WINAPI RegDeleteTree(
    __in      HKEY hKey,
    __in_opt  LPCTSTR lpSubKey
    ); */
    public int RegDeleteTree(HKEY hKey, String lpSubKey);

    /*
    LONG WINAPI RegLoadKey(
    __in      HKEY hKey,
    __in_opt  LPCTSTR lpSubKey,
    __in      LPCTSTR lpFile
    ); */
    public int RegLoadKey(HKEY hKey, String lpSubKey, String lpFile);

    /*
    LONG WINAPI RegUnLoadKey(
    __in      HKEY hKey,
    __in_opt  LPCTSTR lpSubKey
    ); */
    public int RegUnLoadKey(HKEY hKey, String lpSubKey);

    /*
    LONG WINAPI RegSaveKeyEx(
    __in      HKEY hKey,
    __in      LPCTSTR lpFile,
    __in_opt  LPSECURITY_ATTRIBUTES lpSecurityAttributes,
    __in      DWORD Flags
    ); */
    public int RegSaveKeyEx(HKEY hKey, String lpFile, WINBASE.SECURITY_ATTRIBUTES lpSecurityAttributes, int Flags);

    /*
    LONG WINAPI RegRestoreKey(
    __in  HKEY hKey,
    __in  LPCTSTR lpFile,
    __in  DWORD dwFlags
    ); */
    public int RegRestoreKey(HKEY hKey, String lpFile, int dwFlags);

    /*
    typedef struct _LUID {
    DWORD LowPart;
    LONG  HighPart;
    } LUID */
    public static class LUID extends Structure {

        public int LowPart;
        public int HighPart;
    }

    /*
    typedef struct _LUID_AND_ATTRIBUTES {
    LUID  Luid;
    DWORD Attributes;
    } LUID_AND_ATTRIBUTES */
    public static class LUID_AND_ATTRIBUTES extends Structure {

        public LUID Luid;
        public int Attributes;
    }

    /*
    typedef struct _TOKEN_PRIVILEGES {
    DWORD               PrivilegeCount;
    LUID_AND_ATTRIBUTES Privileges[ANYSIZE_ARRAY];
    } TOKEN_PRIVILEGES */
    public static class TOKEN_PRIVILEGES extends Structure {

        public int PrivilegeCount;
        public LUID_AND_ATTRIBUTES[] Privileges;
    }
    public static final int TOKEN_PRIVILEGES_SIZE = 28;

    /*
    BOOL WINAPI AdjustTokenPrivileges(
    __in       HANDLE TokenHandle,
    __in       BOOL DisableAllPrivileges,
    __in_opt   PTOKEN_PRIVILEGES NewState,
    __in       DWORD BufferLength,
    __out_opt  PTOKEN_PRIVILEGES PreviousState,
    __out_opt  PDWORD ReturnLength
    ); */
    public boolean AdjustTokenPrivileges(HANDLE TokenHandle, boolean DisableAllPrivileges, TOKEN_PRIVILEGES NewState, int BufferLength, TOKEN_PRIVILEGES PreviousState, IntByReference ReturnLength);


    /*
    BOOL WINAPI LookupPrivilegeValue(
    __in_opt  LPCTSTR lpSystemName,
    __in      LPCTSTR lpName,
    __out     PLUID lpLuid
    ); */
    public boolean LookupPrivilegeValue(String lpSystemName, String lpName, LUID lpLuid);

    /*
    BOOL WINAPI InitializeSecurityDescriptor(
    __out  PSECURITY_DESCRIPTOR pSecurityDescriptor,
    __in   DWORD dwRevision
    ); */
    public boolean InitializeSecurityDescriptor(byte[] pSecurityDescriptor, int dwRevision);

    /*
    BOOL WINAPI SetSecurityDescriptorOwner(
    __inout   PSECURITY_DESCRIPTOR pSecurityDescriptor,
    __in_opt  PSID pOwner,
    __in      BOOL bOwnerDefaulted
    ); */
    public boolean SetSecurityDescriptorOwner(byte[] pSecurityDescriptor, byte[] pOwner, boolean bOwnerDefaulted);

    /*
    BOOL WINAPI IsValidSecurityDescriptor(
    __in  PSECURITY_DESCRIPTOR pSecurityDescriptor
    ); */
    public boolean IsValidSecurityDescriptor(byte[] pSecurityDescriptor);

    /*
    BOOL WINAPI SetFileSecurity(
    __in  LPCTSTR lpFileName,
    __in  SECURITY_INFORMATION SecurityInformation,
    __in  PSECURITY_DESCRIPTOR pSecurityDescriptor
    ); */
    public boolean SetFileSecurity(String lpFileName, int SecurityInformation, byte[] pSecurityDescriptor);

    interface SERVICE_MAIN_FUNCTION extends StdCallCallback {
        /*
        VOID WINAPI ServiceMain(
        DWORD dwArgc,
        LPTSTR* lpszArgv
        );*/

        public void callback(int dwArgc, Pointer lpszArgv);
    }

    interface Handler extends StdCallCallback {
        /*
        VOID WINAPI Handler(
        DWORD fdwControl
        );*/

        public void callback(int fdwControl);
    }

    interface HandlerEx extends StdCallCallback {
        /*
        DWORD WINAPI HandlerEx(
        DWORD dwControl,
        DWORD dwEventType,
        LPVOID lpEventData,
        LPVOID lpContext
        );*/

        public int callback(int dwControl, int dwEventType, Pointer lpEventData, Pointer lpContext);
    }

    /*
    typedef struct _SERVICE_STATUS {
    DWORD dwServiceType;
    DWORD dwCurrentState;
    DWORD dwControlsAccepted;
    DWORD dwWin32ExitCode;
    DWORD dwServiceSpecificExitCode;
    DWORD dwCheckPoint;
    DWORD dwWaitHint;
    } SERVICE_STATUS,
     *LPSERVICE_STATUS;*/
    public static class SERVICE_STATUS extends Structure {

        public int dwServiceType;
        public int dwCurrentState;
        public int dwControlsAccepted;
        public int dwWin32ExitCode;
        public int dwServiceSpecificExitCode;
        public int dwCheckPoint;
        public int dwWaitHint;
    }

    /*
    typedef struct _SERVICE_TABLE_ENTRY {
    LPTSTR lpServiceName;
    LPSERVICE_MAIN_FUNCTION lpServiceProc;
    } SERVICE_TABLE_ENTRY,
     *LPSERVICE_TABLE_ENTRY;*/
    public static class SERVICE_TABLE_ENTRY extends Structure {

        public String lpServiceName;
        public SERVICE_MAIN_FUNCTION lpServiceProc;
    }

    public static class ChangeServiceConfig2Info extends Structure {
    }

    /*
    typedef struct _SERVICE_DESCRIPTION {
    LPTSTR lpDescription;
    } SERVICE_DESCRIPTION,
     *LPSERVICE_DESCRIPTION;*/
    public static class SERVICE_DESCRIPTION extends ChangeServiceConfig2Info {

        public String lpDescription;
    }
}
