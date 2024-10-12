package SupermarketSoftware;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SupermarketBillingSystem {
    public static void main(String[] args) {
        new GUI();
    }
}

class GUI {
    private JFrame frame;
    private JPanel productDisplayPanel;
    private JTextField shtoProdField; 
    private JTextField shtoCmimField; 
    private JTextField shtoSasiField;
    private ProductStore productStore; 
    private ProductStore testStore; 
    private JLabel totalSum;
    private int clicked;
    private String password = "1234";

    public GUI() {

        productStore = new ProductStore();
        testStore = new ProductStore();

        loadProductsFromCSV(
                "C:\\Users\\User\\OneDrive\\Documents\\VisualStudio\\Java  programming\\src\\SupermarketSoftware\\products.csv");

        try {
            clicked = readClickedFromFile();
        } catch (IOException e) {
            e.printStackTrace();
            clicked = 0; 
            JOptionPane.showMessageDialog(null, "Failed to read previous entry ID. Defaulting to 0.", "Error",
                    JOptionPane.ERROR_MESSAGE);
        }

        
        frame = new JFrame("Software per menaxhimin e marketit - Redi Capoj");
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

        
        JLabel tekstiImajte = new JLabel("Kerkimi/Shtimi i produkteve");
        tekstiImajte.setFont(new Font("Arial", Font.BOLD, 24));
        tekstiImajte.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 0)); 
        tekstiImajte.setHorizontalAlignment(JLabel.LEFT);

        
        JButton statsBtn = new JButton("Stats");
        statsBtn.setPreferredSize(new Dimension(150, 75));
        statsBtn.setFont(new Font("Arial", Font.BOLD, 24));
        statsBtn.setFocusPainted(false);
        statsBtn.setContentAreaFilled(false);
        statsBtn.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 55));
        statsBtn.setHorizontalAlignment(JLabel.LEFT);

        statsBtn.addActionListener(e -> {
            boolean isUserValid = openIfUserValid();
            if (isUserValid) {
                frame.dispose(); 
                StatisticsSystem.main(null);
            } else {
                JOptionPane.showMessageDialog(frame, "Passwordi nuk eshte i sakte.", "Password i gabuar",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        
        headerPanel.add(tekstiImajte, BorderLayout.WEST);
        headerPanel.add(statsBtn, BorderLayout.EAST);

        
        JPanel searchPanel = new JPanel();
        searchPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 0, 5));

        JLabel textInput = new JLabel("Kerko: ");
        textInput.setFont(new Font("Arial", Font.PLAIN, 14));

        JTextField search = new JTextField(20);
        search.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton kerkoBtn = new JButton("Kerko");
        frame.getRootPane().setDefaultButton(kerkoBtn);
        kerkoBtn.addActionListener(e -> {
            String barcode = search.getText();
            if (!barcode.isEmpty()) {
                searchByBarcode(barcode, testStore);
                search.setText("");
            } else {
                JOptionPane.showMessageDialog(frame, "Ju lutemi shtoni barkodin për kërkim.", "Gabim në input",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        searchPanel.add(textInput);
        searchPanel.add(search);
        searchPanel.add(kerkoBtn);

        
        JPanel manualtxt = new JPanel();
        manualtxt.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
        manualtxt.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        JLabel shtoEmrProd = new JLabel("Produkti: ");
        shtoEmrProd.setFont(new Font("Arial", Font.PLAIN, 14));

        shtoProdField = new JTextField(20);
        shtoProdField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel shtoCmimProd = new JLabel("Cmimi: ");
        shtoCmimProd.setFont(new Font("Arial", Font.PLAIN, 14));

        shtoCmimField = new JTextField(20);
        shtoCmimField.setFont(new Font("Arial", Font.PLAIN, 14));

        JLabel shtoSasi = new JLabel("Sasia: ");
        shtoSasi.setFont(new Font("Arial", Font.PLAIN, 14));

        shtoSasiField = new JTextField(20);
        shtoSasiField.setFont(new Font("Arial", Font.PLAIN, 14));

        JButton shtoButton = new JButton("Shto");
        shtoButton.addActionListener(e -> {
            String productName = shtoProdField.getText();
            String capotalizedProductName = productName.substring(0, 1).toUpperCase() + productName.substring(1);
            String productPriceText = shtoCmimField.getText();
            String productQuantityText = shtoSasiField.getText();

            if (productName.isEmpty() || productPriceText.isEmpty()) {
                JOptionPane.showMessageDialog(frame, "Ju lutemi shtoni emrin dhe çmimin e produktit.", "Gabim në input",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int productQuantity = Integer.parseInt(productQuantityText);
                double productPrice = Double.parseDouble(productPriceText);
                if (productPrice < 0 || productQuantity <= 0) {
                    throw new NumberFormatException();
                }
                Product product = new Product(String.valueOf(System.currentTimeMillis()), capotalizedProductName,
                        productPrice, productQuantity); 
                productStore.addProduct(product);

                refreshProductDisplay();

                shtoProdField.setText("");
                shtoCmimField.setText("");
                shtoSasiField.setText("");

                double totalPrice = productStore.calculateTotal();
                totalSum.setText(String.format("%.2fL", totalPrice)); 
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(frame, "Ju lutemi shtoni një çmim të vlefshëm.", "Gabim në input",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        manualtxt.add(shtoEmrProd);
        manualtxt.add(shtoProdField);
        manualtxt.add(shtoCmimProd);
        manualtxt.add(shtoCmimField);
        manualtxt.add(shtoSasi);
        manualtxt.add(shtoSasiField);
        manualtxt.add(shtoButton);

        
        JPanel AddingPanel = new JPanel(new BorderLayout());
        AddingPanel.add(searchPanel, BorderLayout.NORTH);
        AddingPanel.add(manualtxt, BorderLayout.CENTER);

        
        productDisplayPanel = new JPanel();
        productDisplayPanel.setLayout(new BoxLayout(productDisplayPanel, BoxLayout.Y_AXIS));

        
        JScrollPane scrollPane = new JScrollPane(productDisplayPanel);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        scrollPane.setPreferredSize(new Dimension(500, 350)); 

        
        JPanel headerDisplayPanel = new JPanel(new GridLayout(1, 4));
        headerDisplayPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 25));
        headerDisplayPanel.add(new JLabel("Barcode"));
        headerDisplayPanel.add(new JLabel("Emertimi"));
        headerDisplayPanel.add(new JLabel("Cmimi"));
        headerDisplayPanel.add(new JLabel("Sasia"));
        headerDisplayPanel.add(new JLabel("Totali"));

        
        JPanel displayPanel = new JPanel(new BorderLayout());
        displayPanel.add(headerDisplayPanel, BorderLayout.NORTH);
        displayPanel.add(scrollPane, BorderLayout.CENTER);

        AddingPanel.add(displayPanel, BorderLayout.SOUTH);

        
        JPanel buttonPanel = new JPanel(new BorderLayout());
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        
        JPanel leftButtonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 10));
        JButton fshi = new JButton("Fshi");
        fshi.setBackground(Color.WHITE);
        fshi.setForeground(Color.BLACK);
        fshi.addActionListener(e -> {
            int lastIndex = productStore.getProductList().size() - 1;
            if (lastIndex >= 0) {
                productStore.getProductList().remove(lastIndex);
                refreshProductDisplay();

                double totalPrice = productStore.calculateTotal();
                totalSum.setText(String.format("%.2fL", totalPrice)); 
            }
        });

        JButton fshiTeGjitha = new JButton("Fshi te gjitha");
        fshiTeGjitha.setBackground(Color.RED);
        fshiTeGjitha.setForeground(Color.WHITE);
        fshiTeGjitha.addActionListener(e -> {
            productStore.getProductList().clear();
            refreshProductDisplay();

            totalSum.setText("0.00L");
        });

        leftButtonPanel.add(fshi);
        leftButtonPanel.add(fshiTeGjitha);

        
        JPanel rightButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 10));

        JButton printoBtn = new JButton("Printo");
        printoBtn.setBackground(Color.BLUE);
        printoBtn.setForeground(Color.WHITE);
        printoBtn.addActionListener(e -> {
            printInvoice(productStore);
            try {
                clicked++;
                addPrintedBillsToCSV();
                saveCurrentEntryID(clicked); 
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(frame, "Fatura nuk u ruajt ne CSV.", "File Save Error",
                        JOptionPane.ERROR_MESSAGE);
            }
            System.out.println(clicked);
        });

        JButton totalBtn = new JButton("Totali");
        totalBtn.setBackground(Color.GREEN);
        totalBtn.setForeground(Color.WHITE);
        totalBtn.addActionListener(e -> {
            double totalPrice = productStore.calculateTotal();
            totalSum.setText(String.format("%.2fL", totalPrice)); 
        });

        totalSum = new JLabel("0.00L");
        totalSum.setFont(new Font("Arial", Font.BOLD, 24));

        rightButtonPanel.add(totalBtn);
        rightButtonPanel.add(printoBtn);
        rightButtonPanel.add(totalSum);

        buttonPanel.add(leftButtonPanel, BorderLayout.WEST);
        buttonPanel.add(rightButtonPanel, BorderLayout.EAST);

        
        panel.add(headerPanel, BorderLayout.NORTH);

        
        panel.add(AddingPanel, BorderLayout.CENTER);

        
        panel.add(buttonPanel, BorderLayout.SOUTH);

        
        frame.add(panel);

        
        frame.setVisible(true);
    }

    private void loadProductsFromCSV(String filePath) {
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] values = line.split(","); 
                if (values.length == 4) {
                    String barcode = values[0].trim();
                    String name = values[1].trim();
                    double price = Double.parseDouble(values[2].trim());
                    int quantity = Integer.parseInt(values[3].trim());
                    Product product = new Product(barcode, name, price, quantity);
                    testStore.addProduct(product);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(frame, "Failed to load products from CSV file.", "File Load Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addPrintedBillsToCSV() throws IOException {
        ArrayList<Product> products = productStore.getProductList();
        File printedBillsCSV = new File(
                "C:\\Users\\User\\OneDrive\\Documents\\VisualStudio\\Java  programming\\src\\SupermarketSoftware\\printed-bills.csv");

        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm"));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(printedBillsCSV, true))) {
            for (Product product : products) {
                writer.write(String.format("%d,%s,%s,%.2f,%d,%s%n", clicked, product.getBarcode(), product.getName(),
                        product.getPrice(), product.getQuantity(), currentDate));
            }
        }
    }

    private void saveCurrentEntryID(int clicked) throws IOException {
        String path = "Java  programming\\src\\SupermarketSoftware\\clicked-counting.txt";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(path))) {
            writer.write(String.valueOf(clicked));
        }
    }

    private static int readClickedFromFile() throws IOException {
        String path = "Java  programming\\src\\SupermarketSoftware\\clicked-counting.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line = reader.readLine();
            if (line != null) {
                try {
                    return Integer.parseInt(line.trim());
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(null, "Nuk u realizua leximi i ID te meparshme.", "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
        return 0; 
    }

    private void refreshProductDisplay() {
        productDisplayPanel.removeAll();

        ArrayList<Product> products = productStore.getProductList();
        for (Product product : products) {
            double totalProd = product.getPrice() * product.getQuantity();
            JPanel productPanel = new JPanel(new GridLayout(1, 4));
            productPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 5));
            productPanel.add(new JLabel(product.getBarcode()));
            productPanel.add(new JLabel(product.getName()));
            productPanel.add(new JLabel(String.format("%.2fL", product.getPrice())));
            productPanel.add(new JLabel(String.valueOf(product.getQuantity())));
            productPanel.add(new JLabel(String.format("%.2fL", totalProd)));

            productDisplayPanel.add(productPanel);
        }

        productDisplayPanel.revalidate();
        productDisplayPanel.repaint();
    }

    private void searchByBarcode(String barcode, ProductStore productStore) {
        Product product = productStore.findProduct(barcode);
        if (product != null) {
            Product existingProduct = this.productStore.findProduct(barcode);
            if (existingProduct != null) {
                existingProduct.setQuantity(existingProduct.getQuantity() + 1);
            } else {
                this.productStore
                        .addProduct(new Product(product.getBarcode(), product.getName(), product.getPrice(), 1));
            }
            refreshProductDisplay();

            double totalPrice = this.productStore.calculateTotal();
            totalSum.setText(String.format("%.2fL", totalPrice)); 
        } else {
            JOptionPane.showMessageDialog(frame, "Produkti nuk u gjet.", "Kërkimi i produktit",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private boolean openIfUserValid() {
        JPanel panel = new JPanel(new GridLayout(0, 1));
        JLabel label = new JLabel("Vendosni paswordin:");
        JPasswordField passwordField = new JPasswordField(20);
        panel.add(label);
        panel.add(passwordField);

        int option = JOptionPane.showConfirmDialog(frame, panel, "Password Input", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE);

        if (option == JOptionPane.OK_OPTION) {
            char[] password = passwordField.getPassword();
            if (String.valueOf(password).equals(this.password)) {
                return true;
            }
        }
        return false;
    }

    private void printInvoice(ProductStore store) {
        PrinterJob printerJob = PrinterJob.getPrinterJob();
        printerJob.setJobName("Print Invoice");

        
        PageFormat pageFormat = printerJob.defaultPage();
        Paper paper = pageFormat.getPaper();

        
        double margin = 10; 
        paper.setImageableArea(margin, margin, paper.getWidth() - 2 * margin, paper.getHeight() - 2 * margin);
        pageFormat.setPaper(paper);

        printerJob.setPrintable(new InvoicePrintable(store), pageFormat);

        
        try {
            printerJob.print();
        } catch (PrinterException ex) {
            JOptionPane.showMessageDialog(frame, "Printimi i faturës dështoi: " + ex.getMessage(), "Gabim në printim",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}

class InvoicePrintable implements Printable {
    private ProductStore productStore;

    public InvoicePrintable(ProductStore productStore) {
        this.productStore = productStore;
    }

    @Override
    public int print(Graphics g, PageFormat pageFormat, int pageIndex) {
        if (pageIndex > 0) {
            return NO_SUCH_PAGE;
        }

        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

        int x = 10; 
        int y = 15; 

        int printableWidth = (int) pageFormat.getImageableWidth();
        FontMetrics fm = g.getFontMetrics();
        int stringWidth = fm.stringWidth("MARKET ORIKUM"); 
        int centerX = printableWidth / 2 - stringWidth / 2; 

        
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14)); 
        g.drawString("MARKET ORIKUM", centerX, y);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 12)); 

        
        y += fm.getHeight();
        g.drawString("", centerX, y);
        y += fm.getHeight();
        g.drawString("NIPT: K56F403201F", centerX, y);
        y += fm.getHeight();
        g.drawString("    Data/Ora: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")), x,
                y);
        y += fm.getHeight();
        g.drawString("     Kodi operatorit: yv791xd353", x, y);
        y += fm.getHeight();
        g.drawString("Menyra e pageses: Para ne dore", x, y);
        y += fm.getHeight();

        g.drawLine(x, y += 10, (int) pageFormat.getImageableWidth() - 10, y);

        
        for (Product product : productStore.getProductList()) {
            y += fm.getHeight();
            g.drawString("Produkti: " + product.getName(), x, y);
            y += fm.getHeight();
            g.drawString("Sasia: " + product.getQuantity(), x, y);
            y += fm.getHeight();
            g.drawString("Cmimi: " + String.format("%.2fL", product.getPrice()), x, y);
            g.drawLine(x, y += 5, (int) pageFormat.getImageableWidth() - 10, y);
        }

        
        double totalPrice = productStore.calculateTotal();
        y += fm.getHeight();
        g.setFont(g.getFont().deriveFont(Font.BOLD, 14)); 
        y += fm.getHeight();
        g.drawString("TOTAL LEK: " + String.format("%.2fL", totalPrice), x, y);
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 12)); 
        y += fm.getHeight();
        g.drawLine(x, y += 10, (int) pageFormat.getImageableWidth() - 10, y);
        y += fm.getHeight();
        g.drawString("Pa TVSH B: " + String.format("%.2fL", totalPrice * 0.8), x, y);
        y += fm.getHeight();
        g.drawString("TVSH B 20.00%: " + String.format("%.2fL", totalPrice * 0.2), x, y);
        y += fm.getHeight();
        g.drawLine(x, y += 10, (int) pageFormat.getImageableWidth() - 10, y);
        y += fm.getHeight();
        g.setFont(g.getFont().deriveFont(Font.PLAIN, 11));
        g.drawString("          NSLF", centerX, y);
        y += fm.getHeight();
        g.drawString("AFADD34B0AE12C490C98530", x, y);
        y += fm.getHeight();
        g.drawString("           NIVF", centerX, y);
        y += fm.getHeight();
        g.drawString("ef6d765b-449c-48e8-90c3-c733", x, y);
        y += fm.getHeight();
        y += fm.getHeight();
        y += fm.getHeight();

        int bottomMargin = (int) (pageFormat.getImageableHeight() - y);
        if (bottomMargin > 0) {
            y += bottomMargin;
        }

        return PAGE_EXISTS;
    }
}

class Product {
    private String barcode;
    private String name;
    private double price;
    private int quantity;

    public Product(String barcode, String name, double price, int quantity) {
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
}

class ProductStore {
    private ArrayList<Product> products;

    public ProductStore() {
        products = new ArrayList<>();
    }

    public void addProduct(Product product) {
        Product existingProduct = findProduct(product.getBarcode());
        if (existingProduct != null) {
            existingProduct.setQuantity(existingProduct.getQuantity() + product.getQuantity());
        } else {
            products.add(product);
        }
    }

    public Product findProduct(String barcode) {
        for (Product product : products) {
            if (product.getBarcode().equals(barcode)) {
                return product;
            }
        }
        return null;
    }

    public ArrayList<Product> getProductList() {
        return products;
    }

    public double calculateTotal() {
        double total = 0.0;
        for (Product product : products) {
            total += product.getPrice() * product.getQuantity();
        }
        return total;
    }
}