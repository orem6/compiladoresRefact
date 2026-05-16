package com.umg.view.components;

import javax.swing.*;
import java.awt.*;

public class SqlInputArea extends JTextArea {
    public SqlInputArea() {
        super(6, 60);
        setFont(new Font("Monospaced", Font.PLAIN, 14));
        setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.GRAY),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        setFocusTraversalKeysEnabled(true);
    }

    public void clear() {
        setText("");
    }

    public String getText() {
        return super.getText();
    }
}
