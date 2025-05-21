package talend.modifier;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class FileHelper {

    private static List<File> getFilesMatching(File dir, Predicate<File> matcher) {
        List<File> result = new ArrayList<>();
        if (dir == null || !dir.exists() || !dir.isDirectory()) {
            return result;
        }

        File[] files = dir.listFiles();
        if (files == null) return result;

        for (File file : files) {
            if (file.isDirectory()) {
                result.addAll(getFilesMatching(file, matcher));
            } else if (matcher.test(file)) {
                result.add(file);
            }
        }

        return result;
    }

    public static Optional<File> findFileInDirectory(File dir, String targetName) {
        return getFilesMatching(dir, file -> file.getName().equals(targetName))
            .stream()
            .findFirst();
    }

    public static List<File> findFilesContainingNamePart(File dir, String namePart) {
        return getFilesMatching(dir, file -> file.getName().contains(namePart));
    }

    public static Optional<File> resolveSubdirectoryUpwards(File startFile, String targetSubdirectoryName) {
        if (startFile == null || !startFile.exists()) {
            return Optional.empty();
        }

        File current = startFile.isDirectory() ? startFile : startFile.getParentFile();

        while (current != null) {
            File candidate = new File(current, targetSubdirectoryName);
            if (candidate.isDirectory()) {
                return Optional.of(candidate);
            }
            current = current.getParentFile();
        }

        return Optional.empty();
    }

    public static void saveDocument(Document doc, String filePath) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }

    public static Document loadDocument(String jobPath) throws Exception {
        return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new File(jobPath));
    }

}
