package talend.modifier;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class LoggerInjector {
    public static final String DEFAULT_TJAVA_UNIQUE_NAME = "__logconfig__";

    /**
     * Injects a specified block of code into all job items referenced by a given route.
     *
     * <p>Finds all cTalendJob references within the route located at {@code routeItemPath},
     * builds paths to each job, and injects {@code newCode} into them.
     *
     * @param routeItemPath the file system path to the .item file of the route
     * @param newCode       the code block to inject into each referenced job
     */
    public static void injectLoggerCodeToAllJobsOfRoute(String routeItemPath, String newCode) {
        try {
            Map<String, String> jobNamesAndVersions = getJobNamesAndVersionsByRouteItemPath(routeItemPath);

            if (jobNamesAndVersions.isEmpty()) {
                System.out.println("No cTalendJob references found in route.");
            }

            for (Map.Entry<String, String> entry : jobNamesAndVersions.entrySet()) {
                String jobName = entry.getKey();
                String jobVersion = entry.getValue();
                String jobPath = buildJobFilePathByRouteItemPath(routeItemPath, jobName, jobVersion);

                System.out.println("Injecting into: " + jobPath);
                processJobItemFile(jobPath, newCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Parses a route .item XML file and extracts all referenced Talend jobs with their versions.
     *
     * <p>Searches for nodes with component name "cTalendJob" and collects the job name and version
     * from their parameters.
     *
     * @param routeItemPath the path to the route's .item XML file
     * @return a map where the key is the job name and the value is the job version
     * @throws Exception if XML parsing fails or the file is not found
     */
    private static Map<String, String> getJobNamesAndVersionsByRouteItemPath(String routeItemPath) throws Exception {
        File xmlFile = new File(routeItemPath);
        if (!xmlFile.exists()) {
            throw new IllegalArgumentException("Route file not found: " + routeItemPath);
        }

        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        NodeList nodeList = doc.getElementsByTagName("node");
        Map<String, String> jobNamesAndVersions = new HashMap<>();

        for (int i = 0; i < nodeList.getLength(); i++) {
            Element node = (Element) nodeList.item(i);
            if ("cTalendJob".equals(node.getAttribute("componentName"))) {
                NodeList params = node.getElementsByTagName("elementParameter");
                String jobName = "";
                String jobVersion = "";

                for (int j = 0; j < params.getLength(); j++) {
                    Element param = (Element) params.item(j);
                    if ("SELECTED_JOB_NAME".equals(param.getAttribute("name"))) {
                        jobName = param.getAttribute("value");
                    } else if ("SELECTED_JOB_NAME:PROCESS_TYPE_VERSION".equals(param.getAttribute("name"))) {
                        jobVersion = param.getAttribute("value");
                    }
                }

                if ("Latest".equalsIgnoreCase(jobVersion)) {
                    Optional<String> latestItem = LatestVersionResolver.findLatestVersion(
                        FileHelper.resolveSubdirectoryUpwards(new File(routeItemPath).getCanonicalFile(), "process"),
                        jobName);
                    if (latestItem.isPresent()) {
                        jobVersion = latestItem.get();
                    } else {
                        throw new IOException("Job version not resolved");
                    }
                }

                if (!jobName.isBlank() && !jobVersion.isBlank()) {
                    jobNamesAndVersions.put(jobName, jobVersion);
                }
            }
        }
        return jobNamesAndVersions;
    }

    /**
     * Constructs the absolute path to a Talend job .item file based on the route file location,
     * job name, and version.
     *
     * <p>Traverses upward from the given route path to locate the project root folder
     * that contains a "routes" directory. Then searches recursively in the "process" directory
     * for the job .item file corresponding to the specified name and version.
     *
     * @param routeItemPath the path to the route .item file
     * @param jobName       the name of the job to locate
     * @param version       the version of the job
     * @return the absolute path to the job's .item file
     * @throws IOException if the project folder or the job file cannot be found
     */


    private static String buildJobFilePathByRouteItemPath(String routeItemPath, String jobName, String version) throws IOException {
        File routeFile = new File(routeItemPath).getCanonicalFile();
        File processDir = FileHelper.resolveSubdirectoryUpwards(routeFile, "process");

        String targetName = jobName + "_" + version + ".item";
        Optional<File> jobFileOpt = FileHelper.findFileRecursively(processDir, targetName);
        File jobFile;
        if (jobFileOpt.isPresent()) {
            jobFile = jobFileOpt.get();
        } else {
            throw new IOException("Job file not found: " + targetName + " under " + processDir.getAbsolutePath());
        }

        return jobFile.getAbsolutePath();
    }

    /**
     * Ensures that a tPrejob component exists in the given Talend job XML document.
     * <p>
     * If a tPrejob node is already present, this method returns its {@code UNIQUE_NAME} value.
     * If not found, it creates a new tPrejob node, appends it to the document,
     * and returns the {@code UNIQUE_NAME} of the newly created component.
     * <p>
     * If the {@code UNIQUE_NAME} parameter is missing in either case, an exception is thrown.
     *
     * @param doc the XML {@link Document} representing a Talend job
     * @return the {@code UNIQUE_NAME} of the existing or newly created tPrejob component
     * @throws IllegalStateException if the tPrejob component does not contain a {@code UNIQUE_NAME} parameter
     */


    private static String ensurePrejobExistsAndGetPrejobName(Document doc) {
        NodeList nodes = doc.getElementsByTagName("node");

        // Search for an existing tPrejob component
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (!"tPrejob".equals(node.getAttribute("componentName"))) {
                continue;
            }

            // Extract the name of the component
            NodeList params = node.getElementsByTagName("elementParameter");
            for (int j = 0; j < params.getLength(); j++) {
                Element param = (Element) params.item(j);
                if ("UNIQUE_NAME".equals(param.getAttribute("name"))) {
                    return param.getAttribute("value");
                }
            }
        }

        // If not found - create a new tPrejob
        Element root = doc.getDocumentElement();
        Element newPrejob = CreateAndGetElements.getNewTPreJobComponent(doc);
        root.appendChild(newPrejob);
        return CreateAndGetElements.DEFAULT_PREJOB_UNIQUE_NAME;
    }


    /**
     * Ensures that a tJava with custom code exists in the XML.
     *
     * <p>If it does not exist, creates it with the provided code.
     * If it does exist, replaces its CODE content with the new code.
     * <p>
     *
     * @param doc     the XML Document representing a Talend job
     * @param newCode the Java code to inject into the tJava component
     */
    private static void ensureTJavaWithCustomCodeExists(Document doc, String newCode) {
        Element root = doc.getDocumentElement();
        NodeList nodes = doc.getElementsByTagName("node");
        Element tJavaWithCustomCodeNode = null;

        // Searching for a node with UNIQUE_NAME
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);

            // Skip everything except tJava
            if (!"tJava".equals(node.getAttribute("componentName"))) {
                continue;
            }

            NodeList params = node.getElementsByTagName("elementParameter");
            for (int j = 0; j < params.getLength(); j++) {
                Element param = (Element) params.item(j);
                if ("UNIQUE_NAME".equals(param.getAttribute("name"))
                    && DEFAULT_TJAVA_UNIQUE_NAME.equals(param.getAttribute("value"))) {
                    tJavaWithCustomCodeNode = node;
                    break;
                }
            }
            if (tJavaWithCustomCodeNode != null) break;
        }

        // Creating tJava with custom code if it doesn't exist
        if (tJavaWithCustomCodeNode == null) {
            tJavaWithCustomCodeNode = CreateAndGetElements.getNewTJavaComponent(doc, DEFAULT_TJAVA_UNIQUE_NAME, newCode);
            root.appendChild(tJavaWithCustomCodeNode);
        } else {
            // Overwriting code tJava with custom code already exists
            NodeList params = tJavaWithCustomCodeNode.getElementsByTagName("elementParameter");
            for (int i = 0; i < params.getLength(); i++) {
                Element param = (Element) params.item(i);
                if ("CODE".equals(param.getAttribute("name"))) {
                    param.setAttribute("value", newCode);
                }
            }
        }
    }

    /**
     * Ensures there is a connection from tPrejob to tJava with custom code
     *
     * <p>If not reachable, finds the last reachable node in tPrejob chain and
     * adds a new connection (OnComponentOk) with name = OnComponentOkLogger .
     *
     * @param doc the XML Document representing the Talend job
     */

    private static void ensureConnectionFromPrejobToTJavaWithCustomCode(Document doc, String prejobName, String tJavaName) {
        NodeList connections = doc.getElementsByTagName("connection");
        Map<String, List<String>> graph = new HashMap<>();

        // Building the dependency graph
        for (int i = 0; i < connections.getLength(); i++) {
            Element conn = (Element) connections.item(i);
            graph.computeIfAbsent(conn.getAttribute("source"), k -> new ArrayList<>())
                .add(conn.getAttribute("target"));
        }

        // Checking tJava with custom code reachability
        if (!ChainHelper.isNodeReachable(prejobName, tJavaName, graph)) {
            String lastNode = ChainHelper.findLastNodeInChain(prejobName, graph);

            Element connection = CreateAndGetElements.getNewOnComponentOkConnectionElement(doc, "OnComponentOkLogger",
                lastNode, tJavaName);
            Element root = doc.getDocumentElement();
            root.appendChild(connection);
        }
    }

    /**
     * Injects or updates custom Java code into a Talend job .item file.
     * <p>Ensures the presence of tPrejob, tJava with custom code, and proper connection between them.
     *
     * @param jobPath path to the Talend .item file
     * @param newCode Java code to inject into tJava
     * @throws Exception on file or XML processing errors
     */
    private static void processJobItemFile(String jobPath, String newCode) throws Exception {
        File xmlFile = new File(jobPath);

        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        ensureTJavaWithCustomCodeExists(doc, newCode);
        String prejobName = ensurePrejobExistsAndGetPrejobName(doc);
        ensureConnectionFromPrejobToTJavaWithCustomCode(doc, prejobName, DEFAULT_TJAVA_UNIQUE_NAME);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(xmlFile));

        System.out.println("Processing completed for: " + jobPath);
    }
}
