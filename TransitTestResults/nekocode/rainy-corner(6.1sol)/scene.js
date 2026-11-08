/* Rainy Corner — a small, quiet world. All geometry, lettering and textures are local. */
(() => {
'use strict';
const T = THREE;
const scene = new T.Scene();
scene.fog = new T.FogExp2('#223043', .014);
const renderer = new T.WebGLRenderer({antialias:true, alpha:true, powerPreference:'high-performance'});
renderer.setPixelRatio(Math.min(devicePixelRatio, 1.65));
renderer.setSize(innerWidth, innerHeight);
renderer.outputColorSpace = T.SRGBColorSpace;
renderer.toneMapping = T.ACESFilmicToneMapping;
renderer.toneMappingExposure = 1.10;
renderer.shadowMap.enabled = true;
renderer.shadowMap.type = T.PCFSoftShadowMap;
renderer.shadowMap.autoUpdate = false;
renderer.shadowMap.needsUpdate = true;
document.body.appendChild(renderer.domElement);
renderer.domElement.setAttribute('aria-label','雨夜便利店三维微缩景观，可拖拽旋转、滚轮缩放，触屏支持双指缩放');
const camera = new T.PerspectiveCamera(33, innerWidth/innerHeight, .1, 160);
const target = new T.Vector3(0, 1.1, 0);
const frameScale = () => Math.max(1, 1.20 / camera.aspect);
let theta=.64, phi=1.16, distance=40.5;
let desiredTheta=theta, desiredPhi=phi, desiredDistance=distance;
let time=0, frames=0;
const world = new T.Group(); scene.add(world);
let seed=7381;
function rnd(){seed=(seed*1664525+1013904223)>>>0;return seed/4294967296;}
const range=(a,b)=>a+rnd()*(b-a);
const palette={ink:0x182634, cream:0xc7c4ad, wall:0xb7c1b8, metal:0x637c83, teal:0x508b87, dark:0x344b59, mint:0x8acdb9, peach:0xf2b080, coral:0xdd7868, white:0xe8e3d0, asphalt:0x293d4e, yellow:0xe2ba71};
const gradientData = new Uint8Array([70,135,205,255]);
const gradient = new T.DataTexture(gradientData,4,1,T.RedFormat);
gradient.minFilter=T.NearestFilter;gradient.magFilter=T.NearestFilter;gradient.needsUpdate=true;
const materialCache=new Map();
function mat(color,emissive=0,intensity=0){const key=`${color}/${emissive}/${intensity}`;if(!materialCache.has(key))materialCache.set(key,new T.MeshToonMaterial({color,gradientMap:gradient,emissive,emissiveIntensity:intensity}));return materialCache.get(key);}
function glow(color,intensity=1){return new T.MeshBasicMaterial({color:new T.Color(color).multiplyScalar(intensity),toneMapped:false});}
const edgeMaterial=new T.LineBasicMaterial({color:0x132530,transparent:true,opacity:.55});
const geometries=new Map(),edges=new Map();
function box(w,h,d,x,y,z,color=palette.wall,outline=true,parent=world){
 const key=`b${w},${h},${d}`;if(!geometries.has(key))geometries.set(key,new T.BoxGeometry(w,h,d));
 const geo=geometries.get(key),o=new T.Mesh(geo,color?.isMaterial?color:mat(color));
 o.position.set(x,y,z);o.castShadow=true;o.receiveShadow=true;parent.add(o);
 if(outline){if(!edges.has(key))edges.set(key,new T.EdgesGeometry(geo));o.add(new T.LineSegments(edges.get(key),edgeMaterial));}
 return o;
}
function cylinder(rt,rb,h,x,y,z,color,segments=16,parent=world){const o=new T.Mesh(new T.CylinderGeometry(rt,rb,h,segments),color?.isMaterial?color:mat(color));o.position.set(x,y,z);o.castShadow=true;o.receiveShadow=true;parent.add(o);return o;}
function sphere(r,x,y,z,color,parent=world){const o=new T.Mesh(new T.SphereGeometry(r,12,8),color?.isMaterial?color:mat(color));o.position.set(x,y,z);parent.add(o);return o;}
function tube(points,r,color,parent=world,smooth=true){const pts=points.map(p=>new T.Vector3(...p));const curve=smooth?new T.CatmullRomCurve3(pts):new T.LineCurve3(pts[0],pts[1]);const o=new T.Mesh(new T.TubeGeometry(curve,smooth?Math.max(12,pts.length*8):1,r,6,false),color?.isMaterial?color:mat(color));o.castShadow=true;parent.add(o);return o;}
function bar(a,b,r,color,parent=world){return tube([a,b],r,color,parent,false);}
function canvasTexture(w,h,draw){const c=document.createElement('canvas');c.width=w;c.height=h;draw(c.getContext('2d'),w,h);const tex=new T.CanvasTexture(c);tex.colorSpace=T.SRGBColorSpace;tex.anisotropy=renderer.capabilities.getMaxAnisotropy();return tex;}
function decal(tex,w,h,x,y,z,orientation='front',parent=world,opacity=1){const m=new T.MeshBasicMaterial({map:tex,transparent:true,opacity,side:T.DoubleSide,depthWrite:false,polygonOffset:true,polygonOffsetFactor:-2,toneMapped:false});const o=new T.Mesh(new T.PlaneGeometry(w,h),m);o.position.set(x,y,z);if(orientation==='ground')o.rotation.x=-Math.PI/2;if(orientation==='right')o.rotation.y=Math.PI/2;parent.add(o);return o;}
function textTexture(text,bg,fg,size=100,width=1024,height=256,sub=''){
 return canvasTexture(width,height,(c,w,h)=>{if(bg){c.fillStyle=bg;c.fillRect(0,0,w,h);}c.textAlign='center';c.textBaseline='middle';c.fillStyle=fg;c.font=`700 ${size}px "Yu Gothic","Microsoft YaHei",sans-serif`;c.fillText(text,w/2,sub?h*.43:h*.52);if(sub){c.font=`500 ${size*.27}px "Yu Gothic","Microsoft YaHei",sans-serif`;c.fillText(sub,w/2,h*.82);}});
}
function label(text,w,h,x,y,z,bg='#d9dfcf',fg='#315b5c',orientation='front',parent=world,sub=''){return decal(textTexture(text,bg,fg,100,1024,256,sub),w,h,x,y,z,orientation,parent);}
function disk(r,x,y,z,color){const o=new T.Mesh(new T.CircleGeometry(r,48),color?.isMaterial?color:mat(color));o.rotation.x=-Math.PI/2;o.position.set(x,y,z);world.add(o);return o;}
function glowPool(x,z,sx,sz,color,opacity=.25,y=.05){const tex=canvasTexture(128,128,(c,w,h)=>{const g=c.createRadialGradient(w/2,h/2,0,w/2,h/2,w/2);g.addColorStop(0,color);g.addColorStop(.5,color+'70');g.addColorStop(1,color+'00');c.fillStyle=g;c.fillRect(0,0,w,h);});return decal(tex,sx,sz,x,y,z,'ground',world,opacity);}
const batches=new Map();
function product(w,h,d,x,y,z,color,ry=0){const key=[w,h,d,color].join();if(!batches.has(key))batches.set(key,{w,h,d,color,list:[]});batches.get(key).list.push([x,y,z,ry]);}
function flushProducts(){const transform=new T.Object3D();for(const b of batches.values()){const mesh=new T.InstancedMesh(new T.BoxGeometry(b.w,b.h,b.d),mat(b.color),b.list.length);b.list.forEach((p,i)=>{transform.position.set(p[0],p[1],p[2]);transform.rotation.set(0,p[3],0);transform.updateMatrix();mesh.setMatrixAt(i,transform.matrix);});mesh.castShadow=true;mesh.receiveShadow=true;world.add(mesh);}}
// Cool ambient light, a soft moon, and warm light coming from the little store.
scene.add(new T.HemisphereLight(0xb4d1f2,0x354359,1.05));
const moon=new T.DirectionalLight(0xb0c4e6,1.25);moon.position.set(-7,17,8);moon.castShadow=true;
moon.shadow.mapSize.set(2048,2048);moon.shadow.camera.left=-14;moon.shadow.camera.right=14;moon.shadow.camera.top=14;moon.shadow.camera.bottom=-14;moon.shadow.camera.near=.5;moon.shadow.camera.far=45;moon.shadow.normalBias=.035;moon.shadow.bias=-.00015;moon.shadow.radius=4;scene.add(moon);
const backlight=new T.DirectionalLight(0x7fb4c6,.75);backlight.position.set(8,10,-11);scene.add(backlight);
function light(color,intensity,distance,x,y,z){const l=new T.PointLight(color,intensity,distance,1.65);l.position.set(x,y,z);scene.add(l);return l;}
light(0xffc08c,23,12,-2,2.8,.7);light(0xffc98c,17,11,-3.5,2.7,-3.3);light(0xffc98c,16,10,1.5,2.5,-2);
// The complete, beveled square base.
const baseShape=new T.Shape();const s=9.12,r=.18;baseShape.moveTo(-s+r,-s);baseShape.lineTo(s-r,-s);baseShape.quadraticCurveTo(s,-s,s,-s+r);baseShape.lineTo(s,s-r);baseShape.quadraticCurveTo(s,s,s-r,s);baseShape.lineTo(-s+r,s);baseShape.quadraticCurveTo(-s,s,-s,s-r);baseShape.lineTo(-s,-s+r);baseShape.quadraticCurveTo(-s,-s,-s+r,-s);
const baseGeo=new T.ExtrudeGeometry(baseShape,{depth:.76,bevelEnabled:true,bevelSegments:3,steps:1,bevelSize:.10,bevelThickness:.08,curveSegments:6});baseGeo.rotateX(-Math.PI/2);baseGeo.translate(0,-.82,0);
const base=new T.Mesh(baseGeo,mat(0x233a4c));base.receiveShadow=true;base.castShadow=true;world.add(base);
box(18.15,.10,18.15,0,-.07,0,0x536c78,false);
box(18.02,.05,18.02,0,-.005,0,palette.asphalt,false);
// Thin inlaid seams on the solid cut edges of the miniature.
box(18.1,.025,.025,0,-.54,9.16,0x527783,false);box(.025,.025,18.1,9.16,-.54,0,0x527783,false);
box(12.35,.25,10.8,-2.8,.14,-3.6,0x80969b);
box(11.9,.06,10.28,-2.8,.29,-3.65,0x8fa19f,false);
box(12.55,.24,1.75,-2.68,.15,2.45,0x8c9e9f);
box(1.65,.24,11.65,4.12,.15,-2.5,0x8c9e9f);
// Individual curb stones, sidewalk joints, storm drains, tactile paving.
for(let x=-8.7;x<3.65;x+=.65){box(.61,.20,.22,x,.20,3.32,0xa6b5b0);}
for(let z=-8.65;z<3.2;z+=.65){box(.22,.20,.61,4.93,.20,z,0xa6b5b0);}
for(let x=-8.75;x<3.7;x+=.84)box(.014,.009,1.42,x,.278,2.43,0x748e92,false);
for(let z=-8.8;z<3;z+=.84)box(1.35,.009,.014,4.08,.278,z,0x748e92,false);
for(let x=-7.6;x<3.5;x+=.25){box(.22,.025,.40,x,.292,2.62,0xd6bb74,false);for(let j=0;j<3;j++)product(.025,.012,.34,x-.06+j*.06,.312,2.62,0x9d8d5f);}
for(let z=-8;z<2.4;z+=.25){box(.40,.025,.22,4.22,.292,z,0xd6bb74,false);for(let j=0;j<3;j++)product(.34,.012,.025,4.22,.312,z-.06+j*.06,0x9d8d5f);}
function drain(x,z,ry=0){const g=new T.Group();g.position.set(x,.045,z);g.rotation.y=ry;world.add(g);box(1.12,.035,.30,0,0,0,0x1c313d,true,g);for(let i=0;i<12;i++)box(.035,.025,.25,-.49+i*.09,.025,0,0x5a737a,false,g);}
drain(-5.8,3.54);drain(2.3,3.54);drain(5.13,-3.7,Math.PI/2);
for(let x=-8.8;x<3.8;x+=1.5)box(1.43,.012,.04,x,.035,3.58,0x102b3a,false);
for(let z=-8.6;z<2.8;z+=1.5)box(.04,.012,1.43,5.17,.035,z,0x102b3a,false);
// A painted crossing and the quiet turn of the road.
for(let i=0;i<6;i++){box(1.70,.012,.47,6.85,.043,3.9+i*.82,mat(0xc5d1ca,0x829d9a,.06),false);}
box(3.25,.012,.13,6.85,.046,3.22,0xc9cfc0,false);
for(let x=-8.3;x<4.2;x+=2.0)box(1.05,.01,.09,x,.035,7.28,0x87978d,false);
for(let z=-8.1;z<1.6;z+=2.0)box(.09,.01,1.05,7.2,.035,z,0x87978d,false);
// Two parking bays, faded and reflected in the rain.
for(const x of [-7.2,-3.9,-.6])box(.055,.014,2.25,x,.044,4.86,0xc5c6ad,false);
box(6.66,.014,.055,-3.91,.044,5.98,0xc5c6ad,false);
const parkingTex=textTexture('軽',null,'#d4d4b6',145,256,256);decal(parkingTex,.6,.6,-5.55,.049,4.9,'ground');decal(parkingTex,.6,.6,-2.25,.049,4.9,'ground');
// Road arrow.
const arrowTex=canvasTexture(256,256,(c)=>{c.fillStyle='#adbbb1';c.beginPath();c.moveTo(112,225);c.lineTo(144,225);c.lineTo(144,110);c.lineTo(190,110);c.lineTo(128,35);c.lineTo(66,110);c.lineTo(112,110);c.closePath();c.fill();});decal(arrowTex,.75,1.15,6,.049,-5.4,'ground');
// A manhole cast in concentric steel.
disk(.51,-.3,.045,7.95,0x182e3c);disk(.44,-.3,.05,7.95,0x435d68);
for(let j=0;j<5;j++){const torus=new T.Mesh(new T.TorusGeometry(.11+j*.065,.008,4,48),mat(0x273e4c));torus.rotation.x=Math.PI/2;torus.position.set(-.3,.055,7.95);world.add(torus);}
for(let i=0;i<10;i++){const a=i*Math.PI/5;bar([-.3+Math.cos(a)*.12,.058,7.95+Math.sin(a)*.12],[-.3+Math.cos(a)*.42,.058,7.95+Math.sin(a)*.42],.008,0x273e4c);}
// Store walls, low foundations and checker-tiled floor.
box(10,.16,7.65,-1.7,.43,-2.35,0x526c6d);
box(9.84,.06,7.5,-1.7,.54,-2.35,mat(0xd1c8a9,0xe0b881,.08),false);
for(let x=-6.25;x<3;x+=.67)box(.012,.005,7.43,x,.575,-2.35,0xaaa895,false);
for(let z=-5.9;z<1.5;z+=.67)box(9.8,.005,.012,-1.7,.575,z,0xaaa895,false);
box(10,3.55,.18,-1.7,2.25,-6.1,palette.wall);
box(.18,3.55,7.65,-6.67,2.25,-2.35,palette.wall);
box(10,.43,.18,-1.7,.75,1.5,0x9caeaa);
box(.18,.43,7.65,3.26,.75,-2.35,0x9caeaa);
box(10,.45,.24,-1.7,3.66,1.48,0xb1c3b9);
box(.24,.45,7.65,3.26,3.66,-2.35,0xb1c3b9);
// Back wall panels and the door to a well stocked storeroom.
box(1.23,2.45,.06,-5.3,1.79,-5.96,0x7b9891);label('STAFF ONLY',.8,.16,-5.3,2.45,-5.918,'#9fafa4','#365758');sphere(.037,-4.88,1.65,-5.85,0xdbcaa4);
label('こもれびの、できたて。',3.65,.5,-2.14,3.13,-5.95,'#f1d6a4','#7d5e43', 'front',world,'HOT MEALS · FRESH EVERY DAY');
// A large wall of refrigerated drinks, with real shelves, little bottles and frames.
for(let n=0;n<4;n++){
 const x=-3.66+n*1.38;
 box(1.3,2.37,.63,x,1.79,-5.61,0x506b6c);
 box(1.17,2.16,.06,x,1.83,-5.25,mat(0x7fbbb4,0x75b8b0,.55),false);
 for(let j=0;j<5;j++){
  const yy=.83+j*.40;box(1.14,.045,.42,x,yy,-5.30,0xe1ddc1,false);
  for(let k=0;k<7;k++){
   const xx=x-.46+k*.15;const colors=[0xe9c9a1,0xa1caa6,0x91bcc4,0xde947e,0xeddca7];
   product(.105,.24,.12,xx,yy+.145,-5.14,colors[(j+n+k)%5]);product(.07,.025,.08,xx,yy+.275,-5.14,0xe1e7d1);
   product(.105,.048,.012,xx,yy+.14,-5.069,0xebe6cf);
  }
 }
 box(.035,2.2,.035,x-.59,1.83,-4.99,0xb6ccc1,false);box(.035,2.2,.035,x+.59,1.83,-4.99,0xb6ccc1,false);
 box(.027,.40,.038,x+.46,1.8,-4.96,0x324d54,false);
 box(1.18,.035,.035,x,2.94,-5,glow(0xc6fff0,.9),false);
}
// Three grocery gondolas, each with a full assortment, price rails and shelf dividers.
const foodColors=[0xe5bc6b,0xc7856f,0x8cae8d,0xa0b8bc,0xe0d2ab,0xb29bb4,0xccad7c,0xd09584];
function shelf(x,z,w=2.35){
 box(w,.22,.74,x,.73,z,0x8b9b8d);box(w,.95,.055,x,1.27,z,0xc1bda3,false);
 for(let level=0;level<3;level++){
  const y=.9+level*.40;box(w,.045,.79,x,y,z,0xdbd0af);box(w,.085,.035,x,y+.02,z+.4,0xefe0b8,false);
  for(let k=0;k<9;k++){
   const xx=x-w*.43+k*w*.107;let col=foodColors[(k+level*3+Math.round(x+10))%foodColors.length];
   product(.18,.26,.15,xx,y+.15,z+.22,col);product(.18,.26,.15,xx,y+.15,z-.22,col);
   product(.13,.065,.012,xx,y+.16,z+.303,0xf4e3b9);
   product(.115,.024,.012,xx,y+.035,z+.422,k%3?0xb7a78c:0xc87467);
  }
 }
 box(w,.15,.035,x,2.02,z,0x7a9e88,false);
 label('お菓子・日用品',w-.15,.13,x,2.025,z+.023,'#8ba58d','#fff2cc');
}
shelf(-3.95,-2.95,2.5);shelf(-3.95,-.82,2.5);shelf(-.57,-2.95,2.2);
// The low chilled bento / onigiri island sits just behind the front glass.
box(2.85,.76,.83,-3.85,1.01,.54,0xb0b9a8);box(2.77,.055,.9,-3.85,1.41,.53,0xe7dec1);
label('おにぎり  /  お弁当',2.62,.22,-3.85,1.26,.97,'#315f57','#f6e5ba');
for(let row=0;row<3;row++)for(let i=0;i<8;i++){
 const x=-5.03+i*.33,z=.24+row*.25;
 box(.26,.045,.20,x,1.46,z,0x2b4342,false);
 if(row<2){const rice=new T.Mesh(new T.ConeGeometry(.11,.18,3),mat(0xeee4bf));rice.position.set(x,1.56,z);rice.rotation.y=Math.PI/2;world.add(rice);product(.085,.09,.11,x,1.51,z+.035,0x41625b);}
 else{product(.19,.065,.15,x,1.51,z,0xd8bb83);product(.07,.035,.09,x+.05,1.56,z,0x91ad73);product(.08,.035,.09,x-.05,1.56,z,0xd38468);}
}
// Checkout: wood fronts, illuminated point of sale, coffee maker and oden hotpot.
box(3.04,.95,.83,.52,1.08,.23,0x9b8265);box(3.17,.10,.97,.52,1.61,.23,0xddd2ad);
for(let x=-.93;x<2.1;x+=.16)box(.028,.79,.018,x,1.08,.661,0x776c5a,false);
label('あたたかい、ひととき。',2.6,.21,.52,1.28,.68,'#b09e7d','#f5e4bd');
box(.48,.05,.30,.8,1.71,.29,0x53625e);const pos=box(.46,.34,.07,.8,1.96,.22,0x3b5455);pos.rotation.x=-.18;
label('¥  320',.37,.20,.8,1.975,.267,'#b0cfb1','#386b63');
box(.37,.59,.38,-.69,1.97,.12,0x52615b);box(.26,.17,.04,-.69,2.13,.325,0x213d3f,false);box(.23,.04,.23,-.69,1.73,.31,0xbfc3ad,false);cylinder(.085,.07,.16,-.69,1.83,.31,0xece1be);
label('COFFEE',.36,.1,-.69,2.22,.32,'#405653','#f3dab0');
box(.65,.25,.52,1.62,1.8,.21,0x80928a);box(.59,.03,.46,1.62,1.95,.21,0x555f4d,false);
for(let a=0;a<3;a++)for(let b=0;b<2;b++){sphere(.07,1.43+a*.18,1.99,.08+b*.20,[0xc59d63,0xe8cea0,0xab784f][a]);}
label('おでん',.61,.15,1.62,1.81,.482,'#c18b56','#fff0ca');
// Front magazine rack, ice cream freezer and the warm hanging promotional signs.
box(.57,.45,1.18,2.64,.85,-2.19,0xb7c7b8);box(.60,.05,1.2,2.64,1.1,-2.19,0xf2e7bf);box(.45,.015,.96,2.64,1.14,-2.19,mat(0x92c8bb,0x86b5a2,.2),false);
label('ICE',.42,.20,2.944,.91,-2.19,'#d2e6d2','#4c8d87','right');
box(.65,.22,.42,2.37,.72,.91,0x617d70);for(let i=0;i<6;i++){const x=2.11+i*.10;const mag=box(.085,.46,.035,x,1.05,.97,foodColors[i],false);mag.rotation.x=.15;product(.065,.11,.01,x,1.15,1.025,0xebe0b8);}
const posters=[];
function poster(title,subtitle,color,accent){return canvasTexture(256,360,(c,w,h)=>{c.fillStyle=color;c.fillRect(0,0,w,h);c.fillStyle=accent;c.beginPath();c.arc(w*.52,h*.48,w*.28,0,Math.PI*2);c.fill();c.fillStyle='#fff0d3';c.textAlign='center';c.font='bold 28px "Yu Gothic","Microsoft YaHei",sans-serif';c.fillText(title,w/2,48);c.font='16px sans-serif';c.fillText(subtitle,w/2,h-28);c.fillStyle='#f0d09d';c.beginPath();c.ellipse(w*.49,h*.48,43,22,-.12,0,Math.PI*2);c.fill();c.fillStyle='#785442';c.beginPath();c.ellipse(w*.49,h*.46,37,13,-.12,0,Math.PI*2);c.fill();});}
const coffeePoster=poster('ほっと、ひと息。','HOT COFFEE  ¥120','#467c70','#a1b99a');
const foodPoster=poster('できたての幸せ','あったか お弁当','#b86c55','#e2b07e');
decal(coffeePoster,.61,.86,-5.97,2.54,1.559);decal(foodPoster,.55,.78,2.49,2.5,1.56);
for(let i=0;i<3;i++){
 box(1.65,.42,.035,-4.3+i*2.7,2.91,-2.1,mat([0xc3cdb0,0xe6c79d,0xacbfa1][i],0xe1c998,.1));
 label(['新しい毎日','おいしい時間','いつもの場所'][i],1.5,.24,-4.3+i*2.7,2.91,-2.075,'#e9d7ae','#527363');
 bar([-4.3+i*2.7,3.6,-2.1],[-4.3+i*2.7,3.12,-2.1],.009,0x65796e);
}
// Ceiling panels kept shallow so the warm, detailed interior is readable through glass.
const warmStrip=glow(0xffe1aa,1.25);
for(let j=0;j<3;j++)for(let k=0;k<2;k++){box(2.12,.055,.34,-4.6+j*3.1,3.55,-.53-k*3.3,0x667f76);box(2.02,.025,.27,-4.6+j*3.1,3.508,-.53-k*3.3,warmStrip,false);}
// Two-sided storefront glazing: transparent, with only a thin blue reflection.
const glass=new T.MeshPhysicalMaterial({color:0xa9d5d2,transparent:true,opacity:.105,roughness:.1,metalness:.2,side:T.DoubleSide,depthWrite:false});
for(const spec of [[-4.57,4.02],[-.75,2.3],[2.5,1.25]]){
 box(spec[1],2.39,.028,spec[0],2.10,1.51,glass,false);
}
for(let x of [-6.57,-2.52,.42,1.85,3.19])box(.075,2.57,.09,x,2.1,1.57,0x456c6c);
for(let y of [.92,3.31])box(9.9,.075,.10,-1.7,y,1.57,0x4b6c68);
for(let z of [-5.98,-3.65,-1.35,1.47])box(.10,2.55,.075,3.3,2.1,z,0x456c6c);
for(let i=0;i<3;i++)box(.025,2.39,2.20,3.28,2.1,-4.81+i*2.30,glass,false);
for(let y of [.92,3.31])box(.10,.075,7.45,3.3,y,-2.25,0x4b6c68);
// Small white frosted safety stripes on the windows.
for(const [x,w] of [[-4.57,4.02],[2.5,1.25]]){box(w,.075,.012,x,1.43,1.574,mat(0xd3e4d1,0xa6d6be,.1),false);box(w,.027,.012,x,1.32,1.575,0xd2b896,false);}
for(let z=-5.8;z<1.2;z+=.35)product(.015,.07,.16,3.345,1.43,z,0xc9ddc6);
const doorLeft=new T.Group(),doorRight=new T.Group();world.add(doorLeft,doorRight);
function door(g,x){g.position.set(x,0,1.61);box(.72,2.54,.038,0,2.09,0,glass,false,g);for(let xx of [-.38,.38])box(.045,2.58,.075,xx,2.09,.015,0x426b6b,true,g);for(let yy of [.82,1.45,3.36])box(.8,.06,.07,0,yy,.015,0x4a7370,false,g);box(.42,.07,.015,0,1.45,.06,0xd4dac1,false,g);}
door(doorLeft,.84);door(doorRight,1.64);
box(1.72,.13,.18,1.24,3.41,1.64,0x587c75);box(.19,.07,.10,1.24,3.47,1.76,0x233f44,false);sphere(.023,1.24,3.46,1.82,glow(0x95e6b3));
label('自動',.24,.105,.84,1.68,1.68,null,'#ecedce');label('自動',.24,.105,1.64,1.68,1.68,null,'#ecedce');
box(1.70,.018,.87,1.24,.322,2.06,0x3c6c65);label('いらっしゃいませ',1.38,.25,1.24,.337,2.06,null,'#abc5ad','ground');
// Store sign, the luminous mint and tangerine ribbon, and shallow rain canopy.
box(10.36,.90,.30,-1.7,3.99,1.72,0x738f82);
box(10.20,.72,.028,-1.7,4.00,1.885,glow(0xe5e6cd,.88));
box(10.23,.105,.04,-1.7,4.34,1.91,glow(0x79baa2,.9),false);
box(10.23,.115,.04,-1.7,3.67,1.91,glow(0xdfa578,.9),false);
const signTexture=canvasTexture(1536,180,(c,w,h)=>{
 c.fillStyle='#e9edda';c.fillRect(0,0,w,h);
 c.fillStyle='#e0a06c';c.beginPath();c.arc(115,88,51,0,Math.PI*2);c.fill();
 c.fillStyle='#4c8572';c.beginPath();c.ellipse(104,66,17,34,-.7,0,Math.PI*2);c.fill();c.beginPath();c.ellipse(136,55,13,24,.7,0,Math.PI*2);c.fill();
 c.textBaseline='middle';c.textAlign='left';c.fillStyle='#315e55';c.font='bold 98px "Yu Gothic","Microsoft YaHei",sans-serif';c.fillText('こもれび',210,85);
 c.font='500 24px sans-serif';c.fillStyle='#567a68';c.fillText('K O M O R E B I   M A R T',755,83);
 c.fillStyle='#c98f60';c.fillRect(1297,27,3,125);c.textAlign='center';c.font='bold 52px sans-serif';c.fillStyle='#3b7163';c.fillText('24',1402,75);c.font='17px "Yu Gothic",sans-serif';c.fillText('HOURS OPEN',1402,128);
});
const sign=decal(signTexture,10.11,.65,-1.7,4.00,1.92);
box(.32,.88,7.72,3.44,3.99,-2.21,0x718e82);box(.03,.68,7.65,3.61,4.0,-2.21,glow(0xe5e6cd,.8),false);
box(.04,.105,7.70,3.62,4.34,-2.21,glow(0x79baa2,.9),false);box(.04,.115,7.70,3.62,3.67,-2.21,glow(0xdfa578,.9),false);
decal(textTexture('こもれび', '#e5e9d5','#3f7867',95,1024,180,'KOMOREBI MART   /   24 HOURS'),5.6,.64,3.64,4,-2.1,'right');
box(10.65,.12,.91,-1.7,3.53,1.86,0x73978b);box(10.61,.065,.08,-1.7,3.51,2.31,0xa9bbb0);
for(let x=-6.7;x<3.4;x+=1.6)box(.07,.045,.72,x,3.46,1.86,0x4c746c,false);
box(9.98,.022,.10,-1.7,3.45,2.20,glow(0xffcf8f,.9),false);
// Roof: folded sheet metal, water seams, parapet, small utility housing.
box(10.42,.24,7.94,-1.7,4.53,-2.24,0x6b8b83);
box(10.11,.035,7.68,-1.7,4.675,-2.24,mat(0x456e6c,0x24434b,.03),false);
for(let x=-6.6;x<3.4;x+=.48){box(.025,.025,7.58,x,4.707,-2.24,0x7b9d92,false);}
box(10.5,.16,.13,-1.7,4.71,-6.22,0x8baba0);box(.13,.16,7.98,-6.91,4.71,-2.24,0x8baba0);box(.13,.16,7.98,3.5,4.71,-2.24,0x8baba0);
box(10.53,.12,.13,-1.7,4.69,1.73,0x90b1a3);
for(let x of [-6.83,3.48]){cylinder(.055,.055,4.25,x,2.36,1.42,0x476f6b,10);tube([[x,4.56,1.71],[x,4.38,1.50],[x,4.15,1.42]],.065,0x5f8a7d);tube([[x,.4,1.42],[x,.34,1.7],[x,.31,1.86]],.055,0x5b7d76);}
box(2.7,.12,1.55,-3.7,4.83,-3.8,0x46656a);box(2.50,.8,1.34,-3.7,5.27,-3.8,0x91a79e);box(2.58,.08,1.42,-3.7,5.71,-3.8,0xbdc6af);
for(let x of [-4.27,-3.12]){
 const fan=new T.Mesh(new T.TorusGeometry(.40,.035,8,32),mat(0x637e78));fan.rotation.x=Math.PI/2;fan.position.set(x,5.772,-3.8);world.add(fan);disk(.36,x,5.75,-3.8,0x374f55);
 for(let k=0;k<8;k++){const a=k*Math.PI/4;bar([x+Math.cos(a)*.06,5.79,-3.8+Math.sin(a)*.06],[x+Math.cos(a)*.36,5.79,-3.8+Math.sin(a)*.36],.012,0x819b8f);}
 sphere(.055,x,5.79,-3.8,0xa0b1a2);
}
for(let k=0;k<10;k++)box(2.22,.026,.025,-3.7,4.96+k*.065,-3.119,0x586f6b,false);
tube([[-3.7,4.9,-4.47],[-3.7,4.84,-5.2],[-1.2,4.83,-5.2],[-1.2,4.7,-5.8]],.047,0x7f9992);
box(1.30,.18,.80,.92,4.86,-4.85,0x8daba4);for(let i=0;i<6;i++)box(.11,.012,.63,.43+i*.19,4.965,-4.85,0x3d6267,false);
const roofPuddle=canvasTexture(256,128,(c)=>{c.fillStyle='#90b1ad';c.beginPath();c.ellipse(130,70,112,38,-.05,0,Math.PI*2);c.fill();});decal(roofPuddle,2.8,.85,.8,4.72,-.65,'ground',world,.16);
// Small neighboring service alcove and rear alley; every object stays on the base.
box(1.52,2.52,3.9,-7.75,1.62,-3.75,0x7b9090);box(1.67,.16,4.06,-7.75,2.95,-3.75,0x4d6b70);
box(.035,1.72,1.0,-6.96,1.52,-4.4,0x4d7072);label('路地',.40,.25,-6.93,2.5,-4.4,'#749591','#d7ddc0','right');
for(let n=0;n<2;n++){
 box(.31,.66,.93,-6.92,1.2+n*.9,-2.9,0xb4bba6);const f=new T.Mesh(new T.TorusGeometry(.23,.018,6,24),mat(0x6b837b));f.rotation.y=Math.PI/2;f.position.set(-6.75,1.2+n*.9,-2.9);world.add(f);
 for(let j=0;j<7;j++)box(.012,.021,.65,-6.745,.99+n*.9+j*.065,-2.9,0x657d77,false);
 tube([[-6.91,.9+n*.9,-2.4],[-6.88,.66+n*.9,-2.24],[-6.88,.43,-1.85]],.035,0x9ea793);
}
box(10.5,.50,.17,-2.1,.53,-8.63,0x728a8d);for(let x=-7.1;x<3.2;x+=.6)box(.025,.35,.18,x,.55,-8.63,0x526f77,false);
box(.13,1.22,2.1,4.09,.9,-7.35,0x657f7e);for(let z=-8.35;z<-6.3;z+=.15)box(.04,.8,.04,4.16,1.13,z,0x3d5f65,false);// A cherry-red vending machine, with individually modeled illuminated drink rows.
const vending=new T.Group();vending.position.set(4.02,.31,-.31);vending.rotation.y=.10;world.add(vending);
box(1.14,2.44,.79,0,1.22,0,0xae6263,true,vending);box(1.20,.09,.86,0,2.45,0,0xcf8a79,true,vending);
box(.92,1.48,.045,-.035,1.54,.419,0x283f4b,true,vending);box(.81,1.31,.015,-.065,1.56,.45,glow(0xb9e1d6,.83),false,vending);
label('DRINKS',.79,.13,-.06,2.28,.456,'#debd9e','#5a7264','front',vending);
const bottleColors=[0x91bfb4,0xe9d19b,0xcb927e,0x719fbc,0xe2d4ad,0x93ad7c];
for(let j=0;j<3;j++)for(let i=0;i<5;i++){
 const x=-.39+i*.16,y=1.08+j*.36;
 cylinder(.051,.051,.22,x,y,.48,mat(bottleColors[(i+j)%6]),10,vending);cylinder(.029,.029,.04,x,y+.13,.48,0xe7e3c5,8,vending);
 box(.063,.055,.013,x,y,.537,0xe9e0c3,false,vending);box(.104,.045,.018,x,y-.16,.477,0x35565d,false,vending);sphere(.015,x,y-.16,.491,glow(0x9be6be),vending);
}
box(.80,.19,.03,-.03,.47,.43,0x213c47,true,vending);box(.65,.035,.06,-.03,.39,.45,0x63797a,false,vending);
box(.15,.44,.025,.43,.93,.442,0x3d5356,false,vending);box(.075,.085,.02,.43,1.05,.46,0x98b6a5,false,vending);box(.035,.012,.01,.43,.90,.46,0xd7be8b,false,vending);
label('つめた〜い',.76,.13,-.05,.70,.441,'#d9c8ad','#467a81','front',vending);
light(0x8cdbcf,8,4,4,1.9,.8);glowPool(4.2,1.0,2,1.9,'#90d4be',.28,.32);
// Small recycling bins, an umbrella stand with three translucent umbrellas.
for(let i=0;i<2;i++){
 const x=4.04,z=-1.70-i*.62;
 box(.64,.85,.51,x,.74,z,0x9baea3);box(.67,.13,.54,x,1.2,z,[0x698f88,0x9fafa0][i]);
 const opening=new T.Mesh(new T.CircleGeometry(.115,20),mat(0x233f48));opening.rotation.y=Math.PI/2;opening.position.set(x+.326,.92,z);world.add(opening);
 label(i?'かん':'びん',.30,.13,x+.333,.64,z,'#a7b7a5','#355d60','right');
}
box(.82,.55,.36,-6.15,.61,2.0,0x5d827b);for(let i=0;i<6;i++)box(.035,.42,.025,-6.48+i*.13,.69,2.194,0x9caf9b,false);
for(let i=0;i<3;i++){
 const x=-6.40+i*.25,z=2.0;
 bar([x,.77,z],[x,1.66+i*.07,z],.014,0xa3b5a8);
 tube([[x,1.67+i*.07,z],[x,1.78+i*.07,z],[x+.09,1.80+i*.07,z],[x+.12,1.72+i*.07,z]],.018,0x7ca99e);
 const canopy=new T.Mesh(new T.ConeGeometry(.10,.67,8),new T.MeshToonMaterial({color:[0x8faab2,0xc2bf9d,0x93bcae][i],gradientMap:gradient,transparent:true,opacity:.78}));canopy.position.set(x,1.16,z);world.add(canopy);
}
// A silver-green commuter bicycle, leaned into the little side lane.
const bike=new T.Group();bike.position.set(4.0,.36,-4.72);bike.rotation.y=Math.PI/2-.16;world.add(bike);
const tyre=mat(0x263d45),spoke=mat(0x91aaa5),frame=mat(0x709c91);
for(let x of [-.73,.73]){
 const wheel=new T.Mesh(new T.TorusGeometry(.46,.045,8,40),tyre);wheel.position.set(x,.49,0);bike.add(wheel);
 const rim=new T.Mesh(new T.TorusGeometry(.415,.014,6,40),spoke);rim.position.set(x,.49,0);bike.add(rim);
 cylinder(.055,.055,.15,x,.49,0,0xb6beb0,10,bike).rotation.x=Math.PI/2;
 for(let i=0;i<12;i++){const a=i*Math.PI/6;bar([x,.49,0],[x+Math.cos(a)*.41,.49+Math.sin(a)*.41,0],.006,spoke,bike);}
 const fender=new T.Mesh(new T.TorusGeometry(.49,.024,6,25,Math.PI),mat(0xa0b2a6));fender.position.set(x,.49,0);bike.add(fender);
}
const bikePoints=[[-.73,.49,0],[-.29,1.12,0],[.02,.47,0],[.52,1.17,0],[.73,.49,0]];
for(const [a,b] of [[0,1],[1,2],[2,0],[1,3],[3,2],[3,4]])bar(bikePoints[a],bikePoints[b],.032,frame,bike);
bar([-.29,1.12,0],[-.32,1.31,0],.025,0x9ab0a8,bike);box(.38,.065,.21,-.34,1.33,0,0x3e5354,true,bike);
bar([.52,1.17,0],[.50,1.52,0],.026,0x9eb5ac,bike);tube([[.5,1.52,-.28],[.5,1.57,-.14],[.5,1.52,.14],[.5,1.52,.28]],.026,0xb2c1af,bike);
bar([.5,1.52,-.29],[.5,1.52,-.16],.042,0x385658,bike);bar([.5,1.52,.16],[.5,1.52,.29],.042,0x385658,bike);
box(.40,.29,.46,.78,1.21,0,new T.MeshToonMaterial({color:0x889c92,wireframe:true,gradientMap:gradient}),false,bike);
for(let i=0;i<6;i++){bar([.58+i*.075,1.06,-.23],[.58+i*.075,1.37,-.23],.009,0x9db3a4,bike);bar([.58+i*.075,1.06,.23],[.58+i*.075,1.37,.23],.009,0x9db3a4,bike);}
bar([-.94,1.02,-.17],[-.45,1.02,-.17],.018,0xa6b8aa,bike);bar([-.94,1.02,.17],[-.45,1.02,.17],.018,0xa6b8aa,bike);
bar([.02,.47,-.08],[.18,.36,-.08],.02,0x344e54,bike);box(.16,.04,.12,.18,.34,-.08,0x2b444c,false,bike);
bar([-.12,.46,.06],[-.32,.03,.27],.018,0xa9b9aa,bike);
sphere(.05,.74,1.05,.03,glow(0xf9e6b0),bike);sphere(.038,-.92,1.05,.04,glow(0xcf745f),bike);
// A few carefully placed plants soften the very ordinary street corner.
function plant(x,z,scale=1){cylinder(.25*scale,.18*scale,.38*scale,x,.30+.19*scale,z,0xa18a73,10);for(let k=0;k<7;k++){
 const a=k*Math.PI*2/7,rr=.15*scale;const leaf=new T.Mesh(new T.SphereGeometry(.16,7,5),mat([0x698d7d,0x769b80,0x548576][k%3]));leaf.scale.set(.62,1.8,.7);leaf.position.set(x+Math.cos(a)*rr,.62+scale*.18+Math.abs(Math.sin(a))*scale*.14,z+Math.sin(a)*rr);leaf.rotation.z=Math.cos(a)*.65;world.add(leaf);
}}
plant(3.98,-6.07,.9);plant(-6.95,.78,.85);plant(-8.33,-.99,1.1);
// Poster board with a roof, a bus stop style sign, and a weathered little traffic mirror.
box(1.32,1.43,.12,-7.90,1.57,1.37,0x456c6c);box(1.19,1.29,.03,-7.90,1.57,1.45,0xbaa785);box(1.48,.1,.32,-7.90,2.35,1.38,0x6f9690);
for(let x of [-8.4,-7.4])box(.07,1.87,.07,x,1.21,1.36,0x456c6c);
decal(foodPoster,.46,.65,-8.2,1.8,1.478);decal(coffeePoster,.40,.58,-7.61,1.82,1.479);
label('町内のお知らせ',1.05,.15,-7.9,2.17,1.48,'#c7b394','#496c62');label('雨の日も、いつも通り。',1.05,.18,-7.9,1.14,1.481,'#d7c5a0','#6e7b66');
cylinder(.045,.055,3.07,-8.12,1.82,2.58,0x798f87,12);const roundSign=new T.Mesh(new T.CylinderGeometry(.40,.40,.07,32),mat(0xa8b7a5));roundSign.rotation.x=Math.PI/2;roundSign.position.set(-8.12,3.42,2.58);world.add(roundSign);
const roundTex=canvasTexture(256,256,(c)=>{c.fillStyle='#426f66';c.beginPath();c.arc(128,128,120,0,Math.PI*2);c.fill();c.fillStyle='#e3ddbf';c.beginPath();c.arc(128,128,100,0,Math.PI*2);c.fill();c.fillStyle='#426f66';c.textAlign='center';c.font='bold 40px "Yu Gothic",sans-serif';c.fillText('桜町',128,121);c.font='22px sans-serif';c.fillText('SAKURAMACHI',128,158);});decal(roundTex,.76,.76,-8.12,3.42,2.625);
// White roadside guardrails, with warm reflectors on their posts.
function guardrail(x,z,len,ry=0){const group=new T.Group();group.position.set(x,.02,z);group.rotation.y=ry;world.add(group);for(let i=0;i<3;i++){const xx=-len/2+i*len/2;cylinder(.055,.069,.83,xx,.415,0,0xa5b8ad,12,group);cylinder(.075,.075,.06,xx,.85,0,0xd4d1ae,12,group);box(.05,.10,.012,xx,.66,.062,glow(0xe5bc78,.6),false,group);}for(let y of [.38,.72])bar([-len/2,y,0],[len/2,y,0],.033,0xa8bcb2,group);}
guardrail(-5.3,3.12,2.65);guardrail(-.28,3.12,2.55);guardrail(4.73,-6.5,2.5,Math.PI/2);
// Streetlamp with a pool of honey-colored light.
function streetLamp(x,z){cylinder(.075,.13,5.35,x,2.71,z,0x516e73,12);cylinder(.18,.22,.23,x,.15,z,0x6f8582,12);tube([[x,5.17,z],[x,5.57,z],[x+.18,5.77,z],[x+.84,5.77,z]],.057,0x718c84);box(.80,.10,.37,x+.69,5.72,z,0x58796e);box(.65,.035,.26,x+.69,5.65,z,glow(0xffd9a0,1.2),false);light(0xffcf8a,23,8,x+.7,4.9,z);glowPool(x+.7,z,4.5,4.5,'#e9b780',.19,.055);}
streetLamp(-7.88,4.20);
// The familiar tall utility pole, insulators, transformer and sagging overhead lines.
const px=5.22,pz=2.27;cylinder(.11,.16,7.28,px,3.64,pz,0x748b8a,12);cylinder(.18,.18,.6,px,.5,pz,0x263f4a,12);
for(let i=0;i<4;i++)box(.035,.055,.31,px+.13,1.6+i*.43,pz,0xa0b1a2,false);
box(1.95,.10,.10,px,6.58,pz,0x587374);box(1.5,.08,.08,px,7.08,pz,0x587374);
for(let x of [-.78,-.23,.32,.82]){
 bar([px+x,6.60,pz],[px+x,6.83,pz],.026,0x7e9690);
 for(let j=0;j<3;j++)cylinder(.075,.066,.045,px+x,6.77+j*.055,pz,0xa1b8ad,10);
}
cylinder(.28,.28,.73,px-.39,5.75,pz-.04,0xa1b1a5,16);cylinder(.31,.31,.07,px-.39,6.15,pz-.04,0xc1c7ae,16);
tube([[px-.36,6.21,pz],[px-.35,6.45,pz],[px+.32,6.55,pz]],.026,0x2b424c);
const otherPole=[-8.2,6.9,-6.55];cylinder(.075,.11,6.87,otherPole[0],3.43,otherPole[2],0x728985,12);box(1.38,.08,.09,otherPole[0],6.55,otherPole[2],0x5e7e79);
for(let i=0;i<3;i++){
 const a=[px-.69+i*.56,6.93,pz],b=[otherPole[0]-.48+i*.45,6.70,otherPole[2]];
 tube([a,[a[0]*.66+b[0]*.34,5.88,a[2]*.66+b[2]*.34],[a[0]*.33+b[0]*.67,5.80,a[2]*.33+b[2]*.67],b],.017,0x233946);
}
tube([[px,6.33,pz],[5.2,5.97,-2.1],[4.81,6.02,-6.9],[4.70,6.27,-8.5]],.014,0x273e49);
// Pole-mounted street address, narrow traffic sign, safety stripes.
label('桜町二丁目',.40,.94,px,3.15,pz+.142,'#426961','#d6dac0');
for(let i=0;i<4;i++)box(.27,.09,.012,px,1.02+i*.17,pz+.142,i%2?0x425c5c:0xd4bc7f,false);
bar([px,4.6,pz],[px+.92,4.6,pz],.035,0x77958a);box(1.60,.35,.065,px+.83,4.62,pz,0x567f86);label('←  桜町通り',1.48,.28,px+.83,4.62,pz+.041,'#467a85','#dde7cc');
// Distant traffic light at the end of the side street.
const signalGroup=new T.Group();signalGroup.position.set(7.75,0,-7.23);world.add(signalGroup);
cylinder(.055,.075,4.48,0,2.24,0,0x627e7c,12,signalGroup);bar([0,4.34,0],[-1.16,4.34,0],.04,0x627e7c,signalGroup);
box(.89,.32,.30,-1.12,4.29,0,0x29444c,true,signalGroup);
const signals=[];for(let i=0;i<3;i++){const bulb=new T.Mesh(new T.SphereGeometry(.10,12,8),glow([0x79cebd,0x665c3a,0x63474c][i],.9));bulb.scale.z=.5;bulb.position.set(-1.41+i*.29,4.29,.169);signalGroup.add(bulb);signals.push(bulb);}
light(0x73c8bd,2,3.7,6.34,4.3,-7);glowPool(6.4,-6.4,1.7,3,'#78bfb4',.13);
// A familiar blue neighborhood sign, attached to the little square base.
cylinder(.035,.04,2.15,8.15,1.09,1.92,0x819a94,10);box(.52,.58,.07,8.15,2.0,1.92,0x5c8e9d);label('P',.45,.45,8.15,2.02,1.962,'#56869b','#e5e8ce');
// Asphalt grain and broken light smears, all hand drawn rather than external assets.
const asphaltTex=canvasTexture(1024,1024,(c,w,h)=>{for(let i=0;i<19000;i++){const x=rnd()*w,y=rnd()*h,a=rnd()*.12;c.fillStyle=`rgba(168,192,192,${a})`;c.fillRect(x,y,rnd()<.5?1:2,1);}for(let i=0;i<140;i++){c.strokeStyle='rgba(110,151,157,.10)';c.lineWidth=rnd()*1.7;c.beginPath();const x=rnd()*w,y=rnd()*h;c.moveTo(x,y);c.lineTo(x+range(3,30),y+range(-4,4));c.stroke();}});
decal(asphaltTex,18,18,0,.021,0,'ground',world,.65);
// One L-shaped planar reflection, inset on the road; a ripple shader breaks its surface.
const roadShape=new T.Shape();roadShape.moveTo(-8.98,8.98);roadShape.lineTo(8.98,8.98);roadShape.lineTo(8.98,-8.98);roadShape.lineTo(5.18,-8.98);roadShape.lineTo(5.18,3.58);roadShape.lineTo(-8.98,3.58);roadShape.closePath();
const reflectionShader={
 uniforms:{color:{value:null},tDiffuse:{value:null},textureMatrix:{value:null},uTime:{value:0}},
 vertexShader:`uniform mat4 textureMatrix;varying vec4 vUv;varying vec3 vWorld;void main(){vUv=textureMatrix*vec4(position,1.0);vWorld=(modelMatrix*vec4(position,1.0)).xyz;gl_Position=projectionMatrix*modelViewMatrix*vec4(position,1.0);}`,
 fragmentShader:`uniform sampler2D tDiffuse;uniform float uTime;uniform vec3 color;varying vec4 vUv;varying vec3 vWorld;
 float hash(vec2 p){return fract(sin(dot(p,vec2(127.1,311.7)))*43758.5453);}float noise(vec2 p){vec2 i=floor(p),f=fract(p);f=f*f*(3.-2.*f);return mix(mix(hash(i),hash(i+vec2(1,0)),f.x),mix(hash(i+vec2(0,1)),hash(i+vec2(1,1)),f.x),f.y);}
 void main(){vec4 uv=vUv;float a=sin(vWorld.x*15.+vWorld.z*20.+uTime*1.3)*.0014;float b=sin(vWorld.z*33.-uTime*1.7+sin(vWorld.x*9.))*.0008;uv.x+=(a+b)*uv.w;uv.y+=sin(vWorld.z*24.+uTime)*.0012*uv.w;
 vec3 ref=texture2DProj(tDiffuse,uv).rgb;float n=noise(vWorld.xz*2.4);float puddle=smoothstep(.36,.7,n);float brightness=dot(ref,vec3(.2126,.7152,.0722));gl_FragColor=vec4(ref*vec3(.69,.84,.93),(.09+puddle*.27)*(.63+min(brightness,.7))); 
 #include <tonemapping_fragment>
 #include <colorspace_fragment>
 }`
};
const roadReflector=new RainReflector(new T.ShapeGeometry(roadShape),{textureWidth:Math.min(1536,Math.round(innerWidth*1.1)),textureHeight:Math.min(1024,Math.round(innerHeight*1.1)),color:0x526b7a,clipBias:.005,multisample:0,shader:reflectionShader});roadReflector.rotation.x=-Math.PI/2;roadReflector.position.y=.066;roadReflector.material.transparent=true;roadReflector.material.depthWrite=false;roadReflector.renderOrder=4;world.add(roadReflector);
// Shallow irregular pools collect a softer, broader version of the warm storefront glow.
function reflectedStreak(x,z,w,l,col,opacity){const tex=canvasTexture(256,512,(c,cw,ch)=>{
 const g=c.createLinearGradient(0,0,0,ch);g.addColorStop(0,col+'00');g.addColorStop(.18,col+'80');g.addColorStop(.58,col+'45');g.addColorStop(1,col+'00');c.fillStyle=g;
 for(let y=0;y<ch;y+=3){const rr=rnd();const width=cw*(.15+rr*.68)*(1-y/ch*.5);c.globalAlpha=.15+rnd()*.55;c.fillRect(cw/2-width/2+range(-18,18),y,width,1+rnd()*3);}
 });return decal(tex,w,l,x,.072,z,'ground',world,opacity);}
reflectedStreak(-3.8,5.0,3.8,3.4,'#d3af76',.24);reflectedStreak(1.2,5.0,2.4,3.7,'#d2bd8c',.29);reflectedStreak(4.05,3.25,1.45,2.6,'#d88683',.23);reflectedStreak(-7.2,5.9,1.1,3.4,'#e6be7b',.32);reflectedStreak(6.6,-5.9,.5,2.5,'#75cabb',.17);
glowPool(-2,2.92,9,2.2,'#dfba83',.12,.31);
// A delicate rain curtain, made from one draw call rather than thousands of objects.
const dropCount=1030,rainArray=new Float32Array(dropCount*6),dropState=[];
for(let i=0;i<dropCount;i++)dropState.push({x:range(-8.94,8.94),z:range(-8.94,8.94),y:range(.2,8.5),speed:range(7,10),length:range(.09,.23)});
const rainGeo=new T.BufferGeometry();rainGeo.setAttribute('position',new T.BufferAttribute(rainArray,3));
const rainMaterial=new T.LineBasicMaterial({color:0xb6d6df,transparent:true,opacity:.20,depthWrite:false});const rain=new T.LineSegments(rainGeo,rainMaterial);rain.frustumCulled=false;world.add(rain);
// Fine rain rings appear on the road and rooftop; they expand, then fade away.
const rippleCount=88,rippleVerts=new Float32Array(rippleCount*36*6),rippleColors=new Float32Array(rippleVerts.length),rippleStates=[];
for(let i=0;i<rippleCount;i++){
 let x,z;do{x=range(-8.7,8.7);z=range(-8.7,8.7);}while(x<5.25&&z<3.62);
 rippleStates.push({x,z,y:.08,phase:rnd(),speed:range(.55,1.05),max:range(.12,.35)});
}
for(let i=0;i<16;i++)rippleStates[i]={x:range(-6.5,3.1),z:range(-5.8,1.5),y:4.73,phase:rnd(),speed:range(.6,1),max:range(.07,.18)};
const rippleGeo=new T.BufferGeometry();rippleGeo.setAttribute('position',new T.BufferAttribute(rippleVerts,3));rippleGeo.setAttribute('color',new T.BufferAttribute(rippleColors,3));const rippleLines=new T.LineSegments(rippleGeo,new T.LineBasicMaterial({vertexColors:true,transparent:true,opacity:.47,depthWrite:false,blending:T.AdditiveBlending}));rippleLines.frustumCulled=false;world.add(rippleLines);
// Rainwater collects along the canopy, dropping into the gutter at staggered times.
const drips=[];for(let i=0;i<22;i++){
 const x=-6.77+i*.47;const d=new T.Mesh(new T.SphereGeometry(.018,6,5),new T.MeshBasicMaterial({color:0xc4dfd6,transparent:true,opacity:.57}));d.scale.set(.75,2.4,.75);d.position.set(x,3.45,2.31);world.add(d);drips.push({mesh:d,phase:rnd(),speed:range(.55,.95),x});
}
// Animated rain rivulets cling to the storefront glass.
const streaks=[],glassRainPositions=new Float32Array(65*6);
for(let i=0;i<65;i++){const isSide=i>44;streaks.push({side:isSide,a:isSide?range(-5.85,1.35):range(-6.4,3.05),y:range(.98,3.2),length:range(.04,.22),phase:rnd(),speed:range(.04,.12)});}
const glassRainGeo=new T.BufferGeometry();glassRainGeo.setAttribute('position',new T.BufferAttribute(glassRainPositions,3));const glassRain=new T.LineSegments(glassRainGeo,new T.LineBasicMaterial({color:0xb8d4cb,transparent:true,opacity:.29,depthWrite:false}));glassRain.frustumCulled=false;world.add(glassRain);
// Sparse, small pavement splash glints.
const sparklePositions=[];for(let i=0;i<180;i++){let x,z;do{x=range(-8.8,8.8);z=range(-8.8,8.8);}while(x<5.2&&z<3.6);sparklePositions.push(x,.076,z);}
const splashGeo=new T.BufferGeometry();splashGeo.setAttribute('position',new T.Float32BufferAttribute(sparklePositions,3));const splashMat=new T.PointsMaterial({color:0xb4d0ca,size:.024,transparent:true,opacity:.45,depthWrite:false});world.add(new T.Points(splashGeo,splashMat));
flushProducts();
// Bake static geometry by material, preserving animated doors, signal bulbs and transparent glazing.
// This reduces hundreds of individual tiny parts to a few material batches.
world.updateMatrixWorld(true);
function animatedAncestor(o){for(let p=o;p;p=p.parent)if(p===doorLeft||p===doorRight)return true;return signals.includes(o);}
function mergeGeometries(list,attributes){
 const merged=new T.BufferGeometry();
 for(const name of attributes){const size=list[0].attributes[name].itemSize;const length=list.reduce((sum,g)=>sum+g.attributes[name].array.length,0);const array=new Float32Array(length);let offset=0;for(const g of list){array.set(g.attributes[name].array,offset);offset+=g.attributes[name].array.length;}merged.setAttribute(name,new T.BufferAttribute(array,size));}
 merged.computeBoundingSphere();return merged;
}
const staticMeshes=[],staticEdges=[];
world.traverse(o=>{if(animatedAncestor(o))return;if(o.isLineSegments&&o.material===edgeMaterial)staticEdges.push(o);else if(o.isMesh&&!o.isInstancedMesh&&!Array.isArray(o.material)&&!o.material.transparent&&!o.material.wireframe)staticMeshes.push(o);});
const meshBatches=new Map();for(const o of staticMeshes){const key=o.material.uuid+'/'+o.castShadow+'/'+o.receiveShadow;let batch=meshBatches.get(key);if(!batch){batch={material:o.material,cast:o.castShadow,receive:o.receiveShadow,geometries:[]};meshBatches.set(key,batch);}const g=o.geometry.index?o.geometry.toNonIndexed():o.geometry.clone();g.applyMatrix4(o.matrixWorld);batch.geometries.push(g);}
const lineGeos=staticEdges.map(o=>{const g=o.geometry.clone();g.applyMatrix4(o.matrixWorld);return g;});
for(const o of staticEdges)o.removeFromParent();for(const o of staticMeshes)o.removeFromParent();
for(const b of meshBatches.values()){const m=new T.Mesh(mergeGeometries(b.geometries,['position','normal','uv']),b.material);m.castShadow=b.cast;m.receiveShadow=b.receive;world.add(m);b.geometries.forEach(g=>g.dispose());}
if(lineGeos.length){world.add(new T.LineSegments(mergeGeometries(lineGeos,['position']),edgeMaterial));lineGeos.forEach(g=>g.dispose());}
// A subtle photographic finish: glow on illuminated lettering, a soft vignette and barely visible grain.
const renderTarget=new T.WebGLRenderTarget(Math.round(innerWidth*renderer.getPixelRatio()),Math.round(innerHeight*renderer.getPixelRatio()),{samples:Math.min(renderer.capabilities.maxSamples,4),type:T.HalfFloatType});
const postScene=new T.Scene(),postCamera=new T.OrthographicCamera(-1,1,1,-1,0,1);
const postMaterial=new T.ShaderMaterial({depthTest:false,depthWrite:false,uniforms:{tScene:{value:renderTarget.texture},resolution:{value:new T.Vector2(innerWidth,innerHeight)},uTime:{value:0}},vertexShader:`varying vec2 vUv;void main(){vUv=uv;gl_Position=vec4(position.xy,0.,1.);}`,fragmentShader:`
 precision highp float;uniform sampler2D tScene;uniform vec2 resolution;uniform float uTime;varying vec2 vUv;
 vec3 bright(vec2 uv){vec3 col=texture2D(tScene,uv).rgb;return col*smoothstep(.68,1.18,max(max(col.r,col.g),col.b));}
 void main(){vec4 src=texture2D(tScene,vUv);vec3 c=src.rgb;vec3 bloom=vec3(0.);vec2 px=1./resolution;
 for(int i=0;i<8;i++){float a=float(i)*.785398;vec2 d=vec2(cos(a),sin(a));bloom+=bright(vUv+d*px*2.)*.042;bloom+=bright(vUv+d*px*6.)*.025;bloom+=bright(vUv+d*px*13.)*.012;}
 c+=bloom;float v=1.-.16*pow(length((vUv-.5)*vec2(1.,.85)),1.6);c*=v;float grain=fract(sin(dot(vUv*resolution,vec2(12.9898,78.233)))*43758.5453)-.5;c+=grain*.006;gl_FragColor=vec4(c,src.a);
 #include <tonemapping_fragment>
 #include <colorspace_fragment>
 }`});
postScene.add(new T.Mesh(new T.PlaneGeometry(2,2),postMaterial));
// Inertial orbit / pan / zoom. No controls, panels, labels or other interface are drawn.
const canvas=renderer.domElement,pointers=new Map();let pinchDistance=0,lastMid=null;
canvas.addEventListener('contextmenu',e=>e.preventDefault());
canvas.addEventListener('pointerdown',e=>{canvas.setPointerCapture(e.pointerId);pointers.set(e.pointerId,{x:e.clientX,y:e.clientY,button:e.button});pinchDistance=0;lastMid=null;});
function panCamera(dx,dy){const scale=distance*.0014;const right=new T.Vector3(Math.cos(theta),0,-Math.sin(theta));target.addScaledVector(right,-dx*scale);target.y+=dy*scale;target.x=T.MathUtils.clamp(target.x,-4,4);target.z=T.MathUtils.clamp(target.z,-4,4);target.y=T.MathUtils.clamp(target.y,-1,4.5);}
canvas.addEventListener('pointermove',e=>{
 const old=pointers.get(e.pointerId);if(!old)return;const dx=e.clientX-old.x,dy=e.clientY-old.y;pointers.set(e.pointerId,{x:e.clientX,y:e.clientY,button:old.button});
 if(pointers.size===2){const [a,b]=[...pointers.values()];const d=Math.hypot(a.x-b.x,a.y-b.y);const mid={x:(a.x+b.x)/2,y:(a.y+b.y)/2};if(pinchDistance>0)desiredDistance=T.MathUtils.clamp(desiredDistance*pinchDistance/d,15,67);if(lastMid)panCamera(mid.x-lastMid.x,mid.y-lastMid.y);pinchDistance=d;lastMid=mid;}
 else if(e.shiftKey||old.button===2||old.button===1)panCamera(dx,dy);else{desiredTheta-=dx*.006;desiredPhi=T.MathUtils.clamp(desiredPhi-dy*.004,.30,1.46);}
});
for(const name of ['pointerup','pointercancel','lostpointercapture'])canvas.addEventListener(name,e=>{pointers.delete(e.pointerId);pinchDistance=0;lastMid=null;});
canvas.addEventListener('wheel',e=>{e.preventDefault();desiredDistance=T.MathUtils.clamp(desiredDistance*Math.exp(e.deltaY*.0008),15,67);},{passive:false});
canvas.addEventListener('dblclick',()=>{desiredTheta=.64;desiredPhi=1.16;desiredDistance=40.5;target.set(0,1.1,0);});
function resize(){camera.aspect=innerWidth/innerHeight;camera.updateProjectionMatrix();renderer.setSize(innerWidth,innerHeight);renderTarget.setSize(Math.round(innerWidth*renderer.getPixelRatio()),Math.round(innerHeight*renderer.getPixelRatio()));postMaterial.uniforms.resolution.value.set(innerWidth,innerHeight);}
addEventListener('resize',resize);
// Framing scales with aspect ratio, including live portrait/landscape changes.
const clock=new T.Clock();let last=0,lastShadowDoor=0;
function animate(){requestAnimationFrame(animate);if(document.hidden){clock.getDelta();return;}const dt=Math.min(clock.getDelta(),.05);time+=dt;frames++;
 scene.fog.density=.014/frameScale();
 const damping=1-Math.exp(-dt*10);theta+=(desiredTheta-theta)*damping;phi+=(desiredPhi-phi)*damping;distance+=(desiredDistance-distance)*damping;
 camera.position.set(target.x+distance*frameScale()*Math.sin(phi)*Math.sin(theta),target.y+distance*frameScale()*Math.cos(phi),target.z+distance*frameScale()*Math.sin(phi)*Math.cos(theta));camera.lookAt(target);
 for(let i=0;i<dropCount;i++){
  const d=dropState[i];d.y-=dt*d.speed;d.x+=dt*.3;
  let floor=(d.x>-6.88&&d.x<3.52&&d.z>-6.22&&d.z<1.76)?4.75:((d.x<4.94&&d.z<3.34)?.31:.075);
  if(d.y<floor){d.y=range(7.6,9.0);d.x=range(-8.94,8.94);}
  if(d.x>8.95)d.x=-8.94;const k=i*6;rainArray[k]=d.x;rainArray[k+1]=d.y;rainArray[k+2]=d.z;rainArray[k+3]=d.x-.012;rainArray[k+4]=d.y+d.length;rainArray[k+5]=d.z-.008;
 }
 rainGeo.attributes.position.needsUpdate=true;
 for(let i=0;i<rippleCount;i++){
  const r=rippleStates[i];const p=(time*r.speed+r.phase)%1,rr=.025+p*r.max,fade=Math.pow(1-p,2)*.65;
  for(let j=0;j<36;j++)for(let end=0;end<2;end++){
   const a=(j+end)/36*Math.PI*2,k=(i*72+j*2+end)*3;rippleVerts[k]=r.x+Math.cos(a)*rr;rippleVerts[k+1]=r.y;rippleVerts[k+2]=r.z+Math.sin(a)*rr;
   rippleColors[k]=fade*.55;rippleColors[k+1]=fade*.77;rippleColors[k+2]=fade*.82;
  }
 }
 rippleGeo.attributes.position.needsUpdate=true;rippleGeo.attributes.color.needsUpdate=true;
 for(const d of drips){const p=(time*d.speed+d.phase)%1;d.mesh.position.y=3.46-p*p*3.15;d.mesh.scale.y=1.4+p*4;d.mesh.material.opacity=.20+p*.45;}
 for(let i=0;i<streaks.length;i++){
  const d=streaks[i];const y=3.23-((time*d.speed+d.phase)%1)*2.24,k=i*6;
  if(d.side){glassRainPositions[k]=3.352;glassRainPositions[k+1]=y;glassRainPositions[k+2]=d.a;glassRainPositions[k+3]=3.352;glassRainPositions[k+4]=Math.max(.97,y-d.length);glassRainPositions[k+5]=d.a+.005;}
  else{glassRainPositions[k]=d.a;glassRainPositions[k+1]=y;glassRainPositions[k+2]=1.59;glassRainPositions[k+3]=d.a+.005;glassRainPositions[k+4]=Math.max(.97,y-d.length);glassRainPositions[k+5]=1.591;}
 }
 glassRainGeo.attributes.position.needsUpdate=true;
 // Unhurried doors open for a moment in a quiet, empty shop.
 const cycle=time%26;let openness=cycle<17?0:cycle<18.6?T.MathUtils.smoothstep(cycle,17,18.6):cycle<21.4?1:cycle<23?1-T.MathUtils.smoothstep(cycle,21.4,23):0;
 doorLeft.position.x=.84-openness*.72;doorRight.position.x=1.64+openness*.72;
 if(Math.abs(openness-lastShadowDoor)>.12){renderer.shadowMap.needsUpdate=true;lastShadowDoor=openness;}
 const flicker=1-.018*Math.sin(time*2.6)-((time%19.7)>19.54?.17:0);sign.material.color.setScalar(flicker);
 const signalPhase=Math.floor(time/13)%3;for(let i=0;i<3;i++)signals[i].material.color.setHex(i===signalPhase?[0x70d5b6,0xe1bb65,0xcd7d75][i]:[0x345b53,0x5a5135,0x5c3f45][i]);
 roadReflector.material.uniforms.uTime.value=time;postMaterial.uniforms.uTime.value=time;splashMat.opacity=.27+Math.sin(time*2.2)*.07;
 renderer.info.reset();renderer.info.autoReset=false;renderer.setRenderTarget(renderTarget);renderer.setClearColor(0x152536,0);renderer.clear();renderer.render(scene,camera);renderer.setRenderTarget(null);renderer.setClearColor(0x152536,0);renderer.clear();renderer.render(postScene,postCamera);
 if(time-last>1){last=time;window.__rainyCorner={ready:true,frames,time:Number(time.toFixed(2)),objects:world.children.length,drawCalls:renderer.info.render.calls,camera:{theta,phi,distance},door:openness,rain:dropCount,webgl:renderer.capabilities.isWebGL2?'WebGL2':'WebGL1'};}
}
animate();
})();