package graphics.scenery.spirvcrossj;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

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

        String manualPath = System.getProperty("spirvcrossj.LibraryPath");
        String lp = System.getProperty("java.library.path");

        final File tmpDir;
        if(manualPath == null) {
            tmpDir = Files.createTempDirectory(projectName + "-natives-tmp").toFile();
        } else {
            tmpDir = new File(manualPath);
        }

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

        InputStream libraryStream = null;
        logger.debug("Looking for library " + libraryName);

        boolean localLibraryFound = false;

        if(System.getProperty("java.class.path").toLowerCase().contains("imagej-launcher")
                || Boolean.parseBoolean(System.getProperty(projectName + ".useContextClassLoader", "true"))) {
            logger.debug("Using context class loader");
            Enumeration<URL> res = Thread.currentThread().getContextClassLoader().getResources(libraryName);
            if(!res.hasMoreElements() && getPlatform() == Platform.MACOS) {
                res = Thread.currentThread().getContextClassLoader().getResources(libraryNameMacOS.substring(0, libraryNameMacOS.indexOf(".")) + ".dylib");
            }

            if(!res.hasMoreElements()) {
                throw new IllegalStateException("Could not find " + projectName + " libraries using context class loader.");
            } else {
                while (res.hasMoreElements()) {
                    final URL resource = res.nextElement();
                    String p = resource.getPath();
                    logger.debug("Found match at " + p);

                    // result lives in a JAR
                    if (p.contains(".jar!")) {
                        libraryStream = resource.openStream();
                        break;
                    }

                    // result lives in a build directory and not in a JAR, probably part of a CI build
                    if (!p.contains(".jar!") && p.endsWith(libraryName) && p.contains("target") && p.contains("classes")) {
                        logger.debug("Found local library, probably running as CI build.");
                        localLibraryFound = true;
                        libraryStream = resource.openStream();
                        break;
                    }
                }
            }
        } else {
            throw new UnsupportedEncodingException("Extracting natives without using the context class loader is not supported anymore.");
        }

        if(libraryStream == null) {
            throw new IllegalStateException("Could not locate library " + libraryName);
        }

        try {
            logger.debug("Extracting libraries from " + libraryStream);
            File f = new File(tmpDir.getAbsolutePath() + File.separator + libraryName);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            FileOutputStream fos = new FileOutputStream(f);

            byte[] buffer = new byte[1024];
            int len;

            while ((len = libraryStream.read(buffer)) > -1) {
                baos.write(buffer, 0, len);
            }

            baos.flush();
            fos.write(baos.toByteArray());

            fos.close();
            baos.close();
            libraryStream.close();

            System.setProperty("java.library.path", lp + File.pathSeparator + tmpDir.getCanonicalPath());
        } catch (IOException /*| URISyntaxException*/ e) {
            logger.error("Failed to extract native libraries: " + e.getMessage());
            e.printStackTrace();
        }

        String libraryPath = new java.io.File( "." ).getCanonicalPath()
                + File.separator + "target"
                + File.separator + "classes"
                + File.separator + libraryName;

        // we try local path first, in case we're running on the CI
        if(!localLibraryFound && !new File(libraryPath).exists()) {
            logger.debug("Not using local library.");
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
