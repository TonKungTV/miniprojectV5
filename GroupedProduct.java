
class GroupedProduct {
    private Product product;
    private int quantity;

    public GroupedProduct(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int amount) {
        quantity += amount;
    }

    public double getTotalPrice(Customer customer) {
        return product.getPrice(customer) * quantity; // คำนวณราคาทั้งหมด
    }
}