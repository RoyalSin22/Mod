// Revisión visual del conjunto: aire entre costillas y ausencia de interpenetración entre dientes.
const {parts,bones}=osarioF2;
const ys={left:[2.5,1.2,-.1,-1.4,-2.7,-4],right:[1.85,.55,-.75,-2.05,-3.35,-4.55]};
for(const a of parts){
  const match=a.name.match(/^rib_(left|right)_(\d+)_(arc|front)$/);
  if(match){const side=match[1],i=Number(match[2])-1,front=match[3]==='front';a.center[1]=ys[side][i]-(front?.1:0);a.size[1]=.5;
    if(front)a.rotation[2]=(side==='left'?1:-1)*(i===4?9:6);
  }
  if(a.name==='tooth_lower_2'||a.name==='tooth_lower_4'){a.size[1]=.5;a.center[1]=-.75;}
}
function refresh(){
  for(const a of parts){const c=Cube.all.find(c=>c.uuid===a.uuid),p=new THREE.Vector3(...bones.get(a.parent).origin).add(new THREE.Vector3(...a.center));
    c.origin=p.toArray();c.from=p.clone().sub(new THREE.Vector3(...a.size).multiplyScalar(.5)).toArray();
    c.to=p.clone().add(new THREE.Vector3(...a.size).multiplyScalar(.5)).toArray();c.rotation=a.rotation.slice();
  }
  Canvas.updateAll();Project.model_3d.updateMatrixWorld(true);
}
globalThis.osarioF2.refresh=refresh;refresh();
return {stage:'separacion_costal_y_dientes',cubes:Cube.all.length};
