if(Project.model_identifier!=='osario_quebrado'||Cube.all.length!==169||Animation.all.length||!osarioF3?.layout)throw Error('Se requiere el editable de Fase 3.');
const chestCore=Group.all.find(g=>g.name==='chest_core');
const corePieces=Cube.all.filter(c=>c.parent===chestCore);
const sternum=Cube.all.find(c=>c.name==='sternum'),manubrium=Cube.all.find(c=>c.name==='manubrium');
if(Math.abs(sternum.size()[0]-1.5)>1e-5||Math.abs(manubrium.size()[0]-2)>1e-5)throw Error('El ajuste ya está aplicado o la base no coincide.');
const round=n=>Math.round(n*100000)/100000;
for(const [c,width] of [[sternum,.5],[manubrium,1]]){
 const shift=new THREE.Vector3(.25,0,0).applyQuaternion(c.mesh.quaternion);
 c.origin=c.origin.map((v,i)=>round(v+shift.getComponent(i)));
 c.from[0]=round(c.origin[0]-width/2);c.to[0]=round(c.origin[0]+width/2);
 for(const field of ['from','to'])for(const i of [1,2])c[field][i]=round(c[field][i]+shift.getComponent(i));
 const p=osarioF3.parts.find(p=>p.name===c.name),island=osarioF3.layout.islands.find(p=>p.element===c.name);
 p.size[0]=width;
 const [w,h,d]=p.size,[x,y]=island.rectangle,U=x/2,V=y/2;
 c.box_uv=false;island.explicit_faces=true;island.rectangle=[x,y,4*(w+d),2*(h+d)];
 const rects={east:[U,V+d,U+d,V+d+h],north:[U+d,V+d,U+d+w,V+d+h],
 west:[U+d+w,V+d,U+2*d+w,V+d+h],south:[U+2*d+w,V+d,U+2*(w+d),V+d+h],
 up:[U+d,V,U+d+w,V+d],down:[U+d+w,V,U+d+2*w,V+d]};
 for(const [face,uv] of Object.entries(rects)){c.faces[face].uv=uv;c.faces[face].rotation=0;}
}
chestCore.origin[1]=round(chestCore.origin[1]-.5);
for(const c of corePieces)for(const field of ['origin','from','to'])c[field][1]=round(c[field][1]-.5);
Canvas.updateAll();Project.model_3d.updateMatrixWorld(true);
return {sternum:sternum.size(),manubrium:manubrium.size(),core_pivot:chestCore.origin};
