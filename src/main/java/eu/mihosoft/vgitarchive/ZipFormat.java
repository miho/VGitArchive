/*
 * ZipFormat.java
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
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class ZipFormat implements ArchiveFormat {
    
    private static final String IDENTIFIER = "ZIP";

    @Override
    public boolean packContentsOfFolder(
            File folder, File destFile, String... endings) {
        try {
            IOUtil.zipContentOfFolder(
                    folder,
                    destFile,
                    endings);
        } catch (IOException ex) {
            Logger.getLogger(ZipFormat.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }

        return true;
    }
    
    @Override
    public boolean packContentsOfFolder(File folder, File destFile) {
        try {
            IOUtil.zipContentOfFolder(
                    folder,
                    destFile);
        } catch (IOException ex) {
            Logger.getLogger(ZipFormat.class.getName()).
                    log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    @Override
    public boolean unpack(File archive, File destFolder) {
        try {
            IOUtil.unzip(archive, destFolder);
        } catch (IOException ex) {
            Logger.getLogger(ZipFormat.class.getName()).
                    log(Level.SEVERE, null, ex);
            return false;
        }
        
        return true;
    }

    @Override
    public String getIdentifier() {
        return IDENTIFIER;
    }
}
