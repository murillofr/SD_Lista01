import java.net.*;
import java.io.*;
import java.util.*;

public class Servidor extends Thread {
    
    private static Vector clientes;
    private static Vector nomes;
    private Socket conexao;
    private String meuNome;
    
    public Servidor (Socket s) {
        conexao = s;
    }

    public static void main(String[] args) throws UnknownHostException, IOException {
        
        clientes = new Vector();
        nomes = new Vector();
        
        // Chama o método para criação do arquivo de Log
        criarLog();
        
        ServerSocket s = new ServerSocket(2000);
        while (true) {
            System.out.println("Esperando conectar...........");
            
            Socket conexao = s.accept();
            System.out.println("Conectou!");
            
            Thread t = new Servidor(conexao);
            t.start();
        }
        
    }

    @Override
    public void run() {
        try {
            BufferedReader entrada = new BufferedReader(new InputStreamReader(conexao.getInputStream()));
            PrintStream saida = new PrintStream(conexao.getOutputStream());
            
            meuNome = entrada.readLine();
            if (meuNome == null) {
                return;
            }
            
            // Adiciona o Cliente novo ao vetor "clientes" e seu nome ao vetor "nomes"
            clientes.add(saida);
            nomes.add(meuNome);
            
            sendToAll(saida, " entrou ", "no Chat!");
            
            // Chama o método para atualizar a Box de todos os Clientes com o nome Cliente que entrou
            atualizarListaClientes(saida);
            
            String linha = entrada.readLine();
            
            while ((linha != null) && (!linha.trim().equals(""))) {
                // Splita a mensagem recebida
                String[] msgSplit = linha.split(">#<");
                
                // Se a primeira parte da mensagem for para "Todos", chama o sendToAll
                // Se não, chama o sendToOne
                if (msgSplit[0].equals("Todos")) {
                    sendToAll(saida, " disse: ", msgSplit[1]);
                }else {
                    sendToOne(saida, " disse: ", msgSplit[1], msgSplit[0]);
                }
                linha = entrada.readLine();
            }
            sendToAll(saida, " saiu ", "do Chat!");
            
            // Remove o cliente do vetor "clientes" e do vetor "nomes"
            clientes.remove(saida);
            nomes.remove(meuNome);
            
            // Chama o método para atualizar a Box de todos os Clientes com o nome Cliente que saiu
            atualizarListaClientes(saida);
            
            conexao.close();
        }catch (IOException e) {
            e.getMessage();
        }
    }

    private void sendToAll(PrintStream saida, String acao, String linha) throws IOException {
        Enumeration e = clientes.elements();
        
        // Insere no Log o que foi dito
        String log = ("(Público) " + meuNome + acao + linha);
        atualizarLog(conexao, log);
        
        while (e.hasMoreElements()) {
            PrintStream chat = (PrintStream) e.nextElement();
            
            if (chat != saida) {
                chat.println(log);
            }
            if (acao.equals(" saiu ")) {
                if (chat == saida) {
                    chat.println("");
                }
            }
            
        }
    }
    
    private void sendToOne(PrintStream saida, String acao, String linha, String paraQuem) throws IOException {
        Enumeration e = clientes.elements();
        
        // Insere no Log o que foi dito
        String log = ("(Privado) " + meuNome + acao + linha);
        atualizarLog(conexao, log);
        
        while (e.hasMoreElements()) {
            PrintStream chat = (PrintStream) e.nextElement();
            
            // Quando o cliente no momento for o da Mensagem Privada, envia para ele
            if (chat == clientes.get(nomes.indexOf(paraQuem))) {
                chat.println(log);
            }
        }
    }
    
    private static void criarLog() {
        java.io.File arquivo = new java.io.File("src/", "log.txt");
        
        // Verifica se o arquvio de log já existe anteriormente, se não, o cria.
        if (!arquivo.exists()) {
            try {
                arquivo.createNewFile();
                System.out.print("Log criado com sucesso!\n");
            }catch (IOException e) {
            }
        }else {
            System.out.println("Log existente!");
        }
    }
    
    private static void atualizarLog(Socket conexao, String log) throws IOException {
        java.io.File arquivo = new java.io.File("src/", "log.txt");
        
        InetAddress endereco_remoto;
        int porta_remota;
        String logConexao = "";
        
        endereco_remoto = conexao.getInetAddress();
        porta_remota = conexao.getPort();
        logConexao = logConexao + ("<" + endereco_remoto.getHostName() + ">@");
        logConexao = logConexao + ("<" + endereco_remoto.getHostAddress() + ">@");
        logConexao = logConexao + ("<" + porta_remota + ">#");
        logConexao = logConexao + ("<" + log + ">");
        
        FileWriter fileWriter = new FileWriter(arquivo, true);
        try (PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(logConexao);
            printWriter.flush();
            printWriter.close();
        }
    }
    
    private void atualizarListaClientes (PrintStream saida) {
        Enumeration e = clientes.elements();
        
        while (e.hasMoreElements()) {
            PrintStream chat = (PrintStream) e.nextElement();
            chat.println(nomes);
        }
    }
    
}