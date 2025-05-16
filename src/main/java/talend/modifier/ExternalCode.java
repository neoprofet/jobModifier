package talend.modifier;

public class ExternalCode {

    public static final String T_JAVA_LOGCONFIG_CODE =
        "try {\n" +
            "    Class<?> frameworkUtilClass = Class.forName(\"org.osgi.framework.FrameworkUtil\");\n" +
            "    java.lang.reflect.Method getBundleMethod = frameworkUtilClass.getMethod(\"getBundle\", Class.class);\n" +
            "    Object bundle = getBundleMethod.invoke(null, this.getClass());\n" +
            "    if (bundle != null) {\n" +
            "        Class<?> bundleClass = Class.forName(\"org.osgi.framework.Bundle\");\n" +
            "        java.lang.reflect.Method adaptMethod = bundleClass.getMethod(\"adapt\", Class.class);\n" +
            "        Class<?> bundleWiringClass = Class.forName(\"org.osgi.framework.wiring.BundleWiring\");\n" +
            "        Object wiring = adaptMethod.invoke(bundle, bundleWiringClass);\n" +
            "        if (wiring != null) {\n" +
            "            java.lang.reflect.Method findEntries = bundleWiringClass.getMethod(\n" +
            "                \"findEntries\", String.class, String.class, int.class\n" +
            "            );\n" +
            "            int FINDENTRIES_RECURSE = bundleWiringClass.getField(\"FINDENTRIES_RECURSE\").getInt(null);\n" +
            "            @SuppressWarnings(\"unchecked\")\n" +
            "            List<java.net.URL> entries = (List<java.net.URL>) findEntries.invoke(\n" +
            "                wiring, \"/\", \"*.class\", FINDENTRIES_RECURSE\n" +
            "            );\n" +
            "            java.util.List<String> classNames = new java.util.ArrayList<>();\n" +
            "            for (java.net.URL url : entries) {\n" +
            "                String path = url.getPath();\n" +
            "                if (!path.contains(\"$\") && path.endsWith(\".class\")) {\n" +
            "                    if (path.startsWith(\"/\")) path = path.substring(1);\n" +
            "                    String className = path.replace(\".class\", \"\").replace(\"/\", \".\");\n" +
            "                    classNames.add(className);\n" +
            "                }\n" +
            "            }\n" +
            "            classNames.remove(this.getClass().getName());\n" +
            "            String packageName = this.getClass().getPackage().getName();\n" +
            "            org.apache.logging.log4j.Logger customLogger = org.apache.logging.log4j.LogManager.getLogger(packageName);\n" +
            "            for (String subJobClassName : classNames) {\n" +
            "                System.out.println(subJobClassName);\n" +
            "                try {\n" +
            "                    Class<?> thisClass = Class.forName(subJobClassName);\n" +
            "                    java.lang.reflect.Field logField = thisClass.getDeclaredField(\"log\");\n" +
            "                    logField.setAccessible(true);\n" +
            "                    logField.set(null, customLogger);\n" +
            "                } catch (Exception e) {\n" +
            "                    e.printStackTrace();\n" +
            "                }\n" +
            "            }\n" +
            "        } else {\n" +
            "            System.out.println(\"BundleWiring is null\");\n" +
            "        }\n" +
            "    } else {\n" +
            "        System.out.println(\"Bundle is null\");\n" +
            "    }\n" +
            "} catch (ClassNotFoundException e) {\n" +
            "    System.out.println(\"Not running in OSGi environment.\");\n" +
            "} catch (Exception e) {\n" +
            "    e.printStackTrace();\n" +
            "}";

    public static final String T_JAVA_ROW_STATUS_CODE =
        "JSONObject response = new JSONObject();\n" +
            "    response.put(\"status\", \"running\");\n" +
            "    output_row.body = response.toString();";

}
