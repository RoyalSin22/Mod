// Inspección de las interpolaciones efectivas del editor, no de las poses de entrada.
const clips=Animation.all.filter(a=>a.name.startsWith('animation.osario_quebrado.'));
if(clips.length!==10)throw Error('Se esperan diez clips del Osario.');
const oldTime=Timeline.time;
const oldPlaying=Animation.all.map(a=>[a,a.playing]);
const boxes=Cube.all.map(c=>{
  c.mesh.geometry.computeBoundingBox();const b=c.mesh.geometry.boundingBox,corners=[];
  for(const x of [b.min.x,b.max.x])for(const y of [b.min.y,b.max.y])for(const z of [b.min.z,b.max.z])corners.push(new THREE.Vector3(x,y,z));
  return {name:c.name,parent:c.parent.name,mesh:c.mesh,corners};
});
const aliases={walk:[0,.6],chase:[0,.425],alert:[.15],claw_swipe:[.35,.5],heavy_swipe:[.8,.95],
  ground_slam:[.9,1.15],lunge:[.7,.875,1.05],hurt:[.08],death:[.95],idle:[.6,1.3]};
const groups=Object.fromEntries(Group.all.map(g=>[g.name,g]));
function sample(animation,t){
  Animator.showDefaultPose(true);Timeline.time=t;
  for(const animator of Object.values(animation.animators))if(animator instanceof BoneAnimator)animator.displayFrame(1);
  Project.model_3d.updateMatrixWorld(true);
  const bounds=new THREE.Box3(),foot={left:Infinity,right:Infinity},mass=new THREE.Box3(),hands=new THREE.Box3();
  let lowest;
  const points=[];
  for(const c of boxes){
    for(const p of c.corners){
      const at=p.clone().applyMatrix4(c.mesh.matrixWorld);
      if(at.y<bounds.min.y)lowest=c.name;
      bounds.expandByPoint(at);points.push(at);
      if(c.parent==='dragging_end')mass.expandByPoint(at);
      if(['bone_hand','bone_thumb','bone_fingers'].includes(c.parent))hands.expandByPoint(at);
      if(c.parent==='foot_left')foot.left=Math.min(foot.left,at.y);
      if(c.parent==='foot_right')foot.right=Math.min(foot.right,at.y);
    }
  }
  const positions=Object.fromEntries(['pelvis','head','dragging_end','bone_hand','chest_core'].map(n=>[n,groups[n].mesh.getWorldPosition(new THREE.Vector3()).toArray()]));
  return {points,low:bounds.min.y,lowest,bounds:[bounds.min.toArray(),bounds.max.toArray()],
    foot,mass:[mass.min.toArray(),mass.max.toArray()],hands:[hands.min.toArray(),hands.max.toArray()],positions};
}

// Corrige únicamente las décimas residuales producidas por la interpolación Catmull-Rom.
const root=groups.root, corrections={};
try{
 for(const clip of clips){
  const frames=Math.ceil(clip.length*160),poses=[];
  for(let i=0;i<=frames;i++){
   const t=clip.length*i/frames;
   const q=sample(clip,t);
   poses.push({t,base:root.mesh.position.y-root.mesh.fix_position.y,lift:Math.max(0,-q.low-.006)});
  }
  const maximum=Math.max(...poses.map(p=>p.lift));
  if(maximum<=.01)continue;
  const anim=clip.getBoneAnimator(root);
  const sampled=poses.map((item,i)=>{
   let y=0;
   for(let j=Math.max(0,i-6);j<=Math.min(frames,i+6);j++){
    const f=(1+Math.cos(Math.PI*Math.abs(j-i)/7))/2;
    y=Math.max(y,poses[j].lift*f);
   }
   const total=item.base+y;
   return {t:item.t,y:Math.round(total*1e5)/1e5};
  });
  if(clip.loop==='loop')sampled.at(-1).y=sampled[0].y;
  anim.position.splice(0,anim.position.length);
  for(let i=0;i<sampled.length;i++){
   const p=sampled[i];
   if(i>0&&i<sampled.length-1&&[sampled[i-1].y,p.y,sampled[i+1].y].every(v=>Math.abs(v)<1e-5))continue;
   anim.addKeyframe({channel:'position',time:Math.round(p.t*1e6)/1e6,interpolation:'catmullrom',
     data_points:[{x:0,y:p.y,z:0}]},guid());
  }
  corrections[clip.name]={maximum,keyframes:anim.position.length};
 }
}finally{
 Timeline.time=oldTime;
 for(const [a,playing]of oldPlaying)a.playing=playing;
 Animator.showDefaultPose();
}
return corrections;
