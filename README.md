VGitArchive
===========

[![Build Status](https://travis-ci.org/miho/VGitArchive.svg?branch=master)](https://travis-ci.org/miho/VGitArchive)

Library to work with versioned archive files (zip archive with internal Git repository). Use this library to add versioning support without needing additional repositories or external command line tools.

## How to Build VGitArchive

### Requirements

- Java >= 1.7
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)

### IDE

Open the `VGitArchive` [Gradle](http://www.gradle.org/) project in your favourite IDE (tested with NetBeans 7.4) and build it
by calling the `assemble` task.

### Command Line

Navigate to the [Gradle](http://www.gradle.org/) project (e.g., `path/to/VGitArchive`) and enter the following command

#### Bash (Linux/OS X/Cygwin/other Unix-like shell)

    sh gradlew assemble
    
#### Windows (CMD)

    gradlew assemble

## Code Sample:

```java
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
```
