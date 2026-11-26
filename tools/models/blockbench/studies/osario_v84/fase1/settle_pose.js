// Ajuste final de apoyos y separación entre cráneo, mandíbula y caja torácica.
const {bones,authored}=globalThis.osarioF1;
bones.get('head').world.set(0,23.45,-7.5);
bones.get('jaw').world.set(0,22.8,-7.5);
bones.get('neck').world.z=-3.1;
// El pivote permanece en el hombro anatómico: 12 u entre ambos hombros.
// Se retrasa solo la envolvente de tierra para separarla del cráneo.
bones.get('mass_shoulder').world.z-=1.5;
const shoulderOffset=new THREE.Vector3(0,0,3).applyQuaternion(bones.get('mass_shoulder').worldQ.clone().invert());
const shoulderEarth=authored.find(a=>a.name==='F1_shoulder_earth');
shoulderEarth.center=new THREE.Vector3(...shoulderEarth.center).add(shoulderOffset).toArray();
bones.get('dragging_end').world.z=4.2;
authored.find(a=>a.name==='F1_jaw_body').center[1]=-1.75;
for(const name of ['F1_jaw_ramus_left','F1_jaw_ramus_right']){
  const a=authored.find(a=>a.name===name);a.center[1]=-.5;a.size[1]=2.5;
}
const upperRib=authored.find(a=>a.name==='F1_rib_right_1');
upperRib.size[2]=3;upperRib.center[2]=.3;upperRib.center[1]=1.9;
function refreshRig(){
  const V=a=>new THREE.Vector3(...a);
  for(const b of bones.values()){
    const p=bones.get(b.parent);
    b.bind=p?b.world.clone().sub(p.world).applyQuaternion(p.worldQ.clone().invert()).add(p.bind):b.world.clone();
    const q=p?p.worldQ.clone().invert().multiply(b.worldQ):b.worldQ.clone();
    b.group.origin=b.bind.toArray();
    b.group.rotation=new THREE.Euler().setFromQuaternion(q,'ZYX').toArray().slice(0,3).map(v=>v*180/Math.PI);
  }
  for(const a of authored){
    const c=Cube.all.find(c=>c.uuid===a.uuid),pivot=bones.get(a.parent).bind.clone().add(V(a.center));
    c.origin=pivot.toArray();c.from=pivot.clone().sub(V(a.size).multiplyScalar(.5)).toArray();
    c.to=pivot.clone().add(V(a.size).multiplyScalar(.5)).toArray();
  }
  Canvas.updateAll();Project.model_3d.updateMatrixWorld(true);
}
globalThis.osarioF1.refreshRig=refreshRig;
refreshRig();
// La altura fraccionaria de las ramas mandibulares requiere también UV por cara.
let rowX=.5,rowY=.5,rowH=0;
for(const a of authored){
  const c=Cube.all.find(c=>c.uuid===a.uuid),[x,y,z]=a.size,w=2*(x+z),h=y+z;
  if(rowX+w+.5>128){rowX=.5;rowY+=rowH+.5;rowH=0;}
  if(rowY+h>127.5)throw Error('UV provisional fuera de límites');
  c.uv_offset=[rowX,rowY];c.box_uv=!a.size.some(v=>v%1!==0);
  if(!c.box_uv){
    const rects={east:[rowX,rowY+z,rowX+z,rowY+z+y],north:[rowX+z,rowY+z,rowX+z+x,rowY+z+y],
      west:[rowX+z+x,rowY+z,rowX+2*z+x,rowY+z+y],south:[rowX+2*z+x,rowY+z,rowX+w,rowY+z+y],
      up:[rowX+z,rowY,rowX+z+x,rowY+z],down:[rowX+z+x,rowY,rowX+z+2*x,rowY+z]};
    for(const[face,uv]of Object.entries(rects))c.faces[face].uv=uv;
  }
  rowX+=w+.5;rowH=Math.max(rowH,h);
}
Canvas.updateAll();
return {bones:Group.all.length,cubes:Cube.all.length};
