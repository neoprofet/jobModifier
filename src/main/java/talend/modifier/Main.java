package talend.modifier;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            showUsage();
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
                    ExternalCodeFabric.getNewCodeToInsertToLogconfig());
                break;
            default:
                showUsage();
                System.out.println("Invalid flag. Use --s for status injection or --l for log code injection.");
                break;
        }
    }

    private static void showUsage() {
        System.out.println("Usage: --s|--l <jobItemPath>|<routeItemPath>");
    }
}
