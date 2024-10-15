
class Product {
    private String name;
    private double price;
    private int quantity;
    private ProductType type;

    public Product(String name, double price, int quantity, ProductType type) {
        this.name = name;
        this.price = price;
        this.quantity = quantity;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public int getQuantity() {
        return quantity;
    }

    public ProductType getType() {
        return type;
    }
    public double getPrice(Customer customer) {
        if (customer != null && customer.isMember()) {
            return price * 0.95; // ลดราคา 5% สำหรับสมาชิก
        }
        return price; // ไม่ใช่สมาชิกหรือ customer เป็น null
    }
    public double getPrice() {
        return price; // คืนค่าราคา
    }
    public void reduceStock(int amount) {
        if (quantity >= amount) {
            quantity -= amount;
        } else {
            System.out.println("Insufficient stock for product: " + name);
        }
    }

    @Override
    public String toString() {
        return name + " (" + type + ") - Price: " + price + " Quantity: " + quantity;
    }
}