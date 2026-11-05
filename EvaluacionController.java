import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

/**
 * Controlador del patrón MVC.
 * Se encarga de conectar los eventos de la Vista con las operaciones del Modelo.
 * NO contiene lógica de cálculo de métricas (eso está en el Modelo).
 */
public class EvaluacionController {
    private EvaluacionModel model;
    private EvaluacionView view;
    private int currentIndex = 0;      // Índice para el Modo A (paso a paso)
    private boolean datosCargados = false; // Bandera para saber si ya hay dataset

    public EvaluacionController(EvaluacionModel model, EvaluacionView view) {
        this.model = model;
        this.view = view;

        // ============================================================
        // LISTENER: Botón "Examinar" (selección de archivo CSV)
        // ============================================================
        view.addExaminarListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            // Carpeta inicial que se abrirá al buscar el archivo
            fileChooser.setCurrentDirectory(new java.io.File("C:\\Users\\orimu\\Downloads\\Inteligencia artificial\\SistemaDesempeno\\Data"));
            
            // Filtro para que SOLO se muestren archivos con extensión .csv
            javax.swing.filechooser.FileNameExtensionFilter filtroCsv = 
                new javax.swing.filechooser.FileNameExtensionFilter("Archivos de texto CSV (*.csv)", "csv");
            fileChooser.setFileFilter(filtroCsv);
            
            // Si el usuario acepta, se guarda la ruta en la vista
            if (fileChooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
                view.setRutaArchivo(fileChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // ============================================================
        // LISTENER: Botón "Elegir Carpeta" (carpeta de salida)
        // ============================================================
        view.addExaminarCarpetaListener(e -> {
            JFileChooser dirChooser = new JFileChooser();
            // Carpeta inicial por defecto para los resultados
            dirChooser.setCurrentDirectory(new java.io.File("C:\\Users\\orimu\\Downloads\\Inteligencia artificial\\SistemaDesempeno\\Resultados"));
            // Solo se pueden seleccionar directorios, no archivos
            dirChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            if (dirChooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
                view.setCarpetaSalida(dirChooser.getSelectedFile().getAbsolutePath());
            }
        });

        // ============================================================
        // ASIGNACIÓN DE LISTENERS A LOS BOTONES DE LA VISTA
        // ============================================================
        view.addCargarListener(e -> cargarDataset());                 // Cargar CSV
        view.addSiguienteListener(e -> ejecutarPasoAPaso());          // Modo A: paso a paso
        view.addProcesarRestanteListener(e -> procesarRestante());    // Modo A: todo de golpe
        view.addTerminarAListener(e -> terminarModoA());              // Reiniciar Modo A
        view.addProcesarTodoListener(e -> ejecutarProcesamientoCompleto()); // Modo B: métricas finales
    }

    /**
     * Carga el dataset desde el archivo CSV en el modelo.
     * Valida que el archivo tenga extensión .csv antes de leerlo.
     */
    private void cargarDataset() {
        String ruta = view.getRutaArchivo();
        String clases = view.getClasesTexto();

        // Validación 1: Campos vacíos
        if (ruta.isEmpty() || clases.isEmpty()) {
            view.mostrarMensaje("Debe especificar la ruta del archivo CSV y las clases.", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // Validación 2: Extensión del archivo
        if (!ruta.toLowerCase().endsWith(".csv")) {
            view.mostrarMensaje("El archivo seleccionado no es válido.\nPor favor, seleccione un archivo con extensión .csv", "Formato Incorrecto", JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            // Se delega la lectura al modelo
            model.cargarDataset(ruta, clases);
            datosCargados = true;
            currentIndex = 0;
            // Se habilitan los controles de procesamiento
            view.setControlesModoAEnabled(true);
            view.setProcesarTodoEnabled(true);
            view.setModoAText("Dataset cargado exitosamente con " + model.getDataset().size() + " registros.\n");
            view.mostrarMensaje("Dataset cargado correctamente.", "Éxito", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception ex) {
            view.mostrarMensaje("Error al leer el archivo CSV: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Modo A: Procesa UN solo registro a la vez.
     * Muestra en pantalla la evaluación del registro para cada clase (TP/TN/FP/FN).
     */
    private void ejecutarPasoAPaso() {
        List<EvaluacionModel.RegistroDato> dataset = model.getDataset();
        if (currentIndex < dataset.size()) {
            EvaluacionModel.RegistroDato reg = dataset.get(currentIndex);
            StringBuilder sb = new StringBuilder();
            sb.append("--- Evaluando Registro [").append(currentIndex + 1).append("/").append(dataset.size()).append("] ---\n");
            sb.append("ID: ").append(reg.id)
              .append(" | Clase Verdadera: ").append(reg.claseVerdadera)
              .append(" | Predicción: ").append(reg.clasePrediccion).append("\n");
            sb.append("Evaluación respecto a cada clase:\n");
            // Se evalúa el registro contra CADA una de las clases definidas
            for (String clase : model.getListaClases()) {
                String val = model.evaluarRegistro(reg, clase);
                sb.append(" - Clase ").append(clase).append(": ").append(val).append("\n");
            }
            sb.append("\n");
            view.appendModoA(sb.toString());
            currentIndex++;
        } else {
            // Se llegó al final del dataset
            view.mostrarMensaje("Se han evaluado todos los registros del dataset en el Modo A.", "Información", JOptionPane.INFORMATION_MESSAGE);
            view.setControlesModoAEnabled(false);
        }
    }

    /**
     * Modo A: Procesa automáticamente todos los registros restantes
     * llamando repetidamente al método paso a paso.
     */
    private void procesarRestante() {
        while (currentIndex < model.getDataset().size()) {
            ejecutarPasoAPaso();
        }
    }

    /**
     * Reinicia el estado del Modo A.
     */
    private void terminarModoA() {
        currentIndex = 0;
        view.setModoAText("Proceso Modo A reiniciado / terminado por el usuario.\n");
        view.setControlesModoAEnabled(false);
    }

    /**
     * Modo B: Procesa TODO el dataset de una vez, calcula métricas,
     * muestra la matriz de confusión y genera los archivos CSV de salida.
     */
    private void ejecutarProcesamientoCompleto() {
        if (!datosCargados) return;
        try {
            String carpetaBase = view.getCarpetaSalida();
            String nombreFinal = view.getNombreArchivoFinal();
            String rutaCsvOriginal = view.getRutaArchivo();

            // 1. Extraer el nombre del archivo CSV (sin extensión) para usarlo en la carpeta
            java.io.File archivoCsv = new java.io.File(rutaCsvOriginal);
            String nombreArchivoCsv = archivoCsv.getName();
            int puntoIndex = nombreArchivoCsv.lastIndexOf('.');
            if (puntoIndex > 0) {
                nombreArchivoCsv = nombreArchivoCsv.substring(0, puntoIndex);
            }

            // 2. Construir el nombre de la carpeta: "Resultado_<nombre_csv>"
            String nombreBaseCarpeta = "Resultado_" + nombreArchivoCsv;
            String nombreCarpetaDestino = nombreBaseCarpeta;

            // 3. Evitar sobreescribir: si ya existe, se le agrega (1), (2), etc.
            java.io.File dirDestinoFinal = new java.io.File(carpetaBase, nombreCarpetaDestino);
            int contador = 1;
            while (dirDestinoFinal.exists()) {
                nombreCarpetaDestino = nombreBaseCarpeta + " (" + contador + ")";
                dirDestinoFinal = new java.io.File(carpetaBase, nombreCarpetaDestino);
                contador++;
            }

            // 4. Obtener los nombres personalizados que el usuario escribió en la vista
            String nombreMatriz = view.getNombreMatriz();
            String prefijoClases = view.getNombreClases();

            // 5. Llamar al modelo para que haga TODO el procesamiento y guarde los CSV
            EvaluacionModel.ResultadosProcesamiento res = model.procesarCompleto(
                dirDestinoFinal.getAbsolutePath(),
                nombreFinal,
                nombreMatriz,
                prefijoClases
            );

            // ============================================================
            // MOSTRAR RESULTADOS EN LA INTERFAZ (Pestaña 3)
            // ============================================================
            StringBuilder sbTexto = new StringBuilder();
            sbTexto.append("--- SUMAS Y MÉTRICAS POR CLASE ---\n");
            for (String clase : model.getListaClases()) {
                int[] tpFpTnFn = res.metricasClase.get(clase);
                double[] m = res.metricasCalculadas.get(clase);
                sbTexto.append(String.format("Clase %s -> TP:%d, FP:%d, TN:%d, FN:%d\n",
                        clase, tpFpTnFn[0], tpFpTnFn[1], tpFpTnFn[2], tpFpTnFn[3]));
                sbTexto.append(String.format("   Prec: %.2f%% | Rec: %.2f%% | Acc: %.2f%% | F1: %.2f%%\n",
                        m[0]*100, m[1]*100, m[2]*100, m[3]*100));
            }

            sbTexto.append("\n--- PROMEDIO GENERAL ---\n");
            sbTexto.append(String.format("Precisión: %.2f%%\nRecall: %.2f%%\nAccuracy: %.2f%%\nF-Score: %.2f%%\n",
                    res.promediosGenerales[0]*100, res.promediosGenerales[1]*100,
                    res.promediosGenerales[2]*100, res.promediosGenerales[3]*100));

            // Se actualiza la tabla (matriz de confusión) y el área de texto en la vista
            view.mostrarResultadosFinales(res.matrizConf, model.getListaClases(), sbTexto.toString());

            // Mensaje final indicando dónde se guardaron los archivos
            view.mostrarMensaje("Procesamiento completo finalizado.\nArchivos guardados exitosamente en:\n" + dirDestinoFinal.getAbsolutePath(), "Éxito", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            view.mostrarMensaje("Error en el procesamiento: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
}