// Refinamiento de la Fase 1 tras medir colisiones y revisar frente y ambos perfiles.
// Se ejecuta después de build_blockout.js en el mismo proyecto, sin crear clips ni texturas.
const {bones, authored} = globalThis.osarioF1;
const V = a => new THREE.Vector3(...a);
const angles = q => new THREE.Euler().setFromQuaternion(q, 'ZYX').toArray().slice(0,3).map(v=>v*180/Math.PI);
bones.get('head').world.z -= 1;
bones.get('jaw').world.z -= 2;
bones.get('mass_shoulder').world.z += 1.5;
// Conserva las articulaciones ya medidas al recalcular el sistema de coordenadas de cada rama.
for (const b of bones.values()) {
  const p = bones.get(b.parent);
  b.bind = p ? b.world.clone().sub(p.world).applyQuaternion(p.worldQ.clone().invert()).add(p.bind) : b.world.clone();
  const localQ = p ? p.worldQ.clone().invert().multiply(b.worldQ) : b.worldQ.clone();
  b.group.origin = b.bind.toArray();
  b.group.rotation = angles(localQ);
}
for (const a of authored) {
  if (a.name === 'F1_mass_upper') { a.size[1] = 8.5; a.center[1] = -4.25; }
  if (a.name.startsWith('F1_rib_left_')) a.center[0] = .75;
  if (a.name.startsWith('F1_rib_right_')) a.center[0] = -.75;
  if (a.name === 'F1_sternum') a.center[2] = -3;
  if (a.name === 'F1_jaw_body') a.size = [5,1.5,4];
  if (a.name === 'F1_core_vertical' || a.name === 'F1_core_horizontal') a.center[2] = .4;
  const c = Cube.all.find(c=>c.uuid===a.uuid);
  const pivot = bones.get(a.parent).bind.clone().add(V(a.center));
  c.origin = pivot.toArray();
  c.from = pivot.clone().sub(V(a.size).multiplyScalar(.5)).toArray();
  c.to = pivot.clone().add(V(a.size).multiplyScalar(.5)).toArray();
}
function volume(name,parent,center,size,purpose) {
  const pivot=bones.get(parent).bind.clone().add(V(center));
  const c=new Cube({name,from:pivot.clone().sub(V(size).multiplyScalar(.5)).toArray(),
    to:pivot.clone().add(V(size).multiplyScalar(.5)).toArray(),origin:pivot.toArray(),color:9,box_uv:true})
    .addTo(bones.get(parent).group).init();
  authored.push({name,parent,center,size,purpose,uuid:c.uuid});
}
volume('F1_elbow_joint','bone_forearm',[0,.25,0],[2,1.5,2],'Cierra el apoyo humerocubital');
volume('F1_carpal_joint','bone_hand',[0,.2,0],[2,1,1.5],'Une el antebrazo con la palma');
volume('F1_costal_back_bridge','chest',[0,5.8,1],[8,.5,1],'Puente posterior provisional de la caja torácica');
volume('F1_costal_spine_contact','chest',[0,4.3,1],[2,2,2.5],'Apoyo del puente posterior en la columna');
// Reempaquetado provisional; ninguna pintura depende todavía de estas islas.
let rowX=.5,rowY=.5,rowH=0;
for(const a of authored) {
  const c=Cube.all.find(c=>c.uuid===a.uuid),[x,y,z]=a.size,w=2*(x+z),h=y+z;
  if(rowX+w+.5>128){rowX=.5;rowY+=rowH+.5;rowH=0;}
  if(rowY+h>127.5)throw Error('UV provisional fuera de límites');
  c.uv_offset=[rowX,rowY]; c.box_uv=!a.size.some(v=>v%1!==0);
  if(!c.box_uv){
    const rects={east:[rowX,rowY+z,rowX+z,rowY+z+y],north:[rowX+z,rowY+z,rowX+z+x,rowY+z+y],
      west:[rowX+z+x,rowY+z,rowX+2*z+x,rowY+z+y],south:[rowX+2*z+x,rowY+z,rowX+w,rowY+z+y],
      up:[rowX+z,rowY,rowX+z+x,rowY+z],down:[rowX+z+x,rowY,rowX+z+2*x,rowY+z]};
    for(const [face,uv]of Object.entries(rects))c.faces[face].uv=uv;
  }
  rowX+=w+.5;rowH=Math.max(rowH,h);
}
Canvas.updateAll();Project.model_3d.updateMatrixWorld(true);
return {phase:1,revision:'contactos',bones:Group.all.length,cubes:Cube.all.length,uv_rows:rowY+rowH};
