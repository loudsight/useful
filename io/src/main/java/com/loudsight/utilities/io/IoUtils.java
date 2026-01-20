package com.loudsight.utilities.io;

import com.loudsight.useful.helper.logging.LoggingHelper;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

import static java.nio.file.FileVisitResult.CONTINUE;
import static java.nio.file.FileVisitResult.TERMINATE;

public class IoUtils {
    private static final LoggingHelper logger = LoggingHelper.wrap(MethodHandles.lookup().lookupClass());

    public static String readFile(File file, String csName) throws IOException {
        final Charset cs = Charset.forName(csName);
        return readFile(file.toPath(), cs);
    }

    public static String readFile(Path file, String csName) throws IOException {
        final Charset cs = Charset.forName(csName);

        return readFile(file, cs);
    }

    public static String readFile(Path file, Charset cs) throws IOException {
        try (final FileInputStream stream = new FileInputStream(file.toFile())) {
            return new String(stream.readAllBytes(), cs);
        }
    }


    /**
     * https://stackoverflow.com/questions/11651900/how-to-recursively-copy-entire-directory-including-parent-folder-in-java
     *
     * @param source - file/directory to copy from
     * @param dest - file/directory to copy to
     * @param options - copy mode
     * @throws IOException - thrown by underlying nio calls
     */
    public static void copyFileOrFolder(File source, File dest, CopyOption... options) throws IOException {
        if (source.isDirectory())
            copyDirectory(source, dest, options);
        else {
            isParentDirectory(dest);
            copyFile(source, dest, options);
        }
    }
    public static void copyFileOrFolder(String source, String dest, CopyOption... options) throws IOException {
        copyFileOrFolder(new File(source), new File(dest), options);
    }

    /**
     * <a href="https://stackoverflow.com/questions/11651900/how-to-recursively-copy-entire-directory-including-parent-folder-in-java">...</a>
     *
     * @param source - directory to copy from
     * @param dest - directory to copy to
     * @param options - copy mode
     * @throws IOException - thrown by underlying nio calls
     */
    private static void copyDirectory(File source, File dest, CopyOption... options) throws IOException {
        if (!dest.exists())
            dest.mkdirs();
        File[] contents = source.listFiles();
        if (contents != null) {
            for (File f : contents) {
                File newFile = new File(dest.getAbsolutePath() + File.separator + f.getName());
                if (f.isDirectory())
                    copyDirectory(f, newFile, options);
                else
                    copyFile(f, newFile, options);
            }
        }
    }

    public static void deleteFileOrFolder(final Path path) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<Path>(){
            @Override public FileVisitResult visitFile(final Path file, final BasicFileAttributes attrs)
                    throws IOException {
                Files.delete(file);
                return CONTINUE;
            }

            @Override public FileVisitResult visitFileFailed(final Path file, final IOException e) {
                return handleException(e);
            }

            private FileVisitResult handleException(final IOException e) {
                logger.logError("Unexpected error", e); // replace with more robust error handling
                return TERMINATE;
            }

            @Override public FileVisitResult postVisitDirectory(final Path dir, final IOException e)
                    throws IOException {
                if(e!=null)return handleException(e);
                Files.delete(dir);
                return CONTINUE;
            }
        });
    }


    private static void copyFile(File source, File dest, CopyOption... options) throws IOException {
        Files.copy(source.toPath(), dest.toPath(), options);
    }

    private static void isParentDirectory(File file) {
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
    }
}
