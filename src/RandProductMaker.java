import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class RandProductMaker extends JFrame {

    public static final int RECORD_SIZE =
            (Product.NAME_LEN + Product.DESC_LEN + Product.ID_LEN) * 2 + Double.BYTES;

    private JTextField nameField;
    private JTextField descField;
    private JTextField idField;
    private JTextField costField;
    private JTextField recordCountField;

    private JButton addButton;
    private JButton quitButton;

    private int recordCount = 0;

    private RandomAccessFile raf;
    private String filePath;

    public RandProductMaker() {
        super("Random Access Product Maker");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(500, 380);
        setLocationRelativeTo(null);
        setResizable(false);

        buildUI();
        openFile();

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                quit();
            }
        });

        setVisible(true);
    }

    private void buildUI() {
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(6, 6, 6, 6);
        gbc.anchor = GridBagConstraints.WEST;

        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Product Name (max 35):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameField = new JTextField(30);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0; gbc.gridy = 1; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Description (max 75):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        descField = new JTextField(30);
        formPanel.add(descField, gbc);

        gbc.gridx = 0; gbc.gridy = 2; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Product ID (max 6):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        idField = new JTextField(10);
        formPanel.add(idField, gbc);

        gbc.gridx = 0; gbc.gridy = 3; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Cost ($):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        costField = new JTextField(10);
        formPanel.add(costField, gbc);

        gbc.gridx = 0; gbc.gridy = 4; gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0;
        formPanel.add(new JLabel("Record Count:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        recordCountField = new JTextField("0", 5);
        recordCountField.setEditable(false);
        recordCountField.setBackground(new Color(230, 230, 230));
        formPanel.add(recordCountField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));

        addButton = new JButton("Add Record");
        addButton.setPreferredSize(new Dimension(130, 35));
        addButton.addActionListener(e -> addRecord());

        quitButton = new JButton("Quit");
        quitButton.setPreferredSize(new Dimension(100, 35));
        quitButton.addActionListener(e -> quit());

        buttonPanel.add(addButton);
        buttonPanel.add(quitButton);

        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void openFile() {
        filePath = JOptionPane.showInputDialog(this,
                "Enter the output file name (e.g. products.dat):",
                "Output File",
                JOptionPane.QUESTION_MESSAGE);

        if (filePath == null || filePath.trim().isEmpty()) {
            filePath = "products.dat";
        }

        try {
            raf = new RandomAccessFile(filePath, "rw");
            recordCount = (int) (raf.length() / RECORD_SIZE);
            recordCountField.setText(String.valueOf(recordCount));
            setTitle("Random Access Product Maker  —  " + filePath);
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not open file: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void addRecord() {
        String name = nameField.getText().trim();
        String desc = descField.getText().trim();
        String id   = idField.getText().trim();
        String costText = costField.getText().trim();

        if (name.isEmpty() || desc.isEmpty() || id.isEmpty() || costText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "All fields must be filled in before adding a record.",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        double cost;
        try {
            cost = Double.parseDouble(costText);
            if (cost < 0) {
                JOptionPane.showMessageDialog(this,
                        "Cost cannot be negative.",
                        "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Cost must be a valid number (e.g. 19.99).",
                    "Validation Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        Product product = new Product(name, desc, id, cost);

        try {
            raf.seek((long) recordCount * RECORD_SIZE);

            writeFixedString(raf, product.getFixedName(),        Product.NAME_LEN);
            writeFixedString(raf, product.getFixedDescription(), Product.DESC_LEN);
            writeFixedString(raf, product.getFixedID(),          Product.ID_LEN);
            raf.writeDouble(product.getCost());

            recordCount++;
            recordCountField.setText(String.valueOf(recordCount));

            JOptionPane.showMessageDialog(this,
                    "Record #" + recordCount + " added successfully!",
                    "Record Saved", JOptionPane.INFORMATION_MESSAGE);

            clearFields();

        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error writing record: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void writeFixedString(RandomAccessFile file, String s, int len) throws IOException {
        for (int i = 0; i < len; i++) {
            file.writeChar(s.charAt(i));
        }
    }

    private void clearFields() {
        nameField.setText("");
        descField.setText("");
        idField.setText("");
        costField.setText("");
        nameField.requestFocus();
    }

    private void quit() {
        try {
            if (raf != null) raf.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(RandProductMaker::new);
    }
}