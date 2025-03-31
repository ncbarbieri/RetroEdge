package world;

import java.awt.Dimension;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;

import helpers.Logger;

public class Tileset {
	protected BufferedImage tileset;
	protected BufferedImage[][] tileImages;
    private boolean[][] solidTiles;  // Array di booleani per le tile solide
    private Map<Point, List<Point>> animatedTiles;  // Mappa delle tile animate
	
	protected int rows;
	protected int cols;
	protected int tileWidth;
	protected int tileHeight;
	protected int numberOfFrames;

    public Tileset(String fileName, int frameWidth, int frameHeight) {
		this(fileName, frameWidth, frameHeight, 0, 0);
	}
	
    public Tileset(String fileName) throws Exception {
		InputStream is = getClass().getResourceAsStream(fileName);
		try {
			tileset = ImageIO.read(is);
	        Dimension tilesetSize = getTileDimensionsFromFile(fileName.replace(".png", ".solid.txt"));
	        if (tilesetSize != null) {
		        cols = tilesetSize.width;
		        rows = tilesetSize.height;
				tileWidth = tileset.getWidth() / tilesetSize.width;
				tileHeight = tileset.getHeight() / tilesetSize.height;
	        } else {
	        	throw new Exception("Solid data file not found!");
	        }

			// Carico la mappa delle tile solide
            String solidFileName = fileName.replace(".png", ".solid.txt");
           	loadSolidMap(solidFileName);
			
			// Carico la mappa delle tile animate
           	
           	String animFileName = fileName.replace(".png", ".anim.txt");
           	loadAnimationData(animFileName);

           	// Carico le immagini
			this.tileImages = new BufferedImage[this.rows][this.cols];
			for (int i = 0; i < this.rows; i++) {
				for (int j = 0; j < this.cols; j++) {
					tileImages[i][j] = tileset.getSubimage(tileWidth * j,
							tileHeight * i, tileWidth, tileHeight);
				}
			}
		} catch (IOException e) {
			Logger.log("Error loading tileset.", e);
		} finally {
			try {
				// to free resources
				is.close();
			} catch (IOException e) {
				Logger.log("Error closing tileset files.", e);
			}
		}
		
	}
	
	public Tileset(String fileName, int tileWidth, int tileHeight, int margin, int spacing) {
		InputStream is = getClass().getResourceAsStream(fileName);
		try {
			tileset = ImageIO.read(is);
			int width = tileset.getWidth();
			int height = tileset.getHeight();
			this.rows = (height - margin * 2) / (tileHeight + spacing);
			this.cols = (width - margin * 2) / (tileWidth + spacing);
			this.tileWidth = tileWidth;
			this.tileHeight = tileHeight;

			// Carico la mappa delle tile solide
            String txtFileName = fileName.replace(".png", ".solid.txt");
           	loadSolidMap(txtFileName);
			
			// Carico la mappa delle tile animate
           	
           	String animFileName = fileName.replace(".png", ".anim.txt");
           	loadAnimationData(animFileName);

           	// Carico le immagini
			this.tileImages = new BufferedImage[this.rows][this.cols];
			for (int i = 0; i < this.rows; i++) {
				for (int j = 0; j < this.cols; j++) {
					tileImages[i][j] = tileset.getSubimage(margin + (tileWidth + spacing) * j,
							margin + (tileHeight + spacing) * i, tileWidth, tileHeight);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				// to free resources
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	protected void loadSolidMap(String fileName) {
	    this.solidTiles = new boolean[rows][cols];
	    try {
	        InputStream is = getClass().getResourceAsStream(fileName);
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
	        String line;
	        for (int i = 0; i < rows; i++) {
	            line = br.readLine();
	            for (int j = 0; j < cols; j++) {
	                // Considera '1' come vero e '0' come falso, senza il bisogno di un separatore
	            	this.solidTiles[i][j] = (line.charAt(j) == '1');
	            }
	        }
	        br.close();  // Chiudi lo stream una volta terminato
	    } catch (Exception ex) {
	        Logger.log("Error reading solid data "+fileName);
	    }
	}
	
    // Metodo per caricare i dati di animazione da un file
    public void loadAnimationData(String fileName) {
       	this.animatedTiles = new HashMap<>();
	    try {
	    	this.numberOfFrames = 1;
	        InputStream is = getClass().getResourceAsStream(fileName);
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = br.readLine()) != null) {
                // Ogni riga avrà il formato (y,x)->(frameY1,frameX1)(frameY2,frameX2)...
                String[] parts = line.split("->");

                // Estrai la tile principale
                String[] tileCoords = parts[0].replace("(", "").replace(")", "").split(",");
                int tileY = Integer.parseInt(tileCoords[0]);
                int tileX = Integer.parseInt(tileCoords[1]);
                Point tile = new Point(tileX, tileY);

                // Estrai i frame associati alla tile
                int frameCount = 0;
                String[] frameStrings = parts[1].split("\\)\\(");
                List<Point> frames = new ArrayList<>();
                for (String frameString : frameStrings) {
                    String[] frameCoords = frameString.replace("(", "").replace(")", "").split(",");
                    int frameY = Integer.parseInt(frameCoords[0]);
                    int frameX = Integer.parseInt(frameCoords[1]);
                    frames.add(new Point(frameX, frameY));
                    frameCount++;
                }

                // Aggiungi la tile e i frame alla mappa delle tile animate
                animatedTiles.put(tile, frames);
                if (frameCount > this.numberOfFrames)
                	this.numberOfFrames = frameCount;
            }
	        br.close();  // Chiudi lo stream una volta terminato
	    } catch (Exception ex) {
	        Logger.log("Error reading animation file "+fileName);
	    }
    }

    public Dimension getTileDimensionsFromFile(String fileName) {
        Dimension tileDimensions = null;
        try {
	        InputStream is = getClass().getResourceAsStream(fileName);
	        BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line = br.readLine();
            if (line != null) {
                int tileWidth = line.length(); // Numero di colonne (larghezza)
                int tileHeight = 1; // Numero di righe almeno una

                // Continua a contare le righe per determinare l'altezza
                while ((line = br.readLine()) != null) {
                    tileHeight++;
                }

                tileDimensions = new Dimension(tileWidth, tileHeight);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return tileDimensions;
    }    

    // Metodo per ottenere i frame associati a una tile animata
    public List<Point> getAnimationFrames(int tileX, int tileY) {
        return animatedTiles.get(new Point(tileX, tileY));
    }

	public int getRows() {
		return rows;
	}

	public int getCols() {
		return cols;
	}

	public int getTileWidth() {
		return tileWidth;
	}

	public int getTileHeight() {
		return tileHeight;
	}
	
	public BufferedImage getTileImage(int index) {
		return tileImages[index/cols][index%cols];
	}
	public BufferedImage getTileImage(int row, int col) {
		return tileImages[row][col];
	}
	
	public Tile createTile(int index, int row, int col) {
		int tileRow = index / cols;
		int tileCol = index % cols;
	    // Controllo dei limiti per la tile principale
	    if (tileRow >= 0 && tileRow < rows && tileCol >= 0 && tileCol < cols) {
	        // Verifica se la tile è presente nella mappa delle tile animate
	        List<Point> animationFrames = animatedTiles.get(new Point(tileCol, tileRow));

	        if (animationFrames != null) {
	            // Se la tile è animata, crea una tile animata con i frame corretti
	            BufferedImage[] animatedImages = new BufferedImage[animationFrames.size()];
	            
	            for (int i = 0; i < animationFrames.size(); i++) {
	                Point frame = animationFrames.get(i);
	                // Protezione contro gli array out of bounds per i frame animati
	                if (frame.y >= 0 && frame.y < rows && frame.x >= 0 && frame.x < cols) {
	                    animatedImages[i] = tileImages[frame.y][frame.x];  // Recupera l'immagine del frame dalla matrice di immagini
	                } else {
	                    // Se uno dei frame è fuori dai limiti, restituisci null
	                    return null;
	                }
	            }

	            // Creiamo una tile animata con i frame e impostiamo la durata per frame (es. 0.1 secondi per frame)
	            return new Tile(animatedImages, solidTiles[tileRow][tileCol], row, col, 0.1f);  // Passa la durata dei frame come parametro
	        } else {
	            // Se la tile non è animata, crea una tile statica
	            return new Tile(tileImages[tileRow][tileCol], solidTiles[tileRow][tileCol], row, col);
	        }
	    } else {
	        // Se le coordinate della tile sono fuori dai limiti, restituisci null
	        return null;
	    }
	}

	public int getNumberOfFrames() {
		return numberOfFrames;
	}

}
