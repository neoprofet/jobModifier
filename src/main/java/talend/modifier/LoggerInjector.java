package talend.modifier;

import org.w3c.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LoggerInjector {
    public static final String DEFAULT_TJAVA_UNIQUE_NAME = "__logconfig__";

    public static void injectLoggerCodeToAllJobsOfRoute(String routeItemPath, String newCode) {
        try {
            Map<String, String> jobNamesAndVersions = RouteItemHelper.
                getJobNamesAndVersionsByRouteItemPath(routeItemPath);

            if (jobNamesAndVersions.isEmpty()) {
                System.out.println("No cTalendJob references found in route.");
                return;
            }

            for (Map.Entry<String, String> entry : jobNamesAndVersions.entrySet()) {
                String jobPath = RouteItemHelper.buildJobFilePathByRouteItemPath(
                    routeItemPath,
                    entry.getKey(),
                    entry.getValue()
                );
                System.out.println("Injecting into: " + jobPath);
                processItemFile(jobPath, newCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void injectLoggerCodeToAllServicesByRoute(String routeItemPath, String newCode) {
        try {
            File processDir = FileHelper.resolveSubdirectoryUpwards(new File(routeItemPath),
                "process").orElseThrow(
                () -> new IOException("Directory process not found upwards from: " +
                    routeItemPath)
            );

            List<File> svcItems = FileHelper.findFilesContainingNamePart(processDir, "SVC")
                .stream()
                .filter(file -> file.getName().endsWith(".item"))
                .collect(Collectors.toList());

            if (svcItems.isEmpty()) {
                System.out.println("No SVCs items found in project.");
                return;
            }

            for (File f : svcItems) {
                System.out.println("Injecting into: " + f.getAbsolutePath());
                processItemFile(f.getAbsolutePath(), newCode);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private static void processItemFile(String jobPath, String newCode) throws Exception {

        Document doc = FileHelper.loadDocument(jobPath);
        doc.getDocumentElement().normalize();
        Element root = doc.getDocumentElement();

        String prejobName = TalendComponentsHelper.getUniqueComponentName(doc, "tPrejob")
            .orElseGet(() -> {
                Element newPrejob = TalendComponentsHelper.getNewTPreJobComponent(doc);
                root.appendChild(newPrejob);
                return TalendComponentsHelper.DEFAULT_PREJOB_UNIQUE_NAME;
            });

        if (!TalendComponentsHelper.hasComponentExists(doc, DEFAULT_TJAVA_UNIQUE_NAME, "UNIQUE_NAME")) {

            Element tJavaWithCustomCodeNode = TalendComponentsHelper.getNewTJavaComponent(
                doc,
                DEFAULT_TJAVA_UNIQUE_NAME,
                newCode
            );

            root.appendChild(tJavaWithCustomCodeNode);
        } else {
            TalendComponentsHelper.updateParameterValue(
                doc,
                "tJava",
                DEFAULT_TJAVA_UNIQUE_NAME,
                "CODE", newCode
            );
        }

        TalendComponentsHelper.connectOneComponentToTheOther(doc, prejobName, DEFAULT_TJAVA_UNIQUE_NAME);

        FileHelper.saveDocument(doc, jobPath);
        System.out.println("Processing completed for: " + jobPath);
    }
}
