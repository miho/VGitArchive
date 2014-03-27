/*
 * IOUtil.java
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

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.channels.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

/**
 * IOUtil provides several I/O related methods. Among them are methods for
 * creating, copying and (un)compressing files and folders. In addition it
 * provides functionality for converting data loaded from file to base64 strings
 * and vice versa.
 *
 * <p>
 * <b>Note:</b> if running on MS Windows IOUtil adds a shutdown hook that tries
 * to ultimately delete temporary files before the JVM shuts down. To disable
 * this, call {@link #disableShutdownHook(boolean) }.</p>
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class IOUtil {

    private static boolean IO_DEBUG = false;
    private static boolean DISABLE_SHUTDOWN_HOOK = false;
    private static final Collection<String> filesToDeleteOnExit
            = new ArrayList<>();

    static {

        if (VSysUtil.isWindows()) {
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {

                    if (DISABLE_SHUTDOWN_HOOK) {
                        return;
                    }

                    String deleteCmds = "";

                    for (String f : filesToDeleteOnExit) {
                        deleteCmds += " rd /s/q \"\"\"\""
                                + f + "\"\"&";
                    }

                    // this part is all about solving file locking issues:
                    //
                    // search the web for "windows explorer file locking" to 
                    // learn more about this problem
                    //
                    // this command is a hack to force the world's most stupid
                    // OS to delete temporary files
                    String cmd = "cmd.exe /c \" start /w/min cmd.exe /c \"\""
                            // this ping command is a replacement for the sleep
                            // command that works on all windows versions
                            // (cmd & bat is crap!)
                            + "ping -n 5 127.0.0.1 > NUL &"
                            + deleteCmds
                            + "\"\" \"";

                    System.err.println("executing:\n" + cmd);
                    try {
                        Runtime.getRuntime().exec(cmd);
                    } catch (IOException ex) {
                        Logger.getLogger(IOUtil.class.getName()).
                                log(Level.SEVERE, null, ex);
                    }
                }
            }));
        }
    }

    /**
     * Defines whether to disable the shutdown hook added by IOUtil.
     *
     * If running on MS Windows IOUtil adds a shutdown hook that tries to
     * ultimately delete temporary files before the JVM shuts down. Sometimes
     * deletion is not possible due to file locking issues. This is a Windows
     * specific issue that cannot be solved (tested with Windows XP SP3 and
     * Windows 7 SP 1). Thus, this method does only have an effect if running on
     * MS Windows.
     *
     * <p>
     * <b>Note:</b> do not change the default behavior if you don't encounter
     * problems with file locking and the shutdown hook.</p>
     *
     * @param b defines whether to disable shutdown hook
     */
    public static void disableShutdownHook(boolean b) {
        DISABLE_SHUTDOWN_HOOK = b;
    }

    /**
     * Indicates whether shotdown hook is disabled.
     *
     * @return <code>true</code> if disabled; <code>false</code> otherwise
     */
    public static boolean isShutdownHookDisabled() {
        return DISABLE_SHUTDOWN_HOOK;
    }

    /**
     * Creates a new configuration file.
     */
    public static ConfigurationFile newConfigurationFile(File f) {
        return new ConfigurationFileImpl(f);
    }

    /**
     * @return the debug state
     */
    public static boolean isDebugginEnabled() {
        return IO_DEBUG;
    }

    /**
     * @param state the state to set
     */
    public static void enableDebugging(boolean state) {
        IO_DEBUG = state;
    }

    // no instanciation allowed
    private IOUtil() {
        throw new AssertionError(); // not in this class either!
    }

    /**
     * Converts a stream to a string. This method can be used to easily read
     * text files via <code>Class.getResourceAsStream(...)</code>
     *
     * @see
     * http://weblogs.java.net/blog/pat/archive/2004/10/stupid_scanner_1.html
     *
     * @param is
     * @return stream as String
     */
    public static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    /**
     * Loads data from file and converts it to a byte array.
     *
     * @param file the file to convert
     * @return a byte arry containing the converted data
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static byte[] fileToByteArray(File file)
            throws FileNotFoundException, IOException {
        byte[] result;
        try (FileInputStream fileInputStream = new FileInputStream(file)) {
            result = new byte[(int) file.length()];
            fileInputStream.read(result);
        }

        return result;
    }

    /**
     * Loads data from file and converts it to a base64 encoded string.
     *
     * @param file the file to convert
     * @return a string containing the encoded data
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static String fileToBase64(File file)
            throws FileNotFoundException, IOException {
        return Base64.encodeBytes(fileToByteArray(file));
    }

    /**
     * Decodes a base64 string to a byte array.
     *
     * @param data the data to decode
     * @return the decoded data as byte array
     */
    public static byte[] base64ToByteArray(String data) {
        return Base64.decode(data);
    }

    /**
     * Generates a SHA-1 checksum for a given byte array.
     *
     * @param data the data to convert
     * @return the checksum
     */
    @Deprecated
    public static String generateSHASum(byte[] data) {
        return generateSHA1Sum(data);
    }

    /**
     * Generates a SHA-1 checksum for a given byte array.
     *
     * @param data the data to convert
     * @return the checksum
     */
    public static String generateSHA1Sum(byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            return convertToHex(md.digest(data));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(IOUtil.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Generates a SHA-1 checksum for a given File.
     *
     * @param f the file
     * @return the checksum or an empty String (<code>""</code>) if the
     * specified file cannot be found/read
     */
    public static String generateSHA1Sum(File f) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-1");
            return convertToHex(md.digest(fileToByteArray(f)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    /**
     * Generates a MD5 checksum for a given File.
     *
     * @param f the file
     * @return the checksum or an empty String (<code>""</code>) if the
     * specified file cannot be found/read
     */
    public static String generateMD5Sum(File f) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            return convertToHex(md.digest(fileToByteArray(f)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    /**
     * Generates a CRC32 checksum for the specified byte array.
     *
     * @param bytes the byte array
     * @return the checksum for the specified byte array
     */
    public static long generateCRC32Sum(byte[] bytes) {
        Checksum checksum = new CRC32();

        checksum.update(bytes, 0, bytes.length);

        return checksum.getValue();
    }

    /**
     * Generates a CRC32 checksum for the specified file.
     *
     * @param f the file
     * @return the checksum for the specified file
     */
    public static long generateCRC32Sum(File f) {
        try {
            return generateCRC32Sum(fileToByteArray(f));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return 0;
    }

    /**
     * Generates a SHA-256 checksum for a given File.
     *
     * @param f the file
     * @return the checksum or an empty String (<code>""</code>) if the
     * specified file cannot be found/read
     */
    public static String generateSHA256um(File f) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            return convertToHex(md.digest(fileToByteArray(f)));
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException | NoSuchAlgorithmException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return "";
    }

    /**
     * Generates a SHA-2 (SHA-256) checksum for a given byte array.
     *
     * @param data the data to convert
     * @return the checksum
     */
    public static String generateSHA256Sum(byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("SHA-256");
            return convertToHex(md.digest(data));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(IOUtil.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Generates a MD5 checksum for a given byte array.
     *
     * @param data the data to convert
     * @return the checksum
     */
    public static String generateMD5Sum(byte[] data) {
        MessageDigest md = null;
        try {
            md = MessageDigest.getInstance("MD5");
            return convertToHex(md.digest(data));
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(IOUtil.class.getName()).
                    log(Level.SEVERE, null, ex);
        }

        return null;
    }

    /**
     * Verifies the specified file via MD5 checksum.
     *
     * @param f the file to verify (must exist)
     * @param checksum the checksum
     * @return <code>true</code> if the sverification is successful;
     * <code>false</code> otherwise
     */
    public static boolean verifyFileMD5(File f, String checksum) {
        try {
            byte[] fileData = IOUtil.fileToByteArray(f);
            String checksumOfFile = generateMD5Sum(fileData);

            return checksum.equals(checksumOfFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Verifies the specified file via SHA-1 checksum.
     *
     * @param f the file to verify (must exist)
     * @param checksum the checksum
     * @return <code>true</code> if the sverification is successful;
     * <code>false</code> otherwise
     */
    public static boolean verifyFileSHA1(File f, String checksum) {
        if (isDebugginEnabled()) {
            System.out.println(">> IOUtil.verifyFileSHA1: " + f);
        }
        try {
            byte[] fileData = IOUtil.fileToByteArray(f);
            String checksumOfFile = generateSHA1Sum(fileData);
            if (isDebugginEnabled()) {
                System.out.println(" --> sum1: " + checksum);
                System.out.println(" --> sum2: " + checksumOfFile);
                System.out.println(" -- result: " + checksum.equals(checksumOfFile));
            }
            return checksum.equals(checksumOfFile);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    /**
     * Retusn the root parent of the specified file, e.g., "/" on Unix or "C:\"
     * on Windows.
     *
     * @param f file
     * @return the root parent of the specified file
     */
    public static File getRootParent(File f) {

        File parent = f.getAbsoluteFile();

        while (parent != null) {
            File pF = parent.getAbsoluteFile().getParentFile();

            if (pF == null) {
                return parent;
            }

            parent = pF;

        }

        return parent;
    }

    /**
     * Returns the free space on the partion where the specified file is located
     *
     * @param f the file
     * @return the free space (byte)
     */
    public static long getFreeSpaceOnPartition(File f) {
        // TODO sometimes f.getUsableSpace() returned 0 and we had to use
        // the filesystem root to determine free space. is it safe now to
        // directly use f.getUsableSpace() ?
        return f.getUsableSpace();
    }

    /**
     * Returns the size of the specified file (byte). This method may use the
     * {@link File#length() } method or use an alternative implementation for
     * efficiency reasons.
     *
     * @param f file
     * @return the size of the specified file (byte)
     */
    public static long getFileSize(File f) {

        InputStream stream = null;
        try {
            URL url = f.toURI().toURL();
            stream = url.openStream();
            return stream.available();
        } catch (IOException ex) {
            Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            try {
                if (stream != null) {
                    stream.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(IOUtil.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return -1;

    }

    /**
     * Converts a byte array to hexadecimal String.
     *
     * @param data the data to convert
     * @return the data as hexa decimal string
     */
    private static String convertToHex(byte[] data) {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i < data.length; i++) {
            int halfbyte = (data[i] >>> 4) & 0x0F;
            int two_halfs = 0;
            do {
                if ((0 <= halfbyte) && (halfbyte <= 9)) {
                    buf.append((char) ('0' + halfbyte));
                } else {
                    buf.append((char) ('a' + (halfbyte - 10)));
                }
                halfbyte = data[i] & 0x0F;
            } while (two_halfs++ < 1);
        }
        return buf.toString();
    }

    /**
     * Copies a directory. If the target location does not exist it will be
     * created.
     * <p>
     * <b>Note:</b> existing target and contained files and directories will be
     * overwritten.</p>
     *
     * @param sourceLocation the source location
     * @param targetLocation the target location
     * @throws IOException
     */
    public static void copyDirectory(File sourceLocation, File targetLocation)
            throws IOException {

        if (IO_DEBUG) {
            System.out.println(">> Copy:");
            System.out.println(" --> from: " + sourceLocation);
            System.out.println(" --> to: " + targetLocation);
        }

        VParamUtil.throwIfNull(sourceLocation, targetLocation);

//        VParamUtil.throwIfNotValid(
//                VParamUtil.VALIDATOR_EXISTING_FOLDER, null, sourceLocation);
        if (!sourceLocation.exists()) {
            throw new FileNotFoundException(sourceLocation.getPath());
        }

        if (sourceLocation.isDirectory()) {

            if (!targetLocation.exists()) {
                targetLocation.mkdirs();
            }

            String[] children = sourceLocation.list();
            for (String children1 : children) {
                copyDirectory(new File(sourceLocation, children1),
                        new File(targetLocation, children1));
            }
        } else {
            copyFile(sourceLocation, targetLocation);
        }
    }

//    /**
//     * Reads and returns a resource text file, such as changelog etc.
//     *
//     * @param resourceName name of the resource, e.g.
//     * <code>/eu/mihosoft/vrl/resources/changelog/changelog.txt</code>
//     * @return
//     */
//    public static String readResourceTextFile(String resourceName) {
//        // load Sample Code
//        InputStream iStream = VRL.class.getResourceAsStream(
//                resourceName);
//
//        BufferedReader reader =
//                new BufferedReader(new InputStreamReader(iStream));
//
//        String text = "";
//
//        try {
//            while (reader.ready()) {
//                String line = reader.readLine();
//                text += line + "\n";
//            }
//        } catch (IOException ex) {
//            Logger.getLogger(Keywords.class.getName()).
//                    log(Level.SEVERE, null, ex);
//        } finally {
//            try {
//                reader.close();
//            } catch (IOException ex) {
//                Logger.getLogger(Keywords.class.getName()).
//                        log(Level.SEVERE, null, ex);
//            }
//        }
//        return text;
//    }
    /**
     * Recursively returns all files in the specified directory matching the
     * given regular expression.
     *
     * @param dir directory
     * @param pattern regex pattern
     * @return all files in the specified directory matching the given regular
     * expression
     */
    public static ArrayList<File> listFiles(
            File dir, String pattern) {
        return _listFiles(dir, pattern, new ArrayList<>());
    }

    private static ArrayList<File> _listFiles(
            File dir, String pattern, ArrayList<File> files) {
        VParamUtil.throwIfNull(dir, pattern);

        VParamUtil.throwIfNotValid(
                VParamUtil.VALIDATOR_EXISTING_FOLDER, null, dir);

        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                _listFiles(new File(dir, children[i]), pattern, files);
            }
        } else {
            if (dir.getName().matches(pattern)) {
                files.add(dir);
            }
        }

        return files;
    }

    /**
     * Copies a file. Existing target file will be overwritten.
     *
     * @param sourceLocation the source location
     * @param targetLocation the target location
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void copyFile(File sourceLocation, File targetLocation)
            throws FileNotFoundException, IOException {

        VParamUtil.throwIfNull(sourceLocation, targetLocation);

        VParamUtil.throwIfNotValid(
                VParamUtil.VALIDATOR_EXISTING_FILE, null, sourceLocation);

        try (FileChannel sourceChannel = new FileInputStream(sourceLocation).getChannel();
                FileChannel targetChannel = new FileOutputStream(targetLocation).getChannel()) {
            targetChannel.transferFrom(sourceChannel, 0,
                    sourceChannel.size());
        }
    }

    /**
     * Returns a temporary file object that contains the specified data.
     *
     * @todo: streams&files not properly closed if exceptions occur!
     *
     * @param data the binary data
     * @param extension the file extension to use (without dot)
     * @return a temporary file object that contains the specified data or
     * <code>null</code> if the file couldn't be created
     * @throws IOException
     */
    public static File byteArrayToTmpFile(byte[] data, String extension)
            throws IOException {
        File result = File.createTempFile("vrl", "." + extension);
        result.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(result)) {
            out.write(data);
            out.flush();
        }
        return result;
    }

    /**
     * Returns a temporary file object that contains the specified data (using
     * .tmp file extension).
     *
     * @param data the binary data
     * @return a temporary file object that contains the specified data or
     * <code>null</code> if the file couldn't be created
     * @throws IOException
     */
    public static File byteArrayToTmpFile(byte[] data) throws IOException {
        return byteArrayToTmpFile(data, "tmp");
    }

    /**
     * Returns a temporary file object that contains the specified data (using
     * .tmp file extension).
     *
     * @param data the binary data
     * @return a temporary file object that contains the specified data or
     * <code>null</code> if the file couldn't be created
     * @throws IOException
     */
    public static File stringToTmpFile(String data) throws IOException {
        return byteArrayToTmpFile(data.getBytes());
    }

    /**
     * Returns a temporary file object that contains the specified data.
     *
     * @param data the binary data
     * @param extension the file extension to use (without dot)
     * @return a temporary file object that contains the specified data or
     * <code>null</code> if the file couldn't be created
     * @throws IOException
     */
    public static File stringToTmpFile(String data, String extension) throws IOException {
        return byteArrayToTmpFile(data.getBytes(), extension);
    }

    /**
     * Returns a temporary file object that contains the specified data.
     *
     * TODO: streams&files not properly closed if exceptions occur!
     *
     * @param data the base64 encoded data as string
     * @param extension the file extension to use (without dot)
     * @return a temporary file object that contains the specified data or
     * <code>null</code> if the file couldn't be created
     * @throws IOException
     */
    public static File base64ToTmpFile(String data, String extension) throws IOException {
        File result = File.createTempFile("vrl", "." + extension);
        result.deleteOnExit();
        try (FileOutputStream out = new FileOutputStream(result)) {
            out.write(Base64.decode(data, Base64.GZIP));
            out.flush();
        }
        return result;
    }

    /**
     * Writes the specified data to the given file.
     *
     * TODO: streams&files not properly closed if exceptions occur!
     *
     * @param data the base64 encoded data as string
     * @param f the file
     * @return the file object that contains the specified data or
     * <code>null</code> if the file couldn't be created
     * @throws IOException
     */
    public static File base64ToFile(String data, File f) throws IOException {
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(Base64.decode(data, Base64.GZIP));
            out.flush();
        }
        return f;
    }

    /**
     * Returns a temporary file object that contains the specified data (using
     * .tmp file extension).
     *
     * @param data the base64 encoded data as string
     * @return a temporary file object that contains the specified data or
     * <code>null</code> if the file couldn't be created
     * @throws IOException
     */
    public static File base64ToTmpFile(String data) throws IOException {
        return base64ToTmpFile(data, "tmp");
    }

    /**
     * Reads the entire contents of a text file, and returns it in a String
     * list.
     *
     * @param file is a file which already exists and can be read.
     * @return
     */
    static public ArrayList<String> readFileToStringList(File file) {
        //...checks on aFile are elided
        ArrayList<String> contents = new ArrayList<>();

        try {
            try ( //FileReader always assumes default encoding is OK!
                    BufferedReader input = new BufferedReader(new FileReader(file))) {
                String line;

                while ((line = input.readLine()) != null) {
                    contents.add(line);
                }
            }
        } catch (IOException ex) {
        }

        return contents;
    }

    /**
     * Writes a string list to a file. Existing files will be completely
     * replaced.
     *
     * @param file is an existing file which can be written to.
     * @param lines
     * @throws IllegalArgumentException if param does not comply.
     * @throws FileNotFoundException if the file does not exist.
     * @throws IOException if problem encountered during write.
     */
    static public void writeStringListToFile(File file, List<String> lines)
            throws FileNotFoundException, IOException {
        if (file == null) {
            throw new IllegalArgumentException("File should not be null.");
        }
//        if (!file.exists()) {
//            throw new FileNotFoundException("File does not exist: " + file);
//        }
//        if (!file.isFile()) {
//            throw new IllegalArgumentException("Should not be a directory: " + file);
//        }
//        if (!file.canWrite()) {
//            throw new IllegalArgumentException("File cannot be written: " + file);
//        }
        try (BufferedWriter output = new BufferedWriter(new FileWriter(file))) {
            //FileWriter always assumes default encoding is OK!
            for (String line : lines) {
                output.write(line + "\n");
            }
        }
    }

    /**
     * Deletes dir and all files and subdirectories under dir. (deletes dir also
     * if dir is a regular file!).
     *
     * @param dir the directory to delete
     * @return <code>true</code> if all deletions were successful;
     * <code>false</code> otherwise
     */
    public static boolean deleteDirectory(File dir) {
        return deleteDirectory(dir, null);
    }

    /**
     * Deletes dir and all files and subdirectories under dir (deletes dir also
     * if dir is a regular file!).
     *
     * @param dir the directory to delete
     * @param excludes files to exclude
     * @return <code>true</code> if all deletions were successful;
     * <code>false</code> otherwise
     */
    public static boolean deleteDirectory(File dir, Collection<File> excludes) {

        // Deletes all files and subdirectories under dir.
        // Returns true if all deletions were successful.
        // If a deletion fails, the method stops attempting to delete
        // and returns false.
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String children1 : children) {
                boolean success
                        = deleteDirectory(new File(dir, children1), excludes);
                if (!success) {
                    return false;
                }
            }
        }

        boolean exclude = false;

        if (excludes != null) {

            for (File f : excludes) {
                if (dir.getAbsolutePath().startsWith(f.getAbsolutePath())) {
                    exclude = true;
                    break;
                }
            }
        }

        if (excludes == null || !exclude) {
            // The directory is now empty so delete it
            boolean result = dir.delete();

            if (isDebugginEnabled()) {
                System.out.println("DELETE: " + result + ", " + dir);
            }

            return result;
        } else {
            return true;
        }
    }

    /**
     * Deletes all files and subdirectories under dir.
     *
     * @param dir the directory to delete
     * @return <code>true</code> if all deletions were successful;
     * <code>false</code> otherwise
     */
    public static boolean deleteContainedFilesAndDirs(File dir) {
        return deleteContainedFilesAndDirs(dir, null);
    }

    /**
     * Deletes all files and subdirectories under dir.
     *
     * @param dir the directory to delete
     * @param excludes files to exclude
     * @return <code>true</code> if all deletions were successful;
     * <code>false</code> otherwise
     */
    public static boolean deleteContainedFilesAndDirs(
            File dir, Collection<File> excludes) {
        // Deletes all files and subdirectories under dir.
        // Returns true if all deletions were successful.
        // If a deletion fails, the method stops attempting to delete and
        // returns false.
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (String children1 : children) {
                boolean success = deleteDirectory(
                        new File(dir, children1), excludes);
                if (!success) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * Moves the specified source file to the given destination. If the
     * destination is a file (no directoy) it will be replaced. This method will
     * not replace the destination if it is a directory.
     *
     * @param src source file
     * @param dest destination file
     * @return <code>true</code> if successful; <code>false</code> otherwise
     */
    public static boolean move(File src, File dest) {

        if (VSysUtil.isWindows() && dest.exists()) {
            if (dest.isDirectory()) {
                return false;
            } else {
                deleteDirectory(dest);
            }
        }

        return src.renameTo(dest);
    }

    /**
     * Create a new temporary directory relative to the specified dir. The
     * directory and its content can be deleted on exit. Use {@link #deleteTmpFilesOnExit(java.io.File)
     * } to clean this directory up since it isn't deleted automatically.
     *
     * @param dir parent directory
     *
     * @see
     * http://stackoverflow.com/questions/617414/create-a-temporary-directory-in-java
     * @return the new directory
     * @throws IOException if there is an error creating the temporary directory
     */
    public static File createTempDir(File dir) throws IOException {
//        final File sysTempDir = new File(System.getProperty("java.io.tmpdir"));
        final File sysTempDir = dir;
        File newTempDir;
        final int maxAttempts = 9;
        int attemptCount = 0;
        do {
            attemptCount++;
            if (attemptCount > maxAttempts) {
                throw new IOException(
                        "The highly improbable has occurred! Failed to "
                        + "create a unique temporary directory after "
                        + maxAttempts + " attempts.");
            }
            String dirName = UUID.randomUUID().toString();
            newTempDir = new File(sysTempDir, dirName);
            newTempDir.deleteOnExit();
        } while (newTempDir.exists());

        if (newTempDir.mkdirs()) {

            deleteTmpFilesOnExit(newTempDir);

            return newTempDir;
        } else {
            throw new IOException(
                    "Failed to create temp dir named "
                    + newTempDir.getAbsolutePath());
        }
    }

    /**
     * Create a new temporary directory in the VRL tmp folder. The directory and
     * its content can be deleted on exit. Use {@link #deleteTmpFilesOnExit(java.io.File)
     * } to clean this directory up since it isn't deleted automatically.
     *
     * @see
     * http://stackoverflow.com/questions/617414/create-a-temporary-directory-in-java
     * @return the new directory
     * @throws IOException if there is an error creating the temporary directory
     */
    public static File createTempDir() throws IOException {
        return createTempDir(VRL.getPropertyFolderManager().getTmpFolder());
    }

    /**
     * Request deletion of file or directory on exit. Recursively requests
     * deletion of any sub directories and files.
     *
     * @param fileOrDir the file or directory to delete
     */
    public static void deleteTmpFilesOnExit(File fileOrDir) {
        if (fileOrDir.isDirectory()) {
            fileOrDir.deleteOnExit();

            // recursively delete contents
            for (File innerFile : fileOrDir.listFiles()) {
                deleteTmpFilesOnExit(innerFile);
            }

        } else if (fileOrDir.isFile()) {
            fileOrDir.deleteOnExit();
        }
    }

    /**
     * Request deletion of file or directory on exit. Recursively requests
     * deletion of any sub directories and files. If running on Windows it uses
     * cmd to delete files after JVM shutdown to prevent filelock problems.
     *
     * @param fileOrDir the file or directory to delete
     */
    public static void deleteTmpFilesOnExitIgnoreFileLocks(final File fileOrDir) {

        if (VSysUtil.isWindows()) {
            filesToDeleteOnExit.add(fileOrDir.getAbsolutePath());
            return;
        }

        if (fileOrDir.isDirectory()) {
            fileOrDir.deleteOnExit();

            // recursively delete contents
            for (File innerFile : fileOrDir.listFiles()) {
                deleteTmpFilesOnExit(innerFile);
            }

        } else if (fileOrDir.isFile()) {
            fileOrDir.deleteOnExit();
        }
    }

    /**
     * Saves the specified stream to file.
     *
     * @param in stream to save
     * @param f destination file
     * @throws FileNotFoundException
     * @throws IOException
     */
    public static void saveStreamToFile(InputStream in, File f)
            throws FileNotFoundException, IOException {

        VParamUtil.throwIfNull(in, f);
        OutputStream out = new FileOutputStream(f);

        try {
            int read;
            byte[] bytes = new byte[1024];

            while ((read = in.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }
        } catch (IOException ex) {
            throw ex;
        } finally {
            try {
                in.close();

            } catch (IOException ex) {
//                throw ex;
            } finally {
                try {
                    out.flush();
                    out.close();
                } catch (IOException ex) {
//                throw ex;
                }
            }
        }
    }

    /**
     * Recursively returns files that end with at least one of the specified
     * endings.
     * <p>
     * <b>Note</b>Folders are not considered. Thus, the resulting collection
     * only contains files.</p>
     *
     * @param sourceLocation folder to search
     * @param endings endings
     */
    public static ArrayList<File> listFiles(
            File sourceLocation, String[] endings) {
        ArrayList<File> result = new ArrayList<>();

        _getFilesRecursive(sourceLocation, result, endings);

        return result;
    }

    /**
     * Returns files that end with at least one of the specified endings.
     *
     * @param location folder to search
     * @param files files
     * @param endings endings
     */
    private static void _getFilesRecursive(
            File location, Collection<File> files, String[] endings) {

        if (location.isDirectory()) {

            String[] children = location.list();
            for (int i = 0; i < children.length; i++) {
                _getFilesRecursive(
                        new File(location, children[i]), files, endings);
            }
        } else {
            // sourcelocation  is file now
            for (String ending : endings) {
                if (location.getAbsolutePath().endsWith(ending)) {
                    files.add(location);
                    break;
                }
            }
        }
    }

    /**
     * Returns filtered files. If a file ends with at least one of the specified
     * endings it will be added to the set. If it is a directory, all children
     * will be added too. Directories are searched recursively for files that
     * might match.
     *
     * @param srcFolder
     * @param endings
     * @return set containing all matching files
     */
    static private Set<File> _getFilteredContent(
            File srcFolder, String... endings) {

        Set<File> result = new HashSet<File>();

        if (isDebugginEnabled()) {
            for (String e : endings) {
                System.out.println("ENDING: " + e);
            }
        }

        for (File f : srcFolder.listFiles()) {

            boolean fMatches = false;

            // TODO probably not sufficient to check for contains
            for (String e : endings) {
                if (f.getAbsolutePath().endsWith(e)
                        || f.getPath().contains(e)) {
                    fMatches = true;
                    break;
                }
            }

            if (isDebugginEnabled()) {
                System.out.println("Matches: [" + fMatches + "] = " + f);
            }

            if (fMatches) {

                result.add(f);

                if (f.isDirectory()) {
                    result.addAll(_getFilteredContent(f, ""));
                }
            } else if (f.isDirectory()) {
                result.addAll(_getFilteredContent(f, endings));
            }
        }

        return result;
    }

    /**
     * Compresses the content of a given folder and saves it as zip archive file
     * if it ends with one of the specified strings.
     *
     * @param srcFolder the source folder to compress
     * @param destZipFile the destination zip file
     * @param endings strings (only files that end with one of these strings
     * will be included)
     * @throws IOException
     */
    static public void zipContentOfFolder(
            File srcFolder, File destZipFile, String... endings) throws IOException {

        VParamUtil.throwIfNotValid(
                VParamUtil.VALIDATOR_EXISTING_FOLDER,
                null, srcFolder);

        final URI base = srcFolder.toURI();

        Deque<File> queue = new LinkedList<File>();
        queue.push(srcFolder);
        OutputStream out = new FileOutputStream(destZipFile);
        Closeable res = out;

        Set<File> matchedFiles = _getFilteredContent(srcFolder, endings);

        if (isDebugginEnabled()) {
            System.out.println("Zipping Files: ");

            for (File f : matchedFiles) {
                System.out.println(" --> f: " + f);
            }
        }

        try {
            ZipOutputStream zout = new ZipOutputStream(out);
            res = zout;
            while (!queue.isEmpty()) {
                srcFolder = queue.pop();
                for (File kid : srcFolder.listFiles()) {

                    String name = base.relativize(kid.toURI()).getPath();

                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));

                    } else {
                        if (matchedFiles.contains(kid)) {
                            zout.putNextEntry(new ZipEntry(name));
                            _copy(kid, zout);
                            zout.closeEntry();
                        }
                    }
                }
            }
        } finally {
            res.close();
        }
    }

    /**
     * Compresses the content of a given folder and saves it as zip archive
     * file.
     *
     * @param srcFolder the source folder to compress
     * @param destZipFile the destination zip file
     *
     * @throws IOException
     */
    static public void zipContentOfFolder(File srcFolder, File destZipFile) throws IOException {

        VParamUtil.throwIfNotValid(
                VParamUtil.VALIDATOR_EXISTING_FOLDER,
                null, srcFolder);

        URI base = srcFolder.toURI();
        Deque<File> queue = new LinkedList<File>();
        queue.push(srcFolder);
        OutputStream out = new FileOutputStream(destZipFile);
        Closeable res = out;

        try {
            ZipOutputStream zout = new ZipOutputStream(out);
            res = zout;
            while (!queue.isEmpty()) {
                srcFolder = queue.pop();
                for (File kid : srcFolder.listFiles()) {

                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                        zout.closeEntry();
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        _copy(kid, zout);
                        zout.closeEntry();
                    }
                }
            }
        } finally {
            res.close();
        }
    }

    /**
     * Compresses the given folder and saves it as zip archive file. The source
     * folder will be the root node in the zip archive
     *
     * @param srcFolder the source folder to compress
     * @param destZipFile the destination zip file
     *
     * @throws IOException
     */
    public static void zipFolder(File srcFolder, File destZipFile) throws IOException {

        // based on ideas from http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
        // now added nio channel copy methods
        VParamUtil.throwIfNotValid(
                VParamUtil.VALIDATOR_EXISTING_FOLDER,
                null, srcFolder);

        URI base = srcFolder.getParentFile().toURI();

        Deque<File> queue = new LinkedList<File>();
        queue.push(srcFolder);
        OutputStream out = new FileOutputStream(destZipFile);
        Closeable res = out;
        try {
            ZipOutputStream zout = new ZipOutputStream(out);
            res = zout;

            String baseName = srcFolder.getName();

            if (!baseName.endsWith("/")) {
                baseName = baseName + "/";
            }

            zout.putNextEntry(new ZipEntry(baseName));

            while (!queue.isEmpty()) {
                srcFolder = queue.pop();
                for (File kid : srcFolder.listFiles()) {
                    String name = base.relativize(kid.toURI()).getPath();
                    if (kid.isDirectory()) {
                        queue.push(kid);
                        name = name.endsWith("/") ? name : name + "/";
                        zout.putNextEntry(new ZipEntry(name));
                    } else {
                        zout.putNextEntry(new ZipEntry(name));
                        _copy(kid, zout);
                        zout.closeEntry();
                    }
                }
            }
        } finally {
            res.close();
        }
    }

    /**
     * Unzips specified source archive to the specified destination folder. If
     * the destination directory does not exist it will be created.
     *
     * @param archive archive to unzip
     * @param destDir destination directory
     * @throws IOException
     * @throws ZipException
     */
    public static void unzip(File archive, File destDir) throws IOException {

        // based on ideas from http://stackoverflow.com/questions/1399126/java-util-zip-recreating-directory-structure
        // now added nio channel copy methods
        VParamUtil.throwIfNotValid(
                VParamUtil.VALIDATOR_EXISTING_FILE,
                null, archive);

        ZipFile zfile = new ZipFile(archive);
        Enumeration<? extends ZipEntry> entries = zfile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File file = new File(destDir, entry.getName());
            if (entry.isDirectory()) {
                file.mkdirs();
            } else {
                file.getParentFile().mkdirs();
                InputStream in = zfile.getInputStream(entry);
                try {
                    _copy(in, entry.getSize(), file);
                } finally {
                    in.close();
                }
            }
        }

        zfile.close();
    }

    // 08.06.2012 TODO: check whether channels are really so much faster than stream&custom buffer
    private static void _copy(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        while (true) {
            int readCount = in.read(buffer);
            if (readCount < 0) {
                break;
            }
            out.write(buffer, 0, readCount);
        }
    }

    private static void _copy(File file, OutputStream out) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            _copy(in, out);
        } finally {
            in.close();
        }
    }

    // 20.06.2012 TODO: channels cause 'Too Many Open Files' exception
//    private static void _copy(File file, OutputStream out) throws IOException {
////        InputStream in = new FileInputStream(file);
////        try {
////            _copy(in, out);
////        } finally {
////            in.close();
////        }
//        
//        FileChannel sourceChannel = new FileInputStream(file).getChannel();
//        WritableByteChannel targetChannel = Channels.newChannel(out);
//        
//        try {
//            try {           
//                sourceChannel.transferTo(0, sourceChannel.size(), targetChannel);
//            } finally {
////                targetChannel.close();
//            }
//        } finally {
//            sourceChannel.close();
//        }
//        
//    }
    private static void _copy(InputStream in, long size, File file) throws IOException {
//        OutputStream out = new FileOutputStream(file);
//        try {
//            _copy(in, out);
//        } finally {
//            out.close();
//        }

        ReadableByteChannel sourceChannel = Channels.newChannel(in);
        FileChannel targetChannel = new FileOutputStream(file).getChannel();

        try {
            try {
                targetChannel.transferFrom(sourceChannel, 0,
                        size);
            } finally {
                targetChannel.close();
            }
        } finally {
            sourceChannel.close();
        }
    }
}
