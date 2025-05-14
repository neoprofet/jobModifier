package talend.modifier;

public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Usage: java MainClass --s|--l <routeItemPath>");
            return;
        }

        String flag = args[0];
        String routeItemPath = args[1];

        switch (flag) {
            case "--s":
                StatusInjector.injectStatusToAllJobs(routeItemPath);
                break;
            case "--l":
                CodeInjector.injectCodeToAllJobsOfRoute(routeItemPath, ExternalCodeFabric.getNewCodeToInsertToLogconfig());
                break;
            default:
                System.out.println("Invalid flag. Use --s for status injection or --l for log code injection.");
                break;
        }
    }
}
