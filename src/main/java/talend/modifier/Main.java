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
                StatusInjector.injectStatusToJob(itemPath);
                break;
            case "--l":
                LoggerInjector.injectLoggerCodeToAllJobsOfRoute(itemPath,
                        ExternalCodeFabric.getNewCodeToInsertToLogconfig());
                break;
            default:
                System.out.println("Invalid arguments");
                showUsage();
                break;
        }
    }

    private static void showUsage() {
        System.out.println("Usage: ... java -jar talendJobModifier.jar --s|--l <jobItemPath>|<routeItemPath>");
    }
}
