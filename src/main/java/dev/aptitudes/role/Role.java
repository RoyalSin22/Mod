package dev.aptitudes.role;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.data.PlayerAttribute;
import dev.aptitudes.item.ModItems;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Roles (clases). El jugador elige uno principal y uno secundario; ambos tienen
 * sus pasivas y activas en funcionamiento, pero el secundario gana XP más
 * despacio. El progreso de los roles no activos se conserva.
 *
 * <p>El icono se resuelve con un {@link Supplier} para no forzar la carga de
 * {@link ModItems} desde este enum (orden de inicialización).</p>
 */
public enum Role {
	BERSERKER(RoleCategory.MARTIAL, 0xC0392B, () -> ModItems.BERSERKER_AXE),
	MAGE(RoleCategory.ARCANE, 0x8E44AD, () -> ModItems.ARCANE_STAFF),
	ASSASSIN(RoleCategory.SHADOW, 0x2C3E50, () -> ModItems.DAGGER),
	HERBALIST(RoleCategory.GUARDIAN, 0x27AE60, () -> ModItems.HERB_POUCH),
	/** Fusión de Tecnólogo y Futurista. */
	ENGINEER(RoleCategory.CRAFT, 0xD35400, () -> ModItems.WRENCH),
	LANCER(RoleCategory.MARTIAL, 0x2980B9, () -> ModItems.SPEAR),
	SWORDSMAN(RoleCategory.MARTIAL, 0xBDC3C7, () -> ModItems.KATANA),
	PALADIN(RoleCategory.GUARDIAN, 0xF1C40F, () -> ModItems.HOLY_HAMMER),
	NECROMANCER(RoleCategory.ARCANE, 0x6C3483, () -> ModItems.NECRO_TOME),
	DRUID(RoleCategory.GUARDIAN, 0x6E8B3D, () -> ModItems.OAK_STAFF),
	MERCHANT(RoleCategory.CRAFT, 0x16A085, () -> ModItems.GUILD_SEAL),
	MONK(RoleCategory.MARTIAL, 0xE67E22, () -> ModItems.HAND_WRAPS),
	ELEMENTALIST(RoleCategory.ARCANE, 0x3498DB, () -> ModItems.ELEMENTAL_ORB),
	/** Se transforma en mobs vanilla y usa sus poderes. */
	SHAPESHIFTER(RoleCategory.SHADOW, 0x9B59B6, () -> ModItems.MIMIC_MASK),
	/** Mago pistolero: revólver arcano, disparos encadenados y balas explosivas. */
	GUNSLINGER(RoleCategory.ARCANE, 0x00BCD4, () -> ModItems.ARCANE_REVOLVER),
	/** Invocador de armas: arsenal de espadas espectrales que orbitan y se disparan. */
	WEAPON_SUMMONER(RoleCategory.CRAFT, 0x90A4AE, () -> ModItems.WEAPON_SIGIL),
	/** Cazador: marcas, trampas, flechas y un halcón. */
	HUNTER(RoleCategory.SHADOW, 0x7CB342, () -> ModItems.HUNTING_KNIFE),
	/** Bardo: canciones de apoyo en área y notas que aturden. */
	BARD(RoleCategory.GUARDIAN, 0xEC407A, () -> ModItems.LUTE),
	/** Brujo: pactos demoníacos, maldiciones y diablillos. */
	WARLOCK(RoleCategory.SHADOW, 0xAD1457, () -> ModItems.PACT_TALISMAN),
	/** Domador: potencia a sus mascotas y lucha junto a ellas. */
	TAMER(RoleCategory.CRAFT, 0xA1887F, () -> ModItems.BEAST_WHISTLE);

	private final RoleCategory category;
	private final int color;
	private final Supplier<Item> icon;
	private final String key;

	Role(RoleCategory category, int color, Supplier<Item> icon) {
		this.category = category;
		this.color = color;
		this.icon = icon;
		this.key = name().toLowerCase(Locale.ROOT);
	}

	public String key() {
		return key;
	}

	/** Los 2 atributos afines del rol: rinden +50 % (principal) o +25 % (secundario). */
	public dev.aptitudes.data.PlayerAttribute[] affinities() {
		return switch (this) {
			case BERSERKER -> attrs(PlayerAttribute.STRENGTH, PlayerAttribute.VIGOR);
			case MAGE -> attrs(PlayerAttribute.MIND, PlayerAttribute.INTELLIGENCE);
			case ASSASSIN -> attrs(PlayerAttribute.DEXTERITY, PlayerAttribute.LUCK);
			case HERBALIST -> attrs(PlayerAttribute.FAITH, PlayerAttribute.VIGOR);
			case ENGINEER -> attrs(PlayerAttribute.ENDURANCE, PlayerAttribute.INTELLIGENCE);
			case LANCER -> attrs(PlayerAttribute.STRENGTH, PlayerAttribute.ENDURANCE);
			case SWORDSMAN -> attrs(PlayerAttribute.DEXTERITY, PlayerAttribute.STRENGTH);
			case PALADIN -> attrs(PlayerAttribute.FAITH, PlayerAttribute.ENDURANCE);
			case NECROMANCER -> attrs(PlayerAttribute.INTELLIGENCE, PlayerAttribute.MIND);
			case DRUID -> attrs(PlayerAttribute.FAITH, PlayerAttribute.MIND);
			case MERCHANT -> attrs(PlayerAttribute.LUCK, PlayerAttribute.MIND);
			case MONK -> attrs(PlayerAttribute.DEXTERITY, PlayerAttribute.VIGOR);
			case ELEMENTALIST -> attrs(PlayerAttribute.INTELLIGENCE, PlayerAttribute.FAITH);
			case SHAPESHIFTER -> attrs(PlayerAttribute.DEXTERITY, PlayerAttribute.MIND);
			case GUNSLINGER -> attrs(PlayerAttribute.DEXTERITY, PlayerAttribute.INTELLIGENCE);
			case WEAPON_SUMMONER -> attrs(PlayerAttribute.STRENGTH, PlayerAttribute.INTELLIGENCE);
			case HUNTER -> attrs(PlayerAttribute.DEXTERITY, PlayerAttribute.LUCK);
			case BARD -> attrs(PlayerAttribute.FAITH, PlayerAttribute.LUCK);
			case WARLOCK -> attrs(PlayerAttribute.INTELLIGENCE, PlayerAttribute.VIGOR);
			case TAMER -> attrs(PlayerAttribute.VIGOR, PlayerAttribute.MIND);
		};
	}

	private static dev.aptitudes.data.PlayerAttribute[] attrs(dev.aptitudes.data.PlayerAttribute... a) {
		return a;
	}

	public boolean hasAffinity(dev.aptitudes.data.PlayerAttribute attribute) {
		for (dev.aptitudes.data.PlayerAttribute a : affinities()) if (a == attribute) return true;
		return false;
	}

	public RoleCategory category() {
		return category;
	}

	public int color() {
		return color;
	}

	public String translationKey() {
		return "role." + Aptitudes.MOD_ID + "." + key;
	}

	public String descriptionKey() {
		return translationKey() + ".desc";
	}

	public ItemStack iconStack() {
		return new ItemStack(icon.get());
	}

	public static Optional<Role> byKey(String key) {
		return Arrays.stream(values()).filter(r -> r.key.equalsIgnoreCase(key)).findFirst();
	}

	public static Role byIndex(int index) {
		Role[] values = values();
		return index >= 0 && index < values.length ? values[index] : null;
	}
}
