import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
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
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class ClienteForm extends JFrame implements ActionListener, KeyListener{
    private static SecretKey secretKey;
    private JLabel chatLabel;
    private JTextArea chatAll;
    private static Cipher cipher;
    private JTextField txtIP;
    private JTextField txtPorta;
    private JTextField txtNome;
    private JButton btnSend;
    private JButton btnSair;
    private JLabel onlineP;
    private JTextArea onlinePeople;
    private JPanel pnlContent;
    private JTextField msgEnviar;
    private JLabel empetyLabel;
    private JLabel empetyLabel2;
    private JLabel empetyLabel3;
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
        setSize(500,550);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void conectar() throws IOException{

        socket = new Socket(txtIP.getText(),Integer.parseInt(txtPorta.getText()));
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        try {
            String encryptName = encrypt(txtNome.getText());
            bfw.write(encryptName+"\r\n");
            chatAll.append(txtNome.getText() + " entrou no chat\r\n");
            bfw.flush();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void enviarMensagem(String msg) throws IOException{

        if(!(msg.isEmpty())) {
            if (msg.equals("UserExitTheRoomMsg")) {
                try {
                    String disconected = "";
                    disconected = encrypt("Desconectado");
                    bfw.write(disconected + "\r\n");
                    chatAll.append("Desconectado \r\n");
                } catch (Exception e) {
                }

            } else {
                try {
                    String encodedMsg = encrypt(msg);
                    bfw.write(encodedMsg + "\r\n");
//                System.out.println( txtNome.getText() + " diz -> " +         msgEnviar.getText()+"\r\n");
                    chatAll.append(txtNome.getText() + " diz -> " + msgEnviar.getText() + "\r\n");
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            bfw.flush();
            msgEnviar.setText("");
        }
    }
    public void escutar() throws IOException{

        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";
        msg = bfr.readLine();
        System.out.println(msg);

        //Recebe chave do servidor
            byte[] decodedKey = Base64.getDecoder().decode(msg);
            secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        while(!"UserExitTheRoomMsg".equalsIgnoreCase(msg)) {
            String decodedMsg = null;
            if (bfr.ready()) {
                msg = bfr.readLine();
                System.out.println(msg);
                try {
                    decodedMsg = decrypt(msg);
                    System.out.println(decodedMsg);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (msg.equals("UserExitTheRoomMsg")){
                    chatAll.append("Servidor caiu! \r\n");
                }
                else if(decodedMsg.contains("newClientPass")){
                    onlinePeople.setText("");
                    String[] newNome = decodedMsg.replace("newClientPass", "").replace("[", "").replace("]", "").split(", ");
                    for (String nome : newNome) {
                        onlinePeople.append(nome + "\r\n");
                    }
                }else{
                        chatAll.append(decodedMsg + "\r\n");
                    }

            }
        }
    }

    private static String encrypt(String message) throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(message.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    private String decrypt(String encryptedMessage) throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decodedMessage = Base64.getDecoder().decode(encryptedMessage);
        byte[] decryptedBytes = cipher.doFinal(decodedMessage);
        return new String(decryptedBytes);
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
        }
    }
    @Override
    public void keyPressed(KeyEvent e) {

        if(e.getKeyCode() == KeyEvent.VK_ENTER){
            try {
                enviarMensagem(msgEnviar.getText());
            } catch (IOException e1) {
                // TODO Auto-generated catch block
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

    public static void main(String[] args) throws IOException{
        ClienteForm nc = new ClienteForm();
        try {
            cipher = Cipher.getInstance("AES");
        } catch (Exception e){}


        nc.conectar();
        nc.escutar();
    }

}


