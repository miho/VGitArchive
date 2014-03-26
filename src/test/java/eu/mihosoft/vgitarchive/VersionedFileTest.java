/*
 * VersionedFileTest.java
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class VersionedFileTest {

    @Test
    public void createFileTest() {

        File f = new File("versionedFile.vfile");

        createVersionedFile(f);

        assertTrue("Versioned file must exist", f.exists());

    }

    private VersionedFile createVersionedFile(File f) {

        f.delete();

        try {

            // tests do not close files properly
            VersionedFile.clearOpenedFilesRecord();

            VersionedFile vf = new VersionedFile(f).create().cleanup();

            return vf;
        } catch (IOException ex) {
            Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        return null;
    }

    @Test
    public void commitFiles() {

        File f = new File("versionedFile.vfile");

        VersionedFile vf = createVersionedFile(f);

        try {
            vf.open();
        } catch (IOException ex) {
            Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
        }

        assertNotNull(vf);

        File internalFile = new File(vf.getContent().getPath(), "file1.txt");

        List<String> timeStamps = commitToFile(vf, internalFile, 100);

        System.out.println("#versions: " + vf.getNumberOfVersions());

        for (int i = 1; i <= vf.getNumberOfVersions(); i++) {

            List<String> lines = new ArrayList<>();

            try {
                vf.checkoutVersion(i);
                lines = readLines(internalFile);
            } catch (IOException ex) {
                Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
                fail(ex.getMessage());
            }

            String l = lines.get(i - 1);
            String ts = timeStamps.get(i - 1);
            assertTrue("l: " + l + ", ts: " + ts, Objects.equals(ts, l));
        }
    }

    @Test
    public void containsHistoryOfFile() {

        File f0 = new File("versionedFile0.vfile");
        VersionedFile vf0 = createVersionedFile(f0);

        try {
            vf0.open();
        } catch (IOException ex) {
            Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        File internalFile0 = new File(vf0.getContent().getPath(), "file1.txt");
        commitToFile(vf0, internalFile0, 100);

        try {
            vf0.close();
        } catch (IOException ex) {
            Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        File f1 = new File("versionedFile1.vfile");
        VersionedFile vf1 = createVersionedFile(f1);

        try {
            vf1.open();
        } catch (IOException ex) {
            Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        File internalFile1 = new File(vf1.getContent().getPath(), "file1.txt");
        commitToFile(vf1, internalFile1, 100);

        try {
            vf1.close();
        } catch (IOException ex) {
            Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        File f2 = new File("versionedFile2.vfile");

        try {
            IOUtil.copyFile(f1, f2);
        } catch (IOException ex) {
            Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        VersionedFile vf2 = new VersionedFile(f2);

        try {
            vf2.open();
            vf1.open();
            vf0.open();
        } catch (IOException ex) {
            Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        File internalFile2 = new File(vf2.getContent().getPath(), "file1.txt");
        commitToFile(vf2, internalFile2, 100);

        assertTrue("File 2 must contain file 1 since 2 builds on top of 1", vf2.contains(vf1));
        assertFalse("File 2 must not contain file 0 since they are unrelated", vf2.contains(vf0));
        assertFalse("File 1 must not contain file 0 since they are unrelated", vf1.contains(vf0));
        assertFalse("File 1 must not contain file 2 since 2 has more history than 1", vf1.contains(vf2));
    }

    private List<String> readLines(File f) {
        try {
            return Files.readAllLines(Paths.get(f.getPath()));
        } catch (IOException ex) {
            Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
            fail(ex.getMessage());
        }

        return new ArrayList<>();
    }

    private List<String> commitToFile(VersionedFile vf, File internalFile, int numCommits) {

        assertNotNull(vf);

        List<String> timeStamps = new ArrayList<>();

        // first version
        try (
                // prepare writing to a text file
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter(internalFile))) {

                    for (int i = 1; i <= 100; i++) {
                        String timeStamp = "NanoTime " + i + ": " + System.nanoTime();

                        timeStamps.add(timeStamp);

                        writer.write(timeStamp + "\n");
                        writer.flush();

                        vf.commit("timestamp " + i + "added");
                    }

                } catch (IOException ex) {
                    Logger.getLogger(VersionedFileTest.class.getName()).log(Level.SEVERE, null, ex);
                    fail(ex.getMessage());
                }

                return timeStamps;
    }

}
