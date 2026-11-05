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

public class EvaluacionView extends JFrame {
    private JTextField txtArchivoCSV;
    private JTextField txtClases;
    private JButton btnExaminar;
    private JButton btnCargarDatos;
    
    private JTextArea txtAreaModoA;
    private JButton btnSiguienteDato;
    private JButton btnProcesarRestante;
    private JButton btnTerminarModoA;
    
    private JButton btnProcesarTodo;
    private JTable tablaMatriz;
    private JTextArea txtResultadosFinales;
    private JTextField txtNombreArchivoFinal;
    
    // Nuevos componentes para la carpeta de salida
    private JTextField txtCarpetaSalida;
    private JButton btnExaminarCarpeta;

    public EvaluacionView() {
        setTitle("Sistema MVC - Estimación de Desempeño de Clasificación");
        setSize(950, 800);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        initUI();
    }

    private void initUI() {
        JTabbedPane tabbedPane = new JTabbedPane();

        // Panel 1: Configuración
        JPanel panelConfig = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = 0;
        panelConfig.add(new JLabel("Archivo CSV:"), gbc);
        txtArchivoCSV = new JTextField(25);
        gbc.gridx = 1; panelConfig.add(txtArchivoCSV, gbc);
        btnExaminar = new JButton("Examinar");
        gbc.gridx = 2; panelConfig.add(btnExaminar, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        panelConfig.add(new JLabel("Clases (ej: A,B,C):"), gbc);
        txtClases = new JTextField("A,B,C", 25);
        gbc.gridx = 1; panelConfig.add(txtClases, gbc);

        btnCargarDatos = new JButton("Cargar y Validar Dataset");
        gbc.gridx = 1; gbc.gridy = 2;
        panelConfig.add(btnCargarDatos, gbc);
        tabbedPane.addTab("1. Configuración y Carga", panelConfig);

        // Panel 2: Modo A
        JPanel panelModoA = new JPanel(new BorderLayout());
        txtAreaModoA = new JTextArea();
        txtAreaModoA.setEditable(false);
        panelModoA.add(new JScrollPane(txtAreaModoA), BorderLayout.CENTER);

        JPanel panelBotonesA = new JPanel();
        btnSiguienteDato = new JButton("Siguiente Dato");
        btnProcesarRestante = new JButton("Procesar lo Restante");
        btnTerminarModoA = new JButton("Terminar Proceso");
        
        setControlesModoAEnabled(false);

        panelBotonesA.add(btnSiguienteDato);
        panelBotonesA.add(btnProcesarRestante);
        panelBotonesA.add(btnTerminarModoA);
        panelModoA.add(panelBotonesA, BorderLayout.SOUTH);
        tabbedPane.addTab("2. Modo A (Paso a Paso)", panelModoA);

        // Panel 3: Modo B y Resultados
        JPanel panelModoB = new JPanel(new BorderLayout());
        
        // Panel superior para controles de archivos de salida
        JPanel panelTopB = new JPanel(new GridBagLayout());
        GridBagConstraints gbcB = new GridBagConstraints();
        gbcB.insets = new Insets(5, 5, 5, 5);
        gbcB.fill = GridBagConstraints.HORIZONTAL;

        gbcB.gridx = 0; gbcB.gridy = 0;
        panelTopB.add(new JLabel("Carpeta de Guardado:"), gbcB);
        txtCarpetaSalida = new JTextField("C:\\Users\\orimu\\Downloads\\Inteligencia artificial\\SistemaDesempeno\\Resultados", 20);
        gbcB.gridx = 1; panelTopB.add(txtCarpetaSalida, gbcB);
        btnExaminarCarpeta = new JButton("Elegir Carpeta");
        gbcB.gridx = 2; panelTopB.add(btnExaminarCarpeta, gbcB);

        gbcB.gridx = 0; gbcB.gridy = 1;
        panelTopB.add(new JLabel("Nombre Archivo Final:"), gbcB);
        txtNombreArchivoFinal = new JTextField("ResultadosFinal.csv", 20);
        gbcB.gridx = 1; panelTopB.add(txtNombreArchivoFinal, gbcB);

        btnProcesarTodo = new JButton("Procesar Dataset Completo");
        btnProcesarTodo.setEnabled(false);
        gbcB.gridx = 2; panelTopB.add(btnProcesarTodo, gbcB);

        panelModoB.add(panelTopB, BorderLayout.NORTH);

        JPanel panelCentroB = new JPanel(new GridLayout(1, 2));
        tablaMatriz = new JTable();
        JPanel panelMatrizContainer = new JPanel(new BorderLayout());
        panelMatrizContainer.setBorder(BorderFactory.createTitledBorder("Matriz de Confusión"));
        panelMatrizContainer.add(new JScrollPane(tablaMatriz), BorderLayout.CENTER);
        panelCentroB.add(panelMatrizContainer);

        txtResultadosFinales = new JTextArea();
        txtResultadosFinales.setEditable(false);
        JPanel panelResContainer = new JPanel(new BorderLayout());
        panelResContainer.setBorder(BorderFactory.createTitledBorder("Métricas Generales y por Clase"));
        panelResContainer.add(new JScrollPane(txtResultadosFinales), BorderLayout.CENTER);
        panelCentroB.add(panelResContainer);

        panelModoB.add(panelCentroB, BorderLayout.CENTER);
        tabbedPane.addTab("3. Modo B & Resultados", panelModoB);

        add(tabbedPane);
    }

    public void setControlesModoAEnabled(boolean enabled) {
        btnSiguienteDato.setEnabled(enabled);
        btnProcesarRestante.setEnabled(enabled);
        btnTerminarModoA.setEnabled(enabled);
    }

    public void setProcesarTodoEnabled(boolean enabled) {
        btnProcesarTodo.setEnabled(enabled);
    }

    public String getRutaArchivo() { return txtArchivoCSV.getText().trim(); }
    public String getClasesTexto() { return txtClases.getText().trim(); }
    public String getNombreArchivoFinal() { return txtNombreArchivoFinal.getText().trim(); }
    public String getCarpetaSalida() { return txtCarpetaSalida.getText().trim(); }
    public void setRutaArchivo(String ruta) { txtArchivoCSV.setText(ruta); }
    public void setCarpetaSalida(String ruta) { txtCarpetaSalida.setText(ruta); }
    
    public void appendModoA(String texto) { txtAreaModoA.append(texto); }
    public void setModoAText(String texto) { txtAreaModoA.setText(texto); }

    public void mostrarResultadosFinales(int[][] matriz, java.util.List<String> clases, String textoMetricas) {
        int n = clases.size();
        String[] columnas = new String[n + 1];
        columnas[0] = "Verd\\Pred";
        for (int i = 0; i < n; i++) columnas[i + 1] = clases.get(i);

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

    public void mostrarMensaje(String mensaje, String titulo, int tipo) {
        JOptionPane.showMessageDialog(this, mensaje, titulo, tipo);
    }

    // Listeners
    public void addExaminarListener(ActionListener listen) { btnExaminar.addActionListener(listen); }
    public void addExaminarCarpetaListener(ActionListener listen) { btnExaminarCarpeta.addActionListener(listen); }
    public void addCargarListener(ActionListener listen) { btnCargarDatos.addActionListener(listen); }
    public void addSiguienteListener(ActionListener listen) { btnSiguienteDato.addActionListener(listen); }
    public void addProcesarRestanteListener(ActionListener listen) { btnProcesarRestante.addActionListener(listen); }
    public void addTerminarAListener(ActionListener listen) { btnTerminarModoA.addActionListener(listen); }
    public void addProcesarTodoListener(ActionListener listen) { btnProcesarTodo.addActionListener(listen); }
}