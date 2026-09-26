// ADK

package ui;

import core.CPU;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class KeyListener extends KeyAdapter {
	private final CPU cpu;

	public KeyListener(CPU cpu) {
		this.cpu = cpu;
	}

	@Override
	public void keyPressed(KeyEvent e) {
		setKeyState(e.getKeyCode(), true);
	}

	@Override
	public void keyReleased(KeyEvent e) {
		setKeyState(e.getKeyCode(), false);
	}

	private void setKeyState(int keyCode, boolean isPressed) {
        switch (keyCode) {
        case KeyEvent.VK_1: cpu.setKey(0x1, isPressed); break;
        case KeyEvent.VK_2: cpu.setKey(0x2, isPressed); break;
        case KeyEvent.VK_3: cpu.setKey(0x3, isPressed); break;
        case KeyEvent.VK_4: cpu.setKey(0xC, isPressed); break;

        case KeyEvent.VK_Q: cpu.setKey(0x4, isPressed); break;
        case KeyEvent.VK_W: cpu.setKey(0x5, isPressed); break;
        case KeyEvent.VK_E: cpu.setKey(0x6, isPressed); break;
        case KeyEvent.VK_R: cpu.setKey(0xD, isPressed); break;

        case KeyEvent.VK_A: cpu.setKey(0x7, isPressed); break;
        case KeyEvent.VK_S: cpu.setKey(0x8, isPressed); break;
        case KeyEvent.VK_D: cpu.setKey(0x9, isPressed); break;
        case KeyEvent.VK_F: cpu.setKey(0xE, isPressed); break;

        case KeyEvent.VK_Z: cpu.setKey(0xA, isPressed); break;
        case KeyEvent.VK_X: cpu.setKey(0x0, isPressed); break;
        case KeyEvent.VK_C: cpu.setKey(0xB, isPressed); break;
        case KeyEvent.VK_V: cpu.setKey(0xF, isPressed); break;
        }
	}
}
