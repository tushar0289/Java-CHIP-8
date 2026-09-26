// Main.java

import hardware.*;
import ui.EmulatorFrame;
import core.CPU;
import java.io.IOException;
// import util.ROMLoader;

public class Main {

    private static final int CPU_CLOCK_HZ = 500;
    private static final int TARGET_FPS = 60;
    private static final int INSTRUCTIONS_PER_FRAME = CPU_CLOCK_HZ / TARGET_FPS; 
    private static final long FRAME_TIME_MS = 1000 / TARGET_FPS; 

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

        while (true) {
            long frameStart = System.currentTimeMillis();

            for (int i = 0; i < INSTRUCTIONS_PER_FRAME; i++) {
                cpu.cycle();
            }

            long now = System.currentTimeMillis();
            if (now - lastTimerUpdate >= 16) { // ~16.6 ms (60 Hz)
                cpu.updateTimers();
                lastTimerUpdate = now;
            }

            if (cpu.hasDrawnFlag()) {
                frame.render();
                cpu.clearDrawFlag();
            }

            long elapsed = System.currentTimeMillis() - frameStart;
            long sleepMs = FRAME_TIME_MS - elapsed;

            if (sleepMs > 0) {
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
