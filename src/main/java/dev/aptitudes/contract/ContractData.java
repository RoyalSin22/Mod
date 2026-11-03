package dev.aptitudes.contract;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;

/** Estado individual de contratos; Fabric lo copia al morir y lo persiste en playerdata. */
public final class ContractData {
	public record Active(int offer, int progress) {}
	public static final Codec<Active> ACTIVE_CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.INT.fieldOf("offer").forGetter(Active::offer), Codec.INT.fieldOf("progress").forGetter(Active::progress)
	).apply(i, Active::new));
	public static final Codec<ContractData> CODEC = RecordCodecBuilder.create(i -> i.group(
		Codec.INT.optionalFieldOf("reputation", 0).forGetter(d -> d.reputation),
		Codec.LONG.optionalFieldOf("offer_day", Long.MIN_VALUE).forGetter(d -> d.offerDay),
		ACTIVE_CODEC.listOf().optionalFieldOf("active", List.of()).forGetter(d -> List.copyOf(d.active))
	).apply(i, ContractData::new));
	private int reputation;
	private long offerDay;
	private final List<Active> active = new ArrayList<>();
	public ContractData() { this(0, Long.MIN_VALUE, List.of()); }
	private ContractData(int reputation, long offerDay, List<Active> active) {
		this.reputation = Math.max(0, reputation); this.offerDay = offerDay;
		for (Active a : active) if (a.offer() >= 0 && a.offer() < 3 && this.active.size() < 3) this.active.add(new Active(a.offer(), Math.max(0, a.progress())));
	}
	public int reputation() { return reputation; }
	public long offerDay() { return offerDay; }
	public List<Active> active() { return List.copyOf(active); }
	public void refreshDay(long day) { if (offerDay != day) { offerDay = day; active.clear(); } }
	public boolean accept(int offer) { if (active.size() >= ContractRules.activeLimit(reputation) || active.stream().anyMatch(a -> a.offer() == offer)) return false; active.add(new Active(offer, 0)); return true; }
	public void abandon(int offer) { active.removeIf(a -> a.offer() == offer); }
	public void progress(int offer, int amount, int goal) {
		for (int i=0; i<active.size(); i++) if (active.get(i).offer() == offer) { Active a=active.get(i); active.set(i,new Active(offer,ContractRules.progress(a.progress(), amount, goal))); }
	}
	public boolean complete(int offer, int goal) { return active.stream().anyMatch(a -> a.offer() == offer && a.progress() >= goal); }
	public void completeAndReward(int offer, int reputation) { active.removeIf(a -> a.offer() == offer); this.reputation = Math.min(1000000, this.reputation + Math.max(0, reputation)); }
}
