// Autoría pixel art mediante capas nativas de Blockbench. No se usa ruido ni muestreo aleatorio.
// Las agrupaciones son pinceladas discretas; cavidades, fracturas y fibras siguen la anatomía real.
const state=osarioF3,texture=Texture.all.find(t=>t.name==='osario_quebrado.png');
if(!texture||texture.layers.length!==5||Cube.all.length!==169)throw Error('Faltan atlas o capas de pintura.');
texture.uv_width=texture.uv_height=128;
const P={bone:['#F0EEDD','#D9D6C3','#B7B5A6','#8F8B7C','#5F5B50','#3B372F'],
 earth:['#6B5642','#5A4838','#4A3B2E','#382C22','#2A211A'],
 root:['#8A6B4F','#6B4E3A','#4D3828','#33261C'],dark:['#2F221F','#1A1210'],
 moss:['#5C7142','#4A5F35','#3B4D28'],ecto:['#C9FFF0','#7FFFD4','#4DE2A6','#2FB985','#1E8F6A']};
const rgb=hex=>[parseInt(hex.slice(1,3),16),parseInt(hex.slice(3,5),16),parseInt(hex.slice(5,7),16)];
const colors=new Map(Object.values(P).flat().map(c=>[c,rgb(c)]));
const layers=Array.from({length:5},()=>new Uint8ClampedArray(256*256*4));
const owner=new Int16Array(256*256).fill(-1),faceRecords=[],ectoPixels=[];
const layerNames=['01 — Base de materiales','02 — Porosidad y sedimentos','03 — Grietas y cavidades','04 — Musgo localizado','05 — Ectoplasma'];
const V=a=>new THREE.Vector3(...a),clamp=(x,a,b)=>Math.max(a,Math.min(b,x));
function ink(layer,x,y,color){const p=(y*256+x)*4,c=colors.get(color);if(!c)throw Error('Color fuera de paleta: '+color);layers[layer][p]=c[0];layers[layer][p+1]=c[1];layers[layer][p+2]=c[2];layers[layer][p+3]=255;}

// Trece composiciones de pinceladas. Se asignan por región, pieza y cara; no son teselas de textura.
const cards=[
 [[.16,.22],[.64,.48],[.36,.79]],[[.72,.19],[.26,.49],[.69,.81]],
 [[.38,.16],[.77,.56],[.18,.73]],[[.18,.48],[.61,.21],[.64,.76]],
 [[.78,.31],[.39,.67],[.20,.15]],[[.25,.76],[.70,.64],[.40,.29]],
 [[.52,.18],[.17,.60],[.79,.83]],[[.18,.28],[.74,.71],[.52,.51]],
 [[.68,.36],[.25,.19],[.34,.84]],[[.40,.46],[.78,.20],[.15,.77]],
 [[.22,.59],[.65,.84],[.74,.39]],[[.49,.77],[.21,.26],[.76,.55]],
 [[.76,.78],[.44,.21],[.16,.48]]
];
const brushes=[[[0,0],[1,0],[0,1],[1,1],[2,1]],[[1,0],[2,0],[0,1],[1,1],[2,1],[1,2]],
 [[0,0],[1,0],[0,1],[1,1],[0,2]],[[0,0],[1,0],[2,0],[1,1],[2,1]],
 [[1,0],[0,1],[1,1],[2,1],[0,2],[1,2]],[[0,0],[1,0],[1,1],[2,1],[2,2]],
 [[0,1],[1,0],[1,1],[2,1],[3,1],[2,2]],[[0,0],[0,1],[1,1],[1,2],[2,2]]];
const faceNames=Canvas.face_order.slice();
const families={head:0,jaw:6,pelvis:3,body:8,spine_upper:10,chest:1,ribs_left:2,ribs_right:9,
 neck:5,bone_shoulder:7,bone_upper_arm:11,bone_forearm:4,bone_hand:1,bone_fingers:8,bone_thumb:12,
 thigh_left:4,thigh_right:9,shin_left:12,shin_right:6,foot_left:7,foot_right:3};
const namedCards={femur_left_short:10,femur_right:4,tibia_left:7,tibia_right:2,
 heel_left:3,heel_right:9,metatarsal_left:1,metatarsal_right:11};
const rootLines=state.parts.filter(p=>p.material==='root').map(a=>{const c=Cube.all.find(c=>c.name===a.name),axis=a.size.indexOf(Math.max(...a.size)),lo=[0,0,0],hi=[0,0,0];lo[axis]=-a.size[axis]/2;hi[axis]=a.size[axis]/2;
 return {a:V(lo).applyMatrix4(c.mesh.matrixWorld),b:V(hi).applyMatrix4(c.mesh.matrixWorld),radius:.5};});
const earthVolumes=state.parts.filter(p=>p.material==='earth').map(a=>{const c=Cube.all.find(c=>c.name===a.name);return{inverse:c.mesh.matrixWorld.clone().invert(),half:a.size.map(v=>v/2)};});
function earthContact(world){let best=Infinity;for(const e of earthVolumes){const p=world.clone().applyMatrix4(e.inverse);best=Math.min(best,Math.max(Math.abs(p.x)-e.half[0],Math.abs(p.y)-e.half[1],Math.abs(p.z)-e.half[2]));}return best;}
function lineDistance(p,a,b){const ab=b.clone().sub(a),t=clamp(p.clone().sub(a).dot(ab)/ab.lengthSq(),0,1);return p.distanceTo(a.clone().addScaledVector(ab,t));}
function strokeDistance(x,y,path){let d=Infinity;for(let i=1;i<path.length;i++){const a=path[i-1],b=path[i],dx=b[0]-a[0],dy=b[1]-a[1],t=clamp(((x-a[0])*dx+(y-a[1])*dy)/(dx*dx+dy*dy||1),0,1);d=Math.min(d,Math.max(Math.abs(x-a[0]-t*dx),Math.abs(y-a[1]-t*dy)));}return d;}
function localNormal(vertices){const n=new THREE.Vector3();for(let axis=0;axis<3;axis++){if(vertices.every(v=>Math.abs(v.getComponent(axis)-vertices[0].getComponent(axis))<1e-5)){n.setComponent(axis,Math.sign(vertices[0].getComponent(axis)));break;}}return n;}
function patchHit(u,v,W,H,card,index,variant){
  const center=card[index],brush=brushes[(variant+index*3)%brushes.length];
  const ox=Math.floor(center[0]*W)-1+(index===1&&W>8?1:0),oy=Math.floor(center[1]*H)-1;
  return brush.some(([a,b])=>u===ox+a&&v===oy+b);
}
const fractureNames=new Set(['ilium_left_fractured','hip_chip_left','femur_left_short','tibia_right','patella_left',
 'embedded_femur_condyle','maxillary_plate','mandible_chin','lower_ossified_fragment']);
const crackPaths=[[[.15,.2],[.3,.35],[.25,.52],[.43,.69]],[[.75,.15],[.58,.36],[.64,.5],[.48,.64]],
 [[.17,.72],[.37,.64],[.4,.44],[.57,.36]],[[.71,.81],[.6,.65],[.66,.49],[.49,.31]]];
const roofCrack=[[-.25,-2.5],[-.25,-1.3],[-.7,-.3],[-.15,.5],[.2,1.6],[-.35,2.6],[-.35,3.5]];
let serial=0;
for(const part of state.parts){
  const cube=Cube.all.find(c=>c.name===part.name),geo=cube.mesh.geometry,pa=geo.attributes.position,ua=geo.attributes.uv;
  const variant=namedCards[part.name]??(serial*5+(families[part.parent]??4))%13,worldQ=cube.mesh.getWorldQuaternion(new THREE.Quaternion());
  for(let fi=0;fi<6;fi++){
    const vs=[0,1,2,3].map(k=>new THREE.Vector3().fromBufferAttribute(pa,fi*4+k));
    const ts=[0,1,2,3].map(k=>[ua.getX(fi*4+k)*256,(1-ua.getY(fi*4+k))*256]);
    const minX=Math.round(Math.min(...ts.map(v=>v[0]))),maxX=Math.round(Math.max(...ts.map(v=>v[0]))),
      minY=Math.round(Math.min(...ts.map(v=>v[1]))),maxY=Math.round(Math.max(...ts.map(v=>v[1]))),W=maxX-minX,H=maxY-minY;
    if(!W||!H)throw Error('Cara degenerada: '+part.name);
    const u1=ts[1][0]-ts[0][0],v1=ts[1][1]-ts[0][1],u2=ts[2][0]-ts[0][0],v2=ts[2][1]-ts[0][1],det=u1*v2-v1*u2;
    const delta1=vs[1].clone().sub(vs[0]),delta2=vs[2].clone().sub(vs[0]),normal=localNormal(vs),nworld=normal.clone().applyQuaternion(worldQ),face=faceNames[fi],card=cards[(variant+fi*3)%13];
    const record={element:part.name,parent:part.parent,material:part.material,face,rect:[minX,minY,W,H],pixels:W*H};
    const faceId=faceRecords.length;faceRecords.push(record);
    for(let v=0;v<H;v++)for(let u=0;u<W;u++){
      const x=minX+u,y=minY+v,du=x+.5-ts[0][0],dv=y+.5-ts[0][1],a=(du*v2-dv*u2)/det,b=(u1*dv-v1*du)/det;
      const local=vs[0].clone().addScaledVector(delta1,a).addScaledVector(delta2,b),bone=local.clone().applyMatrix4(cube.mesh.matrix),world=local.clone().applyMatrix4(cube.mesh.matrixWorld);
      if(owner[y*256+x]!==-1)throw Error('Solape UV de pintura: '+part.name+'/'+face);
      owner[y*256+x]=faceId;
      if(part.material==='bone'){
        let shade=nworld.y>.4?1:nworld.y<-.4?3:nworld.z<-.45?1:2;
        if(part.parent.startsWith('ribs_')&&normal.x*bone.x<0)shade=Math.max(shade,2);
        if(/^(thoracic_|lumbar_|intervertebral|dorsal_)/.test(part.name))shade=Math.max(2,shade);
        if(part.parent==='jaw'&&((part.name.includes('body_left')&&face==='west')||(part.name.includes('body_right')&&face==='east')||part.name==='mandible_chin'&&face==='south'))shade=Math.max(shade,3);
        ink(0,x,y,P.bone[shade]);
        const brushCount=W*H>=64?3:W*H>=24?2:1;
        for(let j=0;j<brushCount;j++)if(patchHit(u,v,W,H,card,j,variant+fi)){
          const level=clamp(shade+(j===0?-1:1),0,shade<=1?2:4);ink(1,x,y,P.bone[level]);
        }
        // Extremos articulares y barro de contacto, sin tapizar los ejes largos.
        const axis=part.size.indexOf(Math.max(...part.size)),long=part.size[axis],along=(local.getComponent(axis)+long*.5)/long;
        if(long>=4&&(along<.12||along>.91)&&normal.getComponent(axis)===0&&((u+fi)%3!==1))ink(1,x,y,P.bone[clamp(shade+1,2,4)]);
        if(world.y<.6&&!['head','jaw'].includes(part.parent)){
          let level=3;
          if(part.parent==='foot_left'){
            if(v>=Math.floor(H*.67)&&u<=Math.floor(W*.6))level=4;
            if(v===Math.floor(H*.35)&&u>Math.floor(W*.4))level=2;
          }else if(part.parent==='foot_right'){
            if(u>=Math.floor(W*.6)&&v<Math.ceil(H*.55))level=4;
            if(u===Math.floor(W*.25)&&v>Math.floor(H*.45))level=2;
          }else if(u===0||v===H-1)level=4;
          ink(1,x,y,P.bone[level]);
        }
        if(/^(mass_shoulder|dragging_|debris_)/.test(part.parent)){
          const contact=earthContact(world);
          if(contact>-.1&&contact<.42)ink(1,x,y,contact<.18?P.bone[4]:P.bone[2]);
        }
        if(fractureNames.has(part.name)&&W>=4&&H>=3&&fi%2===variant%2){
          const path=crackPaths[variant%4].map(([a,b])=>[Math.floor(a*W),Math.floor(b*H)]);
          if(strokeDistance(u,v,path)<.46)ink(2,x,y,P.dark[0]);
        }
        // Cavidades del rostro: exactamente en el fondo de las cuencas geométricas, no en las placas.
        if(/^cranium_frontal_/.test(part.name)&&face==='north'&&Math.abs(bone.x)>=.5&&Math.abs(bone.x)<2.5&&bone.y>=1.5&&bone.y<4){
          const rim=Math.abs(bone.x)>2.1||bone.y<1.8||bone.y>3.7;ink(2,x,y,rim?P.bone[5]:P.dark[1]);
        }
        if(part.name==='maxillary_plate'&&face==='north'&&Math.abs(bone.x)<.5&&bone.y>.5)ink(2,x,y,bone.y>1?P.dark[1]:P.dark[0]);
        if(part.name==='cranium_occipital'){
          if(face==='north'&&(bone.y<.15||(Math.abs(bone.x+.25)<.3&&bone.y>3.5)))ink(2,x,y,P.dark[1]);
          if(face==='up'){
            const d=strokeDistance(bone.x,bone.z,roofCrack);
            if(d<.23)ink(2,x,y,P.dark[0]);else if(d<.44&&bone.x<.2)ink(2,x,y,P.bone[0]);
          }
        }
        if(part.name==='fracture_chip'&&face!=='up')ink(1,x,y,P.bone[4]);
        if(part.name.startsWith('tooth_')){
          const root=part.parent==='head'?local.y>.1:local.y<-.1;
          ink(1,x,y,P.bone[root?2:nworld.y>0?0:1]);
        }
      }else if(part.material==='earth'){
        let shade=world.y<5?3:nworld.y>.4?0:nworld.y<-.4?4:nworld.z<-.4?1:2;
        ink(0,x,y,P.earth[shade]);
        // Terrones de 2–4 píxeles y estratos discontinuos: agrupaciones, nunca ruido por píxel.
        for(let j=0;j<3;j++)if(patchHit(u,v,W,H,card,j,variant+fi))ink(1,x,y,P.earth[clamp(shade+(j===0?-1:j===1?1:-2),0,4)]);
        if(W*H>=48){
          const secondary=cards[(variant+fi*3+7)%13];
          for(let j=0;j<3;j++)if(patchHit(u,v,W,H,secondary,j,variant+fi+4))ink(1,x,y,P.earth[clamp(shade+(j===1?1:-1),0,4)]);
        }
        const band=Math.floor((world.y+.35)/1.5),shelf=(u+variant)%7;
        if(v>0&&v<H-1&&band%4===0&&shelf<3&&world.y<15)ink(1,x,y,P.earth[Math.min(4,shade+1)]);
        if(W>=5&&H>=5&&(variant+fi)%3===0){const path=crackPaths[(variant+1)%4].map(([a,b])=>[Math.floor(a*W),Math.floor(b*H)]);if(strokeDistance(u,v,path)<.46)ink(2,x,y,P.dark[0]);}
        if(rootLines.some(r=>lineDistance(world,r.a,r.b)<.72))ink(2,x,y,P.earth[4]);
        if(face==='up'&&['shoulder_earth_core','middle_mud_lump','ground_rear_earth'].includes(part.name)){
          const cluster=brushes[(variant+2)%8],ox=Math.max(0,Math.floor(W*.26)),oy=Math.max(0,Math.floor(H*.3));
          const hit=cluster.findIndex(([a,b])=>u===ox+a&&v===oy+b);if(hit>=0)ink(3,x,y,P.moss[hit%3]);
        }
      }else if(part.material==='root'){
        const axis=part.size.indexOf(Math.max(...part.size)),length=part.size[axis],cross=axis===1?(normal.x?2:0):(normal.x?1:0),
          along=Math.floor((local.getComponent(axis)+length/2)*2),across=Math.floor((local.getComponent(cross)+part.size[cross]/2)*2);
        let shade=nworld.y<-.4?3:1;
        if(normal.getComponent(axis)!==0){shade=(u+v+variant)%3;ink(0,x,y,P.root[shade]);}
        else{
          shade=clamp(1+(across+variant+fi)%3-1,0,3);ink(0,x,y,P.root[shade]);
          const knot=Math.floor(length*2*[.28,.57,.43,.71][variant%4]);
          if(along===knot||along===knot+1)ink(1,x,y,P.root[(across+along+variant)%2?3:0]);
          else if(along%5===variant%5&&across===variant%2)ink(2,x,y,P.root[3]);
        }
        if(world.y<.35)ink(1,x,y,P.root[3]);
      }else if(part.material==='moss'){
        ink(0,x,y,P.earth[3]);
        const shape=face==='up'?['.gg.','gGGg','ggGg','.gg.']:['gGgg','gg.g'];
        const uu=Math.floor(u*4/W),vv=Math.floor(v*shape.length/H),ch=shape[vv][uu];
        if(ch!=='.')ink(3,x,y,P.moss[ch==='G'?0:(u+v+variant)%2?1:2]);
      }else if(part.material==='ecto'){
        const distance=Math.max(Math.abs(bone.x)/1.5,Math.abs(bone.y+.05)/1.6),
          edge=Math.min(u,v,W-1-u,H-1-v),center=Math.abs(u-(W-1)/2)+Math.abs(v-(H-1)/2);
        let shade=distance<.32?1:distance<.7?2:distance<.9?3:4;
        if(part.name==='ecto_core'&&u===Math.floor(W/2)&&v===Math.floor(H/2))shade=0;
        ink(4,x,y,P.ecto[shade]);
        ectoPixels.push({x,y,color:P.ecto[shade],shade,face,element:part.name,score:(4-shade)*10+(face==='north'||face==='south'?3:0)-center*.2});
      }
    }
  }
  serial++;
}
for(let i=0;i<5;i++){
  const l=texture.layers[i];l.name=layerNames[i];l.canvas.width=l.canvas.height=256;l.offset=[0,0];l.opacity=100;l.visible=true;
  l.ctx.imageSmoothingEnabled=false;l.ctx.putImageData(new ImageData(layers[i],256,256),0,0);
}
texture.updateLayerChanges(true);texture.saved=false;
const maskCanvas=document.createElement('canvas');maskCanvas.width=maskCanvas.height=256;
const mask=new Uint8ClampedArray(256*256*4);
for(const p of ectoPixels.slice().sort((a,b)=>b.score-a.score||a.y-b.y||a.x-b.x).slice(0,84)){
  const k=(p.y*256+p.x)*4;mask[k]=mask[k+1]=mask[k+2]=255;mask[k+3]=p.shade<=1?255:160;
}
maskCanvas.getContext('2d').putImageData(new ImageData(mask,256,256),0,0);
state.palette=P;state.faceRecords=faceRecords;state.owner=owner;state.maskCanvas=maskCanvas;state.layers=layers;
state.paintStats={faces:faceRecords.length,mapped_pixels:owner.reduce((s,v)=>s+(v>=0),0),emissive_pixels:84,
  layer_pixels:layers.map(a=>{let n=0;for(let i=3;i<a.length;i+=4)n+=a[i]!==0;return n;})};
Canvas.updateAll();
return {physical:[texture.width,texture.height],logical:[Project.texture_width,Project.texture_height],...state.paintStats};
