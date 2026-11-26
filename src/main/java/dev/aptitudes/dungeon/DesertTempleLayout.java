package dev.aptitudes.dungeon;

import java.util.List;

/** V89: plano puro por columna. Las coordenadas son relativas al origen superficial de la región. */
public final class DesertTempleLayout {
	public static final int MIN = -48, MAX = 63, SIZE = 112;
	public static final int BOTTOM_Y = -43, TOP_Y = 8, MAX_SLOPE = 4;
	public static final int[] CONTROLLER = {0, -25, 20};
	public static final int[] AUTOMATON = {0, -9, 40};
	public static final int[] TRIGGER = {0, -9, 43};
	public static final int[] CUSTODIAN = {0, -33, 0};
	public static final int[] RELIQUARY = {0, -33, -7};
	public static final int[] CRYPT_CHEST = {0, -41, -6};
	public static final int[] TRIAL_MIN = {21, -21, 11}, TRIAL_MAX = {39, -16, 27};
	public static final List<int[]> TRIAL_SPAWNS = List.of(new int[]{24,-21,15}, new int[]{36,-21,15}, new int[]{24,-21,25}, new int[]{36,-21,25});
	public static final int[] CRYPT_MIN = {-11,-41,-8}, CRYPT_MAX = {11,-36,9}, CRYPT_CLIMAX = {0,-41,0};
	public static final List<int[]> CRYPT_SPAWNS = List.of(new int[]{-7,-41,-5}, new int[]{7,-41,-5}, new int[]{-7,-41,6}, new int[]{7,-41,6});
	public static final int[] CATACOMBS = {-42,-9,2}, CHAPEL = {-6,-9,-39};

	public enum Cell { AIR, WALL, FLOOR, ROOF, RARE_WALL, RUBBLE, WATER, BRIDGE, LIGHT,
		GATE, EXIT_GATE, DOOR, CONTROLLER, RELIQUARY, CRYPT_HATCH, LADDER,
		CHEST_SUPPLIES, CHEST_HIDDEN, CHEST_COPPER, CHEST_IRON, CHEST_GOLD, CHEST_DIAMOND,
		CHAMBER_COPPER, CHAMBER_IRON, CHAMBER_GOLD, CHAMBER_DIAMOND,
		BOOKSHELF, FORGE, BED, MIRROR, STATUE, ALTAR, OBELISK }

	private record Room(int id, int level, int x0, int x1, int z0, int z1) {
		boolean contains(int x, int z) { return x >= x0 && x <= x1 && z >= z0 && z <= z1; }
	}
	// 0 atrio, 1 barracones, 2 catacumbas, 3 capilla, 4 espejos, 5 taller,
	// 6 pruebas, 7 sombras, 8 forja, 9 santuario, 10–13 cámaras de metal, 14 cripta.
	private static final List<Room> ROOMS = List.of(
		new Room(0,1,-14,14,31,52), new Room(1,1,-48,-26,10,29),
		new Room(2,1,-48,-32,-18,6), new Room(3,1,-17,5,-48,-32),
		new Room(4,1,14,35,-17,7), new Room(5,1,18,40,-5,18),
		new Room(6,2,20,40,10,28), new Room(7,2,-17,8,-48,-32),
		new Room(8,2,-48,-29,-14,8), new Room(9,3,-14,14,-13,13),
		new Room(10,3,-34,-18,-10,6), new Room(11,3,18,34,-10,6),
		new Room(12,3,18,34,-34,-18), new Room(13,3,-34,-18,18,34),
		new Room(14,4,-12,12,-10,11));
	private static final boolean[][] OCCUPIED = occupiedColumns();
	private static boolean[][] occupiedColumns() {
		boolean[][] out=new boolean[SIZE][SIZE];
		for(int x=MIN;x<=MAX;x++) for(int z=MIN;z<=MAX;z++)
			for(int y=BOTTOM_Y;y<=TOP_Y;y++) if(cell(x,y,z,true)!=null) { out[x-MIN][z-MIN]=true; break; }
		return out;
	}

	private DesertTempleLayout() {}
	public static boolean inside(int x, int z) { return x >= MIN && x <= MAX && z >= MIN && z <= MAX; }
	public static boolean occupied(int x, int z) {
		return inside(x,z) && OCCUPIED[x-MIN][z-MIN];
	}
	public static Cell cell(int x, int y, int z, boolean crypt) {
		if (!inside(x,z) || y < BOTTOM_Y || y > TOP_Y) return null;
		if (x == CONTROLLER[0] && y == CONTROLLER[1] && z == CONTROLLER[2]) return Cell.CONTROLLER;
		if (x == RELIQUARY[0] && y == RELIQUARY[1] && z == RELIQUARY[2]) return Cell.RELIQUARY;
		if (crypt && x >= 7 && x <= 8 && z >= 7 && z <= 8 && y == -34) return Cell.CRYPT_HATCH;
		if (lightWell(x,z) && y >= -9 && y <= 8) return lightWellCell(x,y,z);
		if (crypt && x >= 7 && x <= 8 && z >= 7 && z <= 8 && y >= -41 && y <= -35)
			return x == 7 && z == 7 ? Cell.LADDER : Cell.AIR;
		if (x >= -36 && x <= -32 && z == -20 && y >= -21 && y <= -19) return Cell.GATE;
		if (exit(x,z)) return exitCell(x,y,z);
		if (entrance(x,z)) return entranceCell(x,y,z);
		if (cistern(x,z) && y >= -14 && y <= -11)
			return Math.abs(x)==15 || Math.abs(z)==16 ? Cell.WALL : Cell.AIR;
		if (lookout(x,z) && y >= -27 && y <= -21) return y == -21 ? Cell.MIRROR : Cell.AIR;
		if (crypt && y <= -35) {
			for (Room r : ROOMS) if (r.level == 4 && r.contains(x,z)) return roomCell(r,x,y,z);
			if (x >= 7 && x <= 8 && z >= 7 && z <= 8 && y >= -41) return x == 7 && z == 7 ? Cell.LADDER : Cell.AIR;
		}
		if (rampOne(x,z)) return ramp(x,y,z,-10,-22,-37,-22);
		if (rampTwo(x,z)) return ramp(x,y,z,-22,-34,-20,-35);
		int level = y >= -10 && y <= -3 ? 1 : y >= -22 && y <= -15 ? 2 : y >= -34 && y <= -27 ? 3 : 0;
		if (level == 0) return surface(x,y,z);
		for (Room r : ROOMS) if (r.level == level && r.contains(x,z)) return roomCell(r,x,y,z);
		if (level <= 2 && cistern(x,z)) return cisternCell(x,y,z,level);
		if (corridor(level,x,z)) return corridorCell(level,x,y,z);
		return surface(x,y,z);
	}

	private static boolean rect(int x,int z,int x0,int x1,int z0,int z1) { return x>=x0&&x<=x1&&z>=z0&&z<=z1; }
	private static boolean cistern(int x,int z) { return rect(x,z,-15,15,-16,16); }
	private static boolean lookout(int x,int z) { return (x>=8&&x<=10 || x>=-10&&x<=-8) && z>=-1&&z<=1; }
	private static boolean entrance(int x,int z) { return rect(x,z,-4,4,50,63); }
	private static boolean lightWell(int x,int z) { return rect(x,z,22,26,-3,1) || rect(x,z,-5,-1,-44,-40); }
	private static Cell lightWellCell(int x,int y,int z) {
		boolean rim = x==22 || x==26 || z==-3 || z==1 || x==-5 || x==-1 || z==-44 || z==-40;
		return rim && y<=1 ? Cell.WALL : y==0 ? Cell.MIRROR : Cell.AIR;
	}
	private static boolean exit(int x,int z) { return rect(x,z,-2,2,-43,-13); }
	private static boolean rampOne(int x,int z) { return rect(x,z,32,36,-37,-22); }
	private static boolean rampTwo(int x,int z) { return rect(x,z,-36,-32,-35,-20); }
	private static Cell ramp(int x,int y,int z,int from,int to,int startZ,int endZ) {
		int step = Math.abs(z-startZ); int floor = from + Integer.signum(to-from)*Math.min(Math.abs(to-from),step);
		return y == floor ? Cell.FLOOR : y > floor && y <= floor+5 ? Cell.AIR : null;
	}
	private static Cell entranceCell(int x,int y,int z) {
		int floor = -Math.min(10, Math.max(0,62-z));
		if (y == floor) return Cell.FLOOR;
		if (y > floor && y <= TOP_Y) {
			if (z == 58 && Math.abs(x) == 4 && y <= 4) return Cell.WALL;
			if (z == 58 && y == 5 && Math.abs(x) <= 4 && Math.abs(x) != 2) return Cell.RUBBLE;
			return Cell.AIR;
		}
		return null;
	}
	private static Cell exitCell(int x,int y,int z) {
		if (z <= -40 && y == -1) return Cell.FLOOR;
		if (z <= -40 && y >= 0 && y <= TOP_Y) return Cell.AIR;
		if (z == -39 && y >= 0 && y <= 1) return Math.abs(x) <= 1 ? Cell.EXIT_GATE : Cell.WALL;
		if (z >= -18) {
			if (y == -34) return Cell.FLOOR;
			if (y >= -33 && y <= -29) return z == -17 && Math.abs(x) <= 1 && y <= -31 ? Cell.EXIT_GATE : Cell.AIR;
			return null;
		}
		if (z >= -39 && z <= -37) {
			if (y == -34) return Cell.FLOOR;
			if (y >= -33 && y <= 1) return (x == 0 && z == -38) ? Cell.LADDER : (x == 0 && z == -37) ? Cell.WALL : Cell.AIR;
			if (y == 2) return Cell.ROOF;
		}
		if (y == -34) return Cell.FLOOR;
		if (y >= -33 && y <= -29) return Cell.AIR;
		if (y == 0 && z == -39) return Cell.EXIT_GATE;
		return null;
	}
	private static Cell surface(int x,int y,int z) {
		if ((x==7 && z==57 || x==-8 && z==55) && y >= -4 && y <= 7) return Cell.OBELISK;
		if ((Math.abs(x)==6 && z>=51 && z<=62 || Math.abs(x)<=6 && (z==51 || z==62)) && y>=-4 && y<=2)
			return ((x+z)&7)==0 ? Cell.RUBBLE : Cell.WALL;
		return null;
	}
	private static boolean corridor(int level,int x,int z) {
		return switch(level) {
			case 1 -> rect(x,z,-40,-14,35,39) || rect(x,z,-40,-36,-35,39) || rect(x,z,-40,35,-37,-33)
				|| rect(x,z,11,21,-2,2) || rect(x,z,10,21,8,12);
			case 2 -> rect(x,z,15,42,-1,1) || rect(x,z,35,45,-24,-20) || rect(x,z,42,46,-34,34) || rect(x,z,38,45,18,22)
				|| rect(x,z,-36,45,30,34) || rect(x,z,-36,-32,-35,34);
			case 3 -> rect(x,z,-36,42,-37,-33) || rect(x,z,39,43,-35,30) || rect(x,z,-2,43,26,30)
				|| rect(x,z,-2,2,13,30) || rect(x,z,-18,-13,-1,1) || rect(x,z,13,18,-1,1)
				|| rect(x,z,32,42,-27,-23) || rect(x,z,-20,-13,24,28);
			default -> false;
		};
	}
	private static Cell corridorCell(int level,int x,int y,int z) {
		int floor = level==1?-10:level==2?-22:-34;
		if (y == floor) return Cell.FLOOR;
		if (y > floor && y <= floor+5) {
			if (level==2 && z==-20 && x>=-36 && x<=-32 && y<=-19) return Cell.GATE;
			return Cell.AIR;
		}
		if (y == floor+6) return ((x+z)&15)==0 ? Cell.AIR : Cell.ROOF;
		return null;
	}
	private static Cell cisternCell(int x,int y,int z,int level) {
		int floor = level==1?-10:-22;
		boolean edge = Math.abs(x)>=13 || Math.abs(z)>=14;
		boolean bridge = Math.abs(x)<=1 || Math.abs(z)<=1;
		if (level==1) {
			if (y==floor) return edge || bridge ? Cell.BRIDGE : Cell.MIRROR;
			if (y>floor && y<=-4) return edge && (Math.abs(x)==15 || Math.abs(z)==16) ? Cell.WALL : Cell.AIR;
		} else {
			if (y==floor) return Cell.FLOOR;
			if (y==floor+1) return bridge ? Cell.BRIDGE : Cell.WATER;
			if (y>floor+1 && y<=-16) return x==15 && Math.abs(z)<=1 ? Cell.AIR
					: edge && (Math.abs(x)==15 || Math.abs(z)==16) ? Cell.WALL : Cell.AIR;
		}
		return null;
	}
	private static Cell roomCell(Room r,int x,int y,int z) {
		int floor = r.level==1?-10:r.level==2?-22:r.level==3?-34:-42;
		if (y<floor || y>floor+7) return null;
		if (y==floor) return Cell.FLOOR;
		if ((r.id==2 || r.id==4 || r.id==7) && y<=floor+3
				&& (x==r.x0+2 && z==r.z0+1 || x==r.x0+1 && z==r.z0+2)) return Cell.RARE_WALL;
		boolean wall = x==r.x0 || x==r.x1 || z==r.z0 || z==r.z1;
		if (wall && y<=floor+4) {
			if (doorway(r.id,x,z) && y<=floor+3) {
				if (r.id==0 && x==-14) return Cell.GATE;
				if (r.id==9 && z==13) return Cell.DOOR;
				if (r.id>=10 && r.id<=13) return switch(r.id) {
					case 10 -> Cell.CHAMBER_COPPER; case 11 -> Cell.CHAMBER_IRON;
					case 12 -> Cell.CHAMBER_GOLD; default -> Cell.CHAMBER_DIAMOND; };
				return Cell.AIR;
			}
			return ((x*7+z*11+y)&31)==0 ? Cell.RARE_WALL : Cell.WALL;
		}
		if (y==floor+7) return ((x*7+z*11)&23)==0 ? Cell.AIR : Cell.ROOF;
		if (y==floor+1) {
			Cell furnishing = furnish(r.id,x,z);
			if (furnishing!=null) return furnishing;
		}
		if (y==floor+5 && (x==r.x0+2 && z==r.z0+2)) return Cell.LIGHT;
		return Cell.AIR;
	}
	private static boolean doorway(int id,int x,int z) {
		return switch(id) {
			case 0 -> x==-14 && z>=36 && z<=38 || z==52 && Math.abs(x)<=2;
			case 1 -> (z==10 || z==29) && x>=-40 && x<=-36;
			case 2 -> (z==-18 || z==6) && x>=-40 && x<=-36;
			case 3 -> (x==-17 || x==5) && z>=-36 && z<=-33;
			case 4 -> x==14 && Math.abs(z)<=2;
			case 5 -> x==18 && z>=8 && z<=12;
			case 6 -> x==40 && z>=18 && z<=20 || z==28 && x>=28 && x<=30;
			case 7 -> z==-32 && Math.abs(x)<=2;
			case 8 -> (z==-14 || z==8) && x>=-36 && x<=-32;
			case 9 -> z==13 && Math.abs(x)<=2 || z==-13 && Math.abs(x)<=1
				|| x==-14 && Math.abs(z)<=1 || x==14 && Math.abs(z)<=1;
			case 10 -> x==-18 && Math.abs(z)<=1;
			case 11 -> x==18 && Math.abs(z)<=1;
			case 12 -> x==34 && z>=-27 && z<=-25;
			case 13 -> x==-18 && z>=24 && z<=26;
			case 14 -> x>=7 && x<=8 && z>=7 && z<=8;
			default -> false;
		};
	}
	private static Cell furnish(int id,int x,int z) {
		return switch(id) {
			case 0 -> (x==0&&z==40) ? Cell.AIR : (Math.abs(x)==9&&z==40) ? Cell.STATUE : null;
			case 1 -> x==-44 && (z==13||z==17||z==21||z==25) ? Cell.BED : x==-29&&z==25 ? Cell.CHEST_SUPPLIES : null;
			case 2 -> x==-42&&z==0 ? Cell.STATUE : x==-47&&z==-17 ? Cell.CHEST_HIDDEN : null;
			case 3 -> x==-6&&z==-41 ? Cell.ALTAR : x==2&&z==-44 ? Cell.CHEST_SUPPLIES : null;
			case 4 -> (x==18||x==31)&&z%4==0 ? Cell.MIRROR : x==15&&z==-16 ? Cell.CHEST_HIDDEN : null;
			case 5 -> x==35&&z==11 ? Cell.FORGE : x==36&&z==-1 ? Cell.CHEST_SUPPLIES : null;
			case 6 -> x==24&&z==24 ? Cell.STATUE : null;
			case 7 -> x==-16&&z==-47 ? Cell.CHEST_HIDDEN : null;
			case 8 -> x==-44&&z==2 ? Cell.FORGE : x==-44&&z==-10 ? Cell.CHEST_SUPPLIES : null;
			case 9 -> x==-10&&z==0 ? Cell.ALTAR : (Math.abs(x)==10&&Math.abs(z)==9) ? Cell.STATUE : null;
			case 10 -> x==-26&&z==0 ? Cell.CHEST_COPPER : null;
			case 11 -> x==26&&z==0 ? Cell.CHEST_IRON : null;
			case 12 -> x==26&&z==-26 ? Cell.CHEST_GOLD : null;
			case 13 -> x==-26&&z==26 ? Cell.CHEST_DIAMOND : null;
			case 14 -> null;
			default -> null;
		};
	}

	public static List<int[]> gateOne() { return points(-14,-14,-9,-7,36,38); }
	public static List<int[]> gateTwo() { return points(-36,-32,-21,-19,-20,-20); }
	public static List<int[]> exitGates() {
		var out = new java.util.ArrayList<int[]>(); out.addAll(points(-1,1,-33,-31,-17,-17)); out.addAll(points(-1,1,0,1,-39,-39)); return out;
	}
	public static List<int[]> custodianDoor() { return points(-2,2,-33,-31,13,13); }
	public static List<int[]> cryptHatch() { return points(7,8,-34,-34,7,8); }
	private static List<int[]> points(int x0,int x1,int y0,int y1,int z0,int z1) {
		var out=new java.util.ArrayList<int[]>();
		for(int x=x0;x<=x1;x++)
			for(int y=y0;y<=y1;y++) for(int z=z0;z<=z1;z++) out.add(new int[]{x,y,z});
		return out;
	}
	public static List<int[]> chamberDoors() {
		var out=new java.util.ArrayList<int[]>();
		for(int y=-33;y<=-31;y++) {
			for(int z=-1;z<=1;z++) { out.add(new int[]{-18,y,z,0}); out.add(new int[]{18,y,z,1}); }
			for(int z=-27;z<=-25;z++) out.add(new int[]{34,y,z,2});
			for(int z=24;z<=26;z++) out.add(new int[]{-18,y,z,3});
		}
		return out;
	}
}
