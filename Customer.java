import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class Customer {
    private String name;
    private double wallet;
    private int points;
    public boolean isMember; // เพิ่มเป็น public เพื่อเข้าถึงได้ง่าย
    public String membershipId;

    private List<Bill> billHistory = new ArrayList<>();

    // public Customer(String name, double wallet) {
    // this.name = name;
    // this.wallet = wallet;
    // this.isMember = false;
    // }
    public Customer(String name, double wallet) {
        this.name = name;
        this.wallet = wallet;
        this.points = 0;
        this.isMember = true; // กำหนดเป็น true เมื่อสมัครสมาชิก
        this.membershipId = generateMembershipId(); // สร้าง ID สมาชิก
    }

    public int getPoints() {
        return points;
    }

    public void addPoints(int amount) {
        points += amount;
    }

    public void addMoney(double amount) {
        wallet += amount;
    }

    public void deductFromWallet(double amount) {
        if (wallet >= amount) {
            wallet -= amount;
        } else {
            System.out.println("Insufficient funds in wallet.");
        }
    }

    public double getWallet() {
        return wallet;
    }

    public void setMembership(boolean isMember, String membershipId) {
        this.isMember = isMember;
        this.membershipId = membershipId;
    }

    public String getMembershipId() {
        return membershipId;
    }

    public boolean isMember() {
        return isMember;
    }

    public String getName() {
        return name;
    }

    public double getWalletBalance() {
        return wallet;
    }

    public void addBill(Bill bill) {
        billHistory.add(bill);
    }

    public void viewBill(int billNumber) {
        for (int i = 0; i < billHistory.size(); i++) {
            Bill bill = billHistory.get(i);
            if (bill.getBillNumber() == billNumber) {
                System.out.println("Bill found: ");
                bill.printBillDetails();
                return;
            }
        }
        System.out.println("No bill found with number: " + billNumber);
    }

    public void viewAllBills() {
        if (billHistory.isEmpty()) {
            System.out.println("No bills found.");
        } else {
            for (int i = 0; i < billHistory.size(); i++) {
                Bill bill = billHistory.get(i);
                System.out.println(bill);
            }
        }
    }

    public static Customer findCustomerByMembership(List<Customer> customers, String membershipId) {
        for (Customer customer : customers) {
            if (customer.getMembershipId() != null && customer.getMembershipId().equals(membershipId)) {
                return customer;
            }
        }
        return null;
    }

    public void viewWallet(String membershipId) {
        if (this.membershipId != null && this.membershipId.equals(membershipId)) {
            System.out.println("Wallet balance for member " + membershipId + ": " + wallet);
            System.out.println("Points: " + points);
        } else {
            System.out.println("Invalid membership ID.");
        }
    }

    public Bill getLatestBill() {
        return billHistory.isEmpty() ? null : billHistory.get(billHistory.size() - 1);
    }

    // เมธอดสำหรับสมัครสมาชิกใหม่
    public void registerNewCustomer() {
        JSONParser parser = new JSONParser();
        try {
            // อ่านข้อมูลจาก data.json
            JSONObject data = (JSONObject) parser.parse(new FileReader("data.json"));

            // สร้าง JSONObject สำหรับลูกค้าใหม่
            JSONObject newCustomer = new JSONObject();
            newCustomer.put("name", this.name);
            newCustomer.put("wallet", this.wallet);
            newCustomer.put("points", this.points);
            newCustomer.put("isMember", this.isMember); // จะถูกตั้งเป็น true เสมอเมื่อสมัครสมาชิก
            newCustomer.put("membershipId", this.membershipId);

            // เพิ่มลูกค้าใหม่เข้าไปใน JSONArray customers
            JSONArray customersArray = (JSONArray) data.get("customers");
            if (customersArray == null) {
                customersArray = new JSONArray();
                data.put("customers", customersArray);
            }
            customersArray.add(newCustomer);

            // เขียนข้อมูลกลับไปยัง data.json
            try (FileWriter file = new FileWriter("data.json")) {
                file.write(data.toJSONString());
                file.flush();
            }

            System.out.println("Customer registered successfully!");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    // เมธอดสำหรับสร้าง Membership ID ใหม่
    private String generateMembershipId() {
        ArrayList<String> existingIds = new ArrayList<>();

        // อ่านข้อมูลลูกค้าเก่าเพื่อตรวจสอบ ID ที่มีอยู่
        JSONParser parser = new JSONParser();
        try {
            JSONObject data = (JSONObject) parser.parse(new FileReader("data.json"));
            JSONArray customersArray = (JSONArray) data.get("customers");

            if (customersArray != null) {
                for (Object obj : customersArray) {
                    JSONObject customer = (JSONObject) obj;
                    String existingId = (String) customer.get("membershipId");
                    if (existingId != null) {
                        existingIds.add(existingId);
                    }
                }
            }

            // สร้าง ID ใหม่
            int newId = existingIds.size() + 1; // นับจำนวนสมาชิกเพื่อสร้าง ID ใหม่
            return String.format("%04d", newId); // แปลงเป็น String แบบมี Leading Zeros เช่น 0001, 0002, ...

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        return null; // ถ้าหากเกิดข้อผิดพลาด จะคืนค่า null
    }

    // เมธอดช่วยในการแปลง List<Product> เป็น JSONArray
    private JSONArray productsToJsonArray(List<Product> products) {
        JSONArray jsonArray = new JSONArray();
        for (Product product : products) {
            JSONObject productJson = new JSONObject();
            productJson.put("name", product.getName());
            productJson.put("price", product.getPrice());
            productJson.put("quantity", product.getQuantity());
            jsonArray.add(productJson);
        }
        return jsonArray;
    }

    public static void handleCustomerRole(List<Customer> customers, Scanner scanner) {
        boolean customerRunning = true;
        String membershipId = ""; // ตัวแปรเก็บ membershipId

        while (customerRunning) {
            System.out.println("\n--- Customer Menu ---");
            System.out.println("1. View Bill History");
            System.out.println("2. Add Money to Wallet");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");
            int customerChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (customerChoice) {
                case 1:
                    System.out.print("Enter Bill Number: ");
                    String input = scanner.nextLine();
                    try {
                        int billNumber = Integer.parseInt(input);
                        Bill.findBillByNumber(billNumber); // เรียกฟังก์ชันค้นหาบิล
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid Bill Number. Please enter a valid number.");
                    }
                    break;
                case 2:
                    System.out.print("Enter your Membership ID: ");
                    membershipId = scanner.nextLine(); // กรอก membershipId
                    Customer customerToUpdate = Customer.findCustomerByMembership(customers, membershipId); // ค้นหาลูกค้า
                    if (customerToUpdate != null) {
                        System.out.print("Enter amount to add to wallet: ");
                        double amount = scanner.nextDouble();
                        customerToUpdate.addMoney(amount);
                        System.out.println("Money added successfully.");
                    } else {
                        System.out.println("Customer not found.");
                    }
                    break;
                case 3:
                    customerRunning = false;
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }
}
