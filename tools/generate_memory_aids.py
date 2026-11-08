from pathlib import Path
import json
library={c['characterVal'] for c in json.loads(Path('src/main/resources/curriculum.json').read_text(encoding='utf-8')) if c['category']=='HANZI'}
assets={
'tree':'<path d="M110 133V55M110 91L78 66M110 82L141 53"/><path fill="#b8dfbd" d="M63 64Q49 20 91 26Q110 2 131 26Q171 20 161 66Q143 90 112 77Q81 92 63 64Z"/><path d="M110 133V56M110 91L78 66M110 82L141 53"/>',
'sun':'<circle cx="110" cy="78" r="31" fill="#ffd47e"/><path d="M110 22V10M110 146V134M54 78H42M178 78H166M70 38L61 29M150 118L159 127M70 118L61 127M150 38L159 29"/>',
'moon':'<path fill="#ffd47e" d="M135 18A62 62 0 1 0 157 119A58 58 0 0 1 135 18Z"/>',
'mountain':'<path fill="#abd3b9" d="M25 132L71 59L94 96L124 26L193 132Z"/><path d="M109 59L124 26L143 55"/>',
'water':'<path stroke="#67a8d2" d="M102 15C169 38 36 61 104 82S171 116 100 145M134 16C201 40 68 63 136 84S203 118 132 145"/>',
'fire':'<path fill="#ffb387" d="M105 13C131 60 170 67 153 111C140 147 77 149 65 108C59 84 81 68 83 46L104 77C121 59 111 34 105 13Z"/>',
'field':'<path fill="#d7e9b1" d="M35 32H185V133H35Z"/><path d="M110 32V133M35 82H185"/>',
'earth':'<path fill="#dfc1a0" d="M30 105Q80 89 110 105T190 105V145H30Z"/><path d="M110 102V45M110 68Q74 66 78 37Q111 38 110 68M110 59Q140 56 146 29Q111 28 110 59"/>',
'mouth':'<path fill="#ffccc7" d="M47 73Q110 34 173 73Q112 145 47 73Z"/><path d="M48 73Q111 94 173 73"/>',
'eye':'<path fill="white" d="M29 78Q108 9 191 78Q110 148 29 78Z"/><circle cx="110" cy="78" r="25" fill="#b9d7d6"/><circle cx="110" cy="78" r="10" fill="#35554d"/>',
'person':'<circle cx="110" cy="31" r="16" fill="#ffe0b9"/><path d="M110 48V94M110 60L77 83M110 60L143 83M110 94L81 139M110 94L139 139"/>',
'hand':'<path fill="#ffe0b9" d="M79 137L55 85Q51 70 63 72L80 93V37Q80 22 91 27V75V21Q94 10 103 21V73V28Q110 17 116 30V77V43Q123 31 130 45V98L146 84Q160 77 162 89L142 131Q122 150 79 137Z"/>',
'ear':'<path fill="#ffe0b9" d="M142 104Q174 77 151 38Q120 5 83 37Q64 59 76 82Q81 88 89 100Q91 137 113 140Q138 143 142 104Z"/><path d="M94 67Q96 35 129 48Q148 61 128 82Q108 91 115 110"/>',
'cloud':'<path fill="#e1edf4" d="M57 108C13 99 29 56 66 62C61 16 128 7 143 53C184 31 214 86 179 106Z"/>',
'rain':'<path fill="#e1edf4" d="M57 80C13 71 29 28 66 34C70 2 127 1 143 27C184 15 214 62 179 78Z"/><path stroke="#67a8d2" d="M65 100L57 121M111 100L103 121M156 100L148 121M82 127L75 145M133 127L126 145"/>',
'stone':'<path fill="#c8cece" d="M44 124L35 85L65 39L120 26L173 56L191 115L149 139L80 141Z"/><path d="M65 39L87 78L35 85M87 78L142 90L173 56M142 90L149 139"/>',
'grain':'<path d="M110 144V25M110 87L78 68M110 68L142 49M110 49L89 32"/><ellipse cx="77" cy="64" rx="18" ry="8" transform="rotate(35 77 64)" fill="#e5cd77"/><ellipse cx="143" cy="45" rx="18" ry="8" transform="rotate(-35 143 45)" fill="#e5cd77"/>',
'bamboo':'<path stroke="#79ad81" stroke-width="16" d="M87 144V18M131 144V34"/><path d="M80 48H94M80 87H94M124 70H138M124 112H138M132 69Q158 37 178 52M87 48Q59 24 40 40"/>',
'bird':'<ellipse cx="104" cy="89" rx="50" ry="33" fill="#f1d68d"/><circle cx="146" cy="59" r="25" fill="#f1d68d"/><path fill="#e5a66e" d="M170 53L194 63L170 70Z"/><path d="M60 98L30 79M97 122V141M125 122V141M86 82Q106 118 137 82"/><circle cx="153" cy="54" r="3" fill="#35554d"/>',
'bug':'<ellipse cx="110" cy="96" rx="33" ry="40" fill="#e3b596"/><circle cx="110" cy="48" r="20" fill="#b8dfbd"/><path d="M110 59V135M78 76L51 63M77 96H48M79 115L51 133M142 76L169 63M143 96H172M141 115L169 133M99 30L88 17M121 30L132 17"/>',
'door':'<path fill="#e7d0af" d="M57 142V21H163V142Z"/><path fill="#fffaf0" d="M72 142V36H148V142"/><circle cx="133" cy="93" r="4" fill="#35554d"/>',
'cow':'<path fill="#e7d0af" d="M68 54Q46 53 43 22Q68 25 79 46M143 46Q156 25 177 22Q175 54 152 55"/><path fill="#fffaf0" d="M70 50Q111 22 151 50L158 104Q150 141 110 141Q70 141 62 104Z"/><ellipse cx="110" cy="113" rx="35" ry="20" fill="#eab9a0"/><circle cx="89" cy="79" r="4"/><circle cx="132" cy="79" r="4"/>',
}
def svg(body): return '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 220 160" fill="none" stroke="#35554d" stroke-width="4" stroke-linecap="round" stroke-linejoin="round">'+body+'</svg>'
def icon(k): return assets[k]
data={}
def add(ch,key,story,structure):
 if ch in library:data['HANZI:'+ch]={'svg':svg(icon(key)),'story':story,'structure':structure,'alt':'帮助联想的生活图画'}
entries=[
('木','tree','小树有树干，也有向两边伸出的枝条。看树想木，再把木写端正。','独体字，先横、竖，再撇、捺。'),
('林','tree','两棵树并排站，树林的林记心间。左边的木让一让，最后一笔捺变点。','左右两个木，左边最后一笔是点。'),
('森','tree','上面一棵，下面两棵，三棵树一起帮助记住森林的森。','上面一个木，下面两个木。'),
('日','sun','太阳升起来，新的一日开始啦。看太阳，再记住日字中间的一横。','外框里面有一横，最后封口。'),
('月','moon','夜晚月亮出来了。看一看月亮，再记住月字里面两短横。','里面两短横，间距要均匀。'),
('山','mountain','远处三座山，中间那座高。写山时，中间的竖也高一些。','三条竖向的笔画，中间高，两边低。'),
('水','water','小河里的水向前流。写水时，留意中间竖钩和两边的笔画。','先写中间竖钩，再写左右笔画。'),
('火','fire','小火苗向上跳，联想两边的小点和撇，再把下面撇捺写舒展。','先点、撇，再撇、捺。'),
('田','field','一块田分成四小块。记住外框，里面一横一竖。','外框里面有十字，最后封口。'),
('土','earth','小苗扎根在泥土里。土地的土，上横短，下横长。','两横一竖，上横短，下横长。'),
('口','mouth','张开小嘴读一读，口字像个小方框。','三笔写成，最后一横封口。'),
('目','eye','用眼睛仔细看。目字里面两横，把长框分成三格。','长框里面有两横，最后封口。'),
('人','person','小朋友迈开两条腿。写人时，一撇一捺互相照应。','一撇一捺，两笔舒展。'),
('大','person','小朋友把手臂展开，联想一个大字。横要平，撇捺要舒展。','先横，再撇、捺。'),
('天','sun','抬头看天空。大字上面再加一横，帮助记住天空的天。','上面两横，再写撇、捺。'),
('手','hand','伸出自己的小手看一看。写手时，短撇、两横、弯钩依次写。','先撇，两横，最后弯钩。'),
('耳','ear','耳朵认真听。写耳时，看看里面两短横，长横从中穿过去。','上横先写，注意里面两短横。'),
('云','cloud','白云慢慢飘。记住上面两横，下面撇折和点。','上面两横，下面撇折和点。'),
('雨','rain','云下面落雨滴。写雨时，四个点分在里面左右两边。','外框里面一竖，两边各两点。'),
('石','stone','路边有块小石头。记住横和撇下面的小口。','上面横、撇，下面一个口。'),
('禾','grain','禾苗长高结谷粒。木字头上添一撇，帮助记住禾苗的禾。','先短撇，再横、竖、撇、捺。'),
('竹','bamboo','两根竹子并排长。记住两边相似，也留意最后一笔不同。','左右两部分，右边最后是竖钩。'),
('鸟','bird','小鸟有眼睛，也有尾巴。记住里面的小点，下面一横别漏掉。','里面一点，下面一横。'),
('虫','bug','小虫慢慢爬。虫字上面像小口，中间一竖向下伸。','口中穿一竖，下面提和点。'),
('门','door','打开家里的门。写门先点，再竖，最后横折钩。','先点，再竖，最后横折钩。'),
('牛','cow','小牛低头吃青草。写牛时，两横一竖要看清，中间竖要穿过去。','先短撇，两横，最后一竖。'),
]
for e in entries:add(*e)
if 'HANZI:大' in data:data['HANZI:大']['svg']=svg('<circle cx="110" cy="26" r="14" fill="#ffe0b9"/><path d="M110 42V91M62 57H158M110 91L75 140M110 91L145 140"/>')
# Multiple trees are drawn explicitly rather than reusing a single-tree scene.
for ch,positions in [('林',[(5,10,.85),(72,10,.85)]),('森',[(55,-2,.65),(4,57,.65),(104,57,.65)])]:
 if 'HANZI:'+ch in data:data['HANZI:'+ch]['svg']=svg(''.join(f'<g transform="translate({x} {y}) scale({scale})">{icon("tree")}</g>' for x,y,scale in positions))
for n,ch in [(1,'一'),(2,'二'),(3,'三'),(10,'十')]:
 if ch in library:
  body=''.join(f'<circle cx="{45+(i%5)*32}" cy="{57+(i//5)*47}" r="11" fill="#e7ba88"/>' for i in range(n))
  data['HANZI:'+ch]={'svg':svg(body),'story':f'数一数，有{n}颗小果子。再把对应的汉字写下来。','structure':{'一':'一横，从左向右写。','二':'两横，上短下长。','三':'三横，长短和间距要看清。','十':'先横后竖，交点在中间。'}[ch],'alt':'供数数联想的小果子'}
for n in range(10):
 body='<rect x="25" y="22" width="170" height="118" rx="20" fill="#fff5df"/>'
 body+=''.join(f'<circle cx="{55+(i%3)*55}" cy="{47+(i//3)*34}" r="11" fill="#edab8c"/>' for i in range(n))
 data['NUMBER:'+str(n)]={'svg':svg(body),'story':f'盘子里'+('一个果子也没有，用零来表示。' if n==0 else f'有{n}颗果子。用手点着数一遍，再写对应的数字。'),'structure':['像一个封闭的圈，记住起笔和收笔。','从上向下，写得稳稳的。','上面弯，斜着下，下面横。','上下两个弯，开口都朝左。','留意转折和直竖的位置。','上面转折，下面一个弯。','上面弯下来，下面围成圈。','先横，再向左下写。','上下两圈，留意交叉处。','上面一个圈，右边向下写。'][n],'alt':'盘子里的果子，可用手点数'}
Path('src/main/resources/static/memory-aids.json').write_text(json.dumps(data,ensure_ascii=False,indent=2),encoding='utf-8')
print('Memory aid cards:',len(data),'Hanzi:',sum(k.startswith('HANZI:') for k in data))
