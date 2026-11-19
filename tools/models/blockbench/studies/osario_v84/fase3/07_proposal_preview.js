// Comparación visual de una hipótesis; los cubos y huesos nativos permanecen intactos.
// La UV de las dos piezas estrechadas es provisional: debe rehacerse si se aprueba el ajuste.
osarioF3.previewProposal=function(on){
 if(osarioF3.proposalHelpers){for(const m of osarioF3.proposalHelpers)m.removeFromParent();for(const [m,v]of osarioF3.proposalVisibility)m.visible=v;}
 osarioF3.proposalHelpers=[];osarioF3.proposalVisibility=[];
 if(on){
  Project.model_3d.updateMatrixWorld(true);
  const inverse=Project.model_3d.matrixWorld.clone().invert(),q=Group.all.find(g=>g.name==='chest').mesh.getWorldQuaternion(new THREE.Quaternion());
  const shift=new THREE.Vector3(0,-.5,0).applyQuaternion(q),widths={sternum:1/3,manubrium:.5};
  for(const c of Cube.all){
   const m=new THREE.Mesh(c.mesh.geometry,c.mesh.material);m.name='OSARIO_PROPOSAL_REVIEW_ONLY';m.matrixAutoUpdate=false;
   const matrix=c.mesh.matrixWorld.clone();
   if(widths[c.name])matrix.multiply(new THREE.Matrix4().makeScale(widths[c.name],1,1));
   if(c.parent.name==='chest_core')matrix.premultiply(new THREE.Matrix4().makeTranslation(shift.x,shift.y,shift.z));
   m.matrix.copy(inverse.clone().multiply(matrix));Project.model_3d.add(m);
   osarioF3.proposalHelpers.push(m);osarioF3.proposalVisibility.push([c.mesh,c.mesh.visible]);c.mesh.visible=false;
  }
 }
 Project.model_3d.updateMatrixWorld(true);return {hypothesis_preview:on,native_geometry_changed:false,helpers:osarioF3.proposalHelpers.length};
};
return {ready:true};
