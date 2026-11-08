package dev.aptitudes.role.datapack;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import static dev.aptitudes.role.RoleNode.LEVELS_PER_RANK;
import static dev.aptitudes.role.RoleNode.MAX_RANK;

/** Formato y validación del grafo. Separado de los registros y la recarga de Minecraft. */
final class TalentPackRules {
	static final int MAX_NODES = 32;
	static final int MAX_PER_ROLE = 8;
	static final int MAX_FILE_BYTES = 8192;
	static final int MAX_ID_LENGTH = 64;
	private static final int MAX_LEVEL = 50;
	private static final Pattern BUILTIN_ID = Pattern.compile("[a-z][a-z0-9_]*");
	private static final Pattern CUSTOM_ID = Pattern.compile("[a-z0-9_][a-z0-9_.-]*:[a-z0-9_][a-z0-9_.-]*(?:/[a-z0-9_][a-z0-9_.-]*)*");

	enum Template {
		SPEED;

		static Template fromKey(String key) {
			if ("speed".equals(key)) return SPEED;
			throw new IllegalArgumentException("Plantilla desconocida: " + key);
		}
	}

	record Anchor(String role, int level) {}

	record Node(String id, String role, String prerequisite, int level, int ranks, Template template) {}

	private TalentPackRules() {}

	static Node parse(String id, String json) throws IOException {
		Map<String, String> fields = new HashMap<>();
		try (JsonReader reader = new JsonReader(new StringReader(json))) {
			reader.setLenient(false);
			reader.beginObject();
			while (reader.hasNext()) {
				String name = reader.nextName();
				JsonToken expected = switch (name) {
					case "role", "prerequisite", "template" -> JsonToken.STRING;
					case "level", "ranks" -> JsonToken.NUMBER;
					default -> throw new IllegalArgumentException("Campo desconocido: " + name);
				};
				if (fields.containsKey(name)) throw new IllegalArgumentException("Campo repetido: " + name);
				if (reader.peek() != expected) throw new IllegalArgumentException("Tipo inválido para " + name);
				fields.put(name, reader.nextString());
			}
			reader.endObject();
			if (reader.peek() != JsonToken.END_DOCUMENT) throw new IllegalArgumentException("Contenido tras el objeto");
		} catch (IllegalStateException e) {
			throw new IllegalArgumentException("Se esperaba un objeto JSON", e);
		}
		return new Node(id, required(fields, "role"), required(fields, "prerequisite"),
				integer(fields, "level"), integer(fields, "ranks"), Template.fromKey(required(fields, "template")));
	}

	static void validate(Collection<Node> nodes, Predicate<String> roleExists, Function<String, Anchor> builtinAnchor) {
		if (nodes.size() > MAX_NODES) throw new IllegalArgumentException("Máximo " + MAX_NODES + " talentos");
		Map<String, Node> byId = new LinkedHashMap<>();
		Map<String, Integer> counts = new HashMap<>();
		for (Node node : nodes) {
			if (!isCustomId(node.id())) throw invalid(node, "identificador de datapack inválido");
			if (byId.putIfAbsent(node.id(), node) != null) throw invalid(node, "identificador repetido");
			if (!validId(node.role(), BUILTIN_ID) || !roleExists.test(node.role())) {
				throw invalid(node, "rol desconocido: " + node.role());
			}
			if (counts.merge(node.role(), 1, Integer::sum) > MAX_PER_ROLE) {
				throw invalid(node, "máximo " + MAX_PER_ROLE + " talentos por rol");
			}
			if (node.ranks() < 1 || node.ranks() > MAX_RANK || node.level() < 1
					|| (long) node.level() + (node.ranks() - 1L) * LEVELS_PER_RANK > MAX_LEVEL) {
				throw invalid(node, "nivel o rangos fuera de rango");
			}
		}
		for (Node node : nodes) {
			String requirement = node.prerequisite();
			Node parent = byId.get(requirement);
			Anchor anchor = parent != null ? new Anchor(parent.role(), parent.level())
					: validId(requirement, BUILTIN_ID) ? builtinAnchor.apply(requirement) : null;
			if (anchor == null) throw invalid(node, "prerrequisito desconocido o no permitido: " + requirement);
			if (!anchor.role().equals(node.role())) throw invalid(node, "el prerrequisito pertenece a otro rol");
			if (anchor.level() > node.level()) throw invalid(node, "el prerrequisito exige un nivel mayor");
		}
		for (Node node : nodes) {
			Set<String> visited = new HashSet<>();
			for (Node current = node; current != null; current = byId.get(current.prerequisite())) {
				if (!visited.add(current.id())) throw invalid(node, "ciclo de prerrequisitos");
			}
		}
	}

	static boolean isCustomId(String id) {
		return validId(id, CUSTOM_ID);
	}

	private static boolean validId(String value, Pattern pattern) {
		return value.length() <= MAX_ID_LENGTH && pattern.matcher(value).matches();
	}

	private static String required(Map<String, String> fields, String key) {
		String value = fields.get(key);
		if (value == null) throw new IllegalArgumentException("Falta el campo " + key);
		return value;
	}

	private static int integer(Map<String, String> fields, String key) {
		try {
			return Integer.parseInt(required(fields, key));
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Se esperaba un entero para " + key, e);
		}
	}

	private static IllegalArgumentException invalid(Node node, String reason) {
		return new IllegalArgumentException(node.id() + ": " + reason);
	}
}
