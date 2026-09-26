package ui;

import core.CPU;
import javax.swing.JPanel;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Toolkit;

public class DisplayPanel extends JPanel {
	public static final int SCALE = 12;
	public static final int CHIP8_WIDTH = 64;
	public static final int CHIP8_HEIGHT = 32;

	private final CPU cpu;

	private static final Color COLOR_BACKGROUND = new Color(0x12, 0x12, 0x12);
	private static final Color COLOR_PIXEL = new Color(0x33, 0xFF, 0x33);

	public DisplayPanel(CPU cpu) {
		this.cpu = cpu;
		int preferredWidth = CHIP8_WIDTH * SCALE;
		int preferredHeight = CHIP8_HEIGHT * SCALE;

		setPreferredSize(new Dimension(preferredWidth, preferredHeight));
		setBackground(COLOR_BACKGROUND);

		setDoubleBuffered(true);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		boolean[][] display = cpu.getDisplay();

		g.setColor(COLOR_PIXEL);

		for (int x = 0; x < CHIP8_WIDTH; x++) {
			for(int y = 0; y < CHIP8_HEIGHT; y++) {
				if (display[x][y]) {
					g.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
				}
			}
		}
		Toolkit.getDefaultToolkit().sync();
	}
}
