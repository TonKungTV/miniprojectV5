import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.util.Date;
import java.util.HashMap;

class PaymentProcessor {
    private static List<Integer> billNumbers = new ArrayList<>(); // ตัวเก็บหมายเลขบิลที่ถูกสร้างขึ้น
    private static int nonMemberCount = 0; // ตัวนับสำหรับ non-member

    public Bill processPayment(Manager manager,Customer customer, List<Product> products, boolean payWithWallet) {
        Map<Product, Integer> productQuantities = new HashMap<>(); // ใช้แผนที่เพื่อเก็บสินค้าพร้อมจำนวน
    
        double totalAmount = 0.0;
        int totalPoints = 0;

        // ตรวจสอบว่าลูกค้าเป็นสมาชิกหรือไม่
        String membershipId;
        if (customer.isMember()) {
            membershipId = customer.getMembershipId();
        } else {
            membershipId = createNonMemberId();
            System.out.println("Non-member detected. Membership ID assigned: " + membershipId);
        }
    
        // นับจำนวนของแต่ละสินค้า
        for (Product product : products) {
            double productPrice = customer.isMember() ? product.getPrice(customer) : product.getPrice();
            totalAmount += productPrice;

            // บันทึกจำนวนสินค้า
            productQuantities.put(product, productQuantities.getOrDefault(product, 0) + 1);
            totalPoints += productPrice;
        }
    
        if (customer.isMember()) {
            customer.addPoints(totalPoints);
            System.out.println("Earned " + totalPoints + " points!");
        } else {
            System.out.println("Non-members do not earn points.");
        }

        // ลดจำนวนสินค้าในสต็อก
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            product.reduceStock(quantity);
        }

        // จัดการการชำระเงิน
        if (payWithWallet) {
            if (customer.getWallet() >= totalAmount) {
                customer.deductFromWallet(totalAmount);
                System.out.println("Payment successful from Wallet!");
            } else {
                System.out.println("Insufficient funds in wallet.");
                return null;
            }
        } else {
            System.out.println("Payment successful with Cash!");
        }
    
        // สร้างบิล
        int billNumber = generateUniqueBillNumber(); // ใช้ฟังก์ชันเพื่อสร้างหมายเลขบิลที่ไม่ซ้ำ
        Bill bill = new Bill(billNumber, new ArrayList<>(productQuantities.keySet()), totalAmount, new Date());
        customer.addBill(bill);
        // สร้าง ReceiptPrinter และพิมพ์ใบเสร็จ
        ReceiptPrinter receiptPrinter = new ReceiptPrinter();
        receiptPrinter.printReceipt(bill);  // พิมพ์ใบเสร็จหลังชำระเงิน
        manager.addBill(bill); // Add bill to Manager's allBills
        
        // เพิ่ม membershipId ในการบันทึกบิล
        saveBillToFile(bill, customer, productQuantities, membershipId);
        return bill;
    }

    private String createNonMemberId() {
        nonMemberCount++; // เพิ่มจำนวน non-member
        return "non-member" + String.format("%03d", nonMemberCount); // คืนค่า ID เช่น non-member001
    }
    

    private int generateUniqueBillNumber() {
        int billNumber = billNumbers.size() + 1; // เริ่มต้นที่ 1
        while (billNumbers.contains(billNumber)) {
            billNumber++;
        }
        billNumbers.add(billNumber); // เพิ่มหมายเลขบิลลงใน List
        return billNumber;
    }

    public void saveBillToFile(Bill bill, Customer customer, Map<Product, Integer> productQuantities, String membershipId) {
        JSONParser parser = new JSONParser();
        try {
            JSONObject data = (JSONObject) parser.parse(new FileReader("data.json"));
    
            // Update products
            try {
                updateProductsInJson(data, productQuantities);
            } catch (Exception e) {
                System.out.println("Error updating products in JSON: " + e.getMessage());
                e.printStackTrace();
            }
    
            // Update customer
            try {
                updateCustomerInJson(data, customer);
            } catch (Exception e) {
                System.out.println("Error updating customer in JSON: " + e.getMessage());
                e.printStackTrace();
            }
    
            // Add bill
            try {
                addBillToJson(data, bill, productQuantities, membershipId);
            } catch (Exception e) {
                System.out.println("Error adding bill to JSON: " + e.getMessage());
                e.printStackTrace();
            }
    
            try (FileWriter file = new FileWriter("data.json")) {
                file.write(data.toJSONString());
                file.flush();
            } catch (IOException e) {
                System.out.println("Error writing to file: " + e.getMessage());
                e.printStackTrace();
            }
    
        } catch (IOException | ParseException e) {
            System.out.println("Error reading from data.json: " + e.getMessage());
            e.printStackTrace();
        }
    }


    private void updateProductsInJson(JSONObject data, Map<Product, Integer> productQuantities) {
        JSONArray productsArray = (JSONArray) data.get("products");
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            Product product = entry.getKey();
            int quantityToReduce = entry.getValue();

            for (Object obj : productsArray) {
                JSONObject jsonProduct = (JSONObject) obj;
                if (jsonProduct.get("name").equals(product.getName())) {
                    long currentQuantity = (long) jsonProduct.get("quantity");
                    jsonProduct.put("quantity", currentQuantity - quantityToReduce);
                    break;
                }
            }
        }
    }

    private void updateCustomerInJson(JSONObject data, Customer customer) {
        JSONArray customersArray = (JSONArray) data.get("customers");
        if (customersArray == null) {
            System.out.println("No customers found in JSON data.");
            return; // หยุดการทำงานถ้าไม่พบลูกค้า
        }
    
        boolean customerFound = false; // ใช้เพื่อตรวจสอบว่าพบลูกค้าหรือไม่
        for (Object obj : customersArray) {
            JSONObject jsonCustomer = (JSONObject) obj;
            if (jsonCustomer.get("name").equals(customer.getName())) {
                jsonCustomer.put("wallet", customer.getWallet());
                jsonCustomer.put("points", customer.getPoints());
    
                JSONObject newBill = new JSONObject();
                newBill.put("billNumber", customer.getLatestBill().getBillNumber());
                newBill.put("totalAmount", customer.getLatestBill().getTotalAmount());
                newBill.put("date", customer.getLatestBill().getDate().toString());
                customerFound = true; // พบลูกค้า
                break;
            }
        }
        if (!customerFound) {
            System.out.println("Customer not found in JSON data.");
        }
    }

    private void addBillToJson(JSONObject data, Bill bill, Map<Product, Integer> productQuantities, String membershipId) {
        JSONArray billsArray = (JSONArray) data.get("bills");
        if (billsArray == null) {
            billsArray = new JSONArray();
            data.put("bills", billsArray);
        }
    
        JSONObject newBill = new JSONObject();
        newBill.put("billNumber", bill.getBillNumber());
        newBill.put("totalAmount", bill.getTotalAmount());
        newBill.put("date", bill.getDate().toString());
        newBill.put("membershipId", membershipId); // เพิ่ม membershipId
    
        JSONArray billProducts = new JSONArray();
        for (Map.Entry<Product, Integer> entry : productQuantities.entrySet()) {
            Product product = entry.getKey();
            int quantity = entry.getValue();
            JSONObject billProduct = new JSONObject();
            billProduct.put("name", product.getName());
            billProduct.put("quantity", quantity);
            billProduct.put("price", product.getPrice(null)); // อาจจะส่ง null เพราะไม่ใช่สมาชิก
            billProducts.add(billProduct);
        }
        newBill.put("products", billProducts);
        billsArray.add(newBill);
    }
    
}
