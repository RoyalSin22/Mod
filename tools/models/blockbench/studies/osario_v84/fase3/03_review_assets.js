// Archivos de revisión de la Puerta 3. No se exporta geometría ni se escriben recursos de Minecraft.
const state=osarioF3,t=Texture.all.find(t=>t.name==='osario_quebrado.png');
const directory='C:/Users/Johan/Desktop/Gits/MOD/osario-quebrado-v84/tools/models/blockbench/studies/osario_v84/fase3/';
function png(name,canvas){Blockbench.writeFile(directory+name,{savetype:'image',content:canvas.toDataURL('image/png')});}
function json(name,value){Blockbench.writeFile(directory+name,{content:JSON.stringify(value,null,2)+'\n'});}
png('osario_quebrado.png',t.canvas);png('osario_quebrado_glowmask.png',state.maskCanvas);
const large=document.createElement('canvas');large.width=large.height=2048;
large.getContext('2d').imageSmoothingEnabled=false;large.getContext('2d').drawImage(t.canvas,0,0,2048,2048);
png('textura_x8.png',large);
const guide=document.createElement('canvas');guide.width=guide.height=256;
const gc=guide.getContext('2d');gc.fillStyle='#18212b';gc.fillRect(0,0,256,256);gc.drawImage(t.canvas,0,0);
gc.lineWidth=1;for(const i of state.layout.islands){const[x,y,w,h]=i.rectangle;gc.strokeStyle=i.material==='bone'?'#839399':i.material==='ecto'?'#4DE2A6':'#987756';gc.strokeRect(x-.5,y-.5,w+1,h+1);}
png('uv_islas_256.png',guide);
const palette=document.createElement('canvas');palette.width=800;palette.height=536;
const pc=palette.getContext('2d');pc.fillStyle='#10191e';pc.fillRect(0,0,800,536);
pc.fillStyle='#F0EEDD';pc.font='bold 24px monospace';pc.fillText('OSARIO QUEBRADO V84 · PALETA',24,35);
pc.font='15px monospace';pc.fillStyle='#B7B5A6';pc.fillText('256 × 256 px · 2 texeles/u · 25 colores · sin antialiasing',24,61);
const labels={bone:'HUESO ENVEJECIDO',earth:'TIERRA / BARRO',root:'RAÍZ / MADERA',dark:'GRIETAS / CAVIDADES',moss:'MUSGO LOCALIZADO',ecto:'ECTOPLASMA'};
let py=92;for(const[material,colors]of Object.entries(state.palette)){
  pc.fillStyle='#D9D6C3';pc.font='bold 14px monospace';pc.fillText(labels[material],24,py);
  let px=24;for(const hex of colors){pc.fillStyle=hex;pc.fillRect(px,py+9,103,23);pc.font='12px monospace';pc.fillStyle='#D9D6C3';pc.fillText(hex,px,py+48);px+=122;}py+=70;
}
png('paleta.png',palette);
json('uv_layout.json',state.layout);json('caras_pintadas.json',state.faceRecords);json('paleta.json',state.palette);json('pintura_capas.json',state.paintStats);

// Vista previa de emisión: copia de los colores de la base con el alfa de la máscara.
// Solo se añaden ayudantes THREE no exportables y se retiran al terminar la revisión.
const source=t.ctx.getImageData(0,0,256,256),mask=state.maskCanvas.getContext('2d').getImageData(0,0,256,256),rgba=new Uint8ClampedArray(source.data);
for(let i=0;i<rgba.length;i+=4)rgba[i+3]=mask.data[i+3];
const preview=document.createElement('canvas');preview.width=preview.height=256;preview.getContext('2d').putImageData(new ImageData(rgba,256,256),0,0);
state.previewCanvas=preview;state.emissionHelpers=[];
const materials=new Set();for(const c of Cube.all)for(const m of Array.isArray(c.mesh.material)?c.mesh.material:[c.mesh.material])if(m?.uniforms?.LIGHTCOLOR)materials.add(m);
state.originalLights=[...materials].map(m=>({material:m,light:m.uniforms.LIGHTCOLOR.value.clone()}));
state.previewEmission=function(on,dark=false){
  for(const h of state.emissionHelpers)h.removeFromParent();state.emissionHelpers=[];
  if(state.emissionMaterial){state.emissionMaterial.dispose();state.emissionMaterial=null;}
  if(state.emissionTexture){state.emissionTexture.dispose();state.emissionTexture=null;}
  for(const entry of state.originalLights)entry.material.uniforms.LIGHTCOLOR.value.copy(entry.light).multiplyScalar(dark?.22:1);
  if(on){
    const map=new THREE.CanvasTexture(state.previewCanvas);map.magFilter=THREE.NearestFilter;map.minFilter=THREE.NearestFilter;
    // El mismo shader del editor evita diferencias de gamma respecto a la textura base.
    map.encoding=t.material.map.encoding;
    const material=t.material.clone();material.map=map;material.uniforms.map.value=map;
    material.uniforms.EMISSIVE.value=true;material.uniforms.SHADE.value=false;material.uniforms.LIGHTCOLOR.value.setRGB(1,1,1);
    material.transparent=true;material.depthWrite=false;material.polygonOffset=true;material.polygonOffsetFactor=-1;material.polygonOffsetUnits=-1;
    for(const c of Cube.all.filter(c=>c.parent.name==='chest_core')){
      const mesh=new THREE.Mesh(c.mesh.geometry,material);mesh.name='OSARIO_EMISSION_REVIEW_ONLY';mesh.renderOrder=100;c.mesh.add(mesh);state.emissionHelpers.push(mesh);
    }
    state.emissionTexture=map;state.emissionMaterial=material;
  }
  Project.model_3d.updateMatrixWorld(true);
  return {emission:on,dark,preview_helpers:state.emissionHelpers.length};
};
return {review_files:9,layers:t.layers.length,base:directory+'osario_quebrado.png',mask:directory+'osario_quebrado_glowmask.png'};
