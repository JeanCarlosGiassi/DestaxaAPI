package org.example;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.time.LocalDateTime;

public class SocketClientApp extends JFrame {
    public static final	String
            SEQUENCE_FINAL		=	"\r"+"\n"+"\t"+"\t"+"\r"+"\n"+"\t"+"\t"+"\t"+"\r"+"\n"+"\t"+"\t"+"\r"+"\n"+"\t";

    private JTextField ipField;
    private JTextField portField;
    private JButton connectButton;
    private JButton disconnectButton;
    private JButton sendButton;
    private JButton clearButton;
    private JTextArea sendArea;
    private JTextArea receiveArea;

    private Socket socket;
    private PrintWriter writer;
    private BufferedReader reader;
    private Thread listenerThread;

    public SocketClientApp() {
        setTitle("Socket Client");
        setSize(600, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Painel Superior - Conexão
        JPanel connectionPanel = new JPanel(new FlowLayout());
        ipField = new JTextField("127.0.0.1", 10);
        portField = new JTextField("60906", 5);
        connectButton = new JButton("Conectar");
        disconnectButton = new JButton("Desconectar");
        clearButton = new JButton("Limpar Log");

        connectionPanel.add(new JLabel("IP:"));
        connectionPanel.add(ipField);
        connectionPanel.add(new JLabel("Porta:"));
        connectionPanel.add(portField);
        connectionPanel.add(connectButton);
        connectionPanel.add(disconnectButton);
        connectionPanel.add(clearButton);

        // Áreas de Envio e Recebimento
        sendArea = new JTextArea(5, 40);
        receiveArea = new JTextArea(10, 40);
        receiveArea.setEditable(false);

        JScrollPane sendScroll = new JScrollPane(sendArea);
        JScrollPane receiveScroll = new JScrollPane(receiveArea);

        JPanel centerPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        centerPanel.add(new JPanel(new BorderLayout()) {{
            add(new JLabel("Enviar:"), BorderLayout.NORTH);
            add(sendScroll, BorderLayout.CENTER);
        }});
        centerPanel.add(new JPanel(new BorderLayout()) {{
            add(new JLabel("Recebido:"), BorderLayout.NORTH);
            add(receiveScroll, BorderLayout.CENTER);
        }});

        // Botão Enviar
        sendButton = new JButton("Enviar");

        add(connectionPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(sendButton, BorderLayout.SOUTH);

        // Ações dos botões
        connectButton.addActionListener(e -> connect());
        disconnectButton.addActionListener(e -> disconnect());
        sendButton.addActionListener(e -> sendMessage());
        clearButton.addActionListener(e -> clearLog());

        // Habilitar botão "Enviar" somente quando tiver texto
        sendArea.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateSendButton(); }
            public void removeUpdate(DocumentEvent e) { updateSendButton(); }
            public void changedUpdate(DocumentEvent e) { updateSendButton(); }
        });

        // Estado inicial
        updateUIState(false);

        setVisible(true);
    }

    private void connect() {
        String ip = ipField.getText();
        int port;

        try {
            port = Integer.parseInt(portField.getText());
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Porta inválida.");
            return;
        }

        try {
            socket = new Socket(ip, port);
            writer = new PrintWriter(socket.getOutputStream(), true);
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            startListening();

            updateUIState(true);
            JOptionPane.showMessageDialog(this, "Conectado com sucesso.");
        } catch (Exception ex) {
            updateUIState(false);
            JOptionPane.showMessageDialog(this, "Falha ao conectar: " + ex.getMessage());
        }
    }

    private void disconnect() {
        try {
            if (listenerThread != null && listenerThread.isAlive()) {
                listenerThread.interrupt();
            }
            if (writer != null) writer.close();
            if (reader != null) reader.close();
            if (socket != null) socket.close();
        } catch (Exception ignored) {}

        updateUIState(false);
        JOptionPane.showMessageDialog(this, "Desconectado com sucesso.");
    }

    private void clearLog() {
       if (sendArea != null) sendArea.setText("");
       if (receiveArea != null) receiveArea.setText("");
    }

    private void sendMessage() {
        String message = sendArea.getText().trim();

        if (writer != null && !message.isEmpty()) {
            writer.print(message+SEQUENCE_FINAL);
            writer.flush();
            sendArea.setText("");
        } else {
            JOptionPane.showMessageDialog(this, "Conecte-se primeiro e insira uma mensagem.");
        }
    }

    private void startListening() {
        listenerThread = new Thread(() -> {
            try {
                String line;
                while (!Thread.currentThread().isInterrupted() && (line = reader.readLine()) != null) {
                    LocalDateTime localDateTime = LocalDateTime.now();
                    String received = localDateTime+": "+line + "\n";
                    SwingUtilities.invokeLater(() -> receiveArea.append(received));
                }
            } catch (Exception e) {
                if (!Thread.currentThread().isInterrupted()) {
                    SwingUtilities.invokeLater(() -> {
                        JOptionPane.showMessageDialog(this, "Conexão encerrada: " + e.getMessage());
                        updateUIState(false);
                    });
                }
            }
        });
        listenerThread.start();
    }

    private void updateSendButton() {
        String text = sendArea.getText().trim();
        sendButton.setEnabled(!text.isEmpty() && socket != null && socket.isConnected() && !socket.isClosed());
    }

    private void updateUIState(boolean connected) {
        connectButton.setEnabled(!connected);
        disconnectButton.setEnabled(connected);
        sendButton.setEnabled(connected && !sendArea.getText().trim().isEmpty());
        ipField.setEnabled(!connected);
        portField.setEnabled(!connected);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(SocketClientApp::new);
    }
}
