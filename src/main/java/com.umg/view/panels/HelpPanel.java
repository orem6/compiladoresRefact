package com.dataquery.sqlcompiler.view.panels;

import javax.swing.*;
import java.awt.*;

public class HelpPanel extends JPanel {
    public HelpPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Help - SQL Compiler Guide");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        JEditorPane helpContent = new JEditorPane();
        helpContent.setEditable(false);
        helpContent.setContentType("text/html");
        helpContent.setText(
            "<html>" +
            "<body style='font-family: Arial; padding: 10px;'>" +
            "<h2>SQL Compiler - DataQuery Solutions</h2>" +
            "<h3>Supported Statements:</h3>" +
            "<ul>" +
            "<li><b>SELECT</b> - Query data from tables</li>" +
            "<li><b>INSERT</b> - Add new records to a table</li>" +
            "<li><b>UPDATE</b> - Modify existing records</li>" +
            "<li><b>DELETE</b> - Remove records from a table</li>" +
            "<li><b>CREATE TABLE</b> - Create a new table</li>" +
            "<li><b>DROP TABLE</b> - Delete a table</li>" +
            "</ul>" +
            "<h3>How to Use:</h3>" +
            "<ol>" +
            "<li>Type your SQL query in the input area</li>" +
            "<li>Click 'Validate Query' to check syntax</li>" +
            "<li>View results in the output area</li>" +
            "<li>Check the Examples tab for reference</li>" +
            "</ol>" +
            "<h3>Notes:</h3>" +
            "<p>All queries must end with a semicolon (;)</p>" +
            "</body>" +
            "</html>"
        );
        add(new JScrollPane(helpContent), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton backBtn = new JButton("Back");
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }
}
