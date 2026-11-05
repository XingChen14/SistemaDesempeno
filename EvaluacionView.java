import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

/**
 * VISTA: Maneja toda la interfaz gráfica (Swing).
 * NO contiene lógica de negocio ni cálculos de métricas.
 * Solo muestra datos y captura las acciones del usuario.
 */
public class EvaluacionView extends JFrame {
    // --- Componentes de la pestaña 1 (Configuración) ---
    private JTextField txtArchivoCSV;
    private JTextField txtClases;
    private JButton btnExaminar;
    private JButton btnCargarDatos;

    // --- Componentes de la pestaña 2 (Modo A - Paso a paso) ---
    private JTextArea txtAreaModoA;
    private JButton btnSiguienteDato;
    private JButton btnProcesarRestante;
    private JButton btnTerminarModoA;

    // --- Componentes de la pestaña 3 (Modo B - Resultados) ---
    private JButton btnProcesarTodo;
    private JTable tablaMatriz;
    private JTextArea txtResultadosFinales;
    private JTextField txtNombreArchivoFinal;
    private JTextField txtCarpetaSalida;
    private JTextField txtNombreMatriz;
    private JTextField txtNombreClases;
    private JButton btnExaminarCarpeta;

    /**
     * Constructor: configura las propiedades básicas de la ventana.
     */
    public EvaluacionView() {
        setTitle("Sistema MVC - Estimación de Desempeño de Clasificación");
        setSize(950, 700); // Tamaño inicial de la ventana
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la ventana en pantalla
        initUI(); // Construye los componentes
    }

    /**
     * Construye toda la interfaz usando un JTabbedPane con 3 pestañas.
     */
    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // ============================================================
        // PESTAÑA 1: Configuración y Carga del Dataset
        // ============================================================
        JPanel panelConfig = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: campo para la ruta del CSV y botón examinar
        gbc.gridx = 0; gbc.gridy = 0;
        panelConfig.add(new JLabel("Archivo CSV:"), gbc);
        txtArchivoCSV = new JTextField(25);
        gbc.gridx = 1; panelConfig.add(txtArchivoCSV, gbc);
        btnExaminar = new JButton("Examinar");
        gbc.gridx = 2; panelConfig.add(btnExaminar, gbc);

        // Fila 1: campo para las clases
        gbc.gridx = 0; gbc.gridy = 1;
        panelConfig.add(new JLabel("Clases (ej: A,B,C):"), gbc);
        txtClases = new JTextField("A,B,C", 25);
        gbc.gridx = 1; panelConfig.add(txtClases, gbc);

        // Fila 2: botón de cargar
        btnCargarDatos = new JButton("Cargar y Validar Dataset");
        gbc.gridx = 1; gbc.gridy = 2;
        panelConfig.add(btnCargarDatos, gbc);
        tabbedPane.addTab("1. Configuración y Carga", panelConfig);

        // ============================================================
        // PESTAÑA 2: Modo A (Paso a paso)
        // ============================================================
        JPanel panelModoA = new JPanel(new BorderLayout());
        txtAreaModoA = new JTextArea();
        txtAreaModoA.setEditable(false); // Solo lectura
        panelModoA.add(new JScrollPane(txtAreaModoA), BorderLayout.CENTER);

        // Panel inferior con los 3 botones
        JPanel panelBotonesA = new JPanel();
        btnSiguienteDato = new JButton("Siguiente Dato");
        btnProcesarRestante = new JButton("Procesar lo Restante");
        btnTerminarModoA = new JButton("Terminar Proceso");

        setControlesModoAEnabled(false); // Deshabilitados hasta cargar dataset

        panelBotonesA.add(btnSiguienteDato);
        panelBotonesA.add(btnProcesarRestante);
        panelBotonesA.add(btnTerminarModoA);
        panelModoA.add(panelBotonesA, BorderLayout.SOUTH);
        tabbedPane.addTab("2. Modo A (Paso a Paso)", panelModoA);

        // ============================================================
        // PESTAÑA 3: Modo B (Resultados) + Matriz de Confusión
        // ============================================================
        JPanel panelModoB = new JPanel(new BorderLayout());

        // --- Panel superior: controles para nombres de archivos ---
        JPanel panelTopB = new JPanel(new GridBagLayout());
        GridBagConstraints gbcB = new GridBagConstraints();
        gbcB.insets = new Insets(5, 5, 5, 5);
        gbcB.fill = GridBagConstraints.HORIZONTAL;

        // Fila 0: carpeta de guardado
        gbcB.gridx = 0; gbcB.gridy = 0;
        panelTopB.add(new JLabel("Carpeta de Guardado:"), gbcB);
        txtCarpetaSalida = new JTextField("C:\\Users\\orimu\\Downloads\\Inteligencia artificial\\SistemaDesempeno\\Resultados", 20);
        gbcB.gridx = 1; panelTopB.add(txtCarpetaSalida, gbcB);
        btnExaminarCarpeta = new JButton("Elegir Carpeta");
        gbcB.gridx = 2; panelTopB.add(btnExaminarCarpeta, gbcB);

        // Fila 1: nombre del archivo final
        gbcB.gridx = 0; gbcB.gridy = 1;
        panelTopB.add(new JLabel("Nombre Archivo Final:"), gbcB);
        txtNombreArchivoFinal = new JTextField("ResultadosFinal.csv", 20);
        gbcB.gridx = 1; panelTopB.add(txtNombreArchivoFinal, gbcB);

        // Fila 1 (col 2): nombre de la matriz
        gbcB.gridx = 2; gbcB.gridy = 1;
        panelTopB.add(new JLabel("Nombre Matriz:"), gbcB);
        txtNombreMatriz = new JTextField("MatrizConfusion.csv", 15);
        gbcB.gridx = 3; panelTopB.add(txtNombreMatriz, gbcB);

        // Fila 2: prefijo de archivos por clase
        gbcB.gridx = 0; gbcB.gridy = 2;
        panelTopB.add(new JLabel("Prefijo Archivos por Clase:"), gbcB);
        txtNombreClases = new JTextField("Evaluacion_Clase_", 15);
        gbcB.gridx = 1; panelTopB.add(txtNombreClases, gbcB);

        // Fila 2 (col 2 y 3): botón para procesar TODO
        btnProcesarTodo = new JButton("Procesar Dataset Completo");
        btnProcesarTodo.setEnabled(false); // Deshabilitado hasta cargar dataset
        gbcB.gridx = 2; gbcB.gridy = 2; gbcB.gridwidth = 2;
        panelTopB.add(btnProcesarTodo, gbcB);

        panelModoB.add(panelTopB, BorderLayout.NORTH);

        // --- Panel central: tabla de matriz + área de texto de métricas ---
        JPanel panelCentroB = new JPanel(new GridLayout(1, 2));

        // Lado izquierdo: Matriz de confusión (JTable)
        tablaMatriz = new JTable();
        JPanel panelMatrizContainer = new JPanel(new BorderLayout());
        panelMatrizContainer.setBorder(BorderFactory.createTitledBorder("Matriz de Confusión"));
        panelMatrizContainer.add(new JScrollPane(tablaMatriz), BorderLayout.CENTER);
        panelCentroB.add(panelMatrizContainer);

        // Lado derecho: Área de texto con las métricas
        txtResultadosFinales = new JTextArea();
        txtResultadosFinales.setEditable(false);
        JPanel panelResContainer = new JPanel(new BorderLayout());
        panelResContainer.setBorder(BorderFactory.createTitledBorder("Métricas Generales y por Clase"));
        panelResContainer.add(new JScrollPane(txtResultadosFinales), BorderLayout.CENTER);
        panelCentroB.add(panelResContainer);

        panelModoB.add(panelCentroB, BorderLayout.CENTER);
        tabbedPane.addTab("3. Modo B & Resultados", panelModoB);

        // Se agrega el panel de pestañas a la ventana
        add(tabbedPane);
    }

    // ============================================================
    // MÉTODOS DE CONTROL Y ACCESO (llamados por el Controlador)
    // ============================================================

    public void setControlesModoAEnabled(boolean enabled) {
        btnSiguienteDato.setEnabled(enabled);
        btnProcesarRestante.setEnabled(enabled);
        btnTerminarModoA.setEnabled(enabled);
    }

    public void setProcesarTodoEnabled(boolean enabled) {
        btnProcesarTodo.setEnabled(enabled);
    }

    // Getters para obtener los datos escritos por el usuario
    public String getRutaArchivo() { return txtArchivoCSV.getText().trim(); }
    public String getClasesTexto() { return txtClases.getText().trim(); }
    public String getNombreArchivoFinal() { return txtNombreArchivoFinal.getText().trim(); }
    public String getCarpetaSalida() { return txtCarpetaSalida.getText().trim(); }
    public String getNombreMatriz() { return txtNombreMatriz.getText().trim(); }
    public String getNombreClases() { return txtNombreClases.getText().trim(); }

    // Setters para actualizar campos desde el controlador
    public void setRutaArchivo(String ruta) { txtArchivoCSV.setText(ruta); }
    public void setCarpetaSalida(String ruta) { txtCarpetaSalida.setText(ruta); }

    // Utilidades para mostrar y limpiar el área del Modo A
    public void appendModoA(String texto) { txtAreaModoA.append(texto); }
    public void setModoAText(String texto) { txtAreaModoA.setText(texto); }

    /**
     * Actualiza la tabla (matriz) y el área de texto (métricas) con los resultados.
     * Recibe la matriz ya calculada por el Modelo.
     */
    public void mostrarResultadosFinales(int[][] matriz, java.util.List<String> clases, String textoMetricas) {
        int n = clases.size();

        // Construcción de las columnas de la tabla (Verd\Pred, Clase1, Clase2, ...)
        String[] columnas = new String[n + 1];
        columnas[0] = "Verd\\Pred";
        for (int i = 0; i < n; i++) columnas[i + 1] = clases.get(i);

        // Construcción de los datos de la tabla (a partir de la matriz NxN)
        Object[][] datosTabla = new Object[n][n + 1];
        for (int i = 0; i < n; i++) {
            datosTabla[i][0] = clases.get(i);
            for (int j = 0; j < n; j++) {
                datosTabla[i][j + 1] = matriz[i][j];
            }
        }
        tablaMatriz.setModel(new DefaultTableModel(datosTabla, columnas));
        txtResultadosFinales.setText(textoMetricas);
    }

    /**
     * Muestra un cuadro de diálogo con un mensaje.
     */
    public void mostrarMensaje(String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    // ============================================================
    // REGISTRO DE LISTENERS (usados por el Controlador)
    // ============================================================
    public void addExaminarListener(ActionListener listen) { btnExaminar.addActionListener(listen); }
    public void addExaminarCarpetaListener(ActionListener listen) { btnExaminarCarpeta.addActionListener(listen); }
    public void addCargarListener(ActionListener listen) { btnCargarDatos.addActionListener(listen); }
    public void addSiguienteListener(ActionListener listen) { btnSiguienteDato.addActionListener(listen); }
    public void addProcesarRestanteListener(ActionListener listen) { btnProcesarRestante.addActionListener(listen); }
    public void addTerminarAListener(ActionListener listen) { btnTerminarModoA.addActionListener(listen); }
    public void addProcesarTodoListener(ActionListener listen) { btnProcesarTodo.addActionListener(listen); }
}