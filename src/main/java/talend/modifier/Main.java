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
            case "--s":
                StatusInjector.injectStatusToService(itemPath);
                break;
            case "--l":
                LoggerInjector.injectLoggerCodeToAllJobsOfRoute(itemPath,
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
        System.out.println("Usage: --s|--l <serviceItemPath>|<routeItemPath>");
        System.out.println("--s for status injection");
        System.out.println("--l for log code injection");
    }
}
