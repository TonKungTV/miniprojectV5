import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;



class Bill {
    private int billNumber;
    private List<Product> products;
    private double totalAmount;
    private Date date;
    private Customer customer; // เพิ่มฟิลด์ลูกค้า


    public Bill(int billNumber, List<Product> products, double totalAmount, Date date) {
        this.billNumber = billNumber;
        this.products = products;
        this.totalAmount = totalAmount;
        this.date = date;
    }
    public Customer getCustomer() { // เมธอดเพื่อคืนค่าลูกค้า
        return customer;
    }

    public int getBillNumber() {
        return billNumber;
    }

    public List<Product> getProducts() {
        return products;
    }

    public double getTotalAmount() {
        return totalAmount; // ต้องให้ยอดรวมที่ถูกต้อง
    }

    public Date getDate() {
        return date;
    }

    public int getTotalQuantity() {
        int totalQuantity = 0;
        List<GroupedProduct> groupedProducts = groupProducts();
        
        for (GroupedProduct groupedProduct : groupedProducts) {
            totalQuantity += groupedProduct.getQuantity();  
        }
        return totalQuantity;
    }

    public List<GroupedProduct> groupProducts() {
        List<GroupedProduct> groupedProducts = new ArrayList<>();
        for (Product currentProduct : products) {
            boolean found = false;
            for (GroupedProduct groupedProduct : groupedProducts) {
                if (groupedProduct.getProduct().getName().equals(currentProduct.getName())) {
                    groupedProduct.addQuantity(1);
                    found = true;
                    break;
                }
            }
            if (!found) {
                groupedProducts.add(new GroupedProduct(currentProduct, 1));
            }
        }
        return groupedProducts;
    }
    
    public void printBillDetails() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'printBillDetails'");
    }
    public static void findBillByNumber(int billNumber) {
        JSONParser parser = new JSONParser();

        try {
            // อ่านข้อมูลจากไฟล์ data.json
            JSONObject data = (JSONObject) parser.parse(new FileReader("data.json"));
            JSONArray billsArray = (JSONArray) data.get("bills");

            // ค้นหาบิลที่มีหมายเลขตรงกัน
            for (Object obj : billsArray) {
                JSONObject bill = (JSONObject) obj;
                long billNumberInJson = (long) bill.get("billNumber"); // อ่านหมายเลขบิลจาก JSON

                // ตรวจสอบว่าหมายเลขบิลตรงกันหรือไม่
                if (billNumberInJson == billNumber) {
                    // แสดงรายละเอียดของบิล
                    displayBillDetails(bill);
                    return; // หยุดการค้นหาเมื่อเจอบิลที่ตรงกัน
                }
            }

            // ถ้าไม่พบหมายเลขบิล
            System.out.println("Bill not found.");

        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public static void displayBillDetails(JSONObject bill) {
        System.out.println("Bill Number: " + bill.get("billNumber"));
        System.out.println("Total Amount: " + bill.get("totalAmount"));
        System.out.println("Date: " + bill.get("date"));
        System.out.println("Membership ID: " + bill.get("membershipId"));

        // แสดงรายการสินค้าที่ซื้อ
        System.out.println("Items:");
        JSONArray products = (JSONArray) bill.get("products");
        for (Object obj : products) {
            JSONObject product = (JSONObject) obj;
            System.out.println("- " + product.get("name") + " (Quantity: " + product.get("quantity") + ", Price: "
                    + product.get("price") + ")");
        }
    }
}

