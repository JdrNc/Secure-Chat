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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS;

public class ClienteForm extends JFrame implements ActionListener, KeyListener {
    private static SecretKey secretKey;
    private JLabel chatLabel;
    private JTextArea chatAll;
    private Cipher cipher;
    private static JTextField numClientes;
    private JTextField txtPorta;
    private JTextField txtNome;

    private JScrollPane scroll;
    private JButton btnSend;
    private JButton btnSair;
    private JLabel onlineP;
    private JTextArea onlinePeople;
    private JPanel pnlContent;
    private JTextField msgEnviar;
    private JLabel empetyLabel;
    private JLabel empetyLabel2;
    private JLabel empetyLabel3;
    private JScrollPane scrollP;
    private Socket socket;
    private OutputStream ou;
    private Writer ouw;
    private BufferedWriter bfw;


    private String nome;
    private ScheduledExecutorService scheduler;
    private final Semaphore semaphore = new Semaphore(1);

    public ClienteForm(String nome) throws IOException {

        scroll = new JScrollPane(chatAll);
        scrollP = new JScrollPane(onlineP);
        pnlContent.add(scroll, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 372), null, 0, false));
        pnlContent.add(scrollP, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, 372), null, 0, false));
        chatAll.setEditable(false);
        onlinePeople.setEditable(false);
        btnSend.setToolTipText("Enviar Mensagem");
        btnSair.setToolTipText("Sair do Chat");
        btnSend.addActionListener(this);
        btnSair.addActionListener(this);
        btnSend.addKeyListener(this);
        msgEnviar.addKeyListener(this);
        setTitle(nome);
        setContentPane(pnlContent);
        setLocationRelativeTo(null);
        setResizable(false);
        setSize(450, 550);
        setVisible(true);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.nome = nome;

    }

    public void conectar() throws IOException {

        socket = new Socket("127.0.0.1", Integer.parseInt("12347"));
        ou = socket.getOutputStream();
        ouw = new OutputStreamWriter(ou);
        bfw = new BufferedWriter(ouw);
        bfw.write(nome + "\r\n");
        bfw.flush();
    }

    public void enviarMensagem(String msg) throws IOException {

        if (msg.equals("UserExitTheRoomMsg")) {
            try {
                semaphore.acquire();
                String disconected = "";
                disconected = encrypt("Desconectado");
                bfw.write(disconected + "\r\n");
                chatAll.append("Desconectado \r\n");
            } catch (Exception e) {
                Thread.currentThread().interrupt();

            } finally {

            }

        } else {
            try {
                String encodedMsg = encrypt(msg);
                bfw.write(encodedMsg + "\r\n");
                chatAll.append(nome + " diz -> " + msgEnviar.getText() + "\r\n");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        bfw.flush();
        msgEnviar.setText("");
    }

    public void escutar(String id) throws IOException, InterruptedException {
//        socket = new Socket("127.0.0.1", Integer.parseInt("12347"));
        InputStream in = socket.getInputStream();
        InputStreamReader inr = new InputStreamReader(in);
        BufferedReader bfr = new BufferedReader(inr);
        String msg = "";
        msg = bfr.readLine();

        //Recebe chave do servidor
        byte[] decodedKey = Base64.getDecoder().decode(msg);
        secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        System.out.println(id + "Escutando");
        while (!"UserExitTheRoomMsg".equalsIgnoreCase(msg)) {
            String decodedMsg = null;
            if (bfr.ready()) {
                msg = bfr.readLine();
                try {
                    System.out.print("Messagem chega criptografada no cliente: " + msg + "\r\n");
                    decodedMsg = decrypt(msg);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                if (msg.equals("UserExitTheRoomMsg")) {
                    chatAll.append("Servidor caiu! \r\n");
                } else if (decodedMsg.contains("newClientPass")) {
                    onlinePeople.setText("");
                    String[] newNome = decodedMsg.replace("newClientPass", "").replace("[", "").replace("]", "").split(", ");
                    System.out.print("Messagem chega decriptografada no cliente: " + decodedMsg + "\r\n");
                    for (String nome : newNome) {
                        onlinePeople.append(nome + "\r\n");
                    }
                } else {
                    System.out.print("Messagem chega decriptografada no cliente: " + decodedMsg + "\r\n");
                    chatAll.append(decodedMsg + "\r\n");
                }

            }
        }
    }

    private String encrypt(String message) throws Exception {
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

//        try {
//            cipher = Cipher.getInstance("AES");
//        } catch (Exception e) {
//        }


    }

    public void iniciarEnvioMensagens(int delayEntreEnvios) throws InterruptedException, IOException {
        Thread.sleep(10000);
        long lastCall = 0;

        while (true) {
            if (System.currentTimeMillis() - lastCall > 3000) {
                lastCall = System.currentTimeMillis();
                enviarMensagem("Mensagem enviada a cada " + delayEntreEnvios);
            }
        }

    }

    public void setCipher(Cipher cipher) {
        this.cipher = cipher;
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
        scroll = new JScrollPane();
        pnlContent.add(scroll, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, new Dimension(0, 0), 0, false));
        scrollP = new JScrollPane();
        pnlContent.add(scrollP, new GridConstraints(5, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, null, new Dimension(0, 0), 0, false));
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