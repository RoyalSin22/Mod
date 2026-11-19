// Fase 1: construcción del bloqueo mediante la API de Blockbench MCP.
// Se ejecuta únicamente en un proyecto GeckoLib nuevo y vacío. No es un generador de recursos del juego.
if (Format.id !== 'geckolib_model' || Group.all.length || Cube.all.length) {
  throw new Error('El bloqueo requiere un proyecto GeckoLib nuevo y vacío.');
}

const DEG = Math.PI / 180;
const bones = new Map();
const authored = [];
const V = a => new THREE.Vector3(...a);
const Q = a => new THREE.Quaternion().setFromEuler(new THREE.Euler(...a.map(v => v * DEG), 'ZYX'));
const angles = q => new THREE.Euler().setFromQuaternion(q, 'ZYX').toArray().slice(0, 3).map(v => v / DEG);
const along = (from, to) => new THREE.Quaternion().setFromUnitVectors(V([0, -1, 0]), V(to).sub(V(from)).normalize());

// Los puntos se deciden en la pose visible. El cambio de base evita que las rotaciones de los padres
// levanten los pies o acumulen desplazamientos que no pertenecen a la articulación.
function bone(name, parent, point, rotation = [0, 0, 0]) {
  const p = parent ? bones.get(parent) : null;
  const world = V(point);
  const worldQ = rotation instanceof THREE.Quaternion ? rotation.clone() : Q(rotation);
  const bind = p ? world.clone().sub(p.world).applyQuaternion(p.worldQ.clone().invert()).add(p.bind) : world.clone();
  const localQ = p ? p.worldQ.clone().invert().multiply(worldQ) : worldQ.clone();
  const group = new Group({name, origin:bind.toArray(), rotation:angles(localQ)});
  if (p) group.addTo(p.group);
  group.init();
  const data = {name, parent, world, worldQ, bind, group};
  bones.set(name, data);
  return data;
}

// Solo colores de marcador del editor: no se crea ninguna textura ni paleta final.
const BONE = 9, EARTH = 2, CORE = 6, ROOT = 3;
function cube(name, parent, center, size, color = BONE, rotation = [0, 0, 0], purpose = '') {
  const b = bones.get(parent);
  const pivot = b.bind.clone().add(V(center));
  const low = pivot.clone().sub(V(size).multiplyScalar(.5));
  const high = pivot.clone().add(V(size).multiplyScalar(.5));
  const element = new Cube({name, from:low.toArray(), to:high.toArray(), origin:pivot.toArray(),
    rotation, color, box_uv:true}).addTo(b.group).init();
  authored.push({name, parent, size, center, purpose, uuid:element.uuid});
  return element;
}

bone('root', null, [0, 0, 0]);
bone('pelvis', 'root', [0, 12, .75]);
bone('body', 'pelvis', [0, 13.3, .65], [-12, 0, 0]);
bone('spine_upper', 'body', [0, 15.6, .2], [-27, 0, 2]);
bone('chest', 'spine_upper', [0, 16.2, -.15], [-27, 0, 2]);
const chest = bones.get('chest');
const chestPoint = offset => V(offset).applyQuaternion(chest.worldQ).add(chest.world).toArray();
bone('ribs_left', 'chest', chestPoint([2.5, 4, -.5]), chest.worldQ);
bone('ribs_right', 'chest', chestPoint([-2.5, 4, -.5]), chest.worldQ);
bone('chest_core', 'chest', chestPoint([0, 4.1, -1.9]), chest.worldQ);
bone('neck', 'chest', [0, 21.9, -2.6], [-15, 0, 0]);
bone('head', 'neck', [0, 23.2, -4.25], [-4, 0, 12]);
bone('jaw', 'head', [0, 22.05, -2.9], [-10, 0, 12]);

const shoulder = chestPoint([-6, 7, 0]);
const elbow = [-6.95, 16.1, -3.85];
const wrist = [-7.2, 10.15, -4.35];
bone('bone_shoulder', 'chest', shoulder, chest.worldQ);
bone('bone_upper_arm', 'bone_shoulder', shoulder, along(shoulder, elbow));
bone('bone_forearm', 'bone_upper_arm', elbow, along(elbow, wrist));
bone('bone_hand', 'bone_forearm', [-7.2, 10, -4.35], [8, 0, -2]);
const hand = bones.get('bone_hand');
const handPoint = offset => V(offset).applyQuaternion(hand.worldQ).add(hand.world).toArray();
bone('bone_fingers', 'bone_hand', handPoint([0, -1.5, -.1]), hand.worldQ);
// Un pulgar independiente evita cerrar los cuatro dedos como una placa rígida (30 huesos en total).
bone('bone_thumb', 'bone_hand', handPoint([1.25, -1.25, -.1]), hand.worldQ.clone().multiply(Q([5, 0, 45])));

bone('mass_shoulder', 'chest', chestPoint([6, 7, 0]), chest.worldQ);
const upper = [6.15, 22.6, -2.6], middle = [7.3, 14.65, .4];
const lower = [8.25, 7.3, 3.8], end = [8.75, 1.75, 6.7];
bone('dragging_upper', 'mass_shoulder', upper, along(upper, middle));
bone('dragging_middle', 'dragging_upper', middle, along(middle, lower));
bone('dragging_lower', 'dragging_middle', lower, along(lower, end));
bone('dragging_end', 'dragging_lower', end);
bone('debris_roots', 'dragging_middle', [9.1, 11.5, 3.5], [-18, 0, 12]);
bone('debris_chips', 'dragging_lower', [10.5, 4.25, 6], [0, 0, -12]);

const hipL = [2.6, 11.25, .75], kneeL = [2.6, 6.25, -1.55], ankleL = [2.6, 1.25, 1.75];
const hipR = [-2.6, 11.75, .75], kneeR = [-2.8, 6.45, -2.05], ankleR = [-2.8, 1.25, .94];
bone('thigh_left', 'pelvis', hipL, along(hipL, kneeL));
bone('shin_left', 'thigh_left', kneeL, along(kneeL, ankleL));
bone('foot_left', 'shin_left', ankleL);
bone('thigh_right', 'pelvis', hipR, along(hipR, kneeR));
bone('shin_right', 'thigh_right', kneeR, along(kneeR, ankleR));
bone('foot_right', 'shin_right', ankleR);

cube('F1_ilium_left', 'pelvis', [2.4, .4, 0], [3, 2, 3.5], BONE, [0, 0, -5], 'Ala ilíaca del lado corto');
cube('F1_ilium_right', 'pelvis', [-2.4, .9, 0], [3, 2, 3.5], BONE, [0, 0, 4], 'Ala ilíaca del lado largo');
cube('F1_sacrum', 'pelvis', [0, .2, .8], [2, 2.5, 2.5]);
cube('F1_pubis', 'pelvis', [0, -.4, -1], [4, 1, 2]);
cube('F1_lumbar_volume', 'body', [0, 1.1, 0], [2, 3, 2]);
cube('F1_thoracic_spine', 'spine_upper', [0, 2.5, 1.4], [2, 5, 2]);
cube('F1_sternum', 'chest', [0, 4.4, -2.5], [1, 5, 1]);

// Seis pares de volúmenes provisionales; los arcos de dos tramos se elaboran en la Fase 2.
const ribs = [[3.5, 2.5], [3.5, 1.4], [3.5, .3], [3.5, -.8], [3, -1.9], [2.5, -3]];
for (let i = 0; i < ribs.length; i++) {
  const [width, y] = ribs[i];
  cube(`F1_rib_left_${i+1}`, 'ribs_left', [.5, y, -.1], [width, .5, 3.5], BONE, [0, 0, i===1 ? -4 : 0]);
  cube(`F1_rib_right_${i+1}`, 'ribs_right', [-.5, y, -.1], [width, .5, 3.5], BONE, [0, 0, i===4 ? 4 : 0]);
}
cube('F1_core_vertical', 'chest_core', [0, 0, 0], [1.5, 3, 1.5], CORE);
cube('F1_core_horizontal', 'chest_core', [0, 0, 0], [3, 1.5, 1.5], CORE);
cube('F1_cervical_volume', 'neck', [0, 1, 0], [1.5, 2.5, 1.5]);

cube('F1_cranium_bulk', 'head', [0, 2, .5], [7, 7, 6]);
cube('F1_brow', 'head', [0, 4.25, -3], [7, 2.5, 1]);
cube('F1_orbit_outer_left', 'head', [2.75, 2, -3], [1.5, 2, 1]);
cube('F1_orbit_outer_right', 'head', [-2.75, 2, -3], [1.5, 2, 1]);
cube('F1_nasal_bridge', 'head', [0, 2, -3], [1, 2, 1]);
cube('F1_lower_face', 'head', [0, -.25, -3], [7, 2.5, 1]);
cube('F1_jaw_body', 'jaw', [0, -1.25, -1.4], [5, 1, 3.5]);
cube('F1_jaw_ramus_left', 'jaw', [2.5, -.25, 0], [1, 2, 1]);
cube('F1_jaw_ramus_right', 'jaw', [-2.5, -.25, 0], [1, 2, 1]);

cube('F1_bone_clavicle', 'bone_shoulder', [1.1, 0, -.1], [3, 1, 1]);
cube('F1_bone_scapula', 'bone_shoulder', [.7, -.5, 1], [2, 2, 1]);
cube('F1_humerus', 'bone_upper_arm', [0, -2.75, 0], [1.5, 5.5, 1.5]);
cube('F1_radius', 'bone_forearm', [-.7, -2.75, 0], [1, 5.5, 1]);
cube('F1_ulna', 'bone_forearm', [.7, -2.75, 0], [1, 5.5, 1]);
cube('F1_palm', 'bone_hand', [0, -.7, 0], [3, 1.5, 1]);
cube('F1_finger_index', 'bone_fingers', [1.05, -1.25, 0], [1, 2.5, 1], BONE, [0, 0, 5]);
cube('F1_finger_middle', 'bone_fingers', [0, -1.5, 0], [1, 3, 1]);
cube('F1_finger_ring', 'bone_fingers', [-1.05, -1.25, 0], [1, 2.5, 1], BONE, [0, 0, -5]);
cube('F1_thumb', 'bone_thumb', [0, -.9, 0], [.5, 2, 1]);

cube('F1_shoulder_earth', 'mass_shoulder', [0, .5, 1], [6, 4, 6], EARTH);
cube('F1_mass_upper', 'dragging_upper', [0, -3.75, 0], [5, 7.5, 6], EARTH);
cube('F1_mass_middle', 'dragging_middle', [0, -3.75, 0], [5.5, 7.5, 6.5], EARTH);
cube('F1_mass_lower', 'dragging_lower', [0, -2.4, 0], [7, 6, 7], EARTH);
cube('F1_mass_ground_base', 'dragging_end', [0, -.75, 0], [8, 2, 8], EARTH);
cube('F1_upper_silhouette_lump', 'dragging_upper', [1.75, -2, 1], [3, 3, 3], EARTH, [0, 0, 15]);
cube('F1_embedded_femur', 'dragging_upper', [.75, -3.25, -3.1], [1, 6, 1]);
cube('F1_mid_bone_fragment', 'dragging_middle', [2, -3.4, -3.25], [2, 3, 2], BONE, [0, 12, -12]);
cube('F1_lower_structural_root', 'dragging_lower', [-2.5, -2.25, -3.5], [1, 5, 1], ROOT);
cube('F1_hanging_root_volume', 'debris_roots', [0, -1.5, 0], [1, 4, 1], ROOT);
cube('F1_chip_volume', 'debris_chips', [0, 0, 0], [1.5, 1.5, 1.5]);
cube('F1_ground_root_outer', 'dragging_end', [3.1, -1.25, 3.5], [1, 1, 3], ROOT, [0, -12, 0]);
cube('F1_ground_root_inner', 'dragging_end', [-2.5, -1.25, 3.5], [1, 1, 3], ROOT, [0, 16, 0]);

cube('F1_femur_left_short', 'thigh_left', [0, -2.75, 0], [2, 5.5, 2]);
cube('F1_femur_right', 'thigh_right', [0, -3, 0], [2, 6, 2]);
for (const side of ['left', 'right']) {
  cube(`F1_tibia_${side}`, `shin_${side}`, [0, -3, 0], [2, 6, 2]);
  cube(`F1_fibula_${side}`, `shin_${side}`, [side==='left' ? 1.2 : -1.2, -3, .2], [1, 5, 1]);
  cube(`F1_knee_${side}`, `shin_${side}`, [0, 0, -.6], [2.5, 1.5, 2]);
  cube(`F1_heel_${side}`, `foot_${side}`, [0, -.5, .75], [2.5, 1.5, 3]);
  for (let i = 0; i < 3; i++) cube(`F1_toe_${side}_${i+1}`, `foot_${side}`, [i-1, -.75, -1.75], [1, 1, 2]);
}

// UV de trabajo: una isla por cubo. Las fracciones se convierten a caras explícitas para que
// GeckoLib no aplique floor al tamaño. El atlas y su pintura definitivos pertenecen a la Fase 3.
let rowX = .5, rowY = .5, rowH = 0;
for (const a of authored) {
  const c = Cube.all.find(c => c.uuid === a.uuid);
  const [x,y,z] = a.size;
  const w = 2 * (x+z), h = y+z;
  if (rowX+w+.5>128) { rowX=.5;rowY+=rowH+.5;rowH=0; }
  if (rowY+h>.5+127) throw new Error('La UV provisional excede el atlas.');
  c.uv_offset=[rowX,rowY];
  if (a.size.some(v => v%1!==0)) {
    c.box_uv=false;
    const rects={east:[rowX,rowY+z,rowX+z,rowY+z+y],
      north:[rowX+z,rowY+z,rowX+z+x,rowY+z+y],
      west:[rowX+z+x,rowY+z,rowX+2*z+x,rowY+z+y],
      south:[rowX+2*z+x,rowY+z,rowX+w,rowY+z+y],
      up:[rowX+z,rowY,rowX+z+x,rowY+z],
      down:[rowX+z+x,rowY,rowX+z+2*x,rowY+z]};
    for (const [face,uv] of Object.entries(rects)) c.faces[face].uv=uv;
  }
  rowX+=w+.5;rowH=Math.max(rowH,h);
}
Canvas.updateAll();
Project.model_3d.updateMatrixWorld(true);
globalThis.osarioF1={bones,authored};
return {phase:1,bones:Group.all.length,cubes:Cube.all.length,texture_count:Texture.all.length,
  animation_count:Animation.all.length,uv_rows:rowY+rowH,
  fractional_cubes:authored.filter(a=>a.size.some(v=>v%1!==0)).length,
  joints:[...bones.values()].map(b=>({name:b.name,parent:b.parent,desired_world:b.world.toArray(),
    bind_origin:b.bind.toArray(),rotation:b.group.rotation,
    measured_world:b.group.mesh.getWorldPosition(new THREE.Vector3()).toArray()}))};
