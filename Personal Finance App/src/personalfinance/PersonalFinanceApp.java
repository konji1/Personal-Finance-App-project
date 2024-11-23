package personalfinance;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PersonalFinanceApp extends JFrame {
    private final int userId; // Logged-in user's ID
    private final List<TransactionBean> transactions = new ArrayList<>();
    private final JTextArea transactionArea = new JTextArea(); // Move to instance variable for updates

    public PersonalFinanceApp(int userId) {
        this.userId = userId;

        // Setup main application window
        setTitle("Personal Finance Manager");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create Components
        JPanel inputPanel = createInputPanel();
        JPanel buttonPanel = createButtonPanel();

        // Configure transactionArea
        transactionArea.setEditable(false);
        JScrollPane transactionScrollPane = new JScrollPane(transactionArea);

        // Add components to the main frame
        add(inputPanel, BorderLayout.NORTH);
        add(transactionScrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        // Load transactions for the user
        loadTransactions();

        // Show the window
        setVisible(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridLayout(2, 4, 10, 10));

        JLabel categoryLabel = new JLabel("Category:");
        JTextField categoryField = new JTextField();
        JLabel amountLabel = new JLabel("Amount:");
        JTextField amountField = new JTextField();
        JLabel typeLabel = new JLabel("Type:");
        JComboBox<String> typeComboBox = new JComboBox<>(new String[]{"Income", "Expense"});
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):");
        JTextField dateField = new JTextField();

        JButton submitButton = new JButton("Add Transaction");

        submitButton.addActionListener(e -> {
            try {
                String category = categoryField.getText().trim();
                double amount = Double.parseDouble(amountField.getText().trim());
                String type = (String) typeComboBox.getSelectedItem();
                String date = dateField.getText().trim();

                // Input validation
                if (category.isEmpty() || date.isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Category and Date fields cannot be empty.");
                    return;
                }

                addTransaction(category, amount, type, date);

                // Clear fields after successful addition
                categoryField.setText("");
                amountField.setText("");
                dateField.setText("");
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Please enter a valid number for the amount.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        });

        panel.add(categoryLabel);
        panel.add(categoryField);
        panel.add(amountLabel);
        panel.add(amountField);
        panel.add(typeLabel);
        panel.add(typeComboBox);
        panel.add(dateLabel);
        panel.add(dateField);

        panel.add(submitButton); // Add the submit button
        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();

        JButton pieChartButton = new JButton("Show Pie Chart");
        JButton barChartButton = new JButton("Show Bar Chart");

        pieChartButton.addActionListener(e -> showPieChart());
        barChartButton.addActionListener(e -> showBarChart());

        panel.add(pieChartButton);
        panel.add(barChartButton);

        return panel;
    }

    private void loadTransactions() {
        transactions.clear();
        StringBuilder sb = new StringBuilder("Your Transactions:\n");

        try (Connection conn = Database.connect()) {
            String sql = "SELECT * FROM transactions WHERE user_id = ?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                String category = rs.getString("category");
                double amount = rs.getDouble("amount");
                String type = rs.getString("type");
                String date = rs.getDate("date").toString();

                transactions.add(new TransactionBean(category, amount, type, date));
                sb.append(String.format("%s - %s: $%.2f on %s\n", type, category, amount, date));
            }
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error loading transactions: " + e.getMessage());
        }

        transactionArea.setText(sb.toString());
    }

    private void addTransaction(String category, double amount, String type, String date) {
        try (Connection conn = Database.connect()) {
            String sql = "INSERT INTO transactions (user_id, category, amount, type, date) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setInt(1, userId);
            pstmt.setString(2, category);
            pstmt.setDouble(3, amount);
            pstmt.setString(4, type);
            pstmt.setDate(5, Date.valueOf(date));

            pstmt.executeUpdate();

            // Refresh the UI
            loadTransactions();
            JOptionPane.showMessageDialog(this, "Transaction added successfully!");
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this, "Error adding transaction: " + e.getMessage());
        }
    }

    private void showPieChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        double income = 0, expenses = 0;

        for (TransactionBean transaction : transactions) {
            if ("Income".equals(transaction.getType())) {
                income += transaction.getAmount();
            } else if ("Expense".equals(transaction.getType())) {
                expenses += transaction.getAmount();
            }
        }

        dataset.setValue("Income", income);
        dataset.setValue("Expenses", expenses);

        JFreeChart chart = ChartFactory.createPieChart("Income vs Expenses", dataset, true, true, false);
        displayChart(chart, "Pie Chart");
    }

    private void showBarChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (TransactionBean transaction : transactions) {
            dataset.addValue(transaction.getAmount(), transaction.getType(), transaction.getCategory());
        }

        JFreeChart chart = ChartFactory.createBarChart("Transactions", "Category", "Amount", dataset);
        displayChart(chart, "Bar Chart");
    }

    private void displayChart(JFreeChart chart, String title) {
        ChartPanel chartPanel = new ChartPanel(chart);
        JFrame chartFrame = new JFrame(title);
        chartFrame.add(chartPanel);
        chartFrame.pack();
        chartFrame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginPage());
    }
}
