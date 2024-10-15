import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;

class Manager {
    private String name;
    private int nextBillNumber;
    private List<Bill> billHistory = new ArrayList<>();
    private List<Bill> allBills = new ArrayList<>();
    private List<Product> products = new ArrayList<>();

    public Manager(String name) {
        this.name = name;
        this.products = new ArrayList<>();
        this.allBills = new ArrayList<>(); // Initialize the list
        this.nextBillNumber = 1;
        loadProductsFromJson("data.json"); // โหลดผลิตภัณฑ์เมื่อสร้าง Manager
    }

    private void loadProductsFromJson(String filename) {
        JSONParser parser = new JSONParser();
        try (Reader reader = new FileReader(filename)) {
            JSONObject jsonObject = (JSONObject) parser.parse(reader);
            JSONArray productsArray = (JSONArray) jsonObject.get("products");

            for (Object obj : productsArray) {
                JSONObject productJson = (JSONObject) obj;
                String name = (String) productJson.get("name");
                double price = (double) productJson.get("price");
                long quantity = (long) productJson.get("quantity");
                String type = (String) productJson.get("type");

                ProductType productType = ProductType.valueOf(type);
                Product product = new Product(name, price, (int) quantity, productType);
                products.add(product);
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public void addBill(Bill bill) {
        allBills.add(bill);
    }

    public List<Bill> getAllBills() {
        return allBills;
    }

    public void viewAllBills() {
        for (Bill bill : billHistory) {
            System.out.println(bill);
        }
    }

    public void viewBill(int billNumber) {
        for (Bill bill : billHistory) {
            if (bill.getBillNumber() == billNumber) {
                System.out.println(bill);
                return;
            }
        }
        System.out.println("Bill not found.");
    }

    public List<Product> getProducts() {
        return products;
    }

    public void displayProducts() {
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

    public void viewIncome(String period) {
        double totalIncome = 0.0;
        Date now = new Date();

        // ฟอร์แมตวันเวลา
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat weekFormat = new SimpleDateFormat("yyyy-ww");
        SimpleDateFormat monthFormat = new SimpleDateFormat("yyyyMM");
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");

        // เก็บวันเวลาปัจจุบัน
        String currentDate = sdf.format(now);
        String currentWeek = weekFormat.format(now);
        String currentMonth = monthFormat.format(now);
        String currentYear = yearFormat.format(now);

        for (Bill bill : allBills) {
            String billDate = sdf.format(bill.getDate());
            String billWeek = weekFormat.format(bill.getDate());
            String billMonth = monthFormat.format(bill.getDate());
            String billYear = yearFormat.format(bill.getDate());

            switch (period.toLowerCase()) {
                case "daily":
                    if (billDate.equals(currentDate)) {
                        totalIncome += bill.getTotalAmount();
                    }
                    break;
                case "weekly":
                    if (billWeek.equals(currentWeek)) {
                        totalIncome += bill.getTotalAmount();
                    }
                    break;
                case "monthly":
                    if (billMonth.equals(currentMonth)) {
                        totalIncome += bill.getTotalAmount();
                    }
                    break;
                case "yearly":
                    if (billYear.equals(currentYear)) {
                        totalIncome += bill.getTotalAmount();
                    }
                    break;
                default:
                    System.out.println("Invalid period. Choose from daily, weekly, monthly, yearly.");
                    return;
            }
        }

        System.out.println("Total " + period + " income: " + totalIncome);
    }

    public int getNextBillNumber() {
        return nextBillNumber++;
    }

    public void addProduct(Scanner scanner) {
        // รับข้อมูลจากผู้ใช้
        System.out.print("Enter Product Name: ");
        String name = scanner.nextLine();
        System.out.print("Enter Product Price: ");
        double price = scanner.nextDouble();
        System.out.print("Enter Product Quantity: ");
        int quantity = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        System.out.print("Enter Product Type (SNACK, DRINK, FOOD): ");
        String type = scanner.nextLine();

        // แปลงค่า ProductType และตรวจสอบความถูกต้อง
        try {
            ProductType productType = ProductType.valueOf(type.toUpperCase());
            // เพิ่มสินค้าลงในระบบ
            Product newProduct = new Product(name, price, quantity, productType);
            products.add(newProduct);
            saveProductToJson(newProduct);
            System.out.println("Product added successfully.");
        } catch (IllegalArgumentException e) {
            System.out.println("Invalid Product Type. Please enter a valid type (SNACK, DRINK, FOOD).");
        }
    }

    public void deleteProduct(String productName) {
        boolean productFound = false;

        // ค้นหาผลิตภัณฑ์ที่ต้องการลบ
        for (int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (product.getName().equalsIgnoreCase(productName)) {
                products.remove(i); // ลบผลิตภัณฑ์จากรายการ
                productFound = true;
                System.out.println("Product " + productName + " has been deleted successfully.");
                break;
            }
        }

        if (!productFound) {
            System.out.println("Product " + productName + " not found.");
        } else {
            // อัปเดตไฟล์ data.json เพื่อให้ตรงกับการเปลี่ยนแปลงใน products
            updateProductsInJson();
        }
    }

    public void addProduct(Product product) {
        saveProductToJson(product);
    }

    @SuppressWarnings("unchecked")
    private void updateProductsInJson() {
        JSONParser parser = new JSONParser();
        JSONObject data = new JSONObject();
        JSONArray productsArray = new JSONArray();
        JSONArray billsArray = new JSONArray();
        JSONArray customersArray = new JSONArray();

        try {
            // อ่านข้อมูลเดิมจาก data.json
            try (Reader reader = new FileReader("data.json")) {
                data = (JSONObject) parser.parse(reader);
            }

            // เก็บข้อมูล bills และ customers ในตัวแปรชั่วคราว
            billsArray = (JSONArray) data.get("bills");
            customersArray = (JSONArray) data.get("customers");

            // เพิ่มผลิตภัณฑ์ใหม่ลงใน productsArray
            for (Product product : products) {
                JSONObject productJson = new JSONObject();
                productJson.put("name", product.getName());
                productJson.put("price", product.getPrice(null));
                productJson.put("quantity", product.getQuantity());
                productJson.put("type", product.getType().toString());
                productsArray.add(productJson);
            }

            // เพิ่ม arrays ไปยัง JSONObject
            data.put("products", productsArray);
            data.put("bills", billsArray); // ใส่ bills กลับเข้าไป
            data.put("customers", customersArray); // ใส่ customers กลับเข้าไป

            // เขียนข้อมูลกลับไปยัง data.json
            try (FileWriter file = new FileWriter("data.json")) {
                file.write(data.toJSONString());
                file.flush();
                System.out.println("Products updated in data.json successfully.");
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void saveProductToJson(Product product) {
        JSONParser parser = new JSONParser();
        try {
            // อ่านไฟล์ data.json ที่มีอยู่แล้ว
            JSONObject data = (JSONObject) parser.parse(new FileReader("data.json"));
            JSONArray productsArray = (JSONArray) data.get("products");

            // ตรวจสอบว่า productsArray มีอยู่หรือไม่ ถ้าไม่มีให้สร้างใหม่
            if (productsArray == null) {
                productsArray = new JSONArray();
            }

            // สร้าง JSONObject สำหรับสินค้าใหม่
            JSONObject newProduct = new JSONObject();
            newProduct.put("name", product.getName());
            newProduct.put("price", product.getPrice(null));
            newProduct.put("quantity", product.getQuantity());
            newProduct.put("type", product.getType().toString());

            // เพิ่มสินค้าใหม่ลงใน productsArray
            productsArray.add(newProduct);

            // ใส่ productsArray กลับไปใน JSONObject หลัก
            data.put("products", productsArray);

            // เขียนข้อมูลกลับไปยัง data.json
            try (FileWriter file = new FileWriter("data.json")) {
                file.write(data.toJSONString());
                file.flush();
            }

            System.out.println("Product added and saved to data.json successfully.");
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }
    public static void handleManagerRole(Manager manager, Scanner scanner) {
        boolean managerRunning = true;
        while (managerRunning) {
            System.out.println("\n--- Manager Menu ---");
            System.out.println("1. Add Product");
            System.out.println("2. Delete Product");
            System.out.println("3. View Products");
            System.out.println("4. View Income");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");
            int managerChoice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (managerChoice) {
                case 1:
                    manager.addProduct(scanner);
                    break;
                case 2:
                    manager.displayProducts();
                    System.out.print("Enter the name of the product to delete: ");
                    String productName = scanner.nextLine();
                    manager.deleteProduct(productName);
                    break;
                case 3:
                    manager.displayProducts();
                    break;
                case 4:
                    viewIncome(manager, scanner);
                    break;
                case 5:
                    managerRunning = false; // Exit manager menu
                    break;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void viewIncome(Manager manager, Scanner scanner) {
        System.out.println("Select the period for income view:");
        System.out.println("1. Daily");
        System.out.println("2. Weekly");
        System.out.println("3. Monthly");
        System.out.println("4. Yearly");
        System.out.print("Choose an option: ");
        int viewIncomeChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline
        switch (viewIncomeChoice) {
            case 1:
                manager.viewIncome("daily");
                break;
            case 2:
                manager.viewIncome("weekly");
                break;
            case 3:
                manager.viewIncome("monthly");
                break;
            case 4:
                manager.viewIncome("yearly");
                break;
            default:
                System.out.println("Invalid option. Please try again.");
                break;
        }
    }
}
