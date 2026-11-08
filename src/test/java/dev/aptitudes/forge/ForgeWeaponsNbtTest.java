package dev.aptitudes.forge;

import dev.aptitudes.forge.ForgeRules.Affix;
import dev.aptitudes.forge.ForgeRules.Profile;
import net.minecraft.nbt.NbtCompound;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ForgeWeaponsNbtTest {
	@Test
	void preservesOtherKeysWhenSavingAndReplacingAnAffix() {
		NbtCompound original = new NbtCompound();
		original.putString("other_data", "preserved");
		NbtCompound root = original.copy();
		assertNull(ForgeWeapons.readAffix(root));

		Affix reserve = new Affix(Profile.RESERVE, 2);
		ForgeWeapons.writeAffix(root, reserve);
		assertEquals(reserve, ForgeWeapons.readAffix(root));

		Affix impetus = new Affix(Profile.IMPETUS, 2);
		ForgeWeapons.writeAffix(root, impetus);
		assertEquals(impetus, ForgeWeapons.readAffix(root));
		root.remove("aptitudes_forge");
		assertEquals(original, root);
	}

	@Test
	void rejectsMalformedCompoundsAndNumericTypes() {
		NbtCompound root = new NbtCompound();
		root.putString("aptitudes_forge", "reserve");
		assertNull(ForgeWeapons.readAffix(root));

		NbtCompound stored = new NbtCompound();
		root.put("aptitudes_forge", stored);
		stored.putString("profile", "reserve");
		assertNull(ForgeWeapons.readAffix(root));
		stored.putByte("temper", (byte) 1);
		assertNull(ForgeWeapons.readAffix(root));
		stored.putInt("temper", 3);
		assertNull(ForgeWeapons.readAffix(root));
	}
}
