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
const withoutPoints=({points,...rest})=>rest;
const result={sample_rate_hz:160,clips:{}};
const landmarks={};
try{
  for(const a of clips){
    const name=a.name.split('.').at(-1),steps=Math.ceil(a.length*160);
    const report={duration:a.length,loop:a.loop,samples:steps+1,keyframes:Object.values(a.animators).reduce((n,b)=>n+b.keyframes.length,0),
      floor:{y:Infinity},fastest_vertex:{u_per_second:0},max_grounded_mass_y:0,foot_lift:{left:0,right:0},markers:{}};
    let previous,first;
    for(let i=0;i<=steps;i++){
      const t=a.length*i/steps,frame=sample(a,t);
      if(i===0)first=frame;
      if(frame.low<report.floor.y)report.floor={t,y:frame.low,cube:frame.lowest};
      report.max_grounded_mass_y=Math.max(report.max_grounded_mass_y,frame.mass[0][1]);
      for(const side of ['left','right'])report.foot_lift[side]=Math.max(report.foot_lift[side],frame.foot[side]);
      if(previous){
        for(let j=0;j<frame.points.length;j++){
          const v=frame.points[j].distanceTo(previous.points[j])*steps/a.length;
          if(v>report.fastest_vertex.u_per_second)report.fastest_vertex={t,u_per_second:v,cube:boxes[j>>3].name};
        }
      }
      previous=frame;
    }
    for(const t of aliases[name])report.markers[t]=withoutPoints(sample(a,t));
    if(a.loop==='loop'){
      const nearEnd=sample(a,a.length-1e-5);
      report.loop_near_seam_max_u=Math.max(...first.points.map((p,j)=>p.distanceTo(nearEnd.points[j])));
    }
    result.clips[name]=report;
    landmarks[name]={duration:a.length,loop:a.loop,bones:{}};
    for(const [uuid,b]of Object.entries(a.animators)){
      const group=Group.all.find(g=>g.uuid===uuid);if(!group)continue;
      landmarks[name].bones[group.name]=Object.fromEntries(['rotation','position','scale'].filter(c=>b[c].length).map(channel=>[channel,
        b[channel].map(k=>[k.time,...k.data_points.slice(0,1).flatMap(p=>[Number(p.x),Number(p.y),Number(p.z)])])]));
    }
  }
}finally{
  Timeline.time=oldTime;
  for(const [a,playing]of oldPlaying)a.playing=playing;
  Animator.showDefaultPose();
  Project.model_3d.updateMatrixWorld(true);
}
const dir='C:/Users/Johan/Desktop/Gits/MOD/osario-quebrado-v84/tools/models/blockbench/studies/osario_v84/fase4/';
Blockbench.writeFile(dir+'claves_finales.json',{content:JSON.stringify(landmarks)+'\n'});
return result;
