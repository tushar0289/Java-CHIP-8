package ui;

import core.CPU;
import javax.swing.JFrame;

public class EmulatorFrame extends JFrame {
	private final DisplayPanel displayPanel;

	public EmulatorFrame(CPU cpu) {
		super("CHIP-8 Emulaotr");

		displayPanel = new DisplayPanel(cpu);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		add(displayPanel);

		addKeyListener(new KeyListener(cpu));
		setFocusable(true);
		setFocusTraversalKeysEnabled(false);

		pack();
		setLocationRelativeTo(null);
		requestFocusInWindow();

		pack();
		setLocationRelativeTo(null);
		setVisible(true);
	}

	public void render() {
		displayPanel.repaint();
	}
}
