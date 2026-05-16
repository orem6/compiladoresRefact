package com.umg.controller;

import java.awt.event.ActionListener;

public class InputHandler implements ActionListener {
    private final SQLCompilerController controller;

    public InputHandler(SQLCompilerController controller) {
        this.controller = controller;
    }

    @Override
    public void actionPerformed(java.awt.event.ActionEvent e) {
        String command = e.getActionCommand();
        switch (command) {
            case "Validate" -> {
            }
            case "Clear" -> {
            }
            default -> {
            }
        }
    }
}
