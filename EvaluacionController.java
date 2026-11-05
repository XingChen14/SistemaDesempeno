import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

public class EvaluacionController {
    private EvaluacionModel model;
    private EvaluacionView view;
    private int currentIndex = 0;
    private boolean datosCargados = false;

    public EvaluacionController(EvaluacionModel model, EvaluacionView view) {
        this.model = model;
        this.view = view;

        view.addExaminarListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            // Cambia esta ruta por la que tú quieras:
            fileChooser.setCurrentDirectory(new java.io.File("C:\\Users\\orimu\\Downloads\\Inteligencia artificial\\SistemaDesempeno\\Data"));
            
            // Filtro para mostrar solo archivos .csv 
            javax.swing.filechooser.FileNameExtensionFilter filtroCsv = 
                new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto CSV (*.csv)", "csv");
            fileChooser.setFileFilter(filtroCsv);
            
            if (fileChooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
                view.setRutaArchivo(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        view.addExaminarCarpetaListener(e -> {
            JFileChooser dirChooser = new JFileChooser();
            dirChooser.setCurrentDirectory(new java.io.File("C:\\Users\\orimu\\Downloads\\Inteligencia artificial\\SistemaDesempeno\\Resultados"));
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (dirChooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
                view.setCarpetaSalida(dirChooser.getSelectedFile().getAbsolutePath());
            }
        });

        view.addCargarListener(e -> cargarDataset());
        view.addSiguienteListener(e -> ejecutarPasoAPaso());
        view.addProcesarRestanteListener(e -> procesarRestante());
        view.addTerminarAListener(e -> terminarModoA());
        view.addProcesarTodoListener(e -> ejecutarProcesamientoCompleto());
    }

    private void cargarDataset() {
        String ruta = view.getRutaArchivo();
        String clases = view.getClasesTexto();
        if (ruta.isEmpty() || clases.isEmpty()) {
            view.mostrarMensaje("Debe especificar la ruta del archivo CSV y las clases.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (!ruta.toLowerCase().endsWith(".csv")) {
            view.mostrarMensaje("El archivo seleccionado no es válido.\nPor favor, seleccione un archivo con extensión .csv", "Formato Incorrecto", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            model.cargarDataset(ruta, clases);
            datosCargados = true;
            currentIndex = 0;
            view.setControlesModoAEnabled(true);
            view.setProcesarTodoEnabled(true);
            view.setModoAText("Dataset cargado exitosamente con " + model.getDataset().size() + " registros.\n");
            view.mostrarMensaje("Dataset cargado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            view.mostrarMensaje("Error al leer el archivo CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void ejecutarPasoAPaso() {
        List<EvaluacionModel.RegistroDato> dataset = model.getDataset();
        if (currentIndex < dataset.size()) {
            EvaluacionModel.RegistroDato reg = dataset.get(currentIndex);
            StringBuilder sb = new StringBuilder();
            sb.append("--- Evaluando Registro [").append(currentIndex + 1).append("/").append(dataset.size()).append("] ---\n");
            sb.append("ID: ").append(reg.id).append(" | Clase Verdadera: ").append(reg.claseVerdadera).append(" | Predicción: ").append(reg.clasePrediccion).append("\n");
            sb.append("Evaluación respecto a cada clase:\n");
            for (String clase : model.getListaClases()) {
                String val = model.evaluarRegistro(reg, clase);
                sb.append(" - Clase ").append(clase).append(": ").append(val).append("\n");
            }
            sb.append("\n");
            view.appendModoA(sb.toString());
            currentIndex++;
        } else {
            view.mostrarMensaje("Se han evaluado todos los registros del dataset en el Modo A.", "Información", JOptionPane.INFORMATION_MESSAGE);
            view.setControlesModoAEnabled(false);
        }
    }

    private void procesarRestante() {
        while (currentIndex < model.getDataset().size()) {
            ejecutarPasoAPaso();
        }
    }

    private void terminarModoA() {
        currentIndex = 0;
        view.setModoAText("Proceso Modo A reiniciado / terminado por el usuario.\n");
        view.setControlesModoAEnabled(false);
    }

    private void ejecutarProcesamientoCompleto() {
        if (!datosCargados) return;
        try {
            String carpetaBase = view.getCarpetaSalida();
            String nombreFinal = view.getNombreArchivoFinal();
            String rutaCsvOriginal = view.getRutaArchivo();

            // 1. Extraer el nombre del archivo CSV sin la extensión .csv
            java.io.File archivoCsv = new java.io.File(rutaCsvOriginal);
            String nombreArchivoCsv = archivoCsv.getName();
            int puntoIndex = nombreArchivoCsv.lastIndexOf('.');
            if (puntoIndex > 0) {
                nombreArchivoCsv = nombreArchivoCsv.substring(0, puntoIndex);
            }

            // 2. Construir el nombre base de la carpeta
            String nombreBaseCarpeta = "Resultado_" + nombreArchivoCsv;
            String nombreCarpetaDestino = nombreBaseCarpeta;
            
            // 3. Lógica para evitar sobreescribir: buscar el siguiente nombre disponible
            java.io.File dirDestinoFinal = new java.io.File(carpetaBase, nombreCarpetaDestino);
            int contador = 1;
            while (dirDestinoFinal.exists()) {
                // Si existe, probamos con "Resultado_nombre (1)", "Resultado_nombre (2)", etc.
                nombreCarpetaDestino = nombreBaseCarpeta + " (" + contador + ")";
                dirDestinoFinal = new java.io.File(carpetaBase, nombreCarpetaDestino);
                contador++;
            }

            // Obtenemos los nombres desde la vista
            String nombreMatriz = view.getNombreMatriz();
            String prefijoClases = view.getNombreClases();

            // 4. Llamamos al modelo con los 4 parámetros
            EvaluacionModel.ResultadosProcesamiento res = model.procesarCompleto(
                dirDestinoFinal.getAbsolutePath(), 
                nombreFinal, 
                nombreMatriz, 
                prefijoClases
            );

            // --- Mostrar resultados en la interfaz ---
            StringBuilder sbTexto = new StringBuilder();
            sbTexto.append("--- SUMAS Y MÉTRICAS POR CLASE ---\n");
            for (String clase : model.getListaClases()) {
                int[] tpFpTnFn = res.metricasClase.get(clase);
                double[] m = res.metricasCalculadas.get(clase);
                sbTexto.append(String.format("Clase %s -> TP:%d, FP:%d, TN:%d, FN:%d\n", clase, tpFpTnFn[0], tpFpTnFn[1], tpFpTnFn[2], tpFpTnFn[3]));
                sbTexto.append(String.format("   Prec: %.2f%% | Rec: %.2f%% | Acc: %.2f%% | F1: %.2f%%\n", m[0]*100, m[1]*100, m[2]*100, m[3]*100));
            }

            sbTexto.append("\n--- PROMEDIO GENERAL ---\n");
            sbTexto.append(String.format("Precisión: %.2f%%\nRecall: %.2f%%\nAccuracy: %.2f%%\nF-Score: %.2f%%\n",
                    res.promediosGenerales[0]*100, res.promediosGenerales[1]*100, res.promediosGenerales[2]*100, res.promediosGenerales[3]*100));

            view.mostrarResultadosFinales(res.matrizConf, model.getListaClases(), sbTexto.toString());
            
            // Mensaje mostrando la nueva carpeta creada
            view.mostrarMensaje("Procesamiento completo finalizado.\nArchivos guardados exitosamente en:\n" + dirDestinoFinal.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);
            
        } catch (Exception ex) {
            view.mostrarMensaje("Error en el procesamiento: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}