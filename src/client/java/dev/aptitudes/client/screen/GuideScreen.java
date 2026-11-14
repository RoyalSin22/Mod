package dev.aptitudes.client.screen;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.client.ClientConfig;
import dev.aptitudes.client.ClientGrimoire;
import dev.aptitudes.client.ClientRoleState;
import dev.aptitudes.client.ClientTutorial;
import dev.aptitudes.guide.BestiaryRules;
import dev.aptitudes.logic.GuideCardLayout;
import dev.aptitudes.guide.GuideRules;
import dev.aptitudes.item.ModItems;
import dev.aptitudes.network.RoleSyncPayload;
import dev.aptitudes.role.PathRules;
import dev.aptitudes.role.RoleNode;
import dev.aptitudes.role.RoleTree;
import dev.aptitudes.tutorial.TutorialRules;
import dev.aptitudes.tutorial.TutorialView;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.DefaultAttributeRegistry;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.util.Identifier;
import dev.aptitudes.world.GuildShapelessRecipe;
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
 *   <li>v61: <b>Bestiario</b> (páginas que se desbloquean al vencer a cada criatura o jefe), <b>Progreso</b>,
 *       <b>buscador</b> global, <b>marcadores</b>, sendas de cada rol con sus iconos, enlace al capítulo del
 *       tutorial e ilustraciones de cabecera generadas ({@code tools/textures/gen_guide_headers.py}).</li>
 * </ul>
 * Solo lee datos del cliente; lo único que envía es la petición de refresco del bestiario al abrirse.
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
			chapter("drops", Items.ZOMBIE_HEAD, () -> List.of(Items.ZOMBIE_HEAD, Items.SKELETON_SKULL, ModItems.ELITE_ESSENCE)),
			chapter("paths", Items.NETHER_STAR, List::of),
			chapter("achievements", Items.GOLDEN_APPLE, List::of),
			chapter("alchemy", Items.BREWING_STAND, () -> List.of(ModItems.HERB_PETALS)));

	/** Capítulo recomendado para cada paso del tutorial (enlace desde el tutorial, G2). */
	private static final Map<TutorialRules.Step, String> TUTORIAL_CHAPTERS = Map.of(
			TutorialRules.Step.GUIDE, "start", TutorialRules.Step.BONFIRE, "bonfire",
			TutorialRules.Step.PRIMARY_ROLE, "roles", TutorialRules.Step.ACTIVE, "tree",
			TutorialRules.Step.COMBO, "skills", TutorialRules.Step.GUILD_CRAFT, "guild");

	/** Qué se muestra a la derecha del índice. */
	private enum Page { CHAPTER, ITEM, BESTIARY, BEAST, PROGRESS, SEARCH, BOOKMARKS, ROLE }

	/** Posición en el historial (Volver / Retroceso). */
	private record Loc(Page page, int chapter, @Nullable Item item, @Nullable BestiaryRules.Entry beast, @Nullable Role role) {}

	/** Pestañas de la barra de título. */
	private record Tab(Page page, Item icon, String key) {}

	private static final List<Tab> TABS = List.of(
			new Tab(Page.BESTIARY, Items.SKELETON_SKULL, "screen.aptitudes.guide.tab.bestiary"),
			new Tab(Page.PROGRESS, Items.CLOCK, "screen.aptitudes.guide.tab.progress"),
			new Tab(Page.SEARCH, Items.SPYGLASS, "screen.aptitudes.guide.tab.search"),
			new Tab(Page.BOOKMARKS, Items.NAME_TAG, "screen.aptitudes.guide.tab.bookmarks"));

	/** Ilustraciones de cabecera: 384×34, generadas por {@code tools/textures/gen_guide_headers.py}. */
	private static final int BANNER_W = 384;
	private static final int BANNER_H = 34;

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

	private Page page = Page.CHAPTER;
	/** Ficha de objeto abierta (solo en {@link Page#ITEM}). */
	@Nullable private Item viewing;
	@Nullable private BestiaryRules.Entry beast;
	@Nullable private Role role;
	private int recipeIndex;
	private final Deque<Loc> history = new ArrayDeque<>();
	/** Pantalla a la que se vuelve al cerrar (el inventario, si se abrió desde él). */
	@Nullable private final Supplier<Screen> returnTo;

	// Buscador global (G2): resultados recalculados solo al cambiar la consulta.
	private TextFieldWidget finder;
	private String finderQuery = "";
	private record Result(Text title, ItemStack icon, String kindKey, Runnable open) {}
	private List<Result> results = List.of();

	/** Criaturas para el modelo del bestiario (creadas una vez por pantalla). */
	private final Map<String, LivingEntity> models = new HashMap<>();
	/** Tipos sin modelo dibujable (no vivos o con renderizador que falla): se usa su icono. */
	private final Set<String> noModel = new java.util.HashSet<>();

	// Recetas del mod, construidas una vez al abrir.
	private final Map<Item, List<RecipeEntry<?>>> recipesFor = new HashMap<>();
	private final Map<Item, Set<Item>> usedIn = new HashMap<>();
	private final List<Item> craftable = new ArrayList<>();
	private record Brew(ItemStack input, ItemStack ingredient, ItemStack output) { }
	private final List<Brew> brewing = new ArrayList<>();

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
		this(null);
	}

	private GuideScreen(@Nullable Supplier<Screen> returnTo) {
		super(Text.translatable("screen.aptitudes.guide"));
		this.chapter = MathHelper.clamp(lastChapter, 0, CHAPTERS.size() - 1);
		this.returnTo = returnTo;
	}

	/** Abre directamente la ficha de un objeto («ver en el grimorio» desde un inventario). */
	public static GuideScreen forItem(Item item, @Nullable Supplier<Screen> returnTo) {
		GuideScreen screen = new GuideScreen(returnTo);
		screen.page = Page.ITEM;
		screen.viewing = item;
		return screen;
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
		buildBrewingIndex();
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
		finder = new TextFieldWidget(textRenderer, pageX(), top + 40, Math.min(200, pageW()), 14,
				Text.translatable("screen.aptitudes.guide.tab.search"));
		finder.setPlaceholder(Text.translatable("screen.aptitudes.guide.tab.search").formatted(Formatting.DARK_GRAY));
		finder.setMaxLength(GuideRules.MAX_QUERY);
		finder.setText(finderQuery);
		finder.setChangedListener(s -> {
			finderQuery = s;
			scroll = 0;
			results = findResults(s);
		});
		addSelectableChild(finder);
		results = findResults(finderQuery);
		ClientGrimoire.request();
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
			if (!(recipe instanceof ShapedRecipe) && !isShapeless(recipe)) continue;
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

	/** Consulta el registro de destilado del mundo: no mantiene una segunda tabla de recetas. */
	private void buildBrewingIndex() {
		brewing.clear();
		if (client == null || client.world == null) return;
		var registry = client.world.getBrewingRecipeRegistry();
		for (Item item : Registries.ITEM) {
			if (!isModItem(item)) continue;
			ItemStack ingredient = new ItemStack(item);
			if (!registry.isValidIngredient(ingredient)) continue;
			for (var potion : Registries.POTION.streamEntries().toList()) {
				ItemStack input = net.minecraft.component.type.PotionContentsComponent.createStack(Items.POTION, potion);
				if (!registry.hasRecipe(input, ingredient)) continue;
				ItemStack output = registry.craft(ingredient, input);
				var contents = output.get(net.minecraft.component.DataComponentTypes.POTION_CONTENTS);
				if (contents != null && contents.potion().isPresent()
						&& contents.potion().get().getKey().orElseThrow().getValue().getNamespace().equals(Aptitudes.MOD_ID)) {
					brewing.add(new Brew(input, ingredient, output));
				}
			}
		}
	}

	private int pageX() { return left + LIST_W + 14; }
	private int pageW() { return W - LIST_W - 22; }
	private int rowH() { return MathHelper.clamp((H - 32) / CHAPTERS.size(), 9, 13); }
	private boolean recipesMode() { return page == Page.CHAPTER && CHAPTERS.get(chapter).id().equals(RECIPES_CHAPTER); }
	private boolean alchemyMode() { return page == Page.CHAPTER && CHAPTERS.get(chapter).id().equals("alchemy"); }
	private boolean typing() {
		return (recipesMode() && search.isFocused()) || (page == Page.SEARCH && finder.isFocused());
	}

	// ------------------------------------------------------------ Dibujo

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		hits.clear();
		GuideUi.window(ctx, left, top, W, H); // v67 (G3): pergamino
		ctx.drawTextWithShadow(textRenderer, UiStyle.fit(textRenderer, title, LIST_W + 6), left + 10, top + 8, UiStyle.GOLD);
		Tooltip tip = new Tooltip();
		renderTabs(ctx, mouseX, mouseY, tip);
		renderIndex(ctx, mouseX, mouseY);

		int px = pageX();
		int pw = pageW();
		GuideUi.panel(ctx, px - 4, top + 22, pw + 8, H - 30);
		search.visible = recipesMode(); // invisible fuera del Recetario: no recibe clics
		finder.visible = page == Page.SEARCH;
		switch (page) {
			case ITEM -> {
				if (viewing != null) renderItemPage(ctx, mouseX, mouseY, tip);
			}
			case BESTIARY -> renderBestiary(ctx, mouseX, mouseY, tip);
			case BEAST -> renderBeast(ctx, mouseX, mouseY, tip);
			case PROGRESS -> renderProgress(ctx);
			case SEARCH -> renderSearch(ctx, mouseX, mouseY, delta);
			case BOOKMARKS -> renderBookmarks(ctx, mouseX, mouseY);
			case ROLE -> renderRole(ctx, mouseX, mouseY, tip);
			case CHAPTER -> {
				if (recipesMode()) renderRecipeBook(ctx, mouseX, mouseY, delta, tip);
				else renderChapter(ctx, mouseX, mouseY, tip);
			}
		}
		tip.draw(ctx, mouseX, mouseY);
	}

	/** Pestañas a la derecha del título; la de Tutorial solo aparece con un paso pendiente. */
	private void renderTabs(DrawContext ctx, int mouseX, int mouseY, Tooltip tip) {
		int tutorial = tutorialChapter();
		int count = TABS.size() + (tutorial >= 0 ? 1 : 0);
		int x = left + W - 8 - count * 20;
		for (int i = 0; i < count; i++) {
			boolean isTutorial = i == TABS.size();
			Tab tab = isTutorial ? null : TABS.get(i);
			boolean active = tab != null && page == tab.page();
			boolean hover = mouseX >= x && mouseX < x + 18 && mouseY >= top + 4 && mouseY < top + 20;
			ctx.fill(x, top + 4, x + 18, top + 20, active ? 0x60C9A54C : hover ? 0x40303850 : 0x20303850);
			ctx.drawBorder(x, top + 4, 18, 16, active || hover ? UiStyle.GOLD : UiStyle.BORDER);
			ctx.drawItem(new ItemStack(isTutorial ? Items.WRITABLE_BOOK : tab.icon()), x + 1, top + 4);
			if (hover) {
				tip.set(List.of(isTutorial ? Text.translatable("screen.aptitudes.guide.tab.tutorial", tutorialText())
						: Text.translatable(tab.key())));
			}
			final int target = tutorial;
			hits.add(new Hit(x, top + 4, 18, 16, isTutorial ? () -> openChapter(target) : () -> openPage(tab.page())));
			x += 20;
		}
	}

	/** Índice del capítulo del paso de tutorial pendiente, o −1. */
	private int tutorialChapter() {
		TutorialView view = ClientTutorial.view();
		if (view == null || !view.visible() || view.nextStep() < 0 || view.nextStep() >= TutorialRules.ORDER.size()) return -1;
		String id = TUTORIAL_CHAPTERS.get(TutorialRules.ORDER.get(view.nextStep()));
		for (int i = 0; i < CHAPTERS.size(); i++) if (CHAPTERS.get(i).id().equals(id)) return i;
		return -1;
	}

	private Text tutorialText() {
		TutorialView view = ClientTutorial.view();
		if (view == null || view.nextStep() < 0) return Text.empty();
		return Text.translatable("screen.aptitudes.guide.tutorial", view.nextStep() + 1, TutorialRules.ORDER.size(),
				Text.translatable("tutorial.aptitudes.step." + TutorialRules.ORDER.get(view.nextStep()).key()));
	}

	// -------------------------------------------------------- Cabeceras (G4)

	/**
	 * Cabecera común: ilustración generada, título, subtítulo, icono y marcador. Devuelve la primera fila útil.
	 *
	 * @param bookmark id del marcador o null si la página no se puede marcar
	 */
	private int header(DrawContext ctx, String banner, Text heading, Text subtitle, ItemStack icon,
					   @Nullable String bookmark, int mouseX, int mouseY, Tooltip tip) {
		int px = pageX();
		int pw = pageW();
		Identifier texture = Aptitudes.id("textures/gui/guide/" + banner + ".png");
		ctx.drawTexture(texture, px - 2, top + 24, 0f, 0f, Math.min(pw + 4, BANNER_W), BANNER_H, BANNER_W, BANNER_H);
		bigItem(ctx, icon, px + pw - 34, top + 25);
		int textW = pw - 40 - (bookmark != null ? 14 : 0);
		ctx.drawTextWithShadow(textRenderer, UiStyle.fit(textRenderer, heading.copy().formatted(Formatting.BOLD), textW),
				px + 2, top + 30, UiStyle.GOLD);
		ctx.drawTextWithShadow(textRenderer, UiStyle.fit(textRenderer, subtitle, textW), px + 2, top + 43, UiStyle.TEXT);
		if (bookmark != null) star(ctx, bookmark, px + pw - 48, top + 29, mouseX, mouseY, tip);
		UiStyle.line(ctx, px, top + 59, px + pw, top + 59, UiStyle.GOLD_DARK);
		return top + 63;
	}

	/** Estrella de marcador (☆ / ★); el clic añade o quita la página. */
	private void star(DrawContext ctx, String bookmark, int x, int y, int mouseX, int mouseY, Tooltip tip) {
		boolean marked = ClientConfig.get().guideBookmarks.contains(bookmark);
		boolean hover = mouseX >= x - 2 && mouseX < x + 10 && mouseY >= y - 2 && mouseY < y + 10;
		ctx.drawTextWithShadow(textRenderer, Text.literal(marked ? "★" : "☆"), x, y, marked || hover ? UiStyle.GOLD : UiStyle.MUTED);
		if (hover) tip.set(List.of(Text.translatable(marked ? "screen.aptitudes.guide.bookmark.remove" : "screen.aptitudes.guide.bookmark.add")));
		hits.add(new Hit(x - 2, y - 2, 12, 12, () -> {
			ClientConfig.get().guideBookmarks = GuideRules.toggle(ClientConfig.get().guideBookmarks, bookmark);
			ClientConfig.save();
		}));
	}

	private void renderIndex(DrawContext ctx, int mouseX, int mouseY) {
		int rh = rowH();
		for (int i = 0; i < CHAPTERS.size(); i++) {
			int y = top + 24 + i * rh;
			boolean sel = i == chapter && page == Page.CHAPTER;
			boolean current = i == chapter && (page == Page.CHAPTER || page == Page.ITEM || page == Page.ROLE);
			boolean hover = mouseX >= left + 6 && mouseX < left + 6 + LIST_W && mouseY >= y && mouseY < y + rh;
			if (current || hover) ctx.fill(left + 6, y, left + 6 + LIST_W, y + rh, sel ? 0x60C9A54C : current ? 0x40C9A54C : 0x30303850);
			// Con 23 capítulos, la ventana mínima usa filas e iconos de 9 px para que quepa el índice.
			int iconSize = Math.min(10, rh);
			ctx.getMatrices().push();
			ctx.getMatrices().translate(left + 8, y + (rh - iconSize) / 2f, 0);
			ctx.getMatrices().scale(iconSize / 16f, iconSize / 16f, 1f);
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
		// Cabecera con ilustración generada; si el tutorial recomienda este capítulo, lo dice en el subtítulo.
		Text counter = Text.translatable("screen.aptitudes.guide.chapter", chapter + 1, CHAPTERS.size());
		Text subtitle = tutorialChapter() == chapter ? tutorialText().copy().formatted(Formatting.YELLOW) : counter;
		int y = header(ctx, c.id(), Text.translatable("guide.aptitudes." + c.id() + ".title"), subtitle,
				new ItemStack(c.icon()), "chapter:" + c.id(), mouseX, mouseY, tip);
		if (c.id().equals("alchemy")) {
			renderAlchemy(ctx, mouseX, mouseY, tip);
			return;
		}

		// Ilustraciones clicables (en Sendas, un icono por rol que abre sus sendas: G5)
		List<Item> items = c.items().get();
		if (c.id().equals("paths")) {
			Role[] roles = Role.values();
			int perRow = Math.max(1, pw / 20);
			for (int i = 0; i < roles.length; i++) {
				int ix = px + (i % perRow) * 20;
				int iy = y + (i / perRow) * 20;
				Role r = roles[i];
				boolean hover = mouseX >= ix && mouseX < ix + 18 && mouseY >= iy && mouseY < iy + 18;
				slot(ctx, ix, iy, hover);
				NodeIconRenderer.drawRole(ctx, r, NodeIconRenderer.Badge.CORE, ix + 1, iy + 1);
				if (hover) {
					tip.set(List.of(Text.translatable(r.translationKey()),
							Text.translatable("screen.aptitudes.guide.role.click").formatted(Formatting.GRAY)));
				}
				hits.add(new Hit(ix, iy, 18, 18, () -> openRole(r)));
			}
			y += ((roles.length + perRow - 1) / perRow) * 20 + 4;
		} else if (!items.isEmpty()) {
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

	private void renderAlchemy(DrawContext ctx, int mouseX, int mouseY, Tooltip tip) {
		int px = pageX(), pw = pageW(), start = top + 63, bottom = top + H - 12;
		List<OrderedText> lines = textRenderer.wrapLines(Text.translatable("guide.aptitudes.alchemy.body"), pw - 10);
		int total = lines.size() * 10 + 8 + Math.max(1, brewing.size()) * 26 + 26;
		int visible = bottom - start;
		int maxScroll = Math.max(0, total - visible);
		scroll = MathHelper.clamp(scroll, 0, maxScroll);
		int y = start - (int) scroll;
		ctx.enableScissor(px - 2, start, px + pw, bottom);
		for (OrderedText line : lines) { ctx.drawText(textRenderer, line, px, y, UiStyle.TEXT, false); y += 10; }
		y += 8;
		if (brewing.isEmpty()) {
			ctx.drawText(textRenderer, UiStyle.fit(textRenderer,
					Text.translatable("screen.aptitudes.guide.no_recipes_synced"), pw - 10), px, y, UiStyle.MUTED, false);
			y += 26;
		}
		for (Brew brew : brewing) {
			ItemStack[] stacks = {brew.input(), brew.ingredient(), brew.output()};
			int[] offsets = {4, 40, 94};
			ctx.drawText(textRenderer, Text.literal("+"), px + 27, y + 5, UiStyle.GOLD, false);
			arrow(ctx, px + 67, y + 5);
			for (int i = 0; i < 3; i++) {
				int x = px + offsets[i];
				slot(ctx, x, y, false);
				ctx.drawItem(stacks[i], x + 1, y + 1);
				if (mouseY >= Math.max(start, y) && mouseY < Math.min(bottom, y + SLOT)
						&& mouseX >= x && mouseX < x + SLOT) tip.item(stacks[i]);
			}
			Text name = brew.output().getName();
			ctx.drawText(textRenderer, UiStyle.fit(textRenderer, name, pw - 120), px + 116, y + 5, UiStyle.TEXT, false);
			y += 26;
		}
		// La extracción se consulta en la ficha normal y su receta sincronizada.
		slot(ctx, px + 4, y, false);
		ctx.drawItem(new ItemStack(ModItems.HERB_PETALS), px + 5, y + 1);
		ctx.drawText(textRenderer, UiStyle.fit(textRenderer, ModItems.HERB_PETALS.getName(), pw - 30), px + 26, y + 5, UiStyle.TEXT, false);
		if (y >= start && y + SLOT <= bottom) {
			hits.add(new Hit(px + 4, y, SLOT, SLOT, () -> openItem(ModItems.HERB_PETALS, true)));
			if (mouseX >= px + 4 && mouseX < px + 4 + SLOT && mouseY >= y && mouseY < y + SLOT) tip.item(new ItemStack(ModItems.HERB_PETALS));
		}
		ctx.disableScissor();
		scrollBar(ctx, px + pw - 2, start, bottom, scroll, maxScroll, visible, total);
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
		star(ctx, "item:" + Registries.ITEM.getId(item), px + pw - 10, top + 26, mouseX, mouseY, tip);

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
			boolean shapeless = isShapeless(recipe);
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

	// ------------------------------------------------------- Bestiario (G1)

	private static final BestiaryRules.Kind[] SECTIONS = {BestiaryRules.Kind.CREATURE, BestiaryRules.Kind.ELITE, BestiaryRules.Kind.BOSS};

	private void renderBestiary(DrawContext ctx, int mouseX, int mouseY, Tooltip tip) {
		int px = pageX(), pw = pageW();
		Text count = Text.translatable("screen.aptitudes.guide.bestiary.count",
				BestiaryRules.unlocked(ClientGrimoire.kills(), null), BestiaryRules.total(null));
		int start = header(ctx, "bestiary", Text.translatable("screen.aptitudes.guide.tab.bestiary"), count,
				new ItemStack(Items.SKELETON_SKULL), null, mouseX, mouseY, tip);
		int bottom = top + H - 12;
		int perRow = Math.max(1, (pw - 6) / 20);
		int total = 0;
		for (BestiaryRules.Kind kind : SECTIONS) {
			int n = BestiaryRules.total(kind);
			total += 12 + ((n + perRow - 1) / perRow) * 20 + 4;
		}
		int visible = bottom - start;
		int maxScroll = Math.max(0, total - visible);
		scroll = MathHelper.clamp(scroll, 0, maxScroll);
		int y = start - (int) scroll;
		ctx.enableScissor(px - 2, start, px + pw, bottom);
		for (BestiaryRules.Kind kind : SECTIONS) {
			ctx.drawText(textRenderer, Text.translatable("bestiary.aptitudes.section." + kind.name().toLowerCase(Locale.ROOT)),
					px, y + 2, UiStyle.GOLD, false);
			y += 12;
			int i = 0;
			for (BestiaryRules.Entry entry : BestiaryRules.entries()) {
				if (entry.kind() != kind) continue;
				int ix = px + (i % perRow) * 20;
				int iy = y + (i / perRow) * 20;
				i++;
				boolean open = ClientGrimoire.unlocked(entry.id());
				boolean inside = mouseY >= Math.max(start, iy) && mouseY < Math.min(bottom, iy + 18);
				boolean hover = inside && mouseX >= ix && mouseX < ix + 18;
				slot(ctx, ix, iy, hover && open);
				if (open) {
					ctx.drawItem(icon(entry), ix + 1, iy + 1);
				} else {
					ctx.fill(ix + 1, iy + 1, ix + 17, iy + 17, 0xFF0B0D13);
					ctx.drawText(textRenderer, "?", ix + 7, iy + 5, UiStyle.MUTED, false);
				}
				if (hover) {
					tip.set(open ? List.of(Text.translatable(BestiaryRules.nameKey(entry)),
							Text.translatable("bestiary.aptitudes.kills", ClientGrimoire.kills(entry.id())).formatted(Formatting.GRAY))
							: List.of(Text.translatable("bestiary.aptitudes.locked"),
							Text.translatable("bestiary.aptitudes.locked.hint").formatted(Formatting.GRAY)));
				}
				if (open && iy >= start && iy + 18 <= bottom) hits.add(new Hit(ix, iy, 18, 18, () -> openBeast(entry)));
			}
			y += ((i + perRow - 1) / perRow) * 20 + 4;
		}
		ctx.disableScissor();
		scrollBar(ctx, px + pw - 2, start, bottom, scroll, maxScroll, visible, total);
	}

	private static ItemStack icon(BestiaryRules.Entry entry) {
		Item item = Registries.ITEM.get(Identifier.of(entry.icon()));
		return new ItemStack(item == Items.AIR ? Items.BOOK : item);
	}

	/** Carta de criatura (v67, G2): retrato, estadísticas, consejos, botín y lore, revelados según las muertes. */
	private void renderBeast(DrawContext ctx, int mouseX, int mouseY, Tooltip tip) {
		BestiaryRules.Entry entry = beast;
		if (entry == null) return;
		int px = pageX(), pw = pageW(), bottom = top + H - 10;
		Text back = Text.translatable("screen.aptitudes.guide.back");
		boolean backHover = mouseX >= px && mouseX < px + textRenderer.getWidth(back) + 4 && mouseY >= top + 25 && mouseY < top + 35;
		ctx.drawText(textRenderer, back, px, top + 26, backHover ? UiStyle.GOLD : UiStyle.MUTED, false);
		hits.add(new Hit(px, top + 25, textRenderer.getWidth(back) + 4, 10, this::goBack));
		star(ctx, "beast:" + entry.id(), px + pw - 10, top + 26, mouseX, mouseY, tip);

		int line = 10;
		int cy = top + 38;
		GuideCardLayout.Card card = GuideCardLayout.layout(pw, bottom - cy, line);
		int kills = ClientGrimoire.kills(entry.id());
		BestiaryRules.Kind kind = entry.kind();

		// Retrato: el modelo de la criatura (o su icono) con un sello de muertes y región debajo.
		GuideCardLayout.Rect pr = card.portrait();
		int bx = px + pr.x(), by = cy + pr.y();
		GuideUi.portrait(ctx, bx, by, pr.w(), pr.h());
		if (!drawModel(ctx, entry, bx + 3, by + 3, bx + pr.w() - 3, by + pr.h() - 3, mouseX, mouseY)) {
			bigItem(ctx, icon(entry), bx + pr.w() / 2 - 16, by + pr.h() / 2 - 16);
		}
		GuideCardLayout.Rect sr = card.seal();
		int sx = px + sr.x(), sy = cy + sr.y();
		ctx.drawText(textRenderer, UiStyle.fit(textRenderer, Text.translatable("bestiary.aptitudes.kills", kills), sr.w()),
				sx, sy, UiStyle.TEXT, false);
		ctx.drawText(textRenderer, UiStyle.fit(textRenderer, Text.translatable("bestiary.aptitudes.region."
				+ entry.region().name().toLowerCase(Locale.ROOT)), sr.w()), sx, sy + line, UiStyle.MUTED, false);

		// Nombre sobre la cinta y tipo.
		int tx = px + card.textX(), tw = card.textW();
		GuideUi.ribbon(ctx, tx, cy + card.ribbonY(), tw, GuideCardLayout.RIBBON_HEIGHT);
		ctx.drawTextWithShadow(textRenderer, UiStyle.fit(textRenderer,
				Text.translatable(BestiaryRules.nameKey(entry)).formatted(Formatting.BOLD), tw - 8), tx + 4,
				cy + card.ribbonY() + 3, UiStyle.GOLD);
		ctx.drawText(textRenderer, UiStyle.fit(textRenderer, Text.translatable("bestiary.aptitudes.kind." + kind.name().toLowerCase(Locale.ROOT)),
				tw), tx, cy + card.kindY(), UiStyle.MUTED, false);

		// Estadísticas.
		int y = cy + card.statsY();
		if (BestiaryRules.revealed(kind, BestiaryRules.Section.STATS, kills)) {
			int shown = 0;
			for (Text fact : facts(entry)) {
				if (shown >= GuideCardLayout.STATS_LINES) break;
				ctx.drawText(textRenderer, UiStyle.fit(textRenderer, fact, tw), tx, y + shown * line, UiStyle.UP, false);
				shown++;
			}
		} else {
			locked(ctx, tx, y, tw, kind, BestiaryRules.Section.STATS, kills);
		}
		GuideUi.divider(ctx, tx, cy + card.tipsY() - 2, tw);

		// Consejos y botín.
		boolean tipsOpen = BestiaryRules.revealed(kind, BestiaryRules.Section.TIPS, kills);
		ctx.drawText(textRenderer, Text.translatable("bestiary.aptitudes.section.tips"), tx, cy + card.tipsY(), UiStyle.GOLD, false);
		if (tipsOpen) {
			int ty = cy + card.tipsY() + line, n = 0;
			for (OrderedText l : textRenderer.wrapLines(Text.translatable(entry.tipKey()), tw)) {
				if (n++ >= card.tipsLines()) break;
				ctx.drawText(textRenderer, l, tx, ty, UiStyle.TEXT, false);
				ty += line;
			}
		} else {
			locked(ctx, tx, cy + card.tipsY() + line, tw, kind, BestiaryRules.Section.TIPS, kills);
		}
		ctx.drawText(textRenderer, Text.translatable("bestiary.aptitudes.drops"), tx, cy + card.lootY(), UiStyle.GOLD, false);
		if (tipsOpen) {
			int dx = tx, dy = cy + card.lootY() + line;
			for (String id : entry.drops()) {
				Item item = Registries.ITEM.get(Identifier.of(id));
				if (item == Items.AIR || dx + 18 > tx + tw) continue;
				boolean hover = mouseX >= dx && mouseX < dx + 18 && mouseY >= dy && mouseY < dy + 18;
				slot(ctx, dx, dy, hover);
				ItemStack stack = new ItemStack(item);
				ctx.drawItem(stack, dx + 1, dy + 1);
				if (hover) tip.item(stack);
				if (isModItem(item)) hits.add(new Hit(dx, dy, 18, 18, () -> openItem(item, true)));
				dx += 20;
			}
		} else {
			locked(ctx, tx, cy + card.lootY() + line, tw, kind, BestiaryRules.Section.TIPS, kills);
		}

		// Lore.
		GuideUi.divider(ctx, tx, cy + card.loreY() - 2, tw);
		ctx.drawText(textRenderer, Text.translatable("bestiary.aptitudes.section.lore"), tx, cy + card.loreY(), UiStyle.GOLD, false);
		if (BestiaryRules.revealed(kind, BestiaryRules.Section.LORE, kills)) {
			int ly = cy + card.loreY() + line, n = 0;
			for (OrderedText l : textRenderer.wrapLines(Text.translatable(entry.loreKey()).formatted(Formatting.ITALIC), tw)) {
				if (n++ >= card.loreLines()) break;
				ctx.drawText(textRenderer, l, tx, ly, UiStyle.MUTED, false);
				ly += line;
			}
		} else {
			locked(ctx, tx, cy + card.loreY() + line, tw, kind, BestiaryRules.Section.LORE, kills);
		}
	}

	/** Pista de una parte aún no revelada: cuántas muertes faltan. */
	private void locked(DrawContext ctx, int x, int y, int w, BestiaryRules.Kind kind, BestiaryRules.Section section, int kills) {
		int missing = BestiaryRules.killsMissing(kind, section, kills);
		Text hint = Text.translatable(missing == 1 ? "bestiary.aptitudes.reveal.one" : "bestiary.aptitudes.reveal", missing);
		ctx.drawText(textRenderer, UiStyle.fit(textRenderer, hint, w), x, y, UiStyle.MUTED, false);
	}

	/** Estadísticas base del juego y debilidades por etiqueta (la lista se adapta a los datapacks). */
	private List<Text> facts(BestiaryRules.Entry entry) {
		List<Text> out = new ArrayList<>();
		EntityType<?> type = entry.entity() == null ? null : Registries.ENTITY_TYPE.get(Identifier.of(entry.entity()));
		if (type == null) return out;
		if (entry.bossKind() != null) {
			out.add(Text.translatable("bestiary.aptitudes.boss_stats"));
		} else if (entry.kind() == BestiaryRules.Kind.CREATURE || entry.kind() == BestiaryRules.Kind.BOSS) {
			@SuppressWarnings("unchecked")
			EntityType<? extends LivingEntity> living = (EntityType<? extends LivingEntity>) type;
			if (DefaultAttributeRegistry.hasDefinitionFor(living)) {
				DefaultAttributeContainer attrs = DefaultAttributeRegistry.get(living);
				out.add(Text.translatable("bestiary.aptitudes.stats", stat(attrs, EntityAttributes.GENERIC_MAX_HEALTH),
						stat(attrs, EntityAttributes.GENERIC_ARMOR), stat(attrs, EntityAttributes.GENERIC_ATTACK_DAMAGE)));
			}
		}
		if (type.isIn(EntityTypeTags.SENSITIVE_TO_SMITE)) out.add(Text.translatable("bestiary.aptitudes.weak.smite"));
		if (type.isIn(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) out.add(Text.translatable("bestiary.aptitudes.weak.bane"));
		if (type.isIn(EntityTypeTags.SENSITIVE_TO_IMPALING)) out.add(Text.translatable("bestiary.aptitudes.weak.impaling"));
		if (type.isFireImmune()) out.add(Text.translatable("bestiary.aptitudes.fire_immune"));
		return out;
	}

	private static String stat(DefaultAttributeContainer attrs, RegistryEntry<EntityAttribute> attribute) {
		return attrs.has(attribute) ? UiStyle.fmt(attrs.getBaseValue(attribute)) : "—";
	}

	/** Dibuja la criatura con el ratón como mirada; si el renderizador falla, se recuerda y se usa el icono. */
	private boolean drawModel(DrawContext ctx, BestiaryRules.Entry entry, int x1, int y1, int x2, int y2, int mouseX, int mouseY) {
		if (entry.entity() == null || client == null || client.world == null || noModel.contains(entry.entity())) return false;
		LivingEntity model = models.computeIfAbsent(entry.entity(), id -> {
			Entity created = Registries.ENTITY_TYPE.get(Identifier.of(id)).create(client.world);
			return created instanceof LivingEntity living ? living : null;
		});
		if (model == null) {
			noModel.add(entry.entity());
			return false;
		}
		float tallest = Math.max(model.getHeight(), model.getWidth() * 0.9f);
		int size = MathHelper.clamp(Math.round((y2 - y1 - 14) / Math.max(0.5f, tallest)), 4, 30);
		try {
			InventoryScreen.drawEntity(ctx, x1, y1, x2, y2, size, 0.0625f, mouseX, mouseY, model);
			return true;
		} catch (RuntimeException e) {
			Aptitudes.LOGGER.warn("No se pudo dibujar {} en el bestiario", entry.entity(), e);
			noModel.add(entry.entity());
			return false;
		}
	}

	// --------------------------------------------------------- Progreso (G3)

	private void renderProgress(DrawContext ctx) {
		int px = pageX(), pw = pageW();
		var kills = ClientGrimoire.kills();
		RoleSyncPayload data = ClientRoleState.data();
		List<String> roles = new ArrayList<>();
		for (Role r : Role.values()) roles.add(r.key());
		GuideRules.PathProgress paths = GuideRules.pathProgress(
				id -> data == null ? 0 : data.ranks().getOrDefault(id, 0), roles);
		long[][] rows = {
				{ClientGrimoire.achievementsDone(), ClientGrimoire.achievementsTotal()},
				{BestiaryRules.unlocked(kills, null), BestiaryRules.total(null)},
				{BestiaryRules.unlocked(kills, BestiaryRules.Kind.BOSS), BestiaryRules.total(BestiaryRules.Kind.BOSS)},
				{paths.steps(), paths.maxSteps()},
				{paths.keystones(), paths.maxKeystones()}};
		String[] labels = {"achievements", "bestiary", "bosses", "steps", "keystones"};
		int sum = 0;
		for (long[] row : rows) sum += GuideRules.percent(row[0], row[1]);
		int overall = sum / rows.length;
		Text subtitle = ClientGrimoire.received()
				? Text.translatable("screen.aptitudes.guide.progress.total", overall)
				: Text.translatable("screen.aptitudes.guide.progress.waiting");
		int y = header(ctx, "progress", Text.translatable("screen.aptitudes.guide.tab.progress"), subtitle,
				new ItemStack(Items.CLOCK), null, 0, 0, new Tooltip());
		for (int i = 0; i < rows.length; i++) {
			if (y + 20 > top + H - 8) break;
			long done = rows[i][0], total = rows[i][1];
			int pct = GuideRules.percent(done, total);
			ctx.drawText(textRenderer, UiStyle.fit(textRenderer, Text.translatable("screen.aptitudes.guide.progress." + labels[i]), pw - 90),
					px, y, UiStyle.TEXT, false);
			Text value = Text.translatable("screen.aptitudes.guide.progress.value", done, total, pct);
			ctx.drawText(textRenderer, value, px + pw - textRenderer.getWidth(value), y, UiStyle.MUTED, false);
			int barY = y + 11, barW = pw;
			ctx.fill(px, barY, px + barW, barY + 5, 0xFF1B1F2B);
			ctx.fill(px, barY, px + barW * pct / 100, barY + 5, pct >= 100 ? UiStyle.GOOD : UiStyle.GOLD);
			ctx.drawBorder(px - 1, barY - 1, barW + 2, 7, UiStyle.BORDER);
			y += 24;
		}
	}

	// ------------------------------------------------- Buscador global (G2)

	private List<Result> findResults(String query) {
		List<String> terms = GuideRules.terms(query);
		if (terms.isEmpty()) return List.of();
		record Scored(Result result, int score) {}
		List<Scored> found = new ArrayList<>();
		for (int i = 0; i < CHAPTERS.size(); i++) {
			Chapter c = CHAPTERS.get(i);
			Text name = Text.translatable("guide.aptitudes." + c.id() + ".title");
			int score = GuideRules.score(terms, GuideRules.normalize(name.getString()),
					GuideRules.normalize(Text.translatable("guide.aptitudes." + c.id() + ".body").getString()));
			final int index = i;
			if (score > 0) found.add(new Scored(new Result(name, new ItemStack(c.icon()), "chapter", () -> openChapter(index)), score));
		}
		for (Item item : Registries.ITEM) {
			if (!isModItem(item)) continue;
			Text name = item.getName();
			int score = GuideRules.score(terms, GuideRules.normalize(name.getString()), "");
			if (score > 0) found.add(new Scored(new Result(name, new ItemStack(item), "item", () -> openItem(item, true)), score));
		}
		for (BestiaryRules.Entry entry : BestiaryRules.entries()) {
			if (!ClientGrimoire.unlocked(entry.id())) continue; // las páginas cerradas no se revelan
			Text name = Text.translatable(BestiaryRules.nameKey(entry));
			int score = GuideRules.score(terms, GuideRules.normalize(name.getString()),
					GuideRules.normalize(Text.translatable(entry.tipKey()).getString()));
			if (score > 0) found.add(new Scored(new Result(name, icon(entry), "beast", () -> openBeast(entry)), score));
		}
		for (Role r : Role.values()) {
			Text name = Text.translatable(r.translationKey());
			StringBuilder body = new StringBuilder();
			for (int path = 1; path <= PathRules.PATHS; path++) body.append(' ').append(Text.translatable(RoleTree.pathKey(r, path)).getString());
			int score = GuideRules.score(terms, GuideRules.normalize(name.getString()), GuideRules.normalize(body.toString()));
			if (score > 0) found.add(new Scored(new Result(name, r.iconStack(), "role", () -> openRole(r)), score));
		}
		found.sort(Comparator.comparingInt(Scored::score).reversed()
				.thenComparing(s -> s.result().title().getString()));
		List<Result> out = new ArrayList<>();
		for (int i = 0; i < found.size() && i < 100; i++) out.add(found.get(i).result());
		return out;
	}

	private void renderSearch(DrawContext ctx, int mouseX, int mouseY, float delta) {
		int px = pageX(), pw = pageW();
		ctx.drawTextWithShadow(textRenderer, Text.translatable("screen.aptitudes.guide.tab.search").formatted(Formatting.BOLD),
				px, top + 28, UiStyle.GOLD);
		finder.setX(px);
		finder.setY(top + 40);
		finder.setWidth(Math.min(200, pw));
		finder.render(ctx, mouseX, mouseY, delta);
		int y = top + 60;
		if (finderQuery.isBlank() || results.isEmpty()) {
			Text hint = Text.translatable(finderQuery.isBlank() ? "screen.aptitudes.guide.search.empty" : "screen.aptitudes.guide.search.none");
			for (OrderedText line : textRenderer.wrapLines(hint, pw)) {
				ctx.drawText(textRenderer, line, px, y, UiStyle.MUTED, false);
				y += 10;
			}
			return;
		}
		renderRows(ctx, y, mouseX, mouseY, results.size(), (i, ry, hover) -> {
			Result r = results.get(i);
			Text kind = Text.translatable("screen.aptitudes.guide.kind." + r.kindKey());
			drawRow(ctx, ry, r.icon(), r.title(), kind, hover);
			return r.open();
		});
	}

	@FunctionalInterface
	private interface RowRenderer {
		/** Dibuja la fila {@code index} y devuelve su acción al hacer clic (o null). */
		@Nullable Runnable draw(int index, int y, boolean hover);
	}

	/** Lista de filas de 20 px con desplazamiento por filas. */
	private void renderRows(DrawContext ctx, int y, int mouseX, int mouseY, int count, RowRenderer renderer) {
		int px = pageX(), pw = pageW(), bottom = top + H - 10;
		int visible = Math.max(1, (bottom - y) / 20);
		int maxScroll = Math.max(0, count - visible);
		scroll = MathHelper.clamp(scroll, 0, maxScroll);
		for (int i = (int) scroll; i < count && i < (int) scroll + visible; i++) {
			int ry = y + (i - (int) scroll) * 20;
			boolean hover = mouseX >= px && mouseX < px + pw - 4 && mouseY >= ry && mouseY < ry + 19;
			Runnable action = renderer.draw(i, ry, hover);
			if (action != null) hits.add(new Hit(px, ry, pw - 18, 19, action));
		}
		scrollBar(ctx, px + pw - 2, y, bottom, scroll, maxScroll, visible, count);
	}

	private void drawRow(DrawContext ctx, int y, ItemStack icon, Text title, Text kind, boolean hover) {
		int px = pageX(), pw = pageW();
		if (hover) ctx.fill(px, y, px + pw - 4, y + 19, 0x30C9A54C);
		ctx.drawItem(icon, px + 1, y + 1);
		int kindW = textRenderer.getWidth(kind);
		ctx.drawText(textRenderer, UiStyle.fit(textRenderer, title, pw - kindW - 40), px + 21, y + 6, hover ? UiStyle.GOLD : UiStyle.TEXT, false);
		ctx.drawText(textRenderer, kind, px + pw - kindW - 20, y + 6, UiStyle.MUTED, false);
	}

	// ------------------------------------------------------ Marcadores (G2)

	/** Resuelve un marcador guardado; null si ya no existe (se puede quitar igualmente). */
	@Nullable
	private Result resolve(String bookmark) {
		int colon = bookmark.indexOf(':');
		String kind = bookmark.substring(0, colon);
		String id = bookmark.substring(colon + 1);
		switch (kind) {
			case "chapter" -> {
				for (int i = 0; i < CHAPTERS.size(); i++) {
					Chapter c = CHAPTERS.get(i);
					final int index = i;
					if (c.id().equals(id)) return new Result(Text.translatable("guide.aptitudes." + id + ".title"),
							new ItemStack(c.icon()), "chapter", () -> openChapter(index));
				}
			}
			case "item" -> {
				Identifier itemId = Identifier.tryParse(id);
				Item item = itemId == null ? Items.AIR : Registries.ITEM.get(itemId);
				if (item != Items.AIR) return new Result(item.getName(), new ItemStack(item), "item", () -> openItem(item, true));
			}
			case "beast" -> {
				BestiaryRules.Entry entry = BestiaryRules.byId(id);
				if (entry != null && ClientGrimoire.unlocked(id)) {
					return new Result(Text.translatable(BestiaryRules.nameKey(entry)), icon(entry), "beast", () -> openBeast(entry));
				}
			}
			case "role" -> {
				var found = Role.byKey(id);
				if (found.isPresent()) {
					Role r = found.get();
					return new Result(Text.translatable(r.translationKey()), r.iconStack(), "role", () -> openRole(r));
				}
			}
			default -> { }
		}
		return null;
	}

	private void renderBookmarks(DrawContext ctx, int mouseX, int mouseY) {
		int px = pageX(), pw = pageW();
		List<String> marks = List.copyOf(ClientConfig.get().guideBookmarks);
		int y = header(ctx, "bookmarks", Text.translatable("screen.aptitudes.guide.tab.bookmarks"),
				Text.translatable("screen.aptitudes.guide.bookmarks.count", marks.size(), GuideRules.MAX_BOOKMARKS),
				new ItemStack(Items.NAME_TAG), null, 0, 0, new Tooltip());
		if (marks.isEmpty()) {
			for (OrderedText line : textRenderer.wrapLines(Text.translatable("screen.aptitudes.guide.bookmarks.empty"), pw)) {
				ctx.drawText(textRenderer, line, px, y, UiStyle.MUTED, false);
				y += 10;
			}
			return;
		}
		renderRows(ctx, y, mouseX, mouseY, marks.size(), (i, ry, hover) -> {
			String mark = marks.get(i);
			Result r = resolve(mark);
			if (r == null) {
				drawRow(ctx, ry, new ItemStack(Items.BARRIER), Text.literal(mark).formatted(Formatting.DARK_GRAY),
						Text.translatable("screen.aptitudes.guide.kind.missing"), hover);
			} else {
				drawRow(ctx, ry, r.icon(), r.title(), Text.translatable("screen.aptitudes.guide.kind." + r.kindKey()), hover);
			}
			// Quitar: la «×» de la derecha.
			int xx = px + pw - 14;
			boolean overX = mouseX >= xx - 2 && mouseX < xx + 8 && mouseY >= ry && mouseY < ry + 19;
			ctx.drawText(textRenderer, "×", xx, ry + 6, overX ? UiStyle.DOWN : UiStyle.MUTED, false);
			hits.add(new Hit(xx - 2, ry, 10, 19, () -> {
				ClientConfig.get().guideBookmarks = GuideRules.toggle(ClientConfig.get().guideBookmarks, mark);
				ClientConfig.save();
			}));
			return r == null ? null : r.open();
		});
	}

	// -------------------------------------------- Sendas de un rol (G5)

	private void renderRole(DrawContext ctx, int mouseX, int mouseY, Tooltip tip) {
		Role r = role;
		if (r == null) return;
		int px = pageX(), pw = pageW();
		Text back = Text.translatable("screen.aptitudes.guide.back");
		boolean backHover = mouseX >= px && mouseX < px + textRenderer.getWidth(back) + 4 && mouseY >= top + 25 && mouseY < top + 35;
		ctx.drawText(textRenderer, back, px, top + 26, backHover ? UiStyle.GOLD : UiStyle.MUTED, false);
		hits.add(new Hit(px, top + 25, textRenderer.getWidth(back) + 4, 10, this::goBack));
		star(ctx, "role:" + r.key(), px + pw - 10, top + 26, mouseX, mouseY, tip);

		int y = top + 38;
		NodeIconRenderer.drawRole(ctx, r, NodeIconRenderer.Badge.CORE, px, y);
		ctx.drawTextWithShadow(textRenderer, UiStyle.fit(textRenderer,
				Text.translatable("screen.aptitudes.guide.role.paths", Text.translatable(r.translationKey())).formatted(Formatting.BOLD),
				pw - 22), px + 20, y + 4, UiStyle.GOLD);
		y += 20;
		UiStyle.line(ctx, px, y, px + pw, y, UiStyle.GOLD_DARK);
		y += 4;
		RoleSyncPayload data = ClientRoleState.data();
		java.util.function.ToIntFunction<String> rankOf = id -> data == null ? 0 : data.ranks().getOrDefault(id, 0);
		int chosen = PathRules.chosenPath(rankOf, r.key());
		for (int path = 1; path <= PathRules.PATHS; path++) {
			if (y + 30 > top + H - 6) break;
			boolean mine = chosen == path;
			Text name = Text.translatable(RoleTree.pathKey(r, path));
			if (mine) name = name.copy().append(" · ").append(Text.translatable("screen.aptitudes.guide.role.chosen"));
			ctx.drawText(textRenderer, UiStyle.fit(textRenderer, name, pw), px, y, mine ? UiStyle.GOLD : UiStyle.TEXT, false);
			y += 10;
			int x = px;
			for (PathRules.Step step : PathRules.Step.values()) {
				String id = PathRules.nodeId(r.key(), path, step);
				RoleNode node = RoleTree.get(id);
				if (node == null) continue;
				boolean owned = rankOf.applyAsInt(id) > 0;
				boolean hover = mouseX >= x && mouseX < x + 18 && mouseY >= y && mouseY < y + 18;
				ctx.fill(x, y, x + 18, y + 18, 0xFF1B1F2B);
				NodeIconRenderer.draw(ctx, node, x + 1, y + 1);
				ctx.getMatrices().push();
				ctx.getMatrices().translate(0, 0, 200); // encima del icono (drawItem dibuja más arriba)
				if (!owned) ctx.fill(x + 1, y + 1, x + 17, y + 17, 0x70000000);
				ctx.drawBorder(x, y, 18, 18, owned ? UiStyle.GOLD : step == PathRules.Step.S7 ? 0xFF7E57C2 : UiStyle.BORDER);
				ctx.getMatrices().pop();
				if (hover) {
					List<OrderedText> lines = new ArrayList<>();
					lines.add(Text.translatable("node.aptitudes." + id).formatted(Formatting.GOLD).asOrderedText());
					lines.add(Text.translatable("screen.aptitudes.guide.role.step", step.suffix().toUpperCase(Locale.ROOT), step.level())
							.formatted(Formatting.GRAY).asOrderedText());
					lines.addAll(textRenderer.wrapLines(Text.translatable("node.aptitudes." + id + ".desc"), 200));
					tip.ordered(lines);
				}
				x += 19;
			}
			y += 22;
		}
	}

	private static boolean isShapeless(Recipe<?> recipe) {
		return recipe instanceof ShapelessRecipe || recipe instanceof GuildShapelessRecipe;
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
		@Nullable private List<OrderedText> ordered;
		@Nullable private ItemStack stack;

		void set(List<Text> lines) {
			this.lines = lines;
			this.stack = null;
			this.ordered = null;
		}

		void item(ItemStack stack) {
			this.stack = stack;
			this.lines = null;
			this.ordered = null;
		}

		/** Líneas ya ajustadas al ancho (descripciones largas). */
		void ordered(List<OrderedText> ordered) {
			this.ordered = ordered;
			this.lines = null;
			this.stack = null;
		}

		void draw(DrawContext ctx, int mx, int my) {
			if (stack != null) ctx.drawItemTooltip(textRenderer, stack, mx, my);
			else if (lines != null) ctx.drawTooltip(textRenderer, lines, mx, my);
			else if (ordered != null) ctx.drawOrderedTooltip(textRenderer, ordered, mx, my);
		}
	}

	// ------------------------------------------------------- Navegación

	private Loc here() {
		return new Loc(page, chapter, viewing, beast, role);
	}

	/** Cambia de página; con {@code remember} la actual queda en el historial (Volver). */
	private void show(Loc loc, boolean remember) {
		if (remember && page != Page.CHAPTER) history.push(here());
		if (history.size() > 64) history.removeLast();
		page = loc.page();
		chapter = MathHelper.clamp(loc.chapter(), 0, CHAPTERS.size() - 1);
		viewing = loc.page() == Page.ITEM ? loc.item() : null;
		beast = loc.page() == Page.BEAST ? loc.beast() : null;
		role = loc.page() == Page.ROLE ? loc.role() : null;
		recipeIndex = 0;
		scroll = 0;
		dropSearchFocus();
		pageSound();
	}

	private void openChapter(int index) {
		if (index == chapter && page == Page.CHAPTER) return;
		history.clear();
		lastChapter = MathHelper.clamp(index, 0, CHAPTERS.size() - 1);
		show(new Loc(Page.CHAPTER, index, null, null, null), false);
	}

	/** Pestañas: empiezan un recorrido nuevo (sin historial). */
	private void openPage(Page target) {
		if (page == target) return;
		history.clear();
		show(new Loc(target, chapter, null, null, null), false);
	}

	private void openItem(Item item, boolean remember) {
		show(new Loc(Page.ITEM, chapter, item, null, null), remember);
	}

	private void openBeast(BestiaryRules.Entry entry) {
		if (!ClientGrimoire.unlocked(entry.id())) return;
		show(new Loc(Page.BEAST, chapter, null, entry, null), true);
	}

	private void openRole(Role target) {
		show(new Loc(Page.ROLE, chapter, null, null, target), true);
	}

	private void goBack() {
		Loc previous = history.isEmpty() ? new Loc(Page.CHAPTER, chapter, null, null, null) : history.pop();
		show(previous, false);
	}

	/** Los buscadores nunca conservan el foco fuera de su página (si no, se «comerían» las teclas). */
	private void dropSearchFocus() {
		if (search == null || finder == null) return; // antes de init (forItem)
		if (getFocused() == search || getFocused() == finder) setFocused(null);
		search.setFocused(false);
		finder.setFocused(false);
		if (page == Page.SEARCH) setFocused(finder);
	}

	@Override
	public void close() {
		Screen back = returnTo == null ? null : returnTo.get();
		if (back != null && client != null) client.setScreen(back);
		else super.close();
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
		if (page == Page.SEARCH && finder.mouseClicked(mouseX, mouseY, button)) {
			setFocused(finder);
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
		if (button == 3 && hasBack()) { // botón «atrás» del ratón
			goBack();
			return true;
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	/** Páginas con «Volver». */
	private boolean hasBack() {
		return page == Page.ITEM || page == Page.BEAST || page == Page.ROLE;
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		double step = alchemyMode() || page == Page.BESTIARY ? 18 : recipesMode() || page == Page.SEARCH || page == Page.BOOKMARKS ? 1 : 2;
		scroll -= verticalAmount * step;
		return true;
	}

	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		if (typing() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
			TextFieldWidget field = recipesMode() ? search : finder;
			return field.keyPressed(keyCode, scanCode, modifiers) || super.keyPressed(keyCode, scanCode, modifiers);
		}
		switch (keyCode) {
			case GLFW.GLFW_KEY_DOWN -> {
				openChapter(Math.min(CHAPTERS.size() - 1, chapter + (page == Page.CHAPTER ? 1 : 0)));
				return true;
			}
			case GLFW.GLFW_KEY_UP -> {
				openChapter(Math.max(0, chapter - (page == Page.CHAPTER ? 1 : 0)));
				return true;
			}
			case GLFW.GLFW_KEY_BACKSPACE -> {
				if (hasBack()) {
					goBack();
					return true;
				}
			}
			case GLFW.GLFW_KEY_LEFT -> {
				if (page == Page.ITEM) {
					recipeIndex--;
					return true;
				}
			}
			case GLFW.GLFW_KEY_RIGHT -> {
				if (page == Page.ITEM) {
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
		if (page == Page.SEARCH && finder.isFocused()) return finder.charTyped(chr, modifiers);
		return super.charTyped(chr, modifiers);
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
