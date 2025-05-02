package utils;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class TilesetPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private BufferedImage tilesetImage;
    private int tileWidth, tileHeight;
    private int rows, cols;
    private boolean[][] solidTiles;
    private Map<Point, List<Point>> animatedTiles; // Mappa delle tile animate (lista di frame)
    private Point selectedTile;
    private Point initialClick;
    private boolean isDragging = false;
    private final int DRAG_THRESHOLD = 10;
    private String currentImageFileName;

    public TilesetPanel() {
        this.solidTiles = new boolean[0][0];
        this.selectedTile = null;
        this.setPreferredSize(new Dimension(640, 480));  // Dimensione predefinita
        this.animatedTiles = new HashMap<>();

        // Aggiungi un listener per gestire i click sulle tile
        this.addMouseListener(new MouseAdapter() {
			@Override
            public void mousePressed(MouseEvent e) {
//            	System.out.println("Mouse pressed");
                initialClick = e.getPoint();
                isDragging = false;
            }
			
            @Override
            public void mouseReleased(MouseEvent e) {
//            	System.out.println("Mouse released");
                if (!isDragging) {
                    // Handle selection as normal click
                    handleClick(e);
                } else {
                    setCursor(Cursor.getDefaultCursor()); // Reset cursor on release
                    isDragging = false;
                }
            }

            private void handleClick(MouseEvent e) {
//            	System.out.println("handle click");
            	if (currentImageFileName==null)
            		return;
                int x = e.getX() / tileWidth;
                int y = e.getY() / tileHeight;

                if (y >= 0 && y < solidTiles.length && x >= 0 && x < solidTiles[0].length) {
                    selectedTile = new Point(x, y);
                    repaint();
                }
			}

        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseDragged(MouseEvent e) {
//            	System.out.println("mouse dragged");
                int deltaX = Math.abs(initialClick.x - e.getX());
                int deltaY = Math.abs(initialClick.y - e.getY());

                // If mouse moved beyond the drag threshold
                if (deltaX > DRAG_THRESHOLD || deltaY > DRAG_THRESHOLD) {
//                	System.out.println("beyond threshold");

                    isDragging = true;
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)); // Change to a drag cursor
                    handleDrag(e);
                }
            }

            public void mouseMoved(MouseEvent e) {
//            	System.out.println("mouse moved");
                setCursor(Cursor.getDefaultCursor()); // Reset cursor on release
            }  
            
			private void handleDrag(MouseEvent e) {
              // Inizia il drag & drop delle coordinate della tile
              int x = initialClick.x / tileWidth;
              int y = initialClick.y / tileHeight;
              if (y >= 0 && y < solidTiles.length && x >= 0 && x < solidTiles[0].length) {
//                  Point tileCoord = new Point(x, y);
//                  TransferablePoint transferable = new TransferablePoint(tileCoord);
                  TransferHandler handler = getTransferHandler();
                  handler.exportAsDrag(TilesetPanel.this, e, TransferHandler.COPY);
              }
			}
        });
        
        // Abilita il drag & drop con TransferHandler
        setTransferHandler(new TransferHandler("selectedTile") {
            private static final long serialVersionUID = 1L;

			@Override
            protected Transferable createTransferable(JComponent c) {
                if (initialClick != null) {
                    int x = initialClick.x / tileWidth;
                    int y = initialClick.y / tileHeight;
                    if (y >= 0 && y < solidTiles.length && x >= 0 && x < solidTiles[0].length) {
                        Point tileCoord = new Point(x, y);
                        return new TransferablePoint(tileCoord);
                    }
                }
                return null;
            }

            @Override
            public int getSourceActions(JComponent c) {
                return TransferHandler.COPY;
            }
        });

    }

    public Map<Point, List<Point>> getAnimatedTiles() {
		return animatedTiles;
	}

	// Classe interna per il trasferimento delle coordinate
    private class TransferablePoint implements Transferable {
        private Point point;

        public TransferablePoint(Point point) {
            this.point = point;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return new DataFlavor[]{DataFlavor.stringFlavor};  // Usiamo stringFlavor per la semplicità
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return DataFlavor.stringFlavor.equals(flavor);
        }

        @Override
        public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
            if (isDataFlavorSupported(flavor)) {
                return point.x + "," + point.y;  // Ritorna le coordinate come stringa
            }
            throw new UnsupportedFlavorException(flavor);
        }
    }
    
    // Metodo per caricare un'immagine
    private BufferedImage loadImage(String filePath) {
        try {
            return ImageIO.read(new File(filePath));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
	public BufferedImage getTileImage(int row, int col) {
		return tilesetImage.getSubimage(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
	}

	public boolean isTileSolid(int x, int y) {
		return solidTiles[y][x];
	}
	    
    // Imposta se una tile è solida o meno
    public void setTileSolid(int x, int y, boolean isSolid) {
        solidTiles[y][x] = isSolid;
        repaint();  // Ridisegna il tileset con le tile solide aggiornate
    }
    
    public Point getSelectedTile() {
        return selectedTile;
    }
	
    // Metodo per caricare il tileset
    public void loadTileset(String filePath) {
        this.tilesetImage = loadImage(filePath);
        this.currentImageFileName = filePath;
        
        Dimension imageSize = new Dimension(tilesetImage.getWidth(),tilesetImage.getHeight());
        Dimension tileSize = getTileDimensionsFromFile(filePath.replace(".png", ".solid.txt"));
        if (tileSize == null) {
            // Chiedi la dimensione delle tile
            tileSize = getTileSizeFromUser(new Dimension(32, 32));
            if (tileSize == null) {
                return;
                // Usa queste dimensioni per le tile
            }
        }
        
		tileWidth = imageSize.width / tileSize.width;
		tileHeight = imageSize.height / tileSize.height;

        this.cols = tilesetImage.getWidth() / tileWidth;
        this.rows = tilesetImage.getHeight() / tileHeight;
        this.solidTiles = new boolean[rows][cols];

        // Verifica se esiste un file .txt per caricare la griglia delle tile solide
        String solidName = filePath.replace(".png", ".solid.txt");
        File solidFile = new File(solidName);
        if (solidFile.exists()) {
            loadSolidTileArray(solidFile);
        }

        animatedTiles = new HashMap<>();
        
        String animationName = filePath.replace(".png", ".anim.txt");
        File animationFile = new File(animationName);
        if (animationFile.exists()) {
        	loadAnimationData(animationName);
        }
        
        this.setPreferredSize(new Dimension(tilesetImage.getWidth(), tilesetImage.getHeight()));
        repaint();
    }

    private void loadSolidTileArray(File file) {
        try (Scanner scanner = new Scanner(file)) {
            for (int row = 0; row < rows; row++) {
                String line = scanner.nextLine();
                for (int col = 0; col < cols; col++) {
                    solidTiles[row][col] = line.charAt(col) == '1';
                }
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void saveSolidTileArray() {
        if (currentImageFileName != null) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(currentImageFileName.replace(".png", ".solid.txt")))) {
                for (int row = 0; row < rows; row++) {
                    for (int col = 0; col < cols; col++) {
                        writer.print(solidTiles[row][col] ? "1" : "0");
                    }
                    writer.println();
                }
                JOptionPane.showMessageDialog(null, "Griglia salvata in " + currentImageFileName.replace(".png", ".solid.txt"));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveAnimationData() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(currentImageFileName.replace(".png", ".anim.txt")))) {
            for (Point tile : animatedTiles.keySet()) {
                String x = "(" + tile.y + "," + tile.x + ")->";
                List<Point> frames = animatedTiles.get(tile);
                for (Point frame : frames) {
                    x += "(" + frame.y + "," + frame.x + ")";
                }
                writer.write(x);  // Scrivi la riga nel file
                writer.newLine();  // Vai a capo
            }
            writer.flush();  // Assicura che tutti i dati siano stati scritti
            JOptionPane.showMessageDialog(null, "Griglia salvata in " + currentImageFileName.replace(".png", ".anim.txt"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void loadAnimationData(String fileName) {
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                // Ogni riga avrà il formato (y,x)->(frameY1,frameX1)(frameY2,frameX2)...
                String[] parts = line.split("->");
                
                // Estrai la tile principale
                String[] tileCoords = parts[0].replace("(", "").replace(")", "").split(",");
                int tileY = Integer.parseInt(tileCoords[0]);
                int tileX = Integer.parseInt(tileCoords[1]);
                Point tile = new Point(tileX, tileY);

                // Estrai i frame associati alla tile
                String[] frameStrings = parts[1].split("\\)\\(");
                List<Point> frames = new ArrayList<>();
                for (String frameString : frameStrings) {
                    String[] frameCoords = frameString.replace("(", "").replace(")", "").split(",");
                    int frameY = Integer.parseInt(frameCoords[0]);
                    int frameX = Integer.parseInt(frameCoords[1]);
                    frames.add(new Point(frameX, frameY));
                }

                // Aggiungi la tile e i frame alla mappa delle tile animate
                animatedTiles.put(tile, frames);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (tilesetImage != null) {
            // Disegna il tileset
            for (int row = 0; row < rows; row++) {
                for (int col = 0; col < cols; col++) {
                    BufferedImage tile = tilesetImage.getSubimage(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
                    Point tilePos = new Point(col, row);

                    g.drawImage(tile, col * tileWidth, row * tileHeight, null);

                    // Disegna un rettangolo attorno alla tile se è solida
                    if (solidTiles[row][col]) {
                        g.setColor(new Color(1f, 0f, 0f, .5f));  // Colore di evidenziazione per le tile solide
                        g.fillRect(col * tileWidth, row * tileHeight, tileWidth, tileHeight);
                    }
                    // Disegna l'icona della pellicola se la tile è animata
                    // Se la tile è animata, disegna un'icona
                    if (animatedTiles.containsKey(tilePos)) {
                        g.setColor(Color.GREEN);
                        g.fillRect(col * tileWidth + tileWidth - 10, row * tileHeight + tileHeight - 10, 10, 10);  // Icona di "film"
                    }

                }
            }

            // Disegna la griglia
            g.setColor(Color.GRAY);
            for (int row = 0; row <= rows; row++) {
                g.drawLine(0, row * tileHeight, cols * tileWidth, row * tileHeight);
            }
            for (int col = 0; col <= cols; col++) {
                g.drawLine(col * tileWidth, 0, col * tileWidth, rows * tileHeight);
            }

            // Disegna un simbolo per la tile selezionata
            if (selectedTile != null) {
                g.setColor(Color.YELLOW);
                g.drawRect(selectedTile.x * tileWidth, selectedTile.y * tileHeight, tileWidth, tileHeight);
                g.setColor(Color.YELLOW);
                g.fillOval(selectedTile.x * tileWidth + tileWidth / 4, selectedTile.y * tileHeight + tileHeight / 4, tileWidth / 2, tileHeight / 2); // Cerchio giallo
            }
        }
    }
    
    private  Dimension getTileSizeFromUser(Dimension defaultSize) {
        // Crea gli spinner per la larghezza e l'altezza delle tile
        JSpinner widthSpinner = new JSpinner(new SpinnerNumberModel(defaultSize.width, 1, 1000, 1));
        JSpinner heightSpinner = new JSpinner(new SpinnerNumberModel(defaultSize.height, 1, 1000, 1));

        JPanel panel = new JPanel();
        panel.add(new JLabel("Tile Width:"));
        panel.add(widthSpinner);
        panel.add(Box.createHorizontalStrut(15)); // spazio tra gli elementi
        panel.add(new JLabel("Tile Height:"));
        panel.add(heightSpinner);

        int result = JOptionPane.showConfirmDialog(null, panel, "Enter Tile Size", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            int width = (Integer) widthSpinner.getValue();
            int height = (Integer) heightSpinner.getValue();
            return new Dimension(width, height);
        } else {
            return null; // Annullato
        }
    }
    
    public Dimension getTileDimensionsFromFile(String filePath) {
        Dimension tileDimensions = null;
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filePath));
            String line = reader.readLine();
            if (line != null) {
//                String[] firstRow = line.split(",");
                int tileWidth = line.length(); // Numero di colonne (larghezza)
                int tileHeight = 1; // Numero di righe almeno una

                // Continua a contare le righe per determinare l'altezza
                while ((line = reader.readLine()) != null) {
                    tileHeight++;
                }

                tileDimensions = new Dimension(tileWidth, tileHeight);
            }
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tileDimensions;
    }    
}
