package is.ulrik.spirvcrossj;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.file.Files;
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

    public static void loadNatives() throws IOException {
        String lp = System.getProperty("java.library.path");
        File tmpDir = Files.createTempDirectory("scenery-natives-tmp").toFile();

        List<String> jars = Arrays.stream(System.getProperty("java.class.path").split(File.pathSeparator))
                .filter(s -> s.contains("natives") && s.contains("spirvcrossj"))
                .collect(Collectors.toList());

        jars.stream().forEach(s -> {
            try {
                JarFile jar = new JarFile(s);
                Enumeration<JarEntry> enumEntries = jar.entries();

                while (enumEntries.hasMoreElements()) {
                    JarEntry entry = enumEntries.nextElement();
                    File f = new File(tmpDir.getAbsolutePath() + File.separator + entry.getName());

                    if (entry.isDirectory()) {
                        f.mkdir();
                        continue;
                    }

                    InputStream ins = jar.getInputStream(entry);
                    FileOutputStream fos = new FileOutputStream(f);

                    while (ins.available() > 0) {
                        fos.write(ins.read());
                    }

                    fos.close();
                    ins.close();
                }

                System.setProperty("java.library.path", lp + File.pathSeparator + tmpDir.getAbsolutePath());

                Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
                fieldSysPath.setAccessible(true);
                fieldSysPath.set(null, null);
            } catch (IOException | NoSuchFieldException | IllegalAccessException e) {
                System.err.println("Failed to set java.library.path: " + e.getMessage());
            }
        });

        System.setProperty("java.library.path", lp + File.pathSeparator + new java.io.File( "." ).getCanonicalPath() + File.separator + "src" + File.separator + "natives");

        try {
            Field fieldSysPath = ClassLoader.class.getDeclaredField("sys_paths");
            fieldSysPath.setAccessible(true);
            fieldSysPath.set(null, null);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            System.err.println("Failed to set java.library.path: " + e.getMessage());
        }

        try {
            System.loadLibrary("spirvcrossj");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Unable to load native library: " + e.getMessage());
            System.err.println("Did you include spirvcrossj-natives-" + System.getProperty("os.name") + " in your dependencies?");
        }
    }
}
