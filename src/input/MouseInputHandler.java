package input;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class MouseInputHandler implements MouseListener, MouseMotionListener, MouseWheelListener {

    private final Set<Integer> pressedButtons = new HashSet<>();
    private final Set<Integer> releasedButtons = new HashSet<>();
    private int mouseX = 0; // Posizione corrente del cursore X
    private int mouseY = 0; // Posizione corrente del cursore Y
    private int scrollAmount = 0; // Incremento dello scroll della rotella

    @Override
    public void mousePressed(MouseEvent e) {
        pressedButtons.add(e.getButton());
        releasedButtons.remove(e.getButton());
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        pressedButtons.remove(e.getButton());
        releasedButtons.add(e.getButton());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        scrollAmount = e.getWheelRotation();
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Non utilizzato
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // Non utilizzato
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // Non utilizzato
    }

    /**
     * Restituisce se un pulsante del mouse è attualmente premuto.
     */
    public boolean isButtonPressed(int button) {
        return pressedButtons.contains(button);
    }

    /**
     * Restituisce se un pulsante del mouse è stato appena rilasciato.
     */
    public boolean isButtonReleased(int button) {
        return releasedButtons.contains(button);
    }

    /**
     * Restituisce un set immutabile di tutti i pulsanti del mouse attualmente premuti.
     */
    public Set<Integer> getPressedButtons() {
        return Collections.unmodifiableSet(pressedButtons);
    }

    /**
     * Restituisce un set immutabile di tutti i pulsanti del mouse attualmente rilasciati.
     */
    public Set<Integer> getReleasedButtons() {
        return Collections.unmodifiableSet(releasedButtons);
    }

    /**
     * Restituisce la posizione corrente del cursore del mouse (asse X).
     */
    public int getMouseX() {
        return mouseX;
    }

    /**
     * Restituisce la posizione corrente del cursore del mouse (asse Y).
     */
    public int getMouseY() {
        return mouseY;
    }

    /**
     * Restituisce l'ammontare dello scroll della rotella.
     */
    public int getScrollAmount() {
        return scrollAmount;
    }

    /**
     * Resetta l'ammontare dello scroll dopo l'elaborazione.
     */
    public void resetScrollAmount() {
        scrollAmount = 0;
    }

    /**
     * Cancella lo stato dei pulsanti rilasciati (per evitare che persistano tra i frame).
     */
    public void resetReleasedButtons() {
        releasedButtons.clear();
    }
}