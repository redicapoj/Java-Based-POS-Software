package SupermarketSoftware;

import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.nio.file.*;
import java.text.*;
import java.util.*;
import java.util.List;

public class StatisticsSystem {
    public static void main(String[] args) {
        new StatisticsGUI();
    }
}

class StatisticsGUI {
    private JFrame frame;
    private JPanel billsDisplayPanel;
    private JPanel productStatsPanel;
    private StatisticsProductStore allPrintedProducts;

    public StatisticsGUI() {
        allPrintedProducts = new StatisticsProductStore();

        frame = new JFrame("Statistikat e marketit - Redi Capoj");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1200, 650);
        frame.setLocationRelativeTo(null);

        java.net.URL url = ClassLoader.getSystemResource("images/r-logo.png");
        if (url != null) {
            ImageIcon imgIcon = new ImageIcon(url);
            frame.setIconImage(imgIcon.getImage());
        } else {
            System.out.println("Icon image not found!");
        }

        JPanel panel = new JPanel(new BorderLayout());

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setPreferredSize(new Dimension(frame.getWidth(), 80));
        headerPanel.setBackground(Color.LIGHT_GRAY);

        JLabel tekstiImajte = new JLabel("Faturat e prera");
        tekstiImajte.setFont(new Font("Arial", Font.BOLD, 24));
        tekstiImajte.setBorder(BorderFactory.createEmptyBorder(10, 153, 10, 0));
        tekstiImajte.setHorizontalAlignment(JLabel.LEFT);

        JLabel tekstiIdjathte = new JLabel("Informacion mbi produktet");
        tekstiIdjathte.setFont(new Font("Arial", Font.BOLD, 24));
        tekstiIdjathte.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 125));
        tekstiIdjathte.setHorizontalAlignment(JLabel.LEFT);

        headerPanel.add(tekstiImajte, BorderLayout.WEST);
        headerPanel.add(tekstiIdjathte, BorderLayout.EAST);

        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JButton statsBtn = new JButton("Home");
        statsBtn.setBackground(Color.WHITE);
        statsBtn.setForeground(Color.BLACK);
        statsBtn.addActionListener(e -> {
            frame.dispose();
            SupermarketBillingSystem.main(null);
        });

        buttonPanel.add(statsBtn, BorderLayout.WEST);

        JPanel displayPanel = new JPanel(new GridLayout(1, 2));

        billsDisplayPanel = new JPanel();
        billsDisplayPanel.setLayout(new BoxLayout(billsDisplayPanel, BoxLayout.Y_AXIS));

        JScrollPane scrollPane = new JScrollPane(billsDisplayPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(250, 350));

        productStatsPanel = new JPanel();
        productStatsPanel.setLayout(new BoxLayout(productStatsPanel, BoxLayout.Y_AXIS));

        JScrollPane productScrollPane = new JScrollPane(productStatsPanel);
        productScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        productScrollPane.setPreferredSize(new Dimension(250, 350));

        displayPanel.add(scrollPane);
        displayPanel.add(productScrollPane);

        panel.add(headerPanel, BorderLayout.NORTH);
        panel.add(displayPanel, BorderLayout.CENTER);
        panel.add(buttonPanel, BorderLayout.SOUTH);

        frame.add(panel);

        frame.setVisible(true);

        loadBillsFromCSV(
                "C:\\Users\\User\\OneDrive\\Documents\\VisualStudio\\Java  programming\\src\\SupermarketSoftware\\printed-bills.csv");
        loadProductsFromCSV(
                "C:\\Users\\User\\OneDrive\\Documents\\VisualStudio\\Java  programming\\src\\SupermarketSoftware\\printed-bills.csv");

        displayProductStatistics();
    }

    private void loadBillsFromCSV(String filePath) {
        Map<String, List<String>> billsByDate = new TreeMap<>();
        Map<String, Double> salesByDate = new TreeMap<>();

        try (BufferedReader br = Files.newBufferedReader(Paths.get(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 6) {
                    String date = fields[5].split(" ")[0].trim();
                    String billId = fields[0].trim();
                    String product = fields[2].trim();
                    double price = Double.parseDouble(fields[3].trim());
                    int quantity = Integer.parseInt(fields[4].trim());
                    double totalPrice = price * quantity;

                    String billInfo = "Fatura " + billId + ":   " + product + ", Çmimi: " + price + ", Sasia: "
                            + quantity;
                    billsByDate.computeIfAbsent(date, k -> new ArrayList<>()).add(billInfo);

                    salesByDate.put(date, salesByDate.getOrDefault(date, 0.0) + totalPrice);
                }
            }
        } catch (IOException e) {
            JOptionPane.showMessageDialog(frame, "Error loading bills data: " + e.getMessage(), "Error",
                    JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }

        System.out.println("Dates read from CSV:");
        for (String date : billsByDate.keySet()) {
            System.out.println(date);
        }

        for (Map.Entry<String, List<String>> entry : billsByDate.entrySet()) {
            String date = entry.getKey();
            List<String> bills = entry.getValue();

            JLabel dateLabel = new JLabel(formatDate(date));
            JLabel separatorLabel = new JLabel(" ");
            dateLabel.setFont(new Font("Arial", Font.BOLD, 18));
            billsDisplayPanel.add(separatorLabel);
            billsDisplayPanel.add(dateLabel);

            for (String bill : bills) {
                JLabel billLabel = new JLabel(bill);
                billLabel.setToolTipText(bill);
                billsDisplayPanel.add(billLabel);
            }

            double totalSalesForDate = salesByDate.get(date);
            JLabel totalSalesLabel = new JLabel("Shitje Totale: " + totalSalesForDate + " LEK");
            totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 16));
            billsDisplayPanel.add(totalSalesLabel);
            billsDisplayPanel.add(Box.createVerticalStrut(10));
        }

        billsDisplayPanel.revalidate();
        billsDisplayPanel.repaint();
    }

    private void loadProductsFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(",");
                if (values.length == 6) {
                    String barcode = values[1].trim();
                    String name = values[2].trim();
                    double price = Double.parseDouble(values[3].trim());
                    int quantity = Integer.parseInt(values[4].trim());
                    StatisticsProduct product = new StatisticsProduct(barcode, name, price, quantity);
                    allPrintedProducts.addProduct(product);
                    System.out.println("Loaded product: " + product.getName() + ", Quantity: " + product.getQuantity()
                            + ", Total Price: " + product.getTotalPrice());
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Lidhja me databazen (CSV) nuk u krye.", "File Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void displayProductStatistics() {
        double totalSales = 0;
        List<StatisticsProduct> products = allPrintedProducts.getProducts();

        String productWithMaxQuantity = null;
        int maxQuantity = 0;
        String productWithMinQuantity = null;
        int minQuantity = 9999999;

        for (StatisticsProduct product : products) {

            JLabel productLabel = new JLabel("Produkt: " + product.getName() + " - Sasia: " + product.getQuantity()
                    + " - Cmimi: " + product.getTotalPrice() + " LEK");
            productStatsPanel.add(productLabel);

            totalSales += product.getTotalPrice();

            if (product.getQuantity() > maxQuantity) {
                maxQuantity = product.getQuantity();
                productWithMaxQuantity = product.getName();
            }
            if (product.getQuantity() < minQuantity) {
                minQuantity = product.getQuantity();
                productWithMinQuantity = product.getName();
            }

            productStatsPanel.add(Box.createVerticalStrut(10));
        }

        productStatsPanel.add(Box.createVerticalStrut(10));

        JLabel totalSalesLabel = new JLabel("Shitje Totale: " + totalSales + " LEK");
        totalSalesLabel.setFont(new Font("Arial", Font.BOLD, 18));
        productStatsPanel.add(totalSalesLabel);

        productStatsPanel.add(Box.createVerticalStrut(10));

        double avgSales = getAverageTotalPerDay();
        String formattedAvgSales = String.format("%.2f", avgSales);

        JLabel avgSalesPerDay = new JLabel("Shitje Totale ne dite mesatarisht: " + formattedAvgSales + " LEK");
        avgSalesPerDay.setFont(new Font("Arial", Font.BOLD, 18));
        productStatsPanel.add(avgSalesPerDay);

        if (productWithMaxQuantity != null) {
            productStatsPanel.add(Box.createVerticalStrut(10));
            JLabel maxQuantityLabel = new JLabel(
                    "Produkti me i shitur: " + productWithMaxQuantity + " - Sasia: " + maxQuantity);
            maxQuantityLabel.setFont(new Font("Arial", Font.BOLD, 18));
            productStatsPanel.add(maxQuantityLabel);
        }
        if (productWithMinQuantity != null) {
            productStatsPanel.add(Box.createVerticalStrut(10));
            JLabel minQuantityLabel = new JLabel(
                    "Produkti me pak i shitur: " + productWithMinQuantity + " - Sasia: " + minQuantity);
            minQuantityLabel.setFont(new Font("Arial", Font.BOLD, 18));
            productStatsPanel.add(minQuantityLabel);
        }

        productStatsPanel.revalidate();
        productStatsPanel.repaint();
    }

    private int countNumberOfDays() {
        String filePath = "C:\\Users\\User\\OneDrive\\Documents\\VisualStudio\\Java - programming\\src\\SupermarketSoftware\\printed-bills.csv";
        Set<String> uniqueDates = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] fields = line.split(",");
                if (fields.length >= 6) {
                    String date = fields[5].trim().split(" ")[0];
                    uniqueDates.add(date);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return uniqueDates.size();
    }

    private double getAverageTotalPerDay() {
        double totalSales = 0;
        List<StatisticsProduct> products = allPrintedProducts.getProducts();

        for (StatisticsProduct product : products) {
            totalSales += product.getTotalPrice();
        }

        return totalSales / countNumberOfDays();
    }

    private String formatDate(String inputDate) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("dd-MM-yyyy");
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMMM, yyyy");
            Date date = inputFormat.parse(inputDate);
            return outputFormat.format(date);
        } catch (ParseException e) {
            return inputDate;
        }
    }
}

class StatisticsProductStore {
    private Map<String, StatisticsProduct> products;

    public StatisticsProductStore() {
        products = new HashMap<>();
    }

    public void addProduct(StatisticsProduct product) {
        String barcode = product.getBarcode();
        if (products.containsKey(barcode)) {
            StatisticsProduct existingProduct = products.get(barcode);
            existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());
            existingProduct.setPrice(product.getPrice());
        } else {
            products.put(barcode, product);
        }
    }

    public List<StatisticsProduct> getProducts() {
        return new ArrayList<>(products.values());
    }
}

class StatisticsProduct {
    private String barcode;
    private String name;
    private double price;
    private int quantity;

    public StatisticsProduct(String barcode, String name, double price, int quantity) {
        this.barcode = barcode;
        this.name = name;
        this.price = price;
        this.quantity = quantity;
    }

    public String getBarcode() {
        return barcode;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalPrice() {
        return price * quantity;
    }
}
