// fardin

package core;

import hardware.Memory;
import java.util.Random;

public class CPU {
	private final Memory memory;
	private final int[] V = new int[16];
	private int I;
	private int pc;
	private int[] stack = new int[16];
	private int sp;
	private int delayTimer;
	private int soundTimer;

	private final boolean[] keys = new boolean[16];
	private final boolean[][] display = new boolean[64][32];
	private boolean drawFlag = false;
	private final Random random = new Random();

	public CPU(Memory memory) {
		this.memory = memory;
		reset();
	}

	public void reset() {
		for (int i = 0; i < V.length; i++) {
			V[i] = 0;
		}

		for (int i = 0; i < stack.length; i++) {
			stack[i] = 0;
		}

		for (int i = 0; i < keys.length; i++) {
			keys[i] = false;
		}

		clearDisplay();

		I = 0;
		sp = 0;
		delayTimer = 0;
		soundTimer = 0;

		pc = Memory.PROGRAM_START_ADDRESS;
		drawFlag = true;
	}

	public void clearDisplay() {
		for(int x = 0; x < 64; x++) {
			for(int y = 0; y < 32; y++) {
				display[x][y] = false;
			}
		}
		drawFlag = true;
	}


	public void cycle() {
		int byte1 = memory.read(pc);
		int byte2 = memory.read(pc + 1);
		int opcode = (byte1 << 8) | byte2;

		pc += 2;

		int x = (opcode & 0x0F00) >> 8;
		int y = (opcode & 0x00F0) >> 4;
		int four_bits = opcode & 0x000F;
		int eight_bits = opcode & 0x00FF;
		int twelve_bits = opcode & 0x0FFF;

		executeOpcode(opcode, x, y, four_bits, eight_bits, twelve_bits);
	}

	private void executeOpcode(int opcode, int x, int y, int four_bits, int eight_bits, int twelve_bits) {
		switch (opcode & 0xF000) {
			case 0x0000:
				switch(eight_bits) {
					case 0x00E0:
						clearDisplay();
						break;
					case 0x00EE:
						if (sp <= 0) {
							throw new IllegalStateException("CHIP-8 stack underflow");
						}

						sp--;
						pc = stack[sp];
						break;
					default:
						System.out.println(String.format("Unknown 0x0000 Opcode Opcode: 0x%04X", opcode));
						break;
				}
				break;

			case 0x1000:
				pc = twelve_bits;
				break;
			case 0x2000:
				if (sp >= stack.length) {
					throw new IllegalStateException("CHIP-8 stack overflow");
				}

				stack[sp] = pc;
				sp++;
				pc = twelve_bits;
				break;
			case 0x3000:
				if(V[x] == eight_bits) {
					pc += 2;
				}
				break;
			case 0x4000:
				if(V[x] != eight_bits) {
					pc += 2;
				}
				break;
			case 0x5000:
				if (four_bits != 0) {
					System.err.printf("Unknown opcode: 0x%04X%n", opcode);
					break;
				}

				if (V[x] == V[y]) {
					pc += 2;
				}
				break;
			case 0x6000:
				V[x] = eight_bits;
				break;
			case 0x7000:
				V[x] = (V[x] + eight_bits) & 0xFF;
				break;
			case 0x8000:
                switch (four_bits) {
                    case 0x0: V[x] = V[y]; break;
                    case 0x1: V[x] = (V[x] | V[y]) & 0xFF; break;
                    case 0x2: V[x] = (V[x] & V[y]) & 0xFF; break;
                    case 0x3: V[x] = (V[x] ^ V[y]) & 0xFF; break;

                    case 0x4:
                        int sum = V[x] + V[y];
                        V[0xF] = (sum > 255) ? 1 : 0;
                        V[x] = sum & 0xFF;
                        break;

                    case 0x5:
                        V[0xF] = (V[x] >= V[y]) ? 1 : 0;
                        V[x] = (V[x] - V[y]) & 0xFF;
                        break;

                    case 0x6:
                        V[0xF] = V[x] & 0x1;
                        V[x] = (V[x] >> 1) & 0xFF;
                        break;

                    case 0x7:
                        V[0xF] = (V[y] >= V[x]) ? 1 : 0;
                        V[x] = (V[y] - V[x]) & 0xFF;
                        break;

                    case 0x0E:
                        V[0xF] = (V[x] >> 7) & 0x1;
                        V[x] = (V[x] << 1) & 0xFF;
                        break;

                    default:
                        System.out.println(String.format("Unknown 0x8000 Opcode: 0x%04X", opcode));
                        break;
                }
                break;

			case 0x9000:
				if (four_bits != 0) {
					System.err.printf("Unknown opcode: 0x%04X%n", opcode);
					break;
				}

				if (V[x] != V[y]) {
					pc += 2;
				}
				break;

            case 0xA000:
                I = twelve_bits;
                break;

            case 0xB000:
                pc = twelve_bits + V[0];
                break;

            case 0xC000:
                V[x] = (random.nextInt(256)) & eight_bits;
                break;

            case 0xD000:
				drawSprite(x, y, four_bits);
            case 0xE000:
                switch (eight_bits) {
                    case 0x9E:
                        if (keys[V[x] & 0xF]) pc += 2;
                        break;

                    case 0xA1:
                        if (!keys[V[x] & 0xF]) pc += 2;
                        break;

                    default:
                        System.out.println(String.format("Unknown 0xE000 Opcode: 0x%04X", opcode));
                        break;
                }
                break;

            case 0xF000:
                switch (eight_bits) {
                    case 0x07:
                        V[x] = delayTimer;
                        break;

                    case 0x0A:
                        boolean keyPressed = false;
                        for (int i = 0; i < 16; i++) {
                            if (keys[i]) {
                                V[x] = i;
                                keyPressed = true;
                                break;
                            }
                        }
                        if (!keyPressed) {
                            pc -= 2;
                        }
                        break;

                    case 0x15:
                        delayTimer = V[x];
                        break;

                    case 0x18:
                        soundTimer = V[x];
                        break;

                    case 0x1E:
                        I = (I + V[x]) & 0xFFFF;
                        break;

                    case 0x29:
                        I = 0x050 + ((V[x] & 0xF) * 5);
                        break;

                    case 0x33:
                        memory.write(I,     V[x] / 100);
                        memory.write(I + 1, (V[x] / 10) % 10);
                        memory.write(I + 2, V[x] % 10);
                        break;

                    case 0x55:
                        for (int i = 0; i <= x; i++) {
                            memory.write(I + i, V[i]);
                        }
                        break;

                    case 0x65:
                        for (int i = 0; i <= x; i++) {
                            V[i] = memory.read(I + i);
                        }
                        break;

                    default:
                        System.out.println(String.format("Unknown 0xF000 Opcode: 0x%04X", opcode));
                        break;
                }
                break;

			default:
				System.out.println(String.format("Unimplemented opcode: 0x%04X", opcode));
				break;
		}
	}

	public void updateTimers() {
		if (delayTimer > 0) {
			delayTimer--;
		}
		if (soundTimer > 0) {
			soundTimer--;
		}
	}

	private void drawSprite(int xRegister, int yRegister, int height) {
		int xPos = V[xRegister] % 64;
		int yPos = V[yRegister] % 32;

		V[0xF] = 0;

		for (int row = 0; row < height; row++) {
			int spriteByte = memory.read(I + row);

			for (int col = 0; col < 8; col++) {
				if ((spriteByte & (0x80 >> col)) != 0) {
					int targetX = (xPos + col) % 64;
					int targetY = (yPos + row) % 32;

					if (display[targetX][targetY]) {
						V[0xF] = 1;
					}

					display[targetX][targetY] ^= true;
				}
			}
		}

		drawFlag = true;
	}

	public boolean[][] getDisplay() {
		return display;
	}
	public boolean hasDrawnFlag() {
		return drawFlag;
	}
	public void clearDrawFlag() {
		drawFlag = false;
	}
	public void setKey(int keyIndex, boolean pressed) {
		if (keyIndex < 0 || keyIndex >= 16) {
			throw new IllegalArgumentException(
				"Invalid CHIP-8 key: " + keyIndex
			);
    }

    keys[keyIndex] = pressed;
}
	public int getRegister(int index) {
		return V[index] & 0xFF;
	}

	public int getI() {
		return I & 0xFFFF;
	}

	public int getPC() {
		return pc & 0xFFFF;
	}

	public int getSP () {
		return sp & 0xFF;
	}

	public int getDelayTimer() {
		return delayTimer & 0xFF;
	}

	public int getSoundTimer () {
		return soundTimer & 0xFF;
	}
}

