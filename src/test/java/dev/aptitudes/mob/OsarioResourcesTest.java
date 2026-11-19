package dev.aptitudes.mob;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;
import software.bernie.geckolib.animation.Animation;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.loading.json.raw.Model;
import software.bernie.geckolib.loading.json.typeadapter.KeyFramesAdapter;
import software.bernie.geckolib.loading.object.BakedAnimations;
import software.bernie.geckolib.loading.object.BakedModelFactory;
import software.bernie.geckolib.loading.object.GeometryTree;

import javax.imageio.ImageIO;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OsarioResourcesTest {
	private static final String GEOMETRY = "geo/entity/osario_quebrado.geo.json";
	private static final String ANIMATIONS = "animations/entity/osario_quebrado.animation.json";
	private static final String TEXTURE = "textures/entity/osario_quebrado";
	private static final String PREFIX = "animation.osario_quebrado.";

	@Test
	void realGeckoLibLoadersBakeTheWholeModelAndAllTenClips() throws IOException {
		var gson = KeyFramesAdapter.GEO_GSON;
		Model raw = gson.fromJson(json(GEOMETRY), Model.class);
		var model = BakedModelFactory.getForNamespace("aptitudes").constructGeoModel(GeometryTree.fromModel(raw));
		assertEquals(1, model.topLevelBones().size());
		GeoBone root = model.getBone("root").orElseThrow();
		var names = new HashSet<String>();
		assertEquals(169, count(root, names));
		assertEquals(30, names.size());
		for (String name : Set.of("body", "head", "jaw", "chest_core",
				"bone_hand", "bone_fingers", "bone_thumb", "dragging_end", "debris_roots", "debris_chips")) {
			assertTrue(names.contains(name), name);
		}

		JsonObject shapes = json(ANIMATIONS).getAsJsonObject("animations");
		var baked = gson.fromJson(shapes, BakedAnimations.class);
		Map<String, Integer> expected = Map.of("idle", 64, "walk", 24, "chase", 17, "alert", 13,
				"claw_swipe", 18, "heavy_swipe", 28, "ground_slam", 36, "lunge", 27, "hurt", 8, "death", 19);
		assertEquals(expected.size(), baked.animations().size());
		for (var entry : expected.entrySet()) {
			String name = entry.getKey();
			var clip = baked.getAnimation(PREFIX + name);
			assertNotNull(clip, name);
			assertEquals(entry.getValue(), clip.length(), 1e-6, name);
			assertTrue(clip.boneAnimations().length > 0, name);
			for (var bone : clip.boneAnimations()) assertTrue(names.contains(bone.boneName()), bone.boneName());
			var loop = switch (name) {
				case "idle", "walk", "chase" -> Animation.LoopType.LOOP;
				case "death" -> Animation.LoopType.HOLD_ON_LAST_FRAME;
				default -> Animation.LoopType.PLAY_ONCE;
			};
			assertSame(loop, clip.loopType(), name);
		}
	}

	@Test
	void uvUsesLogical128AndTheMaskOnlyMarksTheCore() throws IOException {
		JsonObject geometry = json(GEOMETRY).getAsJsonArray("minecraft:geometry").get(0).getAsJsonObject();
		JsonObject description = geometry.getAsJsonObject("description");
		assertEquals("geometry.osario_quebrado", description.get("identifier").getAsString());
		assertEquals(128, description.get("texture_width").getAsInt());
		assertEquals(128, description.get("texture_height").getAsInt());
		boolean[][] core = new boolean[256][256];
		for (JsonElement boneElement : geometry.getAsJsonArray("bones")) {
			JsonObject bone = boneElement.getAsJsonObject();
			if (!bone.has("cubes")) continue;
			boolean isCore = bone.get("name").getAsString().equals("chest_core");
			for (JsonElement cubeElement : bone.getAsJsonArray("cubes")) {
				JsonObject cube = cubeElement.getAsJsonObject();
				double[] size = vec(cube.getAsJsonArray("size"));
				for (double d : size) {
					assertTrue(d > 0);
					assertEquals(Math.rint(d * 2), d * 2, 1e-6);
				}
				JsonElement uv = cube.get("uv");
				if (uv.isJsonArray()) {
					double[] p = vec(uv.getAsJsonArray());
					rectangle(p[0], p[1], 2 * (size[0] + size[2]), size[1] + size[2], null);
					if (isCore) {
						rectangle(p[0], p[1] + size[2], 2 * (size[0] + size[2]), size[1], core);
						rectangle(p[0] + size[2], p[1], 2 * size[0], size[2], core);
					}
				} else {
					for (JsonElement faceElement : uv.getAsJsonObject().asMap().values()) {
						JsonObject face = faceElement.getAsJsonObject();
						double[] p = vec(face.getAsJsonArray("uv")), s = vec(face.getAsJsonArray("uv_size"));
						rectangle(p[0], p[1], s[0], s[1], isCore ? core : null);
					}
				}
			}
		}
		try (var base = open(TEXTURE + ".png"); var mask = open(TEXTURE + "_glowmask.png")) {
			var a = ImageIO.read(base);
			var m = ImageIO.read(mask);
			assertEquals(256, a.getWidth());
			assertEquals(256, a.getHeight());
			assertEquals(a.getWidth(), m.getWidth());
			assertEquals(a.getHeight(), m.getHeight());
			int glowing = 0;
			for (int y = 0; y < m.getHeight(); y++) {
				for (int x = 0; x < m.getWidth(); x++) {
					int rgba = m.getRGB(x, y), alpha = rgba >>> 24;
					if (alpha == 0) {
						assertEquals(0, rgba);
					} else {
						assertTrue(core[y][x]);
						assertTrue(alpha == 160 || alpha == 255);
						assertEquals(255, a.getRGB(x, y) >>> 24);
						glowing++;
					}
				}
			}
			assertEquals(84, glowing);
		}
	}

	@Test
	void soundAndSubtitleEntriesExistForEachLocale() throws IOException {
		var sounds = json("sounds.json").asMap();
		var locales = Set.of("en_us", "es_es", "es_ar", "es_cl", "es_ec", "es_mx", "es_uy", "es_ve", "pt_br", "fr_fr", "de_de");
		var keys = new HashSet<String>();
		for (String name : Set.of("ambient", "alert", "hurt", "death", "claw", "heavy", "slam_charge", "slam_hit", "lunge", "step")) {
			var sound = sounds.get("osario_" + name).getAsJsonObject();
			assertEquals("aptitudes:osario_" + name, sound.getAsJsonArray("sounds").get(0).getAsString());
			keys.add(sound.get("subtitle").getAsString());
			try (var ogg = open("sounds/osario_" + name + ".ogg")) {
				assertEquals("OggS", new String(ogg.readNBytes(4), StandardCharsets.US_ASCII));
			}
		}
		for (String locale : locales) assertTrue(json("lang/" + locale + ".json").keySet().containsAll(keys), locale);
	}

	private static void rectangle(double u, double v, double w, double h, boolean[][] occupancy) {
		assertTrue(w != 0 && h != 0);
		assertTrue(Math.min(u, u + w) >= 0 && Math.max(u, u + w) <= 128);
		assertTrue(Math.min(v, v + h) >= 0 && Math.max(v, v + h) <= 128);
		for (double number : new double[] {u, v, w, h}) assertEquals(Math.rint(number * 2), number * 2, 1e-6);
		if (occupancy == null) return;
		for (int y = (int) (2 * Math.min(v, v + h)); y < 2 * Math.max(v, v + h); y++)
			for (int x = (int) (2 * Math.min(u, u + w)); x < 2 * Math.max(u, u + w); x++) occupancy[y][x] = true;
	}

	private static double[] vec(JsonArray array) {
		double[] data = new double[array.size()];
		for (int i = 0; i < data.length; i++) data[i] = array.get(i).getAsDouble();
		return data;
	}

	private static int count(GeoBone bone, Set<String> names) {
		assertTrue(names.add(bone.getName()));
		return bone.getCubes().size() + bone.getChildBones().stream().mapToInt(child -> count(child, names)).sum();
	}

	private static JsonObject json(String path) throws IOException {
		try (var reader = new InputStreamReader(open(path), StandardCharsets.UTF_8)) {
			return JsonParser.parseReader(reader).getAsJsonObject();
		}
	}

	private static InputStream open(String path) {
		InputStream in = OsarioResourcesTest.class.getResourceAsStream("/assets/aptitudes/" + path);
		assertNotNull(in, path);
		return in;
	}
}
