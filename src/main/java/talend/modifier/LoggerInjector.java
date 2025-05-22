package talend.modifier;

import org.w3c.dom.*;

import java.io.File;
import java.util.*;

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

    public static void injectLoggerCodeToItem(String itemPathToInject, String newCode) {
        File file = Optional.ofNullable(itemPathToInject)
            .map(File::new)
            .orElseThrow(() -> new IllegalArgumentException("Path is null"));

        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Path does not exist or is not a file: "
                + file.getAbsolutePath());
        }

        if (!file.getName().endsWith(".item")) {
            throw new IllegalArgumentException("Not a .item file: " +
                file.getAbsolutePath());
        }

        try {
            LoggerInjector.processItemFile(file.getAbsolutePath(), newCode);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void processItemFile(String itemPath, String newCode) throws Exception {

        Document doc = FileHelper.loadDocument(itemPath);
        doc.getDocumentElement().normalize();
        Element root = doc.getDocumentElement();

        String prejobName = TalendComponentsHelper.getUniqueComponentName(doc, "tPrejob")
            .orElseGet(() -> {
                Element newPrejob = TalendComponentsHelper.getNewTPreJobComponent(doc);
                root.appendChild(newPrejob);
                return TalendComponentsHelper.DEFAULT_PREJOB_UNIQUE_NAME;
            });

        if (!TalendComponentsHelper.hasComponentExists(
            doc,
            DEFAULT_TJAVA_UNIQUE_NAME,
            "UNIQUE_NAME"
        )) {

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

        TalendComponentsHelper.connectOneComponentToTheOther(
            doc,
            prejobName,
            DEFAULT_TJAVA_UNIQUE_NAME
        );

        FileHelper.saveDocument(doc, itemPath);
        System.out.println("Processing completed for: " + itemPath);
    }
}
