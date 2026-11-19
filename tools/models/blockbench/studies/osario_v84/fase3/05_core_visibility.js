// Diagnóstico de visibilidad, sin transformar piezas ni crear geometría.
// Muestreo de centros de texel a 2 px/u, ponderado por proyección ortográfica.
// Las superficies superpuestas del propio núcleo pueden sumar varias muestras;
// el porcentaje es una estimación de oclusión, no una medición de píxeles de pantalla.
Project.model_3d.updateMatrixWorld(true);
const meshes=Cube.all.map(c=>c.mesh),ray=new THREE.Raycaster();
const views=[{name:'frente',v:new THREE.Vector3(0,0,-1)},
  {name:'tres_cuartos',v:new THREE.Vector3(-25,8,-39).normalize()}];
const output=[];
for(const view of views){
  const occluders={},totals={visible:0,blocked:0};
  for(const c of Cube.all.filter(c=>c.parent.name==='chest_core')){
    const g=c.mesh.geometry,pa=g.attributes.position,uv=g.attributes.uv;
    for(let f=0;f<6;f++){
      const ps=[0,1,2].map(j=>new THREE.Vector3().fromBufferAttribute(pa,f*4+j));
      const ts=[0,1,2].map(j=>new THREE.Vector2(uv.getX(f*4+j)*256,(1-uv.getY(f*4+j))*256));
      const normal=ps[1].clone().sub(ps[0]).cross(ps[2].clone().sub(ps[0])).normalize().transformDirection(c.mesh.matrixWorld);
      const cos=normal.dot(view.v);if(cos<=.01)continue;
      const ua=ts[1].clone().sub(ts[0]),ub=ts[2].clone().sub(ts[0]),d=ua.x*ub.y-ua.y*ub.x;
      for(let y=Math.floor(Math.min(...ts.map(t=>t.y)));y<Math.ceil(Math.max(...ts.map(t=>t.y)));y++){
        for(let x=Math.floor(Math.min(...ts.map(t=>t.x)));x<Math.ceil(Math.max(...ts.map(t=>t.x)));x++){
          const du=x+.5-ts[0].x,dv=y+.5-ts[0].y,a=(du*ub.y-dv*ub.x)/d,b=(ua.x*dv-ua.y*du)/d;
          const p=ps[0].clone().addScaledVector(ps[1].clone().sub(ps[0]),a)
            .addScaledVector(ps[2].clone().sub(ps[0]),b).applyMatrix4(c.mesh.matrixWorld);
          ray.set(p.clone().addScaledVector(view.v,100),view.v.clone().negate());
          const hit=ray.intersectObjects(meshes,false)[0],cube=Cube.all.find(c=>c.mesh===hit?.object);
          if(cube?.parent.name==='chest_core')totals.visible+=cos;
          else{totals.blocked+=cos;const name=cube?.name??'none';occluders[name]=(occluders[name]||0)+cos;}
        }
      }
    }
  }
  output.push({view:view.name,projected_texel_samples:totals,
    estimated_exposed_pct:100*totals.visible/(totals.visible+totals.blocked),occluders});
}
return output;
