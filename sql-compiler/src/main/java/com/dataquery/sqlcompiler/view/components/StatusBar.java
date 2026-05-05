package com.dataquery.sqlcompiler.view.components;

import javax.swing.*;
import java.awt.*;

public class StatusBar extends JPanel {
    private final JLabel statusLabel;

    public StatusBar() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEtchedBorder());
        setPreferredSize(new Dimension(getPreferredSize().width, 25));

        statusLabel = new JLabel("Ready");
        statusLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        JLabel infoLabel = new JLabel("SQL Compiler v1.0.0 - DataQuery Solutions");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(2, 10, 2, 10));

        add(statusLabel, BorderLayout.WEST);
        add(infoLabel, BorderLayout.EAST);
    }

    public void setStatus(String status) {
        statusLabel.setText(status);
    }
}
