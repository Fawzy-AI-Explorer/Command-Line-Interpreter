import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Parser parser = new Parser();
        Terminal terminal = new Terminal(parser);
        Scanner scanner = new Scanner(System.in);

        System.out.println("Welcome to the our Terminal!");

        while (true) {
            System.out.print(terminal.pwd() + "> "); // Print the current directory in the prompt
            String input = scanner.nextLine(); // Read user input

            if (input.equals("exit")) {
                System.out.println("Exiting the terminal. Goodbye!");
                break;
            }

            // Parse user input and execute the corresponding command
            if (parser.parse(input)) {
                terminal.chooseCommandAction();
            } else {
                System.out.println("Invalid input. Please try again.");
            }
        }
        scanner.close();
    }
}
