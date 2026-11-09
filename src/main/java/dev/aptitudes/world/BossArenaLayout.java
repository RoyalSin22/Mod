package dev.aptitudes.world;

import java.util.ArrayList;
import java.util.List;

/** Geometría fija de las salas, relativa al centro del suelo transitable. */
public enum BossArenaLayout {
	TOWER(3, 17, 0), SHRINE(4, 19, 0), CAMP(5, 30, -1.0 / 16),
	/** v65: Ruinas de batalla del Coloso Sin Rostro, círculo de 21 bloques; se levanta al invocarlo. */
	RUINS(10, 100, 0);

	private static final int HEIGHT = 3;
	private final int radius;
	private final int radiusSquared;
	private final double floorY;
	private final List<Cell> wall;

	public record Cell(int x, int y, int z) {}

	BossArenaLayout(int radius, int radiusSquared, double floorY) {
		this.radius = radius;
		this.radiusSquared = radiusSquared;
		this.floorY = floorY;
		List<Cell> cells = new ArrayList<>();
		for (int y = 0; y < HEIGHT; y++) {
			for (int x = -radius; x <= radius; x++) {
				for (int z = -radius; z <= radius; z++) {
					if (footprint(x, z) && edge(x, z)) cells.add(new Cell(x, y, z));
				}
			}
			if (radius == 3) cells.add(new Cell(2, y, 2)); // Salida de la escalera de la azotea.
		}
		wall = List.copyOf(cells);
	}

	public List<Cell> wall() {
		return wall;
	}

	public boolean contains(double x, double y, double z) {
		if (!(x >= -radius && x < radius + 1 && z >= -radius && z < radius + 1 && y >= floorY && y < HEIGHT)) return false;
		int cellX = (int) Math.floor(x);
		int cellZ = (int) Math.floor(z);
		return footprint(cellX, cellZ) && !edge(cellX, cellZ);
	}

	private boolean footprint(int x, int z) {
		return Math.abs(x) <= radius && Math.abs(z) <= radius && x * x + z * z <= radiusSquared;
	}

	private boolean edge(int x, int z) {
		return !footprint(x - 1, z) || !footprint(x + 1, z) || !footprint(x, z - 1) || !footprint(x, z + 1);
	}
}
