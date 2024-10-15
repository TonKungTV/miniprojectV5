import java.util.List;

class ReceiptPrinter {
    public void printReceipt(Bill bill) {
        String storeName = "NextMart";
        String storeAddress = "123 Main St, City, Country";
        String storePhone = "Phone: 123-456-7890";
        
        String separator = "--------------------------------------";
        String lineSeparator = "======================================";
        
        System.out.printf("Bill Number: %-29s", bill.getBillNumber());
        System.out.println("\n" + lineSeparator);
        System.out.printf("%-20s %20s\n", storeName, " ");
        System.out.printf("%-20s %20s\n", storeAddress, " ");
        System.out.printf("%-20s %20s\n", storePhone, " ");
        System.out.println(lineSeparator);
        System.out.printf("Date: %-29s\n", bill.getDate());
        System.out.println(separator);
        System.out.println("Items:");
        
        // ดึงรายการสินค้าที่ถูกจัดกลุ่มจาก Bill
        List<GroupedProduct> groupedProducts = bill.groupProducts();
        
        // ใช้ for loop เพื่อพิมพ์สินค้าพร้อมจำนวนและราคาที่คำนวณรวมแล้ว
        for (int i = 0; i < groupedProducts.size(); i++) {
            GroupedProduct groupedProduct = groupedProducts.get(i);
            System.out.printf("%-10s %10d %15.2f\n", 
                groupedProduct.getProduct().getName(),  // ชื่อสินค้า
                groupedProduct.getQuantity(),           // จำนวนที่ซื้อ
                groupedProduct.getTotalPrice(null)      // ราคาทั้งหมดของสินค้านั้น
            );
        }

        // แสดงยอดรวมจำนวนสินค้าทั้งหมด
        System.out.printf("Total Items: %-25d\n", bill.getTotalQuantity());
        // พิมพ์ยอดรวมทั้งหมดของราคาสินค้า
        System.out.printf("Total Amount: %-25.2f\n", bill.getTotalAmount());
        System.out.println(separator);
        System.out.println("Thank you for shopping at " + storeName + "!");
        System.out.println(lineSeparator);
    }
}
