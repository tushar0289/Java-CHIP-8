package util;

import hardware.Memory;
import java.io.File;
import java.io.IOException;
import java.io.FileInputStream;

public class ROMLoader {
	public static final int START_ADDRESS = 0x200;

	public static void loadROM(Memory memory, String filePath) throws IOException {
		File file = new File(filePath);

		if(!file.exists()) {
			throw new IOException("ROM File not found at path: " + filePath);
		}
		byte[] romBytes = new byte[(int) file.length()];

		if(romBytes.length > (4096 - START_ADDRESS)) {
			throw new IOException("ROM payload exceeds available CIP-8 RAM capacity!");
		}

		try (FileInputStream fis = new FileInputStream(file)) {
			int bytesRead = fis.read(romBytes);

			for(int i = 0; i < bytesRead; i++) {
				int unsignedByte = romBytes[i] & 0xFF;
				memory.write(START_ADDRESS + i,unsignedByte);
			}
		}

		System.out.println(String.format("ROM Successfully loaded! (%d bytes written to 0x200)", romBytes.length));
	}
}
