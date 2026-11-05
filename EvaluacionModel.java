import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EvaluacionModel {
    private List<RegistroDato> dataset = new ArrayList<>();
    private List<String> clases = new ArrayList<>();
    private int numClases;
    private int currentIndex = 0;

    // Clase interna para representar un registro
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

    // Clase interna para los resultados
    public static class ResultadosProcesamiento {
        public int[][] matrizConf;
        public Map<String, int[]> metricasClase; // [tp, fp, tn, fn]
        public Map<String, double[]> metricasCalculadas; // [precision, recall, accuracy, fscore]
        public double[] promediosGenerales; // [avgPrecision, avgRecall, avgAccuracy, avgFScore]

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

    public void cargarDataset(String rutaArchivo, String strClases) throws IOException {
        // Configurar clases
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
        
        try (BufferedReader br = new BufferedReader(new FileReader(rutaArchivo))) {
            String linea;
            while ((linea = br.readLine()) != null) {
                if (linea.trim().isEmpty()) continue;
                String[] partes = linea.split(",");
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

    public String evaluarRegistro(RegistroDato reg, String clase) {
        boolean realEsClase = reg.claseVerdadera.equals(clase);
        boolean predEsClase = reg.clasePrediccion.equals(clase);

        if (realEsClase && predEsClase) return "TP";
        if (!realEsClase && !predEsClase) return "TN";
        if (!realEsClase && predEsClase) return "FP";
        return "FN"; // realEsClase && !predEsClase
    }

    public ResultadosProcesamiento procesarCompleto(String carpetaDestino, String nombreArchivoFinal,
                                                    String nombreMatriz, String prefijoClases) throws IOException {
        int n = clases.size();
        int[][] matrizConf = new int[n][n];
        Map<String, Integer> claseMap = new HashMap<>();
        for (int i = 0; i < n; i++) {
            claseMap.put(clases.get(i), i);
        }

        Map<String, List<String[]>> evaluacionesPorClase = new HashMap<>();
        for (String c : clases) {
            evaluacionesPorClase.put(c, new ArrayList<>());
        }

        for (RegistroDato reg : dataset) {
            int vIdx = claseMap.getOrDefault(reg.claseVerdadera, -1);
            int pIdx = claseMap.getOrDefault(reg.clasePrediccion, -1);
            if (vIdx != -1 && pIdx != -1) {
                matrizConf[vIdx][pIdx]++;
            }

            for (String clase : clases) {
                String val = evaluarRegistro(reg, clase);
                evaluacionesPorClase.get(clase).add(new String[]{reg.id, reg.claseVerdadera, reg.clasePrediccion, val});
            }
        }

        // Calcular TP, FP, TN, FN
        Map<String, int[]> metricasClase = new HashMap<>();
        for (int i = 0; i < n; i++) {
            String clase = clases.get(i);
            int tp = matrizConf[i][i];
            int fp = 0, fn = 0, tn = 0;

            for (int j = 0; j < n; j++) {
                if (j != i) {
                    fp += matrizConf[j][i]; // Columna
                    fn += matrizConf[i][j]; // Fila
                }
            }
            tn = dataset.size() - (tp + fp + fn);
            metricasClase.put(clase, new int[]{tp, fp, tn, fn});
        }

        // Crear carpeta destino
        File dirDestino = new File(carpetaDestino);
        if (!dirDestino.exists()) {
            dirDestino.mkdirs();
        }

        // Escribir archivos por clase
        for (String clase : clases) {
            int[] sumas = metricasClase.get(clase);
            File archivoClase = new File(dirDestino, prefijoClases + clase + ".csv");
            try (FileWriter fw = new FileWriter(archivoClase)) {
                fw.write("=== EVALUACION PARA LA CLASE " + clase + " ===\n");
                fw.write("ID Dato de prueba,Clase Verdadera,Clase Prediccion,Evaluacion (TP/TN/FP/FN)\n");
                for (String[] fila : evaluacionesPorClase.get(clase)) {
                    fw.write(String.join(",", fila) + "\n");
                }
                fw.write("\n=== SUMATORIAS PARA LA CLASE " + clase + " ===\n");
                fw.write("Sum TP," + sumas[0] + "\n");
                fw.write("Sum TN," + sumas[2] + "\n");
                fw.write("Sum FP," + sumas[1] + "\n");
                fw.write("Sum FN," + sumas[3] + "\n");
            }
        }

        // Calcular métricas y armar archivo final
        StringBuilder sbFinal = new StringBuilder();
        sbFinal.append("=== SUMAS DE TP, FP, TN, FN POR CLASE ===\n");
        sbFinal.append("Clase,TP,FP,TN,FN\n");

        double sumPrecision = 0, sumRecall = 0, sumAccuracy = 0, sumFScore = 0;
        Map<String, double[]> metricasCalculadas = new HashMap<>();

        for (String clase : clases) {
            int[] vals = metricasClase.get(clase);
            int tp = vals[0], fp = vals[1], tn = vals[2], fn = vals[3];
            sbFinal.append(clase).append(",").append(tp).append(",").append(fp).append(",").append(tn).append(",").append(fn).append("\n");

            double precision = (tp + fp == 0) ? 0.0 : (double) tp / (tp + fp);
            double recall = (tp + fn == 0) ? 0.0 : (double) tp / (tp + fn);
            double accuracy = (tp + tn + fp + fn == 0) ? 0.0 : (double) (tp + tn) / (tp + tn + fp + fn);
            double fscore = (precision + recall == 0) ? 0.0 : 2 * (precision * recall) / (precision + recall);

            metricasCalculadas.put(clase, new double[]{precision, recall, accuracy, fscore});
            sumPrecision += precision;
            sumRecall += recall;
            sumAccuracy += accuracy;
            sumFScore += fscore;
        }

        sbFinal.append("\n=== METRICAS POR CLASE ===\n");
        sbFinal.append("Clase,Precision,Recall,Accuracy,F-Score\n");
        for (String clase : clases) {
            double[] m = metricasCalculadas.get(clase);
            sbFinal.append(String.format("%s,%.4f,%.4f,%.4f,%.4f\n", clase, m[0], m[1], m[2], m[3]));
        }

        double avgPrecision = sumPrecision / n;
        double avgRecall = sumRecall / n;
        double avgAccuracy = sumAccuracy / n;
        double avgFScore = sumFScore / n;

        sbFinal.append("\n=== PROMEDIO GENERAL ===\n");
        sbFinal.append("Precision,Recall,Accuracy,F-Score\n");
        sbFinal.append(String.format("%.4f,%.4f,%.4f,%.4f\n", avgPrecision, avgRecall, avgAccuracy, avgFScore));

        if (nombreArchivoFinal == null || nombreArchivoFinal.trim().isEmpty()) {
            nombreArchivoFinal = "ResultadosFinal.csv";
        }
        File archivoFinal = new File(dirDestino, nombreArchivoFinal);
        try (FileWriter fw = new FileWriter(archivoFinal)) {
            fw.write(sbFinal.toString());
        }

        // Escribir matriz de confusión
        if (nombreMatriz == null || nombreMatriz.trim().isEmpty()) {
            nombreMatriz = "MatrizConfusion.csv";
        }
        File archivoMatriz = new File(dirDestino, nombreMatriz);
        try (FileWriter fw = new FileWriter(archivoMatriz)) {
            fw.write("ClaseVerdadera/Prediccion," + String.join(",", clases) + "\n");
            for (int i = 0; i < n; i++) {
                StringBuilder filaCsv = new StringBuilder(clases.get(i));
                for (int j = 0; j < n; j++) {
                    filaCsv.append(",").append(matrizConf[i][j]);
                }
                fw.write(filaCsv.toString() + "\n");
            }
        }

        return new ResultadosProcesamiento(matrizConf, metricasClase, metricasCalculadas, 
                                          new double[]{avgPrecision, avgRecall, avgAccuracy, avgFScore});
    }

    // Métodos para el modo paso a paso (Modo A)
    public int getTotalRegistros() {
        return dataset.size();
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
    }

    public RegistroDato obtenerSiguienteDato() {
        if (currentIndex < dataset.size()) {
            return dataset.get(currentIndex++);
        }
        return null;
    }

    public void reiniciarPasoAPaso() {
        currentIndex = 0;
    }
}