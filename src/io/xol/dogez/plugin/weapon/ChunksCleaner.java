package io.xol.dogez.plugin.weapon;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.xol.chunkstories.api.world.World;
import io.xol.chunkstories.api.world.chunk.Chunk;

//(c) 2014 XolioWare Interactive

public class ChunksCleaner {

	// This class takes care of
	public static Map<Integer, List<int[]>> chunksData = new HashMap<Integer, List<int[]>>();

	public static void cleanAllChunks(World w) {
		/*for (Chunk c : w.getAllLoadedChunks()) {
			cleanChunk(c);
		}*/
	}

	@SuppressWarnings("deprecation")
	public static void cleanChunk(Chunk c) {
		/*int x = c.getX();
		int z = c.getZ();
		// System.out.println("Cleaning chunk "+x+":"+z+" cid"+cid);
		if (checkCoords(x, z)) {
			x += 128;
			z += 128;
			int cid = x * 256 + z;
			
			if (chunksData.containsKey(cid)) {
				// System.out.println("Cleaning chunk "+x+":"+z+" "+cid);
				List<int[]> toclean = chunksData.get(cid);
				for (int[] cleanme : toclean) {
					int bx = cleanme[0];
					int by = cleanme[1];
					int bz = cleanme[2];

					int tid = cleanme[3];
					int meta = cleanme[4];
					c.getBlock(clean(bx), by % 255, clean(bz)).setTypeId(tid);
					c.getBlock(clean(bx), by % 255, clean(bz)).setData((byte) meta);
					// System.out.println("brought back block
					// "+bx+":"+by+":"+bz+" to "+tid+":"+meta);
				}
				chunksData.get(cid).clear();
			}
		}*/
	}

	static int clean(int i) {
		i = i % 16;
		if (i < 0)
			i += 16;
		return i;
	}

	@SuppressWarnings("deprecation")
	public static void breakGlass(World w, int bx, int by, int bz) {
		/*Chunk c = w.getChunkAt(new Location(w, bx, by, bz));
		int cx = c.getX();
		int cz = c.getZ();
		if (checkCoords(cx, cz)) {
			if (!checkSides(w, bx, by, bz))
				return;
			cx += 128;
			cz += 128;
			int cid = cx * 256 + cz;
			// int cid = cx*640+cz;
			if (!chunksData.containsKey(cid)) {
				// System.out.println("Made "+cid);
				chunksData.put(cid, new ArrayList<int[]>());
			}
			int[] record = { bx, by, bz, w.getBlockAt(bx, by, bz).getTypeId(), w.getBlockAt(bx, by, bz).getData() };
			chunksData.get(cid).add(record);
			w.getBlockAt(bx, by, bz).setTypeId(0);
			w.getBlockAt(bx, by, bz).setData((byte) 0);
			// System.out.println("destroyed but added record for
			// "+bx+":"+by+":"+bz+" "+cid);
		}*/
	}

	@SuppressWarnings("deprecation")
	public static void setTempTile(World w, int bx, int by, int bz, int tid, byte meta) {
	/*	int cx = bx / 16 + 128;
		int cz = bz / 16 + 128;
		if (checkCoords(cx, cz)) {
			int cid = cx * 256 + cz;
			if (!chunksData.containsKey(cid)) {
				// System.out.println("Made "+cid);
				chunksData.put(cid, new ArrayList<int[]>());
			}
			int[] record = { bx, by, bz, w.getBlockAt(bx, by, bz).getTypeId(), w.getBlockAt(bx, by, bz).getData() };
			chunksData.get(cid).add(record);
			w.getBlockAt(bx, by, bz).setTypeId(tid);
			w.getBlockAt(bx, by, bz).setData((byte) meta);
			// System.out.println("destroyed but added record for
			// "+bx+":"+by+":"+bz+" "+cid);
		}*/
	}

	@SuppressWarnings("deprecation")
	private static boolean checkSides(World w, int bx, int by, int bz) {
		/*if (w.getBlockAt(bx - 1, by, bz).getTypeId() == 68)
			return false;
		if (w.getBlockAt(bx + 1, by, bz).getTypeId() == 68)
			return false;
		if (w.getBlockAt(bx, by, bz + 1).getTypeId() == 68)
			return false;
		if (w.getBlockAt(bx, by, bz - 1).getTypeId() == 68)
			return false;
		return true;*/
		return false;
	}

	private static boolean checkCoords(int x, int y) {
		return true;// !(x < 128 || y < 128 || x > 128 || y > 128);
	}
}
