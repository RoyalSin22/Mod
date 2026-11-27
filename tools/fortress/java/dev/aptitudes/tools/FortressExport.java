package dev.aptitudes.tools;

import dev.aptitudes.dungeon.FortressLayout;
import dev.aptitudes.dungeon.FortressPalette;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/** Exporta el plano puro a Sponge Schematic v3, sin iniciar Minecraft ni añadir una dependencia al mod. */
public final class FortressExport {
	private static final int DATA_VERSION_1_21_1 = 3955;
	private static final int WIDTH = FortressLayout.SIZE;
	private static final int HEIGHT = FortressLayout.TOP_Y - FortressLayout.BOTTOM_Y + 1;
	private static final int LENGTH = FortressLayout.SIZE;

	private FortressExport() {}

	public static void main(String[] args) throws IOException {
		long start = System.nanoTime();
		Path output = Path.of(args.length == 0 ? "build" : args[0]);
		int ring = args.length > 1 ? Integer.parseInt(args[1]) : 3;
		String biome = args.length > 2 ? args[2] : "minecraft:plains";
		Files.createDirectories(output);
		Map<String, Integer> palette = new LinkedHashMap<>();
		ByteArrayOutputStream blockData = new ByteArrayOutputStream(WIDTH * HEIGHT * LENGTH);
		byte[] voxels = new byte[WIDTH * HEIGHT * LENGTH];
		boolean[] occupied = new boolean[WIDTH * LENGTH];
		int[] chunkWrites = new int[64];
		int written = 0, columns = 0, cursor = 0;
		for (int z = FortressLayout.MIN; z <= FortressLayout.MAX; z++) {
			for (int x = FortressLayout.MIN; x <= FortressLayout.MAX; x++) {
				boolean included = FortressLayout.occupied(x, z);
				occupied[(z - FortressLayout.MIN) * WIDTH + x - FortressLayout.MIN] = included;
				if (included) columns++;
			}
		}
		for (int y = FortressLayout.BOTTOM_Y; y <= FortressLayout.TOP_Y; y++) {
			for (int z = FortressLayout.MIN; z <= FortressLayout.MAX; z++) {
				for (int x = FortressLayout.MIN; x <= FortressLayout.MAX; x++) {
					var cell = occupied[(z - FortressLayout.MIN) * WIDTH + x - FortressLayout.MIN]
							? FortressLayout.cell(x, y, z) : null;
					if (cell != null) {
						written++;
						chunkWrites[((z - FortressLayout.MIN) / 16) * 8 + (x - FortressLayout.MIN) / 16]++;
					}
					String state = FortressPalette.state(cell, x, y, z, ring, biome);
					int index = palette.computeIfAbsent(state, ignored -> palette.size());
					if (index > 255) throw new IllegalStateException("La paleta excede 256 estados");
					writeVarInt(blockData, index);
					voxels[cursor++] = (byte) index;
				}
			}
		}
		try (OutputStream file = Files.newOutputStream(output.resolve("fortress.schem"));
				DataOutputStream nbt = new DataOutputStream(new GZIPOutputStream(file))) {
			writeCompound(nbt, "");
			writeCompound(nbt, "Schematic");
			writeInt(nbt, "Version", 3);
			writeInt(nbt, "DataVersion", DATA_VERSION_1_21_1);
			writeShort(nbt, "Width", WIDTH);
			writeShort(nbt, "Height", HEIGHT);
			writeShort(nbt, "Length", LENGTH);
			writeIntArray(nbt, "Offset", new int[] {FortressLayout.MIN, FortressLayout.BOTTOM_Y, FortressLayout.MIN});
			writeCompound(nbt, "Blocks");
			writeCompound(nbt, "Palette");
			for (var entry : palette.entrySet()) writeInt(nbt, entry.getKey(), entry.getValue());
			end(nbt);
			writeByteArray(nbt, "Data", blockData.toByteArray());
			nbt.writeByte(9); nbt.writeUTF("BlockEntities"); nbt.writeByte(10); nbt.writeInt(0);
			end(nbt); end(nbt); end(nbt);
		}
		Files.write(output.resolve("fortress.voxels"), voxels);
		StringBuilder text = new StringBuilder("# y,z,x; ring=" + ring + "; biome=" + biome + "\n");
		for (var entry : palette.entrySet()) text.append(entry.getValue()).append('\t').append(entry.getKey()).append('\n');
		Files.writeString(output.resolve("fortress.palette"), text, StandardCharsets.UTF_8);
		int busiest = 0;
		for (int count : chunkWrites) busiest = Math.max(busiest, count);
		System.out.println("Fortaleza V91: " + columns + " columnas, " + written + " celdas, " + palette.size()
				+ " estados; máximo " + busiest + " celdas/chunk; " + WIDTH + "x" + HEIGHT + "x" + LENGTH);
		System.out.println("Exportación pura: " + ((System.nanoTime() - start) / 1_000_000) + " ms (no mide generación del mundo)");
		System.out.println(output.resolve("fortress.schem").toAbsolutePath());
	}

	private static void writeVarInt(ByteArrayOutputStream out, int value) {
		while ((value & ~0x7f) != 0) { out.write((value & 0x7f) | 0x80); value >>>= 7; }
		out.write(value);
	}

	private static void writeCompound(DataOutputStream out, String name) throws IOException { out.writeByte(10); out.writeUTF(name); }
	private static void writeInt(DataOutputStream out, String name, int value) throws IOException { out.writeByte(3); out.writeUTF(name); out.writeInt(value); }
	private static void writeShort(DataOutputStream out, String name, int value) throws IOException { out.writeByte(2); out.writeUTF(name); out.writeShort(value); }
	private static void writeIntArray(DataOutputStream out, String name, int[] values) throws IOException {
		out.writeByte(11); out.writeUTF(name); out.writeInt(values.length); for (int value : values) out.writeInt(value);
	}
	private static void writeByteArray(DataOutputStream out, String name, byte[] values) throws IOException {
		out.writeByte(7); out.writeUTF(name); out.writeInt(values.length); out.write(values);
	}
	private static void end(DataOutputStream out) throws IOException { out.writeByte(0); }
}
