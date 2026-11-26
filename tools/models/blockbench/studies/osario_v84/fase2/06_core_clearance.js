// Reserva geométrica para el pulso máximo S=1,35, sin crear animaciones.
// Se centra la cruz sobre su pivote de escala y se libera la cavidad interior del tórax.
const {parts,refresh}=osarioF2;
for(const a of parts.filter(a=>a.parent==='chest_core'))a.center[2]-=.85;
parts.find(a=>a.name==='ecto_horizontal').size[2]=.5;
for(const name of ['manubrium','sternum','xiphoid'])parts.find(a=>a.name===name).center[2]-=.5;
parts.find(a=>a.name==='manubrium').center[1]-=.25;
parts.find(a=>a.name==='ecto_vertical').center[1]-=.2;
parts.find(a=>a.name==='ecto_shard_high').center[1]-=.35;
for(const side of ['left','right'])for(const index of [3,4]){
  const front=parts.find(a=>a.name===`rib_${side}_${index}_front`);front.center[2]-=1.1;
  const arc=parts.find(a=>a.name===`rib_${side}_${index}_arc`);arc.size[2]+=1;arc.center[2]-=.25;
}
parts.find(a=>a.name==='rib_right_2_front').center[2]-=.75;
const thirdRight=parts.find(a=>a.name==='rib_right_3_front');
thirdRight.size[0]-=.5;thirdRight.center[0]-=.25;
refresh();
for(const c of Cube.all)c.mesh.geometry.computeBoundingBox();
return {stage:'holgura_nucleo',cubes:Cube.all.length};
