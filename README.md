VGitArchive
===========

[![Build Status](https://travis-ci.org/miho/VGitArchive.svg?branch=master)](https://travis-ci.org/miho/VGitArchive)


```java
public class Main {

    public static void main(String[] args) {
        try {
            VersionedFile.setTmpFolder(Paths.get("tmp"));
            
            // create and open the file
            VersionedFile f
                    = new VersionedFile(new File("project.vfile")).create().open();

            // prepare writing to a text file
            BufferedWriter writer = new BufferedWriter(
                    new FileWriter(f.getContent().getPath() + "/file1.txt"));

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

            // finish writing
            writer.close();

            // checkout latest/newest version
            f.checkoutLatestVersion();

            // checkout previous versions one by one
            while (f.hasPreviousVersion()) {
                System.out.println("-> press enter to checkout the previous version");
                System.in.read(); // waiting for user input
                f.checkoutPreviousVersion();
            }

            // finally, close the file
            f.close();

        } catch (IOException ex) {
            ex.printStackTrace(System.out);
        }
    }
}
```
