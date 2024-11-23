package personalfinance;

public class TransactionBean {
    private String category;
    private double amount;
    private String type;
    private String date;

    public TransactionBean(String category, double amount, String type, String date) {
        this.category = category;
        this.amount = amount;
        this.type = type;
        this.date = date;
    }

    public String getCategory() { return category; }
    public double getAmount() { return amount; }
    public String getType() { return type; }
    public String getDate() { return date; }
}

