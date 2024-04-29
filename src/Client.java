import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.Socket;
import javax.swing.*;

public class Client {
    private static SecretKey secretKey;
    private static Cipher cipher;

    public static void main(String[] args) {

        try {
            int threadPoolSize = 16;
            ExecutorService pool
                    = Executors.newFixedThreadPool(threadPoolSize);
            // Inicializa o cifrador AES
            cipher = Cipher.getInstance("AES");

            // Gera a chave AES
            KeyGenerator keygen = KeyGenerator.getInstance("AES");
            keygen.init(128);
            secretKey = keygen.generateKey();

            // Conecta-se ao servidor na porta 12345
            Socket socket = new Socket("localhost", 12345);
            System.out.println("Conectado ao servidor...");

            // Obtém os fluxos de entrada e saída do servidor
            InputStream inputStream = socket.getInputStream();
            OutputStream outputStream = socket.getOutputStream();

            // Cria um leitor e escritor para facilitar a comunicação
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            PrintWriter writer = new PrintWriter(outputStream, true);

            // Envia a chave AES para o servidor
            String encodedKey = Base64.getEncoder().encodeToString(secretKey.getEncoded());
            writer.println(encodedKey);

            // Loop para enviar mensagens ao servidor
            Random random = new Random();

            for (int i = 0; i < 10000; i++) {
                int randomNumber = random.nextInt((20 - 10) + 1) + 10;; // Números aleatórios entre 10 e 30
                pool.execute(new FactorialCalculator(randomNumber, writer, reader));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Método para criptografar uma mensagem utilizando AES
    private static String encrypt(String message) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }
    static class FactorialCalculator implements Runnable {
        private final int number;
        private final PrintWriter writer;
        private final BufferedReader reader;

        public FactorialCalculator(int number, PrintWriter writer, BufferedReader reader) {
            this.number = number;
            this.writer = writer;
            this.reader = reader;
        }

        @Override
        public void run() {
            try {

                // Monta a mensagem para o cálculo do fatorial
                String message = "CALCULATE_FACTORIAL " + number;
                String encryptedMessage = encrypt(message);

                // Envia a mensagem criptografada para o servidor
                writer.println(encryptedMessage);

                // Aguarda a resposta do servidor
                String response = reader.readLine();
                System.out.println("Resposta do servidor para " + number + ": " + response);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}