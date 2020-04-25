package glb;

import java.net.*;

/**
 * SecurityManagerの性能問題を改善するため、
 * いくつかの処理について無チェックとする。
 * Tenyu基盤ソフトウェアにおいて想定される権限設定のすべてのパターンにおいて
 * 必ず許可される操作はチェックする必要が無い。
 * https://stackoverflow.com/questions/8230792/whats-the-performance-penalty-if-any-of-using-a-securitymanager
 * http://openjdk.java.net/jeps/232
 *
 * @author exceptiontenyu@gmail.com
 *
 */
public final class TenyuSecurityManager extends SecurityManager {

    @Override
    public final void checkAccept(String host, int port) {
    }

    @Override
    public final void checkAccess(Thread t) {
    }

    @Override
    public final void checkAccess(ThreadGroup g) {
    }

    @Override
    public final void checkAwtEventQueueAccess() {
    }

    @Override
    public final void checkConnect(String host, int port) {
    }

    @Override
    public final void checkConnect(String host, int port, Object context) {
    }
/*
    @Override
    public final void checkCreateClassLoader() {
    }

    public final void checkDelete(String file) {
    };

    @Override
    public final void checkExec(String cmd) {
    }
    public final void checkExit(int status) {
        Thread.dumpStack();
    };
        @Override
    public final void checkPackageAccess(String pkg) {
    }

    @Override
    public final void checkPackageDefinition(String pkg) {
    }

    @Override
    public final void checkPermission(Permission perm) {
    }

    @Override
    public final void checkPermission(Permission perm, Object context) {
    }

    @Override
    public final void checkPropertiesAccess() {
    }

    public final void checkPropertyAccess(String key) {
    };

    @Override
    public final void checkPrintJobAccess() {
    }

    @Override
    public final void checkRead(FileDescriptor fd) {
    }

    @Override
    public final void checkRead(String file) {
    }

    @Override
    public final void checkRead(String file, Object context) {
    }

    @Override
    public final void checkSystemClipboardAccess() {
    }

    @Override
    public final void checkWrite(FileDescriptor fd) {
    }

    @Override
    public final void checkWrite(String file) {
    }
*/

    @Override
    public final void checkLink(String lib) {
    }

    @Override
    public final void checkListen(int port) {
    }

    @Override
    public final void checkMemberAccess(Class<?> clazz, int which) {
    }

    @Override
    public final void checkMulticast(InetAddress maddr) {
    }

    @Override
    public final void checkMulticast(InetAddress maddr, byte ttl) {
    }


    @Override
    public final void checkSecurityAccess(String target) {
    }

    @Override
    public final void checkSetFactory() {
    }

    @Override
    public final boolean checkTopLevelWindow(Object window) {
        return true;
    }

}