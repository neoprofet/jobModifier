package talend.modifier;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.Optional;

public class FileHelper {

    public static Optional<File> findFileInDirectory(File dir, String targetName) {
        if (dir == null || !dir.exists()) {
            return Optional.empty();
        }

        File[] files = dir.listFiles();
        if (files == null) {
            return Optional.empty();
        }

        for (File file : files) {
            if (file.isDirectory()) {
                Optional<File> result = findFileInDirectory(file, targetName);
                if (result.isPresent()) {
                    return result;
                }
            } else if (file.getName().equals(targetName)) {
                return Optional.of(file);
            }
        }
        return Optional.empty();
    }

    public static Optional<File> resolveSubdirectoryUpwards(File startFile, String targetSubdirectoryName) {
        if (startFile == null || !startFile.exists()) {
            return Optional.empty();
        }

        File current = startFile;
        while (current != null) {
            File[] files = current.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isDirectory() && file.getName().equals(targetSubdirectoryName)) {
                        return Optional.of(file);
                    }
                }
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
