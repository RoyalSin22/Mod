package dev.aptitudes.client.screen;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.item.RoleArmor;
import dev.aptitudes.item.weapon.RoleWeapons;
import dev.aptitudes.role.Role;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.registry.Registries;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Grimorio de Aptitudes (v20).
 *
 * <ul>
 *   <li><b>Índice</b> con iconos; flechas ↑/↓ o clic; sonido de página; recuerda el último capítulo.</li>
 *   <li><b>Ilustraciones</b>: fila de objetos de cada capítulo; clic → ficha del objeto.</li>
 *   <li><b>Ficha</b>: objeto grande, descripción, receta en cuadrícula 3×3 leída del juego (las recetas
 *       llegan sincronizadas del servidor, así que siempre coinciden), varias recetas con ‹ ›, «Se usa en»
 *       y navegación por ingredientes con historial (Volver / Retroceso).</li>
 *   <li><b>Recetario</b>: catálogo de todos los objetos fabricables del mod con buscador.</li>
 * </ul>
 * Solo lee datos del cliente: no envía nada al servidor.
 */
public class GuideScreen extends Screen {
	private record Chapter(String id, Item icon, Supplier<List<Item>> items) {}

	private static final String RECIPES_CHAPTER = "recipes";

	private static final List<Chapter> CHAPTERS = List.of(
			chapter("start", ModItems.GUIDE_BOOK, () -> List.of(ModItems.GUIDE_BOOK, dev.aptitudes.block.ModBlocks.BONFIRE_ITEM)),
			chapter("keys", Items.OAK_BUTTON, List::of),
			chapter("bonfire", dev.aptitudes.block.ModBlocks.BONFIRE_ITEM,
					() -> List.of(dev.aptitudes.block.ModBlocks.BONFIRE_ITEM, ModItems.ESTUS_FLASK, ModItems.ETHER_FLASK, ModItems.SOUL_SHARD)),
			chapter("attributes", Items.EXPERIENCE_BOTTLE, () -> List.of(Items.EXPERIENCE_BOTTLE, ModItems.REBIRTH_OFFERING)),
			chapter("skills", Items.IRON_PICKAXE, () -> List.of(Items.IRON_PICKAXE, Items.IRON_SHOVEL, Items.IRON_AXE, Items.IRON_HOE,
					Items.IRON_SWORD, Items.BOW, Items.SHIELD, Items.FEATHER, Items.FISHING_ROD, Items.ENCHANTING_TABLE)),
			chapter("roles", ModItems.REBIRTH_OFFERING, GuideScreen::roleItems),
			chapter("tree", Items.BOOK, () -> List.of(Items.BOOK, ModItems.REBIRTH_OFFERING)),
			chapter("evolution", ModItems.MASTERY_EMBLEM, () -> List.of(ModItems.MASTERY_EMBLEM, ModItems.ULTIMATE_CORE)),
			chapter("weapons", Items.NETHERITE_SWORD, () -> RoleWeapons.all().stream().limit(10).toList()),
			chapter("elements", ModItems.ELEMENTAL_ORB, () -> List.of(ModItems.ELEMENTAL_ORB)),
			chapter("armor", Items.IRON_CHESTPLATE, GuideScreen::armorItems),
			chapter("soul_items", ModItems.SOUL_PICKAXE,
					() -> List.of(ModItems.SOUL_SHARD, ModItems.SOUL_PICKAXE, ModItems.SOUL_AXE, ModItems.SOUL_SHOVEL, ModItems.SOUL_HOE)),
			chapter("accessories", Items.GOLD_INGOT, () -> List.copyOf(ModItems.ACCESSORIES.values())),
			chapter("elites", ModItems.ELITE_ESSENCE, () -> List.of(ModItems.ELITE_ESSENCE, ModItems.CHAMPION_HEART, ModItems.TOWER_CORE, ModItems.SHRINE_SIGIL, ModItems.WARLORD_MEDALLION)),
			chapter("eclipse", ModItems.ECLIPSE_FRAGMENT, () -> List.of(ModItems.ECLIPSE_FRAGMENT, ModItems.ECLIPSE_CORE, ModItems.RUNE_ECLIPSE)),
			chapter("structures", Items.SPAWNER, () -> List.of(Items.CHEST, Items.SPAWNER)),
			chapter("contracts", dev.aptitudes.block.ModBlocks.CONTRACT_BOARD_ITEM, () -> List.of(dev.aptitudes.block.ModBlocks.CONTRACT_BOARD_ITEM)),
			chapter("guild", dev.aptitudes.world.GuildVillagers.GUILD_DESK_ITEM,
					() -> List.of(dev.aptitudes.world.GuildVillagers.GUILD_DESK_ITEM, ModItems.GUILD_SEAL, ModItems.ARMORY_VOUCHER, ModItems.RUNE_FLAME, ModItems.RUNE_LEECH, ModItems.RUNE_SWIFT, ModItems.RUNE_FORCE,
						ModItems.RUNE_FROST, ModItems.RUNE_THUNDER, ModItems.RUNE_GUARD)),
			chapter(RECIPES_CHAPTER, Items.CRAFTING_TABLE, List::of),
			chapter("drops", Items.ZOMBIE_HEAD, () -> List.of(Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, ModItems.ELITE_ESSENCE)));

	/** Último capítulo abierto (dura la sesión de juego). */
	private static int lastChapter;

	private static final int LIST_W = 124;
	private static final int SLOT = 18;

	private int W = 380;
	private int H = 236;
	private int left;
	private int top;
	private int chapter;
	private double scroll;

	/** Ficha abierta (null = capítulo). */
	@Nullable private Item viewing;
	private int recipeIndex;
	private final Deque<Item> history = new ArrayDeque<>();

	// Recetas del mod, construidas una vez al abrir.
	private final Map<Item, List<RecipeEntry<?>>> recipesFor = new HashMap<>();
	private final Map<Item, Set<Item>> usedIn = new HashMap<>();
	private final List<Item> craftable = new ArrayList<>();

	private TextFieldWidget search;
	private String query = "";

	/** Zonas clicables registradas en cada fotograma: rectángulo → acción. */
	private record Hit(int x, int y, int w, int h, Runnable action) {
		boolean contains(double mx, double my) {
			return mx >= x && mx < x + w && my >= y && my < y + h;
		}
	}

	private final List<Hit> hits = new ArrayList<>();

	public GuideScreen() {
		super(Text.translatable("screen.aptitudes.guide"));
		this.chapter = MathHelper.clamp(lastChapter, 0, CHAPTERS.size() - 1);
	}

	private static Chapter chapter(String id, Item icon, Supplier<List<Item>> items) {
		return new Chapter(id, icon, items);
	}

	private static List<Item> roleItems() {
		List<Item> out = new ArrayList<>();
		for (Role role : Role.values()) out.add(role.iconStack().getItem());
		return out;
	}

	private static List<Item> armorItems() {
		List<Item> out = new ArrayList<>(RoleArmor.pieces(Role.BERSERKER));
		for (Role role : new Role[]{Role.MAGE, Role.ASSASSIN, Role.DRUID}) {
			List<Item> p = RoleArmor.pieces(role);
			if (!p.isEmpty()) out.add(p.get(Math.min(1, p.size() - 1))); // peto de otros roles
		}
		return out;
	}

	// ------------------------------------------------------------ Montaje

	@Override
	protected void init() {
		W = MathHelper.clamp(width - 20, 380, 520);
		H = MathHelper.clamp(height - 16, 236, 300);
		left = (width - W) / 2;
		top = (height - H) / 2;
		buildRecipeIndex();
		search = new TextFieldWidget(textRenderer, pageX(), top + 40, Math.min(160, pageW()), 14,
				Text.translatable("screen.aptitudes.guide.search"));
		search.setPlaceholder(Text.translatable("screen.aptitudes.guide.search").formatted(Formatting.DARK_GRAY));
		search.setText(query);
		search.setChangedListener(s -> {
			query = s;
			scroll = 0;
		});
		search.setMaxLength(40);
		addSelectableChild(search);
	}

	/** Índice de recetas del mod a partir de las recetas sincronizadas por el servidor. */
	private void buildRecipeIndex() {
		recipesFor.clear();
		usedIn.clear();
		craftable.clear();
		if (client == null || client.world == null) return;
		var lookup = client.world.getRegistryManager();
		for (RecipeEntry<?> entry : client.world.getRecipeManager().values()) {
			Recipe<?> recipe = entry.value();
			if (!(recipe instanceof ShapedRecipe) && !(recipe instanceof ShapelessRecipe)) continue;
			ItemStack result = recipe.getResult(lookup);
			if (result.isEmpty() || !isModItem(result.getItem())) continue;
			recipesFor.computeIfAbsent(result.getItem(), k -> new ArrayList<>()).add(entry);
			for (Ingredient ing : recipe.getIngredients()) {
				for (ItemStack s : ing.getMatchingStacks()) {
					usedIn.computeIfAbsent(s.getItem(), k -> new LinkedHashSet<>()).add(result.getItem());
				}
			}
		}
		craftable.addAll(recipesFor.keySet());
		craftable.sort(Comparator.comparing(i -> i.getName().getString()));
	}

	private static boolean isModItem(Item item) {
		return Registries.ITEM.getId(item).getNamespace().equals(Aptitudes.MOD_ID);
	}

	private int pageX() { return left + LIST_W + 14; }
	private int pageW() { return W - LIST_W - 22; }
	private int rowH() { return MathHelper.clamp((H - 32) / CHAPTERS.size(), 10, 13); }
	private boolean recipesMode() { return viewing == null && CHAPTERS.get(chapter).id().equals(RECIPES_CHAPTER); }

	// ------------------------------------------------------------ Dibujo

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		hits.clear();
		UiStyle.window(ctx, left, top, W, H);
		ctx.drawTextWithShadow(textRenderer, title, left + 10, top + 8, UiStyle.GOLD);
		renderIndex(ctx, mouseX, mouseY);

		int px = pageX();
		int pw = pageW();
		UiStyle.panel(ctx, px - 4, top + 22, pw + 8, H - 30);
		Tooltip tip = new Tooltip();
		search.visible = recipesMode(); // invisible fuera del Recetario: no recibe clics
		if (viewing != null) {
			renderItemPage(ctx, mouseX, mouseY, tip);
		} else if (recipesMode()) {
			renderRecipeBook(ctx, mouseX, mouseY, delta, tip);
		} else {
			renderChapter(ctx, mouseX, mouseY, tip);
		}
		tip.draw(ctx, mouseX, mouseY);
	}

	private void renderIndex(DrawContext ctx, int mouseX, int mouseY) {
		int rh = rowH();
		for (int i = 0; i < CHAPTERS.size(); i++) {
			int y = top + 24 + i * rh;
			boolean sel = i == chapter && viewing == null;
			boolean current = i == chapter;
			boolean hover = mouseX >= left + 6 && mouseX < left + 6 + LIST_W && mouseY >= y && mouseY < y + rh;
			if (current || hover) ctx.fill(left + 6, y, left + 6 + LIST_W, y + rh, sel ? 0x60C9A54C : current ? 0x40C9A54C : 0x30303850);
			// Icono a 10 px
			ctx.getMatrices().push();
			ctx.getMatrices().translate(left + 8, y + (rh - 10) / 2f, 0);
			ctx.getMatrices().scale(0.625f, 0.625f, 1f);
			ctx.drawItem(new ItemStack(CHAPTERS.get(i).icon()), 0, 0);
			ctx.getMatrices().pop();
			UiStyle.label(ctx, textRenderer, Text.translatable("guide.aptitudes." + CHAPTERS.get(i).id() + ".title"),
					left + 21, y + (rh - 8) / 2, LIST_W - 17, current ? UiStyle.GOLD : UiStyle.TEXT);
			final int index = i;
			hits.add(new Hit(left + 6, y, LIST_W, rh, () -> openChapter(index)));
		}
	}

	private void renderChapter(DrawContext ctx, int mouseX, int mouseY, Tooltip tip) {
		int px = pageX();
		int pw = pageW();
		Chapter c = CHAPTERS.get(chapter);
		// Cabecera con ilustración grande
		bigItem(ctx, new ItemStack(c.icon()), px + pw - 34, top + 25);
		ctx.drawTextWithShadow(textRenderer, Text.translatable("guide.aptitudes." + c.id() + ".title").formatted(Formatting.BOLD),
				px, top + 30, UiStyle.GOLD);
		Text counter = Text.translatable("screen.aptitudes.guide.chapter", chapter + 1, CHAPTERS.size());
		ctx.drawText(textRenderer, counter, px, top + 42, UiStyle.MUTED, false);
		UiStyle.line(ctx, px, top + 59, px + pw, top + 59, UiStyle.GOLD_DARK);
		int y = top + 63;

		// Ilustraciones clicables
		List<Item> items = c.items().get();
		if (!items.isEmpty()) {
			int perRow = Math.max(1, pw / 20);
			for (int i = 0; i < items.size(); i++) {
				int ix = px + (i % perRow) * 20;
				int iy = y + (i / perRow) * 20;
				Item item = items.get(i);
				boolean hover = mouseX >= ix && mouseX < ix + 18 && mouseY >= iy && mouseY < iy + 18;
				slot(ctx, ix, iy, hover);
				ctx.drawItem(new ItemStack(item), ix + 1, iy + 1);
				if (hover) {
					tip.set(List.of(item.getName(), Text.translatable(recipesFor.containsKey(item)
							? "screen.aptitudes.guide.click_recipe" : "screen.aptitudes.guide.click_details").formatted(Formatting.GRAY)));
				}
				hits.add(new Hit(ix, iy, 18, 18, () -> openItem(item, true)));
			}
			y += ((items.size() + perRow - 1) / perRow) * 20 + 4;
		}

		// Texto con desplazamiento y barra
		List<OrderedText> lines = textRenderer.wrapLines(Text.translatable("guide.aptitudes." + c.id() + ".body"), pw - 10);
		int bottom = top + H - 12;
		int visible = Math.max(1, (bottom - y) / 10);
		int maxScroll = Math.max(0, lines.size() - visible);
		scroll = MathHelper.clamp(scroll, 0, maxScroll);
		ctx.enableScissor(px - 2, y, px + pw + 2, bottom);
		for (int i = (int) scroll; i < lines.size() && i < (int) scroll + visible + 1; i++) {
			ctx.drawText(textRenderer, lines.get(i), px, y + (i - (int) scroll) * 10, UiStyle.TEXT, false);
		}
		ctx.disableScissor();
		scrollBar(ctx, px + pw - 2, y, bottom, scroll, maxScroll, visible, lines.size());
		if (maxScroll > 0) {
			Text more = Text.translatable("screen.aptitudes.guide.scroll", (int) scroll + 1, maxScroll + 1);
			ctx.drawText(textRenderer, more, px + pw - textRenderer.getWidth(more) - 6, top + H - 10, UiStyle.MUTED, false);
		}
	}

	private void renderRecipeBook(DrawContext ctx, int mouseX, int mouseY, float delta, Tooltip tip) {
		int px = pageX();
		int pw = pageW();
		ctx.drawTextWithShadow(textRenderer, Text.translatable("guide.aptitudes.recipes.title").formatted(Formatting.BOLD),
				px, top + 28, UiStyle.GOLD);
		List<Item> shown = filtered();
		Text count = Text.translatable("screen.aptitudes.guide.recipes_count", shown.size());
		ctx.drawText(textRenderer, count, px + pw - textRenderer.getWidth(count), top + 28, UiStyle.MUTED, false);
		search.setX(px);
		search.setY(top + 40);
		search.render(ctx, mouseX, mouseY, delta);
		int y = top + 60;
		int bottom = top + H - 12;
		if (craftable.isEmpty()) {
			for (OrderedText line : textRenderer.wrapLines(Text.translatable("screen.aptitudes.guide.no_recipes_synced"), pw)) {
				ctx.drawText(textRenderer, line, px, y, UiStyle.MUTED, false);
				y += 10;
			}
			return;
		}
		int perRow = Math.max(1, (pw - 6) / 20);
		int rows = (shown.size() + perRow - 1) / perRow;
		int visibleRows = Math.max(1, (bottom - y) / 20);
		int maxScroll = Math.max(0, rows - visibleRows);
		scroll = MathHelper.clamp(scroll, 0, maxScroll);
		int first = (int) scroll * perRow;
		for (int i = first; i < shown.size() && i < first + visibleRows * perRow; i++) {
			int ix = px + ((i - first) % perRow) * 20;
			int iy = y + ((i - first) / perRow) * 20;
			Item item = shown.get(i);
			boolean hover = mouseX >= ix && mouseX < ix + 18 && mouseY >= iy && mouseY < iy + 18;
			slot(ctx, ix, iy, hover);
			ctx.drawItem(new ItemStack(item), ix + 1, iy + 1);
			if (hover) tip.set(List.of(item.getName(), Text.translatable("screen.aptitudes.guide.click_recipe").formatted(Formatting.GRAY)));
			hits.add(new Hit(ix, iy, 18, 18, () -> openItem(item, true)));
		}
		scrollBar(ctx, px + pw - 2, y, bottom, scroll, maxScroll, visibleRows, rows);
	}

	private void renderItemPage(DrawContext ctx, int mouseX, int mouseY, Tooltip tip) {
		int px = pageX();
		int pw = pageW();
		Item item = viewing;
		ItemStack stack = new ItemStack(item);
		// Volver
		Text back = Text.translatable("screen.aptitudes.guide.back");
		boolean backHover = mouseX >= px && mouseX < px + textRenderer.getWidth(back) + 4 && mouseY >= top + 25 && mouseY < top + 35;
		ctx.drawText(textRenderer, back, px, top + 26, backHover ? UiStyle.GOLD : UiStyle.MUTED, false);
		hits.add(new Hit(px, top + 25, textRenderer.getWidth(back) + 4, 10, this::goBack));

		// Cabecera: objeto grande + nombre + descripción
		int y = top + 38;
		ctx.fill(px, y, px + 36, y + 36, 0xFF1B1F2B);
		ctx.drawBorder(px, y, 36, 36, UiStyle.GOLD_DARK);
		bigItem(ctx, stack, px + 2, y + 2);
		ctx.drawTextWithShadow(textRenderer, UiStyle.fit(textRenderer, stack.getName().copy().formatted(Formatting.BOLD), pw - 44),
				px + 42, y + 2, UiStyle.GOLD);
		List<Text> desc = description(stack);
		int dy = y + 14;
		for (Text line : desc) {
			for (OrderedText ol : textRenderer.wrapLines(line, pw - 44)) {
				if (dy > y + 34) break;
				ctx.drawText(textRenderer, ol, px + 42, dy, UiStyle.MUTED, false);
				dy += 9;
			}
		}
		y += 42;
		UiStyle.line(ctx, px, y, px + pw, y, UiStyle.GOLD_DARK);
		y += 5;

		// Receta
		List<RecipeEntry<?>> recipes = recipesFor.getOrDefault(item, List.of());
		if (recipes.isEmpty()) {
			for (OrderedText line : textRenderer.wrapLines(Text.translatable("screen.aptitudes.guide.no_recipe"), pw)) {
				ctx.drawText(textRenderer, line, px, y, UiStyle.MUTED, false);
				y += 10;
			}
		} else {
			recipeIndex = Math.floorMod(recipeIndex, recipes.size());
			Recipe<?> recipe = recipes.get(recipeIndex).value();
			boolean shapeless = recipe instanceof ShapelessRecipe;
			Text kind = Text.translatable(shapeless ? "screen.aptitudes.guide.shapeless" : "screen.aptitudes.guide.shaped");
			ctx.drawText(textRenderer, kind, px, y, UiStyle.TEXT, false);
			if (recipes.size() > 1) {
				Text nav = Text.literal("‹ " + (recipeIndex + 1) + "/" + recipes.size() + " ›");
				int nx = px + pw - textRenderer.getWidth(nav);
				ctx.drawText(textRenderer, nav, nx, y, UiStyle.GOLD, false);
				hits.add(new Hit(nx, y - 1, 10, 10, () -> recipeIndex--));
				hits.add(new Hit(nx + textRenderer.getWidth(nav) - 10, y - 1, 10, 10, () -> recipeIndex++));
			}
			y += 12;
			drawGrid(ctx, recipe, px + 4, y, mouseX, mouseY, tip);
			// Flecha y resultado
			int ax = px + 4 + 3 * SLOT + 8;
			int ay = y + SLOT + 4;
			arrow(ctx, ax, ay);
			int rx = ax + 22;
			int ry = y + SLOT - 4;
			ctx.fill(rx, ry, rx + 26, ry + 26, 0xFF1B1F2B);
			ctx.drawBorder(rx, ry, 26, 26, UiStyle.GOLD);
			ItemStack result = recipe.getResult(client.world.getRegistryManager());
			ctx.drawItem(result, rx + 5, ry + 5);
			ctx.drawItemInSlot(textRenderer, result, rx + 5, ry + 5);
			if (mouseX >= rx && mouseX < rx + 26 && mouseY >= ry && mouseY < ry + 26) tip.item(result);
			// v27: las armaduras de rol se fabrican en la Mesa del gremio.
			Text where = Text.translatable(recipe.getType() == dev.aptitudes.world.GuildCrafting.RECIPE_TYPE
					? "screen.aptitudes.guide.guild_desk" : "screen.aptitudes.guide.crafting_table");
			ctx.drawText(textRenderer, UiStyle.fit(textRenderer, where, px + pw - rx - 32), rx + 32, ry + 9, UiStyle.MUTED, false);
			y += 3 * SLOT + 6;
		}

		// Se usa en
		Set<Item> uses = usedIn.getOrDefault(item, Set.of());
		if (!uses.isEmpty() && y + 30 < top + H - 8) {
			ctx.drawText(textRenderer, Text.translatable("screen.aptitudes.guide.used_in"), px, y, UiStyle.TEXT, false);
			y += 11;
			int ix = px;
			for (Item use : uses) {
				if (ix + 18 > px + pw) break;
				boolean hover = mouseX >= ix && mouseX < ix + 18 && mouseY >= y && mouseY < y + 18;
				slot(ctx, ix, y, hover);
				ctx.drawItem(new ItemStack(use), ix + 1, y + 1);
				if (hover) tip.set(List.of(use.getName(), Text.translatable("screen.aptitudes.guide.click_recipe").formatted(Formatting.GRAY)));
				hits.add(new Hit(ix, y, 18, 18, () -> openItem(use, true)));
				ix += 20;
			}
		}
	}

	/** Cuadrícula 3×3: con forma respeta ancho/alto; sin forma, en orden. */
	private void drawGrid(DrawContext ctx, Recipe<?> recipe, int x, int y, int mouseX, int mouseY, Tooltip tip) {
		List<Ingredient> ings = recipe.getIngredients();
		int w = 3;
		if (recipe instanceof ShapedRecipe shaped) w = shaped.getWidth();
		for (int r = 0; r < 3; r++) {
			for (int c = 0; c < 3; c++) slot(ctx, x + c * SLOT, y + r * SLOT, false);
		}
		long tick = Util.getMeasuringTimeMs() / 1000; // los ingredientes alternativos rotan cada segundo
		for (int i = 0; i < ings.size() && i < 9; i++) {
			Ingredient ing = ings.get(i);
			ItemStack[] options = ing.getMatchingStacks();
			if (options.length == 0) continue;
			ItemStack shown = options[(int) (tick % options.length)];
			int col = i % w;
			int row = i / w;
			if (row > 2 || col > 2) continue;
			int sx = x + col * SLOT;
			int sy = y + row * SLOT;
			ctx.drawItem(shown, sx + 1, sy + 1);
			if (mouseX >= sx && mouseX < sx + SLOT && mouseY >= sy && mouseY < sy + SLOT) {
				tip.item(shown);
				if (recipesFor.containsKey(shown.getItem()) || isModItem(shown.getItem())) {
					Item target = shown.getItem();
					hits.add(new Hit(sx, sy, SLOT, SLOT, () -> openItem(target, true)));
				}
			}
		}
	}

	private List<Text> description(ItemStack stack) {
		List<Text> out = new ArrayList<>();
		if (client == null || client.world == null) return out;
		List<Text> lines = stack.getTooltip(Item.TooltipContext.create(client.world), client.player, TooltipType.BASIC);
		for (int i = 1; i < lines.size() && out.size() < 3; i++) {
			if (!lines.get(i).getString().isBlank()) out.add(lines.get(i));
		}
		return out;
	}

	private List<Item> filtered() {
		String q = query.trim().toLowerCase(Locale.ROOT);
		if (q.isEmpty()) return craftable;
		List<Item> out = new ArrayList<>();
		for (Item item : craftable) {
			if (item.getName().getString().toLowerCase(Locale.ROOT).contains(q)) out.add(item);
		}
		return out;
	}

	// --------------------------------------------------------- Utilidades

	private void bigItem(DrawContext ctx, ItemStack stack, int x, int y) {
		ctx.getMatrices().push();
		ctx.getMatrices().translate(x, y, 0);
		ctx.getMatrices().scale(2f, 2f, 1f);
		ctx.drawItem(stack, 0, 0);
		ctx.getMatrices().pop();
	}

	private static void slot(DrawContext ctx, int x, int y, boolean hover) {
		ctx.fill(x, y, x + SLOT, y + SLOT, hover ? 0xFF2E3550 : 0xFF1B1F2B);
		ctx.drawBorder(x, y, SLOT, SLOT, hover ? UiStyle.GOLD : UiStyle.BORDER);
	}

	private static void arrow(DrawContext ctx, int x, int y) {
		ctx.fill(x, y + 3, x + 12, y + 6, UiStyle.GOLD_DARK);
		for (int i = 0; i < 5; i++) ctx.fill(x + 12 + i, y + i - 1 + 1, x + 13 + i, y + 9 - i - 1, UiStyle.GOLD_DARK);
	}

	private static void scrollBar(DrawContext ctx, int x, int y1, int y2, double scroll, int maxScroll, int visible, int total) {
		if (maxScroll <= 0) return;
		int track = y2 - y1;
		int thumb = Math.max(10, track * visible / Math.max(1, total));
		int ty = y1 + (int) ((track - thumb) * (scroll / maxScroll));
		ctx.fill(x, y1, x + 2, y2, 0x40FFFFFF);
		ctx.fill(x, ty, x + 2, ty + thumb, UiStyle.GOLD);
	}

	/** Tooltip diferido: se dibuja al final, encima de todo. */
	private final class Tooltip {
		@Nullable private List<Text> lines;
		@Nullable private ItemStack stack;

		void set(List<Text> lines) {
			this.lines = lines;
			this.stack = null;
		}

		void item(ItemStack stack) {
			this.stack = stack;
			this.lines = null;
		}

		void draw(DrawContext ctx, int mx, int my) {
			if (stack != null) ctx.drawItemTooltip(textRenderer, stack, mx, my);
			else if (lines != null) ctx.drawTooltip(textRenderer, lines, mx, my);
		}
	}

	// ------------------------------------------------------- Navegación

	private void openChapter(int index) {
		if (index == chapter && viewing == null) return;
		chapter = MathHelper.clamp(index, 0, CHAPTERS.size() - 1);
		lastChapter = chapter;
		viewing = null;
		history.clear();
		scroll = 0;
		dropSearchFocus();
		pageSound();
	}

	private void openItem(Item item, boolean remember) {
		if (remember && viewing != null) history.push(viewing);
		viewing = item;
		recipeIndex = 0;
		dropSearchFocus();
		pageSound();
	}

	private void goBack() {
		viewing = history.isEmpty() ? null : history.pop();
		recipeIndex = 0;
		pageSound();
	}

	/** El buscador nunca conserva el foco fuera del Recetario (si no, se «comería» las teclas). */
	private void dropSearchFocus() {
		if (getFocused() == search) setFocused(null);
		search.setFocused(false);
	}

	private void pageSound() {
		if (client != null) client.getSoundManager().play(PositionedSoundInstance.master(SoundEvents.ITEM_BOOK_PAGE_TURN, 1.0f));
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if (recipesMode() && search.mouseClicked(mouseX, mouseY, button)) {
			setFocused(search);
			return true;
		}
		if (button == 0) {
			// Copia: la acción puede cambiar de página (y la lista se rehace en el siguiente fotograma).
			for (Hit hit : List.copyOf(hits)) {
				if (hit.contains(mouseX, mouseY)) {
					hit.action().run();
					return true;
				}
			}
		}
		if (button == 3 && viewing != null) { // botón «atrás» del ratón
			goBack();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		scroll -= verticalAmount * (recipesMode() ? 1 : 2);
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		boolean typing = recipesMode() && search.isFocused();
		if (typing && keyCode != GLFW.GLFW_KEY_ESCAPE) {
			return search.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
		}
		switch (keyCode) {
			case GLFW.GLFW_KEY_DOWN -> {
				openChapter(Math.min(CHAPTERS.size() - 1, chapter + 1));
				return true;
			}
			case GLFW.GLFW_KEY_UP -> {
				openChapter(Math.max(0, chapter - 1));
				return true;
			}
			case GLFW.GLFW_KEY_BACKSPACE -> {
				if (viewing != null) {
					goBack();
					return true;
				}
			}
			case GLFW.GLFW_KEY_LEFT -> {
				if (viewing != null) {
					recipeIndex--;
					return true;
				}
			}
			case GLFW.GLFW_KEY_RIGHT -> {
				if (viewing != null) {
					recipeIndex++;
					return true;
				}
			}
			default -> {
			}
		}
		return super.keyPressed(keyCode, scanCode, modifiers);
	}

	@Override
	public boolean charTyped(char chr, int modifiers) {
		if (recipesMode() && search.isFocused()) return search.charTyped(chr, modifiers);
		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
