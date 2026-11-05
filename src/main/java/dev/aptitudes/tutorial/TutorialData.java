package dev.aptitudes.tutorial;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import java.util.List;

/** Attachment inmutable del tutorial: claves de pasos observados, cobrados y marca de omisión. */
public record TutorialData(TutorialRules.State state) {
	public static final Codec<TutorialData> CODEC = RecordCodecBuilder.create(i -> i.group(
			Codec.STRING.listOf().optionalFieldOf("observed", List.of()).forGetter(d ->
					TutorialRules.ORDER.stream().filter(d.state().observed()::contains).map(TutorialRules.Step::key).toList()),
			Codec.STRING.listOf().optionalFieldOf("rewarded", List.of()).forGetter(d ->
					TutorialRules.ORDER.stream().filter(d.state().rewarded()::contains).map(TutorialRules.Step::key).toList()),
			Codec.BOOL.optionalFieldOf("skipped", false).forGetter(d -> d.state().skipped())
	).apply(i, (observed, rewarded, skipped) -> new TutorialData(TutorialRules.restore(observed, rewarded, skipped))));

	public TutorialData() { this(TutorialRules.empty()); }
	public TutorialData observe(TutorialRules.Step step) { return new TutorialData(TutorialRules.observe(state, step)); }
	public TutorialData claim(TutorialRules.Step step) { return new TutorialData(TutorialRules.claim(state, step)); }
	public TutorialData skip() { return new TutorialData(TutorialRules.skip(state)); }
}
