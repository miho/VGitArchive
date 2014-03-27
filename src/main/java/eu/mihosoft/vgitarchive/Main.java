/*
 * Main.java
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
import java.nio.file.Paths;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
public class Main {

    public static void main(String[] args) {
        VersionedFile.setTmpFolder(Paths.get("tmp"));

        try (
                // create and open the file
                VersionedFile f = new VersionedFile(new File("project.vfile")).
                create().open();
                // prepare writing to a text file
                BufferedWriter writer = new BufferedWriter(
                        new FileWriter(f.getContent().
                                getPath() + "/file1.txt"))) {

            // first version
            f.commit("empty file created");

            // second version
            writer.write("NanoTime 1: " + System.nanoTime() + "\n");
            writer.flush();
            f.commit("timestamp added");

            // third version
            writer.write("NanoTime 2: " + System.nanoTime() + "\n");
            writer.flush();
            f.commit("another timestamp added");

            // checkout latest/newest version
            f.checkoutLatestVersion();

            // checkout previous versions one by one
            while (f.hasPreviousVersion()) {
                System.out.println("-> press enter to checkout the previous version");
                System.in.read(); // waiting for user input
                f.checkoutPreviousVersion();
            }
        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }
}
