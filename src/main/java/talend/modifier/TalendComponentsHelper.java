package talend.modifier;

import org.w3c.dom.*;

import java.util.*;

public class TalendComponentsHelper {

    public static final String DEFAULT_PREJOB_UNIQUE_NAME = "tPrejob_1";

    public static Element getNewTJavaComponent(Document doc, String name, String code) {
        Element component = doc.createElement("node");
        component.setAttribute("componentName", "tJava");
        component.setAttribute("componentVersion", "0.101");
        component.setAttribute("offsetLabelX", "0");
        component.setAttribute("offsetLabelY", "0");
        component.setAttribute("posX", "320");
        component.setAttribute("posY", "64");
        Element uniqueNameParam = doc.createElement("elementParameter");
        uniqueNameParam.setAttribute("field", "TEXT");
        uniqueNameParam.setAttribute("name", "UNIQUE_NAME");
        uniqueNameParam.setAttribute("value", name);
        Element codeParam = doc.createElement("elementParameter");
        codeParam.setAttribute("field", "MEMO_JAVA");
        codeParam.setAttribute("name", "CODE");
        codeParam.setAttribute("value", code);
        Element importParam = doc.createElement("elementParameter");
        importParam.setAttribute("field", "MEMO_IMPORT");
        importParam.setAttribute("name", "IMPORT");
        importParam.setAttribute("value", "");
        Element connectionFormatParam = doc.createElement("elementParameter");
        connectionFormatParam.setAttribute("field", "TEXT");
        connectionFormatParam.setAttribute("name", "CONNECTION_FORMAT");
        connectionFormatParam.setAttribute("value", "row");
        Element metadata = doc.createElement("metadata");
        metadata.setAttribute("connector", "FLOW");
        metadata.setAttribute("name", name);

        component.appendChild(uniqueNameParam);
        component.appendChild(codeParam);
        component.appendChild(importParam);
        component.appendChild(connectionFormatParam);
        component.appendChild(metadata);
        return component;
    }

    public static Element getNewTPreJobComponent(Document doc) {

        Element component = doc.createElement("node");
        component.setAttribute("componentName", "tPrejob");
        component.setAttribute("componentVersion", "0.101");
        component.setAttribute("offsetLabelX", "0");
        component.setAttribute("offsetLabelY", "0");
        component.setAttribute("posX", "0");
        component.setAttribute("posY", "0");

        Element uniqueNameParam = doc.createElement("elementParameter");
        uniqueNameParam.setAttribute("field", "TEXT");
        uniqueNameParam.setAttribute("name", "UNIQUE_NAME");
        uniqueNameParam.setAttribute("value", DEFAULT_PREJOB_UNIQUE_NAME);

        component.appendChild(uniqueNameParam);
        return component;
    }

    public static Element getNewOnComponentOkConnectionComponent(Document doc, String name,
                                                                 String source, String target) {
        Element connection = doc.createElement("connection");
        connection.setAttribute("connectorName", "COMPONENT_OK");
        connection.setAttribute("label", "OnComponentOk");
        connection.setAttribute("lineStyle", "3");
        connection.setAttribute("metaname", source);
        connection.setAttribute("offsetLabelX", "64");
        connection.setAttribute("offsetLabelY", "64");
        connection.setAttribute("source", source);
        connection.setAttribute("target", target);

        Element param = doc.createElement("elementParameter");
        param.setAttribute("field", "TEXT");
        param.setAttribute("name", "UNIQUE_NAME");
        param.setAttribute("show", "false");
        param.setAttribute("value", name);

        connection.appendChild(param);
        return connection;
    }

    public static Element getNewMainConnectionComponentWithoutSchema(Document doc, String source,
                                                                     String target, String label) {

        Element connection = doc.createElement("connection");
        connection.setAttribute("connectorName", "FLOW");
        connection.setAttribute("label", label);
        connection.setAttribute("lineStyle", "0");
        connection.setAttribute("metaname", label);
        connection.setAttribute("offsetLabelX", "0");
        connection.setAttribute("offsetLabelY", "0");
        connection.setAttribute("source", source);
        connection.setAttribute("target", target);
        Element monitorParam = doc.createElement("elementParameter");
        monitorParam.setAttribute("field", "CHECK");
        monitorParam.setAttribute("name", "MONITOR_CONNECTION");
        monitorParam.setAttribute("value", "false");
        Element uniqueNameParam = doc.createElement("elementParameter");
        uniqueNameParam.setAttribute("field", "TEXT");
        uniqueNameParam.setAttribute("name", "UNIQUE_NAME");
        uniqueNameParam.setAttribute("value", label);
        uniqueNameParam.setAttribute("show", "false");

        connection.appendChild(uniqueNameParam);
        connection.appendChild(monitorParam);

        return connection;
    }

    public static Element getNewMainConnectionComponentWithSingleSchemaColumn(Document doc,
                                                                              String source, String target,
                                                                              String traceColumn) {
        String label = source + "_" + target;

        Element connection = doc.createElement("connection");
        connection.setAttribute("connectorName", "FLOW");
        connection.setAttribute("label", label);
        connection.setAttribute("lineStyle", "0");
        connection.setAttribute("metaname", source);
        connection.setAttribute("offsetLabelX", "0");
        connection.setAttribute("offsetLabelY", "0");
        connection.setAttribute("source", source);
        connection.setAttribute("target", target);
        Element param = doc.createElement("elementParameter");
        param.setAttribute("field", "TABLE");
        param.setAttribute("name", "TRACES_CONNECTION_FILTER");
        param.setAttribute("show", "false");
        Element traceColumnElement = doc.createElement("elementValue");
        traceColumnElement.setAttribute("elementRef", "TRACE_COLUMN");
        traceColumnElement.setAttribute("value", traceColumn);
        Element traceCheckedElement = doc.createElement("elementValue");
        traceCheckedElement.setAttribute("elementRef", "TRACE_COLUMN_CHECKED");
        traceCheckedElement.setAttribute("value", "true");
        Element traceConditionElement = doc.createElement("elementValue");
        traceConditionElement.setAttribute("elementRef", "TRACE_COLUMN_CONDITION");
        traceConditionElement.setAttribute("value", "");

        param.appendChild(traceColumnElement);
        param.appendChild(traceCheckedElement);
        param.appendChild(traceConditionElement);
        connection.appendChild(param);

        Element monitorParam = doc.createElement("elementParameter");
        monitorParam.setAttribute("field", "CHECK");
        monitorParam.setAttribute("name", "MONITOR_CONNECTION");
        monitorParam.setAttribute("value", "false");
        connection.appendChild(monitorParam);
        Element uniqueNameParam = doc.createElement("elementParameter");
        uniqueNameParam.setAttribute("field", "TEXT");
        uniqueNameParam.setAttribute("name", "UNIQUE_NAME");
        uniqueNameParam.setAttribute("value", label);
        uniqueNameParam.setAttribute("show", "false");
        connection.appendChild(uniqueNameParam);

        createConnectionMetadataToNode(doc, source, "id_String", traceColumn);
        createConnectionMetadataToNode(doc, target, "id_String", traceColumn);
        return connection;
    }

    private static void createConnectionMetadataToNode(Document doc, String uniqueName,
                                                       String type, String colName) {
        NodeList nodes = doc.getElementsByTagName("node");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);

            NodeList parameters = node.getElementsByTagName("elementParameter");
            for (int j = 0; j < parameters.getLength(); j++) {
                Element param = (Element) parameters.item(j);
                if ("TEXT".equals(param.getAttribute("field")) &&
                    "UNIQUE_NAME".equals(param.getAttribute("name")) &&
                    uniqueName.equals(param.getAttribute("value"))) {

                    NodeList metadataList = node.getElementsByTagName("metadata");
                    boolean metadataExists = false;
                    boolean columnExists = false;

                    for (int k = 0; k < metadataList.getLength(); k++) {
                        Element metadata = (Element) metadataList.item(k);
                        if ("FLOW".equals(metadata.getAttribute("connector")) &&
                            uniqueName.equals(metadata.getAttribute("name"))) {
                            metadataExists = true;

                            NodeList columns = metadata.getElementsByTagName("column");
                            for (int m = 0; m < columns.getLength(); m++) {
                                Element column = (Element) columns.item(m);
                                if (colName.equals(column.getAttribute("name"))) {
                                    columnExists = true;
                                    break;
                                }
                            }

                            if (!columnExists) {
                                Element column = getElementConnectionMetadataColumn(doc,
                                    colName, type);
                                metadata.appendChild(column);
                            }
                            break;
                        }
                    }

                    if (!metadataExists) {
                        Element metadata = doc.createElement("metadata");
                        metadata.setAttribute("connector", "FLOW");
                        metadata.setAttribute("name", uniqueName);

                        Element column = getElementConnectionMetadataColumn(doc, colName, type);
                        metadata.appendChild(column);
                        node.appendChild(metadata);
                    }
                    return;
                }
            }
        }
    }

    private static Element getElementConnectionMetadataColumn(Document doc, String colName,
                                                              String type) {
        Element column = doc.createElement("column");
        column.setAttribute("name", colName);
        column.setAttribute("type", type);
        column.setAttribute("key", "false");
        column.setAttribute("nullable", "true");
        column.setAttribute("usefulColumn", "true");
        column.setAttribute("length", "0");
        column.setAttribute("precision", "0");
        column.setAttribute("sourceType", "");
        column.setAttribute("originalLength", "-1");
        return column;
    }

    public static Element getNewTJavaRowComponent(Document doc, String name,
                                                  String code) {

        Element component = doc.createElement("node");
        component.setAttribute("componentName", "tJavaRow");
        component.setAttribute("componentVersion", "0.101");
        component.setAttribute("offsetLabelX", "0");
        component.setAttribute("offsetLabelY", "0");
        component.setAttribute("posX", "320");
        component.setAttribute("posY", "64");
        Element uniqueNameParam = doc.createElement("elementParameter");
        uniqueNameParam.setAttribute("field", "TEXT");
        uniqueNameParam.setAttribute("name", "UNIQUE_NAME");
        uniqueNameParam.setAttribute("value", name);
        Element codeParam = doc.createElement("elementParameter");
        codeParam.setAttribute("field", "MEMO_JAVA");
        codeParam.setAttribute("name", "CODE");
        codeParam.setAttribute("value", code);
        Element importParam = doc.createElement("elementParameter");
        importParam.setAttribute("field", "MEMO_IMPORT");
        importParam.setAttribute("name", "IMPORT");
        importParam.setAttribute("value", "");
        Element connectionFormatParam = doc.createElement("elementParameter");
        connectionFormatParam.setAttribute("field", "TEXT");
        connectionFormatParam.setAttribute("name", "CONNECTION_FORMAT");
        connectionFormatParam.setAttribute("value", "row");
        Element metadata = doc.createElement("metadata");
        metadata.setAttribute("connector", "FLOW");
        metadata.setAttribute("name", name);

        component.appendChild(uniqueNameParam);
        component.appendChild(codeParam);
        component.appendChild(importParam);
        component.appendChild(connectionFormatParam);
        component.appendChild(metadata);

        return component;
    }

    public static Element getNewTRestResponseComponent(Document doc, String name,
                                                       String returnBodyType, String returnStatusCode) {

        Element component = doc.createElement("node");
        component.setAttribute("componentName", "tRESTResponse");
        component.setAttribute("componentVersion", "0.101");
        component.setAttribute("offsetLabelX", "0");
        component.setAttribute("offsetLabelY", "0");
        component.setAttribute("posX", "640");
        component.setAttribute("posY", "64");
        Element uniqueNameParam = doc.createElement("elementParameter");
        uniqueNameParam.setAttribute("field", "TEXT");
        uniqueNameParam.setAttribute("name", "UNIQUE_NAME");
        uniqueNameParam.setAttribute("value", name);
        uniqueNameParam.setAttribute("show", "false");
        Element responseTypeParam = doc.createElement("elementParameter");
        responseTypeParam.setAttribute("field", "REST_RESPONSE_SCHEMA_TYPE");
        responseTypeParam.setAttribute("name", "RESPONSE_TYPE");
        responseTypeParam.setAttribute("value", "id_" + returnBodyType);
        Element statusCodeParam = doc.createElement("elementParameter");
        statusCodeParam.setAttribute("field", "CLOSED_LIST");
        statusCodeParam.setAttribute("name", "STATUS_CODE");
        statusCodeParam.setAttribute("value", returnStatusCode);
        Element customStatusCodeParam = doc.createElement("elementParameter");
        customStatusCodeParam.setAttribute("field", "TEXT");
        customStatusCodeParam.setAttribute("name", "CUSTOM_STATUS_CODE");
        customStatusCodeParam.setAttribute("value", returnStatusCode);
        customStatusCodeParam.setAttribute("show", "false");
        Element unwrapJsonResponseParam = doc.createElement("elementParameter");
        unwrapJsonResponseParam.setAttribute("field", "CHECK");
        unwrapJsonResponseParam.setAttribute("name", "UNWRAP_JSON_RESPONSE");
        unwrapJsonResponseParam.setAttribute("value", "false");
        Element jsonArrayKeysParam = doc.createElement("elementParameter");
        jsonArrayKeysParam.setAttribute("field", "TEXT");
        jsonArrayKeysParam.setAttribute("name", "JSON_ARRAY_KEYS");
        jsonArrayKeysParam.setAttribute("value", "");
        Element connectionFormatParam = doc.createElement("elementParameter");
        connectionFormatParam.setAttribute("field", "TEXT");
        connectionFormatParam.setAttribute("name", "CONNECTION_FORMAT");
        connectionFormatParam.setAttribute("value", "row");
        Element responseHeadersParam = doc.createElement("elementParameter");
        responseHeadersParam.setAttribute("field", "TABLE");
        responseHeadersParam.setAttribute("name", "RESPONSE_HEADERS");
        Element metadata = doc.createElement("metadata");
        metadata.setAttribute("connector", "FLOW");
        metadata.setAttribute("name", name);
        Element column = doc.createElement("column");
        column.setAttribute("defaultValue", "");
        column.setAttribute("key", "false");
        column.setAttribute("length", "0");
        column.setAttribute("name", "body");
        column.setAttribute("nullable", "true");
        column.setAttribute("precision", "0");
        column.setAttribute("sourceType", "");
        column.setAttribute("type", "id_" + returnBodyType);
        column.setAttribute("originalLength", "-1");
        column.setAttribute("usefulColumn", "true");
        metadata.appendChild(column);

        component.appendChild(uniqueNameParam);
        component.appendChild(responseTypeParam);
        component.appendChild(statusCodeParam);
        component.appendChild(customStatusCodeParam);
        component.appendChild(responseHeadersParam);
        component.appendChild(unwrapJsonResponseParam);
        component.appendChild(jsonArrayKeysParam);
        component.appendChild(connectionFormatParam);
        component.appendChild(metadata);

        return component;
    }

    public static void addOutputFlowToTRestRequestIfItsNotExisted(Document doc, String outputFlow,
                                                                  String verb, String pattern,
                                                                  String consumes, String produces) {
        NodeList nodes = doc.getElementsByTagName("node");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if ("tRESTRequest".equals(node.getAttribute("componentName"))) {

                Element schemasParam = null;
                NodeList parameters = node.getElementsByTagName("elementParameter");
                for (int j = 0; j < parameters.getLength(); j++) {
                    Element param = (Element) parameters.item(j);
                    if ("TABLE".equals(param.getAttribute("field")) &&
                        "SCHEMAS".equals(param.getAttribute("name"))) {
                        schemasParam = param;
                        break;
                    }
                }

                if (schemasParam == null) {
                    schemasParam = doc.createElement("elementParameter");
                    schemasParam.setAttribute("field", "TABLE");
                    schemasParam.setAttribute("name", "SCHEMAS");
                    node.appendChild(schemasParam);
                }

                NodeList elements = schemasParam.getElementsByTagName("elementValue");
                for (int k = 0; k < elements.getLength(); k++) {
                    Element element = (Element) elements.item(k);
                    if ("SCHEMA".equals(element.getAttribute("elementRef")) &&
                        outputFlow.equals(element.getAttribute("value"))) {
                        System.out.println("output flow: " + outputFlow + " existed");
                        return;
                    }
                }

                String[][] schemaElements = {
                    {"SCHEMA", outputFlow},
                    {"HTTP_VERB", verb},
                    {"URI_PATTERN", "\"" + pattern + "\""},
                    {"CONSUMES", consumes},
                    {"PRODUCES", produces},
                    {"STREAMING", "false"}
                };

                for (String[] schemaElement : schemaElements) {
                    Element elementValue = doc.createElement("elementValue");
                    elementValue.setAttribute("elementRef", schemaElement[0]);
                    elementValue.setAttribute("value", schemaElement[1]);
                    schemasParam.appendChild(elementValue);
                }

                Element metadata = doc.createElement("metadata");
                metadata.setAttribute("connector", "FLOW");
                metadata.setAttribute("label", outputFlow);
                metadata.setAttribute("name", outputFlow);
                node.appendChild(metadata);

                System.out.println("output flow: " + outputFlow + " created");
                return;
            }
        }
    }

    public static boolean isConnectionAlreadyPresent(Document doc, String label) {
        NodeList connections = doc.getElementsByTagName("connection");
        for (int i = 0; i < connections.getLength(); i++) {
            Element connection = (Element) connections.item(i);
            if (label.equals(connection.getAttribute("label"))) {
                return true;
            }
        }
        return false;
    }

    public static Optional<String> getUniqueComponentName(Document doc, String componentName) {
        NodeList nodes = doc.getElementsByTagName("node");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (componentName.equals(node.getAttribute("componentName"))) {
                NodeList parameters = node.getElementsByTagName("elementParameter");
                for (int j = 0; j < parameters.getLength(); j++) {
                    Element param = (Element) parameters.item(j);
                    if ("TEXT".equals(param.getAttribute("field")) &&
                        "UNIQUE_NAME".equals(param.getAttribute("name"))) {
                        return Optional.of(param.getAttribute("value"));
                    }
                }
            }
        }
        return Optional.empty();
    }

    public static boolean hasComponentExists(Document doc, String name, String filterBy) {
        NodeList nodes = doc.getElementsByTagName("node");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);

            if ("UNIQUE_NAME".equals(filterBy)) {
                NodeList params = node.getElementsByTagName("elementParameter");
                for (int j = 0; j < params.getLength(); j++) {
                    Element param = (Element) params.item(j);
                    if ("UNIQUE_NAME".equals(param.getAttribute("name")) &&
                        name.equals(param.getAttribute("value"))) {
                        return true;
                    }
                }
            } else if (name.equals(node.getAttribute(filterBy))) {
                return true;
            }
        }
        return false;
    }

    public static void connectOneComponentToTheOther(Document doc, String componentA, String componentB) {
        NodeList connections = doc.getElementsByTagName("connection");
        Map<String, List<String>> graph = new HashMap<>();

        // Building the dependency graph
        for (int i = 0; i < connections.getLength(); i++) {
            Element conn = (Element) connections.item(i);
            graph.computeIfAbsent(conn.getAttribute("source"), k -> new ArrayList<>())
                .add(conn.getAttribute("target"));
        }

        // Checking tJava with custom code reachability
        if (!ChainHelper.isNodeReachable(componentA, componentB, graph)) {
            String lastNode = ChainHelper.findLastNodeInChain(componentA, graph);

            Element connection = getNewOnComponentOkConnectionComponent(doc, "OnComponentOkLogger",
                lastNode, componentB);
            Element root = doc.getDocumentElement();
            root.appendChild(connection);
        }
    }

    public static Optional<String> getParameterValue(Element node, String paramName) {
        NodeList params = node.getElementsByTagName("elementParameter");
        for (int j = 0; j < params.getLength(); j++) {
            Element param = (Element) params.item(j);
            if (paramName.equals(param.getAttribute("name"))) {
                return Optional.of(param.getAttribute("value"));
            }
        }
        return Optional.empty();
    }


    public static void updateParameterValue(Document doc,
                                            String componentType, String uniqueName,
                                            String paramName, String newValue) {
        NodeList nodes = doc.getElementsByTagName("node");

        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if (componentType.equals(node.getAttribute("componentName"))) {
                NodeList params = node.getElementsByTagName("elementParameter");
                for (int j = 0; j < params.getLength(); j++) {
                    Element param = (Element) params.item(j);
                    if ("UNIQUE_NAME".equals(param.getAttribute("name"))
                        && uniqueName.equals(param.getAttribute("value"))) {
                        for (int k = 0; k < params.getLength(); k++) {
                            Element codeParam = (Element) params.item(k);
                            if (paramName.equals(codeParam.getAttribute("name"))) {
                                codeParam.setAttribute("value", newValue);
                             /*   System.out.println("Updated parameter '" + paramName +
                                    "' in component '" + uniqueName +
                                    "' to value: " + newValue); */
                                return;
                            }
                        }
                        System.out.println("Parameter '" + paramName +
                            "' not found in component '" + uniqueName +
                            "'. No update performed.");
                        return;
                    }
                }
            }
        }
        System.out.println("Component '" + uniqueName
            + "' of type '" + componentType +
            "' not found. No update performed.");
    }
}