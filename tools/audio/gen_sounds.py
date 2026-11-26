#!/usr/bin/env python3
"""
Generador de los sonidos propios de Aptitudes (v37). Síntesis por programa, sin muestras externas:
todos los sonidos son originales y reproducibles (semilla fija).

Uso:  python3 tools/audio/gen_sounds.py <carpeta assets/aptitudes> [nombre ...]
Sin nombres genera todos; con nombres, solo esos (p. ej. los nuevos, sin reescribir los demás).
Escribe <carpeta>/sounds/<nombre>.ogg (mono, 44,1 kHz, Vorbis). Requiere numpy y ffmpeg con libvorbis.
Los sonidos son mono para que Minecraft los sitúe en el espacio (atenuación por distancia).
"""
import os
import subprocess
import sys
import tempfile
import wave

import numpy as np

RATE = 44_100
SEED = 37_037


def t_axis(seconds):
    return np.arange(int(RATE * seconds)) / RATE


def env(t, attack, decay):
    """Envolvente: subida lineal de `attack` s y caída exponencial con constante `decay` s."""
    a = np.clip(t / max(attack, 1e-4), 0, 1)
    return a * np.exp(-np.maximum(t - attack, 0) / decay)


def lowpass(x, cutoff):
    """Filtro paso bajo de un polo (suficiente para suavizar ruido)."""
    alpha = 1 - np.exp(-2 * np.pi * cutoff / RATE)
    y = np.empty_like(x)
    acc = 0.0
    for i, v in enumerate(x):
        acc += alpha * (v - acc)
        y[i] = acc
    return y


def normalize(x, peak=0.85):
    m = np.max(np.abs(x))
    return x if m == 0 else x * (peak / m)


def fade_out(x, seconds=0.05):
    n = min(len(x), int(RATE * seconds))
    x[-n:] *= np.linspace(1, 0, n)
    return x


# ----------------------------------------------------------------------------- Sonidos

def eclipse_announce(rng):
    """Campana grave y lejana: parciales inarmónicos con caídas distintas (aviso del día anterior)."""
    t = t_axis(4.0)
    base = 98.0
    partials = [(0.5, 1.0, 2.8), (1.0, 0.8, 2.2), (1.19, 0.5, 1.6), (1.56, 0.35, 1.2), (2.0, 0.3, 1.0), (2.74, 0.15, 0.6)]
    x = sum(a * np.sin(2 * np.pi * base * r * t) * env(t, 0.004, d) for r, a, d in partials)
    x += 0.05 * lowpass(rng.standard_normal(len(t)), 900) * env(t, 0.002, 0.05)
    return fade_out(normalize(x))


def eclipse_start(rng):
    """Retumbo que crece y un zumbido disonante: el sol empieza a apagarse."""
    t = t_axis(5.0)
    swell = np.clip(t / 2.5, 0, 1) ** 2 * np.exp(-np.maximum(t - 3.5, 0) / 0.6)
    rumble = lowpass(rng.standard_normal(len(t)), 120) * 6
    drone = np.sin(2 * np.pi * 55 * t) + 0.7 * np.sin(2 * np.pi * 58.3 * t) + 0.4 * np.sin(2 * np.pi * 82.4 * t)
    x = (rumble + drone) * swell
    return fade_out(normalize(x))


def eclipse_totality(rng):
    """Golpe grave seguido de un coro sintético que asciende (comienza la totalidad)."""
    t = t_axis(4.5)
    boom = np.sin(2 * np.pi * (60 - 25 * np.minimum(t, 1)) * t) * env(t, 0.005, 0.5)
    boom += 0.5 * lowpass(rng.standard_normal(len(t)), 300) * env(t, 0.002, 0.25)
    freq = 110 * (1 + 0.5 * np.clip((t - 0.6) / 3.0, 0, 1))
    phase = 2 * np.pi * np.cumsum(freq) / RATE
    choir = sum(np.sin(phase * k * (1 + d)) / k for k, d in [(1, 0), (1, 0.006), (2, -0.004), (3, 0.003), (4, 0)])
    choir *= np.clip((t - 0.4) / 1.2, 0, 1) * np.exp(-np.maximum(t - 3.6, 0) / 0.3)
    x = boom + 0.45 * choir
    return fade_out(normalize(x))


def herald_spawn(rng):
    """Impacto pesado y un chirrido metálico que cae: llega el Heraldo."""
    t = t_axis(2.6)
    impact = lowpass(rng.standard_normal(len(t)), 400) * env(t, 0.002, 0.35) * 3
    impact += np.sin(2 * np.pi * 45 * t) * env(t, 0.003, 0.6)
    screech_f = 900 * np.exp(-t * 0.9) + 180
    screech = np.sin(2 * np.pi * np.cumsum(screech_f) / RATE)
    screech = np.sign(screech) * np.abs(screech) ** 0.6  # timbre áspero
    screech *= np.clip((t - 0.15) / 0.2, 0, 1) * np.exp(-np.maximum(t - 0.4, 0) / 0.7) * 0.35
    return fade_out(normalize(impact + screech))


def combo_finisher(rng):
    """Silbido rápido y golpe seco para el remate del combo."""
    t = t_axis(0.65)
    whoosh = rng.standard_normal(len(t))
    whoosh = lowpass(whoosh, 2500) * np.sin(np.pi * np.clip(t / 0.25, 0, 1)) * (t < 0.25)
    hit = (lowpass(rng.standard_normal(len(t)), 1200) * 1.5 + np.sin(2 * np.pi * 80 * t)) * env(np.maximum(t - 0.22, 0), 0.002, 0.12) * (t >= 0.22)
    return fade_out(normalize(0.6 * whoosh + hit))


def rune_socket(rng):
    """Campanilla cristalina en dos notas: la runa encaja en el arma."""
    t = t_axis(1.0)
    x = np.zeros_like(t)
    for start, f in ((0.0, 1318.5), (0.09, 1975.5)):
        tt = np.maximum(t - start, 0)
        note = (np.sin(2 * np.pi * f * tt) + 0.3 * np.sin(2 * np.pi * f * 2.76 * tt)) * env(tt, 0.002, 0.28)
        x += note * (t >= start)
    return fade_out(normalize(x, 0.7))


def rune_remove(rng):
    """Raspado de piedra con un chasquido final: la runa se rompe al extraerla."""
    t = t_axis(0.9)
    grit = lowpass(rng.standard_normal(len(t)), 1800) * (0.6 + 0.4 * np.sin(2 * np.pi * 28 * t)) * env(t, 0.03, 0.4)
    crack_t = np.maximum(t - 0.62, 0)
    crack = rng.standard_normal(len(t)) * env(crack_t, 0.001, 0.04) * (t >= 0.62) * 1.5
    return fade_out(normalize(grit + crack, 0.75))


# ---------------------------------------------------------------- Piedras angulares (v46.1)

def _tone(t, f, attack, decay, harm=0.0):
    return (np.sin(2 * np.pi * f * t) + harm * np.sin(2 * np.pi * f * 2 * t)) * env(t, attack, decay)


def keystone_carnage(rng):
    """Latido grave y rasgueo: al matar, la sangre devuelve fuerza."""
    t = t_axis(0.8)
    beat = sum(_tone(np.maximum(t - s, 0), 62, 0.005, 0.09) * (t >= s) for s in (0.0, 0.18))
    rasp = lowpass(rng.standard_normal(len(t)), 900) * env(t, 0.01, 0.2) * 0.5
    return fade_out(normalize(beat + rasp, 0.8))


def keystone_bulwark(rng):
    """Golpe metálico de escudo con zumbido protector."""
    t = t_axis(1.4)
    clang = sum(_tone(t, f, 0.002, d) * a for f, a, d in ((523, 1.0, 0.5), (1047, 0.5, 0.3), (1568, 0.3, 0.2)))
    hum = np.sin(2 * np.pi * 110 * t) * env(t, 0.15, 0.6) * 0.4
    return fade_out(normalize(clang + hum, 0.75))


def keystone_overload(rng):
    """Barrido eléctrico ascendente: habilidad sin coste."""
    t = t_axis(0.7)
    f = 300 + 1500 * (t / t[-1]) ** 2
    zap = np.sign(np.sin(2 * np.pi * np.cumsum(f) / RATE)) * env(t, 0.01, 0.25) * 0.4
    crackle = rng.standard_normal(len(t)) * (rng.random(len(t)) < 0.02) * env(t, 0.01, 0.3)
    return fade_out(normalize(lowpass(zap, 4000) + crackle, 0.7))


def keystone_detonation(rng):
    """Estallido sordo con cola grave."""
    t = t_axis(1.2)
    boom = lowpass(rng.standard_normal(len(t)), 400) * env(t, 0.003, 0.25) * 2.0
    sub = np.sin(2 * np.pi * (55 - 20 * t) * t) * env(t, 0.003, 0.4)
    return fade_out(normalize(boom + sub, 0.9))


def keystone_contagion(rng):
    """Burbujeo viscoso que se extiende."""
    t = t_axis(1.0)
    x = np.zeros_like(t)
    for _ in range(9):
        s0 = rng.uniform(0, 0.7)
        f0 = rng.uniform(180, 420)
        tt = np.maximum(t - s0, 0)
        x += np.sin(2 * np.pi * (f0 + 600 * tt) * tt) * env(tt, 0.004, 0.06) * (t >= s0)
    return fade_out(normalize(lowpass(x, 2200), 0.6))


def keystone_legion(rng):
    """Coro grave de voces sin palabras (acorde menor con vibrato)."""
    t = t_axis(1.5)
    x = sum(np.sin(2 * np.pi * f * t + 0.4 * np.sin(2 * np.pi * 5 * t)) for f in (98, 116.5, 146.8))
    return fade_out(normalize(lowpass(x * env(t, 0.25, 0.7), 1500), 0.6))


def keystone_specter(rng):
    """Susurro que se desvanece."""
    t = t_axis(0.9)
    air = lowpass(rng.standard_normal(len(t)), 3000) - lowpass(rng.standard_normal(len(t)), 600)
    return fade_out(normalize(air * env(t, 0.08, 0.3), 0.55))


def keystone_fortune(rng):
    """Tintineo de monedas."""
    t = t_axis(0.9)
    x = np.zeros_like(t)
    for s0, f in ((0.0, 2637), (0.07, 3136), (0.15, 2794), (0.22, 3520)):
        tt = np.maximum(t - s0, 0)
        x += _tone(tt, f, 0.001, 0.12, 0.3) * (t >= s0)
    return fade_out(normalize(x, 0.6))


def keystone_tempest(rng):
    """Chasquido de chispa eléctrica."""
    t = t_axis(0.35)
    snap = rng.standard_normal(len(t)) * env(t, 0.0005, 0.03) * 1.5
    buzz = np.sign(np.sin(2 * np.pi * 180 * t)) * env(t, 0.002, 0.08) * 0.3
    return fade_out(normalize(snap + buzz, 0.7))


def keystone_sanctuary(rng):
    """Acorde mayor luminoso y suave (cura)."""
    t = t_axis(1.6)
    x = sum(_tone(t, f, 0.12, 0.7) for f in (523.3, 659.3, 784.0, 1046.5))
    return fade_out(normalize(x, 0.5))


def arena_theme(rng):
    """Motivo de arena en 16 pulsos, con bordón, timbales y una melodía ascendente."""
    beat = 0.65
    t = t_axis(12.0)
    x = np.zeros_like(t)
    for start, note in zip((0, 4, 8, 12), (38, 34, 36, 33)):
        f = 440 * 2 ** ((note - 69) / 12)
        tt = np.maximum(t - start * beat, 0)
        chord = sum(np.sin(2 * np.pi * f * ratio * tt) for ratio in (1, 1.5, 2))
        x += 0.25 * chord * env(tt, 0.16, 1.5) * (t >= start * beat)
    for start, note in zip((1, 2.5, 4, 6, 7, 8.5, 10, 11, 12, 13.5, 15),
                           (62, 65, 69, 67, 65, 62, 64, 65, 69, 73, 74)):
        f = 440 * 2 ** ((note - 69) / 12)
        tt = np.maximum(t - start * beat, 0)
        brass = np.sin(2 * np.pi * f * tt) + 0.24 * np.sin(4 * np.pi * f * tt)
        x += 0.36 * brass * env(tt, 0.06, 0.58) * (t >= start * beat)
    for start in (0, 2, 4, 5.5, 8, 10, 12, 14, 15.5):
        tt = np.maximum(t - start * beat, 0)
        drum = np.sin(2 * np.pi * (68 * tt - 25 * tt ** 2))
        x += 0.48 * drum * env(tt, 0.004, 0.16) * (t >= start * beat)
    return fade_out(normalize(x, 0.72), 0.9)


# ---------------------------------------------------------------- Interfaz del árbol (v65): sonidos metálicos

def ui_metal_select(rng):
    """Clic metálico corto: al elegir un nodo del árbol."""
    t = t_axis(0.25)
    x = _tone(t, 2400, 0.001, 0.05, 0.4) + 0.5 * _tone(t, 3650, 0.001, 0.03)
    click = lowpass(rng.standard_normal(len(t)), 6000) * env(t, 0.0005, 0.01)
    return fade_out(normalize(x + 0.4 * click, 0.55), 0.02)


def ui_metal_unlock(rng):
    """Campana metálica con chispa: el nodo se desbloquea."""
    t = t_axis(1.2)
    x = np.zeros_like(t)
    for f, a in ((1046.5, 1.0), (1567.9, 0.6), (2093.0, 0.35)):
        x += a * _tone(t, f, 0.002, 0.45, 0.25)
    spark = lowpass(rng.standard_normal(len(t)), 9000) * env(t, 0.001, 0.03) * (t < 0.04)
    return fade_out(normalize(x + 0.5 * spark, 0.7))


def ui_metal_denied(rng):
    """Golpe sordo: el nodo está bloqueado o no se puede pagar."""
    t = t_axis(0.35)
    x = _tone(t, 170, 0.002, 0.12, 0.6)
    rattle = lowpass(rng.standard_normal(len(t)), 2000) * env(t, 0.001, 0.05)
    return fade_out(normalize(x + 0.3 * rattle, 0.6))



def _errante_voice(rng, seconds, frequencies, sweep, texture, cutoff, decay):
    """Resonancias huecas y fricción de tela sin muestras externas."""
    t = t_axis(seconds)
    breath = rng.standard_normal(len(t))
    cloth = lowpass(breath, cutoff) - lowpass(breath, max(75, cutoff * 0.20))
    phases = [2 * np.pi * f * (t + sweep * t * t / (2 * seconds)) for f in frequencies]
    hollow = sum(np.sin(phase + 0.22 * np.sin(2 * np.pi * (7 + i) * t)) / (i + 1)
                 for i, phase in enumerate(phases))
    flutter = 0.78 + 0.22 * np.sin(2 * np.pi * (11 * t + 8 * t * t))
    x = (hollow * 0.22 + cloth * texture * flutter) * env(t, 0.025, decay)
    return fade_out(normalize(x, 0.69), 0.055)


def errante_ambient(rng):
    return _errante_voice(rng, 0.90, (93, 141, 273), -0.18, 0.68, 1300, 0.48)


def errante_alert(rng):
    return _errante_voice(rng, 0.31, (172, 291, 463), -0.36, 0.45, 2400, 0.13)


def errante_prepare(rng):
    return _errante_voice(rng, 0.38, (120, 181, 307), 0.88, 0.54, 1450, 0.25)


def errante_rend(rng):
    dry = _errante_voice(rng, 0.43, (140, 223, 420), -0.45, 1.40, 3800, 0.13)
    t = t_axis(0.43)
    snap = (rng.standard_normal(len(t)) * env(t, 0.004, 0.025)) * 0.23
    return fade_out(normalize(dry + snap, 0.80), 0.043)


def errante_hurt(rng):
    return _errante_voice(rng, 0.26, (108, 183, 284), -0.34, 0.73, 1850, 0.087)


def errante_death(rng):
    hollow = _errante_voice(rng, 0.86, (111, 173, 328), -0.58, 0.91, 1020, 0.31)
    t = t_axis(0.86)
    drop = np.sin(2 * np.pi * 58 * t) * env(t - 0.12, 0.009, 0.099)
    return fade_out(normalize(hollow + 0.38 * drop, 0.75), 0.09)


def errante_despawn(rng):
    return _errante_voice(rng, 0.69, (156, 263, 398), 0.28, 0.88, 2700, 0.19)


# ---------------------------------------------------------------- Bersérker (V77)

def _sweep_noise(rng, seconds, f_start, f_peak, f_end, width):
    """Ruido de banda cuya frecuencia central sube y baja (aire cortado). Pasos de 12 dB/oct arriba y abajo."""
    t = t_axis(seconds)
    n = len(t)
    pos = np.clip(t / seconds, 0, 1)
    freq = np.where(pos < 0.4, f_start + (f_peak - f_start) * (pos / 0.4), f_peak + (f_end - f_peak) * ((pos - 0.4) / 0.6))
    noise = rng.standard_normal(n)
    out = np.empty(n)
    up1 = up2 = lo1 = lo2 = 0.0
    for i in range(n):
        a_up = 1 - np.exp(-2 * np.pi * freq[i] * (1 + width) / RATE)
        a_lo = 1 - np.exp(-2 * np.pi * freq[i] / (1 + width) / RATE)
        up1 += a_up * (noise[i] - up1)
        up2 += a_up * (up1 - up2)
        lo1 += a_lo * (noise[i] - lo1)
        lo2 += a_lo * (lo1 - lo2)
        out[i] = up2 - lo2
    return t, out


def _growl(rng, seconds, f_start, f_end, drive, breath, formants, ceiling):
    """Gruñido: diente de sierra con vibrato que baja de tono, formantes y aire, saturado y oscurecido."""
    t = t_axis(seconds)
    glide = np.clip(t / seconds, 0, 1)
    f0 = (f_start + (f_end - f_start) * glide) * (1 + 0.025 * np.sin(2 * np.pi * 6.5 * t) + 0.012 * np.sin(2 * np.pi * 31 * t))
    phase = 2 * np.pi * np.cumsum(f0) / RATE
    saw = sum(np.sin(k * phase) / k for k in range(1, 9))
    voiced = sum(w * lowpass(saw, c) for c, w in formants)
    air = lowpass(rng.standard_normal(len(t)), 1800) * breath * (0.6 + 0.4 * np.sin(2 * np.pi * 9 * t))
    body = np.tanh(drive * (voiced + air))
    return t, lowpass(lowpass(body, ceiling), ceiling)


def berserker_roar(rng):
    """Rugido grave y sostenido que se hunde: el Bersérker se enfurece."""
    t, body = _growl(rng, 1.7, 98.0, 58.0, 1.7, 0.4, [(520, 1.0), (1100, 0.5), (1900, 0.15)], 1500)
    amp = np.clip(t / 0.14, 0, 1) * np.exp(-np.maximum(t - 1.0, 0) / 0.32)
    thump = np.sin(2 * np.pi * 52 * t) * env(t, 0.003, 0.22) * 0.9
    return fade_out(normalize(body * amp + thump, 0.85))


def berserker_war_cry(rng):
    """Grito de guerra corto y áspero con un golpe sordo al inicio."""
    t, body = _growl(rng, 1.05, 168.0, 112.0, 2.6, 0.5, [(760, 1.0), (1500, 0.6), (2400, 0.2)], 2100)
    amp = np.clip(t / 0.05, 0, 1) * np.exp(-np.maximum(t - 0.45, 0) / 0.2)
    boom = (np.sin(2 * np.pi * (90 * np.exp(-t * 7) + 38) * t) + 0.5 * lowpass(rng.standard_normal(len(t)), 300)) * env(t, 0.002, 0.2)
    return fade_out(normalize(body * amp * 0.8 + boom, 0.88))


def berserker_slam(rng):
    """Impacto demoledor: golpe de graves que cae, chasquido de roca, retumbo y escombros."""
    t = t_axis(2.2)
    thump = np.sin(2 * np.pi * np.cumsum(34 + 70 * np.exp(-t * 9)) / RATE) * env(t, 0.002, 0.45) * 2.2
    crack = lowpass(rng.standard_normal(len(t)), 3500) * env(t, 0.0008, 0.03) * 0.7
    body = lowpass(rng.standard_normal(len(t)), 380) * env(t, 0.004, 0.34) * 1.6
    rumble = lowpass(rng.standard_normal(len(t)), 85) * env(t, 0.03, 0.9) * 6.0
    debris = np.zeros(len(t))
    for _ in range(46):
        at = 0.12 + rng.random() ** 1.6 * 1.5
        i = int(at * RATE)
        n = int(0.012 * RATE)
        if i + n < len(t):
            debris[i:i + n] += rng.standard_normal(n) * np.exp(-np.arange(n) / (0.003 * RATE)) * (0.5 + rng.random()) * np.exp(-at * 1.3)
    mix = thump + crack + body + rumble + 0.18 * lowpass(debris, 3500)
    return fade_out(normalize(np.tanh(0.9 * mix), 0.92), 0.12)


def berserker_whoosh(rng):
    """Corte de aire de un arma pesada que gira."""
    t, noise = _sweep_noise(rng, 0.55, 300.0, 1500.0, 380.0, 0.8)
    amp = np.sin(np.pi * np.clip(t / 0.55, 0, 1)) ** 1.4
    low = np.sin(2 * np.pi * (60 + 25 * np.sin(np.pi * np.clip(t / 0.55, 0, 1))) * t) * amp * 0.35
    return fade_out(normalize(normalize(noise) * amp + low, 0.8))


def berserker_heartbeat(rng):
    """Latido doble y grave: la furia late en el pecho."""
    t = t_axis(0.7)
    x = np.zeros_like(t)
    for start, gain in ((0.0, 1.0), (0.24, 0.75)):
        tt = np.maximum(t - start, 0)
        x += gain * (np.sin(2 * np.pi * (46 + 40 * np.exp(-tt * 22)) * tt) + 0.5 * np.sin(2 * np.pi * 92 * tt)
                     + 0.3 * np.sin(2 * np.pi * 150 * tt)) * env(tt, 0.004, 0.11) * (t >= start)
    return fade_out(normalize(x, 0.85))


def berserker_leap(rng):
    """Impulso de salto: gruñido seco y un bufido de aire."""
    t, body = _growl(rng, 0.42, 130.0, 210.0, 2.2, 0.45, [(700, 1.0), (1400, 0.4)], 1900)
    amp = np.clip(t / 0.03, 0, 1) * np.exp(-t / 0.13)
    _, air = _sweep_noise(rng, 0.42, 300.0, 1200.0, 500.0, 0.8)
    return fade_out(normalize(body * amp * 0.9 + normalize(air) * np.sin(np.pi * np.clip(t / 0.42, 0, 1)) * 0.45, 0.82))


# ---------------------------------------------------------------- Mago (V83)

def _highpass(x, cutoff):
    return x - lowpass(x, cutoff)


def _bell(t, base, partials, decay):
    """Campana: parciales inarmónicos con caída propia (como eclipse_announce, pero más corta y brillante)."""
    return sum(a * np.sin(2 * np.pi * base * r * t) * env(t, 0.002, decay / (1 + 0.6 * i)) for i, (r, a) in enumerate(partials))


def _soft_air(rng, n, low, high):
    """Ruido limitado a una banda (evita el siseo estridente de un paso alto a secas)."""
    return lowpass(_highpass(rng.standard_normal(n), low), high)


def mage_charge(rng):
    """Carga arcana: un brillo que sube de tono mientras la magia se reúne en la mano."""
    t = t_axis(0.42)
    glide = 520 + 1500 * np.clip(t / 0.4, 0, 1) ** 1.6
    phase = 2 * np.pi * np.cumsum(glide) / RATE
    tone = (np.sin(phase) + 0.4 * np.sin(2 * phase) + 0.15 * np.sin(3.02 * phase)) * np.clip(t / 0.35, 0, 1) ** 1.5
    shimmer = _soft_air(rng, len(t), 2500, 7000) * 0.18 * np.clip(t / 0.4, 0, 1)
    return fade_out(normalize(tone * 0.8 + shimmer, 0.8), 0.06)


def mage_bolt(rng):
    """Rayo arcano: chasquido con cuerpo, un barrido que cae y chispas finas."""
    t = t_axis(0.65)
    sweep = np.sin(2 * np.pi * np.cumsum(2200 * np.exp(-t * 8) + 220) / RATE) * env(t, 0.001, 0.16)
    body = np.sin(2 * np.pi * (420 * np.exp(-t * 6) + 110) * t) * env(t, 0.001, 0.14) * 0.8
    zap = _soft_air(rng, len(t), 1800, 6500) * env(t, 0.0008, 0.05) * 0.4
    sparkle = _bell(t, 1500, [(1.0, 0.5), (1.5, 0.35), (2.3, 0.2)], 0.22) * 0.3
    return fade_out(normalize(sweep + body + zap + sparkle, 0.85))


def mage_blink(rng):
    """Salto arcano: un barrido que se cierra hacia dentro (0,15 s) y un chasquido seco al reaparecer, a la vez que los brazos se abren."""
    t, noise = _sweep_noise(rng, 0.5, 1700.0, 420.0, 1500.0, 0.7)
    amp = np.sin(np.pi * np.clip(t / 0.15, 0, 1)) ** 1.2 * (t < 0.15)
    tone = np.sin(2 * np.pi * np.cumsum(300 + 1500 * np.clip(t / 0.15, 0, 1)) / RATE) * amp * 0.45
    pop_t = np.maximum(t - 0.15, 0)
    pop = (np.sin(2 * np.pi * (170 * np.exp(-pop_t * 22) + 65) * pop_t) * env(pop_t, 0.001, 0.12)
           + _soft_air(rng, len(t), 1200, 5000) * env(pop_t, 0.0005, 0.03) * 0.5
           + _bell(pop_t, 1400, [(1.0, 0.4), (1.5, 0.3)], 0.25) * 0.35) * (t >= 0.15)
    return fade_out(normalize(normalize(noise) * amp * 0.5 + tone + pop, 0.85))


def mage_time(rng):
    """Distorsión temporal: una campana grave y una cuenta de chasquidos que se frena."""
    t = t_axis(1.3)
    bell = _bell(t, 196.0, [(1.0, 1.0), (2.01, 0.6), (2.76, 0.45), (4.07, 0.3), (5.4, 0.2)], 0.9) * 1.4
    ticks = np.zeros(len(t))
    when = 0.12
    gap = 0.08
    for _ in range(9):
        i = int(when * RATE)
        n = int(0.02 * RATE)
        if i + n < len(t):
            ticks[i:i + n] += _soft_air(rng, n, 1500, 5500) * np.exp(-np.arange(n) / (0.004 * RATE)) * 0.6
        when += gap
        gap *= 1.28
    hum = np.sin(2 * np.pi * 98 * t) * env(t, 0.08, 0.7) * 0.6
    return fade_out(normalize(bell + ticks * 0.5 + hum, 0.85), 0.12)


def mage_missile(rng):
    """Misil arcano: un «piu» corto que cae de tono."""
    t = t_axis(0.26)
    f = 1600 * np.exp(-t * 7) + 520
    phase = 2 * np.pi * np.cumsum(f) / RATE
    x = (np.sin(phase) + 0.3 * np.sin(2 * phase)) * env(t, 0.003, 0.08)
    x += _soft_air(rng, len(t), 2500, 6000) * env(t, 0.001, 0.02) * 0.15
    return fade_out(normalize(x, 0.78), 0.04)


def mage_impact(rng):
    """Estallido de cristal: golpe con cuerpo y parciales de vidrio que se apagan."""
    t = t_axis(0.7)
    burst = _soft_air(rng, len(t), 1800, 6500) * env(t, 0.0006, 0.07) * 0.45
    glass = _bell(t, 1900, [(1.0, 0.6), (1.58, 0.5), (2.12, 0.4), (2.9, 0.25)], 0.32) * 0.6
    thump = np.sin(2 * np.pi * (200 * np.exp(-t * 18) + 75) * t) * env(t, 0.001, 0.1) * 0.9
    return fade_out(normalize(burst + glass + thump, 0.85))


# ---------------------------------------------------------------- Asesino (V84)

def assassin_vanish(rng):
    """Bomba de humo: un soplido grave que se abre y se apaga, con un susurro de tela."""
    t = t_axis(0.8)
    puff = _soft_air(rng, len(t), 120, 1400) * env(t, 0.012, 0.16) * 1.0
    thud = np.sin(2 * np.pi * (140 * np.exp(-t * 14) + 55) * t) * env(t, 0.003, 0.1) * 0.7
    cloth = _soft_air(rng, len(t), 1800, 5200) * env(t, 0.03, 0.18) * 0.22
    return fade_out(normalize(puff + thud + cloth, 0.8), 0.1)


def assassin_slash(rng):
    """Tajo: un siseo de filo que cruza y un tintineo metálico corto."""
    t, noise = _sweep_noise(rng, 0.32, 2200.0, 4800.0, 1800.0, 0.8)
    amp = np.sin(np.pi * np.clip(t / 0.3, 0, 1)) ** 1.4
    ring = _bell(t, 2600, [(1.0, 0.5), (1.9, 0.35), (2.7, 0.2)], 0.16) * env(t, 0.05, 0.2) * 0.35
    return fade_out(normalize(normalize(noise) * amp * 0.8 + ring, 0.82), 0.05)


def assassin_step(rng):
    """Paso de sombra: un barrido rápido hacia dentro y un golpe sordo al reaparecer a la espalda."""
    t, noise = _sweep_noise(rng, 0.45, 900.0, 3200.0, 700.0, 0.7)
    amp = np.sin(np.pi * np.clip(t / 0.16, 0, 1)) ** 1.2 * (t < 0.16)
    land_t = np.maximum(t - 0.14, 0)
    land = (np.sin(2 * np.pi * (150 * np.exp(-land_t * 20) + 60) * land_t) * env(land_t, 0.001, 0.12) * 0.9
            + _soft_air(rng, len(t), 600, 3500) * env(land_t, 0.0008, 0.03) * 0.4) * (t >= 0.14)
    return fade_out(normalize(normalize(noise) * amp * 0.5 + land, 0.85), 0.06)


def assassin_throw(rng):
    """Lanzamiento de cuchillas: un zumbido agudo y rápido que se aleja."""
    t, noise = _sweep_noise(rng, 0.3, 3000.0, 6000.0, 2500.0, 0.9)
    amp = env(t, 0.006, 0.07)
    spin = np.sin(2 * np.pi * np.cumsum(1800 * np.exp(-t * 6) + 700) / RATE) * env(t, 0.004, 0.06) * 0.25
    return fade_out(normalize(normalize(noise) * amp * 0.7 + spin, 0.78), 0.05)


def assassin_impact(rng):
    """Clavada: un golpe seco en madera o carne y un tic metálico."""
    t = t_axis(0.35)
    thunk = np.sin(2 * np.pi * (240 * np.exp(-t * 26) + 90) * t) * env(t, 0.001, 0.07) * 0.9
    tick = _soft_air(rng, len(t), 2000, 6500) * env(t, 0.0005, 0.018) * 0.5
    metal = _bell(t, 3100, [(1.0, 0.4), (2.2, 0.25)], 0.1) * 0.25
    return fade_out(normalize(thunk + tick + metal, 0.85), 0.05)


# ---------------------------------------------------------------- Herbolista (V85)

def herbalist_balm(rng):
    """Bálsamo: un acorde cálido y suave que florece, con un roce de hojas."""
    t = t_axis(0.9)
    chord = sum(np.sin(2 * np.pi * f * t) * env(t, 0.03, 0.35 + 0.1 * i) for i, f in enumerate((392.0, 494.0, 587.0, 784.0)))
    bell = _bell(t, 1568, [(1.0, 0.4), (2.0, 0.2), (3.01, 0.1)], 0.45) * 0.25
    leaves = _soft_air(rng, len(t), 1500, 5500) * env(t, 0.02, 0.12) * 0.15
    return fade_out(normalize(chord * 0.5 + bell + leaves, 0.78), 0.15)


def herbalist_rain(rng):
    """Lluvia sanadora: un chaparrón suave que se asienta y unas gotas que tintinean."""
    t = t_axis(1.5)
    rain = _soft_air(rng, len(t), 700, 6500) * np.clip(t / 0.25, 0, 1) * env(t, 0.01, 0.9) * 0.55
    drops = np.zeros(len(t))
    for _ in range(14):
        i = int(rng.uniform(0.05, 1.3) * RATE)
        n = int(0.12 * RATE)
        if i + n < len(t):
            tt = np.arange(n) / RATE
            drops[i:i + n] += np.sin(2 * np.pi * rng.uniform(1100, 2200) * tt) * np.exp(-tt / 0.03) * rng.uniform(0.15, 0.35)
    hum = np.sin(2 * np.pi * 220 * t) * env(t, 0.2, 0.6) * 0.15
    return fade_out(normalize(rain + drops + hum, 0.75), 0.2)


def herbalist_spore(rng):
    """Nube de esporas: un estallido sordo de vaina y un siseo largo y húmedo."""
    t = t_axis(1.0)
    pop = np.sin(2 * np.pi * (160 * np.exp(-t * 24) + 70) * t) * env(t, 0.002, 0.08) * 0.8
    hiss = _soft_air(rng, len(t), 900, 4200) * env(t, 0.03, 0.3) * 0.5
    wet = _soft_air(rng, len(t), 200, 900) * env(t, 0.05, 0.4) * 0.5
    return fade_out(normalize(pop + hiss + wet, 0.78), 0.15)


# ---------------------------------------------------------------- Ingeniero (V86)

def engineer_grapple(rng):
    """Garfio: un disparo de muelle, un zumbido de cable que se tensa y un golpe metálico al clavarse."""
    t = t_axis(0.55)
    shot = np.sin(2 * np.pi * (260 * np.exp(-t * 30) + 90) * t) * env(t, 0.001, 0.05) * 0.8
    cable = np.sin(2 * np.pi * np.cumsum(500 + 1800 * np.clip(t / 0.18, 0, 1)) / RATE) * env(t, 0.004, 0.09) * 0.3 * (t < 0.2)
    zip_ = _soft_air(rng, len(t), 2200, 6500) * env(t, 0.004, 0.08) * 0.35
    hit_t = np.maximum(t - 0.17, 0)
    hit = (_bell(hit_t, 1800, [(1.0, 0.5), (1.6, 0.3), (2.4, 0.2)], 0.22) * 0.45
           + np.sin(2 * np.pi * (180 * np.exp(-hit_t * 28) + 80) * hit_t) * env(hit_t, 0.001, 0.07) * 0.8) * (t >= 0.17)
    return fade_out(normalize(shot + cable + zip_ + hit, 0.82), 0.08)


def engineer_throw(rng):
    """Lanzamiento de granada: un zumbido grave y un pitido de espoleta."""
    t = t_axis(0.35)
    whoosh = _soft_air(rng, len(t), 300, 2400) * env(t, 0.03, 0.09) * 0.7
    beep = np.sin(2 * np.pi * 1900 * t) * (np.sin(2 * np.pi * 18 * t) > 0) * env(t, 0.01, 0.2) * 0.2
    return fade_out(normalize(whoosh + beep, 0.75), 0.06)


def engineer_blast(rng):
    """Pulso eléctrico: golpe grave, chasquido de chispas y un zumbido que se apaga."""
    t = t_axis(1.0)
    boom = np.sin(2 * np.pi * (130 * np.exp(-t * 9) + 40) * t) * env(t, 0.002, 0.2) * 1.0
    crack = _soft_air(rng, len(t), 800, 6000) * env(t, 0.0008, 0.09) * 0.6
    sparks = np.zeros(len(t))
    for _ in range(18):
        i = int(rng.uniform(0.02, 0.5) * RATE)
        n = int(0.02 * RATE)
        if i + n < len(t):
            sparks[i:i + n] += _soft_air(rng, n, 2500, 7000) * np.exp(-np.arange(n) / (0.005 * RATE)) * rng.uniform(0.2, 0.5)
    hum = np.sin(2 * np.pi * 120 * t) * env(t, 0.05, 0.3) * 0.25
    return fade_out(normalize(boom + crack + sparks * 0.5 + hum, 0.85), 0.15)


# ---------------------------------------------------------------- Lancero, Espadachín y Paladín (V87)

def lancer_leap(rng):
    """Salto del dragón: un aleteo grave y un siseo de aire que sube."""
    t = t_axis(0.8)
    flap = sum(_soft_air(rng, len(t), 80, 700) * np.exp(-((t - c) / 0.07) ** 2) for c in (0.08, 0.3)) * 1.0
    air = _soft_air(rng, len(t), 400, 3500) * env(t, 0.12, 0.3) * 0.4
    thump = np.sin(2 * np.pi * (110 * np.exp(-t * 14) + 50) * t) * env(t, 0.002, 0.1) * 0.7
    return fade_out(normalize(flap + air + thump, 0.8), 0.12)


def lancer_thrust(rng):
    """Estocada: un silbido agudo y recto y un golpe seco."""
    t = t_axis(0.4)
    whistle = np.sin(2 * np.pi * np.cumsum(900 + 1600 * np.clip(t / 0.12, 0, 1)) / RATE) * env(t, 0.004, 0.06) * 0.35
    air = _soft_air(rng, len(t), 1500, 6000) * env(t, 0.004, 0.07) * 0.5
    hit_t = np.maximum(t - 0.1, 0)
    hit = (np.sin(2 * np.pi * (200 * np.exp(-hit_t * 26) + 85) * hit_t) * env(hit_t, 0.001, 0.06) * 0.9) * (t >= 0.1)
    return fade_out(normalize(whistle + air + hit, 0.82), 0.05)


def lancer_slam(rng):
    """Aterrizaje del dragón: golpe profundo, grava que salta y un eco."""
    t = t_axis(1.0)
    boom = np.sin(2 * np.pi * (95 * np.exp(-t * 7) + 32) * t) * env(t, 0.002, 0.22) * 1.0
    gravel = _soft_air(rng, len(t), 300, 3000) * env(t, 0.002, 0.14) * 0.6
    crack = _soft_air(rng, len(t), 2000, 6500) * env(t, 0.0006, 0.03) * 0.4
    return fade_out(normalize(boom + gravel + crack, 0.88), 0.15)


def swordsman_draw(rng):
    """Iaido: un roce de vaina, un siseo largo de filo y un tintineo."""
    t, noise = _sweep_noise(rng, 0.45, 1500.0, 5200.0, 2200.0, 0.8)
    amp = np.sin(np.pi * np.clip(t / 0.4, 0, 1)) ** 1.5
    ring = _bell(t, 3200, [(1.0, 0.5), (2.1, 0.3), (3.3, 0.15)], 0.3) * env(t, 0.08, 0.3) * 0.3
    scrape = _soft_air(rng, len(t), 700, 2500) * env(t, 0.01, 0.06) * 0.3
    return fade_out(normalize(normalize(noise) * amp * 0.8 + ring + scrape, 0.82), 0.08)


def swordsman_cut(rng):
    """Corte rápido: un chasquido de filo breve (se reproduce con distinto tono en cada tajo)."""
    t = t_axis(0.2)
    air = _soft_air(rng, len(t), 2500, 7000) * env(t, 0.002, 0.04) * 0.8
    ring = _bell(t, 2900, [(1.0, 0.5), (1.9, 0.3)], 0.09) * 0.4
    return fade_out(normalize(air + ring, 0.8), 0.03)


def paladin_consecrate(rng):
    """Consagración: un acorde luminoso y grave que se abre como un coro, con campanas."""
    t = t_axis(1.4)
    chord = sum(np.sin(2 * np.pi * f * t) * env(t, 0.25, 0.7) for f in (174.6, 220.0, 261.6, 349.2)) * 0.35
    shimmer = _bell(t, 1046.5, [(1.0, 0.4), (1.5, 0.25), (2.0, 0.2)], 0.9) * 0.4
    air = _soft_air(rng, len(t), 1500, 5000) * env(t, 0.2, 0.4) * 0.1
    return fade_out(normalize(chord + shimmer + air, 0.78), 0.25)


def paladin_hammer(rng):
    """Martillo de la justicia: yunque pesado, campana sagrada y un trueno corto."""
    t = t_axis(1.0)
    anvil = _bell(t, 520.0, [(1.0, 0.8), (2.4, 0.5), (3.9, 0.3), (5.2, 0.2)], 0.5) * 0.7
    thud = np.sin(2 * np.pi * (120 * np.exp(-t * 16) + 45) * t) * env(t, 0.001, 0.14) * 1.0
    crack = _soft_air(rng, len(t), 1500, 6500) * env(t, 0.0006, 0.04) * 0.5
    holy = _bell(t, 1318.5, [(1.0, 0.4), (1.5, 0.3)], 0.7) * env(t, 0.12, 0.5) * 0.25
    return fade_out(normalize(anvil + thud + crack + holy, 0.85), 0.15)


# ---------------------------------------------------------------- Nigromante, Druida, Mercader y Monje (V88)

def necromancer_spear(rng):
    """Lanza de hueso: un siseo grave, un chasquido de hueso y un susurro."""
    t = t_axis(0.55)
    whoosh = _soft_air(rng, len(t), 200, 2200) * env(t, 0.03, 0.1) * 0.7
    clack = _bell(t, 760, [(1.0, 0.5), (2.7, 0.4), (4.9, 0.25)], 0.06) * 0.5
    thud = np.sin(2 * np.pi * (140 * np.exp(-t * 20) + 60) * t) * env(t, 0.002, 0.09) * 0.6
    whisper = _soft_air(rng, len(t), 1200, 3500) * env(t, 0.1, 0.25) * 0.15
    return fade_out(normalize(whoosh + clack + thud + whisper, 0.8), 0.1)


def necromancer_siphon(rng):
    """Sifón de almas: un lamento que gira y se arrastra hacia dentro."""
    t = t_axis(0.9)
    wail = np.sin(2 * np.pi * np.cumsum(520 - 260 * np.clip(t / 0.8, 0, 1) + 25 * np.sin(2 * np.pi * 6 * t)) / RATE) * env(t, 0.08, 0.5) * 0.5
    hum = np.sin(2 * np.pi * 70 * t) * env(t, 0.1, 0.5) * 0.4
    air = _soft_air(rng, len(t), 800, 3200) * env(t, 0.1, 0.3) * 0.3
    return fade_out(normalize(wail + hum + air, 0.78), 0.2)


def druid_howl(rng):
    """Aullido de la manada: un tono que sube y baja con vibrato y aire."""
    t = t_axis(1.3)
    glide = 330 + 260 * np.sin(np.pi * np.clip(t / 1.1, 0, 1)) ** 0.8 + 8 * np.sin(2 * np.pi * 5.5 * t)
    ph = 2 * np.pi * np.cumsum(glide) / RATE
    tone = (np.sin(ph) + 0.35 * np.sin(2 * ph) + 0.12 * np.sin(3 * ph)) * env(t, 0.15, 0.8)
    breath = _soft_air(rng, len(t), 600, 2800) * env(t, 0.1, 0.5) * 0.12
    return fade_out(normalize(tone * 0.7 + breath, 0.78), 0.25)


def druid_thorns(rng):
    """Tormenta de espinas: un crujido de zarzas y chasquidos secos."""
    t = t_axis(0.8)
    rustle = _soft_air(rng, len(t), 900, 5000) * env(t, 0.03, 0.25) * 0.5
    snaps = np.zeros(len(t))
    for _ in range(12):
        i = int(rng.uniform(0.02, 0.5) * RATE)
        n = int(0.02 * RATE)
        if i + n < len(t):
            snaps[i:i + n] += _soft_air(rng, n, 1500, 6500) * np.exp(-np.arange(n) / (0.004 * RATE)) * rng.uniform(0.3, 0.7)
    low = np.sin(2 * np.pi * (110 * np.exp(-t * 10) + 50) * t) * env(t, 0.004, 0.12) * 0.5
    return fade_out(normalize(rustle + snaps * 0.6 + low, 0.8), 0.12)


def merchant_coin(rng):
    """Moneda lanzada: un tintineo agudo y un zumbido rápido."""
    t = t_axis(0.45)
    ting = _bell(t, 2400, [(1.0, 0.6), (2.3, 0.4), (3.7, 0.2)], 0.2) * 0.6
    zip_ = _soft_air(rng, len(t), 2500, 7000) * env(t, 0.003, 0.05) * 0.4
    hit_t = np.maximum(t - 0.09, 0)
    hit = np.sin(2 * np.pi * (200 * np.exp(-hit_t * 28) + 90) * hit_t) * env(hit_t, 0.001, 0.06) * 0.6 * (t >= 0.09)
    return fade_out(normalize(ting + zip_ + hit, 0.8), 0.06)


def merchant_guard(rng):
    """Guardia dorada: un arpegio de campanillas que sube y un brillo."""
    t = t_axis(1.0)
    out = np.zeros(len(t))
    for k, f in enumerate((784.0, 988.0, 1175.0, 1568.0)):
        tt = np.maximum(t - 0.07 * k, 0)
        out += _bell(tt, f, [(1.0, 0.5), (2.0, 0.2)], 0.35) * (t >= 0.07 * k)
    shimmer = _soft_air(rng, len(t), 3000, 8000) * env(t, 0.2, 0.3) * 0.08
    return fade_out(normalize(out + shimmer, 0.78), 0.2)


def monk_kick(rng):
    """Patada voladora: un barrido de aire y un golpe seco con cuerpo."""
    t, noise = _sweep_noise(rng, 0.4, 600.0, 2800.0, 900.0, 0.8)
    amp = np.sin(np.pi * np.clip(t / 0.18, 0, 1)) ** 1.2 * (t < 0.18)
    hit_t = np.maximum(t - 0.15, 0)
    hit = (np.sin(2 * np.pi * (170 * np.exp(-hit_t * 22) + 65) * hit_t) * env(hit_t, 0.001, 0.1) * 1.0
           + _soft_air(rng, len(t), 800, 4500) * env(hit_t, 0.0006, 0.04) * 0.5) * (t >= 0.15)
    return fade_out(normalize(normalize(noise) * amp * 0.5 + hit, 0.85), 0.06)


def monk_fist(rng):
    """Puñetazo: un golpe corto y seco (se reproduce con distinto tono en cada golpe)."""
    t = t_axis(0.18)
    thud = np.sin(2 * np.pi * (220 * np.exp(-t * 30) + 95) * t) * env(t, 0.001, 0.05) * 0.9
    slap = _soft_air(rng, len(t), 1000, 5000) * env(t, 0.0005, 0.02) * 0.5
    return fade_out(normalize(thud + slap, 0.82), 0.03)


# ---------------------------------------------------------------- Pistolero, Invocador de armas y Cambiaformas (V89)

def gunslinger_shot(rng):
    """Disparo arcano: un chasquido agudo con cola eléctrica."""
    t = t_axis(0.4)
    crack = _soft_air(rng, len(t), 1800, 7000) * env(t, 0.0006, 0.035) * 0.8
    zap = np.sin(2 * np.pi * np.cumsum(2600 * np.exp(-t * 14) + 380) / RATE) * env(t, 0.001, 0.07) * 0.5
    thump = np.sin(2 * np.pi * (180 * np.exp(-t * 26) + 80) * t) * env(t, 0.001, 0.06) * 0.7
    tail = _bell(t, 1900, [(1.0, 0.3), (1.7, 0.2)], 0.12) * 0.25
    return fade_out(normalize(crack + zap + thump + tail, 0.84), 0.05)


def gunslinger_mark(rng):
    """Marca de Ojo de halcón: una campanilla de cierre y un clic de mira."""
    t = t_axis(0.6)
    bell = _bell(t, 1480, [(1.0, 0.6), (2.0, 0.3), (3.0, 0.15)], 0.3) * 0.7
    click = _soft_air(rng, len(t), 2500, 7000) * env(t, 0.0005, 0.012) * 0.5
    lock = np.sin(2 * np.pi * 190 * t) * env(t, 0.003, 0.05) * 0.4
    return fade_out(normalize(bell + click + lock, 0.78), 0.1)


def weapon_greatsword(rng):
    """Espadón espectral: un barrido pesado y un golpe de acero con eco."""
    t, noise = _sweep_noise(rng, 0.7, 500.0, 2400.0, 600.0, 0.8)
    amp = np.sin(np.pi * np.clip(t / 0.35, 0, 1)) ** 1.3 * (t < 0.35)
    hit_t = np.maximum(t - 0.3, 0)
    hit = (_bell(hit_t, 620, [(1.0, 0.6), (2.3, 0.4), (3.8, 0.2)], 0.35) * 0.6
           + np.sin(2 * np.pi * (130 * np.exp(-hit_t * 14) + 50) * hit_t) * env(hit_t, 0.001, 0.12) * 0.9) * (t >= 0.3)
    return fade_out(normalize(normalize(noise) * amp * 0.6 + hit, 0.85), 0.1)


def weapon_prison(rng):
    """Prisión de espadas: cadenas que se tensan y un tintineo de filos cerrándose."""
    t = t_axis(0.8)
    chains = np.zeros(len(t))
    for k in range(7):
        i = int((0.04 + 0.07 * k) * RATE)
        n = int(0.05 * RATE)
        if i + n < len(t):
            chains[i:i + n] += _soft_air(rng, n, 1500, 5500) * np.exp(-np.arange(n) / (0.012 * RATE)) * 0.6
    ring = _bell(t, 2100, [(1.0, 0.5), (1.9, 0.3), (3.1, 0.2)], 0.28) * env(t, 0.15, 0.4) * 0.4
    low = np.sin(2 * np.pi * 85 * t) * env(t, 0.05, 0.2) * 0.4
    return fade_out(normalize(chains + ring + low, 0.78), 0.12)


def shapeshifter_pounce(rng):
    """Zarpazo: un bufido corto, un gruñido que cae y el golpe de la garra."""
    t = t_axis(0.5)
    breath = _soft_air(rng, len(t), 500, 3000) * env(t, 0.02, 0.07) * 0.7
    growl = np.sin(2 * np.pi * np.cumsum(240 - 110 * np.clip(t / 0.3, 0, 1)) / RATE) * (np.sin(2 * np.pi * 38 * t) > -0.3) * env(t, 0.02, 0.12) * 0.4
    hit_t = np.maximum(t - 0.14, 0)
    hit = (np.sin(2 * np.pi * (170 * np.exp(-hit_t * 22) + 70) * hit_t) * env(hit_t, 0.001, 0.08) * 0.9
           + _soft_air(rng, len(t), 1800, 6000) * env(hit_t, 0.0005, 0.03) * 0.4) * (t >= 0.14)
    return fade_out(normalize(breath + growl + hit, 0.85), 0.07)


def shapeshifter_howl(rng):
    """Aullido de la bestia: un rugido grave con onda sónica."""
    t = t_axis(1.1)
    f = 150 + 60 * np.sin(np.pi * np.clip(t / 0.9, 0, 1)) + 6 * np.sin(2 * np.pi * 7 * t)
    ph = 2 * np.pi * np.cumsum(f) / RATE
    saw = (np.sin(ph) + 0.5 * np.sin(2 * ph) + 0.3 * np.sin(3 * ph)) * (0.6 + 0.4 * np.sin(2 * np.pi * 30 * t)) * env(t, 0.06, 0.55)
    boom = np.sin(2 * np.pi * (90 * np.exp(-t * 6) + 40) * t) * env(t, 0.003, 0.2) * 0.6
    air = _soft_air(rng, len(t), 400, 2500) * env(t, 0.05, 0.3) * 0.25
    return fade_out(normalize(saw * 0.6 + boom + air, 0.82), 0.2)


# ---------------------------------------------------------------- Cazador, Bardo, Brujo y Domador (V90)

def hunter_shot(rng):
    """Disparo de arco: cuerda que vibra, flecha que silba y un golpe seco."""
    t = t_axis(0.5)
    twang = np.sin(2 * np.pi * 190 * t) * env(t, 0.001, 0.07) * 0.6 + np.sin(2 * np.pi * 380 * t) * env(t, 0.001, 0.04) * 0.3
    whistle = np.sin(2 * np.pi * np.cumsum(1700 - 900 * np.clip(t / 0.3, 0, 1)) / RATE) * env(t, 0.01, 0.1) * 0.15
    air = _soft_air(rng, len(t), 1800, 6000) * env(t, 0.003, 0.05) * 0.35
    hit_t = np.maximum(t - 0.22, 0)
    hit = np.sin(2 * np.pi * (200 * np.exp(-hit_t * 28) + 85) * hit_t) * env(hit_t, 0.001, 0.05) * 0.7 * (t >= 0.22)
    return fade_out(normalize(twang + whistle + air + hit, 0.8), 0.06)


def hunter_trap(rng):
    """Trampa de oso: un chasquido metálico seco con eco de resorte."""
    t = t_axis(0.6)
    clank = _bell(t, 480, [(1.0, 0.7), (2.9, 0.5), (4.6, 0.3), (6.3, 0.2)], 0.12) * 0.7
    snap = _soft_air(rng, len(t), 1500, 6500) * env(t, 0.0005, 0.03) * 0.6
    thud = np.sin(2 * np.pi * (150 * np.exp(-t * 24) + 60) * t) * env(t, 0.001, 0.08) * 0.8
    spring = _bell(t, 1400, [(1.0, 0.3), (1.6, 0.2)], 0.3) * 0.2
    return fade_out(normalize(clank + snap + thud + spring, 0.85), 0.08)


def hunter_falcon(rng):
    """Halcón: un grito agudo que baja y un aleteo."""
    t = t_axis(0.7)
    cry = np.sin(2 * np.pi * np.cumsum(3200 - 1400 * np.clip(t / 0.4, 0, 1) + 120 * np.sin(2 * np.pi * 18 * t)) / RATE) * env(t, 0.02, 0.18) * 0.45
    flap = _soft_air(rng, len(t), 200, 1500) * (0.5 + 0.5 * np.sin(2 * np.pi * 14 * t)) * env(t, 0.05, 0.3) * 0.4
    return fade_out(normalize(cry + flap, 0.75), 0.1)


def bard_song(rng):
    """Canción: un acorde de laúd que se abre en campanas."""
    t = t_axis(1.2)
    out = np.zeros(len(t))
    for k, f in enumerate((293.7, 370.0, 440.0, 587.3)):
        tt = np.maximum(t - 0.05 * k, 0)
        out += np.sin(2 * np.pi * f * tt) * env(tt, 0.004, 0.5) * (t >= 0.05 * k) * 0.5
        out += np.sin(2 * np.pi * f * 2 * tt) * env(tt, 0.004, 0.25) * (t >= 0.05 * k) * 0.2
    bell = _bell(t, 1174.7, [(1.0, 0.4), (2.0, 0.2)], 0.6) * 0.3
    return fade_out(normalize(out + bell, 0.78), 0.2)


def bard_dissonance(rng):
    """Disonancia: dos tonos chocando y un chirrido que sacude."""
    t = t_axis(0.8)
    a = np.sin(2 * np.pi * 440 * t) + np.sin(2 * np.pi * 466 * t) + 0.6 * np.sin(2 * np.pi * 622 * t)
    a *= env(t, 0.005, 0.35) * 0.4
    screech = _soft_air(rng, len(t), 2000, 6500) * env(t, 0.01, 0.12) * 0.45
    thump = np.sin(2 * np.pi * (120 * np.exp(-t * 14) + 50) * t) * env(t, 0.002, 0.1) * 0.8
    return fade_out(normalize(a + screech + thump, 0.8), 0.1)


def warlock_curse(rng):
    """Maldición: un susurro que se enrosca y baja de tono."""
    t = t_axis(0.9)
    whisper = _soft_air(rng, len(t), 1200, 5000) * (0.6 + 0.4 * np.sin(2 * np.pi * 9 * t)) * env(t, 0.1, 0.4) * 0.5
    drone = np.sin(2 * np.pi * np.cumsum(180 - 70 * np.clip(t / 0.8, 0, 1)) / RATE) * env(t, 0.1, 0.5) * 0.5
    return fade_out(normalize(whisper + drone, 0.75), 0.2)


def warlock_bolt(rng):
    """Rayo abismal: un chasquido grave con cola de sombra."""
    t = t_axis(0.7)
    boom = np.sin(2 * np.pi * (200 * np.exp(-t * 12) + 55) * t) * env(t, 0.002, 0.18) * 0.9
    zap = np.sin(2 * np.pi * np.cumsum(1500 * np.exp(-t * 16) + 160) / RATE) * env(t, 0.001, 0.08) * 0.5
    crack = _soft_air(rng, len(t), 1000, 5500) * env(t, 0.0008, 0.05) * 0.5
    tail = _soft_air(rng, len(t), 150, 900) * env(t, 0.05, 0.3) * 0.35
    return fade_out(normalize(boom + zap + crack + tail, 0.84), 0.1)


def warlock_imps(rng):
    """Diablillos: risitas agudas que revolotean y un estallido de azufre."""
    t = t_axis(0.9)
    laugh = np.zeros(len(t))
    for k in range(5):
        i = int((0.05 + 0.1 * k) * RATE)
        n = int(0.09 * RATE)
        if i + n < len(t):
            tt = np.arange(n) / RATE
            laugh[i:i + n] += np.sin(2 * np.pi * np.cumsum(900 + 300 * rng.random() + 500 * tt / 0.09) / RATE * np.ones(n)) * np.exp(-tt / 0.04) * 0.3
    burst = _soft_air(rng, len(t), 400, 3500) * env(t, 0.02, 0.18) * 0.5
    thud = np.sin(2 * np.pi * (110 * np.exp(-t * 14) + 45) * t) * env(t, 0.003, 0.1) * 0.7
    return fade_out(normalize(laugh + burst + thud, 0.8), 0.15)


def warlock_doom(rng):
    """Perdición: una campanada grave y un estruendo."""
    t = t_axis(1.4)
    bell = _bell(t, 110.0, [(1.0, 0.9), (2.4, 0.6), (4.1, 0.4), (5.9, 0.25)], 1.2) * 1.2
    boom = np.sin(2 * np.pi * (80 * np.exp(-t * 5) + 35) * t) * env(t, 0.004, 0.4) * 0.8
    air = _soft_air(rng, len(t), 200, 1800) * env(t, 0.02, 0.3) * 0.25
    return fade_out(normalize(bell + boom + air, 0.85), 0.3)


def tamer_command(rng):
    """Orden de la manada: un ladrido grave seguido de un gruñido."""
    t = t_axis(0.8)
    bark = np.sin(2 * np.pi * np.cumsum(300 * np.exp(-t * 14) + 170) / RATE) * env(t, 0.004, 0.09) * 0.8
    growl = np.sin(2 * np.pi * np.cumsum(130 + 20 * np.sin(2 * np.pi * 30 * t)) / RATE) * (np.sin(2 * np.pi * 36 * t) > -0.2) * env(np.maximum(t - 0.2, 0), 0.05, 0.25) * 0.5 * (t >= 0.2)
    air = _soft_air(rng, len(t), 400, 2500) * env(t, 0.005, 0.1) * 0.3
    return fade_out(normalize(bark + growl + air, 0.8), 0.12)


def tamer_stampede(rng):
    """Estampida: cascos que retumban y un empujón sordo."""
    t = t_axis(0.9)
    hooves = np.zeros(len(t))
    for k in range(8):
        i = int((0.02 + 0.075 * k) * RATE)
        n = int(0.1 * RATE)
        if i + n < len(t):
            tt = np.arange(n) / RATE
            hooves[i:i + n] += np.sin(2 * np.pi * (140 * np.exp(-tt * 30) + 60) * tt) * np.exp(-tt / 0.03) * (0.5 + 0.1 * k)
    dust = _soft_air(rng, len(t), 200, 2000) * env(t, 0.05, 0.4) * 0.35
    return fade_out(normalize(hooves + dust, 0.84), 0.15)


# ---------------------------------------------------------------- Elementalista (V91)

def element_fire(rng):
    """Fuego: un rugido de llama que crepita."""
    t = t_axis(0.8)
    roar = _soft_air(rng, len(t), 150, 2200) * env(t, 0.02, 0.25) * 0.8
    crackle = np.zeros(len(t))
    for _ in range(16):
        i = int(rng.uniform(0.02, 0.6) * RATE)
        n = int(0.012 * RATE)
        if i + n < len(t):
            crackle[i:i + n] += _soft_air(rng, n, 1500, 6000) * np.exp(-np.arange(n) / (0.003 * RATE)) * rng.uniform(0.3, 0.8)
    low = np.sin(2 * np.pi * (100 * np.exp(-t * 8) + 45) * t) * env(t, 0.004, 0.2) * 0.6
    return fade_out(normalize(roar + crackle * 0.6 + low, 0.82), 0.12)


def element_ice(rng):
    """Hielo: un tintineo de cristal que se quiebra."""
    t = t_axis(0.8)
    glass = _bell(t, 2300, [(1.0, 0.6), (1.7, 0.4), (2.6, 0.3), (3.9, 0.2)], 0.3) * 0.7
    crack = _soft_air(rng, len(t), 3000, 8000) * env(t, 0.0005, 0.03) * 0.5
    low = np.sin(2 * np.pi * (170 * np.exp(-t * 16) + 70) * t) * env(t, 0.002, 0.08) * 0.5
    return fade_out(normalize(glass + crack + low, 0.8), 0.12)


def element_lightning(rng):
    """Rayo: un chasquido seco con eco de trueno."""
    t = t_axis(0.9)
    crack = _soft_air(rng, len(t), 2000, 8000) * env(t, 0.0003, 0.025) * 0.9
    zap = np.sin(2 * np.pi * np.cumsum(3000 * np.exp(-t * 25) + 250) / RATE) * env(t, 0.0008, 0.06) * 0.5
    thunder = _soft_air(rng, len(t), 40, 500) * env(t, 0.04, 0.35) * 0.9
    return fade_out(normalize(crack + zap + thunder, 0.88), 0.15)


def element_earth(rng):
    """Tierra: un retumbo grave con grava."""
    t = t_axis(0.9)
    rumble = np.sin(2 * np.pi * (80 * np.exp(-t * 5) + 32) * t) * env(t, 0.004, 0.3) * 1.0
    gravel = _soft_air(rng, len(t), 250, 2500) * env(t, 0.01, 0.2) * 0.7
    crack = _soft_air(rng, len(t), 1500, 5000) * env(t, 0.0006, 0.04) * 0.4
    return fade_out(normalize(rumble + gravel + crack, 0.86), 0.15)


def element_wind(rng):
    """Viento: una ráfaga que sube y se aleja."""
    t, noise = _sweep_noise(rng, 0.9, 400.0, 2400.0, 700.0, 0.9)
    amp = np.sin(np.pi * np.clip(t / 0.85, 0, 1)) ** 1.2
    whistle = np.sin(2 * np.pi * np.cumsum(900 + 500 * np.sin(np.pi * np.clip(t / 0.8, 0, 1))) / RATE) * env(t, 0.1, 0.3) * 0.12
    return fade_out(normalize(normalize(noise) * amp * 0.9 + whistle, 0.8), 0.15)


def element_water(rng):
    """Agua: un chapoteo con burbujas."""
    t = t_axis(0.8)
    splash = _soft_air(rng, len(t), 600, 5500) * env(t, 0.004, 0.12) * 0.8
    bubbles = np.zeros(len(t))
    for _ in range(6):
        i = int(rng.uniform(0.05, 0.5) * RATE)
        n = int(0.08 * RATE)
        if i + n < len(t):
            tt = np.arange(n) / RATE
            bubbles[i:i + n] += np.sin(2 * np.pi * np.cumsum(500 + 1800 * tt / 0.08) / RATE * np.ones(n)) * np.exp(-tt / 0.03) * rng.uniform(0.2, 0.4)
    low = np.sin(2 * np.pi * (120 * np.exp(-t * 14) + 50) * t) * env(t, 0.003, 0.1) * 0.6
    return fade_out(normalize(splash + bubbles + low, 0.8), 0.12)


def element_magic(rng):
    """Luz y cristal: un acorde de campanas brillantes."""
    t = t_axis(1.0)
    out = np.zeros(len(t))
    for k, f in enumerate((1046.5, 1318.5, 1568.0, 2093.0)):
        tt = np.maximum(t - 0.04 * k, 0)
        out += _bell(tt, f, [(1.0, 0.5), (2.0, 0.2)], 0.5) * (t >= 0.04 * k)
    shimmer = _soft_air(rng, len(t), 3500, 9000) * env(t, 0.1, 0.3) * 0.1
    thump = np.sin(2 * np.pi * (140 * np.exp(-t * 14) + 55) * t) * env(t, 0.003, 0.1) * 0.5
    return fade_out(normalize(out + shimmer + thump, 0.78), 0.2)


def element_dark(rng):
    """Sombra y vacío: un latido grave que se traga el aire."""
    t = t_axis(1.0)
    sub = np.sin(2 * np.pi * (70 * np.exp(-t * 4) + 30) * t) * env(t, 0.02, 0.4) * 1.0
    suck = _soft_air(rng, len(t), 200, 1800) * (1 - np.clip(t / 0.5, 0, 1)) * env(t, 0.01, 0.25) * 0.6
    whisper = _soft_air(rng, len(t), 1500, 5000) * env(t, 0.15, 0.3) * 0.2
    return fade_out(normalize(sub + suck + whisper, 0.82), 0.2)


def ossuary_sound(rng, duration, knocks=(), scrape=0.0, pulse=0.0, rise=False):
    """Percusión de hueso, arrastre de tierra e impulso hueco en capas independientes."""
    t = t_axis(duration)
    noise = rng.standard_normal(len(t))
    dust = lowpass(noise, 1150) - .7 * lowpass(noise, 190)
    swell = np.sin(np.pi * np.minimum(t / duration, 1)) ** (2 if rise else 1)
    grit = scrape * dust * swell * (0.6 + 0.4 * np.sin(2 * np.pi * 21 * t) ** 2)
    voice_phase = 2 * np.pi * (64 * t + (24 if rise else -18) * t * t / duration)
    voice = pulse * (np.sin(voice_phase) + .34 * np.sin(voice_phase * 2.17)) * swell
    voice *= .7 + .3 * np.sin(2 * np.pi * 13 * t)
    result = grit + voice
    for start, gain, pitch in knocks:
        after = np.maximum(t - start, 0)
        # Parciales rígidos al principio, tierra y cámara baja en la cola.
        snap = sum(weight * np.sin(2 * np.pi * pitch * ratio * after) * np.exp(-after / decay)
                   for ratio, weight, decay in [(1, .70, .053), (2.31, .37, .023), (4.78, .18, .012)])
        rumble = .6 * lowpass(noise, 350) * np.exp(-after / .11)
        result += gain * (snap + rumble) * np.minimum(after / .003, 1) * (t >= start)
    return fade_out(normalize(result, .82), .045)


def osario_ambient(rng):
    return ossuary_sound(rng, .92, [(.14, .25, 214), (.63, .22, 182)], .48, .25)


def osario_alert(rng):
    return ossuary_sound(rng, .61, [(.06, .38, 182), (.18, .55, 236)], .41, .74, True)


def osario_hurt(rng):
    return ossuary_sound(rng, .30, [(.008, .72, 253), (.084, .45, 183)], .19, .14)


def osario_death(rng):
    return ossuary_sound(rng, .91, [(.08, .35, 216), (.42, .65, 128), (.67, 1., 83), (.76, .28, 158)], .65, .19)


def osario_claw(rng):
    return ossuary_sound(rng, .42, [(.015, .27, 346), (.30, .79, 274)], .59, .11)


def osario_heavy(rng):
    return ossuary_sound(rng, .81, [(.04, .35, 190), (.33, .30, 208), (.55, .94, 147)], .82, .28, True)


def osario_slam_charge(rng):
    return ossuary_sound(rng, .66, [(.10, .24, 138), (.28, .37, 159), (.49, .39, 182)], .72, .55, True)


def osario_slam_hit(rng):
    return ossuary_sound(rng, .57, [(.006, 1.35, 74), (.04, .52, 168), (.12, .37, 220), (.21, .29, 135)], .70, .34)


def osario_lunge(rng):
    return ossuary_sound(rng, .77, [(.025, .37, 203), (.50, .69, 101), (.65, .62, 133)], .65, .73, True)


def osario_step(rng):
    return ossuary_sound(rng, .26, [(.005, .63, 146), (.071, .39, 197)], .72)


# ---------------------------------------------------------------- Feedback compartido de combate (V95)

def feedback_heavy(rng):
    """Impacto pesado: golpe grave con crujido corto."""
    t = t_axis(0.35)
    thud = np.sin(2 * np.pi * (110 * np.exp(-t * 22) + 48) * t) * env(t, 0.001, 0.09) * 1.0
    crunch = _soft_air(rng, len(t), 500, 3500) * env(t, 0.0008, 0.05) * 0.6
    return fade_out(normalize(thud + crunch, 0.82), 0.05)


def feedback_crit(rng):
    """Crítico: chasquido agudo y un acento metálico."""
    t = t_axis(0.4)
    crack = _soft_air(rng, len(t), 2500, 8000) * env(t, 0.0004, 0.03) * 0.8
    ping = _bell(t, 2700, [(1.0, 0.6), (2.0, 0.3), (3.2, 0.15)], 0.16) * 0.7
    body = np.sin(2 * np.pi * (200 * np.exp(-t * 26) + 90) * t) * env(t, 0.001, 0.05) * 0.5
    return fade_out(normalize(crack + ping + body, 0.82), 0.06)


def feedback_block(rng):
    """Bloqueo: choque metálico con eco."""
    t = t_axis(0.5)
    clang = _bell(t, 640, [(1.0, 0.8), (2.4, 0.6), (4.1, 0.35), (5.8, 0.2)], 0.22) * 0.8
    spark = _soft_air(rng, len(t), 2000, 7000) * env(t, 0.0005, 0.03) * 0.5
    thump = np.sin(2 * np.pi * (140 * np.exp(-t * 24) + 60) * t) * env(t, 0.001, 0.06) * 0.6
    return fade_out(normalize(clang + spark + thump, 0.84), 0.08)


def feedback_heal(rng):
    """Curación efectiva: dos notas cálidas que suben."""
    t = t_axis(0.6)
    out = np.zeros(len(t))
    for k, f in enumerate((659.3, 987.8)):
        tt = np.maximum(t - 0.11 * k, 0)
        out += np.sin(2 * np.pi * f * tt) * env(tt, 0.01, 0.2) * (t >= 0.11 * k) * 0.5
        out += np.sin(2 * np.pi * f * 2 * tt) * env(tt, 0.01, 0.1) * (t >= 0.11 * k) * 0.15
    return fade_out(normalize(out, 0.7), 0.12)


def feedback_denied(rng):
    """Recurso insuficiente: dos golpecitos sordos y discretos."""
    t = t_axis(0.25)
    out = np.zeros(len(t))
    for k in range(2):
        tt = np.maximum(t - 0.08 * k, 0)
        out += np.sin(2 * np.pi * (170 * np.exp(-tt * 40) + 110) * tt) * env(tt, 0.001, 0.03) * (t >= 0.08 * k) * 0.8
    return fade_out(normalize(out, 0.55), 0.04)


# ---------------------------------------------------------------- Firmas y definitivas de los roles piloto (V96)

def grand_berserker(rng):
    """Bersérker: rugido grave que se rompe, con golpe de tambor."""
    t = t_axis(1.3)
    f = 120 + 50 * np.sin(np.pi * np.clip(t / 1.0, 0, 1)) + 7 * np.sin(2 * np.pi * 9 * t)
    ph = 2 * np.pi * np.cumsum(f) / RATE
    roar = (np.sin(ph) + 0.6 * np.sin(2 * ph) + 0.4 * np.sin(3 * ph)) * (0.55 + 0.45 * np.sin(2 * np.pi * 28 * t)) * env(t, 0.05, 0.5)
    drum = np.sin(2 * np.pi * (85 * np.exp(-t * 9) + 38) * t) * env(t, 0.002, 0.3) * 1.1
    grit = _soft_air(rng, len(t), 300, 3500) * env(t, 0.02, 0.25) * 0.4
    return fade_out(normalize(roar * 0.6 + drum + grit, 0.86), 0.2)


def grand_elementalist(rng):
    """Elementalista: acorde brillante de campanas con barrido de elementos."""
    t = t_axis(1.4)
    out = np.zeros(len(t))
    for k, f in enumerate((523.3, 659.3, 784.0, 1046.5)):
        tt = np.maximum(t - 0.06 * k, 0)
        out += _bell(tt, f, [(1.0, 0.5), (2.0, 0.25), (3.0, 0.12)], 0.8) * (t >= 0.06 * k)
    sweep = np.sin(2 * np.pi * np.cumsum(300 + 1800 * np.clip(t / 0.9, 0, 1) ** 2) / RATE) * env(t, 0.1, 0.4) * 0.25
    air = _soft_air(rng, len(t), 1500, 7000) * env(t, 0.15, 0.4) * 0.15
    return fade_out(normalize(out + sweep + air, 0.8), 0.25)


def grand_shapeshifter(rng):
    """Cambiaformas: transformación orgánica, burbujeo húmedo y gruñido que cae."""
    t = t_axis(1.3)
    wet = _soft_air(rng, len(t), 150, 1500) * (0.5 + 0.5 * np.sin(2 * np.pi * 11 * t)) * env(t, 0.05, 0.5) * 0.7
    growl = np.sin(2 * np.pi * np.cumsum(260 - 150 * np.clip(t / 1.0, 0, 1)) / RATE) * (np.sin(2 * np.pi * 34 * t) > -0.2) * env(t, 0.1, 0.5) * 0.5
    crack = _soft_air(rng, len(t), 800, 5000) * env(t, 0.003, 0.05) * 0.5
    return fade_out(normalize(wet + growl + crack, 0.82), 0.2)


def grand_paladin(rng):
    """Paladín: campana mayor con un coro luminoso."""
    t = t_axis(1.6)
    bell = _bell(t, 392.0, [(1.0, 0.9), (2.0, 0.5), (3.01, 0.35), (4.2, 0.2)], 1.2) * 1.0
    choir = sum(np.sin(2 * np.pi * f * t) * env(t, 0.3, 0.7) for f in (196.0, 246.9, 293.7, 392.0)) * 0.22
    shimmer = _bell(t, 1568.0, [(1.0, 0.4), (1.5, 0.25)], 0.9) * 0.3
    return fade_out(normalize(bell + choir + shimmer, 0.82), 0.3)


def grand_engineer(rng):
    """Ingeniero: servo que sube, golpe de engranaje y descarga eléctrica."""
    t = t_axis(1.2)
    whine = np.sin(2 * np.pi * np.cumsum(220 + 1400 * np.clip(t / 0.55, 0, 1) ** 1.5) / RATE) * env(t, 0.05, 0.3) * 0.35 * (t < 0.7)
    hit_t = np.maximum(t - 0.55, 0)
    clank = _bell(hit_t, 520, [(1.0, 0.7), (2.7, 0.5), (4.5, 0.3)], 0.2) * (t >= 0.55) * 0.7
    boom = np.sin(2 * np.pi * (120 * np.exp(-hit_t * 12) + 45) * hit_t) * env(hit_t, 0.002, 0.2) * 0.9 * (t >= 0.55)
    zap = _soft_air(rng, len(t), 2000, 8000) * env(hit_t, 0.001, 0.05) * 0.5 * (t >= 0.55)
    return fade_out(normalize(whine + clank + boom + zap, 0.84), 0.2)


def grand_rumble(rng):
    """Retumbo común de las definitivas: subgrave que crece y se apaga."""
    t = t_axis(1.8)
    sub = np.sin(2 * np.pi * (55 + 12 * np.sin(np.pi * np.clip(t / 1.6, 0, 1))) * t) * env(t, 0.5, 0.7) * 1.0
    air = _soft_air(rng, len(t), 60, 700) * env(t, 0.4, 0.6) * 0.5
    return fade_out(normalize(sub + air, 0.8), 0.4)


# ---------------------------------------------------------------- Firmas y definitivas de los otros 15 roles (V96, 2.ª parte)

def _grand_voice(kind, f0):
    """Motivo de un rol según su timbre: campana, soplo, golpe, filo, bordón, madera, monedas, gong, disparo, cuernos."""
    def make(rng):
        t = t_axis(1.4)
        n = len(t)
        if kind == 'bell':
            out = sum(_bell(t, f0 * m, [(1.0, 0.5), (2.0, 0.25)], 0.9) * a for m, a in ((1, 1.0), (1.5, 0.6), (2, 0.5)))
            out = out + np.sin(2 * np.pi * np.cumsum(200 + 1500 * np.clip(t / 0.8, 0, 1) ** 2) / RATE) * env(t, 0.1, 0.4) * 0.15
        elif kind == 'whoosh':
            out = _soft_air(rng, n, 300, 4500) * (1 - np.clip(t / 0.9, 0, 1)) * env(t, 0.05, 0.5) * 0.9 + _bell(t, 70, [(1.0, 0.9)], 0.4) * 0.5
        elif kind == 'chime':
            out = sum(_bell(np.maximum(t - 0.07 * k, 0), f0 * m, [(1.0, 0.5), (2.0, 0.2)], 0.9) * (t >= 0.07 * k) for k, m in enumerate((1, 1.25, 1.5, 2)))
            out = out + _soft_air(rng, n, 1500, 6000) * env(t, 0.2, 0.5) * 0.12
        elif kind == 'thud':
            out = np.sin(2 * np.pi * (f0 * 1.6 * np.exp(-t * 8) + f0 * 0.5) * t) * env(t, 0.002, 0.3) * 1.2 + _soft_air(rng, n, 200, 2500) * env(t, 0.02, 0.2) * 0.5
        elif kind == 'shing':
            out = _bell(t, f0, [(1.0, 0.6), (1.7, 0.4), (2.9, 0.3)], 0.5) * 0.6 + _highpass(_soft_air(rng, n, 100, 9000), 3000) * env(t, 0.003, 0.18) * 1.0
        elif kind == 'drone':
            out = (np.sin(2 * np.pi * f0 * t) + 0.6 * np.sin(2 * np.pi * f0 * 1.51 * t) + 0.4 * np.sin(2 * np.pi * f0 * 2.02 * t)) * env(t, 0.3, 0.8) * 0.7 + _soft_air(rng, n, 80, 900) * env(t, 0.3, 0.6) * 0.4
        elif kind == 'wood':
            out = _bell(t, f0, [(1.0, 0.8), (2.5, 0.5), (4.1, 0.3)], 0.35) * 0.9 + _soft_air(rng, n, 1500, 6000) * (0.5 + 0.5 * np.sin(2 * np.pi * 7 * t)) * env(t, 0.1, 0.6) * 0.25
        elif kind == 'coins':
            out = sum(_bell(np.maximum(t - 0.05 * k, 0), f0 * (1 + 0.13 * (k % 3)), [(1.0, 0.7), (2.4, 0.4)], 0.18) * (t >= 0.05 * k) for k in range(8)) * 0.4
            out = out + _bell(t, 110, [(1.0, 0.9)], 0.5) * 0.4
        elif kind == 'gong':
            out = _bell(t, f0, [(1.0, 0.9), (1.6, 0.6), (2.3, 0.5), (3.7, 0.3)], 1.3)
        elif kind == 'crack':
            out = _soft_air(rng, n, 500, 9000) * env(t, 0.001, 0.06) * 1.2 + _bell(t, f0, [(1.0, 0.7), (2.0, 0.3)], 0.7) * 0.5
        elif kind == 'blades':
            out = sum(_bell(np.maximum(t - 0.06 * k, 0), f0 * (1 + 0.21 * k), [(1.0, 0.6), (2.76, 0.4)], 0.35) * (t >= 0.06 * k) for k in range(5)) * 0.5
        elif kind == 'hawk':
            out = np.sin(2 * np.pi * np.cumsum(f0 + 1400 * np.clip(t / 0.5, 0, 1) - 900 * np.clip((t - 0.5) / 0.6, 0, 1)) / RATE) * env(t, 0.05, 0.4) * 0.5 + _soft_air(rng, n, 1200, 7000) * env(t, 0.05, 0.4) * 0.25
        elif kind == 'lute':
            out = sum(_bell(np.maximum(t - 0.1 * k, 0), f0 * m, [(1.0, 0.7), (2.0, 0.4), (3.0, 0.2)], 0.5) * (t >= 0.1 * k) for k, m in enumerate((1, 1.25, 1.5, 2)))
        else:  # horn
            out = (np.sin(2 * np.pi * f0 * t) + 0.5 * np.sin(2 * np.pi * f0 * 2 * t) + 0.3 * np.sin(2 * np.pi * f0 * 3 * t)) * env(t, 0.15, 0.7) * 0.8 + _soft_air(rng, n, 100, 1200) * env(t, 0.1, 0.5) * 0.25
        return fade_out(normalize(out, 0.82), 0.25)
    return make


_GRAND_VOICES = {'mage': ('bell', 659.3), 'assassin': ('whoosh', 900), 'herbalist': ('chime', 523.3), 'lancer': ('thud', 95), 'swordsman': ('shing', 2400), 'necromancer': ('drone', 62), 'druid': ('wood', 146.8), 'merchant': ('coins', 1760), 'monk': ('gong', 196), 'gunslinger': ('crack', 420), 'weapon_summoner': ('blades', 880), 'hunter': ('hawk', 1400), 'bard': ('lute', 392), 'warlock': ('drone', 83), 'tamer': ('horn', 155.6)}
for _name, (_kind, _f0) in _GRAND_VOICES.items():
    globals()['grand_' + _name] = _grand_voice(_kind, _f0)


SOUNDS = {
    'eclipse_announce': eclipse_announce,
    'eclipse_start': eclipse_start,
    'eclipse_totality': eclipse_totality,
    'herald_spawn': herald_spawn,
    'combo_finisher': combo_finisher,
    'rune_socket': rune_socket,
    'rune_remove': rune_remove,
    'keystone_carnage': keystone_carnage,
    'keystone_bulwark': keystone_bulwark,
    'keystone_overload': keystone_overload,
    'ui_metal_select': ui_metal_select,
    'ui_metal_unlock': ui_metal_unlock,
    'ui_metal_denied': ui_metal_denied,
    'keystone_detonation': keystone_detonation,
    'keystone_contagion': keystone_contagion,
    'keystone_legion': keystone_legion,
    'keystone_specter': keystone_specter,
    'keystone_fortune': keystone_fortune,
    'keystone_tempest': keystone_tempest,
    'keystone_sanctuary': keystone_sanctuary,
    'arena_theme': arena_theme,
    'errante_ambient': errante_ambient,
    'errante_alert': errante_alert,
    'errante_prepare': errante_prepare,
    'errante_rend': errante_rend,
    'errante_hurt': errante_hurt,
    'errante_death': errante_death,
    'errante_despawn': errante_despawn,
    'osario_ambient': osario_ambient,
    'osario_alert': osario_alert,
    'osario_hurt': osario_hurt,
    'osario_death': osario_death,
    'osario_claw': osario_claw,
    'osario_heavy': osario_heavy,
    'osario_slam_charge': osario_slam_charge,
    'osario_slam_hit': osario_slam_hit,
    'osario_lunge': osario_lunge,
    'osario_step': osario_step,
    'berserker_roar': berserker_roar,
    'berserker_war_cry': berserker_war_cry,
    'berserker_slam': berserker_slam,
    'berserker_whoosh': berserker_whoosh,
    'berserker_heartbeat': berserker_heartbeat,
    'berserker_leap': berserker_leap,
    'mage_charge': mage_charge,
    'mage_bolt': mage_bolt,
    'mage_blink': mage_blink,
    'mage_time': mage_time,
    'mage_missile': mage_missile,
    'mage_impact': mage_impact,
    'assassin_vanish': assassin_vanish,
    'assassin_slash': assassin_slash,
    'assassin_step': assassin_step,
    'assassin_throw': assassin_throw,
    'assassin_impact': assassin_impact,
    'herbalist_balm': herbalist_balm,
    'herbalist_rain': herbalist_rain,
    'herbalist_spore': herbalist_spore,
    'engineer_grapple': engineer_grapple,
    'engineer_throw': engineer_throw,
    'engineer_blast': engineer_blast,
    'lancer_leap': lancer_leap,
    'lancer_thrust': lancer_thrust,
    'lancer_slam': lancer_slam,
    'swordsman_draw': swordsman_draw,
    'swordsman_cut': swordsman_cut,
    'paladin_consecrate': paladin_consecrate,
    'paladin_hammer': paladin_hammer,
    'necromancer_spear': necromancer_spear,
    'necromancer_siphon': necromancer_siphon,
    'druid_howl': druid_howl,
    'druid_thorns': druid_thorns,
    'merchant_coin': merchant_coin,
    'merchant_guard': merchant_guard,
    'monk_kick': monk_kick,
    'monk_fist': monk_fist,
    'gunslinger_shot': gunslinger_shot,
    'gunslinger_mark': gunslinger_mark,
    'weapon_greatsword': weapon_greatsword,
    'weapon_prison': weapon_prison,
    'shapeshifter_pounce': shapeshifter_pounce,
    'shapeshifter_howl': shapeshifter_howl,
    'hunter_shot': hunter_shot,
    'hunter_trap': hunter_trap,
    'hunter_falcon': hunter_falcon,
    'bard_song': bard_song,
    'bard_dissonance': bard_dissonance,
    'warlock_curse': warlock_curse,
    'warlock_bolt': warlock_bolt,
    'warlock_imps': warlock_imps,
    'warlock_doom': warlock_doom,
    'tamer_command': tamer_command,
    'tamer_stampede': tamer_stampede,
    'element_fire': element_fire,
    'element_ice': element_ice,
    'element_lightning': element_lightning,
    'element_earth': element_earth,
    'element_wind': element_wind,
    'element_water': element_water,
    'element_magic': element_magic,
    'element_dark': element_dark,
    'feedback_heavy': feedback_heavy,
    'feedback_crit': feedback_crit,
    'feedback_block': feedback_block,
    'feedback_heal': feedback_heal,
    'feedback_denied': feedback_denied,
    'grand_berserker': grand_berserker,
    'grand_elementalist': grand_elementalist,
    'grand_shapeshifter': grand_shapeshifter,
    'grand_paladin': grand_paladin,
    'grand_engineer': grand_engineer,
    'grand_rumble': grand_rumble,
    'grand_mage': grand_mage,
    'grand_assassin': grand_assassin,
    'grand_herbalist': grand_herbalist,
    'grand_lancer': grand_lancer,
    'grand_swordsman': grand_swordsman,
    'grand_necromancer': grand_necromancer,
    'grand_druid': grand_druid,
    'grand_merchant': grand_merchant,
    'grand_monk': grand_monk,
    'grand_gunslinger': grand_gunslinger,
    'grand_weapon_summoner': grand_weapon_summoner,
    'grand_hunter': grand_hunter,
    'grand_bard': grand_bard,
    'grand_warlock': grand_warlock,
    'grand_tamer': grand_tamer,
}


def write_ogg(samples, path):
    pcm = (np.clip(samples, -1, 1) * 32767).astype('<i2')
    with tempfile.TemporaryDirectory() as tmp:
        wav = os.path.join(tmp, 'in.wav')
        with wave.open(wav, 'wb') as w:
            w.setnchannels(1)
            w.setsampwidth(2)
            w.setframerate(RATE)
            w.writeframes(pcm.tobytes())
        subprocess.run(['ffmpeg', '-v', 'error', '-y', '-i', wav, '-c:a', 'libvorbis', '-q:a', '4',
                        '-map_metadata', '-1', '-fflags', '+bitexact', path], check=True)


def main():
    if len(sys.argv) < 2:
        print(__doc__)
        sys.exit(1)
    out = os.path.join(sys.argv[1], 'sounds')
    os.makedirs(out, exist_ok=True)
    only = set(sys.argv[2:])
    unknown = only - set(SOUNDS)
    if unknown:
        sys.exit('Sonidos desconocidos: ' + ', '.join(sorted(unknown)))
    for name, fn in SOUNDS.items():
        if only and name not in only:
            continue
        rng = np.random.default_rng(SEED + sum(map(ord, name)))
        write_ogg(fn(rng), os.path.join(out, name + '.ogg'))
        print(name + '.ogg')


if __name__ == '__main__':
    main()
