import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Base64;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private static SecretKey secretKey;
    private static Cipher cipher;

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

                String encodedKey = reader.readLine();
                byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

                // Loop para receber e enviar mensagens
                String message;
                while ((message = reader.readLine()) != null) {
                    System.out.println("Thread id: " + Thread.currentThread().getId() + " Task:" +  message);
                    System.out.println("Mensagem recebida do cliente: " + message);

                    // Decifra a mensagem recebida
                    String decryptedMessage = decrypt(message);
                    System.out.println("Mensagem decifrada: " + decryptedMessage);

                    //Pega só o número da mensagem
                    String[] splitedMessage = decryptedMessage.split(" ");

                    //Calcula fatorial
                    int numFat = Integer.parseInt(splitedMessage[1]);
                    System.out.println(numFat);
                   Long resultFat = calculateFactorial(numFat);

                    // Responde ao cliente
                    writer.println("Mensagem recebida referente a: " + decryptedMessage + " Resultado: " + resultFat);
                }

                // Fecha o socket
                socket.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        //Calcula fatorial
        private long calculateFactorial(int n) {
            if (n == 0)
                return 1;
            return n * calculateFactorial(n - 1);
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