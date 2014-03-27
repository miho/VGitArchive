/*
 * VJarUtil.java
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

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;
import java.util.jar.Manifest;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class VJarUtil {

    // no instanciation allowed
    private VJarUtil() {
        throw new AssertionError(); // not in this class either!
    }

    /**
     * Reads the contents of the current jar entry of the specified jar input
     * stream and stores it in a byte array.
     *
     * @param jarInStream the stream to read from
     * @return the contents of the current jar entry
     * @throws IOException
     */
    public static byte[] readCurrentJarEntry(JarInputStream jarInStream)
            throws IOException {
        // read the whole contents of the
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buffer = new byte[2048];
        int len = 0;
        while ((len = jarInStream.read(buffer)) > 0) {
            out.write(buffer, 0, len);
        }
        return out.toByteArray();
    }

    /**
     * Returns the name of all classes in the specified stream.
     *
     * @param jarInStream the stream to read
     * @return a list containing the names of all classes
     * @throws IOException
     */
    public static ArrayList<String> getClassNamesFromStream(
            JarInputStream jarInStream) throws IOException {
        ArrayList<String> result = new ArrayList<String>();

        // the current jar entry
        JarEntry entry = jarInStream.getNextJarEntry();

        // iterate through all entries
        while (entry != null) {
            String name = entry.getName();

            // indicates whether the current entry is in folder or if it is
            // in the root folder of the jar
            boolean isInDirectory = name.lastIndexOf("/") > 0;

            // indicates that the entry is a class file
            boolean isClassFile = name.endsWith(".class");

            if (isInDirectory && isClassFile) {
                String className = pathToClassName(name);
                result.add(className);
            }

            entry = jarInStream.getNextJarEntry();
        }

        jarInStream.close();

        return result;
    }

    /**
     * Returns the name of all entries in the specified stream.
     *
     * @param jarInStream the stream to read
     * @return a list containing the names of all entries
     * @throws IOException
     */
    public static ArrayList<String> getEntryNamesFromStream(
            JarInputStream jarInStream) throws IOException {
        ArrayList<String> result = new ArrayList<String>();

        // the current jar entry
        JarEntry entry = jarInStream.getNextJarEntry();

        // iterate through all entries
        while (entry != null) {
            String name = entry.getName();

            // indicates whether the current entry is in folder or if it is
            // in the root folder of the jar
//            boolean isInDirectory = name.lastIndexOf("/") > 0;

            // indicates that the entry is a class file
//            boolean isClassFile = name.endsWith(".class");

//            if (isInDirectory && isClassFile) {

//            if (isClassFile) {
//                String className = pathToClassName(name);
            result.add(name);
//            }

            entry = jarInStream.getNextJarEntry();
        }

        jarInStream.close();

        return result;
    }

    /**
     * Converts a path to class name, i.e., replaces "/" by "." and removes the
     * ".class" extension.
     *
     * @param path the path to convert
     * @return the class name
     */
    public static String pathToClassName(String path) {
        return path.substring(0, path.length() - 6).replace("/", ".");
    }

    /**
     * Loads all classes of the specified jar archive. If errors occure while
     * loading a class it will be silently ignored.
     *
     * @param f jar file
     * @return all classes of the specified jar archive
     */
    public static Collection<Class<?>> loadClasses(File f) {

        return loadClasses(f, null);
    }

    /**
     * Loads all classes of the specified jar archive. If errors occure while
     * loading a class it will be silently ignored.
     *
     * @param f jar file
     * @return all classes of the specified jar archive
     */
    public static Collection<Class<?>> loadClasses(File f, ClassLoader loader) {

        ArrayList<String> classNames = null;

        try {
            classNames = VJarUtil.getClassNamesFromStream(
                    new JarInputStream(new FileInputStream(f)));
            if (loader == null) {
                loader =
                        new URLClassLoader(new URL[]{f.toURI().toURL()});
            }
        } catch (IOException ex) {
//            System.err.println(
//                    ">> ERROR while loading classes from file: " + f.getName());
            Logger.getLogger(
                    VJarUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        ArrayList<Class<?>> classes = new ArrayList<Class<?>>();

        for (String n : classNames) {
            try {
                classes.add(loader.loadClass(n));

            } catch (NoClassDefFoundError ex) {
//                System.err.println(">> ERROR: cannot add \"" + n +
//                        "\"");
//                System.err.println(" > cause: " + ex.toString());
            } catch (Exception ex) {
//                System.err.println(">> ERROR: cannot add \"" + n +
//                        "\"");
//                System.err.println(" > cause: " + ex.toString());
            } catch (java.lang.IncompatibleClassChangeError ex) {
//                System.err.println(">> ERROR: cannot add \"" + n +
//                        "\"");
//                System.err.println(" > cause: " + ex.toString());
            } catch (Throwable tr) {
                //
            }
        }

        return classes;
    }

    /**
     * Determines if the given Jar file contains the specified entry.
     *
     * @param in file to check
     * @param entryName name of the entry to search
     * @return
     * <code>true</code> if an entry with the specified name could be found;
     * <code>false</code> otherwise
     */
    static public boolean containsEntry(File in, String entryName) {

        if (!in.isFile()) {
            throw new IllegalArgumentException(
                    "The file \"" + in
                    + "\" is a directory or does not exit!");
        }

        // we are no jar file and cannot contain the requested entry
        if (!in.getAbsolutePath().toLowerCase().endsWith(".jar")) {
            return false;
        }


        try {
            java.util.jar.JarFile jar = new java.util.jar.JarFile(in);

            boolean result =  jar.getEntry(entryName) != null;
            
            jar.close();
            
            return result;

        } catch (IOException ex) {
            Logger.getLogger(VJarUtil.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Extracts the content of the specified Jar file.
     *
     * @param in the jar file to extract
     * @param out the ouptut directory
     * @throws IOException
     */
    static public void extractJarFile(File in, File out) throws IOException {

        if (!in.isFile()) {
            throw new IllegalArgumentException(
                    "The file \"" + in
                    + "\" is a directory or does not exit!");
        }

        if (!out.isDirectory()) {
            throw new IllegalArgumentException(
                    "The file \"" + in
                    + "\" is no directory or does not exit!");
        }

        java.util.jar.JarFile jar = new java.util.jar.JarFile(in);
        java.util.Enumeration entries = jar.entries();
        while (entries.hasMoreElements()) {
            java.util.jar.JarEntry file =
                    (java.util.jar.JarEntry) entries.nextElement();
            java.io.File f = new java.io.File(
                    out + java.io.File.separator + file.getName());
            if (file.isDirectory()) { // if its a directory, create it
                f.mkdir();
                continue;
            }
            BufferedInputStream is =
                    new BufferedInputStream(jar.getInputStream(file));
            java.io.FileOutputStream fos = new java.io.FileOutputStream(f);

            byte[] buffer = new byte[1024];
            while (true) {
                int count = is.read(buffer);
                if (count == -1) {
                    break;
                }
                fos.write(buffer, 0, count);
            }

            fos.close();
            is.close();
        }
    }

    /**
     * Returns the location of the Jar archive or .class file the specified
     * class has been loaded from. <b>Note:</b> this only works if the class is
     * loaded from a jar archive or a .class file on the locale file system.
     *
     * @param cls class to locate
     * @return the location of the Jar archive the specified class comes from
     */
    public static File getClassLocation(Class<?> cls) {

        VParamUtil.throwIfNull(cls);

        String className = cls.getName();
        ClassLoader cl = cls.getClassLoader();
        URL url = cl.getResource(className.replace(".", "/") + ".class");

        String urlString = url.toString().replace("jar:", "");

        if (!urlString.startsWith("file:")) {
            throw new IllegalArgumentException("The specified class\""
                    + cls.getName() + "\" has not been loaded from a location"
                    + "on the local filesystem.");
        }

        urlString = urlString.replace("file:", "");
        urlString = urlString.replace("%20", " ");

        int location = urlString.indexOf(".jar!");

        if (location > 0) {
            urlString = urlString.substring(0, location) + ".jar";
        } else {
            //System.err.println("No Jar File found: " + cls.getName());
        }

        return new File(urlString);
    }

    /**
     * Writes a default manifest file to the specified location (directory).
     *
     * @param location location (directory)
     * @throws IOException
     */
    public static void writeManifest(File location) throws IOException {

        VParamUtil.throwIfNotValid(
                VParamUtil.VALIDATOR_EXISTING_FOLDER, location);

        // Construct a string version of a manifest
        StringBuilder sbuf = new StringBuilder();
        sbuf.append("Manifest-Version: 1.0\n");
//        sbuf.append("Created-By: VRL-" + Constants.VERSION + "\n");

        // Convert the string to a input stream
        InputStream is = null;
        try {
            is = new ByteArrayInputStream(sbuf.toString().
                    getBytes("UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(VJarUtil.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        File meta_inf = new File(location.getAbsolutePath() + "/META-INF");
        meta_inf.mkdir();

        Manifest manifest = new Manifest(is);

        manifest.write(new FileOutputStream(
                new File(meta_inf.getAbsolutePath() + "/MANIFEST.MF")));
    }
}
