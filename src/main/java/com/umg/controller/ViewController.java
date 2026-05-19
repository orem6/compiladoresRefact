package com.umg.controller;
import com.umg.model.ViewModel;
import com.umg.model.lexer.*;

import java.awt.event.*;

public class ViewController implements ActionListener, MouseListener, WindowListener {
    ViewModel view;

    public ViewController(ViewModel viewModel) {
        this.view = viewModel;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getActionCommand().equals(view.getVista().BtnAnalizar.getActionCommand())){
            try{
                AnalizadorSql lexer = new AnalizadorSql();
                ResultadoLexer resultado = lexer.analizar(view.getVista().TxAConsultas.getText());
                if(resultado.isValido()){
                } else {
                }
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

        } else if(e.getActionCommand().equals(view.getVista().BtnLimpiar.getActionCommand())){

        } else if(e.getActionCommand().equals(view.getVista().BtnConsola.getActionCommand())){

        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void windowOpened(WindowEvent e) {

    }

    @Override
    public void windowClosing(WindowEvent e) {

    }

    @Override
    public void windowClosed(WindowEvent e) {

    }

    @Override
    public void windowIconified(WindowEvent e) {

    }

    @Override
    public void windowDeiconified(WindowEvent e) {

    }

    @Override
    public void windowActivated(WindowEvent e) {

    }

    @Override
    public void windowDeactivated(WindowEvent e) {

    }

    public void limpiarCampos(){
    }
}
