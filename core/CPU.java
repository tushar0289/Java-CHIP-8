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
                    case 0x0: V[x] = V[y]; break; // 8XY0: Set VX = VY
                    case 0x1: V[x] = (V[x] | V[y]) & 0xFF; break; // 8XY1: VX = VX | VY
                    case 0x2: V[x] = (V[x] & V[y]) & 0xFF; break; // 8XY2: VX = VX & VY
                    case 0x3: V[x] = (V[x] ^ V[y]) & 0xFF; break; // 8XY3: VX = VX ^ VY

                    case 0x4: // 8XY4: VX = VX + VY, set VF = carry
                        int sum = V[x] + V[y];
                        V[0xF] = (sum > 255) ? 1 : 0;
                        V[x] = sum & 0xFF;
                        break;

                    case 0x5: // 8XY5: VX = VX - VY, set VF = NOT borrow
                        V[0xF] = (V[x] >= V[y]) ? 1 : 0;
                        V[x] = (V[x] - V[y]) & 0xFF;
                        break;

                    case 0x6: // 8XY6: VX = VX >> 1, VF = dropped bit
                        V[0xF] = V[x] & 0x1;
                        V[x] = (V[x] >> 1) & 0xFF;
                        break;

                    case 0x7: // 8XY7: VX = VY - VX, set VF = NOT borrow
                        V[0xF] = (V[y] >= V[x]) ? 1 : 0;
                        V[x] = (V[y] - V[x]) & 0xFF;
                        break;

                    case 0x0E: // 8XYE: VX = VX << 1, VF = MSB bit
                        V[0xF] = (V[x] >> 7) & 0x1;
                        V[x] = (V[x] << 1) & 0xFF;
                        break;

                    default:
                        System.out.println(String.format("Unknown 0x8000 Opcode: 0x%04X", opcode));
                        break;
                }
                break;

            case 0x9000: // 9XY0: Skip next instruction if VX != VY
                if (V[x] != V[y]) pc += 2;
                break;

            case 0xA000: // ANNN: Set Index Register I = NNN
                I = twelve_bits;
                break;

            case 0xB000: // BNNN: Jump to address NNN + V0
                pc = twelve_bits + V[0];
                break;

            case 0xC000: // CXNN: Set VX = random byte & NN
                V[x] = (random.nextInt(256)) & eight_bits;
                break;

            case 0xD000: // DXYN: Draw sprite at (VX, VY) with width 8 and height N
                int xPos = V[x] % 64;
                int yPos = V[y] % 32;
                V[0xF] = 0; // Reset collision flag

                for (int row = 0; row < four_bits; row++) {
                    int spriteByte = memory.read(I + row);

                    for (int col = 0; col < 8; col++) {
                        // Extract bit from sprite byte (left to right)
                        if ((spriteByte & (0x80 >> col)) != 0) {
                            int targetX = (xPos + col) % 64;
                            int targetY = (yPos + row) % 32;

                            // If pixel is already ON, collision detected -> turn OFF
                            if (display[targetX][targetY]) {
                                V[0xF] = 1;
                            }
                            display[targetX][targetY] ^= true; // XOR toggle
                        }
                    }
                }
                drawFlag = true;
                break;

            case 0xE000:
                switch (eight_bits) {
                    case 0x9E: // EX9E: Skip next instruction if key in VX is pressed
                        if (keys[V[x] & 0xF]) pc += 2;
                        break;

                    case 0xA1: // EXA1: Skip next instruction if key in VX is NOT pressed
                        if (!keys[V[x] & 0xF]) pc += 2;
                        break;

                    default:
                        System.out.println(String.format("Unknown 0xE000 Opcode: 0x%04X", opcode));
                        break;
                }
                break;

            case 0xF000:
                switch (eight_bits) {
                    case 0x07: // FX07: Set VX = Delay Timer value
                        V[x] = delayTimer;
                        break;

                    case 0x0A: // FX0A: Wait for key press, store key index in VX
                        boolean keyPressed = false;
                        for (int i = 0; i < 16; i++) {
                            if (keys[i]) {
                                V[x] = i;
                                keyPressed = true;
                                break;
                            }
                        }
                        if (!keyPressed) {
                            pc -= 2; // Rewind PC so execution blocks until a key is pressed
                        }
                        break;

                    case 0x15: // FX15: Set Delay Timer = VX
                        delayTimer = V[x];
                        break;

                    case 0x18: // FX18: Set Sound Timer = VX
                        soundTimer = V[x];
                        break;

                    case 0x1E: // FX1E: Set I = I + VX
                        I = (I + V[x]) & 0xFFFF;
                        break;

                    case 0x29: // FX29: Set I = memory location of font character sprite in VX
                        I = 0x050 + ((V[x] & 0xF) * 5);
                        break;

                    case 0x33: // FX33: Store Binary-Coded Decimal (BCD) representation of VX at I, I+1, I+2
                        memory.write(I,     V[x] / 100);
                        memory.write(I + 1, (V[x] / 10) % 10);
                        memory.write(I + 2, V[x] % 10);
                        break;

                    case 0x55: // FX55: Dump registers V0 through VX into memory starting at I
                        for (int i = 0; i <= x; i++) {
                            memory.write(I + i, V[i]);
                        }
                        break;

                    case 0x65: // FX65: Load registers V0 through VX from memory starting at I
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
		keys[keyIndex & 0xF] = pressed;
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

