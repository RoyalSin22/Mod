// Inspección de superficie a 2 muestras/u: descarta puntos enterrados en otros cubos y apoyos bajo el suelo.
// Estima cobertura visible de musgo; no modifica el modelo ni su pintura.
const texture=Texture.all[0],bitmap=texture.ctx.getImageData(0,0,256,256).data,
  moss=new Set(osarioF3.palette.moss.map(c=>c.toUpperCase()));
const isMass=c=>/^(mass_shoulder|dragging_|debris_)/.test(c.parent.name);
const volumes=Cube.all.map(c=>{c.mesh.geometry.computeBoundingBox();return{cube:c,box:c.mesh.geometry.boundingBox,
 inverse:c.mesh.matrixWorld.clone().invert(),world:c.mesh.geometry.boundingBox.clone().applyMatrix4(c.mesh.matrixWorld)};});
let all=0,exposed=0,green=0,greenExposed=0;
for(const cube of Cube.all.filter(isMass)){
  const g=cube.mesh.geometry,pa=g.attributes.position,uv=g.attributes.uv,q=cube.mesh.getWorldQuaternion(new THREE.Quaternion());
  for(let fi=0;fi<6;fi++){
    const vs=[0,1,2,3].map(k=>new THREE.Vector3().fromBufferAttribute(pa,fi*4+k)),ts=[0,1,2,3].map(k=>[uv.getX(fi*4+k)*256,(1-uv.getY(fi*4+k))*256]);
    const x0=Math.round(Math.min(...ts.map(v=>v[0]))),x1=Math.round(Math.max(...ts.map(v=>v[0]))),
      y0=Math.round(Math.min(...ts.map(v=>v[1]))),y1=Math.round(Math.max(...ts.map(v=>v[1])));
    const u1=ts[1][0]-ts[0][0],v1=ts[1][1]-ts[0][1],u2=ts[2][0]-ts[0][0],v2=ts[2][1]-ts[0][1],det=u1*v2-v1*u2;
    const d1=vs[1].clone().sub(vs[0]),d2=vs[2].clone().sub(vs[0]),normal=new THREE.Vector3();
    for(let axis=0;axis<3;axis++)if(vs.every(v=>Math.abs(v.getComponent(axis)-vs[0].getComponent(axis))<1e-5)){normal.setComponent(axis,Math.sign(vs[0].getComponent(axis)));break;}
    normal.applyQuaternion(q);
    for(let y=y0;y<y1;y++)for(let x=x0;x<x1;x++){
      all++;const k=(y*256+x)*4,hex='#'+Array.from(bitmap.slice(k,k+3)).map(v=>v.toString(16).padStart(2,'0')).join('').toUpperCase(),isGreen=moss.has(hex);if(isGreen)green++;
      const du=x+.5-ts[0][0],dv=y+.5-ts[0][1],a=(du*v2-dv*u2)/det,b=(u1*dv-v1*du)/det,
        p=vs[0].clone().addScaledVector(d1,a).addScaledVector(d2,b).applyMatrix4(cube.mesh.matrixWorld);
      if(p.y<.02&&normal.y<-.8)continue;
      const outward=p.clone().addScaledVector(normal,.01);
      const buried=volumes.some(v=>{if(v.cube===cube||!v.world.containsPoint(outward))return false;const z=outward.clone().applyMatrix4(v.inverse);
        return z.x>v.box.min.x+.001&&z.x<v.box.max.x-.001&&z.y>v.box.min.y+.001&&z.y<v.box.max.y-.001&&z.z>v.box.min.z+.001&&z.z<v.box.max.z-.001;});
      if(!buried){exposed++;if(isGreen)greenExposed++;}
    }
  }
}
return {method:'Face texel centers at 2 px/u; oriented-volume occlusion and ground contact removed',mass_surface_samples:all,
  moss_samples:green,exposed_samples:exposed,exposed_moss_samples:greenExposed,
  moss_gross_uv_pct:green/all*100,moss_exposed_sample_pct:greenExposed/exposed*100,limit_pct:10};
