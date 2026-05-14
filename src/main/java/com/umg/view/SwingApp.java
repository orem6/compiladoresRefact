package com.umg.view;

import javax.swing.*;
import java.awt.*;
import com.umg.view.panels.*;

public class SwingApp extends JFrame {
    private CardLayout cardLayout;
    private JPanel mainContainer;

    public SwingApp() {
        setTitle("SQL Compiler - DataQuery Solutions");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 650);
        setLocationRelativeTo(null);

        setJMenuBar(new AppMenuBar());

        cardLayout = new CardLayout();
        mainContainer = new JPanel(cardLayout);

        mainContainer.add(new MainPanel(this), "main");
        mainContainer.add(new ResultPanel(), "result");
        mainContainer.add(new HistoryPanel(), "history");
        mainContainer.add(new HelpPanel(), "help");
        mainContainer.add(new ExamplesPanel(), "examples");

        add(mainContainer);
        setVisible(true);
    }

    public void showPanel(String panelName) {
        cardLayout.show(mainContainer, panelName);
    }
}
