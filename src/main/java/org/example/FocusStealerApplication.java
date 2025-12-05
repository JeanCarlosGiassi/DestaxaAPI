package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Uma classe de demonstração que cria uma janela que agressivamente
 * tenta "roubar" e manter o foco do sistema operacional.
 *
 * @author Gemini
 */
public class FocusStealerApplication {

    public static void main(String[] args) {
        // É uma boa prática inicializar a GUI na Event Dispatch Thread (EDT)
        SwingUtilities.invokeLater(() -> createAndShowGui());
    }

    private static void createAndShowGui() {
        // Cria a janela principal
        final JFrame frame = new JFrame("Ladrão de Foco");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(450, 250);

        // --- Ponto Chave 1: Manter a janela sempre no topo ---
        // Isso garante que a janela não será obscurecida por outras.
        frame.setAlwaysOnTop(true);

        // Cria um painel com um layout para organizar os componentes
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);

        // Adiciona um rótulo e um campo de texto à janela
        JLabel label = new JLabel("Tente usar outra aplicação enquanto eu estiver aqui!");
        final JTextField textField = new JTextField("Eu terei o foco!", 20);

        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(label, gbc);

        gbc.gridy = 1;
        panel.add(textField, gbc);

        frame.getContentPane().add(panel);

        // Posiciona a janela no centro da tela
        frame.setLocationRelativeTo(null);

        // Garante que o campo de texto receba o foco quando a janela for ativada
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowGainedFocus(WindowEvent e) {
                textField.requestFocusInWindow();
            }
        });

        // Torna a janela visível
        frame.setVisible(true);

        // --- Ponto Chave 2: O ladrão de foco ---
        // Um Timer que dispara a cada 10 milissegundos para forçar o foco.
        // Um intervalo baixo torna a aplicação mais agressiva.
        ActionListener focusStealerListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Traz a janela para a frente do sistema operacional
                frame.toFront();
                // Solicita o foco para a janela. Necessário em alguns SOs.
                frame.requestFocus();
                // Solicita o foco para o componente específico.
                // requestFocusInWindow() é geralmente preferível a requestFocus().
                textField.requestFocusInWindow();
            }
        };

        Timer timer = new Timer(100, focusStealerListener);
        timer.start(); // Inicia o roubo de foco
    }
}