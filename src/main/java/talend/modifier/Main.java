package talend.modifier;

public class Main {

    public static void main(String[] args) {

        if (args.length < 2) {
            showUsage("The number of arguments is less than necessary;" +
                " exactly two are needed.");
            return;
        }

        String flag = args[0];
        String itemPath = args[1];

        switch (flag) {
            case "--status-svc":
                StatusInjector.injectStatusToService(itemPath);
                break;
            case "--log-route":
                LoggerInjector.injectLoggerCodeToAllJobsOfRoute(itemPath,
                    ExternalCode.T_JAVA_LOGCONFIG_CODE);
                break;
            case "--log-item":
                LoggerInjector.injectLoggerCodeToItem(itemPath,
                    ExternalCode.T_JAVA_LOGCONFIG_CODE);
                break;
            default:
                showUsage("Invalid flag: " + flag);
                break;
        }
    }

    private static void showUsage(String error) {
        if (error != null) {
            System.out.println(error + "\n");
        }
        System.out.println("Usage:");
        System.out.println("  --status-svc <itemPath>    " +
            "Injects status handling into the specified service.");
        System.out.println("  --log-route <itemPath>   " +
            "Injects logging code into the main item(s) of the specified route.");
        System.out.println("  --log-item <itemPath>    " +
            "Injects logging code into the specified item");
    }
}
