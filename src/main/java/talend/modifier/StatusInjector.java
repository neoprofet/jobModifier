package talend.modifier;

import org.w3c.dom.*;

import java.util.function.Predicate;

public class StatusInjector {

    public static final String DEFAULT_T_REST_RESPONSE_UNIQUE_NAME = "__tRESTResponce_status__";
    public static final String DEFAULT_T_JAVA_ROW_UNIQUE_NAME = "__tJavaRow_status__";
    public static final String DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME = "__status__";

    public static final Predicate<Document> IS_T_REST_RESPONSE_MISSING =
        doc -> !TalendComponentsHelper.hasComponentExists(
            doc,
            StatusInjector.DEFAULT_T_REST_RESPONSE_UNIQUE_NAME,
            "UNIQUE_NAME"
        );

    public static final Predicate<Document> IS_T_JAVA_ROW_MISSING =
        doc -> !TalendComponentsHelper.hasComponentExists(
            doc,
            StatusInjector.DEFAULT_T_JAVA_ROW_UNIQUE_NAME,
            "UNIQUE_NAME"
        );

    public static final Predicate<Document> IS_T_REST_REQUEST_PRESENT =
        doc -> TalendComponentsHelper.hasComponentExists(
            doc,
            "tRESTRequest",
            "componentName"
        );

    public static final Predicate<Document> IS_CONNECTION_BETWEEN_T_JAVA_ROW_AND_T_REST_RESPONSE_MISSING =
        doc -> !TalendComponentsHelper.isConnectionAlreadyPresent(
            doc,
            "row " + DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME
        );

    public static final Predicate<Document> IS_CONNECTION_BETWEEN_T_REST_REQUEST_AND_T_JAVA_ROW_MISSING =
        doc -> !TalendComponentsHelper.isConnectionAlreadyPresent(
            doc,
            DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME
        );

    public static final Predicate<Document> ARE_T_JAVA_ROW_AND_T_REST_RESPONSE_MISSING =
        IS_T_REST_RESPONSE_MISSING.or(IS_T_JAVA_ROW_MISSING);

    public static void injectStatusToService(String servicePath) {
        try {
            Document doc = FileHelper.loadDocument(servicePath);
            doc.getDocumentElement().normalize();

            if (IS_T_REST_REQUEST_PRESENT.test(doc)) {

                if (ARE_T_JAVA_ROW_AND_T_REST_RESPONSE_MISSING.test(doc)) {
                    System.out.println("Processing file: " + servicePath);

                    if (IS_T_REST_RESPONSE_MISSING.test(doc)) {
                        Element tRestResponse = TalendComponentsHelper.getNewTRestResponseComponent(
                            doc,
                            DEFAULT_T_REST_RESPONSE_UNIQUE_NAME,
                            "String",
                            "OK (200)"
                        );
                        doc.getDocumentElement().appendChild(tRestResponse);
                        System.out.println("component: " + DEFAULT_T_REST_RESPONSE_UNIQUE_NAME + " created");
                    } else System.out.println("component: " + DEFAULT_T_REST_RESPONSE_UNIQUE_NAME + " existed");

                    if (IS_T_JAVA_ROW_MISSING.test(doc)) {
                        Element tJavaRow = TalendComponentsHelper.getNewTJavaRowComponent(doc,
                            DEFAULT_T_JAVA_ROW_UNIQUE_NAME,
                            ExternalCode.T_JAVA_ROW_STATUS_CODE
                        );
                        doc.getDocumentElement().appendChild(tJavaRow);
                        System.out.println("component: " + DEFAULT_T_JAVA_ROW_UNIQUE_NAME + " created");
                    } else System.out.println("component: " + DEFAULT_T_JAVA_ROW_UNIQUE_NAME + " existed");

                    if (IS_CONNECTION_BETWEEN_T_JAVA_ROW_AND_T_REST_RESPONSE_MISSING.test(doc)) {
                        Element connection = TalendComponentsHelper.getNewMainConnectionComponentWithSingleSchemaColumn(
                            doc,
                            DEFAULT_T_JAVA_ROW_UNIQUE_NAME,
                            DEFAULT_T_REST_RESPONSE_UNIQUE_NAME,
                            "body"
                        );
                        doc.getDocumentElement().appendChild(connection);
                        System.out.println("connection: " + DEFAULT_T_JAVA_ROW_UNIQUE_NAME +
                            "_" + DEFAULT_T_REST_RESPONSE_UNIQUE_NAME + " created");
                    } else System.out.println("connection: " + DEFAULT_T_JAVA_ROW_UNIQUE_NAME +
                        "_" + DEFAULT_T_REST_RESPONSE_UNIQUE_NAME + " existed");

                    TalendComponentsHelper.addOutputFlowToTRestRequestIfItsNotExisted(
                        doc,
                        DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME,
                        "GET",
                        "/" + DEFAULT_STATUS_OUTPUT_FLOW_UNIQUE_NAME,
                        "NONE",
                        "JSON"
                    );

                    String tRestRequestName = TalendComponentsHelper.getUniqueComponentName(doc,
                        "tRESTRequest").get();

                    if (IS_CONNECTION_BETWEEN_T_REST_REQUEST_AND_T_JAVA_ROW_MISSING.test(doc)) {
                        Element connection = TalendComponentsHelper.getNewMainConnectionComponentWithoutSchema(
                            doc,
                            tRestRequestName,
                            DEFAULT_T_JAVA_ROW_UNIQUE_NAME
                        );
                        doc.getDocumentElement().appendChild(connection);
                        System.out.println("connection: " + tRestRequestName +
                            "_" + DEFAULT_T_JAVA_ROW_UNIQUE_NAME + " created");
                    } else System.out.println("connection: " + tRestRequestName +
                        "_" + DEFAULT_T_JAVA_ROW_UNIQUE_NAME + " existed");

                    FileHelper.saveDocument(doc, servicePath);

                } else System.out.println("tRestResponse and tJavaRow components already exist. No action required.");

            } else System.out.println("tRestRequest component not found");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}