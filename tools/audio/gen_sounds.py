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
