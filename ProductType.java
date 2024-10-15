public enum ProductType {
    SNACK, DRINK, FOOD; 

    public String getDescription() {
        switch (this) {
            case SNACK:
                return "A small item of food.";
            case DRINK:
                return "A liquid for drinking.";
            case FOOD:
                return "A substance for eating.";
            default:
                return "Unknown type.";
        }
    }
}
