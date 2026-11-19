const {put}=osarioF2;
// Brazo delgado: elementos articulares, radio y cúbito diferenciados.
put('bone_clavicle','bone_shoulder',[1.1,0,-.1],[3,1,1],[0,-6,7],'bone','Clavícula del lado delgado');
put('bone_scapula','bone_shoulder',[.7,-.5,1],[2,2,1],[3,8,-4],'bone','Escápula posterior');
put('humerus','bone_upper_arm',[0,-2.75,0],[1.5,5.5,1.5],[0,0,0],'bone','Diáfisis humeral');
put('humeral_head','bone_upper_arm',[0,-.25,0],[2,1.5,2],[0,5,-5],'bone','Cabeza humeral');
put('radius','bone_forearm',[-.7,-2.75,-.1],[1,5.5,1],[0,0,1],'bone','Radio del antebrazo');
put('ulna','bone_forearm',[.7,-2.75,.1],[1,5.5,1],[0,0,-1],'bone','Cúbito independiente');
put('olecranon','bone_forearm',[0,.2,.1],[2,1.5,2],[0,0,3],'bone','Articulación del codo');
put('palm','bone_hand',[0,-.7,0],[3,1.5,1],[0,0,0],'bone','Palma de cuatro dedos');
put('carpals','bone_hand',[0,.2,0],[2,1,1],[0,4,0],'bone','Articulación de la muñeca');

// Tres dedos y un pulgar, con falanges inclinadas de forma individual.
put('index_proximal','bone_fingers',[1.05,-.65,-.1],[.5,1.5,.5],[-10,0,5],'bone','Primera falange del índice');
put('index_distal','bone_fingers',[1.1,-1.85,.15],[.5,1,.5],[-30,0,7],'bone','Extremo doblado del índice');
put('middle_proximal','bone_fingers',[0,-.8,0],[1,2,1],[-8,0,0],'bone','Dedo medio más largo');
put('middle_distal','bone_fingers',[0,-2.2,.35],[.5,1,.5],[-34,0,0],'bone','Extremo erosionado del dedo medio');
put('ring_proximal','bone_fingers',[-1,-.65,0],[.5,1.5,.5],[-13,0,-5],'bone','Primera falange del anular');
put('ring_distal','bone_fingers',[-1.05,-1.8,.35],[.5,1,.5],[-38,0,-8],'bone','Falange irregular del anular');
put('thumb_proximal','bone_thumb',[0,-.5,0],[.5,1,.5],[-12,0,0],'bone','Pulgar oponible con hueso propio');
put('thumb_distal','bone_thumb',[0,-1.3,.2],[.5,1,.5],[-35,0,0],'bone','Extremo curvado del pulgar');

// Pierna izquierda más corta, con apoyo y flexión distintos de la derecha.
put('femur_left_short','thigh_left',[0,-2.75,0],[2,5.5,2],[0,0,0],'bone','Fémur izquierdo, 0,5 u más corto');
put('trochanter_left','thigh_left',[.2,-.15,.1],[2,1.5,2],[0,-6,7],'bone','Cabeza femoral izquierda');
put('femur_right','thigh_right',[0,-3,0],[2,6,2],[0,0,0],'bone','Fémur derecho de mayor apoyo');
put('trochanter_right','thigh_right',[-.2,-.1,.1],[2,1.5,2],[0,8,-5],'bone','Cabeza femoral derecha');
put('tibia_left','shin_left',[0,-3,0],[2,6,2],[0,0,0],'bone','Tibia izquierda');
put('fibula_left','shin_left',[1.6,-3,.3],[1,5,1],[0,0,-2],'bone','Peroné separado de la tibia');
put('patella_left','shin_left',[0,-.1,-.6],[2.5,2,2.5],[0,-5,5],'bone','Rótula izquierda erosionada');
put('tibia_right','shin_right',[0,-3,0],[2,6,2],[0,0,0],'bone','Tibia derecha');
put('fibula_right','shin_right',[-1.6,-3,.3],[1,5,1],[0,0,3],'bone','Peroné derecho independiente');
put('patella_right','shin_right',[0,-.15,-.6],[2.5,2,2.5],[0,7,-4],'bone','Rótula de apoyo derecho');
put('heel_left','foot_left',[0,-.5,.75],[2.5,1.5,2.5],[0,0,0],'bone','Talón izquierdo, suela y=0');
put('metatarsal_left','foot_left',[0,-.75,-1],[2.5,1,3],[0,0,0],'bone','Metatarso izquierdo');
put('toe_left_inner','foot_left',[-1,-.75,-2.5],[.5,1,1.5],[0,-4,0],'bone','Falange interior izquierda');
put('toe_left_middle','foot_left',[0,-.75,-2.7],[1,1,1.5],[0,0,0],'bone','Falange central izquierda');
put('toe_left_outer','foot_left',[1,-.75,-2.4],[.5,1,1.5],[0,5,0],'bone','Falange exterior izquierda');
put('heel_right','foot_right',[0,-.5,.75],[2.5,1.5,2.5],[0,0,0],'bone','Talón derecho, suela y=0');
put('metatarsal_right','foot_right',[0,-.75,-1],[2.5,1,3],[0,0,0],'bone','Metatarso derecho');
put('toe_right_inner','foot_right',[1,-.75,-2.5],[.5,1,1.5],[0,4,0],'bone','Falange interior derecha');
put('toe_right_middle','foot_right',[0,-.75,-2.65],[1,1,1.5],[0,0,0],'bone','Falange central derecha');
put('toe_right_outer','foot_right',[-1,-.75,-2.35],[.5,1,1.5],[0,-7,0],'bone','Falange exterior derecha');
Canvas.updateAll();Project.model_3d.updateMatrixWorld(true);
return {stage:'extremidades',cubes:Cube.all.length,bones:Group.all.length};
