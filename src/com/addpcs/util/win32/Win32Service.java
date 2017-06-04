/*
 * Win32Service.java
 *
 * Created on 12. September 2007, 12:05
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.addpcs.util.win32;

import com.addpcs.jna.*;
import com.addpcs.jna.Advapi32b.SERVICE_STATUS;
import com.sun.jna.Pointer;

/**
 * Baseclass for a Win32 service.
 */
public abstract class Win32Service {

    public final static int GENERIC_EXECUTE = 0x20000000;
    public final static int SERVICE_WIN32_OWN_PROCESS = 0x00000010;

    protected String serviceName;
    private ServiceMain serviceMain;
    private ServiceControl serviceControl;
    private Pointer serviceStatusHandle;
    private Object waitObject = new Object();

    /**
     * Creates a new instance of Win32Service.
     *
     * @param serviceName internal name of the service
     */
    public Win32Service(String serviceName) {
        this.serviceName = serviceName;
    }

    /**
     * Install the service.
     *
     * @param displayName visible name
     * @param description description
     * @param dependencies array of other services to depend on or null
     * @param account service account or null for LocalSystem
     * @param password password for service account or null
     * @throws java.lang.Exception
     * @return true on success
     */
    public boolean install(String displayName, String description, String[] dependencies, String account, String password) {
        return (install(displayName, description, dependencies, account, password, "java.exe -cp \"" +
                System.getProperty("java.class.path") + "\" -Xrs " + this.getClass().getName()));
    }

    /**
     * Install the service.
     *
     * @return true on success
     * @param displayName visible name
     * @param description description
     * @param dependencies array of other services to depend on or null
     * @param account service account or null for LocalSystem
     * @param password password for service account or null
     * @param command command line to start the service
     * @throws java.lang.Exception
     */
    public boolean install(String displayName, String description, String[] dependencies, String account, String password, String command) {
        Advapi32b advapi32;
        Advapi32b.SERVICE_DESCRIPTION desc;
        Pointer serviceManager, service;
        boolean success = false;
        String dep = "";

        if (dependencies != null) {
            for (String s : dependencies) {
                dep += s + "\0";
            }
        }
        dep += "\0";

        desc = new Advapi32b.SERVICE_DESCRIPTION();
        desc.lpDescription = description;

        advapi32 = Advapi32b.INSTANCE;
        serviceManager = openServiceControlManager(null, WINSVC.SC_MANAGER_ALL_ACCESS);

        if (serviceManager != null) {
            service = advapi32.CreateService(serviceManager, serviceName, displayName,
                    WINSVC.SERVICE_ALL_ACCESS, WINSVC.SERVICE_WIN32_OWN_PROCESS, WINSVC.SERVICE_DEMAND_START,
                    WINSVC.SERVICE_ERROR_NORMAL,
                    command,
                    null, null, dep, account, password);

            if (service != null) {
                success = advapi32.ChangeServiceConfig2(service, WINSVC.SERVICE_CONFIG_DESCRIPTION, desc);
                advapi32.CloseServiceHandle(service);
            }
            advapi32.CloseServiceHandle(serviceManager);
        }
        return (success);
    }

    /**
     * Uninstall the service.
     *
     * @throws java.lang.Exception
     * @return true on success
     */
    public boolean uninstall() {
        Advapi32b advapi32;
        Pointer serviceManager, service;
        boolean success = false;

        advapi32 = Advapi32b.INSTANCE;
        serviceManager = openServiceControlManager(null, WINSVC.SC_MANAGER_ALL_ACCESS);

        if (serviceManager != null) {
            service = advapi32.OpenService(serviceManager, serviceName, WINSVC.SERVICE_ALL_ACCESS);

            if (service != null) {
                success = advapi32.DeleteService(service);
                advapi32.CloseServiceHandle(service);
            }
            advapi32.CloseServiceHandle(serviceManager);
        }
        return (success);
    }

    /**
     * Ask the ServiceControlManager to start the service.
     * @return true on success
     */
    public boolean start() {
        Advapi32b advapi32;
        Pointer serviceManager, service;
        boolean success = false;

        advapi32 = Advapi32b.INSTANCE;

        serviceManager = openServiceControlManager(null, GENERIC_EXECUTE);

        if (serviceManager != null) {
            service = advapi32.OpenService(serviceManager, serviceName, GENERIC_EXECUTE);

            if (service != null) {
                success = advapi32.StartService(service, 0, null);
                advapi32.CloseServiceHandle(service);
            }
            advapi32.CloseServiceHandle(serviceManager);
        }

        return (success);
    }

    public static boolean StartService(String serviceName) {
        Advapi32b advapi32;
        Pointer serviceManager, service;
        boolean success = false;

        advapi32 = Advapi32b.INSTANCE;

        serviceManager = advapi32.OpenSCManager(null, null, GENERIC_EXECUTE);

        if (serviceManager != null) {
            service = advapi32.OpenService(serviceManager, serviceName, GENERIC_EXECUTE);

            if (service != null) {
                success = advapi32.StartService(service, 0, null);
                advapi32.CloseServiceHandle(service);
            }
            advapi32.CloseServiceHandle(serviceManager);
        }

        return (success);
    }

    public static boolean StopService(String serviceName){
        Advapi32b advapi32;
        Pointer serviceManager, service;
        Advapi32b.SERVICE_STATUS serviceStatus;
        boolean success = false;

        advapi32 = Advapi32b.INSTANCE;

        serviceManager = advapi32.OpenSCManager(null, null, GENERIC_EXECUTE);

        if (serviceManager != null) {
            service = advapi32.OpenService(serviceManager, serviceName, GENERIC_EXECUTE);

            if (service != null) {
                serviceStatus = new Advapi32b.SERVICE_STATUS();
                success = advapi32.ControlService(service, WINSVC.SERVICE_CONTROL_STOP, serviceStatus);
                advapi32.CloseServiceHandle(service);
            }
            advapi32.CloseServiceHandle(serviceManager);
        }

        return (success);
    }

    public static SERVICE_STATUS QueryService(String serviceName){
        Advapi32b advapi32;
        Pointer serviceManager, service;
        SERVICE_STATUS serviceStatus = null;
        boolean success = false;

        advapi32 = Advapi32b.INSTANCE;

        serviceManager = advapi32.OpenSCManager(null, null, GENERIC_EXECUTE);

        if (serviceManager != null) {
            service = advapi32.OpenService(serviceManager, serviceName, GENERIC_EXECUTE);

            if (service != null) {
                serviceStatus = new SERVICE_STATUS();
                success = advapi32.ControlService(service, WINSVC.SERVICE_QUERY_STATUS, serviceStatus);
                advapi32.CloseServiceHandle(service);
            }
            advapi32.CloseServiceHandle(serviceManager);
        }

        if (success) {
            return (serviceStatus);
        }
        return null;
    }

    /**
     * Ask the ServiceControlManager to stop the service.
     * @return true on success
     */
    public boolean stop() throws Exception {
        Advapi32b advapi32;
        Pointer serviceManager, service;
        Advapi32b.SERVICE_STATUS serviceStatus;
        boolean success = false;

        advapi32 = Advapi32b.INSTANCE;

        serviceManager = openServiceControlManager(null, GENERIC_EXECUTE);

        if (serviceManager != null) {
            service = advapi32.OpenService(serviceManager, serviceName, GENERIC_EXECUTE);

            if (service != null) {
                serviceStatus = new Advapi32b.SERVICE_STATUS();
                success = advapi32.ControlService(service, WINSVC.SERVICE_CONTROL_STOP, serviceStatus);
                advapi32.CloseServiceHandle(service);
            }
            advapi32.CloseServiceHandle(serviceManager);
        }

        return (success);
    }

    /**
     * Initialize the service, connect to the ServiceControlManager.
     */
    public void init() {
        Advapi32b advapi32;
        Advapi32b.SERVICE_TABLE_ENTRY[] entries = new Advapi32b.SERVICE_TABLE_ENTRY[2];
        Advapi32b.SERVICE_TABLE_ENTRY entry;

        serviceMain = new ServiceMain();
        advapi32 = Advapi32b.INSTANCE;
        entry = new Advapi32b.SERVICE_TABLE_ENTRY();
        entry.lpServiceName = serviceName;
        entry.lpServiceProc = serviceMain;

        advapi32.StartServiceCtrlDispatcher(entry.toArray(2));
    }

    /**
     * Get a handle to the ServiceControlManager.
     *
     * @param machine name of the machine or null for localhost
     * @param access access flags
     * @return handle to ServiceControlManager or null when failed
     */
    private Pointer openServiceControlManager(String machine, int access) {
        Pointer handle = null;
        Advapi32b advapi32;

        advapi32 = Advapi32b.INSTANCE;
        handle = advapi32.OpenSCManager(machine, null, access);
        return (handle);
    }

    /**
     * Report service status to the ServiceControlManager.
     *
     * @param status status
     * @param win32ExitCode exit code
     * @param waitHint time to wait
     */
    private void reportStatus(int status, int win32ExitCode, int waitHint) {
        Advapi32b advapi32;
        Advapi32b.SERVICE_STATUS serviceStatus;

        advapi32 = Advapi32b.INSTANCE;
        serviceStatus = new Advapi32b.SERVICE_STATUS();
        serviceStatus.dwServiceType = SERVICE_WIN32_OWN_PROCESS;
        serviceStatus.dwControlsAccepted = WINSVC.SERVICE_ACCEPT_STOP | WINSVC.SERVICE_ACCEPT_SHUTDOWN;
        serviceStatus.dwWin32ExitCode = win32ExitCode;
        serviceStatus.dwWaitHint = waitHint;
        serviceStatus.dwCurrentState = status;

        advapi32.SetServiceStatus(serviceStatusHandle, serviceStatus);
    }

    /**
     * Called when service is starting.
     */
    public abstract void onStart();

    /*
     * Called when service should stop.
     */
    public abstract void onStop();

    /**
     * Implementation of the service main function.
     */
    private class ServiceMain implements Advapi32b.SERVICE_MAIN_FUNCTION {

        /**
         * Called when the service is starting.
         *
         * @param dwArgc number of arguments
         * @param lpszArgv pointer to arguments
         */
        @Override
        public void callback(int dwArgc, Pointer lpszArgv) {
            Advapi32b advapi32;

            advapi32 = Advapi32b.INSTANCE;

            serviceControl = new ServiceControl();
            serviceStatusHandle = advapi32.RegisterServiceCtrlHandlerEx(serviceName, serviceControl, null);

            reportStatus(WINSVC.SERVICE_START_PENDING, WINERROR.NO_ERROR, 3000);
            reportStatus(WINSVC.SERVICE_RUNNING, WINERROR.NO_ERROR, 0);

            onStart();

            try {
                synchronized (waitObject) {
                    waitObject.wait();
                }
            } catch (InterruptedException ex) {
            }
            reportStatus(WINSVC.SERVICE_STOPPED, WINERROR.NO_ERROR, 0);
        }
    }

    /**
     * Implementation of the service control function.
     */
    private class ServiceControl implements Advapi32b.HandlerEx {

        /**
         * Called when the service get a control code.
         *
         * @param dwControl
         * @param dwEventType
         * @param lpEventData
         * @param lpContext
         */
        @Override
        public int callback(int dwControl, int dwEventType, Pointer lpEventData, Pointer lpContext) {
            switch (dwControl) {
                case WINSVC.SERVICE_CONTROL_STOP:
                case WINSVC.SERVICE_CONTROL_SHUTDOWN:
                    reportStatus(WINSVC.SERVICE_STOP_PENDING, WINERROR.NO_ERROR, 5000);
                    onStop();
                    synchronized (waitObject) {
                        waitObject.notifyAll();
                    }
            }
            return 0;
        }
    }
}
