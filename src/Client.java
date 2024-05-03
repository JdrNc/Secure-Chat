import java.net.Socket;

public class Client {

    private String nome;
    private Socket socket;

    public Client(String nome, Socket socket) {
        this.nome = nome;
        this.socket = socket;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}
