// Fase 2, operaciones de autoría en Blockbench MCP sobre el bloqueo aprobado.
// Cada entrada define una pieza anatómica; no se crean texturas, clips ni recursos del juego.
if(Project.model_identifier!=='osario_quebrado' || Group.all.length!==30 || Cube.all.length!==74
  || Cube.all.some(c=>!c.name.startsWith('F1_')))throw Error('Se requiere el bloqueo aprobado de Fase 1.');
const bones=new Map(Group.all.map(g=>[g.name,g]));
const originalRig=Group.all.map(g=>({name:g.name,uuid:g.uuid,origin:g.origin.slice(),rotation:g.rotation.slice()}));
const parts=[];
function put(name,parent,center,size,rotation=[0,0,0],material='bone',purpose=''){
  if(size.some(v=>v<=0||Math.abs(v*2-Math.round(v*2))>1e-6))throw Error('Dimensión inválida: '+name);
  const b=bones.get(parent);if(!b)throw Error('Hueso ausente: '+parent);
  const p=new THREE.Vector3(...b.origin).add(new THREE.Vector3(...center));
  const color={bone:9,earth:2,root:3,moss:7,ecto:6}[material];
  const c=new Cube({name,from:p.clone().sub(new THREE.Vector3(...size).multiplyScalar(.5)).toArray(),
    to:p.clone().add(new THREE.Vector3(...size).multiplyScalar(.5)).toArray(),origin:p.toArray(),rotation,
    color,box_uv:true}).addTo(b).init();
  parts.push({name,parent,center,size,rotation,material,purpose,uuid:c.uuid});return c;
}
// La instantánea aprobada se preservó antes de sustituir estas envolventes.
for(const c of Cube.all.slice())c.remove();
globalThis.osarioF2={bones,parts,put,originalRig};

// Pelvis: alas de distinta inclinación, sacro portante y fracturas laterales.
put('ilium_left_fractured','pelvis',[2.25,.5,.2],[3,2.5,4],[0,-5,-6],'bone','Ala ilíaca corta, fracturada');
put('ilium_right','pelvis',[-2.25,.95,.2],[3,2.5,4],[0,4,3],'bone','Ala ilíaca con mayor apoyo');
put('sacrum','pelvis',[0,.4,1.1],[2,2.5,3],[0,0,0],'bone','Soporte posterior de la pelvis');
put('pubic_arch','pelvis',[0,-.25,-1.1],[4,1,2],[0,3,0],'bone','Arco anterior abierto sobre las caderas');
put('hip_chip_left','pelvis',[3.4,-.3,-1.25],[1,1,2],[8,-14,21],'bone','Borde roto del ala izquierda');
put('hip_chip_right','pelvis',[-3.4,.35,-.7],[1,1.5,1.5],[-8,9,-13],'bone','Esquirla asimétrica del ala derecha');

// Dos lumbares y cuatro torácicas; el disco y el espolón rompen la columna posterior.
put('lumbar_1','body',[0,.45,.25],[2,1.5,2.5],[0,0,-3],'bone','Vértebra lumbar inferior');
put('lumbar_2','body',[.15,1.9,.35],[2,1.5,2.5],[-3,4,4],'bone','Vértebra lumbar superior');
put('intervertebral_disc','body',[0,1.15,.4],[1,1,1.5],[0,0,0],'bone','Separación intervertebral');
put('thoracic_1','spine_upper',[0,.25,1.15],[2,1.5,2.5],[0,3,2],'bone','Articulación dorsolumbar');
put('thoracic_2','spine_upper',[.15,1.65,1.25],[2,1.5,2.5],[0,-4,-2],'bone','Vértebra dorsal baja');
put('thoracic_3','spine_upper',[-.1,3.05,1.2],[2,1.5,2.5],[-2,4,3],'bone','Vértebra dorsal media');
put('thoracic_4','spine_upper',[.1,4.45,1.05],[2,1.5,2.5],[-4,-3,-2],'bone','Vértebra dorsal alta');
put('dorsal_spur','spine_upper',[.2,2.8,2.65],[1,1,1.5],[8,0,11],'bone','Espolón posterior visible');

// Esternón adelantado y estrecho; deja el núcleo a la vista a ambos lados.
put('manubrium','chest',[0,5.8,-3],[2,2,1],[0,0,3],'bone','Unión alta del esternón');
put('sternum','chest',[.1,3.25,-3],[1.5,3.5,1],[0,0,-3],'bone','Pieza central longitudinal');
put('xiphoid','chest',[-.1,1.05,-3],[1,1,.5],[-7,0,8],'bone','Extremo inferior erosionado');

// Doce arcos, dos piezas por costilla. Las tablas describen cada arco por separado:
// [altura relativa, centro X lateral, centro Z lateral, largo lateral, giro Y, ancho frontal, giro Z frontal].
const left=[
 [2.5,2.55,-.9,4.5,-49,3,8], [1.35,2.45,-.8,4,-47,2,12],
 [.2,2.7,-.8,4.5,-51,3,9], [-.95,2.65,-.65,4.5,-49,3,10],
 [-2.1,2.45,-.45,4,-48,2.5,11],[-3.2,2.2,-.15,3.5,-46,2,12]
];
const right=[
 [1.85,-2.45,-.55,4,48,3,-9],[.8,-2.6,-.8,4.5,49,3,-8],
 [-.3,-2.75,-.7,4.5,51,3,-11],[-1.4,-2.55,-.55,4.5,48,2.5,-10],
 [-2.5,-2.3,-.2,3.5,43,1.5,-16],[-3.5,-2.1,.05,3.5,45,2,-12]
];
for(const [side,rows,sign]of[['left',left,1],['right',right,-1]])for(let i=0;i<rows.length;i++){
  const[y,x,z,len,ry,fw,rz]=rows[i],parent='ribs_'+side;
  const localX=x-sign*2.5;
  put(`rib_${side}_${i+1}_arc`,parent,[localX,y,z+.5],[1,i===0?.5:1,len],[0,ry,sign*(i===1?-4:2)],'bone',`Arco costal ${i+1}, ${side}`);
  const frontX=sign*(Math.abs(x)+Math.sin(Math.abs(ry)*Math.PI/180)*len*.5-fw*.5+.15);
  const frontZ=z-Math.cos(ry*Math.PI/180)*len*.5+.2;
  put(`rib_${side}_${i+1}_front`,parent,[frontX-sign*2.5,y-.12,frontZ+.5],[fw,i===0?.5:1,1],[sign*3,sign*4,rz],'bone',
    side==='left'&&i===1?'Segunda costilla izquierda acortada':side==='right'&&i===4?'Quinta derecha fracturada':'Retorno frontal independiente');
}

// Un solo núcleo cruciforme: centro, dos barras y dos esquirlas del mismo hueso.
put('ecto_core','chest_core',[0,.1,.4],[1.5,1.5,1.5],[0,0,0],'ecto','Centro único del tórax');
put('ecto_vertical','chest_core',[0,.1,.4],[1,3,1],[0,0,0],'ecto','Brazo vertical de la cruz');
put('ecto_horizontal','chest_core',[0,.1,.25],[3,1,1],[0,0,0],'ecto','Brazo horizontal de la cruz');
put('ecto_shard_high','chest_core',[1.2,1.25,.35],[.5,.5,.5],[0,10,12],'ecto','Esquirla próxima del núcleo');
put('ecto_shard_low','chest_core',[-1.1,-1.2,.4],[.5,.5,.5],[0,-12,-8],'ecto','Esquirla próxima del núcleo');
put('cervical_1','neck',[0,.45,0],[1.5,1.5,1.5],[0,4,-3],'bone','Cervical inferior');
put('cervical_2','neck',[.1,1.7,-.1],[1.5,1.5,1.5],[-5,-5,4],'bone','Cervical superior');

// Cráneo: placas separadas forman una fractura real y cuencas hundidas; se libera el borde oral inferior.
put('cranium_occipital','head',[0,2.5,1.25],[7,6,4.5],[0,0,0],'bone','Caja posterior, boca inferior despejada');
put('cranium_frontal_left','head',[-2,2.75,-1.75],[3,5.5,1.5],[0,0,0],'bone','Placa izquierda del surco frontal');
put('cranium_frontal_right','head',[1.75,2.75,-1.75],[3.5,5.5,1.5],[0,0,0],'bone','Placa derecha del surco frontal');
put('brow_left','head',[-2,4.75,-3],[3,1.5,1],[0,0,0],'bone','Arco superciliar interrumpido por la fractura');
put('brow_right','head',[1.75,4.75,-3],[3.5,1.5,1],[0,0,0],'bone','Arco superciliar derecho');
put('orbital_bar_left','head',[3,2.75,-3],[1,2.5,1],[0,0,0],'bone','Límite exterior de cuenca izquierda');
put('orbital_bar_right','head',[-3,2.75,-3],[1,2.5,1],[0,0,0],'bone','Límite exterior de cuenca derecha');
put('nasal_bridge','head',[0,2.75,-3],[1,2.5,1],[0,0,0],'bone','Puente nasal entre cuencas de 2 u');
put('maxillary_plate','head',[0,.75,-3],[7,1.5,1],[0,0,0],'bone','Arco maxilar inferior');
put('fracture_chip','head',[-.15,4.1,-1.7],[1,1,2],[0,-14,18],'bone','Astilla dentro de la fractura superior');
put('zygomatic_left','head',[2.5,.8,-2.75],[1.5,1.5,1.5],[0,0,7],'bone','Pómulo saliente izquierdo');
put('zygomatic_right','head',[-2.5,.75,-2.75],[1.5,1.5,1.5],[0,0,-4],'bone','Pómulo derecho desgastado');
const upperTeeth=[[-2.2,-.5,.5,1],[-1.1,-.5,1,1],[0,-.25,.5,.5],[1.0,-.65,1,1],[2.2,-.45,.5,1]];
for(let i=0;i<upperTeeth.length;i++){const[x,y,w,h]=upperTeeth[i];put(`tooth_upper_${i+1}`,'head',[x,y,-3],[w,h,1],[0,0,[4,-3,0,7,-5][i]],'bone',i===2?'Diente superior roto':'Diente superior individual');}

// Mandíbula hueca en U: frente y dos cuerpos laterales, dos ramas y cinco dientes.
put('mandible_chin','jaw',[0,-1.75,-2.9],[5,1.5,1],[0,0,0],'bone','Mentón anterior');
put('mandible_body_left','jaw',[2,-1.75,-.9],[1,1,3],[0,0,0],'bone','Cuerpo izquierdo de la U mandibular');
put('mandible_body_right','jaw',[-2,-1.75,-.9],[1,1,3],[0,0,0],'bone','Cuerpo derecho de la U mandibular');
put('mandible_ramus_left','jaw',[2,-.25,.15],[1,2.5,1],[0,0,0],'bone','Rama y articulación izquierda');
put('mandible_ramus_right','jaw',[-2,-.25,.15],[1,2.5,1],[0,0,0],'bone','Rama y articulación derecha');
const lowerTeeth=[[-2.0,-.65,.5,.5],[-1.0,-.5,.5,1],[0,-.65,1,.5],[1.05,-.5,.5,1],[2.0,-.65,.5,.5]];
for(let i=0;i<lowerTeeth.length;i++){const[x,y,w,h]=lowerTeeth[i];put(`tooth_lower_${i+1}`,'jaw',[x,y,-2.9],[w,h,1],[0,0,[-6,4,-2,6,-3][i]],'bone','Diente inferior con altura y desgaste propios');}

Canvas.updateAll();Project.model_3d.updateMatrixWorld(true);
return {stage:'anatomia_axial',cubes:Cube.all.length,bones:Group.all.length};
