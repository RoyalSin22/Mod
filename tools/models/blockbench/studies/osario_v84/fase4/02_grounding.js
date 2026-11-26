// Resuelve apoyos en el espacio del modelo y hornea únicamente los canales que los necesitan.
const A=osarioPerformances, V=(x=0,y=0,z=0)=>new THREE.Vector3(x,y,z), D=Math.PI/180;
const groups=new Map(Group.all.map(group=>[group.name,group]));
const nodes=new Map();
for(const [name,g] of groups){
  nodes.set(name,{parent:g.mesh.parent,group:g,restP:g.mesh.fix_position.clone(),
    restR:g.mesh.fix_rotation.clone(),local:new THREE.Matrix4(),world:new THREE.Matrix4()});
}
const owners=new Map([...nodes.values()].map(n=>[n.group.mesh,n]));
const Z=[0,0,0], U=[1,1,1];
const aliases={
  p:['pelvis','position'],v:['pelvis','rotation'],b:['body','rotation'],s:['spine_upper','rotation'],
  h:['head','rotation'],j:['jaw','rotation'],a:['bone_upper_arm','rotation'],e:['bone_forearm','rotation'],
  w:['bone_hand','rotation'],f:['bone_fingers','rotation'],m:['mass_shoulder','rotation'],
  u:['dragging_upper','rotation'],mid:['dragging_middle','rotation'],lo:['dragging_lower','rotation'],
  end:['dragging_end','rotation'],k:['chest_core','scale'],coreP:['chest_core','position'],
  chip:['debris_chips','position']
};
const massNames=['dragging_upper','dragging_middle','dragging_lower','dragging_end','debris_roots','debris_chips','mass_shoulder'];
const boxes=Cube.all.map(c=>{
  c.mesh.updateMatrix();c.mesh.geometry.computeBoundingBox();
  const box=c.mesh.geometry.boundingBox, corners=[];
  for(const x of [box.min.x,box.max.x])for(const y of [box.min.y,box.max.y])for(const z of [box.min.z,box.max.z]){
    corners.push(V(x,y,z).applyMatrix4(c.mesh.matrix));
  }
  return {name:c.name,parent:c.parent.name,corners};
});
function minimum(selection,axis='y'){
  let min=Infinity, element;
  for(const box of selection)for(const point of box.corners){
    const y=point.clone().applyMatrix4(nodes.get(box.parent).world)[axis];
    if(y<min){min=y;element=box.name;}
  }
  return {min,element};
}
const byBone=new Map([...nodes.keys()].map(name=>[name,boxes.filter(c=>c.parent===name)]));
const mass=boxes.filter(c=>massNames.includes(c.parent));
const chain=['dragging_upper','dragging_middle','dragging_lower'];
const chainPivots=[...chain,'dragging_end'];
const segmentLengths=chainPivots.slice(1).map(b=>nodes.get(b).restP.length());
const rotatedVector=(v,q)=>v.clone().applyQuaternion(q);
let pose={};
const channel=(bone,kind,fallback)=>pose[bone]?.[kind]??fallback;
function set(bone,kind,value){(pose[bone]??={})[kind]=Array.isArray(value)?value:value.toArray();}
function world(name){return nodes.get(name).world;}
function worldP(name){return V().setFromMatrixPosition(world(name));}
function worldQ(name){return new THREE.Quaternion().setFromRotationMatrix(world(name));}
function evaluate(){
  for(const [name,n] of nodes){
    const r=channel(name,'rotation',Z),p=channel(name,'position',Z),s=channel(name,'scale',U);
    const e=new THREE.Euler(n.restR.x+r[0]*D,n.restR.y+r[1]*D,n.restR.z+r[2]*D,'ZYX');
    n.local.compose(n.restP.clone().add(V(...p)),new THREE.Quaternion().setFromEuler(e),V(...s));
    const parent=owners.get(n.parent);
    if(parent)n.world.multiplyMatrices(parent.world,n.local);else n.world.copy(n.local);
  }
}
function orient(bone,q){
  const n=nodes.get(bone),parent=owners.get(n.parent);
  const local=parent?worldQ(parent.group.name).invert().multiply(q):q;
  const e=new THREE.Euler().setFromQuaternion(local,'ZYX');
  set(bone,'rotation',[(e.x-n.restR.x)/D,(e.y-n.restR.y)/D,(e.z-n.restR.z)/D]);
  evaluate();
}
function aim(bone,from,to){
  if(from.lengthSq()<1e-8||to.lengthSq()<1e-8)return;
  const correction=new THREE.Quaternion().setFromUnitVectors(from.normalize(),to.normalize());
  orient(bone,correction.multiply(worldQ(bone)));
}
function move(bone,change){
  const n=nodes.get(bone),parent=owners.get(n.parent);
  if(parent)change.applyQuaternion(worldQ(parent.group.name).invert());
  const current=V(...channel(bone,'position',Z));
  set(bone,'position',current.add(change));evaluate();
}
evaluate();
const restFoot={};
for(const side of ['left','right']){
  const leg='foot_'+side;
  restFoot[side]={p:worldP(leg),q:worldQ(leg),ground:minimum(byBone.get(leg)).min};
}
const restMass={p:worldP('dragging_end'),q:worldQ('dragging_end'),ground:minimum(byBone.get('dragging_end')).min};
const restChain=chainPivots.map(b=>({position:worldP(b),rotation:worldQ(b)}));
const deathPose={massX:4.8,massZ:-1,bend:.05,feetZ:.55,bodyYaw:8,armX:-4.7};
const lerp=(a,b,t)=>a+(b-a)*t;
const smooth=t=>t*t*(3-2*t);
const clamp=(n,a,b)=>Math.max(a,Math.min(b,n));
const circle=(n,length)=>((n%length)+length)%length;
function interpolate(keys,t){
  if(t<=keys[0][0])return keys[0][1].slice();
  if(t>=keys.at(-1)[0])return keys.at(-1)[1].slice();
  const i=keys.findIndex(k=>k[0]>t), [ta,a]=keys[i-1],[tb,b]=keys[i],dt=tb-ta,u=(t-ta)/dt;
  const before=keys[i-2],after=keys[i+1];
  return a.map((v,j)=>{
    const slope=(b[j]-v)/dt;
    const previous=before?(v-before[1][j])/(ta-before[0]):0;
    const next=after?(after[1][j]-b[j])/(after[0]-tb):0;
    const tangent=(x,y)=>x*y>0?2*x*y/(x+y):0;
    return (2*u**3-3*u*u+1)*v +(u**3-2*u*u+u)*dt*tangent(previous,slope)
      +(-2*u**3+3*u*u)*b[j]+(u**3-u*u)*dt*tangent(slope,next);
  });
}
function tracks(data){
  const channels={};
  for(const [key,[bone,kind]] of Object.entries(aliases)){
    const neutral=kind==='scale'?U:Z, keys=[];
    for(const [t,p] of data.poses)if(p[key]!==undefined){
      const v=p[key];keys.push([t,Array.isArray(v)?v:(kind==='scale'?[v,v,v]:[v,0,0])]);
    }
    if(!keys.length)continue;
    if(keys[0][0]>0)keys.unshift([0,neutral]);
    if(keys.at(-1)[0]<data.length)keys.push([data.length,data.loop==='loop'?keys[0][1]:data.loop==='hold'?keys.at(-1)[1]:neutral]);
    (channels[bone]??={})[kind]=keys;
  }
  return channels;
}
function performance(data,t,curves){
  pose={};
  for(const [bone,channels] of Object.entries(curves))for(const [kind,keys] of Object.entries(channels)){
    set(bone,kind,interpolate(keys,t));
  }
  function delayed(source,lag,amount){
    const time=data.loop==='loop'?circle(t-lag,data.length):Math.max(0,t-lag);
    const recovery=data.loop==='once'?smooth(clamp((data.length-t)/.18,0,1)):1;
    return (curves[source]?.rotation?interpolate(curves[source].rotation,time):Z).map(v=>v*amount*recovery);
  }
  if(!pose.dragging_middle){
    const upper=delayed('dragging_upper',.12,.43),lower=delayed('dragging_upper',.24,.43*1.25);
    set('dragging_middle','rotation',upper);
    set('dragging_lower','rotation',lower);
  }
  const lagged=delayed('dragging_upper',.24,1.25);
  set('debris_roots','rotation',[lagged[0]*.10,lagged[1]*.20,clamp(lagged[1]*.85+lagged[2]*1.25,-25,25)]);
  if(!pose.debris_chips){
    const wobble=Math.abs(lagged[1])*.036;
    set('debris_chips','position',[0,wobble,0]);
  }
  if(!pose.bone_hand) set('bone_hand','rotation',delayed('bone_forearm',.12,-.23));
  if(!pose.bone_fingers) set('bone_fingers','rotation',delayed('bone_forearm',.24,.30));
  set('bone_thumb','rotation',channel('bone_fingers','rotation',Z).map(v=>v*.62));
  if(data.feet?.death){
    const r=channel('body','rotation',Z).slice();r[1]+=deathPose.bodyYaw*smooth(clamp((t-.25)/.7,0,1));
    set('body','rotation',r);
  }
  evaluate();
}
function footPlan(data,t,side){
  const isLeft=side==='left', rest=restFoot[side], p=rest.p.clone();
  let roll=0;
  if(data.feet?.stride){
    const phase=circle(t/data.length+(isLeft?0:.5),1),{duty,stride,lift}=data.feet;
    const asym=isLeft?1:.91;
    if(phase<=duty){
      p.z+=lerp(-stride,stride,phase/duty)*asym;
      roll=-8*smooth(clamp((phase/duty-.75)/.25,0,1));
    }else{
      const u=(phase-duty)/(1-duty);
      p.z+=lerp(stride,-stride,smooth(u))*asym;
      p.y+=lift*Math.sin(Math.PI*u)**1.45*asym;
      roll=lerp(-8,0,smooth(u))+15*Math.sin(2*Math.PI*u);
    }
  }else if(data.feet?.dash){
    const steps=isLeft?[[0,0,0],[.7,0,0],[.79,-2.30,1.8],[.875,-3,0],[.98,1.1,1.35],[1.05,.9,0],[1.35,0,0]]
      :[[0,0,0],[.7,0,0],[.79,1.9,0],[.875,-2.8,1.55],[.98,-3.15,0],[1.05,-1.75,0],[1.35,0,0]];
    const [z,y]=interpolate(steps.map(([a,b,c])=>[a,[b,c]]),t);
    p.z+=z;p.y+=y;roll=-8*y;
  }else if(data.feet?.death){
    const knee=smooth(clamp((t-.19)/.76,0,1));
    p.z+=(deathPose.feetZ-(isLeft?0:.5))*knee;
    p.x+=(isLeft?.31:-.29)*knee;
    roll=-63*knee;
  }
  return {p,roll,lift:Math.max(0,p.y-rest.p.y)};
}
function legs(data,t){
  for(const side of ['left','right']){
    const thigh='thigh_'+side,shin='shin_'+side,foot='foot_'+side;
    const info=footPlan(data,t,side),rest=restFoot[side], hip=worldP(thigh);
    const footQ=new THREE.Quaternion().setFromAxisAngle(V(1),info.roll*D).multiply(rest.q);
    // La planta se mide con la orientación de este fotograma; el tobillo no es el suelo.
    const toeMin=Math.min(...byBone.get(foot).flatMap(box=>box.corners.map(p=>rotatedVector(p,footQ).y)));
    info.p.y=info.lift-toeMin;
    const thighLength=nodes.get(shin).restP.length(),shinLength=nodes.get(foot).restP.length();
    const axis=info.p.clone().sub(hip), distance=clamp(axis.length(),.25,thighLength+shinLength-.002);
    axis.normalize();
    const pole=V(0,0,-1);pole.addScaledVector(axis,-pole.dot(axis)).normalize();
    const length=(thighLength**2-shinLength**2+distance**2)/(2*distance);
    const knee=hip.clone().addScaledVector(axis,length).addScaledVector(pole,Math.sqrt(Math.max(0,thighLength**2-length**2)));
    const ankle=hip.clone().addScaledVector(axis,distance);
    aim(thigh,worldP(shin).sub(hip),knee.clone().sub(hip));
    aim(shin,worldP(foot).sub(worldP(shin)),ankle.sub(knee));
    orient(foot,footQ);
  }
}
function massTarget(name,data,t,height){
  const current=worldP('dragging_end'), rest=restMass.p;
  if(name==='ground_slam'){
    return V(...interpolate([[0,rest.toArray()],[.30,rest.toArray()],[.52,[12,10,13]],[.72,[12,23,11]],
      [.90,[11,32,3]],[1.02,[10,23,-14]],[1.15,[7,rest.y,-20]],[1.25,[8,rest.y+.7,-19]],
      [1.44,[13,4,-10]],[1.61,[16,2,0]],[1.8,rest.toArray()]],t));
  }
  if(name==='death'){
    const u=smooth(t/data.length);
    return V(lerp(rest.x,deathPose.massX,u),rest.y-restMass.ground+height,lerp(rest.z,deathPose.massZ,u));
  }
  const intent=name==='heavy_swipe'||name==='claw_swipe'?.23:.14;
  return V(lerp(rest.x,current.x,intent),rest.y-restMass.ground+height,lerp(rest.z,current.z,intent));
}
function stretchMass(name,data,t){
  const height=interpolate(data.drag.map(([a,b])=>[a,[b]]),t)[0];
  const airBlend=name==='ground_slam'?smooth(clamp((height-.15)/10,0,1)):0;
  const target=massTarget(name,data,t,height);
  const shoulder=worldP('dragging_upper'), delta=target.clone().sub(shoulder);
  const maxReach=segmentLengths.reduce((s,n)=>s+n,0)-.13;
  if(delta.length()>maxReach)target.copy(shoulder).add(delta.setLength(maxReach));
  if(name==='ground_slam'||name==='death'){
    // El plano lateral de plegado mantiene los segmentos fuera del cuerpo.
    const folded=name==='death'?smooth(clamp((t-.1)/.4,0,1)):0, swing=name==='ground_slam'?Math.sin(Math.PI*t/data.length)**2:0;
    const guideWeight=Math.max(folded,Math.sqrt(swing));
    const points=restChain.map(b=>b.position.clone().add(shoulder.clone().sub(restChain[0].position)));
    for(let i=1;i<=2;i++){
      const f=segmentLengths.slice(0,i).reduce((s,n)=>s+n,0)/maxReach;
      const guide=shoulder.clone().lerp(target,f);
      guide.x+=Math.sin(Math.PI*f)*(2+deathPose.bend*folded+4*swing);
      guide.y+=Math.sin(Math.PI*f)*(3*folded+5*swing);
      points[i].lerp(guide,guideWeight);
    }
    for(let pass=0;pass<17;pass++){
      points[3].copy(target);
      for(let i=2;i>=0;i--)points[i].sub(points[i+1]).setLength(segmentLengths[i]).add(points[i+1]);
      points[0].copy(shoulder);
      for(let i=1;i<4;i++){
        if(i<3)points[i].y=Math.max(points[i].y,i===1?4.1:3.6);
        points[i].sub(points[i-1]).setLength(segmentLengths[i-1]).add(points[i-1]);
      }
    }
    for(let i=0;i<3;i++){
      const pivot=worldP(chain[i]),direction=points[i+1].clone().sub(pivot).normalize();
      const restAxis=nodes.get(chainPivots[i+1]).restP.clone().normalize();
      const sourceSide=V(1,0,0).addScaledVector(restAxis,-restAxis.x).normalize();
      const restSide=sourceSide.clone().applyQuaternion(restChain[i].rotation);
      const guideSide=restSide.clone().addScaledVector(direction,-restSide.dot(direction)).normalize();
      const source=new THREE.Matrix4().makeBasis(sourceSide,restAxis,sourceSide.clone().cross(restAxis));
      const destination=new THREE.Matrix4().makeBasis(guideSide,direction,guideSide.clone().cross(direction));
      const q=new THREE.Quaternion().setFromRotationMatrix(destination.multiply(source.invert()));
      orient(chain[i],q);
    }
  }else{
    for(let pass=0;pass<6;pass++){
      for(const bone of chain.slice().reverse()){
        const pivot=worldP(bone),current=worldP('dragging_end');
        const from=current.sub(pivot),to=target.clone().sub(pivot);
        const corrected=from.clone().normalize().lerp(to.clone().normalize(),.73);
        aim(bone,from,corrected);
      }
    }
  }
  const original=(name==='ground_slam')?worldQ('dragging_lower').multiply(new THREE.Quaternion().setFromEuler(nodes.get('dragging_end').restR)):worldQ('dragging_end');
  const q=restMass.q.clone().slerp(original,airBlend*.68);
  orient('dragging_end',q);
  if(airBlend<.8){
    const min=minimum(byBone.get('dragging_end')).min;
    move('dragging_end',V(0,clamp((height-min)*(1-airBlend),-1.25,1.25),0));
  }
}
const armPlans={
  claw_swipe:{weight:[[0,0],[.14,0],[.31,1],[.65,1],[.90,0]],targets:[
    [.14,[-9,12,1]],[.35,[-12,16,1]],[.43,[-12,14,-7]],[.50,[-3.9,14,-16.2]],
    [.64,[1.6,12,-13.5]],[.76,[-4,10,-8]],[.90,[-7,10,-3]]]},
  heavy_swipe:{weight:[[0,0],[.22,0],[.43,1],[1.17,1],[1.4,0]],targets:[
    [.22,[-11,21,-5]],[.43,[-10,28,-4]],[.8,[-7,33,-1]],[.87,[-12,25,-8]],
    [.95,[-5,10,-17.2]],[1.10,[1.8,9,-13.5]],[1.22,[-3,9,-8]],[1.4,[-7,10,-3]]]},
  lunge:{weight:[[0,0],[.12,0],[.37,1],[1.02,1],[1.35,0]],targets:[
    [.12,[-8,9,-3]],[.37,[-10,10,-1]],[.70,[-10,10,-.6]],[.80,[-9,13,-14]],[.875,[-5.5,13.5,-18]],[.98,[-4.5,10,-16]],
    [1.05,[-6,10,-12]],[1.22,[-8,9,-5]]]},
  death:{weight:[[.24,0],[.40,0],[.69,1],[.95,1]],targets:[
    [.4,[-7,9,-8]],[.67,[-6,4,-11]],[.83,[-5,2,-12]],[.95,[-5,1.8,-12]]]}
};
function guideArm(name,t){
  const plan=armPlans[name];if(!plan)return;
  const weight=interpolate(plan.weight.map(([x,y])=>[x,[y]]),t)[0];
  if(weight<1e-5)return;
  const upper='bone_upper_arm',lower='bone_forearm',hand='bone_hand';
  const shoulder=worldP(upper),originalUpper=worldQ(upper),originalLower=worldQ(lower);
  const wrist=V(...interpolate(plan.targets,t));
  if(name==='death')wrist.x+=(deathPose.armX+5)*smooth(clamp((t-.55)/.4,0,1));
  const l1=nodes.get(lower).restP.length(),l2=nodes.get(hand).restP.length();
  const axis=wrist.clone().sub(shoulder),length=clamp(axis.length(),2.6,l1+l2-.30);
  axis.normalize();wrist.copy(shoulder).addScaledVector(axis,length);
  const outside=name==='death'?V(.13,-.98,.12):V(-.45,-.52,.72);
  const bend=outside.clone().addScaledVector(axis,-outside.dot(axis));
  if(bend.lengthSq()<.02) bend.add(V(0,0,1).addScaledVector(axis,-axis.z));
  bend.normalize();
  const along=(l1*l1-l2*l2+length*length)/(2*length);
  const elbow=shoulder.clone().addScaledVector(axis,along).addScaledVector(bend,Math.sqrt(l1*l1-along*along));
  const plane=axis.clone().cross(bend).normalize();
  function armOrientation(child,direction){
    const localAxis=nodes.get(child).restP.clone().normalize(),localWidth=V(1,0,0).addScaledVector(localAxis,-localAxis.x).normalize();
    const source=new THREE.Matrix4().makeBasis(localWidth,localAxis,localWidth.clone().cross(localAxis));
    const dest=new THREE.Matrix4().makeBasis(plane,direction,plane.clone().cross(direction));
    return new THREE.Quaternion().setFromRotationMatrix(dest.multiply(source.invert()));
  }
  orient(upper,originalUpper.slerp(armOrientation(lower,elbow.clone().sub(shoulder).normalize()),weight));
  orient(lower,originalLower.slerp(armOrientation(hand,wrist.clone().sub(elbow).normalize()),weight));
}
const baked={}, reports={};
for(const [name,data] of Object.entries(A)){
  const curves=tracks(data), steps=Math.round(data.length*80), frames=[];
  const legNames=['thigh_left','shin_left','foot_left','thigh_right','shin_right','foot_right'];
  const adjusted=new Set([...legNames,...chain,'dragging_end','debris_roots','debris_chips','bone_hand','bone_fingers','bone_thumb','root','pelvis']);
  for(let i=0;i<=steps;i++){
    const t=i===steps?data.length:i*data.length/steps;
    performance(data,t,curves);
    if(i===0&&data.loop!=='loop'||i===steps&&data.loop==='once'||name==='idle'&&(i===0||i===steps)){
      pose={};evaluate();frames.push({t,pose});continue;
    }
    legs(data,t);
    if(name==='death'){
      const knees=minimum([...byBone.get('shin_left'),...byBone.get('shin_right')]).min;
      if(knees<-1e-6){move('root',V(0,-knees,0));legs(data,t);}
    }
    stretchMass(name,data,t);guideArm(name,t);
    let lower=minimum(boxes).min;
    if(lower<-1e-6){
      // Compensación global solo cuando una cadena no puede absorber el resto sin cruzar el suelo.
      move('root',V(0,-lower,0));
      legs(data,t);stretchMass(name,data,t);
      lower=minimum(boxes).min;
      if(lower<-1e-6)move('root',V(0,-lower,0));
    }
    if(name==='ground_slam'&&Math.abs(t-1.15)<.06){
      const contact=smooth(1-Math.abs(t-1.15)/.06),tip=minimum(byBone.get('dragging_end')).min;
      if(tip>0)move('dragging_end',V(0,-Math.min(.55,tip)*contact,0));
    }
    frames.push({t,pose});
  }
  if(data.loop==='loop') frames.at(-1).pose=frames[0].pose;
  const packed={};
  for(const bone of new Set(frames.flatMap(f=>Object.keys(f.pose)))){
    const channels=new Set(frames.flatMap(f=>Object.keys(f.pose[bone]??{})));
    for(const kind of channels){
      const fallback=kind==='scale'?U:Z;
      const values=frames.map(({t,pose})=>[t,(pose[bone]?.[kind]??fallback).slice()]);
      if(kind==='rotation'){
        const base=nodes.get(bone).restR.toArray().slice(0,3).map(v=>v/D);
        for(let i=1;i<values.length;i++){
          const current=values[i][1],previous=values[i-1][1];
          const options=[current,[current[0]+180,180-current[1]-2*base[1],current[2]+180]]
            .map(a=>a.map((n,j)=>n-360*Math.round((n-previous[j])/360)));
          const score=a=>a.reduce((s,n,j)=>s+(n-previous[j])**2,0);
          values[i][1]=score(options[1])<score(options[0])?options[1]:options[0];
        }
      }
      if(values.every(([,v])=>v.every((n,i)=>Math.abs(n-fallback[i])<1e-5)))continue;
      (packed[bone]??={})[kind]={keys:values,correction:adjusted.has(bone)};
    }
  }
  reports[name]={samples:frames.length};
  for(const item of [{id:'ground',set:boxes},{id:'mass_tip',set:byBone.get('dragging_end')},{id:'left_foot',set:byBone.get('foot_left')},{id:'right_foot',set:byBone.get('foot_right')}]) {
    const mins=frames.map(f=>{pose=f.pose;evaluate();return {t:f.t,...minimum(item.set)};});
    reports[name][item.id]={min:mins.reduce((a,b)=>a.min<b.min?a:b),max:mins.reduce((a,b)=>a.min>b.min?a:b)};
  }
  baked[name]={length:data.length,loop:data.loop,bones:packed};
}
globalThis.osarioBaked=baked;
globalThis.osarioBakeReport=reports;
return reports;
