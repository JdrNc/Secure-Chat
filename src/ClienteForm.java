import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.*;
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

public class ClienteForm extends JFrame implements ActionListener, KeyListener {
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
        txtPorta = new JTextField("12347");
        txtNome = new JTextField("Cliente");
        Object[] texts = {lblMessage, txtIP, txtPorta, txtNome};
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
        setSize(500, 550);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void conectar() throws IOException {

        socket = new Socket(txtIP.getText(), Integer.parseInt(txtPorta.getText()));
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.write(txtNome.getText() + "\r\n");
        bfw.flush();
    }

    public void enviarMensagem(String msg) throws IOException {

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

    public void escutar() throws IOException {

        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";
        msg = bfr.readLine();
        System.out.println(msg);

        //Recebe chave do servidor
        byte[] decodedKey = Base64.getDecoder().decode(msg);
        secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        while (!"UserExitTheRoomMsg".equalsIgnoreCase(msg)) {
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
                if (msg.equals("UserExitTheRoomMsg")) {
                    chatAll.append("Servidor caiu! \r\n");
                } else if (decodedMsg.contains("newClientPass")) {
                    onlinePeople.setText("");
                    String[] newNome = decodedMsg.replace("newClientPass", "").replace("[", "").replace("]", "").split(", ");
                    for (String nome : newNome) {
                        onlinePeople.append(nome + "\r\n");
                    }
                } else {
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

    public void sair() throws IOException {

        enviarMensagem("UserExitTheRoomMsg");
        bfw.close();
        ouw.close();
        ou.close();
        socket.close();
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        try {
            if (e.getActionCommand().equals(btnSend.getActionCommand()))
                enviarMensagem(msgEnviar.getText());
            else if (e.getActionCommand().equals(btnSair.getActionCommand()))
                sair();
        } catch (IOException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
    }

    @Override
    public void keyPressed(KeyEvent e) {

        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
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
        try {
            cipher = Cipher.getInstance("AES");
        } catch (Exception e) {
        }

        nc.conectar();
        nc.escutar();

    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        pnlContent = new JPanel();
        pnlContent.setLayout(new GridLayoutManager(6, 4, new Insets(0, 0, 0, 0), -1, -1));
        onlineP = new JLabel();
        onlineP.setBackground(new Color(-16745984));
        onlineP.setEnabled(true);
        onlineP.setForeground(new Color(-16754688));
        onlineP.setText("Online");
        pnlContent.add(onlineP, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(40, 45), null, 0, false));
        onlinePeople = new JTextArea();
        onlinePeople.setBackground(new Color(-2500134));
        onlinePeople.setEditable(false);
        onlinePeople.setLineWrap(true);
        onlinePeople.setText("");
        pnlContent.add(onlinePeople, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(130, 372), null, 0, false));
        btnSair = new JButton();
        btnSair.setText("Sair");
        pnlContent.add(btnSair, new GridConstraints(4, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(100, -1), null, 0, false));
        msgEnviar = new JTextField();
        msgEnviar.setAlignmentY(0.5f);
        msgEnviar.setColumns(0);
        msgEnviar.setName("");
        msgEnviar.setSelectionStart(0);
        msgEnviar.setText("");
        msgEnviar.setToolTipText("Escreva a mensagem aqui");
        pnlContent.add(msgEnviar, new GridConstraints(2, 1, 3, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
        chatLabel = new JLabel();
        chatLabel.setHorizontalAlignment(0);
        chatLabel.setHorizontalTextPosition(0);
        chatLabel.setText("Chat");
        pnlContent.add(chatLabel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(216, 45), null, 0, false));
        chatAll = new JTextArea();
        chatAll.setBackground(new Color(-2168065));
        chatAll.setEditable(false);
        chatAll.setLineWrap(true);
        chatAll.setText("");
        pnlContent.add(chatAll, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 372), null, 0, false));
        btnSend = new JButton();
        btnSend.setHorizontalTextPosition(11);
        btnSend.setText("Enviar");
        pnlContent.add(btnSend, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(100, 49), null, 0, false));
        empetyLabel = new JLabel();
        empetyLabel.setText("");
        pnlContent.add(empetyLabel, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        empetyLabel2 = new JLabel();
        empetyLabel2.setText("");
        pnlContent.add(empetyLabel2, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        empetyLabel3 = new JLabel();
        empetyLabel3.setText("");
        pnlContent.add(empetyLabel3, new GridConstraints(0, 3, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        onlineP.setLabelFor(onlinePeople);
        chatLabel.setLabelFor(chatAll);
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return pnlContent;
    }
}


