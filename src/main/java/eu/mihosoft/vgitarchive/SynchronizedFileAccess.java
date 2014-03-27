/*
 * SynchronizedFileAccess.java
 *
 * Copyright 2013-2014 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */

package eu.mihosoft.vgitarchive;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Class to simplify synchronized file access via file locks.
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class SynchronizedFileAccess {

    /**
     * <p>Accesses a file, i.e., performs a file task and ensures that only one
     * task can access this file at once.</p> <p> <b>Note:</b> this method
     * may invoke
     * <code>Thread.sleep</code> on the current thread. </p>
     *
     * @param fTask the task to perform
     * @param f the file to access
     * @param maxRetries number of retries
     * @param retryDelay the retry delay (in milliseconds)
     * @throws FileNotFoundException if the file could not be found
     * @throws IOException if access failed
     */
    public static synchronized void access(
            SynchronizedFileTask fTask, File f, int maxRetries, long retryDelay)
            throws FileNotFoundException, IOException {

        access(fTask, f, maxRetries, retryDelay, true);
    }

    /**
     * <p>Accesses a file, i.e., performs a file task and ensures that only one
     * task can access this file at once.</p> <p> <b>Note:</b> this method
     * may invoke
     * <code>Thread.sleep</code> on the current thread.</p>
     *
     * <p> <b>Warning:</b> files locked with this method using
     * <code>unlock=false</code> cannot be unlocked from within the process that
     * calls this method. Only use this method to lock preference files to
     * assure that no other process accidently changes its content. </p>
     *
     * @param fTask the task to perform
     * @param f the file to access
     * @param maxRetries number of retries
     * @param retryDelay the retry delay (in milliseconds)
     * @param unlock defines whether to unlock the file after usage
     * @throws FileNotFoundException if the file could not be found
     * @throws IOException if access failed
     */
    private static synchronized void access(
            SynchronizedFileTask fTask, File f, int maxRetries, long retryDelay,
            boolean unlock)
            throws FileNotFoundException, IOException {

        if (f.exists()) {
            // Try to get the lock
            FileChannel channel = new RandomAccessFile(f, "rw").getChannel();
            FileLock lock;

            int counter = 0;

            while ((lock = channel.tryLock()) == null && counter < maxRetries) {
                // File is locked by other application
                System.out.println(
                        ">> SFA: Resource unavailable. Trying again in "
                        + (retryDelay / 1000.f) + " sec.");
                try {
                    Thread.sleep(retryDelay);
                } catch (InterruptedException e) {
                    //
                }

                counter++;
            }

            if (lock != null) {
                fTask.performTask(f);
            }

            // release file lock
            if (unlock) {
                try {
                    if (lock != null) {
                        lock.release();
                    }
                    channel.close();
                } catch (IOException e) {
                    //
                }
            }
        } else {
            fTask.performTask(f);
        }
    }

    /**
     * Locks the specified file. <p> <b>Warning:</b> files locked with this
     * method cannot be unlocked from within the process that calls this method.
     * Apossible use case for this method is to lock files to assure that no
     * other process accidently changes its content. </p>
     *
     * @param f the file to lock
     * @return
     * <code>true</code> if the file could be successfully locked;
     * <code>false</code> otherwise
     */
    public static synchronized boolean lockFile(File f) {
        LockedTask task = new LockedTask();

        try {
            access(task, f, false);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SynchronizedFileAccess.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SynchronizedFileAccess.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return task.locked;
    }

    public static synchronized boolean isLocked(File f) {

        LockedTask task = new LockedTask();

        try {
            access(task, f);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(SynchronizedFileAccess.class.getName()).
                    log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(SynchronizedFileAccess.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return task.locked;
    }

    /**
     * <p>Accesses a file, i.e., performs a file task and ensures that only one
     * task can access this file at once. It tries 10 times to get exclusive
     * access to the file with a retry delay of 0.3 seconds.</p> <p>
     * <b>Note:</b> this method may invoke
     * <code>Thread.sleep</code> on the current thread. </p>
     *
     * @param fTask the task to perform
     * @param f the file to access
     * @throws FileNotFoundException if the file could not be found
     * @throws IOException if access failed
     */
    public static synchronized void access(SynchronizedFileTask fTask, File f)
            throws FileNotFoundException, IOException {
        access(fTask, f, 10, 300);
    }

    /**
     * <p>Accesses a file, i.e., performs a file task and ensures that only one
     * task can access this file at once. It tries 10 times to get exclusive
     * access to the file with a retry delay of 0.3 seconds.</p> <p>
     * <b>Note:</b> this method may invoke
     * <code>Thread.sleep</code> on the current thread. </p>
     *
     * @param fTask the task to perform
     * @param f the file to access
     * @param unlock defines whether to unlock the file after usage
     * @throws FileNotFoundException if the file could not be found
     * @throws IOException if access failed
     */
    private static synchronized void access(SynchronizedFileTask fTask, File f,
            boolean unlock)
            throws FileNotFoundException, IOException {
        access(fTask, f, 10, 300, unlock);
    }
}
class LockedTask implements SynchronizedFileTask {

    boolean locked = true;

    @Override
    public void performTask(File f) {
        locked = false;
    }
}
