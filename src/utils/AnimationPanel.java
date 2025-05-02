package utils;

import javax.swing.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

public class AnimationPanel extends JPanel  implements Runnable {
    private static final long serialVersionUID = 1L;
	private int currentFrame; // Frame corrente per ogni tile animata
    private List<BufferedImage> frames;
    private Thread thread;
    private JCheckBox solidCheckbox;
    private JCheckBox animatedCheckbox;
    private TilesetPanel tilesetPanel;
    private Point selectedTile;
    private JPanel frameGrid;   // Griglia per visualizzare i frame dell'animazione
    private JLabel previewLabel; // Etichetta per l'anteprima
    private JLabel tileInfoLabel; // Etichetta per mostrare le informazioni della tile selezionata
    private long frameDelay = 100;  // Ritardo tra i frame (in millisecondi)
    private boolean running;

    public AnimationPanel(TilesetPanel tilesetPanel) {
        this.tilesetPanel = tilesetPanel;
        this.currentFrame = 0;
        this.frames = new ArrayList<>();
        this.running = false;
		thread = new Thread(this);

        // Crea e configura i checkbox
        solidCheckbox = new JCheckBox("Solida");
        solidCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedTile != null) {
                    tilesetPanel.setTileSolid(selectedTile.x, selectedTile.y, solidCheckbox.isSelected());
                }
            }
        });

        animatedCheckbox = new JCheckBox("Animata");
        animatedCheckbox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (selectedTile != null) {
                    setTileAnimated(selectedTile.x, selectedTile.y, animatedCheckbox.isSelected());
                    updateFrameGrid();  // Aggiorna la griglia dei frame se la tile è animata
                }
            }
        });

        // Layout del pannello
        setLayout(new GridLayout(4, 1));

        // Pannello per i checkbox
        JPanel checkboxPanel = new JPanel(new GridLayout(1, 2));
        checkboxPanel.add(solidCheckbox);
        checkboxPanel.add(animatedCheckbox);

        // Etichetta per le informazioni della tile
        tileInfoLabel = new JLabel("Seleziona una tile");

        JPanel infoPanel = new JPanel(new GridLayout(2, 1));
        infoPanel.add(tileInfoLabel);
        infoPanel.add(checkboxPanel);

        // Anteprima della tile selezionata
        previewLabel = new JLabel();
        previewLabel.setHorizontalAlignment(JLabel.CENTER);
        previewLabel.setPreferredSize(new Dimension(128,128));
        previewLabel.setBorder(BorderFactory.createTitledBorder("Anteprima"));

        // Pannello per i frame dell'animazione
        frameGrid = new JPanel(new GridLayout(1, 1));  // Vuoto inizialmente

        JPanel optionsPanel = new JPanel(new GridLayout(1, 2));
        JLabel speedLabel = new JLabel("Velocità Animazione (ms):");
        JSlider speedSlider = new JSlider(50,500,100);
        optionsPanel.add(speedLabel);
        optionsPanel.add(speedSlider);
        
        speedSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                // Imposta la velocità dell'animazione in base al valore del JSlider
            	frameDelay = speedSlider.getValue();
            }
        });
        
        // Aggiunge i componenti al pannello principale
        add(infoPanel);
        add(previewLabel);
        add(frameGrid);
        add(optionsPanel);

        setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
            public boolean canImport(TransferSupport support) {
                return support.isDataFlavorSupported(DataFlavor.stringFlavor);
            }

            @Override
            public boolean importData(TransferSupport support) {
                if (!canImport(support)) {
                    return false;
                }

                try {
                    Transferable transferable = support.getTransferable();
                    String data = (String) transferable.getTransferData(DataFlavor.stringFlavor);
                    String[] coords = data.split(",");
                    Point frameCoord = new Point(Integer.parseInt(coords[0].trim()), Integer.parseInt(coords[1].trim()));

                    if (selectedTile != null) {
                        addFrameToAnimation(frameCoord);  // Aggiungi il frame all'animazione
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
                return true;
            }
        });
        
    }

    // Gestione dell'animazione delle tile
    public void setTileAnimated(int col, int row, boolean isAnimated) {
        Point tilePos = new Point(col, row);
        if (isAnimated) {
        	frames.add(tilesetPanel.getTileImage(row, col));
        	tilesetPanel.getAnimatedTiles().computeIfAbsent(tilePos, k -> new ArrayList<>()).add(tilePos);
        	if (!tilesetPanel.getAnimatedTiles().containsKey(tilePos)) {
        		List<Point> animatedTiles = new ArrayList<>();
        		animatedTiles.add(tilePos);
        		tilesetPanel.getAnimatedTiles().put(tilePos, animatedTiles);
        	}
        	updateFrameGrid();
            currentFrame = 0;  // Inizializza il frame corrente a 0 se non esiste
        	running = true;
    		thread = new Thread(this);
    		thread.start();
        } else {
        	tilesetPanel.getAnimatedTiles().remove(tilePos);
        	frames.clear();
            running = false;
        }
        repaint();
    }

    public void getTileFrames(Point tile) {
        List<Point> animation = tilesetPanel.getAnimatedTiles().get(tile);
        frames.clear();
        if (animation != null) {
            for (Point point : animation) {
            	frames.add(tilesetPanel.getTileImage(point.y, point.x));
            }
        }
    }

    public boolean isTileAnimated(Point tile) {
        return tilesetPanel.getAnimatedTiles().containsKey(tile);
    }

    public void setSelectedTile(Point tile) {
        this.selectedTile = tile;
        running = false;
        try {
			thread.join();
		} catch (InterruptedException e) {
			System.out.println("Error joining thread");
		}
        getTileFrames(tile);  // Ottieni i frame da tilesetPanel
        if (frames != null && !frames.isEmpty()) {
        	currentFrame = 0;
        	running = true;
    		thread = new Thread(this);
    		thread.start();
        }
        
        solidCheckbox.setSelected(tilesetPanel.isTileSolid(tile.x, tile.y));
        animatedCheckbox.setSelected(isTileAnimated(tile));

        // Aggiorna l'anteprima della tile selezionata
        BufferedImage tileImage = tilesetPanel.getTileImage(tile.y, tile.x);
        previewLabel.setIcon(new ImageIcon(tileImage));

        // Aggiorna le informazioni della tile
        tileInfoLabel.setText(String.format("Tile: [%d, %d]", tile.y, tile.x));

        updateFrameGrid();  // Aggiorna la griglia dei frame se animata
        repaint();
    }

    public void addFrameToAnimation(Point tileCoord) {
        List<Point> animation = tilesetPanel.getAnimatedTiles().get(selectedTile);
        if (animation != null) {
        	animation.add(tileCoord);
        	frames.add(tilesetPanel.getTileImage(tileCoord.y, tileCoord.x));
            updateFrameGrid();  // Aggiorna la griglia dopo aver aggiunto il frame
            repaint();
        }
    }

    public void removeFrameFromAnimation(BufferedImage frame) {
    	int index = frames.indexOf(frame);
        List<Point> animation = tilesetPanel.getAnimatedTiles().get(selectedTile);
        if (animation != null && index>=0 && index<frames.size()) {
        	animation.remove(index);
        	frames.remove(index);
        }
    }

    private void updateFrameGrid() {
        frameGrid.removeAll();  // Rimuove i frame precedenti

        if (animatedCheckbox.isSelected() && !frames.isEmpty()) {
            for (int i = 0; i < frames.size(); i++) {
                BufferedImage frame = frames.get(i);
                JButton frameButton = new JButton(new ImageIcon(frame));
                
                if (i > 0) {  // Permetti di cancellare solo dal secondo frame in poi
                    frameButton.addActionListener(e -> {
                        running = false;
                        try {
            				thread.join();
            			} catch (InterruptedException ex) {
            				System.out.println("Error joining thread");
            			}
                        currentFrame = 0;
                    	removeFrameFromAnimation(frame);
                        updateFrameGrid();  // Aggiorna la griglia
                        repaint();
                    	running = true;
                		thread = new Thread(this);
                		thread.start();
                    });
                }
                
                frameGrid.add(frameButton);
            }

        } else {
            frameGrid.setLayout(new GridLayout(1, 1));  // Reset layout quando non ci sono frame
        }
        revalidate();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
    }
    
	@Override
    public void run() {
        while (running) {
            // Aggiorna i frame per ogni tile animata
        	if (!frames.isEmpty()) {
                BufferedImage tileImage = frames.get(currentFrame);
                previewLabel.setIcon(new ImageIcon(tileImage));
                repaint();  // Ridisegna la griglia delle tile
        		currentFrame = (currentFrame+1)%frames.size();
                try {
                    Thread.sleep(frameDelay);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
        	}
        }
    }
}
