// Revisión por cajas orientadas: cerrar uniones reales, sin alterar los 30 pivotes aprobados.
const {put,parts,refresh}=osarioF2;
put('cervicothoracic_arch','chest',[0,5.15,1],[2,2,2.5],[0,0,0],'bone',
  'Arco de la unión cervicotorácica: sostiene cuello y costillas superiores desde la dorsal');
put('clavicle_medial_fragment','bone_shoulder',[3.4,-.3,-1.8],[4,1,1],[0,28,-8],'bone',
  'Tramo medial de clavícula entre el hombro y el manubrio');
for(const name of ['rib_left_3_front','rib_left_4_front','rib_right_3_front','rib_right_4_front']){
  const a=parts.find(a=>a.name===name),sign=name.includes('_left_')?1:-1;
  a.size[0]+=1;a.center[0]-=sign*.5;a.center[2]-=.5;
  a.purpose+='; extremo medial unido al esternón';
}
for(const a of parts.filter(a=>a.parent==='chest_core'))a.center[2]+=.45;
parts.find(a=>a.name==='rib_right_2_front').center[2]-=.25;
for(const a of parts.filter(a=>a.parent==='bone_thumb'))a.center[0]+=.5;
refresh();
return {stage:'uniones_anatomicas',cubes:Cube.all.length,bones:Group.all.length};
