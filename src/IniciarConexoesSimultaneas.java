import javax.crypto.Cipher;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;

public class IniciarConexoesSimultaneas {

private int numConexoes;

private static int timeBetMsg;

private static Cipher cipher;
    public IniciarConexoesSimultaneas(int numConexoes) throws IOException {
        this.numConexoes = numConexoes;
        starrt(numConexoes);
    }

    private void starrt(int numConexoes) throws IOException {
        CountDownLatch latch = new CountDownLatch(numConexoes);

        for (int i = 0; i < numConexoes; i++) {
            try {
                cipher = Cipher.getInstance("AES");
            } catch (Exception e) {
            }
            int finalI = i;
            new Thread(() -> {
                try {
                    ClienteForm cliente = new ClienteForm("Cliente " + Integer.toString(finalI + 1));
                    System.out.println("cypher " + finalI);
                    cliente.setCipher(cipher);
                    cliente.conectar();
                    cliente.escutar(Thread.currentThread().getName());

//                    cliente.iniciarEnvioMensagens(timeBetMsg);


                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }).start();
        }


    }


    public static void main(String[] args) throws IOException {



    }
}
