package controlador;

/**
 * Interfaz que define los eventos del compilador.
 * Permite desacoplar completamente la lógica visual de la lógica
 * de análisis léxico, sintáctico y semántico.
 *
 * La vista invoca estos métodos cuando el usuario interactúa,
 * y la capa del compilador los implementa para ejecutar el análisis.
 */
public interface CompiladorListener {

    /**
     * Se invoca cuando el usuario hace clic en "Analizar".
     * @param codigoFuente Texto completo del área de código.
     */
    void onAnalizar(String codigoFuente);

    /**
     * Se invoca cuando el usuario hace clic en "Limpiar".
     * Debe reiniciar todos los paneles de resultados.
     */
    void onLimpiar();

    /**
     * Se invoca cuando el usuario quiere abrir un archivo.
     * @return El contenido del archivo seleccionado, o null si se cancela.
     */
    String onAbrirArchivo();

    /**
     * Se invoca cuando el usuario quiere guardar el código.
     * @param codigoFuente Contenido actual del área de código.
     */
    void onGuardarArchivo(String codigoFuente);

    /**
     * Se invoca cuando el usuario hace clic en "Regresar".
     * Permite volver a la ventana principal del sistema.
     */
    void onRegresarPaginaPrincipal();
}
