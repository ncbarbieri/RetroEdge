package utils;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.filechooser.FileNameExtensionFilter;

public class MainApp {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Tileset Editor");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Crea il pannello del tileset
        TilesetPanel tilesetPanel = new TilesetPanel();
        
        // Aggiungi un pulsante per caricare il tileset
        JButton loadButton = new JButton("Carica Tileset");
        loadButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("PNG files", "png");
                fileChooser.addChoosableFileFilter(filter);            
                fileChooser.setFileFilter(filter);           int result = fileChooser.showOpenDialog(null);
//                fileChooser.setCurrentDirectory(new java.io.File("."));
                if (result == JFileChooser.APPROVE_OPTION) {
                    tilesetPanel.loadTileset(fileChooser.getSelectedFile().getAbsolutePath());
                }
            }
        });

        // Aggiungi un pulsante per caricare il tileset
        JButton saveAnimationButton = new JButton("Salva animazioni");
        saveAnimationButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	tilesetPanel.saveAnimationData();
            }
        });

        // Aggiungi un pulsante per caricare il tileset
        JButton saveSolidButton = new JButton("Salva solid");
        saveSolidButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	tilesetPanel.saveSolidTileArray();
            }
        });

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 3));
        buttonPanel.add(loadButton);
        buttonPanel.add(saveAnimationButton);
        buttonPanel.add(saveSolidButton);
        
        // Crea il pannello di anteprima
        AnimationPanel animationPanel = new AnimationPanel(tilesetPanel);

        // Aggiungi un listener per aggiornare il pannello di anteprima quando si seleziona una tile
        tilesetPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                Point selectedTile = tilesetPanel.getSelectedTile();
                if (selectedTile != null) {
                    animationPanel.setSelectedTile(selectedTile);
                }
            }
        });

        frame.setLayout(new BorderLayout());
        frame.add(buttonPanel, BorderLayout.NORTH);
        frame.add(tilesetPanel, BorderLayout.CENTER);
        frame.add(animationPanel, BorderLayout.EAST);
        frame.pack();
        frame.setVisible(true);
    }
}