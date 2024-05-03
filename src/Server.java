import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static SecretKey secretKey;
    private static Cipher cipher;

    private static List<PrintWriter> clientes = new ArrayList<PrintWriter>();
    private static List<String> clientesConectados = new ArrayList<String>();

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(10);
        try {
            // Inicializa o gerador de chaves AES
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);
            secretKey = keygen.generateKey();

            // Inicializa o cifrador AES
            cipher = Cipher.getInstance("AES");

            // Inicia o servidor na porta 12345
            ServerSocket serverSocket = new ServerSocket(12345);
            System.out.println("Servidor iniciado...");
            clientesConectados.add("newClientPass");

            while (true) {
                // Aceita conexões de clientes
                Socket socket = serverSocket.accept();
                System.out.println("Cliente conectado: " + socket);


                // Inicia uma nova thread para lidar com o cliente
                executor.execute(new ClientHandler(socket));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            executor.shutdown();
        }
    }

    // Classe interna para lidar com cada cliente em uma thread separada
    static class ClientHandler extends Thread {
        private final Socket socket;

        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {


                // Obtém os fluxos de entrada e saída do cliente
                InputStream inputStream = socket.getInputStream();
                OutputStream outputStream = socket.getOutputStream();

                // Cria um leitor e escritor para facilitar a comunicação
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                PrintWriter writer = new PrintWriter(outputStream, true);
                clientes.add(writer);
                int indexofMe;
                String message;
                String nome;
                //Envia chave pro cliente novo
                String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
                writer.println("key:" + encodedKey);

                String messageForChat;
                nome = message = reader.readLine();

//                String encodedKey = reader.readLine();
//                byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
//                secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
                clientesConectados.add(nome);
                indexofMe = clientesConectados.size() - 1;
                System.out.println(indexofMe + " " + clientesConectados);
                newClient(clientesConectados, writer);
                messageForChat = nome + " entrou no chat";
                System.out.println(messageForChat);


                sendToAll(writer, messageForChat);

                // Loop para receber e enviar mensagens



                while (!("UserExitTheRoomMsg".equalsIgnoreCase(message)) && (message = reader.readLine()) != null) {

                    messageForChat = nome + " diz -> " + message;
                    System.out.println(messageForChat);
                    sendToAll(writer, messageForChat);



//                    System.out.println("Thread id: " + Thread.currentThread().getId() + " Task:" +  message);
//                    System.out.println("Mensagem recebida do cliente: " + message);
//
//                    // Decifra a mensagem recebida
//                    String decryptedMessage = decrypt(message);
//                    System.out.println("Mensagem decifrada: " + decryptedMessage);
//
//                    //Pega só o número da mensagem
//                    String[] splitedMessage = decryptedMessage.split(" ");
//
//                    //Calcula fatorial
//                    int numFat = Integer.parseInt(splitedMessage[1]);
//                    System.out.println(numFat);
//                   Long resultFat = calculateFactorial(numFat);
//
//                    // Responde ao cliente
//                    writer.println("Mensagem recebida referente a: " + decryptedMessage + " Resultado: " + resultFat);
                }

                // Fecha o socket
                clientesConectados.remove(indexofMe);
                newClient(clientesConectados, writer);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void newClient(List clientsConnected, PrintWriter writer){
            PrintWriter wr;
            for(PrintWriter wrs : clientes){
                    wrs.println(clientsConnected);
            }


        }
        //Manda a mensagem para todos os clientes
        private void sendToAll(PrintWriter writer, String msg) {
            PrintWriter wr;
            for(PrintWriter wrs : clientes){
                wr = (PrintWriter) wrs;
                if(!(writer == wr)){
                    wrs.println(msg);
                }
            }
        }
        // Método para decifrar uma mensagem utilizando AES
        private String decrypt(String encryptedMessage) throws Exception {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedMessage = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(decodedMessage);
            return new String(decryptedBytes);
        }
    }
}