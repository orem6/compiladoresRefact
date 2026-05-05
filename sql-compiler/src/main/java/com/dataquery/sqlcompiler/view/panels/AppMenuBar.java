package com.dataquery.sqlcompiler.view.panels;

import javax.swing.*;

public class AppMenuBar extends JMenuBar {
    public AppMenuBar() {
        JMenu fileMenu = new JMenu("File");
        JMenuItem clearItem = new JMenuItem("Clear");
        JMenuItem exitItem = new JMenuItem("Exit");
        exitItem.addActionListener(e -> System.exit(0));
        fileMenu.add(clearItem);
        fileMenu.addSeparator();
        fileMenu.add(exitItem);

        JMenu editMenu = new JMenu("Edit");
        JMenuItem undoItem = new JMenuItem("Undo");
        JMenuItem redoItem = new JMenuItem("Redo");
        editMenu.add(undoItem);
        editMenu.add(redoItem);

        JMenu viewMenu = new JMenu("View");
        JMenuItem historyItem = new JMenuItem("History");
        JMenuItem examplesItem = new JMenuItem("Examples");
        viewMenu.add(historyItem);
        viewMenu.add(examplesItem);

        JMenu helpMenu = new JMenu("Help");
        JMenuItem guideItem = new JMenuItem("User Guide");
        JMenuItem aboutItem = new JMenuItem("About");
        helpMenu.add(guideItem);
        helpMenu.addSeparator();
        helpMenu.add(aboutItem);

        add(fileMenu);
        add(editMenu);
        add(viewMenu);
        add(helpMenu);
    }
}
