package utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Utility grafica per convertire uno spritesheet LPC con frame 64x64 e 192x192
 * in uno spritesheet uniforme 192x192, centrando i frame piccoli.
 */
public class LPCSpritesheetConverter {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                new LPCSpritesheetConverter().run();
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Errore durante la conversione:\n" + e.getMessage(),
                        "Errore", JOptionPane.ERROR_MESSAGE);
            }
        });
    }

    private void run() throws Exception {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Seleziona uno spritesheet LPC da convertire");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) {
            return; // Annullato
        }

        File inputFile = chooser.getSelectedFile();
        String inputPath = inputFile.getAbsolutePath();

        // Parametri con valori di default
        int smallTile = askInt("Dimensione tile piccoli (default 64):", 64);
        int largeTile = askInt("Dimensione tile grandi (default 192):", 192);
        int smallRows = askInt("Numero righe piccole (default 54):", 54);

        // Calcolo nome di output
        String outputPath = getOutputPath(inputFile, "_normalized");

        // Conversione
        convert(inputPath, outputPath, smallTile, largeTile, smallRows);

        JOptionPane.showMessageDialog(null,
                "Conversione completata!\n\nFile salvato in:\n" + outputPath,
                "Conversione completata", JOptionPane.INFORMATION_MESSAGE);
    }

    private int askInt(String message, int defaultValue) {
        String input = JOptionPane.showInputDialog(null, message, defaultValue);
        if (input == null || input.isEmpty()) return defaultValue;
        try {
            return Integer.parseInt(input.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private String getOutputPath(File inputFile, String suffix) {
        String name = inputFile.getName();
        int dot = name.lastIndexOf('.');
        String baseName = (dot > 0) ? name.substring(0, dot) : name;
        String ext = (dot > 0) ? name.substring(dot) : ".png";
        return new File(inputFile.getParent(), baseName + suffix + ext).getAbsolutePath();
    }

    /**
     * Esegue la conversione vera e propria.
     */
    public static void convert(String inputPath, String outputPath,
                               int smallTileSize, int largeTileSize, int smallRows) throws Exception {

        BufferedImage source = ImageIO.read(new File(inputPath));

        int width = source.getWidth();
        int height = source.getHeight();
        int cols = width / smallTileSize;

        int largeRows = (height - smallRows * smallTileSize) / largeTileSize;
        int totalRows = smallRows + largeRows;

        int outputWidth = cols * largeTileSize;
        int outputHeight = totalRows * largeTileSize;

        BufferedImage result = new BufferedImage(outputWidth, outputHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = result.createGraphics();
        g.setComposite(AlphaComposite.Src);

        for (int row = 0; row < totalRows; row++) {
            boolean isSmall = row < smallRows;
            int tileSize = isSmall ? smallTileSize : largeTileSize;
            int y = isSmall
                    ? row * smallTileSize
                    : smallRows * smallTileSize + (row - smallRows) * largeTileSize;
            int tilesPerRow = width / tileSize;

            for (int col = 0; col < tilesPerRow; col++) {
                if (y + tileSize > height) continue;

                int sx = col * tileSize;
                BufferedImage tile = source.getSubimage(sx, y, tileSize, tileSize);

                int dx = col * largeTileSize + (largeTileSize - tileSize) / 2;
                int dy = row * largeTileSize + (largeTileSize - tileSize) / 2;

                g.drawImage(tile, dx, dy, null);
            }
        }

        g.dispose();
        ImageIO.write(result, "png", new File(outputPath));

        System.out.printf("Conversione completata: %s\n→ %d colonne × %d righe (%dx%d)\n",
                outputPath, cols, totalRows, largeTileSize, largeTileSize);
    }
}