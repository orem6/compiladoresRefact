package com.umg.view.components;

import com.umg.model.error.CompilerError;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class ErrorListPanel extends JPanel {
    private final DefaultListModel<String> errorListModel;
    private final JList<String> errorList;

    public ErrorListPanel() {
        setLayout(new BorderLayout());

        JLabel titleLabel = new JLabel("Errors");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 14));
        add(titleLabel, BorderLayout.NORTH);

        errorListModel = new DefaultListModel<>();
        errorList = new JList<>(errorListModel);
        errorList.setCellRenderer(new ErrorListCellRenderer());
        add(new JScrollPane(errorList), BorderLayout.CENTER);
    }

    public void setErrors(List<CompilerError> errors) {
        errorListModel.clear();
        for (CompilerError error : errors) {
            errorListModel.addElement("[" + error.getType() + "] Line " +
                error.getLine() + ", Col " + error.getColumn() + ": " + error.getMessage());
        }
    }

    public void clearErrors() {
        errorListModel.clear();
    }

    private static class ErrorListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value,
                int index, boolean isSelected, boolean cellHasFocus) {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            String text = value.toString();
            if (text.contains("LEXICAL")) setForeground(Color.RED);
            else if (text.contains("SYNTAX")) setForeground(Color.ORANGE);
            else if (text.contains("SEMANTIC")) setForeground(Color.MAGENTA);
            return this;
        }
    }
}
