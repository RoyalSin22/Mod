package dev.aptitudes.config;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * v40: reglas puras para editar la configuración desde la pantalla: qué campos se pueden editar y cómo se
 * interpreta el texto que escribe el jugador. Los límites de cada valor los sigue poniendo
 * {@link AptitudesConfig#validated}; aquí solo se rechaza lo que ni siquiera es del tipo correcto.
 */
public final class ConfigFieldRules {
	/** Longitud máxima de un valor de texto (p. ej., {@code attributeScaling}). */
	public static final int MAX_STRING = 64;

	private ConfigFieldRules() {
	}

	/** ¿Es un tipo que la pantalla sabe editar? */
	public static boolean supported(Class<?> type) {
		return type == int.class || type == double.class || type == boolean.class || type == String.class;
	}

	/** Campos editables de una clase de configuración: públicos, no estáticos y de un tipo admitido, en orden. */
	public static List<Field> editableFields(Class<?> configClass) {
		List<Field> out = new ArrayList<>();
		for (Field f : configClass.getFields()) {
			int mod = f.getModifiers();
			if (Modifier.isStatic(mod) || Modifier.isFinal(mod) || !supported(f.getType())) continue;
			if (f.getName().equals("configRevision")) continue; // v46: interno (migraciones), no se edita
			out.add(f);
		}
		return out;
	}

	/**
	 * Convierte el texto al tipo del campo. Lanza {@link IllegalArgumentException} si no es válido: número mal
	 * escrito, fuera del rango de {@code int}, no finito, booleano distinto de true/false o texto demasiado largo.
	 * Acepta coma decimal («1,5»).
	 */
	public static Object parse(Class<?> type, String raw) {
		if (raw == null) throw new IllegalArgumentException("vacío");
		String s = raw.trim();
		if (type == boolean.class) {
			String b = s.toLowerCase(Locale.ROOT);
			if (b.equals("true") || b.equals("sí") || b.equals("si") || b.equals("1")) return Boolean.TRUE;
			if (b.equals("false") || b.equals("no") || b.equals("0")) return Boolean.FALSE;
			throw new IllegalArgumentException("booleano");
		}
		if (type == int.class) {
			long v;
			try {
				v = Long.parseLong(s);
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("entero");
			}
			if (v < Integer.MIN_VALUE || v > Integer.MAX_VALUE) throw new IllegalArgumentException("fuera de rango");
			return (int) v;
		}
		if (type == double.class) {
			double v;
			try {
				v = Double.parseDouble(s.replace(',', '.'));
			} catch (NumberFormatException e) {
				throw new IllegalArgumentException("decimal");
			}
			if (!Double.isFinite(v)) throw new IllegalArgumentException("no finito");
			return v;
		}
		if (type == String.class) {
			if (s.length() > MAX_STRING) throw new IllegalArgumentException("demasiado largo");
			return s;
		}
		throw new IllegalArgumentException("tipo no admitido");
	}

	/** Texto para mostrar un valor (sin «.0» sobrante en los decimales enteros). */
	public static String format(Object value) {
		if (value instanceof Double d) {
			if (d == Math.rint(d) && Math.abs(d) < 1e15) return Long.toString(d.longValue());
			return Double.toString(d);
		}
		return String.valueOf(value);
	}
}
