package dev.aptitudes.client.compat;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.aptitudes.client.screen.ConfigScreen;

/**
 * v40: integración opcional con Mod Menu (botón «Configurar» en la lista de mods). Solo se carga si Mod Menu está
 * instalado: Fabric no instancia el punto de entrada {@code modmenu} si nadie lo pide. Mod Menu no se incluye en el
 * .jar ni es obligatorio para jugar ({@code modCompileOnly}).
 */
public class ModMenuCompat implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigScreen::new;
	}
}
