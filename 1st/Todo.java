import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class Todo {
    public static void main(String[] args) {
        // require at least one arg
        Art();
        if (args.length < 1) {
            System.out.println(Colors.WHITE_BOLD_BRIGHT
                    + "   [!] At least one command is required\n   [!] Use the help command for instructions\n"
                    + Colors.RESET);
            return;
        }
        String command = args[0];

        App app = new App(33);
        switch (command) {
            case "add":
                app.Add();
                break;
            case "list":
                app.List();
                break;
            case "edit":
                if (args.length < 2) {
                    System.out.println(Colors.WHITE_BOLD_BRIGHT + "   [!] Task ID is required\n" + Colors.RESET);
                    return;
                }
                app.Edit(args[1]);
                break;
            case "wipe":
                if (args.length < 2) {
                    System.out.println(Colors.WHITE_BOLD_BRIGHT + "   [!] Task ID is required\n" + Colors.RESET);
                    return;
                }
                app.Remove(args[1]);
                break;
            case "help":
                app.Help();
                break;
            default:
                System.out.println(Colors.RED_BOLD_BRIGHT + "   [!] Invalid Command" + Colors.RESET);
                break;
        }
    }

    static void Art() {
        String art = "      _____        _     \n" +
                "     |_   _|__  __| |___\n" +
                "       | |/ _ \\/ _` / _ \\\n" +
                "       |_|\\___/\\__,_\\___/\n";

        System.out.println(Colors.YELLOW_BOLD_BRIGHT + art + Colors.RESET);
    }
}

class App extends Store {
    static int maxLen;

    App(int maxTaskLen) {
        // plus two because we use two extra whitespaces in the task column
        maxLen = maxTaskLen + 2;
    }

    void Add() {
        // minus 2 to go back to the original maxlen
        int limit = maxLen - 2;
        Scanner sc = new Scanner(System.in);
        // System.out.printf("Enter task description (%d chars): ", limit);
        System.out.print(Colors.WHITE_BOLD_BRIGHT);
        System.out.println("   ┌────────────────────────┐");
        System.out.println("   │ Enter task description │");
        System.out.println("   └────────────────────────┘");
        System.out.print("    > ");
        System.out.print(Colors.RESET);
        String desc = sc.nextLine().trim();

        if (desc.length() > limit) {
            System.out.printf(
                    Colors.RED_BOLD_BRIGHT + "\n    [!] Task description must be within %d chars\n" + Colors.RESET,
                    limit);
            return;
        } else if (desc.length() < 4) {
            // becuase of ID size around line 96
            System.out.println(Colors.RED_BOLD_BRIGHT
                    + "\n    [!] You need to provide a description of at least 4 chars" + Colors.RESET);
            return;
        }

        Task created = CreateTask(desc);
        System.out.println(Colors.GREEN_BOLD_BRIGHT + "\n    [#] Task added!" + Colors.RESET);

        int descLen = created.description.length();
        System.out.println("   ┌────────" + "─".repeat(descLen) + "─┐");
        System.out.println(
                "   │ ID   : " + Colors.WHITE+ created.ID + Colors.RESET
                        + " ".repeat(descLen - 4) + " │");
        System.out.println("   │ Task : " + created.description + " │");
        System.out.println("   └────────" + "─".repeat(descLen) + "─┘");

    }

    void List() {
        List<Task> tasks = ReadTasks();

        if (tasks.size() > 0) {
            System.out.println(Colors.GREEN_BOLD_BRIGHT + "    [#] You got it!" + Colors.RESET);
        }

        String idhead = Colors.WHITE_BOLD_BRIGHT + "ID" + Colors.RESET;
        String taskhead = Colors.WHITE_BOLD_BRIGHT + "Task" + Colors.RESET;
        // draw the header
        System.out.println("    ┌" + "─".repeat(6) + "┬" + "─".repeat(maxLen) + "┐");
        System.out.println("    │" + "  " + idhead + "  " + "│" + "  " + taskhead + " ".repeat(maxLen - 6) + "│");
        System.out.println("    ├" + "─".repeat(6) + "┼" + "─".repeat(maxLen) + "┤");

        // rest of the table
        for (Task t : tasks) {
            String tid = " " + t.ID + " ";
            String tdesc = " " + t.description + " ";
            System.out.println("    │" + tid + "│" + tdesc + " ".repeat(padding(tdesc)) + "│");
        }

        // last line
        System.out.println("    └" + "─".repeat(6) + "┴" + "─".repeat(maxLen) + "┘");
    }

    void Edit(String ID) {
        if (!IdExists(ID)) {
            System.out.println(Colors.WHITE_BOLD_BRIGHT + "   [!] No task with this ID exists" + Colors.RESET);
            return;
        }

        System.out.println(Colors.WHITE_BOLD_BRIGHT + "    [#] Editing Task" + Colors.RESET);
        // fetch original
        Task original = ReadTask(ID);
        int descLen = original.description.length();
        // print
        System.out.println("   ┌────────" + "─".repeat(descLen) + "─┐");
        System.out.println(
                "   │ ID   : " + Colors.WHITE+ original.ID + Colors.RESET
                        + " ".repeat(descLen - 4) + " │");
        System.out.println("   │ Task : " + original.description + " │");
        System.out.println("   └────────" + "─".repeat(descLen) + "─┘");
        System.out
                .print(Colors.WHITE_BOLD_BRIGHT + "\n    What would be the new description?\n    > " + Colors.RESET);
        // input
        Scanner sc = new Scanner(System.in);
        String input = sc.nextLine();

        // minus 2 to go back to the original maxlen
        int limit = maxLen - 2;
        if (input.length() > limit) {
            System.out.printf(
                    Colors.RED_BOLD_BRIGHT + "\n    [!] Task description must be within %d chars\n" + Colors.RESET,
                    limit);
            return;
        }

        // update
        UpdateTask(ID, input);
        System.out.println(Colors.GREEN_BOLD_BRIGHT + "\n    [#] Task updated!" + Colors.RESET);

        // confirmation print
        descLen = input.length();
        System.out.println("   ┌────────" + "─".repeat(descLen) + "─┐");
        System.out.println(
                "   │ ID   : " + Colors.WHITE+ original.ID + Colors.RESET
                        + " ".repeat(descLen - 4) + " │");
        System.out.println("   │ Task : " + input + " │");
        System.out.println("   └────────" + "─".repeat(descLen) + "─┘");
    }

    void Remove(String ID) {
        if (!IdExists(ID)) {
            System.out.println(Colors.WHITE_BOLD_BRIGHT + "   [!] No task with this ID exists\n" + Colors.RESET);
            return;
        }

        Task task = ReadTask(ID);
        System.out.println(Colors.WHITE_BOLD_BRIGHT + "    [#] Task to delete" + Colors.RESET);
        int descLen = task.description.length();
        System.out.println("   ┌────────" + "─".repeat(descLen) + "─┐");
        System.out.println(
                "   │ ID   : " + Colors.WHITE+ task.ID + Colors.RESET + " ".repeat(descLen - 4) + " │");
        System.out.println("   │ Task : " + task.description + " │");
        System.out.println("   └────────" + "─".repeat(descLen) + "─┘");

        System.out
                .print(Colors.WHITE_BOLD_BRIGHT + "\n    Are you sure you want to delete this task?" + Colors.RESET
                        + " (y/n)\n");
        System.out.print("    > ");

        Scanner sc = new Scanner(System.in);
        String character = sc.nextLine().trim().toLowerCase();

        // cannot use equal here. We need a value comparison
        if (character.equals("y")) {
            DeleteTask(ID);
            System.out.print(Colors.GREEN_BOLD_BRIGHT);
            System.out.println("\n    [#] Done!");
            System.out.print(Colors.RESET);

            System.out.println("   ┌───────────────────┐");
            String greenTask = Colors.WHITE+ task.ID + Colors.RESET;
            System.out.printf("   │ Task %s Removed │\n", greenTask);
            System.out.println("   └───────────────────┘");
            return;
        }

        System.out.print(Colors.RED_BRIGHT);
        System.out.println("   ┌─────────┐");
        System.out.println("   │ Aborted │");
        System.out.println("   └─────────┘");
        System.out.println(Colors.RESET);
    }

    void Help() {
        System.out.println(Colors.WHITE + "        - Cause I forget everything\n" + Colors.RESET);
        System.out.println("Usage:");
        System.out.println("  java Todo <command> [arguments]\n");
        System.out.println("Available Commands:");
        System.out.println("  add           Add a new todo item");
        System.out.println("  list          List all todo items");
        System.out.println("  edit <ID>     Edit a todo item by ID");
        System.out.println("  wipe <ID>     Delete a todo item by ID");
        System.out.println("  help          Show this help message");
    }

    // Find the padding remaining to complete max length
    private static int padding(String text) {
        if (text.length() >= maxLen) {
            return 0;
        }

        return maxLen - text.length();
    }
}

class Colors {
    public static final String RESET = "\033[0m"; // Text Reset

    // Regular Colors
    public static final String BLACK = "\033[0;30m"; // BLACK
    public static final String RED = "\033[0;31m"; // RED
    public static final String GREEN = "\033[0;32m"; // GREEN
    public static final String YELLOW = "\033[0;33m"; // YELLOW
    public static final String BLUE = "\033[0;34m"; // BLUE
    public static final String PURPLE = "\033[0;35m"; // PURPLE
    public static final String CYAN = "\033[0;36m"; // CYAN
    public static final String WHITE = "\033[0;37m"; // WHITE

    // Bold
    public static final String BLACK_BOLD = "\033[1;30m"; // BLACK
    public static final String RED_BOLD = "\033[1;31m"; // RED
    public static final String GREEN_BOLD = "\033[1;32m"; // GREEN
    public static final String YELLOW_BOLD = "\033[1;33m"; // YELLOW
    public static final String BLUE_BOLD = "\033[1;34m"; // BLUE
    public static final String PURPLE_BOLD = "\033[1;35m"; // PURPLE
    public static final String CYAN_BOLD = "\033[1;36m"; // CYAN
    public static final String WHITE_BOLD = "\033[1;37m"; // WHITE

    // Underline
    public static final String BLACK_UNDERLINED = "\033[4;30m"; // BLACK
    public static final String RED_UNDERLINED = "\033[4;31m"; // RED
    public static final String GREEN_UNDERLINED = "\033[4;32m"; // GREEN
    public static final String YELLOW_UNDERLINED = "\033[4;33m"; // YELLOW
    public static final String BLUE_UNDERLINED = "\033[4;34m"; // BLUE
    public static final String PURPLE_UNDERLINED = "\033[4;35m"; // PURPLE
    public static final String CYAN_UNDERLINED = "\033[4;36m"; // CYAN
    public static final String WHITE_UNDERLINED = "\033[4;37m"; // WHITE

    // Background
    public static final String BLACK_BACKGROUND = "\033[40m"; // BLACK
    public static final String RED_BACKGROUND = "\033[41m"; // RED
    public static final String GREEN_BACKGROUND = "\033[42m"; // GREEN
    public static final String YELLOW_BACKGROUND = "\033[43m"; // YELLOW
    public static final String BLUE_BACKGROUND = "\033[44m"; // BLUE
    public static final String PURPLE_BACKGROUND = "\033[45m"; // PURPLE
    public static final String CYAN_BACKGROUND = "\033[46m"; // CYAN
    public static final String WHITE_BACKGROUND = "\033[47m"; // WHITE

    // High Intensity
    public static final String BLACK_BRIGHT = "\033[0;90m"; // BLACK
    public static final String RED_BRIGHT = "\033[0;91m"; // RED
    public static final String GREEN_BRIGHT = "\033[0;92m"; // GREEN
    public static final String YELLOW_BRIGHT = "\033[0;93m"; // YELLOW
    public static final String BLUE_BRIGHT = "\033[0;94m"; // BLUE
    public static final String PURPLE_BRIGHT = "\033[0;95m"; // PURPLE
    public static final String CYAN_BRIGHT = "\033[0;96m"; // CYAN
    public static final String WHITE_BRIGHT = "\033[0;97m"; // WHITE

    // Bold High Intensity
    public static final String BLACK_BOLD_BRIGHT = "\033[1;90m"; // BLACK
    public static final String RED_BOLD_BRIGHT = "\033[1;91m"; // RED
    public static final String GREEN_BOLD_BRIGHT = "\033[1;92m"; // GREEN
    public static final String YELLOW_BOLD_BRIGHT = "\033[1;93m";// YELLOW
    public static final String BLUE_BOLD_BRIGHT = "\033[1;94m"; // BLUE
    public static final String PURPLE_BOLD_BRIGHT = "\033[1;95m";// PURPLE
    public static final String CYAN_BOLD_BRIGHT = "\033[1;96m"; // CYAN
    public static final String WHITE_BOLD_BRIGHT = "\033[1;97m"; // WHITE
}

// clazz
class Store extends FileOpts {
    static String file = "./store.txt"; // hidden file
    private static String separator = "~~~";

    public static void UpdateTask(String TaskID, String content) {
        TouchFile(file);
        List<Task> tasks = ReadTasks();

        for (int i = 0; i < tasks.size(); i++) {
            if (tasks.get(i).ID.equals(TaskID)) {
                Task t = new Task(TaskID, content);
                tasks.set(i, t);
            }
        }

        String content2 = "";
        for (Task t : tasks) {
            content2 += t.ID + separator + t.description + "\n";
        }

        WriteFile(file, content2);
    }

    public static void DeleteTask(String TaskID) {
        List<Task> tasks = ReadTasks();
        // remove all
        // tasks.removeIf(task -> task.ID.equals(TaskID));

        // NOTE: we are iterating backwards because elements of an array can't be
        // deleted reliably when iterating forward at the same time
        // A better solution would be to use predicate
        for (int i = tasks.size() - 1; i >= 0; i--) {
            if (tasks.get(i).ID.equals(TaskID)) {
                tasks.remove(i);
            }
        }

        String content = "";
        for (Task t : tasks) {
            content += t.ID + separator + t.description + "\n";
        }

        WriteFile(file, content);
    }

    // Create a task and return it
    public static Task CreateTask(String description) {
        TouchFile(file);

        // regenerate the ID if it's already assigned
        String id = GenerateID();
        while (IdExists(id)) {
            id = GenerateID();
        }

        Task task = new Task(id, description);
        AppendFile(file, task.ID + separator + task.description + "\n");
        return task;
    }

    public static List<Task> ReadTasks() {
        List<Task> tasks = new ArrayList<>();

        // return empty list incase file doesn't exist
        if (!FileExists(file)) {
            return tasks;
        }

        List<String> lines = ReadFile(file);

        for (String s : lines) {
            if (s.trim().equals("")) {
                continue; // avoid empty lines
            }
            String[] parts = s.split(separator);
            Task task = new Task(parts[0], parts[1]);
            tasks.add(task);
        }

        return tasks;
    }

    public static Task ReadTask(String ID) {
        for (Task t : ReadTasks()) {
            if (t.ID.equals(ID)) {
                return t;
            }
        }
        return null;
    }

    public static boolean IdExists(String ID) {
        List<Task> tasks = ReadTasks();
        for (Task t : tasks) {
            if (t.ID.equals(ID)) {
                return true;
            }
        }
        return false;
    }

    private static String GenerateID() {
        Random rand = new Random();
        int randomNum = rand.nextInt(999);
        return String.format("T%03d", randomNum);
    }
}

class FileOpts {
    static boolean FileExists(String filepath) {
        File f = new File(filepath);
        if (f.exists() && !f.isDirectory()) {
            return true;
        }
        return false;
    }

    static void AppendFile(String filepath, String line) {
        try {
            FileWriter myWriter = new FileWriter(filepath, true); // true for append mode
            myWriter.write(line);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void WriteFile(String filepath, String content) {
        try {
            FileWriter myWriter = new FileWriter(filepath);
            myWriter.write(content);
            myWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Create file if not exists
    static void TouchFile(String filepath) {
        try {
            File fileobj = new File(filepath);
            fileobj.createNewFile();
            // the above method returns a boolean, which is false if the file already exists
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // Read a file into a list
    static List<String> ReadFile(String filepath) {
        List<String> list = new ArrayList<>();
        File file = new File(filepath);

        try {
            Scanner sc = new Scanner(file);
            // keep reading as long as there is a line
            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                list.add(line);
            }
            sc.close();

        } catch (FileNotFoundException e) {
            System.out.println("[Error] could not read file " + file.getAbsolutePath());
            e.printStackTrace();
        }

        return list;
    }
}

class Task {
    public String description;
    public String ID;

    Task(String ID, String description) {
        this.ID = ID;
        this.description = description;
    }
}
