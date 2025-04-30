package talend.modifier;

import org.w3c.dom.*;

import javax.xml.parsers.*;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.IOException;
import java.util.*;


public class CodeInjector {

    /**
     * Injects a specified block of code into all job items referenced by a given route.
     *
     * <p>Finds all cTalendJob references within the route located at {@code routeItemPath},
     * builds paths to each job, and injects {@code newCode} into them.
     *
     * @param routeItemPath the file system path to the .item file of the route
     * @param newCode the code block to inject into each referenced job
     */
    public static void injectCodeToAllJobsOfRoute(String routeItemPath, String newCode) {
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
     * <p>Traverses upward from the given route path to find the project root containing a "routes" directory,
     * then builds the path to the corresponding job file under the "process" folder.
     *
     * @param routeItemPath the path to the route .item file
     * @param jobName the name of the job to locate
     * @param version the version of the job
     * @return the absolute path to the job's .item file
     * @throws IOException if the project folder cannot be located
     */

    private static String buildJobFilePathByRouteItemPath(String routeItemPath, String jobName, String version) throws IOException {
        File routeFile = new File(routeItemPath).getCanonicalFile();
        File projectDir = routeFile.getParentFile();

        while (projectDir != null && !new File(projectDir, "routes").exists()) {
            projectDir = projectDir.getParentFile();
        }

        if (projectDir == null) {
            throw new IOException("Unable to locate Talend project folder from: " + routeItemPath);
        }

        File jobFile = new File(projectDir, "process/" + jobName + "_" + version + ".item");
        return jobFile.getAbsolutePath();
    }

    /**
     * Ensures that a tPrejob component with a specific unique name exists in the given XML document.
     *
     * <p>If the component with UNIQUE_NAME = "tPrejob_1" is not found,
     * this method appends a new tPrejob element to the root of the document.
     *
     * @param doc the XML Document representing a Talend job
     */

    private static void ensurePrejobExists(Document doc) {
        NodeList nodes = doc.getElementsByTagName("node");
        boolean hasPrejob = false;

        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            NodeList params = node.getElementsByTagName("elementParameter");
            for (int j = 0; j < params.getLength(); j++) {
                Element param = (Element) params.item(j);
                if ("UNIQUE_NAME".equals(param.getAttribute("name"))
                        && "tPrejob_1".equals(param.getAttribute("value"))) {
                    hasPrejob = true;
                    break;
                }
            }
            if (hasPrejob) break;
        }

        if (!hasPrejob) {
            Element root = doc.getDocumentElement();
            root.appendChild(CreateAndGetElements.getNewTPreJobComponent(doc));
        }
    }

    /**
     * Ensures that a tJava component with UNIQUE_NAME="logconfig" exists in the XML.
     *
     * <p>If it does not exist, creates it with the provided code.
     * If it does exist, replaces its CODE content with the new code.
     *
     * @param doc     the XML Document representing a Talend job
     * @param newCode the Java code to inject into the tJava component
     */
    private static void ensureTJavaWithCustomCodeExists(Document doc, String newCode) {
        Element root = doc.getDocumentElement();
        NodeList nodes = doc.getElementsByTagName("node");
        Element logconfigNode = null;

        // Searching for a node with UNIQUE_NAME="logconfig"
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            NodeList params = node.getElementsByTagName("elementParameter");
            for (int j = 0; j < params.getLength(); j++) {
                Element param = (Element) params.item(j);
                if ("UNIQUE_NAME".equals(param.getAttribute("name"))
                        && "logconfig".equals(param.getAttribute("value"))) {
                    logconfigNode = node;
                    break;
                }
            }
            if (logconfigNode != null) break;
        }

        // Creating logconfig if it doesn't exist
        if (logconfigNode == null) {
            logconfigNode = CreateAndGetElements.getNewTJavaComponent(doc, "logconfig", newCode);
            root.appendChild(logconfigNode);
        } else {
            // Overwriting code if logconfig already exists
            NodeList params = logconfigNode.getElementsByTagName("elementParameter");
            for (int i = 0; i < params.getLength(); i++) {
                Element param = (Element) params.item(i);
                if ("CODE".equals(param.getAttribute("name"))) {
                    param.setAttribute("value", newCode);
                }
            }
        }
    }

    /**
     * Ensures there is a connection from tPrejob_1 to logconfig (tJava with custom code)
     *
     * <p>If not reachable, finds the last reachable node in tPrejob_1 chain and
     * adds a new connection (OnComponentOk) with name = OnComponentOkLogger .
     *
     * @param doc the XML Document representing the Talend job
     */

    private static void ensureConnectionFromPrejobToTJavaWithCustomCode(Document doc) {
        NodeList connections = doc.getElementsByTagName("connection");
        Map<String, List<String>> graph = new HashMap<>();

        // Building the dependency graph
        for (int i = 0; i < connections.getLength(); i++) {
            Element conn = (Element) connections.item(i);
            graph.computeIfAbsent(conn.getAttribute("source"), k -> new ArrayList<>())
                    .add(conn.getAttribute("target"));
        }

        // Checking logconfig reachability
        if (!ChainHelper.isNodeReachable("tPrejob_1", "logconfig", graph)) {
            String lastNode = ChainHelper.findLastNodeInChain("tPrejob_1", graph);

           Element connection = CreateAndGetElements.getNewConnectionElement(doc,"OnComponentOkLogger",
                   lastNode,"logconfig");
            Element root = doc.getDocumentElement();
            root.appendChild(connection);
        }
    }

    /**
     * Injects or updates custom Java code into a Talend job .item file.
     * <p>Ensures the presence of tPrejob, tJava (logconfig), and proper connection between them.
     *
     * @param jobPath path to the Talend .item file
     * @param newCode Java code to inject into tJava
     * @throws Exception on file or XML processing errors
     */
    private static void processJobItemFile(String jobPath, String newCode) throws Exception {
        File xmlFile = new File(jobPath);
        if (!xmlFile.exists()) {
            System.err.println("Job file not found: " + jobPath);
            return;
        }

        DocumentBuilder dBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document doc = dBuilder.parse(xmlFile);
        doc.getDocumentElement().normalize();

        ensureTJavaWithCustomCodeExists(doc, newCode);
        ensurePrejobExists(doc);
        ensureConnectionFromPrejobToTJavaWithCustomCode(doc);

        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(new DOMSource(doc), new StreamResult(xmlFile));

        System.out.println("Processing completed for: " + jobPath);
    }
}
