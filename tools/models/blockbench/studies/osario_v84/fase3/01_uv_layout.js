// Fase 3: atlas único 256², resolución lógica 128², densidad uniforme 2 px/u.
// Bandas por material y por región anatómica. No se altera la geometría aprobada.
if(Project.model_identifier!=='osario_quebrado'||Cube.all.length!==169||Group.all.length!==30||Texture.all.length)
  throw Error('Se requiere la geometría aprobada, todavía sin texturas.');
const materials={9:'bone',2:'earth',3:'root',7:'moss',6:'ecto'};
const parts=Cube.all.map(c=>({name:c.name,parent:c.parent.name,size:c.size().map(v=>Math.round(v*2)/2),material:materials[c.color]}));
if(parts.some(p=>!p.material))throw Error('Marcador de material desconocido en la copia aprobada.');
function zone(p){
  if(p.material!=='bone')return p.material;
  if(['head','jaw'].includes(p.parent))return 'bone_head';
  if(['pelvis','body','spine_upper','chest','ribs_left','ribs_right','neck'].includes(p.parent))return 'bone_torso';
  if(/^(bone_|thigh_|shin_|foot_)/.test(p.parent))return 'bone_limbs';
  return 'bone_embedded';
}
const order=['bone_head','bone_torso','bone_limbs','bone_embedded','earth','root','moss','ecto'];
let x=1,y=1,row=0;const islands=[],bands=[];
for(const group of order){
  x=1;row=0;const start=y;
  const selected=parts.filter(a=>zone(a)===group).sort((a,b)=>(b.size[1]+b.size[2])-(a.size[1]+a.size[2])||a.name.localeCompare(b.name));
  for(const a of selected){
    const c=Cube.all.find(c=>c.name===a.name),[w,h,d]=a.size,W=Math.round(4*(w+d)),H=Math.round(2*(h+d));
    if(x+W>255){x=1;y+=row+1;row=0;}
    if(y+H>255)throw Error('Atlas fuera de 256²');
    const U=x/2,V=y/2;c.uv_offset=[U,V];c.mirror_uv=false;
    c.box_uv=!a.size.some(v=>Math.abs(v-Math.round(v))>1e-6);
    if(!c.box_uv){
      const rects={east:[U,V+d,U+d,V+d+h],north:[U+d,V+d,U+d+w,V+d+h],
        west:[U+d+w,V+d,U+2*d+w,V+d+h],south:[U+2*d+w,V+d,U+2*(w+d),V+d+h],
        up:[U+d,V,U+d+w,V+d],down:[U+d+w,V,U+d+2*w,V+d]};
      for(const [face,uv]of Object.entries(rects)){c.faces[face].uv=uv;c.faces[face].rotation=0;}
    }
    islands.push({element:a.name,parent:a.parent,material:a.material,zone:group,rectangle:[x,y,W,H],explicit_faces:!c.box_uv});
    x+=W+1;row=Math.max(row,H);
  }
  y+=row+2;bands.push({group,top:start,bottom:y-2});
}
Canvas.updateAll();Project.model_3d.updateMatrixWorld(true);
for(const c of Cube.all)c.mesh.traverse(o=>{if(o.geometry)o.geometry.computeBoundingBox();});
globalThis.osarioF3={parts,layout:{logical:[128,128],physical:[256,256],density:2,islands,bands,last_row:y-2}};
return {islands:islands.length,rows:y-2,bands,textures:Texture.all.length};
