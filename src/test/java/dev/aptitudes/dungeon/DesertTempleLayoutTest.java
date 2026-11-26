package dev.aptitudes.dungeon;

import org.junit.jupiter.api.Test;

import java.util.ArrayDeque;

import static dev.aptitudes.dungeon.DesertTempleLayout.*;
import static org.junit.jupiter.api.Assertions.*;

class DesertTempleLayoutTest {
	@Test void boundsAndDepthFitTheApprovedScale() {
		assertEquals(112, SIZE);
		assertTrue(SIZE >= 80 && SIZE <= 128);
		assertEquals(-43, BOTTOM_Y);
		assertEquals(8, TOP_Y);
		assertNull(cell(-49,-10,0,true));
		assertNull(cell(64,-10,0,true));
	}

	@Test void keepsTheRequiredProgressionMarkers() {
		assertEquals(Cell.CONTROLLER, cell(CONTROLLER[0],CONTROLLER[1],CONTROLLER[2],true));
		assertEquals(Cell.DOOR, cell(0,-33,13,true));
		assertEquals(Cell.RELIQUARY, cell(RELIQUARY[0],RELIQUARY[1],RELIQUARY[2],true));
		assertEquals(Cell.CRYPT_HATCH, cell(7,-34,7,true));
		assertEquals(Cell.LADDER, cell(7,-39,7,true));
		assertEquals(Cell.GATE, cell(-14,-9,37,true));
		assertEquals(Cell.GATE, cell(-34,-21,-20,true));
		assertEquals(Cell.EXIT_GATE, cell(0,-33,-17,true));
		assertEquals(Cell.EXIT_GATE, cell(0,0,-39,true));
		assertEquals(Cell.AIR, cell(0,5,55,true)); // Las dunas sobre la escalera se excavan hasta el cielo.
		assertEquals(Cell.AIR, cell(0,5,-42,true));
	}

	@Test void noFallingSandAndCryptIsOptional() {
		for(int x=MIN;x<=MAX;x++) for(int z=MIN;z<=MAX;z++) {
			for(int y=BOTTOM_Y;y<=TOP_Y;y++) {
				Cell c=cell(x,y,z,false);
				assertNotEquals(Cell.CRYPT_HATCH,c);
			}
		}
		assertNull(cell(0,-42,-6,false));
		assertEquals(Cell.FLOOR,cell(0,-42,-6,true));
	}

	@Test void encounterSpawnsHaveHeadroomAndHiddenChestsAreBehindMasonry() {
		for(int[] p:new int[][]{AUTOMATON,CUSTODIAN,CATACOMBS,CHAPEL}) {
			assertEquals(Cell.AIR,cell(p[0],p[1],p[2],true));
			assertEquals(Cell.AIR,cell(p[0],p[1]+1,p[2],true));
		}
		for(int[] p:TRIAL_SPAWNS) assertEquals(Cell.AIR,cell(p[0],p[1],p[2],true));
		assertEquals(Cell.CHEST_HIDDEN,cell(-47,-9,-17,true));
		assertEquals(Cell.RARE_WALL,cell(-46,-9,-17,true));
		assertEquals(Cell.CHEST_HIDDEN,cell(15,-9,-16,true));
		assertEquals(Cell.CHEST_HIDDEN,cell(-16,-21,-47,true));
	}

	@Test void metalChambersAreByTheCustodianAndCarryDistinctLoot() {
		assertEquals(Cell.CHEST_COPPER,cell(-26,-33,0,true));
		assertEquals(Cell.CHEST_IRON,cell(26,-33,0,true));
		assertEquals(Cell.CHEST_GOLD,cell(26,-33,-26,true));
		assertEquals(Cell.CHEST_DIAMOND,cell(-26,-33,26,true));
		assertEquals(36,chamberDoors().size());
	}

	@Test void spiralRouteRequiresTheTwoEncounterGatesAndThenReachesTheBossDoor() {
		int[] start={0,-9,40}, finish={0,-33,15};
		assertFalse(reachable(start,finish,false,false));
		assertFalse(reachable(start,finish,true,false));
		assertTrue(reachable(start,finish,true,true));
	}

	private static boolean reachable(int[] start,int[] finish,boolean gateOneOpen,boolean gateTwoOpen) {
		boolean[][][] visited=new boolean[SIZE][52][SIZE];
		ArrayDeque<int[]> queue=new ArrayDeque<>(); queue.add(start);
		while(!queue.isEmpty()) {
			int[] p=queue.removeFirst(); int x=p[0],y=p[1],z=p[2];
			if(x==finish[0]&&y==finish[1]&&z==finish[2]) return true;
			if(x<MIN||x>MAX||z<MIN||z>MAX||y<BOTTOM_Y+1||y>8) continue;
			int ix=x-MIN,iy=y-BOTTOM_Y,iz=z-MIN;
			if(visited[ix][iy][iz]||!passable(x,y,z,gateOneOpen,gateTwoOpen)) continue;
			visited[ix][iy][iz]=true;
			for(int[] d:new int[][]{{1,0},{-1,0},{0,1},{0,-1}})
				for(int dy=-1;dy<=1;dy++) queue.add(new int[]{x+d[0],y+dy,z+d[1]});
		}
		return false;
	}

	private static boolean passable(int x,int y,int z,boolean gateOneOpen,boolean gateTwoOpen) {
		Cell feet=cell(x,y,z,true), head=cell(x,y+1,z,true), below=cell(x,y-1,z,true);
		return open(feet,x,y,z,gateOneOpen,gateTwoOpen) && open(head,x,y+1,z,gateOneOpen,gateTwoOpen)
				&& below!=null && below!=Cell.AIR && below!=Cell.WATER && below!=Cell.GATE && below!=Cell.EXIT_GATE;
	}
	private static boolean open(Cell c,int x,int y,int z,boolean gateOneOpen,boolean gateTwoOpen) {
		return c==Cell.AIR || c==Cell.LADDER || c==Cell.DOOR
				|| c==Cell.GATE && ((x==-14 && gateOneOpen) || (z==-20 && gateTwoOpen));
	}
}
