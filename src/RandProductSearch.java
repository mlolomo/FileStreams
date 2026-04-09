import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;

public class RandProductSearch extends JFrame {

    public static final int RECORD_SIZE =
            (Product.NAME_LEN + Product.DESC_LEN + Product.ID_LEN) * 2 + Double.BYTES;

    private JTextField searchField;

    private JButton searchButton;
    private JButton clearButton;
    private JButton quitButton;

    private JTextArea resultsArea;

    private RandomAccessFile raf;
    private String filePath;

    public RandProductSearch() {
        super("Random Access Product Search");
        setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        setSize(650, 480);
        setLocationRelativeTo(null);
        setResizable(true);

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

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.add(new JLabel("Search Product Name:"));
        searchField = new JTextField(25);
        searchPanel.add(searchField);

        searchButton = new JButton("Search");
        searchButton.addActionListener(e -> performSearch());
        searchPanel.add(searchButton);

        clearButton = new JButton("Clear");
        clearButton.addActionListener(e -> {
            searchField.setText("");
            resultsArea.setText("");
            searchField.requestFocus();
        });
        searchPanel.add(clearButton);

        searchField.addActionListener(e -> performSearch());

        resultsArea = new JTextArea();
        resultsArea.setEditable(false);
        resultsArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        resultsArea.setBackground(new Color(250, 250, 250));
        JScrollPane scrollPane = new JScrollPane(resultsArea);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Results"));

        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        quitButton = new JButton("Quit");
        quitButton.setPreferredSize(new Dimension(100, 35));
        quitButton.addActionListener(e -> quit());
        bottomPanel.add(quitButton);

        mainPanel.add(searchPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
    }

    private void openFile() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select Product Data File");
        int result = chooser.showOpenDialog(this);

        if (result != JFileChooser.APPROVE_OPTION) {
            JOptionPane.showMessageDialog(this,
                    "No file selected. Program will exit.",
                    "No File", JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }

        filePath = chooser.getSelectedFile().getAbsolutePath();

        try {
            raf = new RandomAccessFile(filePath, "r");
            setTitle("Random Access Product Search  —  " + chooser.getSelectedFile().getName());
        } catch (IOException ex) {
            JOptionPane.showMessageDialog(this,
                    "Could not open file: " + ex.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    private void performSearch() {
        String searchTerm = searchField.getText().trim();

        if (searchTerm.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter a search term.",
                    "Search Error", JOptionPane.WARNING_MESSAGE);
            return;
        }

        resultsArea.setText("");
        StringBuilder sb = new StringBuilder();

        try {
            long totalRecords = raf.length() / RECORD_SIZE;

            if (totalRecords == 0) {
                resultsArea.setText("The data file contains no records.");
                return;
            }

            sb.append(String.format("%-6s  %-35s  %-75s  %8s%n",
                    "ID", "Name", "Description", "Cost"));
            sb.append("-".repeat(132)).append("\n");

            int matchCount = 0;

            for (long i = 0; i < totalRecords; i++) {
                raf.seek(i * RECORD_SIZE);

                String name = readFixedString(raf, Product.NAME_LEN).trim();
                String desc = readFixedString(raf, Product.DESC_LEN).trim();
                String id   = readFixedString(raf, Product.ID_LEN).trim();
                double cost = raf.readDouble();

                if (name.toLowerCase().contains(searchTerm.toLowerCase())) {
                    sb.append(String.format("%-6s  %-35s  %-75s  %8.2f%n",
                            id, name, desc, cost));
                    matchCount++;
                }
            }

            if (matchCount == 0) {
                sb.append("\nNo products found matching: \"").append(searchTerm).append("\"");
            } else {
                sb.append("\n").append(matchCount).append(" record(s) found.");
            }

        } catch (IOException ex) {
            sb.append("Error reading file: ").append(ex.getMessage());
        }

        resultsArea.setText(sb.toString());
        resultsArea.setCaretPosition(0);
    }

    private String readFixedString(RandomAccessFile file, int len) throws IOException {
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(file.readChar());
        }
        return sb.toString();
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
        SwingUtilities.invokeLater(RandProductSearch::new);
    }
}