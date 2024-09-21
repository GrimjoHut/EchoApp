package org.example;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Vector;

public class EchoServiceApp extends JFrame {

    private JTextArea logArea;
    private JTable dataTable;
    private DefaultTableModel tableModel;
    private JTextField filterField;

    public EchoServiceApp() {
        setTitle("Echo Service Application");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.addTab("Log", createLogPanel());
        tabbedPane.addTab("Data", createDataPanel());

        add(tabbedPane);

        loadData();
        setupFilter();
    }

    private JPanel createLogPanel() {
        logArea = new JTextArea();
        logArea.setEditable(false);

        JPanel logPanel = new JPanel(new BorderLayout());
        logPanel.add(new JScrollPane(logArea), BorderLayout.CENTER);

        return logPanel;
    }

    private JPanel createDataPanel() {
        filterField = new JTextField();

        tableModel = new DefaultTableModel();
        dataTable = new JTable(tableModel);

        JPanel dataPanel = new JPanel(new BorderLayout());
        dataPanel.add(filterField, BorderLayout.NORTH);
        dataPanel.add(new JScrollPane(dataTable), BorderLayout.CENTER);

        return dataPanel;
    }

    private void loadData() {
        HttpClient client = HttpClient.newHttpClient();

        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(new URI("https://jsonplaceholder.typicode.com/posts"))
                    .GET()
                    .build();

            client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenApply(HttpResponse::body)
                    .thenAccept(this::processResponse)
                    .join();

        } catch (Exception e) {
            logArea.append("Error: " + e.getMessage() + "\n");
        }
    }

    private void processResponse(String responseBody) {
        logArea.append("Received response:\n" + responseBody + "\n");

        JsonArray jsonArray = JsonParser.parseString(responseBody).getAsJsonArray();

        String[] columns = {"UserId", "Id", "Title", "Body"};
        tableModel.setColumnIdentifiers(columns);

        for (JsonElement element : jsonArray) {
            Vector<String> row = new Vector<>();
            row.add(element.getAsJsonObject().get("userId").getAsString());
            row.add(element.getAsJsonObject().get("id").getAsString());
            row.add(element.getAsJsonObject().get("title").getAsString());
            row.add(element.getAsJsonObject().get("body").getAsString());
            tableModel.addRow(row);
        }
    }

    private void setupFilter() {
        TableRowSorter<DefaultTableModel> rowSorter = new TableRowSorter<>(tableModel);
        dataTable.setRowSorter(rowSorter);

        filterField.addActionListener(e -> {
            String text = filterField.getText().trim();
            if (text.isEmpty()) {
                rowSorter.setRowFilter(null);
            } else {
                rowSorter.setRowFilter(RowFilter.regexFilter("(?i)" + text));
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EchoServiceApp app = new EchoServiceApp();
            app.setVisible(true);
        });
    }
}
