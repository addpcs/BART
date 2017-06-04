/*
 * Kernel32b.java
 *
 * Created on 6. August 2007, 14:43
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */
package com.addpcs.jna;

import com.sun.jna.Native;
import com.sun.jna.Pointer;
import com.sun.jna.Structure;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.win32.StdCallLibrary;

/**
 *
 * @author TB
 */
public interface Kernel32b extends StdCallLibrary {

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

    Kernel32b INSTANCE = (Kernel32b) Native.loadLibrary("Kernel32", Kernel32b.class, Options.UNICODE_OPTIONS);

    /*
    HLOCAL WINAPI LocalFree(
    HLOCAL hMem
    );*/
    public Pointer LocalFree(Pointer hMem);

    /*
    DWORD WINAPI GetLastError(void);*/
    public int GetLastError();

    /*
    BOOL WINAPI GetComputerName(
    __out    LPTSTR lpBuffer,
    __inout  LPDWORD lpnSize
    ); */
    public boolean GetComputerNameA(byte[] lpBuffer, IntByReference lpnSize);

    /*
    DWORD WINAPI ExpandEnvironmentStrings(
      __in       LPCTSTR lpSrc,
      __out_opt  LPTSTR lpDst,
      __in       DWORD nSize
    ); */
    public int ExpandEnvironmentStrings(String lpSrc, byte[] lpDst, int nSize);

    /*
    DWORD WINAPI GetEnvironmentVariable(
      __in_opt   LPCTSTR lpName,
      __out_opt  LPTSTR lpBuffer,
      __in       DWORD nSize
    ); */
    public int GetEnvironmentVariable(String lpName, byte[] lpBuffer, int nSize);

    /*
    DWORD WINAPI GetLongPathName(
      __in   LPCTSTR lpszShortPath,
      __out  LPTSTR lpszLongPath,
      __in   DWORD cchBuffer
    ); */
    public int GetLongPathName(String lpszShortPath, byte[] lpszLongPath, int cchBuffer);




    /*
    BOOL WINAPI GlobalMemoryStatusEx(
    __inout  LPMEMORYSTATUSEX lpBuffer
    ); */
    public boolean GlobalMemoryStatusEx(MEMORYSTATUSEX lpBuffer);

    /*
    typedef struct _MEMORYSTATUSEX {
    DWORD     dwLength;
    DWORD     dwMemoryLoad;
    DWORDLONG ullTotalPhys;
    DWORDLONG ullAvailPhys;
    DWORDLONG ullTotalPageFile;
    DWORDLONG ullAvailPageFile;
    DWORDLONG ullTotalVirtual;
    DWORDLONG ullAvailVirtual;
    DWORDLONG ullAvailExtendedVirtual;
    } MEMORYSTATUSEX */
    public static class MEMORYSTATUSEX extends Structure {
        public int dwLength;
        public int dwMemoryLoad;
        public long ullTotalPhys;
        public long ullAvailPhys;
        public long ullTotalPageFile;
        public long ullAvailPageFile;
        public long ullTotalVirtual;
        public long ullAvailVirtual;
        public long ullAvailExtendedVirtual;
    }

    /*
    DWORD WINAPI GetFileAttributes(
      __in  LPCTSTR lpFileName
    ); */
    public int GetFileAttributes(String lpFileName);

    /*
    BOOL WINAPI SetCurrentDirectory(
      __in  LPCTSTR lpPathName
    ); */
    public boolean SetCurrentDirectory(String lpPathName);

}
