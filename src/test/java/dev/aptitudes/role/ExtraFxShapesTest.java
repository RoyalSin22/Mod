package dev.aptitudes.role;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ExtraFxShapesTest {
	@Test
	void everyActiveExtraOfEveryRoleHasAShape() {
		assertEquals(20, ExtraFxShapes.roles().size());
		int actives = 0;
		for (String role : ExtraFxShapes.roles()) {
			for (RoleExtras.Extra extra : RoleExtras.of(role)) {
				if (!extra.active()) {
					assertNull(ExtraFxShapes.shapeOf(extra.id()), extra.id() + " es pasiva y no debe tener forma");
					continue;
				}
				actives++;
				assertNotNull(ExtraFxShapes.shapeOf(extra.id()), "falta la forma de " + extra.id());
			}
		}
		assertEquals(100, actives);
	}

	@Test
	void shapesMatchTheEvolvedTemplatesByNameAndOrder() {
		ExtraFxShapes.Shape[] shapes = ExtraFxShapes.Shape.values();
		EvolvedTree.Template[] templates = EvolvedTree.Template.values();
		assertEquals(templates.length, shapes.length);
		for (int i = 0; i < shapes.length; i++) assertEquals(templates[i].name(), shapes[i].name());
	}

	@Test
	void unknownIdsAreIgnoredAndBigOnesAreTheAscent() {
		assertNull(ExtraFxShapes.shapeOf("fireball"));
		assertNull(ExtraFxShapes.shapeOf("berserker_n5"));
		assertNull(ExtraFxShapes.shapeOf(null));
		assertTrue(ExtraFxShapes.big("mage_n8"));
		assertTrue(ExtraFxShapes.big("mage_n10"));
		assertFalse(ExtraFxShapes.big("mage_n3"));
	}
}
