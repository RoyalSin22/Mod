// UV provisional sin pintura: empaqueta islas únicas y preserva dimensiones fraccionarias al exportar.
// La composición artística del atlas pertenece a la Fase 3.
const {parts}=osarioF2;
const sorted=parts.slice().sort((a,b)=>(b.size[1]+b.size[2])-(a.size[1]+a.size[2])||a.name.localeCompare(b.name));
let x=.5,y=.5,rowHeight=0;
const islands=[];
for(const a of sorted){
  const c=Cube.all.find(c=>c.uuid===a.uuid),[w,h,d]=a.size,iw=2*(w+d),ih=h+d;
  if(x+iw+.5>128){x=.5;y+=rowHeight+.5;rowHeight=0;}
  if(y+ih>127.5)throw Error('El empaquetado provisional excede 128²');
  c.uv_offset=[x,y];c.box_uv=!a.size.some(v=>Math.abs(v-Math.round(v))>1e-6);
  if(!c.box_uv){
    const rects={east:[x,y+d,x+d,y+d+h],north:[x+d,y+d,x+d+w,y+d+h],
      west:[x+d+w,y+d,x+2*d+w,y+d+h],south:[x+2*d+w,y+d,x+iw,y+d+h],
      up:[x+d,y,x+d+w,y+d],down:[x+d+w,y,x+d+2*w,y+d]};
    for(const[face,uv]of Object.entries(rects))c.faces[face].uv=uv;
  }
  islands.push({element:a.name,rectangle:[x,y,iw,ih],explicit_faces:!c.box_uv});
  x+=iw+.5;rowHeight=Math.max(rowHeight,ih);
}
Canvas.updateAll();Project.model_3d.updateMatrixWorld(true);
// setFromPoints del contorno puede conservar su boundingBox anterior. Recalcular también las ayudas
// invisibles evita que Box3.setFromObject mida el tamaño antiguo de una pieza ya refinada.
for(const c of Cube.all)c.mesh.traverse(o=>{if(o.geometry)o.geometry.computeBoundingBox();});
globalThis.osarioF2.uv={resolution:[128,128],intended_png:[256,256],pixels_per_unit:2,rows:y+rowHeight,islands};
return {cubes:Cube.all.length,bones:Group.all.length,uv_rows:y+rowHeight,
  fractional_cubes:islands.filter(i=>i.explicit_faces).length,textures:Texture.all.length,animations:Animation.all.length};
