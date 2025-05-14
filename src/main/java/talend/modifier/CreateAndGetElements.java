package talend.modifier;

import org.w3c.dom.*;

import java.util.Optional;

/**
 * Utility class for creating Talend job elements in XML format.
 * <p>
 * Provides factory methods to create tJava, tPrejob components and connections between nodes
 * for use in Talend .item files.
 */
public class CreateAndGetElements {

    public static final String DEFAULT_PREJOB_UNIQUE_NAME = "tPrejob_1";

    /**
     * Creates a new tJava component node with specified name and Java code.
     *
     * @param doc  the XML Document to create elements in
     * @param name the unique name of the tJava component
     * @param code the Java code to be embedded in the component
     * @return the constructed XML Element representing the tJava node
     */
    public static Element getNewTJavaComponent(Document doc, String name, String code) {
        Element tjavaNode = doc.createElement("node");
        tjavaNode.setAttribute("componentName", "tJava");
        tjavaNode.setAttribute("componentVersion", "0.101");
        tjavaNode.setAttribute("offsetLabelX", "0");
        tjavaNode.setAttribute("offsetLabelY", "0");
        tjavaNode.setAttribute("posX", "320");
        tjavaNode.setAttribute("posY", "64");

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

        tjavaNode.appendChild(uniqueNameParam);
        tjavaNode.appendChild(codeParam);
        tjavaNode.appendChild(importParam);
        tjavaNode.appendChild(connectionFormatParam);
        tjavaNode.appendChild(metadata);
        return tjavaNode;
    }

    /**
     * Creates a new tPrejob component node with a fixed name "tPrejob_1".
     *
     * @param doc the XML Document to create elements in
     * @return the constructed XML Element representing the tPrejob node
     */
    public static Element getNewTPreJobComponent(Document doc) {

        Element prejobNode = doc.createElement("node");
        prejobNode.setAttribute("componentName", "tPrejob");
        prejobNode.setAttribute("componentVersion", "0.101");
        prejobNode.setAttribute("offsetLabelX", "0");
        prejobNode.setAttribute("offsetLabelY", "0");
        prejobNode.setAttribute("posX", "0");
        prejobNode.setAttribute("posY", "0");

        Element uniqueNameParam = doc.createElement("elementParameter");
        uniqueNameParam.setAttribute("field", "TEXT");
        uniqueNameParam.setAttribute("name", "UNIQUE_NAME");
        uniqueNameParam.setAttribute("value", DEFAULT_PREJOB_UNIQUE_NAME);

        prejobNode.appendChild(uniqueNameParam);
        return prejobNode;
    }

    /**
     * Creates a new connection element between two Talend components.
     * <p>
     * This connection uses the "OnComponentOk" trigger type.
     *
     * @param doc    the XML Document to create elements in
     * @param name   the unique name of the connection
     * @param source the name of the source component
     * @param target the name of the target component
     * @return the constructed XML Element representing the connection
     */
    public static Element getNewOnComponentOkConnectionElement(Document doc, String name,
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

    public static void createNewMainConnectionElementWithoutSchema(Document doc, String label,
                                                                   String source, String target) {

        if (isConnectionAlreadyPresent(doc, label)) {
            System.out.println("Connection is already present between tRestRequest and tJavaRow: " + label);
            return;
        }

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
        connection.appendChild(monitorParam);

        Element uniqueNameParam = doc.createElement("elementParameter");
        uniqueNameParam.setAttribute("field", "TEXT");
        uniqueNameParam.setAttribute("name", "UNIQUE_NAME");
        uniqueNameParam.setAttribute("value", label);
        uniqueNameParam.setAttribute("show", "false");
        connection.appendChild(uniqueNameParam);

        doc.getDocumentElement().appendChild(connection);
    }

    public static void createNewMainConnectionElementWithSchema(Document doc, String name,
                                                                String source, String target,
                                                                String label, String traceColumn) {
        if (isConnectionAlreadyPresent(doc, label)) {
            System.out.println("Connection is already present " +
                    "between tJavaRow and tRestResponse: " + label);
            return;
        }

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
        uniqueNameParam.setAttribute("value", name);
        uniqueNameParam.setAttribute("show", "false");
        connection.appendChild(uniqueNameParam);

        createConnectionMetadataToNode(doc, source, "id_String", traceColumn);
        createConnectionMetadataToNode(doc, target, "id_String", traceColumn);

        doc.getDocumentElement().appendChild(connection);
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

    public static void createNewTJavaRowComponent(Document doc, String name,
                                                  String code) {
        Element tjavaRowNode = doc.createElement("node");
        tjavaRowNode.setAttribute("componentName", "tJavaRow");
        tjavaRowNode.setAttribute("componentVersion", "0.101");
        tjavaRowNode.setAttribute("offsetLabelX", "0");
        tjavaRowNode.setAttribute("offsetLabelY", "0");
        tjavaRowNode.setAttribute("posX", "320");
        tjavaRowNode.setAttribute("posY", "64");

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

        tjavaRowNode.appendChild(uniqueNameParam);
        tjavaRowNode.appendChild(codeParam);
        tjavaRowNode.appendChild(importParam);
        tjavaRowNode.appendChild(connectionFormatParam);
        tjavaRowNode.appendChild(metadata);

        Element rootElement = doc.getDocumentElement();
        rootElement.appendChild(tjavaRowNode);
    }

    public static void createNewTRestResponseComponent(Document doc, String name,
                                                       String returnBodyType, String returnStatusCode) {

        Element tRestResponseNode = doc.createElement("node");
        tRestResponseNode.setAttribute("componentName", "tRESTResponse");
        tRestResponseNode.setAttribute("componentVersion", "0.101");
        tRestResponseNode.setAttribute("offsetLabelX", "0");
        tRestResponseNode.setAttribute("offsetLabelY", "0");
        tRestResponseNode.setAttribute("posX", "640");
        tRestResponseNode.setAttribute("posY", "64");
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

        tRestResponseNode.appendChild(uniqueNameParam);
        tRestResponseNode.appendChild(responseTypeParam);
        tRestResponseNode.appendChild(statusCodeParam);
        tRestResponseNode.appendChild(customStatusCodeParam);
        tRestResponseNode.appendChild(responseHeadersParam);
        tRestResponseNode.appendChild(unwrapJsonResponseParam);
        tRestResponseNode.appendChild(jsonArrayKeysParam);
        tRestResponseNode.appendChild(connectionFormatParam);
        tRestResponseNode.appendChild(metadata);

        Element rootElement = doc.getDocumentElement();
        rootElement.appendChild(tRestResponseNode);
    }

    public static void addOutputFlowToTRestRequest(Document doc, String outputFlow,
                                                   String verb, String pattern,
                                                   String consumes, String produces) {
        NodeList nodes = doc.getElementsByTagName("node");
        for (int i = 0; i < nodes.getLength(); i++) {
            Element node = (Element) nodes.item(i);
            if ("tRESTRequest".equals(node.getAttribute("componentName"))) {
                System.out.println("tRESTRequest found");

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
                        System.out.println("Output Flow already exists: " + outputFlow);
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

                System.out.println("Output Flow added: " + outputFlow);
                return;
            }
        }
        System.out.println("Component tRESTRequest not found");
    }

    private static boolean isConnectionAlreadyPresent(Document doc, String label) {
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

}