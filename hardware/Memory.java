package hardware;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Memory {
	private final byte[] ram = new byte[4096];
	public static final int PROGRAM_START_ADDRESS = 0x200;

	private static final int[] FONT_SET = {
		0xF0, 0x90, 0x90, 0x90, 0xF0, // 0
		0x20, 0x60, 0x20, 0x20, 0x70, // 1
		0xF0, 0x10, 0xF0, 0x80, 0xF0, // 2
		0xF0, 0x10, 0xF0, 0x10, 0xF0, // 3
		0x90, 0x90, 0xF0, 0x10, 0x10, // 4
		0xF0, 0x80, 0xF0, 0x10, 0xF0, // 5
		0xF0, 0x80, 0xF0, 0x90, 0xF0, // 6
		0xF0, 0x10, 0x20, 0x40, 0x40, // 7
		0xF0, 0x90, 0xF0, 0x90, 0xF0, // 8
		0xF0, 0x90, 0xF0, 0x10, 0xF0, // 9
		0xF0, 0x90, 0xF0, 0x90, 0x90, // A
		0xE0, 0x90, 0xE0, 0x90, 0xE0, // B
		0xF0, 0x80, 0x80, 0x80, 0xF0, // C
		0xE0, 0x90, 0x90, 0x90, 0xE0, // D
		0xF0, 0x80, 0xF0, 0x80, 0xF0, // E
		0xF0, 0x80, 0xF0, 0x80, 0x80  // F
	};

	public int read (int address) {
		checkBounds (address);
		return ram[address] & 0xFF;
	}

	public void write (int address, int value) {
		checkBounds(address);
		ram[address] = (byte) (value & 0xFF);
	}

	public void checkBounds (int address) {
		if (address < 0 || address >= ram.length) {
			throw new IndexOutOfBoundsException(
					String.format("Memory access violation at address: 0x%03X", address)
					);
		}
	}

	public void clear () {
		for (int i = 0; i < ram.length; i++) {
			ram[i] = 0;
		}
	}

	public Memory() {
		loadFontSet();
	}

	public void loadFontSet() {
		int fontStartAddress = 0x050;
		for (int i = 0; i < FONT_SET.length; i++) {
			ram[fontStartAddress + i] = (byte) FONT_SET[i];
		}
	}

	public void loadROM (String filePath) throws IOException {
		File file = new File(filePath);

		if (!file.exists()) {
			throw new IOException("ROM file not found: " + filePath);
		}

		int maxAllowedSize = ram.length - PROGRAM_START_ADDRESS;
		if (file.length() > maxAllowedSize) {
			throw new IOException(String.format (
						"ROM file too large (%d bytes). Max capacity is %d bytes.", file.length(), maxAllowedSize
						));
		}

		try (FileInputStream fis = new FileInputStream(file)) {
			byte[] buffer = fis.readAllBytes();
			for (int i = 0; i < buffer.length; i++) {
				ram[PROGRAM_START_ADDRESS + i] = buffer[i];
			}

			System.out.println(String.format(
						"[Memory] Successfully loaded %d bytes into RAM starting at 0x%03X", buffer.length, PROGRAM_START_ADDRESS
						));
		}
	}
}
