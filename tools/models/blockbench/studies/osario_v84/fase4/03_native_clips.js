// Escribe las poses resueltas como animaciones nativas en el editable GeckoLib.
const prefix='animation.osario_quebrado.';
const managedNames=Object.keys(osarioBaked).map(name=>prefix+name);
if(Animation.all.some(a=>!managedNames.includes(a.name)))throw Error('Hay animaciones ajenas al trabajo de Fase 4.');
for(const animation of Animation.all.slice())Animator.animations.remove(animation);
function selectKeys(keys,channel,correction){
  const tolerance=channel==='rotation'?(correction?.095:.22):channel==='position'?.008:.002;
  const gap=correction?.075:.12;
  // Mantiene los extremos y subdivide donde el intervalo pierde curvatura o precisión.
  const selection=new Set([0,keys.length-1]);
  function divide(first,last){
    if(last-first<2)return;
    const [a,start]=keys[first],[b,end]=keys[last],duration=b-a;
    let worst=0,index=-1;
    for(let i=first+1;i<last;i++){
      const [t,value]=keys[i],u=(t-a)/duration;
      const error=Math.max(...value.map((v,j)=>Math.abs(v-start[j]-(end[j]-start[j])*u)));
      if(error>worst){worst=error;index=i;}
    }
    if(worst>tolerance||duration>gap){
      if(worst<=tolerance)index=Math.round((first+last)/2);
      selection.add(index);divide(first,index);divide(index,last);
    }
  }
  divide(0,keys.length-1);
  return [...selection].sort((a,b)=>a-b).map(i=>keys[i]);
}
const result={};
for(const [name,data] of Object.entries(osarioBaked)){
  const animation=new Animation({name:prefix+name,length:data.length,loop:data.loop,snapping:80,override:false}).add(false);
  let count=0;
  for(const [bone,channels] of Object.entries(data.bones)){
    const group=Group.all.find(g=>g.name===bone),animator=animation.getBoneAnimator(group);
    for(const [channel,{keys,correction}] of Object.entries(channels)){
      for(const [time,delta] of selectKeys(keys,channel,correction)){
        const xyz=delta.map(v=>Math.round(v*1e5)/1e5);
        animator.addKeyframe({channel,time:Math.round(time*1e6)/1e6,
          interpolation:'catmullrom',uniform:channel==='scale',data_points:[{x:xyz[0],y:xyz[1],z:xyz[2]}]},guid());
        count++;
      }
    }
  }
  animation.length=data.length;
  result[name]={animation:animation.name,length:animation.length,loop:animation.loop,keyframes:count};
}
Project.saved=false;
return result;
