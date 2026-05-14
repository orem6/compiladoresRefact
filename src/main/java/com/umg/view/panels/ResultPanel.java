package com.umg.view.panels;

import javax.swing.*;
import java.awt.*;

public class ResultPanel extends JPanel {
    private final JTextArea resultDisplay;

    public ResultPanel() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel titleLabel = new JLabel("Validation Result");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 18));
        add(titleLabel, BorderLayout.NORTH);

        resultDisplay = new JTextArea();
        resultDisplay.setEditable(false);
        resultDisplay.setFont(new Font("Monospaced", Font.PLAIN, 13));
        resultDisplay.setBackground(new Color(250, 250, 250));
        add(new JScrollPane(resultDisplay), BorderLayout.CENTER);

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
        });
        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        btnPanel.add(backBtn);
        add(btnPanel, BorderLayout.SOUTH);
    }

    public void setResultText(String text) {
        resultDisplay.setText(text);
    }
}
