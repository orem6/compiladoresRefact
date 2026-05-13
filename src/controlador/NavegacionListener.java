package controlador;

/**
 * Interfaz para manejar la navegación desde la pantalla principal
 * hacia los diferentes módulos del compilador.
 */
public interface NavegacionListener {

    void onAnalisisLexico();
    void onAnalisisSintactico();
    void onAnalisisSemantico();
    void onTablaTokens();
    void onAbrirEditor();
    void onSalir();
}
