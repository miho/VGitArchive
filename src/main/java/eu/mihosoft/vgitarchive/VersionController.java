/*
 * VersionController.java
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

import java.io.IOException;
import java.util.ArrayList;
import org.eclipse.jgit.revwalk.RevCommit;




/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface VersionController {

    /**
     * Checkout first version, i.e., version 1.
     * @throws IOException
     */
    void checkoutFirstVersion() throws IOException;

    /**
     * Checkout latest version, i.e., version with highest version number.
     * @throws IOException
     */
    void checkoutLatestVersion() throws IOException;

    /**
     * Checks out next version. Throws an {@link IllegalStateException}
     * if if such a version does not exist.
     * @return this file
     * @throws IOException
     * @throws IllegalStateException if the specified version does not exist
     */
    void checkoutNextVersion() throws IOException;

    /**
     * Checks out previous version. Throws an {@link IllegalStateException}
     * if if such a version does not exist.
     * @throws IOException
     * @throws IllegalStateException if the specified version does not exist
     */
    void checkoutPreviousVersion() throws IOException;

    /**
     * Checks out the specified version.
     * @param i version to checkout (version counting starts with <code>1</code>)
     * @throws IOException
     * @throws IllegalArgumentException if the specified version does not exist
     */
    void checkoutVersion(int i) throws IOException;

    /**
     * <p>Deletes the complete history of this file keeping only the latest
     * version, i.e., the version with the highest version number.</p>
     * <p><b>Warning:</b>Uncommited
     * changes will be lost. This action cannot be undone!</p>
     * @throws java.io.IOException
     * @throws IllegalStateException if this file is currently not open
     */
    void deleteHistory() throws IOException;

    /**
     * Returns the number of the current version.
     * @return the number of the current version
     * @throws IllegalStateException if this file is currently not open
     */
    int getCurrentVersion();

    /**
     * Returns the number of versions.
     * @return the number of versions or <code>-1</code> if an error occured
     * @throws IllegalStateException if this file is currently not open
     */
    int getNumberOfVersions();

    /**
     * Returns a list containing commit objects of all versions. This method
     * can be used to show the version messages, e.g., for creating a ui that
     * does allow the selection of the version that shall be checked out.
     * @return a list containing commit objects of all versions
     * @throws IOException
     * @throws IllegalStateException if this file is currently not open
     */
    ArrayList<RevCommit> getVersions() throws IOException;

    /**
     * Determines whether a version with version number
     * <code>currentVersion+1</code> exists.
     * @return <code>true</code> if a next version exists
     * @throws IllegalStateException if this file is currently not open
     */
    boolean hasNextVersion();

    /**
     * Determines whether a version with version number
     * <code>currentVersion-1</code>
     * exists. Version counting starts with <code>1</code>.
     * Version <code>0</code> is for internal use only and cannot be accessed.
     * @return <code>true</code> if a previous version exists
     * @throws IllegalStateException if this file is currently not open
     */
    boolean hasPreviousVersion();
    
    public void addVersionEventListener(VersionEventListener l );
    
    public void removeVersionEventListener(VersionEventListener l );
    
    public void removeAllVersionEventListeners();
    
    public Iterable<VersionEventListener> getVersionEventListeners();
    
}
