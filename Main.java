// Main.java

import hardware.*;
import ui.EmulatorFrame;
import core.CPU;
import util.ROMLoader;

public class Main {

    private static final int CPU_CLOCK_HZ = 500;
    private static final int TARGET_FPS = 60;
    private static final int INSTRUCTIONS_PER_FRAME = CPU_CLOCK_HZ / TARGET_FPS; // ~8 cycles per frame
    private static final long FRAME_TIME_MS = 1000 / TARGET_FPS; // 16 ms per frame

    public static void main(String[] args) {
        String romPath = (args.length > 0) ? args[0] : "roms/pong.ch8";

        Memory memory = new Memory();
        CPU cpu = new CPU(memory);
        EmulatorFrame frame = new EmulatorFrame(cpu);

        try {
            ROMLoader.loadROM(memory, romPath);
        } catch (Exception e) {
            System.err.println("Failed to load ROM: " + e.getMessage());
            return;
        }

        long lastTimerUpdate = System.currentTimeMillis();

        while (true) {
            long frameStart = System.currentTimeMillis();

            // 1. Run CPU Cycles (~8 instructions per frame @ 60 FPS)
            for (int i = 0; i < INSTRUCTIONS_PER_FRAME; i++) {
                cpu.cycle();
            }

            // 2. Decrement Delay and Sound Timers strictly at 60 Hz
            long now = System.currentTimeMillis();
            if (now - lastTimerUpdate >= 16) { // ~16.6 ms (60 Hz)
                cpu.updateTimers();
                lastTimerUpdate = now;
            }

            // 3. Render frame only when CPU draw flag is set
            if (cpu.hasDrawnFlag()) {
                frame.render();
                cpu.clearDrawFlag();
            }

            // 4. Stable Frame Rate Control
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
