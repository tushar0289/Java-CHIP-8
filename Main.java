// Main.java

import hardware.*;
import ui.EmulatorFrame;
import core.CPU;
import java.io.IOException;

public class Main {
	private static final long CPU_PERIOD_NS = 1_000_000_000L / 500;
	private static final long TIMER_PERIOD_NS = 1_000_000_000L / 60;
	private static final long FRAME_PERIOD_NS = 1_000_000_000L / 60;
		
    public static void main(String[] args) {
        String romPath = (args.length > 0) ? args[0] : "roms/pong.ch8";

        Memory memory = new Memory();
        CPU cpu = new CPU(memory);
        EmulatorFrame frame = new EmulatorFrame(cpu);

        try {
			memory.loadROM(romPath);
        } catch (IOException e) {
            System.err.println("Failed to load ROM: " + e.getMessage());
            return;
        }

        long lastTimerUpdate = System.currentTimeMillis();

	    long now = System.nanoTime();

		long lastCpuTime = now;
		long lastTimerTime = now;
		long lastFrameTime = now;

		while (true) {
			now = System.nanoTime();

			while (now - lastCpuTime >= CPU_PERIOD_NS) {
				cpu.cycle();
				lastCpuTime += CPU_PERIOD_NS;
			}

			while (now - lastTimerTime >= TIMER_PERIOD_NS) {
				cpu.updateTimers();
				lastTimerTime += TIMER_PERIOD_NS;
			}

			if (now - lastFrameTime >= FRAME_PERIOD_NS) {
				if (cpu.hasDrawnFlag()) {
					frame.render();
					cpu.clearDrawFlag();
				}

				lastFrameTime += FRAME_PERIOD_NS;
			}

			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				break;
			}
		}
    }
}
