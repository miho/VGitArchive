/*
 * VPropertyFolderManager.java
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Michael Hoffer &lt;info@michaelhoffer.de&gt;
 */
class VPropertyFolderManager {

    private final String HOME_FOLDER
            = System.getProperty("user.home");
    private final File PROPERTY_FOLDER_BASE;
    private File PROPERTY_FOLDER;
    private File PROPERTY_FOLDER_TEMPLATE;
    private File TMP_BASE;
    private File PLUGIN_UPDATES;
    private File CONFIG_FOLDER;
    private File UPDATES;
    private File ETC;
    private File TMP;
    private File RESOURCES;
    private File PROJECT_TEMPLATES;
    private int maximumNumberOfBackups = 5;
    private boolean initialized = false;
    private Runnable alreadyRunningTask;

    public VPropertyFolderManager() {
        PROPERTY_FOLDER_BASE = generatePropertyBasePath(Paths.get(System.getProperty("java.io.tmpdir")));
    }

    public VPropertyFolderManager(Path baseLocation) {
        PROPERTY_FOLDER_BASE = generatePropertyBasePath(baseLocation);
    }

    private static File generatePropertyBasePath(Path baseLocation) {
        return new File(baseLocation.toFile()
                + "/.vgitarchive/"
                + Constants.VERSION_INFO.getVersion());
    }

    public void setAlreadyRunningTask(Runnable r) {
        alreadyRunningTask = r;
    }

    public void installBundledSoftwareAndResources() throws IOException {
        IOUtil.copyDirectory(PROPERTY_FOLDER_TEMPLATE, getPropertyFolder());
    }

    public void create() throws IOException {

        if (PROPERTY_FOLDER.exists() && !getPropertyFolder().isDirectory()) {
            throw new IOException("VRL property folder cannot be created: "
                    + getPropertyFolder().getAbsolutePath()
                    + " A file with this"
                    + " name already exists.");
        }

        if (PROPERTY_FOLDER_TEMPLATE != null) {

            if (!getPropertyFolder().exists()) {
                System.out.println(
                        " --> initializing property folder from template");

                if (PROPERTY_FOLDER_TEMPLATE.isDirectory()) {
                    IOUtil.copyDirectory(PROPERTY_FOLDER_TEMPLATE, getPropertyFolder());
                } else {
                    System.out.println(
                            "  --> requested template does not exist! Using empty folder.");
                }
            }
        }

        if (!getPropertyFolder().exists()) {
            if (!getPropertyFolder().mkdirs()) {
                throw new IOException("VRL property folder cannot be created: "
                        + getPropertyFolder().getAbsolutePath());
            }
        }

        CONFIG_FOLDER.mkdirs();

        // init plugin update folder
        PLUGIN_UPDATES.mkdir();

        // init update folder
        UPDATES.mkdir();

        // init tmp folder
        TMP_BASE.mkdir();

        // init etc folder
        ETC.mkdir();

        // init resources folder
        RESOURCES.mkdir();

        // init templates folder
        PROJECT_TEMPLATES.mkdir();
    }

    public void remove() throws IOException {
        System.out.println(">> removing preference folder");
        if (VSysUtil.isWindows()) {
            IOUtil.deleteTmpFilesOnExitIgnoreFileLocks(getPropertyFolder());
        } else {
            IOUtil.deleteDirectory(getPropertyFolder());
        }
    }

    public void init() {
        init(null, false);
    }

    public void init(String folder, boolean asSuffix) {
        if (!isInitialized()) {

            if (asSuffix) {
                if (folder == null) {
                    setPropertyFolder(new File(PROPERTY_FOLDER_BASE, "default"));
                } else {
                    setPropertyFolder(new File(PROPERTY_FOLDER_BASE, folder));
                }
            } else {
                if (folder == null) {
                    setPropertyFolder(new File(PROPERTY_FOLDER_BASE, "default"));
                } else {
                    setPropertyFolder(new File(folder));
                }
            }
            System.out.println(
                    " --> initializing property folder: " + getPropertyFolder());

            TMP_BASE = new File(getPropertyFolder(), "tmp");
            TMP = new File(TMP_BASE, "0");

            CONFIG_FOLDER = new File(getPropertyFolder(), "config");

            PLUGIN_UPDATES = new File(getPropertyFolder(), "plugin-updates");
            UPDATES = new File(getPropertyFolder(), "updates");
            ETC = new File(getPropertyFolder(), "etc");
            RESOURCES = new File(getPropertyFolder(), "resources");
            PROJECT_TEMPLATES = new File(RESOURCES, "project-templates");

//            if (isLocked() && alreadyRunningTask != null) {
//                VSwingUtil.invokeAndWait(alreadyRunningTask);
////                alreadyRunningTask.run();
//            }
            lockFolder(true);

            try {
                create();
            } catch (IOException ex) {
                Logger.getLogger(VPropertyFolderManager.class.getName()).
                        log(Level.SEVERE, null, ex);
            }

            shiftTmpFolders();
            initialized = true;
        } else {
            throw new IllegalStateException("Already initialized.");
        }
    }

    private void shiftTmpFolders() {

        // delete all tmp files that don't match
        for (File f : TMP_BASE.listFiles()) {

            // if name is not a number delete it
            if (!f.getName().matches("\\d+")) {
                IOUtil.deleteDirectory(f);
            }//
            // if name interpreted as number is bigger than maxNumberBackup
            // delete it
            else {
                int value = new Integer(f.getName());
                if (value > maximumNumberOfBackups) {
                    IOUtil.deleteDirectory(f);
                }
            }
        }

        // delete latest backup
        IOUtil.deleteDirectory(new File(TMP_BASE, "" + maximumNumberOfBackups));

        for (int i = maximumNumberOfBackups - 1; i >= 0; i--) {
            new File(TMP_BASE, "" + i).renameTo(new File(TMP_BASE, "" + (i + 1)));
        }

        TMP.mkdir();
    }

    public File getTmpFolder() {
        return TMP;
    }

    public File getConfigFolder() {
        return CONFIG_FOLDER;
    }

    public File getPluginUpdatesFolder() {
        return PLUGIN_UPDATES;
    }

    public File getUpdatesFolder() {
        return UPDATES;
    }

    public File getResourcesFolder() {
        return RESOURCES;
    }

    public File getProjectTemplatesFolder() {
        return PROJECT_TEMPLATES;
    }

    public File getPropertyFolder() {
        return PROPERTY_FOLDER;
    }

    public void setPropertyFolder(File propertyFolder) {
        PROPERTY_FOLDER = propertyFolder;
    }

    public File getPropertyFolderTemplate() {
        return PROPERTY_FOLDER_TEMPLATE;
    }

    public File getEtcFolder() {
        return ETC;
    }

//    public static void main(String[] args) {
//        VPropertyFolderManager.init();
//
//        System.out.println(VSysUtil.getPID());
//
//        File[] roots = File.listRoots();
//        for (int i = 0; i < roots.length; i++) {
//            System.out.println("Root[" + i + "]:" + roots[i].getAbsolutePath());
//        }
//    }
    public File toLocalPathInTmpFolder(File f) {

        File result = new File(getTmpFolder(), toLocalPath(f).getPath());

        return result;
    }

    public File toLocalPath(File f) {
        
        VParamUtil.throwIfNull(f);
        
        String path = f.getAbsolutePath();

        // if on windows, replace drive letter,
        // e.g., c:\test123 becomes drive_c\test123
        if (VSysUtil.isWindows()) {
            for (File drive : File.listRoots()) {
                String s = drive.getAbsolutePath();
                if (path.startsWith(s)) {
                    path = "Drive_" + s.substring(0, 1) + "\\"
                            + path.substring(2);
                }
            }
        }// 
        // on unix it's much easier. we just rempve the first /. Unix only has
        // one file system root
        else if (VSysUtil.isLinux() || VSysUtil.isMacOSX()) {
            if (path.startsWith("/")) {
                path = path.substring(1);
            }
        }

        return new File(path);

    }

    public void evalueteArgs(String[] args) {
        String template = null;
        String folder = null;
        String subfolder = "default";

        System.out.println(">> Property Folder Options:");

        if (template != null) {
            System.out.println(
                    " --> using property folder template, path: " + template);
            PROPERTY_FOLDER_TEMPLATE = new File(template);
        }

        boolean ignoreSuffix = false;

        if (folder != null && subfolder != null) {

            System.out.println(
                    "folder: " + folder + ", subfolder: " + subfolder);

            System.out.println(
                    " --> cannot define both, custom folder and suffix."
                    + " Ignoring suffix!");

            ignoreSuffix = true;
        }

        if (folder != null) {
            System.out.println(
                    " --> using custom property folder, path: " + folder);
            init(folder, false);

            return;
        }

        if (!ignoreSuffix) {
            if (subfolder != null) {
                System.out.println(
                        " --> using custom property folder suffix, suffix: "
                        + subfolder);
                init(subfolder, true);
                return;
            }
        }

        System.out.println(
                " --> no folder or suffix specified, using default.");
        init();

    }
//    public static String[] knownArgs() {
//        return new String[]{"-property-folder", "-property-folder-suffix"};
//    }

    public void lockFolder(boolean force) {
        File lockFile = new File(getPropertyFolder(), ".lock");

//        if (!force && isLocked()) {
//            return;
//        }
        try {
            lockFile.createNewFile();
        } catch (IOException ex) {
            //
            System.out.println(" --> cannot lock property folder (does not exist).");
        }

        SynchronizedFileAccess.lockFile(lockFile);
    }

    public boolean isLocked() {
        File lockFile = new File(getPropertyFolder(), ".lock");
//        return new File(getPropertyFolder(), ".locked").exists();

        return SynchronizedFileAccess.isLocked(lockFile);
    }

    public void unlockFolder() {
        System.out.println(" --> unlocking property folder"
                + " (action will be performed on shutdown).");
//        File lockFile = new File(getPropertyFolder(), ".locked");
//
//        IOUtil.deleteDirectory(lockFile);
//
//        if (lockFile.exists()) {
//            IOUtil.deleteTmpFilesOnExitIgnoreFileLocks(lockFile);
//        }
    }

    /**
     * @return the initialized
     */
    public boolean isInitialized() {
        return initialized;
    }
}
