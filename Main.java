import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Manager manager = new Manager("John");
        Cashier cashier = new Cashier("Alice");
        List<Customer> customers = new ArrayList<>();
        Scanner scanner = new Scanner(System.in);

        boolean running = true;

        // Role selection
        while (running) {
            System.out.println("Select your role:");
            System.out.println("1. Customer");
            System.out.println("2. Cashier");
            System.out.println("3. Manager");
            System.out.println("4. Exit");
            System.out.print("Choose an option: ");
            int roleChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (roleChoice) {
                case 1: // Customer
                    Customer.handleCustomerRole(customers, scanner);
                    break;
                case 2: // Cashier
                    Cashier.handleCashierRole(cashier, manager, customers, scanner);
                    break;
                case 3: // Manager
                    Manager.handleManagerRole(manager, scanner);
                    break;
                case 4: // Exit
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }

        scanner.close();
        System.out.println("Thank you for using the system!");
    }
}
