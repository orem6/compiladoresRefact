package com.umg.model;
import com.umg.view.Visa_Compilador;

public class ViewModel {
    public Visa_Compilador vista;

    public ViewModel(Visa_Compilador vista) {
        this.vista = vista;
    }

    public Visa_Compilador getVista() {
        return vista;
    }

    public void setVista(Visa_Compilador vista) {
        this.vista = vista;
    }
}
