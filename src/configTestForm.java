import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.IOException;


public class configTestForm extends JFrame implements ActionListener, KeyListener {

    private JTextField numClientes;


    public configTestForm() throws IOException{
        JLabel lblMessage = new JLabel("Configure!!");
        JLabel lblNumClientes = new JLabel("Indique o número de clientes que quer testar");
        numClientes = new JTextField("1");
        Object[] texts = {lblMessage, lblNumClientes, numClientes};
        JOptionPane.showMessageDialog(null, texts);

        IniciarConexoesSimultaneas cf = new IniciarConexoesSimultaneas(Integer.parseInt(numClientes.getText()));
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
