package it.unibo.oop.reactivegui02;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Second example of reactive GUI.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class ConcurrentGUI extends JFrame {

    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");
    public ConcurrentGUI() {
        super();
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel canvas = new JPanel();
        canvas.add(display);
        canvas.add(up);
        canvas.add(down);
        canvas.add(stop);
        this.getContentPane().add(canvas);
        this.setVisible(true);
        final Agent agent = new Agent();
        new Thread(agent).start();
        stop.addActionListener((e) -> agent.stopCounting());
        up.addActionListener((e) -> agent.up());
        down.addActionListener((e) -> agent.down());
    }

    private class Agent implements Runnable {

        private volatile boolean stop;
        /**
         * Counter direction: true = up, false = down
         */
        private boolean direction = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> ConcurrentGUI.this.display.setText(nextText));
                    if (direction) {
                        this.counter++;
                    } else {
                        this.counter--;
                    }
                    Thread.sleep(100);
                } catch (InvocationTargetException | InterruptedException ex) {
                    /*
                     * This is just a stack trace print, in a real program there
                     * should be some logging and decent error reporting
                     */
                    ex.printStackTrace();
                }
            }
        }

        public void down() {
            this.direction = false;
        }

        public void up() {
            this.direction = true;
        }

        public void stopCounting() {
            this.stop = true;
            up.setEnabled(false);
            down.setEnabled(false);
            /**
            * Referenced stop button with ConcurrentGUI.this.stop to differentiate from boolean stop field in Agent class
            */
            ConcurrentGUI.this.stop.setEnabled(false);
        }
        
    }
}
