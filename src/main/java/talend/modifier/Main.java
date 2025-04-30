package talend.modifier;

public class Main {
    public static void main(String[] args) {
        String routeItemPath = args[0];
        String newCode = getNewCodeToInsert();
        CodeInjector.injectCodeToAllJobsOfRoute(routeItemPath,newCode);
    }

    /**
     * Generates a block of Java code as a {@code String} that performs runtime introspection within an OSGi environment.
     * <p>
     * The returned code snippet:
     * <ul>
     *   <li>Uses reflection to access OSGi classes such as {@code FrameworkUtil}, {@code Bundle}, and {@code BundleWiring}.</li>
     *   <li>Retrieves the current bundle associated with the class via {@code FrameworkUtil.getBundle(Class)}.</li>
     *   <li>Uses the {@code adapt} method to access the {@code BundleWiring} and retrieves all class files within the bundle using {@code findEntries}.</li>
     *   <li>Builds a list of fully-qualified class names from the discovered .class files, excluding the current class.</li>
     *   <li>Initializes a custom {@code Logger} for the current package using Log4j and assigns it to a static {@code log} field in each of the discovered classes.</li>
     * </ul>
     *
     * The purpose of this generated code is typically to dynamically inject a common logger into all classes of the same bundle
     * at runtime — useful in OSGi modular environments where traditional static logger injection may be more complex.
     *
     * <p>This method does <b>not</b> execute the code — it only returns it as a {@code String} for later use (e.g., code generation, instrumentation).
     *
     * <p><b>Note:</b> This code assumes that the context is an OSGi container. If run outside OSGi, the generated code will log
     * a message stating that it is not running in such an environment.
     *
     * @return A string representing Java source code that performs reflective discovery and logger injection in an OSGi environment.
     */

    private static String getNewCodeToInsert() {
        return "try {\n" +
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
    }
}
