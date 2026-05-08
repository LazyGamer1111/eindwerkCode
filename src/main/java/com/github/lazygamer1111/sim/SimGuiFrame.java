package com.github.lazygamer1111.sim;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Hashtable;

/**
 * Simple Swing GUI for the simulator. Visualizes steering and throttle and
 * provides buttons/sliders and keyboard controls to drive the shared controllerData.
 */
public class SimGuiFrame extends JFrame {
    private final int[] controllerData; // shared array
    private final ControllerSimulator simulator;

    private final JLabel lblSteerVal = new JLabel();
    private final JLabel lblThrottleVal = new JLabel();
    private final JLabel lblRange = new JLabel();
    private final JLabel lblKill = new JLabel();
    private final JLabel lblServoAngle = new JLabel();

    private final JSlider sldSteer = new JSlider(1000, 2000, 1500);
    private final JSlider sldThrottle = new JSlider(1000, 2000, 1000);

    private final RenderPanel renderPanel = new RenderPanel();

    public SimGuiFrame(int[] controllerData, ControllerSimulator simulator) {
        super("Eindwerk Simulator (GUI)");
        this.controllerData = controllerData;
        this.simulator = simulator;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(900, 600);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(10, 10, 10, 10));
        setContentPane(root);

        // Left: visualization
        renderPanel.setPreferredSize(new Dimension(520, 520));
        root.add(renderPanel, BorderLayout.CENTER);

        // Right: controls
        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(0, 10, 0, 0));
        root.add(right, BorderLayout.EAST);

        // Sliders
        sldSteer.setMajorTickSpacing(250);
        sldSteer.setPaintTicks(true);
        sldSteer.setPaintLabels(true);
        Hashtable<Integer, JLabel> steerLabels = new Hashtable<>();
        steerLabels.put(1000, new JLabel("L"));
        steerLabels.put(1500, new JLabel("C"));
        steerLabels.put(2000, new JLabel("R"));
        sldSteer.setLabelTable(steerLabels);

        sldThrottle.setMajorTickSpacing(250);
        sldThrottle.setPaintTicks(true);
        sldThrottle.setPaintLabels(true);
        Hashtable<Integer, JLabel> thrLabels = new Hashtable<>();
        thrLabels.put(1000, new JLabel("0%"));
        thrLabels.put(1500, new JLabel("50%"));
        thrLabels.put(2000, new JLabel("100%"));
        sldThrottle.setLabelTable(thrLabels);

        JPanel pnlSteer = new JPanel(new BorderLayout());
        pnlSteer.setBorder(BorderFactory.createTitledBorder("Steering (ch0)"));
        pnlSteer.add(sldSteer, BorderLayout.CENTER);
        pnlSteer.add(lblSteerVal, BorderLayout.SOUTH);

        JPanel pnlThrottle = new JPanel(new BorderLayout());
        pnlThrottle.setBorder(BorderFactory.createTitledBorder("Throttle (ch2)"));
        pnlThrottle.add(sldThrottle, BorderLayout.CENTER);
        pnlThrottle.add(lblThrottleVal, BorderLayout.SOUTH);

        right.add(pnlSteer);
        right.add(Box.createVerticalStrut(10));
        right.add(pnlThrottle);
        right.add(Box.createVerticalStrut(10));

        JPanel pnlButtons = new JPanel(new GridLayout(0, 3, 5, 5));
        pnlButtons.setBorder(BorderFactory.createTitledBorder("Controls"));
        JButton btnW = new JButton("W +");
        JButton btnS = new JButton("S -");
        JButton btnA = new JButton("A ←");
        JButton btnD = new JButton("D →");
        JButton btnC = new JButton("Center C");
        JButton btnR = new JButton("Range R");
        JButton btnK = new JButton("Kill K");
        JButton btnB = new JButton("Beacon B");

        pnlButtons.add(btnW);
        pnlButtons.add(btnS);
        pnlButtons.add(btnC);
        pnlButtons.add(btnA);
        pnlButtons.add(btnD);
        pnlButtons.add(new JLabel());
        pnlButtons.add(btnR);
        pnlButtons.add(btnK);
        pnlButtons.add(btnB);

        right.add(pnlButtons);
        right.add(Box.createVerticalStrut(10));

        JPanel pnlInfo = new JPanel();
        pnlInfo.setLayout(new BoxLayout(pnlInfo, BoxLayout.Y_AXIS));
        pnlInfo.setBorder(BorderFactory.createTitledBorder("Info"));
        pnlInfo.add(lblRange);
        pnlInfo.add(lblKill);
        pnlInfo.add(lblServoAngle);
        right.add(pnlInfo);

        // Bindings
        btnW.addActionListener(e -> simulator.acceptCommand("w"));
        btnS.addActionListener(e -> simulator.acceptCommand("s"));
        btnA.addActionListener(e -> simulator.acceptCommand("a"));
        btnD.addActionListener(e -> simulator.acceptCommand("d"));
        btnC.addActionListener(e -> simulator.acceptCommand("c"));
        btnR.addActionListener(e -> simulator.acceptCommand("r"));
        btnK.addActionListener(e -> simulator.acceptCommand("k"));
        btnB.addActionListener(e -> simulator.acceptCommand("b"));

        sldSteer.addChangeListener(e -> controllerData[0] = clamp(sldSteer.getValue(), 1000, 2000));
        sldThrottle.addChangeListener(e -> controllerData[2] = clamp(sldThrottle.getValue(), 1000, 2000));

        // Keyboard shortcuts on root pane
        addKeyBinding(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), root.getActionMap(), "W", "incThr", () -> simulator.acceptCommand("w"));
        addKeyBinding(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), root.getActionMap(), "S", "decThr", () -> simulator.acceptCommand("s"));
        addKeyBinding(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), root.getActionMap(), "A", "left", () -> simulator.acceptCommand("a"));
        addKeyBinding(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), root.getActionMap(), "D", "right", () -> simulator.acceptCommand("d"));
        addKeyBinding(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), root.getActionMap(), "C", "center", () -> simulator.acceptCommand("c"));
        addKeyBinding(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), root.getActionMap(), "R", "range", () -> simulator.acceptCommand("r"));
        addKeyBinding(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), root.getActionMap(), "K", "kill", () -> simulator.acceptCommand("k"));
        addKeyBinding(root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), root.getActionMap(), "B", "beacon", () -> simulator.acceptCommand("b"));

        // Update loop using Swing Timer
        new Timer(33, e -> tick()).start();

        // Initialize labels
        tick();
    }

    private void addKeyBinding(InputMap im, ActionMap am, String key, String name, Runnable r) {
        im.put(KeyStroke.getKeyStroke(key), name);
        am.put(name, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) { r.run(); }
        });
    }

    private int clamp(int v, int lo, int hi) { return Math.max(lo, Math.min(hi, v)); }

    private void tick() {
        // Sync slider positions if user pressed keys/buttons
        if (!sldSteer.getValueIsAdjusting()) sldSteer.setValue(controllerData[0]);
        if (!sldThrottle.getValueIsAdjusting()) sldThrottle.setValue(controllerData[2]);

        lblSteerVal.setText("ch0=" + controllerData[0]);
        lblThrottleVal.setText("ch2=" + controllerData[2]);
        lblRange.setText("range ch7=" + controllerData[7]);
        lblKill.setText("kill ch8=" + controllerData[8]);

        // Compute same servo mapping as SimIOJob: 90 - ((ch0-1000)*90/1000)
        double servoThing = controllerData[0] - 1000; // 0..1000
        double angle = (servoThing * 90) / 1000.0; // 0..90
        double simAngle = 90 - angle;
        lblServoAngle.setText(String.format("servo angle=%.1f°", simAngle));

        renderPanel.repaint();
    }

    private class RenderPanel extends JPanel {
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int w = getWidth();
            int h = getHeight();
            g2.setColor(new Color(30, 30, 30));
            g2.fillRect(0, 0, w, h);

            // draw a simple track grid
            g2.setColor(new Color(60, 60, 60));
            for (int x = 0; x < w; x += 40) g2.drawLine(x, 0, x, h);
            for (int y = 0; y < h; y += 40) g2.drawLine(0, y, w, y);

            // Car position (centered), steering based on ch0, speed bar based on ch2
            double steerNorm = (controllerData[0] - 1500) / 500.0; // -1..1
            double throttleNorm = (controllerData[2] - 1000) / 1000.0; // 0..1

            int carW = 120;
            int carH = 60;
            int cx = w / 2;
            int cy = h / 2;

            // Car body
            int x = cx - carW / 2;
            int y = cy - carH / 2;
            g2.setColor(new Color(0x2D, 0x9C, 0xDB));
            g2.fillRoundRect(x, y, carW, carH, 16, 16);
            g2.setColor(Color.WHITE);
            g2.drawRoundRect(x, y, carW, carH, 16, 16);

            // Wheels - front wheels rotated by steering
            g2.setStroke(new BasicStroke(3f));
            double steerAngle = steerNorm * Math.toRadians(30); // +/-30° visual
            int wheelLen = 24;
            int fwY = y + 10;
            int lwx = x + 15;
            int rwx = x + carW - 15;
            drawWheel(g2, lwx, fwY, wheelLen, steerAngle);
            drawWheel(g2, rwx, fwY, wheelLen, steerAngle);

            // Heading line to show direction
            int hx = cx;
            int hy = y + carH + 10;
            int headingLen = 80;
            g2.setColor(new Color(255, 255, 255, 180));
            g2.drawLine(hx, hy, hx + (int)(Math.sin(steerAngle) * headingLen), hy - (int)(Math.cos(steerAngle) * headingLen));

            // Throttle bar
            int barW = 20;
            int barH = 200;
            int barX = w - 50;
            int barY = h / 2 - barH / 2;
            g2.setColor(Color.DARK_GRAY);
            g2.fillRect(barX, barY, barW, barH);
            g2.setColor(new Color(0x4C, 0xAF, 0x50));
            int fill = (int) Math.round(barH * throttleNorm);
            g2.fillRect(barX, barY + (barH - fill), barW, fill);
            g2.setColor(Color.WHITE);
            g2.drawRect(barX, barY, barW, barH);

            g2.dispose();
        }

        private void drawWheel(Graphics2D g2, int cx, int cy, int len, double angleRad) {
            int x2 = cx + (int) Math.round(Math.sin(angleRad) * len);
            int y2 = cy - (int) Math.round(Math.cos(angleRad) * len);
            g2.drawLine(cx, cy, x2, y2);
        }
    }
}
