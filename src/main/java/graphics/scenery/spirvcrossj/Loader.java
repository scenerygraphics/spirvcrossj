package graphics.scenery.spirvcrossj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Created by ulrik on 11/14/2016.
 */
public class Loader {

    static boolean nativesReady = false;
    static final String projectName = "spirvcrossj";
    static final String libraryNameWindows = "spirvcrossj.dll";
    static final String libraryNameLinux = "libspirvcrossj.so";
    static final String libraryNameMacOS = "libspirvcrossj.jnilib";
    static final Logger logger = LoggerFactory.getLogger("Loader(" + projectName + ")");

    enum Platform { UNKNOWN, WINDOWS, LINUX, MACOS }

    public static Platform getPlatform() {
        final String os = System.getProperty("os.name").toLowerCase();

        if(os.contains("win")) {
            return Platform.WINDOWS;
        } else if(os.contains("linux")) {
            return Platform.LINUX;
        } else if(os.contains("mac")) {
            return Platform.MACOS;
        }

        return Platform.UNKNOWN;
    }

    public static void cleanTempFiles() {
        try {
            File[] files = new File(System.getProperty("java.io.tmpdir")).listFiles();

            for (File file : files) {
                if (file.isDirectory() && file.getName().contains(projectName + "-natives-tmp")) {
                    File lock = new File(file, ".lock");

                    // delete the temporary directory only if the lock does not exist
                    if (!lock.exists()) {
                        Files.walk(file.toPath())
                                .map(Path::toFile)
                                .sorted((f1, f2) -> -f1.compareTo(f2))
                                .forEach(File::delete);
                        logger.debug("Deleted leftover temp directory for " + projectName + " at " + file);
                    }
                }
            }
        } catch(NullPointerException | IOException e) {
            logger.error("Unable to delete leftover temporary directories: " + e);
            e.printStackTrace();
        }
    }

    public static void loadNatives() throws IOException {
        if(nativesReady) {
            return;
        }

        String lp = System.getProperty("java.library.path");
        File tmpDir = Files.createTempDirectory(projectName + "-natives-tmp").toFile();

        File lock = new File(tmpDir, ".lock");
        lock.createNewFile();
        lock.deleteOnExit();

        cleanTempFiles();

        String libraryName;
        String classifier;

        switch(getPlatform()) {
            case WINDOWS:
                libraryName = libraryNameWindows;
                classifier = "natives-windows";
                break;
            case LINUX:
                libraryName = libraryNameLinux;
                classifier = "natives-linux";
                break;
            case MACOS:
                libraryName = libraryNameMacOS;
                classifier = "natives-macos";
                break;
            default:
                logger.error(projectName + " is not supported on this platform.");
                classifier = "none";
                libraryName = "none";
        }

        String[] jars;
        logger.debug("Looking for library " + libraryName);

        if(System.getProperty("java.class.path").toLowerCase().contains("imagej-launcher")
                || Boolean.parseBoolean(System.getProperty(projectName + ".useContextClassLoader", "true"))) {
            logger.debug("Using context class loader");
            Enumeration<URL> res = Thread.currentThread().getContextClassLoader().getResources(libraryName);
            if(!res.hasMoreElements() && getPlatform() == Platform.MACOS) {
                res = Thread.currentThread().getContextClassLoader().getResources(libraryNameMacOS.substring(0, libraryNameMacOS.indexOf(".")) + ".dylib");
            }

            if(!res.hasMoreElements()) {
                logger.warn("Could not find " + projectName + " libraries using context class loader, falling back to manual method.");
                jars = System.getProperty("java.class.path").split(File.pathSeparator);
            } else {

                String jar = "";
                while (res.hasMoreElements()) {
                    String p = res.nextElement().getPath();
                    if (p.contains("-natives-")) {
                        jar = p;
                        break;
                    }
                }

                if (jar.length() == 0) {
                    logger.error("Could not find " + projectName + " libraries, no matching JARs detected.");
                    return;
                }

                // on Windows, file URLs are stated as file:///, on OSX and Linux only as file:/
                int pathOffset = 5;

                if (getPlatform() == Platform.WINDOWS) {
                    pathOffset = 6;
                }

                jar = jar.substring(jar.indexOf("file:/") + pathOffset);

                if (jar.contains(classifier)) {
                    jar = jar.substring(0, jar.indexOf("!"));
                } else {
                    jar = jar.substring(0, jar.indexOf("!") - 4) + "-" + classifier + ".jar";
                }

                jars = jar.split(File.pathSeparator);
            }
        } else {
            logger.debug("Not using context class loader");
            jars = System.getProperty("java.class.path").split(File.pathSeparator);
        }

        for (String s : jars) {
            if (!(s.contains(projectName) && s.contains("natives"))) {
                continue;
            }

            try {
                logger.debug("Extracting libraries from " + s);
                JarFile jar = new JarFile(s);
                Enumeration<JarEntry> enumEntries = jar.entries();

                while (enumEntries.hasMoreElements()) {
                    JarEntry entry = enumEntries.nextElement();

                    // only extract library files
                    String extension = entry.getName().substring(entry.getName().lastIndexOf('.') + 1);
                    if (!(extension.startsWith("so") || extension.startsWith("dll") || extension.startsWith("dylib") || extension.startsWith("jnilib")) && !entry.isDirectory()) {
                        logger.debug("SKIPPED file " + entry.getName());
                        continue;
                    }

                    File f = new File(tmpDir.getAbsolutePath() + File.separator + entry.getName());
                    logger.debug("Reading and extracting " + entry.getName() + " to " + f.toString());

                    if (entry.isDirectory()) {
                        logger.debug("Creating new directory");
                        f.mkdir();
                        continue;
                    }

                    InputStream ins = jar.getInputStream(entry);
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    FileOutputStream fos = new FileOutputStream(f);

                    byte[] buffer = new byte[1024];
                    int len;

                    while ((len = ins.read(buffer)) > -1) {
                        baos.write(buffer, 0, len);
                    }

                    baos.flush();
                    fos.write(baos.toByteArray());

                    fos.close();
                    baos.close();
                    ins.close();
                }

                System.setProperty("java.library.path", lp + File.pathSeparator + tmpDir.getCanonicalPath());
            } catch (IOException /*| URISyntaxException*/ e) {
                logger.error("Failed to extract native libraries: " + e.getMessage());
                e.printStackTrace();
            }
        }

        String libraryPath = new java.io.File( "." ).getCanonicalPath()
                + File.separator + "target"
                + File.separator + "classes"
                + File.separator + libraryName;

        // we try local path first, in case we're running on the CI
        if(!new File(libraryPath).exists()) {
            libraryPath = tmpDir + File.separator + libraryName;
        }

        try {
            System.load(libraryPath);
        } catch (UnsatisfiedLinkError e) {
            logger.error("Unable to load native library: " + e.getMessage());
            logger.error("Did you include " + projectName + "-" + classifier + " in your dependencies?");
        }

        logger.debug("Successfully loaded native library for " + projectName + " from " + libraryPath);
        nativesReady = true;
    }
}
