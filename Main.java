// Main.java

import hardware.*;
import ui.EmulatorFrame;
import core.CPU;
import util.ROMLoader;

public class Main {
	private static final int CPU_CLOCK_HZ = 500;
	private static final int TARGET_FPS = 60;
	private static final int INSTRUCTIONS_PER_FRAME = CPU_CLOCK_HZ / TARGET_FPS;
	private static final long FRAME_DELAY_MS = 1000 / TARGET_FPS;

	public static void main(String[] args) {
		String romPath = (args.length > 0) ? args[0] : "roms/pong.ch8";

        Memory memory = new Memory();
        CPU cpu = new CPU(memory);
        EmulatorFrame frame = new EmulatorFrame(cpu);
		
		try {
			ROMLoader.loadROM(memory, romPath);
		}
		catch (Exception e) {
			System.err.println("Failed to lead ROM " + e.getMessage());
			System.err.println("Usage: java Main <path/to/rom.ch8");
			return;
		}

		System.out.println("Starting emulation loop...");

		while(true) {
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < INSTRUCTIONS_PER_FRAME; i++) {
				cpu.cycle();
			}

			cpu.updateTimers();

			if (cpu.hasDrawnFlag()) {
				frame.render();
				cpu.clearDrawFlag();
			}

			long elapsedTime = System.currentTimeMillis() - startTime;
			long sleepTime = FRAME_DELAY_MS - elapsedTime;

			if (sleepTime > 0) {
				try {
					Thread.sleep(sleepTime);
				}
				catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					break;
				}
			}
		}
    }
}
