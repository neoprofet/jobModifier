package talend.modifier;

public class Main {

    public static void main(String[] args) {

        if (args.length < 2) {
            showUsage("The number of arguments is less than necessary; exactly two are needed.");
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
            case "--log-svcs":
                LoggerInjector.injectLoggerCodeToAllServicesByRoute(itemPath,
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
        System.out.println("  --status-svc <svcItemPath>    " +
            "Injects status handling into the specified service.");
        System.out.println("  --log-route <routeItemPath>   " +
            "Injects logging code into the main job(s) of the specified route.");
        System.out.println("  --log-svcs <routeItemPath>    " +
            "Injects logging code into all services of the project, resolved via the specified route.");
    }
}
