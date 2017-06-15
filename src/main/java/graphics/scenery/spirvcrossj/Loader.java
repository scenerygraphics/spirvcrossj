package graphics.scenery.spirvcrossj;

import java.io.*;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;

/**
 * Created by ulrik on 11/14/2016.
 */
public class Loader {

    static boolean nativesReady = false;

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
                if (file.isDirectory() && file.getName().contains("spirvcrossj-natives-tmp")) {
                    File lock = new File(file, ".lock");

                    // delete the temporary directory only if the lock does not exist
                    if (!lock.exists()) {
                        Files.walk(file.toPath())
                                .map(Path::toFile)
                                .sorted((f1, f2) -> -f1.compareTo(f2))
                                .forEach(File::delete);
                    }
                }
            }
        } catch(NullPointerException | IOException e) {
            System.err.println("Unable to delete leftover temporary directories: " + e);
            e.printStackTrace();
        }
    }

    public static void loadNatives() throws IOException {
        if(nativesReady) {
            return;
        }

        String lp = System.getProperty("java.library.path");
        File tmpDir = Files.createTempDirectory("spirvcrossj-natives-tmp").toFile();

        File lock = new File(tmpDir, ".lock");
        lock.createNewFile();
        lock.deleteOnExit();

        cleanTempFiles();

        String libraryName;
        String classifier;

        switch(getPlatform()) {
            case WINDOWS:
                libraryName = "spirvcrossj.dll";
                classifier = "natives-windows";
                break;
            case LINUX:
                libraryName = "libspirvcrossj.so";
                classifier = "natives-linux";
                break;
            case MACOS:
                libraryName = "libspirvcrossj.jnilib";
                classifier = "natives-macos";
                break;
            default:
                System.err.println("spirvcrossj is not supported on this platform.");
                classifier = "none";
                libraryName = "none";
        }

        String[] jars;

        // FIXME: This incredibly ugly workaround here is needed due to the way ImageJ handles it's classpath
        // Maybe there's a better way?
        if(System.getProperty("java.class.path").toLowerCase().contains("imagej-launcher")) {
            URL res = Thread.currentThread().getContextClassLoader().getResource(libraryName);
            if(res == null && getPlatform() == Platform.MACOS) {
                res = Thread.currentThread().getContextClassLoader().getResource("libspirvcrossj.dylib");
            }

            if(res == null) {
                System.err.println("ERROR: Could not find spirvcrossj libraries.");
                return;
            }

            String jar = res.getPath();
            jar = jar.substring(jar.indexOf("file:/") + 6);
            jar = jar.substring(0, jar.indexOf("!") - 4) + "-" + classifier + ".jar";
            jars = jar.split(File.pathSeparator);
        } else {
            jars = System.getProperty("java.class.path").split(File.pathSeparator);
        }

        for(int i = 0; i < jars.length; i ++) {
            String s = jars[i];

            if(!(s.contains("spirvcrossj") && s.contains("natives"))) {
                continue;
            }

            try {
                JarFile jar = new JarFile(s);
                Enumeration<JarEntry> enumEntries = jar.entries();

                while (enumEntries.hasMoreElements()) {
                    JarEntry entry = enumEntries.nextElement();

                    // only extract library files
                    String extension = entry.getName().substring(entry.getName().lastIndexOf('.') + 1);
                    if (!(extension.startsWith("so") || extension.startsWith("dll") || extension.startsWith("dylib") || extension.startsWith("jnilib"))) {
                        continue;
                    }

                    File f = new File(tmpDir.getAbsolutePath() + File.separator + entry.getName());

                    if (entry.isDirectory()) {
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
            } catch (IOException e) {
                System.err.println("Failed to extract native libraries: " + e.getMessage());
                e.printStackTrace();
            }
        }

        lp = System.getProperty("java.library.path");
        System.setProperty("java.library.path", lp + File.pathSeparator + new java.io.File( "." ).getCanonicalPath() + File.separator + "src" + File.separator + "natives");

        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed to set java.library.path: " + e.getMessage());
            e.printStackTrace();
        }

        try {
            System.loadLibrary("spirvcrossj");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Unable to load native library: " + e.getMessage());
            String osname = System.getProperty("os.name");
            String osclass = osname.substring(0, osname.indexOf(' ')).toLowerCase();

            System.err.println("Did you include spirvcrossj-natives-" + osclass + " in your dependencies?");
        }

        nativesReady = true;
    }
}
