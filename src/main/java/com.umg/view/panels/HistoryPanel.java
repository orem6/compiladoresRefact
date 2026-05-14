package com.dataquery.sqlcompiler.view.panels;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class HistoryPanel extends JPanel {
    private final JList<String> historyList;
    private final DefaultListModel<String> listModel;

    public HistoryPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Query History");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        historyList = new JList<>(listModel);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 12));
        add(new JScrollPane(historyList), BorderLayout.CENTER);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton clearBtn = new JButton("Clear History");
        JButton backBtn = new JButton("Back");
        clearBtn.addActionListener(e -> listModel.clear());
        btnPanel.add(clearBtn);
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void addQuery(String query) {
        listModel.addElement(query);
    }
}
