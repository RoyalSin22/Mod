// SAT de 15 ejes sobre matrices virtuales. No transforma objetos del proyecto.
Project.model_3d.updateMatrixWorld(true);
const q=Group.all.find(g=>g.name==='chest').mesh.getWorldQuaternion(new THREE.Quaternion());
const pivot=Group.all.find(g=>g.name==='chest_core').mesh.getWorldPosition(new THREE.Vector3());
function volume(c,m){
  c.mesh.geometry.computeBoundingBox();const box=c.mesh.geometry.boundingBox;
  const center=box.getCenter(new THREE.Vector3()).applyMatrix4(m),size=box.getSize(new THREE.Vector3()).multiplyScalar(.5);
  const axes=[0,1,2].map(i=>new THREE.Vector3().setFromMatrixColumn(m,i)),half=axes.map((a,i)=>a.length()*size.getComponent(i));
  axes.forEach(a=>a.normalize());return {name:c.name,core:c.parent.name==='chest_core',center,axes,half};
}
function penetration(a,b){
  const delta=b.center.clone().sub(a.center),axes=[...a.axes,...b.axes];
  for(const aa of a.axes)for(const bb of b.axes){const n=aa.clone().cross(bb);if(n.lengthSq()>1e-10)axes.push(n.normalize());}
  let depth=Infinity;
  for(const n of axes){
    const ra=a.axes.reduce((v,axis,i)=>v+Math.abs(axis.dot(n))*a.half[i],0);
    const rb=b.axes.reduce((v,axis,i)=>v+Math.abs(axis.dot(n))*b.half[i],0),d=ra+rb-Math.abs(delta.dot(n));
    if(d<0)return 0;depth=Math.min(depth,d);
  }
  return depth;
}
const output=[];
for(const variant of [{name:'actual',narrow:false,y:0,z:0},{name:'descenso_0_5_adelanto_0_25',narrow:true,y:-.5,z:-.25},
  {name:'descenso_0_5_sin_adelanto',narrow:true,y:-.5,z:0},{name:'solo_estrechamiento',narrow:true,y:0,z:0}]){
  const shift=new THREE.Vector3(0,variant.y,variant.z).applyQuaternion(q),cases=[];
  for(const scale of [1,1.12,1.35]){
    const transform=new THREE.Matrix4().makeTranslation(pivot.x+shift.x,pivot.y+shift.y,pivot.z+shift.z)
      .multiply(new THREE.Matrix4().makeScale(scale,scale,scale))
      .multiply(new THREE.Matrix4().makeTranslation(-pivot.x,-pivot.y,-pivot.z));
    const volumes=Cube.all.map(c=>{const m=c.mesh.matrixWorld.clone();
      if(variant.narrow&&['sternum','manubrium'].includes(c.name))m.multiply(new THREE.Matrix4().makeScale(c.name==='sternum'?1/3:.5,1,1));
      if(c.parent.name==='chest_core')m.premultiply(transform);return volume(c,m);
    });
    const overlaps=[];
    for(const a of volumes.filter(v=>v.core))for(const b of volumes.filter(v=>!v.core)){
      const depth=penetration(a,b);if(depth>.01)overlaps.push({energy:a.name,obstacle:b.name,penetration:depth});
    }
    cases.push({scale,overlaps});
  }
  output.push({hypothesis:variant,cases});
}
return {applied_to_project:false,tolerance:.01,method:'15-axis OBB SAT; virtual uniform scale about core pivot',studies:output};
