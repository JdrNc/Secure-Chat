import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;


public class configTestForm extends JFrame implements ActionListener, KeyListener {

    private JTextField numClientes;
    private JTextField IntervaloEntreMensagens;
    private JTextField TamMsg;

    public configTestForm() throws IOException{
        JLabel lblMessage = new JLabel("Configure!!");
        JLabel lblNumClientes = new JLabel("Indique o número de clientes que quer testar");
        JLabel lblIntervalo = new JLabel("Indique o intervalo entre as mensagens em segundos");
        JLabel lblTamMsg = new JLabel("Indique o tamanho da mensagens por caractér");
        numClientes = new JTextField("1");
        IntervaloEntreMensagens = new JTextField("1");
        TamMsg = new JTextField("1");
        Object[] texts = {lblMessage, lblNumClientes, numClientes, lblIntervalo,IntervaloEntreMensagens, lblTamMsg, TamMsg};
        JOptionPane.showMessageDialog(null, texts);

        IniciarConexoesSimultaneas cf = new IniciarConexoesSimultaneas(Integer.parseInt(numClientes.getText()), Integer.parseInt(IntervaloEntreMensagens.getText()));
    }


    public static void main(String[] args) throws IOException {
      configTestForm ctf =  new  configTestForm();
    }







    @Override
    public void actionPerformed(ActionEvent e) {

    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {

    }
}
