package it.unibo.oop.reactivegui03;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * Third experiment with reactive gui.
 */
@SuppressWarnings("PMD.AvoidPrintStackTrace")
public final class AnotherConcurrentGUI extends JFrame {
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private final JLabel display = new JLabel();
    private final JButton up = new JButton("up");
    private final JButton down = new JButton("down");
    private final JButton stop = new JButton("stop");
    private final Agent agent = new Agent();
    public AnotherConcurrentGUI() {
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
        new Thread(agent).start();
        final TimeAgent timeAgent = new TimeAgent();
        new Thread(timeAgent).start();
        stop.addActionListener((e) -> agent.stopCounting());
        up.addActionListener((e) -> agent.up());
        down.addActionListener((e) -> agent.down());
    }

    private class Agent implements Runnable {

        private volatile boolean stop;
        /**
         * Counter direction: true = up, false = down
         */
        private volatile boolean direction = true;
        private int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    // The EDT doesn't access `counter` anymore, it doesn't need to be volatile 
                    final var nextText = Integer.toString(this.counter);
                    SwingUtilities.invokeAndWait(() -> AnotherConcurrentGUI.this.display.setText(nextText));
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
            AnotherConcurrentGUI.this.stop.setEnabled(false);
        }  
    }

    private class TimeAgent implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(10_000);
                agent.stopCounting();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }
}
