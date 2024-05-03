import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.*;
import java.awt.event.KeyListener;
import java.net.Socket;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Base64;

public class ClienteForm extends JFrame implements ActionListener, KeyListener{
    private static SecretKey secretKey;
    private JLabel chatLabel;
    private JTextArea chatAll;

    private JTextField txtIP;
    private JTextField txtPorta;
    private JTextField txtNome;
    private JButton btnSend;
    private JButton btnSair;
    private JLabel onlineP;
    private JTextArea onlinePeople;
    private JPanel pnlContent;
    private JTextField msgEnviar;
    private JScrollBar scrollBar1;
    private Socket socket;
    private OutputStream ou;
    private Writer ouw;
    private BufferedWriter bfw;
    public ClienteForm() throws IOException {
        JLabel lblMessage = new JLabel("Verificar!");
        txtIP = new JTextField("127.0.0.1");
        txtPorta = new JTextField("12345");
        txtNome = new JTextField("Cliente");
        Object[] texts = {lblMessage, txtIP, txtPorta, txtNome };
        JOptionPane.showMessageDialog(null, texts);
        chatAll.setEditable(false);
//        chatAll.setBackground(new Color(0,0,0));
        onlinePeople.setEditable(false);
        btnSend.setToolTipText("Enviar Mensagem");
        btnSair.setToolTipText("Sair do Chat");
        btnSend.addActionListener(this);
        btnSair.addActionListener(this);
        btnSend.addKeyListener(this);
        msgEnviar.addKeyListener(this);
        chatAll.setLineWrap(true);
        setTitle(txtNome.getText());
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(500,350);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void conectar() throws IOException{

        socket = new Socket(txtIP.getText(),Integer.parseInt(txtPorta.getText()));
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.write(txtNome.getText()+"\r\n");
        bfw.flush();
    }

    public void enviarMensagem(String msg) throws IOException{

        if(msg.equals("UserExitTheRoomMsg")){
            bfw.write("Desconectado \r\n");
            chatAll.append("Desconectado \r\n");
        }else{
            bfw.write(msg+"\r\n");
            System.out.println( txtNome.getText() + " diz -> " +         msgEnviar.getText()+"\r\n");
            chatAll.append( txtNome.getText() + " diz -> " +         msgEnviar.getText()+"\r\n");
        }
        bfw.flush();
        msgEnviar.setText("");
    }
    public void escutar() throws IOException{

        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";

        while(!"UserExitTheRoomMsg".equalsIgnoreCase(msg))

            if(bfr.ready()){
                msg = bfr.readLine();
                if(msg.equals("UserExitTheRoomMsg"))
                    chatAll.append("Servidor caiu! \r\n");
                else if(msg.contains("key:")){
                   String encodedKey =  msg.replace("key:", "");
                    byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
                    secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

                } else if(msg.contains("newClientPass")){
                    onlinePeople.setText("");
                    String[] newNome = msg.replace("newClientPass", "").replace("[", "").replace("]", "").split(", ");
                    for(String nome : newNome){
                        onlinePeople.append(nome + "\r\n");
                    }

                } else
                    chatAll.append(msg+"\r\n");
            }
    }

    public void sair() throws IOException{

        enviarMensagem("UserExitTheRoomMsg");
        bfw.close();
        ouw.close();
        ou.close();
        socket.close();
    }
    @Override
    public void actionPerformed(ActionEvent e) {

        try {
            if(e.getActionCommand().equals(btnSend.getActionCommand()))
                enviarMensagem(msgEnviar.getText());
            else
            if(e.getActionCommand().equals(btnSair.getActionCommand()))
                sair();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {

        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                enviarMensagem(msgEnviar.getText());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        }
    }

    @Override
    public void keyReleased(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public void keyTyped(KeyEvent arg0) {
        // TODO Auto-generated method stub
    }

    public static void main(String[] args) throws IOException {
        ClienteForm nc = new ClienteForm();
        nc.conectar();
        nc.escutar();
    }

}


