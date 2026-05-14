package com.umg.view.panels;

import com.umg.controller.SQLCompilerController;
import com.umg.view.SwingApp;
import com.umg.view.components.SqlInputArea;
import com.umg.view.components.StatusBar;

import javax.swing.*;
import java.awt.*;

public class MainPanel extends JPanel {
    private final SwingApp parentFrame;
    private final SqlInputArea sqlInputArea;
    private final JTextArea resultArea;
    private final StatusBar statusBar;

    public MainPanel(SwingApp parentFrame) {
        this.parentFrame = parentFrame;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JLabel titleLabel = new JLabel("SQL Query Validator");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        topPanel.add(titleLabel);

        sqlInputArea = new SqlInputArea();
        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.add(new JLabel("Enter SQL query:"), BorderLayout.NORTH);
        centerPanel.add(sqlInputArea, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 5));
        JButton validateBtn = new JButton("Validate Query");
        JButton clearBtn = new JButton("Clear");

        SQLCompilerController controller = new SQLCompilerController();
        statusBar = new StatusBar();

        resultArea = new JTextArea(8, 60);
        resultArea.setEditable(false);
        resultArea.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultArea.setBackground(new Color(245, 245, 245));

        validateBtn.addActionListener(e -> {
            String sql = sqlInputArea.getText();
            String result = controller.validateQuery(sql);
            resultArea.setText(result);
            resultArea.setForeground(controller.lastResultValid() ? Color.GREEN.darker() : Color.RED);
            statusBar.setStatus(controller.lastResultValid() ? "Valid query" : "Invalid query");
        });

        clearBtn.addActionListener(e -> {
            sqlInputArea.clear();
            resultArea.setText("");
            statusBar.setStatus("Ready");
        });

        buttonPanel.add(validateBtn);
        buttonPanel.add(clearBtn);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);

        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(new JScrollPane(resultArea), BorderLayout.CENTER);
        southPanel.add(statusBar, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);
    }
}
