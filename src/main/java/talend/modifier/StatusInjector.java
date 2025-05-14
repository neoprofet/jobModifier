package talend.modifier;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StatusInjector {

    public static final String DEFAULT_T_REST_RESPONSE_UNIQUE_NAME = "__tRESTResponce_status__";
    public static final String DEFAULT_T_JAVA_ROW_UNIQUE_NAME = "__tJavaRow_status__";
    public static final String DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME = "__status__";

    public static void injectStatusToJob(String jobPath) {
        try {
            DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = dBuilder.parse(new File(jobPath));
            doc.getDocumentElement().normalize();

            if (hasTRestRequestExists(doc)) {
                if (!hasTRestResponseStatusExists(doc)) {
                    System.out.println("Processing file: " + jobPath);

                    CreateAndGetElements.createNewTRestResponseComponent(
                            doc,
                            DEFAULT_T_REST_RESPONSE_UNIQUE_NAME,
                            "String",
                            "OK (200)"
                    );
                    System.out.println(DEFAULT_T_REST_RESPONSE_UNIQUE_NAME + " created");

                    if (!hasTJavaRowStatusExists(doc)) {
                        CreateAndGetElements.createNewTJavaRowComponent(doc,
                                DEFAULT_T_JAVA_ROW_UNIQUE_NAME,
                                ExternalCodeFabric.getNewCodeToInsertToTJavaRowStatus()
                        );
                    }
                    System.out.println(DEFAULT_T_JAVA_ROW_UNIQUE_NAME + " created");

                    //Connection between tJavaRow and tRestResponse
                    CreateAndGetElements.createNewMainConnectionElementWithSchema(doc,
                            "mainConnection " + DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME,
                            DEFAULT_T_JAVA_ROW_UNIQUE_NAME, DEFAULT_T_REST_RESPONSE_UNIQUE_NAME,
                            "row " + DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME,
                            "body"
                    );

                    CreateAndGetElements.addOutputFlowToTRestRequest(doc,
                            DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME,
                            "GET",
                            "/" + DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME,
                            "NONE",
                            "JSON");

                    String tRestRequestName = CreateAndGetElements.getUniqueComponentName(doc,
                            "tRESTRequest").get();

                    CreateAndGetElements.createNewMainConnectionElementWithoutSchema(doc,
                            DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME,
                            tRestRequestName,
                            DEFAULT_T_JAVA_ROW_UNIQUE_NAME
                    );
                }
            } else {
                System.out.println("tRestRequest component not found");
            }
            saveDocument(doc, jobPath);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void saveDocument(Document doc, String filePath) throws Exception {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        DOMSource source = new DOMSource(doc);
        StreamResult result = new StreamResult(new File(filePath));
        transformer.transform(source, result);
    }

    private static List<String> getAllJobItemFilePathsByRouteItemPath(String routeItemPath) throws IOException {
        File routeFile = new File(routeItemPath).getCanonicalFile();
        File processDir = FileHelper.resolveSubdirectoryUpwards(routeFile, "process");

        if (processDir != null && processDir.isDirectory()) {
            try (Stream<Path> allFiles = Files.walk(processDir.toPath())) {
                return allFiles
                        .filter(Files::isRegularFile)
                        .filter(path -> path.toString().endsWith(".item"))
                        .map(Path::toString)
                        .collect(Collectors.toList());
            }
        }
        return Collections.emptyList();
    }

    private static boolean hasTRestRequestExists(Document doc) {
        NodeList nodes = doc.getElementsByTagName("node");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if ("tRESTRequest".equals(node.getAttribute("componentName"))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTRestResponseStatusExists(Document doc) {
        NodeList nodes = doc.getElementsByTagName("node");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (DEFAULT_T_REST_RESPONSE_UNIQUE_NAME.equals(node.getAttribute("UNIQUE_NAME"))) {
                return true;
            }
        }
        return false;
    }

    private static boolean hasTJavaRowStatusExists(Document doc) {
        NodeList nodes = doc.getElementsByTagName("node");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (DEFAULT_T_JAVA_ROW_UNIQUE_NAME.equals(node.getAttribute("UNIQUE_NAME"))) {
                return true;
            }
        }
        return false;
    }
}
