package talend.modifier;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Utility class for performing file-related operations such as recursive file search
 * and upward directory resolution.
 */
public class FileHelper {

    /**
     * Recursively searches for a file with the specified name starting from the given directory.
     *
     * <p>The method traverses all subdirectories of the specified root directory to find the first occurrence
     * of a file matching the target name. If no such file is found, {@link Optional#empty()} is returned.
     *
     * @param dir the root directory from which to begin the search
     * @param targetName the name of the file to search for
     * @return an {@link Optional} containing the matching {@link File}, or {@link Optional#empty()} if not found
     */

    public static Optional<File> findFileRecursively(File dir, String targetName) {
        File[] files = dir.listFiles();
        if (files == null) return Optional.empty();

        for (File file : files) {
            if (file.isDirectory()) {
                Optional<File> result = findFileRecursively(file, targetName);
                if (result.isPresent()) return result;
            } else if (file.getName().equals(targetName)) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }

    /**
     * Resolves a subdirectory by moving upwards from the given start file until
     * the target subdirectory is found.
     *
     * @param startFile                the file or directory to start searching from
     * @param targetSubdirectoryName   the name of the subdirectory to search for
     * @return the resolved subdirectory as a File object
     * @throws IOException if the subdirectory cannot be found or the resolved directory does not exist
     *
     */
    public static File resolveSubdirectoryUpwards(File startFile, String targetSubdirectoryName) throws IOException {
        File current = startFile.getCanonicalFile().getParentFile();

        while (current != null) {
            File target = new File(current, targetSubdirectoryName);
            if (target.exists()) {
                return target;
            }
            current = current.getParentFile();
        }

        throw new IOException("Directory '" + targetSubdirectoryName + "' not found upwards from: " + startFile.getAbsolutePath());
    }

}
