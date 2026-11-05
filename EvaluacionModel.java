import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * MODELO: Contiene TODA la lógica de negocio.
 * Aquí se calculan manualmente las métricas (TP, TN, FP, FN, Precision, Recall, Accuracy, F-Score)
 * SIN usar librerías externas de Machine Learning ni de estadística.
 */
public class EvaluacionModel {

    // Lista que almacena todos los registros del CSV cargado
    private List<RegistroDato> dataset = new ArrayList<>();
    // Lista de las clases posibles (ej. A, B, C)
    private List<String> clases = new ArrayList<>();
    private int numClases;
    private int currentIndex = 0;

    // ============================================================
    // CLASE INTERNA: Representa un solo registro (una fila del CSV)
    // ============================================================
    public static class RegistroDato {
        public String id;
        public String claseVerdadera;
        public String clasePrediccion;

        public RegistroDato(String id, String claseVerdadera, String clasePrediccion) {
            this.id = id.trim();
            this.claseVerdadera = claseVerdadera.trim();
            this.clasePrediccion = clasePrediccion.trim();
        }
    }

    // ============================================================
    // CLASE INTERNA: Encapsula TODOS los resultados del procesamiento
    // ============================================================
    public static class ResultadosProcesamiento {
        public int[][] matrizConf;                          // Matriz de confusión NxN
        public Map<String, int[]> metricasClase;            // [tp, fp, tn, fn] por clase
        public Map<String, double[]> metricasCalculadas;    // [precision, recall, accuracy, fscore]
        public double[] promediosGenerales;                 // [avgPrecision, avgRecall, avgAccuracy, avgFScore]

        public ResultadosProcesamiento(int[][] matrizConf, Map<String, int[]> metricasClase,
                                       Map<String, double[]> metricasCalculadas, double[] promediosGenerales) {
            this.matrizConf = matrizConf;
            this.metricasClase = metricasClase;
            this.metricasCalculadas = metricasCalculadas;
            this.promediosGenerales = promediosGenerales;
        }
    }

    public EvaluacionModel() {
        this.dataset = new ArrayList<>();
        this.clases = new ArrayList<>();
    }

    /**
     * Lee el archivo CSV y llena la lista de registros.
     * @param rutaArchivo Ruta física del archivo .csv
     * @param strClases Cadena con las clases separadas por coma (ej. "A,B,C")
     */
    public void cargarDataset(String rutaArchivo, String strClases) throws IOException {
        // Configurar la lista de clases
        String[] arrClases = strClases.split(",");
        clases.clear();
        for (String c : arrClases) {
            if (!c.trim().isEmpty()) {
                clases.add(c.trim());
            }
        }
        this.numClases = clases.size();

        dataset.clear();
        currentIndex = 0;

        // Lectura del archivo línea por línea (API estándar de Java)
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue; // Ignorar líneas vacías
                String[] partes = linea.split(",");
                // Se esperan al menos 3 columnas: ID, Clase Verdadera, Clase Predicción
                if (partes.length >= 3) {
                    dataset.add(new RegistroDato(partes[0], partes[1], partes[2]));
                }
            }
        }

        if (dataset.isEmpty()) {
            throw new IOException("El archivo CSV está vacío o no tiene el formato correcto.");
        }
    }

    public List<RegistroDato> getDataset() { return dataset; }
    public List<String> getListaClases() { return clases; }

    /**
     * Evalúa UN registro contra UNA clase específica y devuelve su clasificación:
     * TP = Verdadero Positivo, TN = Verdadero Negativo, FP = Falso Positivo, FN = Falso Negativo.
     * 
     * LÓGICA MANUAL (sin librerías):
     * - TP: la clase real ES la clase evaluada Y la predicción ES la clase evaluada.
     * - TN: la clase real NO ES la clase evaluada Y la predicción NO ES la clase evaluada.
     * - FP: la clase real NO ES la clase evaluada PERO la predicción SÍ ES la clase evaluada.
     * - FN: la clase real ES la clase evaluada PERO la predicción NO ES la clase evaluada.
     */
    public String evaluarRegistro(RegistroDato reg, String clase) {
        boolean realEsClase = reg.claseVerdadera.equals(clase);
        boolean predEsClase = reg.clasePrediccion.equals(clase);

        if (realEsClase && predEsClase) return "TP";
        if (!realEsClase && !predEsClase) return "TN";
        if (!realEsClase && predEsClase) return "FP";
        return "FN"; // realEsClase && !predEsClase
    }

    /**
     * Procesa TODO el dataset, calcula las métricas manualmente y genera los archivos CSV.
     * 
     * @param carpetaDestino Carpeta donde se guardarán los resultados
     * @param nombreArchivoFinal Nombre del CSV de resultados finales
     * @param nombreMatriz Nombre del CSV de la matriz de confusión
     * @param prefijoClases Prefijo de los CSVs individuales por clase
     */
    public ResultadosProcesamiento procesarCompleto(String carpetaDestino, String nombreArchivoFinal,
                                                    String nombreMatriz, String prefijoClases) throws IOException {
        int n = clases.size();
        // Matriz de confusión NxN inicializada en ceros
        int[][] matrizConf = new int[n][n];

        // Mapa para traducir el nombre de la clase a su índice en la matriz
        Map<String, Integer> claseMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            claseMap.put(clases.get(i), i);
        }

        // Estructura para guardar el detalle de las evaluaciones por clase (para los CSVs)
        Map<String, List<String[]>> evaluacionesPorClase = new HashMap<>();
        for (String c : clases) {
            evaluacionesPorClase.put(c, new ArrayList<>());
        }

        // ============================================================
        // RECORRIDO DEL DATASET: Se llena la matriz de confusión
        // y se calcula el TP/TN/FP/FN de cada registro para cada clase.
        // ============================================================
        for (RegistroDato reg : dataset) {
            int vIdx = claseMap.getOrDefault(reg.claseVerdadera, -1);
            int pIdx = claseMap.getOrDefault(reg.clasePrediccion, -1);
            // Se incrementa la celda correspondiente de la matriz
            if (vIdx != -1 && pIdx != -1) {
                matrizConf[vIdx][pIdx]++;
            }

            // Se evalúa el registro contra TODAS las clases
            for (String clase : clases) {
                String val = evaluarRegistro(reg, clase);
                evaluacionesPorClase.get(clase).add(new String[]{reg.id, reg.claseVerdadera, reg.clasePrediccion, val});
            }
        }

        // ============================================================
        // CÁLCULO MANUAL DE TP, FP, TN, FN POR CLASE
        // A partir de la matriz de confusión:
        //   TP = diagonal principal (matrizConf[i][i])
        //   FP = suma de la COLUMNA i (excluyendo TP)
        //   FN = suma de la FILA i (excluyendo TP)
        //   TN = Total registros - (TP + FP + FN)
        // ============================================================
        Map<String, int[]> metricasClase = new HashMap<>();
        for (int i = 0; i < n; i++) {
            String clase = clases.get(i);
            int tp = matrizConf[i][i];
            int fp = 0, fn = 0, tn = 0;

            for (int j = 0; j < n; j++) {
                if (j != i) {
                    fp += matrizConf[j][i]; // Suma de la columna (falsos positivos)
                    fn += matrizConf[i][j]; // Suma de la fila (falsos negativos)
                }
            }
            tn = dataset.size() - (tp + fp + fn); // Verdaderos negativos
            metricasClase.put(clase, new int[]{tp, fp, tn, fn});
        }

        // Crear la carpeta destino si no existe
        File dirDestino = new File(carpetaDestino);
        if (!dirDestino.exists()) {
            dirDestino.mkdirs();
        }

        // ============================================================
        // GENERACIÓN DE ARCHIVOS CSV POR CADA CLASE
        // ============================================================
        for (String clase : clases) {
            int[] sumas = metricasClase.get(clase);
            File archivoClase = new File(dirDestino, prefijoClases + clase + ".csv");
            try (FileWriter fw = new FileWriter(archivoClase)) {
                fw.write("=== EVALUACION PARA LA CLASE " + clase + " ===\n");
                fw.write("ID Dato de prueba,Clase Verdadera,Clase Prediccion,Evaluacion (TP/TN/FP/FN)\n");
                for (String[] fila : evaluacionesPorClase.get(clase)) {
                    fw.write(String.join(",", fila) + "\n");
                }
                // Sección de sumatorias (Paso 2 del taller)
                fw.write("\n=== SUMATORIAS PARA LA CLASE " + clase + " ===\n");
                fw.write("Sum TP," + sumas[0] + "\n");
                fw.write("Sum TN," + sumas[2] + "\n");
                fw.write("Sum FP," + sumas[1] + "\n");
                fw.write("Sum FN," + sumas[3] + "\n");
            }
        }

        // ============================================================
        // CÁLCULO MANUAL DE LAS MÉTRICAS: Precision, Recall, Accuracy, F-Score
        // FÓRMULAS APLICADAS DIRECTAMENTE (sin librerías externas):
        //   Precision = TP / (TP + FP)
        //   Recall    = TP / (TP + FN)
        //   Accuracy  = (TP + TN) / (TP + TN + FP + FN)
        //   F-Score   = 2 * (Precision * Recall) / (Precision + Recall)
        // ============================================================
        StringBuilder sbFinal = new StringBuilder();
        sbFinal.append("=== SUMAS DE TP, FP, TN, FN POR CLASE ===\n");
        sbFinal.append("Clase,TP,FP,TN,FN\n");

        double sumPrecision = 0, sumRecall = 0, sumAccuracy = 0, sumFScore = 0;
        Map<String, double[]> metricasCalculadas = new HashMap<>();

        for (String clase : clases) {
            int[] vals = metricasClase.get(clase);
            int tp = vals[0], fp = vals[1], tn = vals[2], fn = vals[3];
            sbFinal.append(clase).append(",").append(tp).append(",").append(fp).append(",").append(tn).append(",").append(fn).append("\n");

            // --- CÁLCULO MATEMÁTICO PURO (sin librerías) ---
            double precision = (tp + fp == 0) ? 0.0 : (double) tp / (tp + fp);
            double recall    = (tp + fn == 0) ? 0.0 : (double) tp / (tp + fn);
            double accuracy  = (tp + tn + fp + fn == 0) ? 0.0 : (double) (tp + tn) / (tp + tn + fp + fn);
            double fscore    = (precision + recall == 0) ? 0.0 : 2 * (precision * recall) / (precision + recall);

            metricasCalculadas.put(clase, new double[]{precision, recall, accuracy, fscore});
            // Acumuladores para el promedio general
            sumPrecision += precision;
            sumRecall += recall;
            sumAccuracy += accuracy;
            sumFScore += fscore;
        }

        // Sección de métricas por clase (Tabla 4 del taller)
        sbFinal.append("\n=== METRICAS POR CLASE ===\n");
        sbFinal.append("Clase,Precision,Recall,Accuracy,F-Score\n");
        for (String clase : clases) {
            double[] m = metricasCalculadas.get(clase);
            sbFinal.append(String.format("%s,%.4f,%.4f,%.4f,%.4f\n", clase, m[0], m[1], m[2], m[3]));
        }

        // ============================================================
        // CÁLCULO DEL PROMEDIO GENERAL (Tabla 5 del taller)
        // ============================================================
        double avgPrecision = sumPrecision / n;
        double avgRecall = sumRecall / n;
        double avgAccuracy = sumAccuracy / n;
        double avgFScore = sumFScore / n;

        sbFinal.append("\n=== PROMEDIO GENERAL ===\n");
        sbFinal.append("Precision,Recall,Accuracy,F-Score\n");
        sbFinal.append(String.format("%.4f,%.4f,%.4f,%.4f\n", avgPrecision, avgRecall, avgAccuracy, avgFScore));

        // ============================================================
        // ESCRITURA DEL ARCHIVO DE RESULTADOS FINALES
        // ============================================================
        if (nombreArchivoFinal == null || nombreArchivoFinal.trim().isEmpty()) {
            nombreArchivoFinal = "ResultadosFinal.csv";
        }
        File archivoFinal = new File(dirDestino, nombreArchivoFinal);
        try (FileWriter fw = new FileWriter(archivoFinal)) {
            fw.write(sbFinal.toString());
        }

        // ============================================================
        // ESCRITURA DE LA MATRIZ DE CONFUSIÓN (Paso 7 del taller)
        // ============================================================
        if (nombreMatriz == null || nombreMatriz.trim().isEmpty()) {
            nombreMatriz = "MatrizConfusion.csv";
        }
        File archivoMatriz = new File(dirDestino, nombreMatriz);
        try (FileWriter fw = new FileWriter(archivoMatriz)) {
            // Encabezado: primera celda vacía + nombres de las clases
            fw.write("ClaseVerdadera/Prediccion," + String.join(",", clases) + "\n");
            for (int i = 0; i < n; i++) {
                StringBuilder filaCsv = new StringBuilder(clases.get(i));
                for (int j = 0; j < n; j++) {
                    filaCsv.append(",").append(matrizConf[i][j]);
                }
                fw.write(filaCsv.toString() + "\n");
            }
        }

        // Se devuelven los resultados al controlador
        return new ResultadosProcesamiento(matrizConf, metricasClase, metricasCalculadas,
                                          new double[]{avgPrecision, avgRecall, avgAccuracy, avgFScore});
    }

    // ============================================================
    // MÉTODOS AUXILIARES PARA EL MODO A (PASO A PASO)
    // ============================================================
    public int getTotalRegistros() { return dataset.size(); }
    public int getCurrentIndex() { return currentIndex; }
    public void setCurrentIndex(int index) { this.currentIndex = index; }

    public RegistroDato obtenerSiguienteDato() {
        if (currentIndex < dataset.size()) {
            return dataset.get(currentIndex++);
        }
        return null;
    }

    public void reiniciarPasoAPaso() { currentIndex = 0; }
}