import javax.swing.SwingUtilities;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            EvaluacionModel model = new EvaluacionModel();
            EvaluacionView view = new EvaluacionView();
            new EvaluacionController(model, view);
            view.setVisible(true);
        });
    }
}