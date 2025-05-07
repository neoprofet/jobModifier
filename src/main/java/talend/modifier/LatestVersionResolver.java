package talend.modifier;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.*;

/**
 * Utility class for resolving the latest version of a job in the given directory.
 */
public class LatestVersionResolver {

    /**
     * Finds the latest version of a job based on the given directory and job name.
     *
     * @param dir     the directory where job files are located
     * @param jobName the name of the job to search for
     * @return an Optional containing the latest version as a string, or an empty Optional if not found
     * @throws IOException if an I/O error occurs while reading files
     */
    public static Optional<String> findLatestVersion(File dir, String jobName) throws IOException {

        if (!dir.exists() || !dir.isDirectory()) {
            return Optional.empty();
        }

        /* 1. Walk through the directory structure to find all files.
         * 2. Filter only regular files (skip directories).
         * 3. Get the file name from the path and convert to a string.
         * 4. Filter filenames that match the pattern "jobName_version.item".
         * 5. Remove the job name and file extension to extract the version part.
         * 6. Parse the version string into a list of integers.
         * 7. Find the maximum version using a custom comparator.
         * 8. If a version is found, format it back to a string.
         * */
        try (Stream<Path> allFiles = Files.walk(dir.toPath())) {
            return allFiles
                    .filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.matches(Pattern.quote(jobName) + "_\\d+\\.\\d+\\.item"))
                    .map(name -> name.replaceFirst(Pattern.quote(jobName) + "_", "").replace(".item", ""))
                    .map(LatestVersionResolver::parseVersion)
                    .max(LatestVersionResolver::compareVersions)
                    .map(LatestVersionResolver::formatVersion);
        }
    }

    /**
     * Parses the version string into a list of integers.
     * Example: Input: "1.2", Output: [1, 2]
     *
     * @param version the version string
     * @return a list of integers representing the version parts
     */
    private static List<Integer> parseVersion(String version) {
        return Arrays.stream(version.split("\\."))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    /**
     * Compares two version lists (Comparator).
     *
     * @param v1 the first version list
     * @param v2 the second version list
     * @return a negative value if v1 is less than v2, positive if greater, or zero if equal
     */
    private static int compareVersions(List<Integer> v1, List<Integer> v2) {
        for (int i = 0; i < Math.min(v1.size(), v2.size()); i++) {
            int cmp = Integer.compare(v1.get(i), v2.get(i));
            if (cmp != 0) return cmp;
        }
        return Integer.compare(v1.size(), v2.size());
    }

    /**
     * Formats the version parts as a dot-separated string.
     * Example: Input: [1, 2], Output: "1.2"
     *
     * @param versionParts the list of version parts
     * @return the formatted version string
     */
    private static String formatVersion(List<Integer> versionParts) {
        return versionParts.stream()
                .map(String::valueOf)
                .collect(Collectors.joining("."));
    }
}
