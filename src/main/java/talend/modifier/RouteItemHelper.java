package talend.modifier;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RouteItemHelper {

    public static Map<String, String> getJobNamesAndVersionsByRouteItemPath(String routeItemPath) throws Exception {
        Document doc = FileHelper.loadDocument(routeItemPath);
        NodeList nodes = doc.getElementsByTagName("node");
        Map<String, String> jobNamesAndVersions = new HashMap<>();

        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if ("cTalendJob".equals(node.getAttribute("componentName"))) {

                String jobName = TalendComponentsHelper.getParameterValue(node,
                    "SELECTED_JOB_NAME");
                String jobVersion = TalendComponentsHelper.getParameterValue(node,
                    "SELECTED_JOB_NAME:PROCESS_TYPE_VERSION");

                if ("Latest".equalsIgnoreCase(jobVersion)) {
                    File routeFile = new File(routeItemPath).getCanonicalFile();
                    jobVersion = LatestVersionResolver.findLatestVersion(
                        FileHelper.resolveSubdirectoryUpwards(routeFile, "process")
                            .orElseThrow(
                                () -> new IOException("Directory process not found upwards from: " +
                                    routeFile.getAbsolutePath())
                            ),
                        jobName
                    ).orElseThrow(() -> new IOException("Job version not resolved"));
                }

                if (!jobName.isBlank() && !jobVersion.isBlank()) {
                    jobNamesAndVersions.put(jobName, jobVersion);
                }
            }
        }
        return jobNamesAndVersions;
    }

    public static String buildJobFilePathByRouteItemPath(String routeItemPath, String jobName, String version)
        throws IOException {
        File routeFile = new File(routeItemPath).getCanonicalFile();
        File processDir = FileHelper.resolveSubdirectoryUpwards(routeFile, "process")
            .orElseThrow(
                () -> new IOException("Directory process not found upwards from: " + routeFile.getAbsolutePath())
            );

        String targetName = jobName + "_" + version + ".item";
        File jobFile = FileHelper.findFileInDirectory(processDir, targetName)
            .orElseThrow(
                () -> new IOException("Job file not found: " + targetName + " under " + processDir.getAbsolutePath())
            );

        return jobFile.getAbsolutePath();
    }
}
