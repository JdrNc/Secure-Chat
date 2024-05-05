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
import java.util.concurrent.Semaphore;

public class Server {
    private static SecretKey secretKey;
    private static Cipher cipher;

    private static List<PrintWriter> clientes = new ArrayList<PrintWriter>();
    private static List<String> clientesConectados = new ArrayList<String>();

    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(100);
        try {
            // Inicializa o gerador de chaves AES
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);
            secretKey = keygen.generateKey();

            // Inicializa o cifrador AES
            cipher = Cipher.getInstance("AES");

            // Inicia o servidor na porta 12345
            ServerSocket serverSocket = new ServerSocket(12347);
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
        private final Semaphore semaphore = new Semaphore(1);
        public ClientHandler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                System.out.println(currentThread().getName());
                Thread.sleep(10000);

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
                System.out.println(Base64.getEncoder().encodeToString(secretKey.getEncoded()));
                writer.println(encodedKey);

                String messageForChat;
                nome = message = reader.readLine();
                String encodedMessage;


                    clientesConectados.add(nome);
                    newClient(clientesConectados.toString(), writer);
                    indexofMe = clientesConectados.size() - 1;
                    messageForChat = nome + " entrou no chat";
                    encodedMessage = encrypt(messageForChat);
//                    System.out.println(messageForChat);




//                sendToAll(writer, encodedMessage);

                // Loop para receber e enviar mensagens
                while (!("UserExitTheRoomMsg".equalsIgnoreCase(message)) && (message = reader.readLine()) != null) {
                    try{
                        semaphore.acquire();
                        String decodedMsg = decrypt(message);
                        messageForChat = nome + " diz -> " + decodedMsg;
                        encodedMessage = encrypt(messageForChat);
                        System.out.println("Mensagem original no server: " + messageForChat + "\r\n");
                        System.out.println("Mensagem criptografada no server: " + encodedMessage + "\r\n");
                        sendToAll(writer, encodedMessage);
                    } catch (Exception e){
                        semaphore.release();
                    }



                }
                // Fecha o socket
                clientesConectados.remove(indexofMe);
                newClient(clientesConectados.toString(), writer);
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        private void newClient(String clientsConnected, PrintWriter writer){
            synchronized (clientsConnected){
                PrintWriter wr;
                for(PrintWriter wrs : clientes){
                    try {
                        String encripted = encrypt(clientsConnected);
                        System.out.print("Messagem criptografada: " + encripted + "\r\n");
                        wrs.println(encripted);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
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
        // Método para decriptar uma mensagem utilizando AES
        private String decrypt(String encryptedMessage) throws Exception {
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedMessage = Base64.getDecoder().decode(encryptedMessage);
            byte[] decryptedBytes = cipher.doFinal(decodedMessage);
            return new String(decryptedBytes);
        }
        //Método para encriptar a mensagem a ser enviada utilizando AES
        private static String encrypt(String message) throws Exception {
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(message.getBytes());
            return Base64.getEncoder().encodeToString(encryptedBytes);
        }
    }
}