package talend.modifier;
import org.w3c.dom.*;

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
    public static Element getNewTJavaComponent(Document doc, String name, String code){
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
    public static Element getNewTPreJobComponent(Document doc){

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
    public static Element getNewConnectionElement(Document doc, String name,
                                                  String source, String target){
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
}
