package it.unibo.oop.lab.reactivegui02;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

/**
 * 
 */
public class ConcGUI  extends JFrame {

    private static final long serialVersionUID = 1L;
    private static final double WIDTH_PERC = 0.2;
    private static final double HEIGHT_PERC = 0.1;
    private static final int MAXBOUND = 20;
    private final JLabel display = new JLabel();
    private final JButton stop = new JButton("Stop");
    private final JButton up = new JButton("Up");
    private final JButton down = new JButton("Down");
    /**
     * 
     */
    public ConcGUI() {
        final Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        this.setSize((int) (screenSize.getWidth() * WIDTH_PERC), (int) (screenSize.getHeight() * HEIGHT_PERC));
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        final JPanel panel = new JPanel();
        panel.add(display);
        panel.add(stop);
        panel.add(up);
        panel.add(down);
        this.getContentPane().add(panel);
        this.setVisible(true);
        final Agent agent = new Agent();
        new Thread(agent).start();
        /*
         * Register a listener that stops it
         */
        stop.addActionListener(new ActionListener() {
            /**
             * event handler associated to action event on button stop.
             * 
             * @param e
             *            the action event that will be handled by this listener
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                // Agent should be final
                agent.stopCounting();
                stop.setEnabled(false);
                up.setEnabled(false);
                down.setEnabled(false);
            }
        });
        up.addActionListener(new ActionListener() {
            /**
             * @param e
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                agent.makeGrow();
            }
        });
        down.addActionListener(new ActionListener() {
            /**
             * @param e
             */
            @Override
            public void actionPerformed(final ActionEvent e) {
                agent.makeDecrease();
            }
        });
    }
    private class Agent implements Runnable {
        /*
         * Stop is volatile to ensure visibility. Look at:
         * 
         * http://archive.is/9PU5N - Sections 17.3 and 17.4
         * 
         * For more details on how to use volatile:
         * 
         * http://archive.is/4lsKW
         * 
         */
        private volatile boolean stop;
        private volatile boolean grow = true;
        private volatile int counter;

        @Override
        public void run() {
            while (!this.stop) {
                try {
                    /*
                     * All the operations on the GUI must be performed by the
                     * Event-Dispatch Thread (EDT)!
                     */
                    SwingUtilities.invokeAndWait(new Runnable() {
                        @Override
                        public void run() {
                            // This will happen in the EDT: since i'm reading counter it needs to be volatile.
                            ConcGUI.this.display.setText(Integer.toString(Agent.this.counter));
                            outBounds(MAXBOUND);
                        }
                    });
                    /*
                     * SpotBugs shows a warning because the increment of a volatile variable is not atomic,
                     * so the concurrent access is potentially not safe. In the specific case of this exercise,
                     * we do synchronization with invokeAndWait, so it can be ignored.
                     *
                     * EXERCISE: Can you think of a solution that doesn't require counter to be volatile? (without
                     * using synchronized or locks)
                     */
                    if (grow) {
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

        /**
         * External command to stop counting.
         */
        public void stopCounting() {
            this.stop = true;
        }

        public void makeGrow() {
            this.grow = true;
        }

        public void makeDecrease() {
            this.grow = false;
        }
        /**
         * Implemented game functionality to otherwise boring application.
         * @param maxBound
         */
        public void outBounds(final int maxBound) {
            if (this.counter < 0 || this.counter > maxBound) {
                JOptionPane.showMessageDialog(ConcGUI.this, "You're out of bounds!", "You lost!",
                        JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        }
    }

}
