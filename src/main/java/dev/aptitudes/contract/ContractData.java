package dev.aptitudes.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.aptitudes.contract.ContractRules.Difficulty;
import dev.aptitudes.contract.ContractRules.Kind;
import dev.aptitudes.contract.ContractRules.Offer;

import java.util.ArrayList;
import java.util.List;

/**
 * Contratos de un jugador (attachment {@code aptitudes:contracts}, se copia al morir).
 *
 * <ul>
 *   <li>Un contrato aceptado guarda la oferta <b>completa</b>: aunque cambien la reputación, la evolución o el día,
 *       su objetivo y su recompensa no cambian, y sigue activo al día siguiente.</li>
 *   <li>Cada oferta del día solo se puede aceptar una vez ({@link #taken}), así que tampoco se puede volver a
 *       aceptar y cobrar después de cobrarla.</li>
 *   <li>La oferta del día se genera con la reputación y la evolución del inicio del día ({@link #offerReputation},
 *       {@link #offerEvolution}): cobrar un contrato no cambia las otras ofertas del mismo día.</li>
 * </ul>
 */
public final class ContractData {
	public record Active(Offer offer, int progress) {
		public boolean complete() { return progress >= offer.goal(); }
	}

	private static final Codec<Offer> OFFER_CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.fieldOf("kind").forGetter(o -> o.kind().ordinal()),
			Codec.INT.fieldOf("difficulty").forGetter(o -> o.difficulty().ordinal()),
			Codec.INT.fieldOf("target").forGetter(Offer::target),
			Codec.INT.fieldOf("goal").forGetter(Offer::goal),
			Codec.INT.fieldOf("emeralds").forGetter(Offer::emeralds),
			Codec.INT.fieldOf("fragments").forGetter(Offer::fragments),
			Codec.INT.fieldOf("role_xp").forGetter(Offer::roleXp),
			Codec.INT.fieldOf("reputation").forGetter(Offer::reputation),
			Codec.BOOL.fieldOf("voucher").forGetter(Offer::voucher),
			Codec.BOOL.fieldOf("emblem").forGetter(Offer::emblem)
	).apply(i, (kind, difficulty, target, goal, emeralds, fragments, roleXp, reputation, voucher, emblem) -> new Offer(
			Kind.values()[Math.max(0, Math.min(Kind.values().length - 1, kind))],
			Difficulty.values()[Math.max(0, Math.min(Difficulty.values().length - 1, difficulty))],
			target, Math.max(1, goal), Math.max(0, emeralds), Math.max(0, fragments), Math.max(0, roleXp),
			Math.max(0, reputation), voucher, emblem)));

	private static final Codec<Active> ACTIVE_CODEC = RecordCodecBuilder.create(i -> i.group(
			OFFER_CODEC.fieldOf("offer").forGetter(Active::offer),
			Codec.INT.optionalFieldOf("progress", 0).forGetter(Active::progress)
	).apply(i, Active::new));

	public static final Codec<ContractData> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.INT.optionalFieldOf("reputation", 0).forGetter(d -> d.reputation),
			Codec.LONG.optionalFieldOf("offer_day", Long.MIN_VALUE).forGetter(d -> d.offerDay),
			Codec.INT.optionalFieldOf("offer_reputation", 0).forGetter(d -> d.offerReputation),
			Codec.INT.optionalFieldOf("offer_evolution", 0).forGetter(d -> d.offerEvolution),
			Codec.INT.listOf().optionalFieldOf("taken", List.of()).forGetter(d -> List.copyOf(d.taken)),
			ACTIVE_CODEC.listOf().optionalFieldOf("active", List.of()).forGetter(d -> List.copyOf(d.active)),
			Codec.INT.optionalFieldOf("completed", 0).forGetter(d -> d.completed)
	).apply(i, ContractData::new));

	private int reputation;
	private long offerDay;
	private int offerReputation;
	private int offerEvolution;
	private final List<Integer> taken = new ArrayList<>();
	private final List<Active> active = new ArrayList<>();
	/** Contratos cobrados en total (para logros). */
	private int completed;

	public ContractData() {
		this(0, Long.MIN_VALUE, 0, 0, List.of(), List.of(), 0);
	}

	private ContractData(int reputation, long offerDay, int offerReputation, int offerEvolution, List<Integer> taken,
						 List<Active> active, int completed) {
		this.reputation = Math.max(0, reputation);
		this.offerDay = offerDay;
		this.offerReputation = Math.max(0, offerReputation);
		this.offerEvolution = Math.max(0, offerEvolution);
		for (Integer t : taken) if (t != null && t >= 0 && t < ContractRules.OFFERS_PER_DAY && !this.taken.contains(t)) this.taken.add(t);
		for (Active a : active) {
			if (this.active.size() >= ContractRules.MAX_ACTIVE) break;
			this.active.add(new Active(a.offer(), Math.max(0, Math.min(a.offer().goal(), a.progress()))));
		}
		this.completed = Math.max(0, completed);
	}

	public int reputation() { return reputation; }
	public long offerDay() { return offerDay; }
	public int offerReputation() { return offerReputation; }
	public int offerEvolution() { return offerEvolution; }
	public int completed() { return completed; }
	public List<Active> active() { return List.copyOf(active); }
	public boolean isTaken(int offerIndex) { return taken.contains(offerIndex); }

	/** Nuevo día: nuevas ofertas (con la reputación y la evolución de ahora). Los contratos activos se conservan. */
	public void refreshDay(long day, int evolution) {
		if (offerDay == day) return;
		offerDay = day;
		offerReputation = reputation;
		offerEvolution = Math.max(0, evolution);
		taken.clear();
	}

	/** Acepta la oferta {@code index} del día: una sola vez por día y sin superar el límite de activos. */
	public boolean accept(int index, Offer offer) {
		if (offer == null || index < 0 || index >= ContractRules.OFFERS_PER_DAY || taken.contains(index)) return false;
		if (active.size() >= Math.min(ContractRules.MAX_ACTIVE, ContractRules.activeLimit(reputation))) return false;
		taken.add(index);
		active.add(new Active(offer, 0));
		return true;
	}

	public boolean abandon(int slot) {
		if (slot < 0 || slot >= active.size()) return false;
		active.remove(slot);
		return true;
	}

	/** Suma progreso al contrato de la ranura {@code slot}. Devuelve si cambió. */
	public boolean progress(int slot, int amount) {
		if (slot < 0 || slot >= active.size() || amount <= 0) return false;
		Active a = active.get(slot);
		int next = ContractRules.progress(a.progress(), amount, a.offer().goal());
		if (next == a.progress()) return false;
		active.set(slot, new Active(a.offer(), next));
		return true;
	}

	/** Cobra el contrato de la ranura si está completo: lo quita, suma reputación y lo cuenta. Null si no se puede. */
	public Offer claim(int slot) {
		if (slot < 0 || slot >= active.size() || !active.get(slot).complete()) return null;
		Offer offer = active.remove(slot).offer();
		reputation = (int) Math.min(1_000_000L, (long) reputation + Math.max(0, offer.reputation()));
		completed = (int) Math.min(Integer.MAX_VALUE, (long) completed + 1);
		return offer;
	}
}
