const {put}=osarioF2;
// Anclaje: tres volúmenes de tierra entrelazados, clavícula expuesta y dos raíces de sujeción.
put('shoulder_earth_core','mass_shoulder',[-.3,-.75,3.5],[4,3.5,4.5],[0,5,-7],'earth','Volumen portante del hombro');
put('shoulder_outer_clod','mass_shoulder',[2,-.6,3.5],[2.5,3,4],[8,-12,16],'earth','Rotura exterior de la silueta');
put('shoulder_rear_clod','mass_shoulder',[-.2,.1,5],[4.5,2.5,2.5],[-10,8,-7],'earth','Cresta posterior irregular');
put('mass_clavicle_exposed','mass_shoulder',[-1.8,-.1,1.5],[3,1,1],[0,-8,12],'bone','Clavícula parcialmente enterrada');
put('shoulder_root_inner','mass_shoulder',[-1,-1,1.1],[1,3,1],[-12,8,-16],'root','Raíz de unión interior');
put('shoulder_root_outer','mass_shoulder',[1.8,-1.5,1.7],[1,3,1],[8,-5,23],'root','Raíz de sujeción exterior');

// Tramo superior: sección estratificada, fémur incrustado reconocible y raíces que cruzan las junturas.
put('upper_earth_spine','dragging_upper',[0,-4.25,.4],[4,8,4.5],[0,4,0],'earth','Columna compacta de tierra');
put('upper_front_slab','dragging_upper',[1.75,-3.5,-.7],[2.5,5,3],[0,-8,12],'earth','Estrato frontal descentrado');
put('upper_back_ridge','dragging_upper',[-.5,-4.25,2.75],[3.5,6.5,2.5],[-4,-10,-9],'earth','Cresta posterior oblicua');
put('embedded_femur_shaft','dragging_upper',[.75,-4,-2.35],[1,7,1],[7,0,-8],'bone','Fémur ajeno incrustado');
put('embedded_femur_head','dragging_upper',[1.2,-.8,-1.95],[1.5,1,1.5],[7,0,-8],'bone','Extremo proximal del fémur');
put('embedded_femur_condyle','dragging_upper',[.3,-7.2,-2.75],[1.5,1,1.5],[7,0,-8],'bone','Cóndilos del fémur incrustado');
put('upper_root_a','dragging_upper',[-1.75,-3,-2.1],[1,5,1],[12,0,-15],'root','Raíz longitudinal frontal');
put('upper_root_b','dragging_upper',[2,-5.7,0],[1,4,1],[-8,7,18],'root','Raíz oblicua exterior');
put('upper_moss_clump','dragging_upper',[1,-.3,-.6],[2,.5,2],[0,12,0],'moss','Musgo escaso en una junta húmeda');
put('upper_earth_chip','dragging_upper',[-1.9,-6,-.9],[2,2,2],[0,18,-16],'earth','Casco desprendido que rompe la caja');

// Tramo medio: dos costillas incrustadas y un fragmento, sin otro cráneo.
put('middle_earth_core','dragging_middle',[0,-3.7,.25],[4.5,8,5],[0,-4,0],'earth','Núcleo compacto del tramo medio');
put('middle_front_chunk','dragging_middle',[2,-4,-1.25],[3,5,3],[-3,8,-11],'earth','Grumo frontal ladeado');
put('middle_rear_chunk','dragging_middle',[-1,-4,2],[4,5.5,3.5],[8,-7,6],'earth','Capa posterior erosionada');
put('embedded_rib_a','dragging_middle',[1.5,-3,-2.5],[1,5,1],[4,-12,25],'bone','Costilla ajena inclinada');
put('embedded_rib_b','dragging_middle',[-1.25,-4.5,-2.35],[1,5,1],[-7,10,-20],'bone','Costilla oblicua con punta expuesta');
put('middle_bone_splinter','dragging_middle',[3,-1.8,.6],[1,3,1],[12,-8,35],'bone','Astilla exterior de hueso');
put('middle_root_c','dragging_middle',[-2,-4,-2.4],[1,6,1],[8,12,-10],'root','Raíz que abraza el tramo medio');
put('middle_mud_lump','dragging_middle',[-2,-5.75,.5],[3,3,3],[0,18,13],'earth','Grumo de tierra húmeda irregular');
put('middle_moss_patch','dragging_middle',[2,-.9,-1],[2,.5,2.5],[0,-8,0],'moss','Segundo y último parche de musgo');

// Tramo inferior ensanchado: fragmento óseo sin rasgos faciales y cadena de vértebras enterrada.
put('lower_earth_core','dragging_lower',[0,-2.2,.1],[5.5,6,5],[0,-4,0],'earth','Volumen portante inferior');
put('lower_outer_slab','dragging_lower',[2,-2.3,-.9],[3,4,3.5],[0,-9,12],'earth','Placa lateral desalineada');
put('lower_rear_clod','dragging_lower',[-1,-3.6,1.8],[4,3,4],[10,6,-6],'earth','Base posterior quebrada');
put('lower_ossified_fragment','dragging_lower',[.9,-1.5,-2.7],[2.5,2.5,2],[0,-17,22],'bone','Fragmento sin cuencas ni identidad de cráneo');
put('embedded_vertebra_a','dragging_lower',[-1.2,-.5,-2.6],[1.5,1.5,1.5],[0,8,-9],'bone','Vértebra incrustada superior');
put('embedded_vertebra_b','dragging_lower',[-.8,-2,-2.75],[1.5,1.5,1.5],[4,-7,12],'bone','Vértebra incrustada media');
put('embedded_vertebra_c','dragging_lower',[-1.1,-3.5,-2.65],[1.5,1.5,1.5],[-6,10,-5],'bone','Vértebra incrustada inferior');
put('lower_root_d','dragging_lower',[-2.6,-2.5,-2.3],[1,5,1],[-10,9,17],'root','Raíz gruesa del arrastre inferior');
put('lower_inner_clump','dragging_lower',[-2.6,-3.5,1.5],[3,2,3],[0,-16,-10],'earth','Rotura interior de la masa');

// Base terminal: cuatro bloques de apoyo escalonados, sin una losa rectangular continua.
// Las piezas que tocan el suelo solo giran en Y para conservar una suela mundial horizontal.
put('ground_front_earth','dragging_end',[-1.75,-1,-1.5],[4,1.5,4],[0,9,0],'earth','Estrato frontal, apoyo y=0');
put('ground_outer_sprawl','dragging_end',[2,-1,0],[3.5,1.5,5],[0,-8,0],'earth','Derrame de tierra exterior, apoyo y=0');
put('ground_rear_earth','dragging_end',[-.25,-1.25,2.25],[4.5,1,3],[0,14,0],'earth','Base posterior baja, apoyo y=0');
put('ground_central_ridge','dragging_end',[0,-.5,0],[3,2,3],[0,-11,0],'earth','Cresta de compresión sobre la base');
put('ground_root_outer','dragging_end',[3,-1.25,2],[1,1,4],[0,-20,0],'root','Raíz exterior tendida en el suelo');
put('ground_root_inner','dragging_end',[-2.5,-1.25,2],[1,1,4],[0,22,0],'root','Raíz interior tendida en el suelo');
put('ground_root_front','dragging_end',[2,-1.25,-2],[1,1,4],[0,-24,0],'root','Raíz frontal de arrastre');
put('ground_bone_tip','dragging_end',[0,-1.25,-2.6],[1,1,4],[0,14,0],'bone','Punta ósea expuesta en el arrastre');
put('ground_earth_chip_a','dragging_end',[-3,-1,.1],[2,1.5,2],[0,22,0],'earth','Escombro interior apoyado');
put('ground_earth_chip_b','dragging_end',[3,-1,-2.2],[2,1.5,2],[0,-16,0],'earth','Escombro exterior apoyado');

// Cinco raíces secundarias con inclinaciones distintas y anclaje dentro de la tierra.
put('hanging_root_0','debris_roots',[-1.8,-1.2,-2.5],[1,4,1],[8,7,-20],'root','Raíz colgante interior');
put('hanging_root_1','debris_roots',[-.5,-1.8,-2.8],[1,4,1],[-10,-5,8],'root','Raíz frontal larga');
put('hanging_root_2','debris_roots',[.9,-.9,-2.4],[1,4,1],[14,11,-12],'root','Raíz quebrada exterior');
put('hanging_root_3','debris_roots',[1.7,-1.4,0],[1,4,1],[-8,-12,21],'root','Raíz lateral emergente');
put('hanging_root_4','debris_roots',[-1.1,-.8,1.25],[1,4,1],[11,9,-6],'root','Raíz posterior');

// Seis fragmentos ligados al segmento inferior para el movimiento secundario futuro.
put('debris_bone_1','debris_chips',[-1,-.2,-1.8],[1,2,1],[12,-18,20],'bone','Fragmento óseo inferior frontal');
put('debris_bone_2','debris_chips',[.2,-1.2,-.4],[1,2,1],[-17,12,-24],'bone','Fragmento óseo lateral');
put('debris_stone_1','debris_chips',[-1.8,-1.6,.3],[1.5,1.5,1.5],[8,21,12],'earth','Fragmento compacto interior');
put('debris_stone_2','debris_chips',[.3,.4,.1],[1.5,1.5,1.5],[-12,-13,-17],'earth','Fragmento compacto exterior');
put('debris_mud_1','debris_chips',[-.8,-2.1,-.7],[1,1,1],[7,15,18],'earth','Terrón bajo');
put('debris_mud_2','debris_chips',[.9,-.7,-1.3],[1,1,1],[-9,-17,-12],'earth','Terrón exterior');
Canvas.updateAll();Project.model_3d.updateMatrixWorld(true);
return {stage:'masa_funeraria',cubes:Cube.all.length,bones:Group.all.length};
