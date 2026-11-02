package dev.aptitudes.client.screen;

import dev.aptitudes.item.ModItems;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.math.MathHelper;

import java.util.List;

/**
 * Pantalla del Grimorio: capítulos a la izquierda; a la derecha, título, una fila de
 * objetos relacionados (con su nombre al pasar el ratón) y el texto, con desplazamiento.
 * Todo el texto está en el archivo de idioma ({@code guide.aptitudes.<capítulo>.title/.body}).
 */
public class GuideScreen extends Screen {
	private record Chapter(String id, List<Item> items) {}

	private static final List<Chapter> CHAPTERS = List.of(
			new Chapter("start", List.of(ModItems.GUIDE_BOOK)),
			new Chapter("keys", List.of()),
			new Chapter("bonfire", List.of(dev.aptitudes.block.ModBlocks.BONFIRE_ITEM, ModItems.ESTUS_FLASK, ModItems.ETHER_FLASK)),
			new Chapter("attributes", List.of(Items.EXPERIENCE_BOTTLE)),
			new Chapter("skills", List.of(Items.IRON_PICKAXE, Items.IRON_SWORD, Items.BOW, Items.FEATHER)),
			new Chapter("roles", List.of(ModItems.REBIRTH_OFFERING)),
			new Chapter("tree", List.of(Items.BOOK)),
			new Chapter("evolution", List.of(ModItems.MASTERY_EMBLEM, ModItems.ULTIMATE_CORE)),
			new Chapter("weapons", List.of(Items.DIAMOND_SWORD, Items.NETHERITE_SWORD)),
			new Chapter("elements", List.of(ModItems.ELEMENTAL_ORB)),
			new Chapter("armor", List.of(ModItems.SOUL_SHARD, Items.IRON_CHESTPLATE)),
			new Chapter("soul_items", List.of(ModItems.SOUL_SHARD, ModItems.SOUL_PICKAXE, ModItems.SOUL_AXE, ModItems.SOUL_SHOVEL, ModItems.SOUL_HOE)),
			new Chapter("accessories", List.of(ModItems.ELITE_ESSENCE, ModItems.CHAMPION_HEART)),
			new Chapter("elites", List.of(ModItems.ELITE_ESSENCE, ModItems.CHAMPION_HEART)),
			new Chapter("structures", List.of(Items.CHEST, Items.SPAWNER)),
			new Chapter("guild", List.of(dev.aptitudes.world.GuildVillagers.GUILD_DESK_ITEM, ModItems.ARMORY_VOUCHER)),
			new Chapter("recipes", List.of(Items.CRAFTING_TABLE)),
			new Chapter("drops", List.of(Items.ZOMBIE_HEAD, Items.SKELETON_SKULL)));

	private static final int W = 360;
	private static final int H = 240;
	private static final int LIST_W = 110;
	private int left;
	private int top;
	private int chapter;
	private double scroll;

	public GuideScreen() {
		super(Text.translatable("screen.aptitudes.guide"));
	}

	@Override
	protected void init() {
		left = (width - W) / 2;
		top = (height - H) / 2;
	}

	@Override
	public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
		super.render(ctx, mouseX, mouseY, delta);
		UiStyle.window(ctx, left, top, W, H);
		ctx.drawTextWithShadow(textRenderer, title, left + 10, top + 8, UiStyle.GOLD);
		// Capítulos
		for (int i = 0; i < CHAPTERS.size(); i++) {
			int y = top + 24 + i * 11;
			boolean sel = i == chapter;
			boolean hover = mouseX >= left + 6 && mouseX < left + 6 + LIST_W && mouseY >= y && mouseY < y + 11;
			if (sel || hover) ctx.fill(left + 6, y, left + 6 + LIST_W, y + 11, sel ? 0x60C9A54C : 0x30303850);
			UiStyle.label(ctx, textRenderer, Text.translatable("guide.aptitudes." + CHAPTERS.get(i).id() + ".title"),
					left + 9, y + 2, LIST_W - 6, sel ? UiStyle.GOLD : UiStyle.TEXT);
		}
		// Página
		int px = left + LIST_W + 14;
		int pw = W - LIST_W - 22;
		UiStyle.panel(ctx, px - 4, top + 22, pw + 8, H - 30);
		Chapter c = CHAPTERS.get(chapter);
		UiStyle.header(ctx, textRenderer, Text.translatable("guide.aptitudes." + c.id() + ".title"), px, top + 26, pw);
		int y = top + 42;
		Text hovered = null;
		if (!c.items().isEmpty()) {
			int ix = px;
			for (Item item : c.items()) {
				ctx.drawItem(new ItemStack(item), ix, y);
				if (mouseX >= ix && mouseX < ix + 16 && mouseY >= y && mouseY < y + 16) hovered = item.getName();
				ix += 20;
			}
			y += 20;
		}
		List<OrderedText> lines = textRenderer.wrapLines(Text.translatable("guide.aptitudes." + c.id() + ".body"), pw - 6);
		int visible = (top + H - 14 - y) / 10;
		int maxScroll = Math.max(0, lines.size() - visible);
		scroll = MathHelper.clamp(scroll, 0, maxScroll);
		ctx.enableScissor(px - 2, y, px + pw + 2, top + H - 12);
		for (int i = (int) scroll; i < lines.size() && i < (int) scroll + visible + 1; i++) {
			ctx.drawText(textRenderer, lines.get(i), px, y + (i - (int) scroll) * 10, UiStyle.TEXT, false);
		}
		ctx.disableScissor();
		if (maxScroll > 0) {
			Text more = Text.translatable("screen.aptitudes.guide.scroll", (int) scroll + 1, maxScroll + 1);
			ctx.drawText(textRenderer, more, px + pw - textRenderer.getWidth(more), top + H - 10, UiStyle.MUTED, false);
		}
		if (hovered != null) ctx.drawTooltip(textRenderer, hovered, mouseX, mouseY);
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		for (int i = 0; i < CHAPTERS.size(); i++) {
			int y = top + 24 + i * 11;
			if (mouseX >= left + 6 && mouseX < left + 6 + LIST_W && mouseY >= y && mouseY < y + 11) {
				chapter = i;
				scroll = 0;
				return true;
			}
		}
		return super.mouseClicked(mouseX, mouseY, button);
	}

	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
		scroll -= verticalAmount * 2;
		return true;
	}

	@Override
	public boolean shouldPause() {
		return false;
	}
}
