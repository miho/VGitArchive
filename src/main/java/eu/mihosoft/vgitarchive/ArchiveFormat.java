/*
 * ArchiveFormat.java
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

/**
 * Archive format used for VersionedFile. Possible implementations are ZIP,
 * TAR, etc.
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
interface ArchiveFormat {

    /**
     * Packs the content of the source folder to the destination file. Existing
     * files will be overwritten.
     * @param srcFolder source folder
     * @param destFile destination file
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean packContentsOfFolder(
            File srcFolder, File destFile);
    
    /**
     * Packs the content of the source folder to the destination file that 
     * ends with one of the specified endings. Existing
     * files will be overwritten.
     * @param srcFolder source folder
     * @param destFile destination file
     * @param endings accepted file endings
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean packContentsOfFolder(
            File srcFolder, File destFile, String... endings);
    
    /**
     * Unpacks the specified archive to the specified destinaltion folder.
     * Existing files and folders will be overwritten.
     * @param archive archive file
     * @param destFolder destination folder
     * @return {@code true} if successful; {@code false} otherwise
     */
    public boolean unpack(File archive, File destFolder);
    
    /**
     * Returns the identifier of this format, e.g., {@code "ZIP"}.
     * @return the identifier of this format
     */
    public String getIdentifier();
}
