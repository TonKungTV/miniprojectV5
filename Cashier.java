import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Cashier {
    private String name;
    private PaymentProcessor paymentProcessor = new PaymentProcessor();
    private ReceiptPrinter receiptPrinter = new ReceiptPrinter();


    public Cashier(String name) {
        this.name = name;
    }
    public void displayProducts(List<Product> products) {
        System.out.println("\n--- Product List ---");
        if (products.isEmpty()) {
            System.out.println("No products available.");
            return;
        }
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            System.out.printf("%d. %s\n", (i + 1), product);
        }
        System.out.println("----------------------");
    }
    

    public Bill processPayment(Manager manager,Customer customer, List<Product> products, boolean payWithWallet) {
        Bill bill = paymentProcessor.processPayment(manager,customer, products, payWithWallet);
        if (bill != null) {
            receiptPrinter.printReceipt(bill);
        }
        return bill;
    }


    public void registerMembership(Customer customer, Scanner scanner) {
        System.out.print("Would you like to register for membership? (yes/no): ");
        String registerResponse = scanner.next();
        switch (registerResponse.toLowerCase()) {
            case "yes":
                System.out.print("Enter a membership ID: ");
                String membershipId = scanner.next();
                customer.setMembership(true, membershipId);
                System.out.println("Membership registered successfully!");
                break;
            case "no":
                System.out.println("Proceeding without membership.");
                break;
            default:
                System.out.println("Invalid response. Proceeding without membership.");
                break;
        }
    }

    public void checkMembershipStatus(Customer customer, Scanner scanner) {
        System.out.print("Is the customer a member? (yes/no): ");
        String isMemberResponse = scanner.next();
        switch (isMemberResponse.toLowerCase()) {
            case "yes":
                System.out.print("Enter membership ID: ");
                String membershipId = scanner.next();
                customer.setMembership(true, membershipId);
                System.out.println("Membership verified.");
                break;
            case "no":
                registerMembership(customer, scanner);
                break;
            default:
                System.out.println("Invalid response. Registering as non-member.");
                registerMembership(customer, scanner);
                break;
        }
    }

    public void printReceipt(Bill bill) {
        String storeName = "NextMart";
        String storeAddress = "123 Main St, City, Country";
        String storePhone = "Phone: 123-456-7890";
    
        String separator = "--------------------------------------";
        String lineSeparator = "======================================";
        
        System.out.printf("Bill Number: %-29s\n", bill.getBillNumber());
        System.out.println("\n" + lineSeparator);
        System.out.printf("%-20s %20s\n", storeName, " ");
        System.out.printf("%-20s %20s\n", storeAddress, " ");
        System.out.printf("%-20s %20s\n", storePhone, " ");
        System.out.println(lineSeparator);
        System.out.printf("Date: %-29s\n", bill.getDate());
        System.out.println(separator);
        System.out.println("Items:");
        List<Product> products = bill.getProducts();
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            System.out.printf("%-25s %10.2f\n", product.getName(), product.getPrice(null)); // ส่ง null เพราะไม่ต้องใช้ข้อมูล customer
        }
        System.out.printf("Total Amount: %-25.2f\n", bill.getTotalAmount());
        System.out.println(separator);
        System.out.printf("Member information\n");
        System.out.println(lineSeparator);
        System.out.println("Thank you for shopping at " + storeName + "!");
        System.out.println(lineSeparator);
    }
    public static void handleCashierRole(Cashier cashier, Manager manager, List<Customer> customers, Scanner scanner) {
        System.out.print("Enter Membership ID (leave blank if not a member): ");
        String membershipId = scanner.nextLine();

        Customer customer = null;
        if (!membershipId.isEmpty()) {
            customer = Customer.findCustomerByMembership(customers, membershipId);
        }

        if (customer == null) {
            System.out.println("Customer not found. Proceeding as non-member.");
            customer = new Customer("Guest", 100.0); // Create guest customer
            customers.add(customer);
        }

        boolean cashierRunning = true;
        while (cashierRunning) {
            System.out.println("\n--- Cashier Menu ---");
            System.out.println("1. Process Payment");
            System.out.println("2. Register Membership");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int cashierChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (cashierChoice) {
                case 1:
                    cashier.displayProducts(manager.getProducts());

                    List<Product> products = new ArrayList<>();
                    boolean selectingProducts = true;

                    while (selectingProducts) {
                        System.out.print("Enter Product ID (or 'done' to finish): ");
                        String input = scanner.nextLine();

                        if (input.equalsIgnoreCase("done")) {
                            selectingProducts = false;
                            continue; // Exit product selection loop
                        }

                        try {
                            int productId = Integer.parseInt(input.trim()) - 1;
                            if (productId >= 0 && productId < manager.getProducts().size()) {
                                Product selectedProduct = manager.getProducts().get(productId);

                                System.out.print("Enter quantity for " + selectedProduct.getName() + ": ");
                                int quantity = scanner.nextInt();
                                scanner.nextLine();

                                if (quantity > 0 && quantity <= selectedProduct.getQuantity()) {
                                    for (int i = 0; i < quantity; i++) {
                                        products.add(selectedProduct);
                                    }
                                    System.out
                                            .println(quantity + " of " + selectedProduct.getName() + " added to cart.");
                                } else {
                                    System.out.println("Invalid quantity. Please enter a quantity between 1 and "
                                            + selectedProduct.getQuantity());
                                }
                            } else {
                                System.out.println("Invalid Product ID: " + (productId + 1));
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Please enter a valid Product ID or 'done' to finish.");
                        }
                    }

                    System.out.print("Pay with Wallet? (yes/no): ");
                    boolean payWithWallet = scanner.next().equalsIgnoreCase("yes");
                    scanner.nextLine(); // Consume newline

                    cashier.processPayment(manager, customer, products, payWithWallet);
                    break;

                case 2:
                    registerNewMember(customers, scanner);
                    break;
                case 3:
                    cashierRunning = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void registerNewMember(List<Customer> customers, Scanner scanner) {
        System.out.println("Registering a new membership...");
        System.out.print("Enter new member's name: ");
        String newName = scanner.nextLine();
        System.out.print("Enter initial wallet amount: ");
        double initialWallet = scanner.nextDouble();
        scanner.nextLine(); // Consume newline

        // Create a new customer and register
        Customer newCustomer = new Customer(newName, initialWallet);
        newCustomer.registerNewCustomer(); // Register the new customer in the JSON file
        customers.add(newCustomer); // Add to the customer list
        System.out.println("Membership registered successfully.");
        System.err.println("Your MembershipID is: " + newCustomer.getMembershipId());
    }
}
