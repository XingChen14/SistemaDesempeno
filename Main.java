import javax.swing.SwingUtilities;

/**
 * Punto de entrada de la aplicación.
 * Se utiliza SwingUtilities.invokeLater para garantizar que toda la interfaz
 * gráfica se construya y manipule en el Event Dispatch Thread (EDT),
 * lo cual es la práctica recomendada en Swing.
 */
public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            // 1. Se crea el modelo (lógica de negocio)
            EvaluacionModel model = new EvaluacionModel();
            // 2. Se crea la vista (interfaz gráfica)
            EvaluacionView view = new EvaluacionView();
            // 3. Se crea el controlador que conecta modelo y vista
            new EvaluacionController(model, view);
            // 4. Se hace visible la ventana principal
            view.setVisible(true);
        });
    }
}