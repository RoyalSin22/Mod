package dev.aptitudes.item;

import dev.aptitudes.Aptitudes;
import dev.aptitudes.role.Role;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

/**
 * v62 (A6): icono de rol dentro de cualquier texto. La fuente {@code aptitudes:role_icons} (generada por
 * {@code tools/textures/gen_role_glyphs.py}) dibuja el sello del rol en el carácter U+E000 + ordinal.
 * Es solo estilo de texto: el servidor puede enviarlo y un cliente sin el recurso verá un recuadro.
 */
public final class RoleGlyphs {
	public static final Identifier FONT = Aptitudes.id("role_icons");
	private static final char FIRST = '';

	private RoleGlyphs() {
	}

	/** El sello del rol (en blanco, para no teñir el icono). */
	public static MutableText glyph(Role role) {
		return Text.literal(String.valueOf((char) (FIRST + role.ordinal()))).styled(s -> s.withFont(FONT).withColor(0xFFFFFF));
	}

	/** «[sello] texto», con el texto en el color del rol. */
	public static MutableText line(Role role, Text text) {
		return Text.empty().append(glyph(role)).append(" ").append(text.copy().withColor(role.color()));
	}
}
