import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.logging.*;
import javax.swing.*;

public class Cliente extends Thread {
    JFrame applicationFrame;
    Thread someThread;
    
    private static boolean done = false;
    private Socket conexao;
    
    private JTextField campoMsg;
    private JLabel labelNome;
    private JTextArea areaConversa;
    private JComboBox boxClientes;
    
    private Boolean saiu = false;

    Cliente(Socket s) {
        conexao = s;
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                PrintStream saida;
                try {
                    saida = new PrintStream(conexao.getOutputStream());
                    
                    applicationFrame = new JFrame();
                    
                    // Adiciona o Listener para capturar o fechamento da janela
                    applicationFrame.addWindowListener(new WindowAdapter() {
                        @Override
                        public void windowClosing(WindowEvent evt) {
                            if (!saiu) {
                                if (JOptionPane.showConfirmDialog(null,"Deseja sair do Chat?")==JOptionPane.OK_OPTION){
                                    saida.println("");
                                    System.exit(0);
                                }
                            }else {
                                System.exit(0);
                            }
                        }
                    });
                    
                    // Label para colocar o nome
                    labelNome = new JLabel();
                    labelNome.setVisible(false);
                    labelNome.setText("");
                    labelNome.setForeground(Color.blue);
                    applicationFrame.add(labelNome, BorderLayout.NORTH);
                    
                    // Box inicial para seleção do sendToOne
                    boxClientes = new JComboBox(new String[]{"Todos"});
                    boxClientes.setBounds(200,0,183,17);
                    boxClientes.setVisible(false);
                    applicationFrame.add(boxClientes, BorderLayout.CENTER);
                    
                    // Area aonde todas as mensagens são visualizadas
                    areaConversa = new JTextArea();
                    areaConversa.setEditable(false);
                    areaConversa.setText("Digite seu nome");
                    areaConversa.setForeground(Color.RED);
                    areaConversa.setBackground(new java.awt.Color(255, 255, 215));
                    applicationFrame.add(new JScrollPane(areaConversa), BorderLayout.CENTER);
                    
                    // Campo de texto para digitar a mensagem
                    campoMsg = new JTextField();
                    campoMsg.setBounds(10, 10, 10, 10);
                    campoMsg.setEnabled(true);
                    campoMsg.addActionListener(new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            // Verifica se o nome já foi digitado,
                            // Se sim, segue com o código
                            // Se não, coloca o nome na label e limpa os campos
                            if (!labelNome.getText().replace(" (Online)", "").equals("")) {
                                if (!campoMsg.getText().equals("")) {
                                    // Manda para a saida o Nome selecionado no Box + Delimitador + Mensagem
                                    // Ex: Todos>#<Olá Mundo
                                    // Ex: Crush>#<Olá sumida
                                    saida.println(boxClientes.getSelectedItem() + ">#<" + campoMsg.getText());
                                    
                                    if (boxClientes.getSelectedItem().equals("Todos")) {
                                        areaConversa.setText(areaConversa.getText() + "(Público) Você disse: " + campoMsg.getText() + "\n");
                                    }else {
                                        areaConversa.setText(areaConversa.getText() + "(Privado) Você disse: " + campoMsg.getText() + "\n");
                                    }
                                }else {
                                    // Manda para a saida o campo em branco a fim de deslogar do servidor
                                    saida.println(campoMsg.getText());
                                    areaConversa.setText(areaConversa.getText() + "(Público) Você saiu do Chat!");
                                    saiu = true;
                                }
                                campoMsg.setText("");
                            }else {
                                labelNome.setText(campoMsg.getText() + " (Online)");
                                labelNome.setVisible(true);
                                boxClientes.setVisible(true);
                                areaConversa.setText("");
                                areaConversa.setForeground(Color.BLACK);
                                campoMsg.setText("");
                                saida.println(labelNome.getText().replace(" (Online)", ""));
                            }
                        }
                    });
                    applicationFrame.add(campoMsg, BorderLayout.PAGE_END);
                    
                    // Altera o comportamento da janela para travar o fechamento
                    applicationFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                    applicationFrame.setSize(400, 450);
                    applicationFrame.setVisible(true);
                    campoMsg.requestFocus();
                } catch (IOException ex) {
                    Logger.getLogger(Cliente.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        });

        someThread = new Thread(new Runnable() {
            @Override public void run() {
                
                try {
                    BufferedReader entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
                    String linha;
                    
                    while (true) {
                        linha = entrada.readLine();
                        if (linha.trim().equals("")) {
                            // Recebe o aviso de desconexão do servidor e bloqueia os campos
                            System.out.println("Conexao encerrada!!!");
                            campoMsg.setEnabled(false);
                            boxClientes.setEnabled(false);
                            labelNome.setForeground(Color.RED);
                            labelNome.setText(labelNome.getText().replace("Online", "Offline"));
                            break;
                        }
                        
                        // Verifica se entrada é para atualizar a lista de Clientes ou não
                        if (linha.startsWith("[")) {
                            atualizarListaClientes(linha, boxClientes, labelNome);
                        }else {
                            areaConversa.setText(areaConversa.getText() + linha + "\n");
                        }
                        
                    }
                    done = true;
                }
                catch (IOException e) {
                    e.getMessage();
                }
                
            }
        });
        someThread.start();
    }
    
    public static void atualizarListaClientes (String linha, JComboBox boxClientes, JLabel labelNome) {
        // Limpa String recebida e a transforma em vetor
        linha = linha.replace("[", "");
        linha = linha.replace("]", "");
        linha = linha.replaceAll(", ", ",");
        String[] nomesSplit = linha.split(",");
        
        // Remove todos os itens da Box e inclui primeiramente o "Todos"
        boxClientes.removeAllItems();
        boxClientes.addItem("Todos");
        
        // Percorre todo o vetor de Clientes adicionando cada item a Box
        for (String nomesSplit1 : nomesSplit) {
            if (!labelNome.getText().replace(" (Online)", "").equals(nomesSplit1)) {
                boxClientes.addItem(nomesSplit1);
            }
        }
    }
    
    public static void main(String[] args) throws IOException {
        Socket conexao = new Socket("localhost", 2000);
        Cliente teste = new Cliente(conexao);
    }
    
}