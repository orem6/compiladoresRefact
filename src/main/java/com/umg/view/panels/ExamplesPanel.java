package com.umg.view.panels;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class ExamplesPanel extends JPanel {
    public ExamplesPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("SQL Query Examples");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        String[] columns = {"Type", "Example"};
        DefaultTableModel model = new DefaultTableModel(columns, 0);

        Object[][] validExamples = {
            {"VALID", "SELECT * FROM users WHERE age > 18;"},
            {"VALID", "INSERT INTO users (name, age) VALUES ('John', 25);"},
            {"VALID", "UPDATE users SET age = 26 WHERE name = 'John';"},
            {"VALID", "DELETE FROM users WHERE age < 18;"},
            {"VALID", "CREATE TABLE users (id INT, name VARCHAR);"},
            {"VALID", "DROP TABLE users;"},
            {"INVALID", "SELCT * FROM users;"},
            {"INVALID", "SELECT FROM WHERE;"},
            {"INVALID", "INSERT users VALUES;"},
            {"INVALID", "SELECT * FROM;"}
        };

        for (Object[] row : validExamples) {
            model.addRow(row);
        }

        JTable examplesTable = new JTable(model);
        examplesTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        examplesTable.setRowHeight(25);
        add(new JScrollPane(examplesTable), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backBtn = new JButton("Back");
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
}
