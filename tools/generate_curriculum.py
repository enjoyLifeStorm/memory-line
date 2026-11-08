from pathlib import Path
import json,re,unicodedata
# Local editable glyph coordinates: 200x240; the four-line canvas adds horizontal padding.
lower={
'a':['M 55 95 C 40 65, 0 80, 5 125 C 10 175, 55 170, 55 125','M 55 80 L 55 160'],
'b':['M 5 30 L 5 160','M 5 105 C 25 65, 65 80, 60 125 C 55 170, 20 170, 5 150'],
'c':['M 60 90 C 0 55, -5 165, 60 150'],
'd':['M 55 95 C 30 65, 0 85, 5 125 C 10 175, 55 170, 55 120','M 55 30 L 55 160'],
'e':['M 5 118 L 60 118 C 60 65, 0 65, 5 125 C 10 170, 45 170, 60 150'],
'f':['M 55 35 C 30 10, 20 35, 20 70 L 20 160','M 0 85 L 50 85'],
'g':['M 55 95 C 35 65, 0 80, 5 125 C 10 170, 55 170, 55 120','M 55 80 L 55 185 C 55 230, 10 230, 5 210'],
'h':['M 5 30 L 5 160','M 5 110 C 25 65, 60 80, 60 115 L 60 160'],
'i':['M 30 82 L 30 160','M 30 48 L 30 50'],
'j':['M 45 82 L 45 190 C 45 225, 10 230, 0 210','M 45 48 L 45 50'],
'k':['M 5 30 L 5 160','M 60 80 L 5 125 L 60 160'],
'l':['M 30 30 L 30 160'],
'm':['M 0 80 L 0 160','M 0 110 C 15 65, 30 80, 30 110 L 30 160','M 30 110 C 45 65, 60 80, 60 110 L 60 160'],
'n':['M 5 80 L 5 160','M 5 110 C 25 65, 60 80, 60 115 L 60 160'],
'o':['M 35 80 C -5 65, -5 170, 35 160 C 75 170, 75 65, 35 80'],
'p':['M 5 80 L 5 220','M 5 105 C 30 65, 65 80, 60 125 C 55 175, 20 165, 5 150'],
'q':['M 55 95 C 30 65, 0 85, 5 125 C 10 175, 55 165, 55 120','M 55 80 L 55 220'],
'r':['M 5 80 L 5 160','M 5 110 C 20 80, 40 75, 55 90'],
's':['M 55 90 C 15 60, -10 100, 30 120 C 85 145, 30 185, 0 150'],
' t'.strip():['M 30 40 L 30 135 C 30 165, 45 170, 60 150','M 5 85 L 55 85'],
'u':['M 5 80 L 5 130 C 5 170, 55 175, 55 125','M 55 80 L 55 160'],
'v':['M 0 80 L 30 160 L 60 80'],
'w':['M 0 80 L 15 160 L 30 105 L 45 160 L 60 80'],
'x':['M 0 80 L 60 160','M 60 80 L 0 160'],
'y':['M 0 80 L 30 150','M 60 80 L 15 220'],
'z':['M 0 80 L 60 80 L 0 160 L 60 160']}
lower['ü']=lower['u']+['M 15 48 L 15 50','M 45 48 L 45 50']
upper={
'A':['M 0 160 L 30 30 L 60 160','M 12 110 L 48 110'],
'B':['M 0 30 L 0 160','M 0 30 C 70 15, 70 100, 0 95 C 80 80, 80 175, 0 160'],
'C':['M 60 40 C -15 -5, -15 195, 60 150'],
'D':['M 0 30 L 0 160','M 0 30 C 85 15, 85 175, 0 160'],
'E':['M 0 30 L 0 160','M 0 30 L 60 30','M 0 95 L 50 95','M 0 160 L 60 160'],
'F':['M 0 30 L 0 160','M 0 30 L 60 30','M 0 95 L 50 95'],
'G':['M 60 45 C -10 -10, -20 190, 60 150 L 60 100 L 35 100'],
'H':['M 0 30 L 0 160','M 60 30 L 60 160','M 0 95 L 60 95'],
'I':['M 0 30 L 60 30','M 30 30 L 30 160','M 0 160 L 60 160'],
'J':['M 60 30 L 60 120 C 60 180, 0 180, 0 135'],
'K':['M 0 30 L 0 160','M 60 30 L 0 100 L 60 160'],
'L':['M 0 30 L 0 160 L 60 160'],
'M':['M 0 160 L 0 30 L 30 105 L 60 30 L 60 160'],
'N':['M 0 160 L 0 30 L 60 160 L 60 30'],
'O':['M 30 30 C -10 15, -10 175, 30 160 C 75 175, 75 15, 30 30'],
'P':['M 0 30 L 0 160','M 0 30 C 75 15, 75 110, 0 100'],
'Q':['M 30 30 C -10 15, -10 175, 30 160 C 75 175, 75 15, 30 30','M 35 135 L 70 175'],
'R':['M 0 30 L 0 160','M 0 30 C 75 15, 75 110, 0 100 L 65 160'],
'S':['M 60 40 C 10 0, -20 70, 30 95 C 95 130, 40 200, 0 150'],
'T':['M 0 30 L 60 30','M 30 30 L 30 160'],
'U':['M 0 30 L 0 120 C 0 180, 60 180, 60 120 L 60 30'],
'V':['M 0 30 L 30 160 L 60 30'],
'W':['M 0 30 L 15 160 L 30 85 L 45 160 L 60 30'],
'X':['M 0 30 L 60 160','M 60 30 L 0 160'],
'Y':['M 0 30 L 30 95 L 60 30','M 30 95 L 30 160'],
'Z':['M 0 30 L 60 30 L 0 160 L 60 160']}

def transform(path,offset,scale=1):
    i=0
    def replace(match):
        nonlocal i
        value=float(match.group());result=offset+value*scale if i%2==0 else value
        i+=1;return f'{result:.2f}'.rstrip('0').rstrip('.')
    return re.sub(r'-?\d+(?:\.\d+)?',replace,path)

def latin_paths(text):
    glyphs=[]
    for raw in text:
        nfd=unicodedata.normalize('NFD',raw)
        base=nfd[0]
        if '\u0308' in nfd:base='ü'
        paths=list((upper if base.isupper() else lower)[base])
        tones=[ch for ch in nfd if ch in '\u0304\u0301\u030c\u0300']
        if tones:
            if base=='i':paths=paths[:1]
            tone=tones[0]
            paths.append({'\u0304':'M 10 50 L 50 50','\u0301':'M 10 55 L 50 35','\u030c':'M 10 35 L 30 55 L 50 35','\u0300':'M 10 35 L 50 55'}[tone])
            if base=='ü':
                axis=[0]
                def lift(match):
                    value=float(match.group());axis[0]+=1
                    return str(int(value-25 if axis[0]%2==0 else value))
                paths[-1]=re.sub(r'-?\d+(?:\.\d+)?',lift,paths[-1])
        glyphs.append(paths)
    width=len(glyphs)*75-15;scale=min(1,160/width);start=(200-width*scale)/2
    return [transform(path,start+i*75*scale,scale) for i,paths in enumerate(glyphs) for path in paths]

cards=[]
def add(value,category,lesson,level,order,pinyin,chant,paths,reading=None):
    cards.append(dict(characterVal=value,category=category,lessonTitle=lesson,learningLevel=level,learningOrder=order,
        pinyin=pinyin,chant=chant,strokeCount=len(paths),strokePaths=json.dumps(paths,ensure_ascii=False),
        strokeHints=json.dumps(['观察起笔与收笔，按演示顺序写第%d笔。'%(i+1) for i in range(len(paths))],ensure_ascii=False),
        gridType='MI_ZI_GE' if category=='HANZI' else 'FOUR_LINE',readingText=reading or value))
# Each subject has an independent, explicit learning order, never Unicode or alphabetical sorting.
pinyin_order=0
for value,example in zip(['a','o','e','i','u','ü'],['啊','喔','鹅','衣','乌','鱼']):
    pinyin_order+=1;add(value,'PINYIN','01 单韵母入门',1,pinyin_order,value,'单韵母：先看口型，再听示例字“'+example+'”。',latin_paths(value),example)
for base,values,examples in [('a','ā á ǎ à','阿 拔 把 爸'),('o','ō ó ǒ ò','喔 佛 我 卧'),('e','ē é ě è','喝 鹅 可 饿'),('i','ī í ǐ ì','衣 姨 椅 意'),('u','ū ú ǔ ù','乌 无 五 物'),('ü','ǖ ǘ ǚ ǜ','迂 鱼 雨 玉')]:
    for tone,(value,example) in enumerate(zip(values.split(),examples.split()),1):
        pinyin_order+=1;add(value,'PINYIN','02 四声辨读',1,pinyin_order,value,['一声平','二声扬','三声拐弯','四声降'][tone-1]+'；听示例字“'+example+'”辨认声调（示例可能带声母）。',latin_paths(value),example)
initial_groups=[('03 声母 b p m f','b p m f','玻 坡 摸 佛',2),('04 声母 d t n l','d t n l','得 特 呢 勒',2),('05 声母 g k h','g k h','哥 科 喝',2),('06 声母 j q x','j q x','鸡 七 西',2),('07 平舌与翘舌','z c s zh ch sh r','资 刺 丝 知 吃 诗 日',3),('08 y w 入门','y w','衣 乌',3)]
for lesson,values,examples,level in initial_groups:
    for value,example in zip(values.split(),examples.split()):
        pinyin_order+=1;add(value,'PINYIN',lesson,level,pinyin_order,value,'声母读得轻而短；示例字“'+example+'”辅助辨音。',latin_paths(value),example)
for lesson,values,examples,level in [
('09 复韵母 ai ei ui','ai ei ui','挨 杯 归',3),('10 复韵母 ao ou iu','ao ou iu','奥 欧 秋',3),('11 复韵母 ie üe er','ie üe er','叶 月 耳',3),
('12 前鼻韵母','an en in un ün','安 恩 因 温 云',4),('13 后鼻韵母','ang eng ing ong','昂 灯 英 工',4),
('14 整体认读音节','zhi chi shi ri zi ci si yi wu yu ye yue yuan yin yun ying','知 吃 诗 日 字 次 丝 衣 乌 鱼 叶 月 圆 音 云 英',4)]:
    for value,example in zip(values.split(),examples.split()):
        pinyin_order+=1;add(value,'PINYIN',lesson,level,pinyin_order,value,'整体认读，不拆开拼。示例字：'+example if lesson.startswith('14') else '从前一个音滑向后一个音，听示例字“'+example+'”辅助辨音。',latin_paths(value),example)
assert pinyin_order==87,pinyin_order
# Hanzi: preserve authentic upstream stroke order; map centerlines to the writing grid.
entries=json.loads(Path(__file__).with_name('curriculum-hanzi-plan.json').read_text(encoding='utf-8'))
order=0
for unit in range(1,6):
    group=[c for c in entries if c['unit']==unit]
    group.sort(key=lambda c:len(json.loads(Path('src/main/resources/static/hanzi-data',c['characterVal']+'.json').read_text(encoding='utf-8'))['medians']))
    for c in group:
        order+=1;data=json.loads(Path('src/main/resources/static/hanzi-data',c['characterVal']+'.json').read_text(encoding='utf-8'))
        paths=['M '+' L '.join(f'{20+x/1024*200:.2f} {20+(900-y)/1024*200:.2f}' for x,y in median) for median in data['medians']]
        add(c['characterVal'],'HANZI',f'{unit:02d} '+c['lessonTitle'],[1,2,2,3,4][unit-1],order,c['pinyin'],'先认读“'+c['characterVal']+'”，再观察'+str(len(paths))+'笔的先后顺序；每个单元从较少笔画逐步练起。',paths)
letter_order=0
for lesson,letters,level in [('01 直线字形入门','LTIHEF',1),('02 斜线组合','VXYAKMNWZ',2),('03 曲线字形','COUSJ',3),('04 直曲结合','DPBRGQ',4)]:
    for value in letters:
        letter_order+=1;pair=value+value.lower();add(pair,'LETTER',lesson,level,letter_order,pair,'大写占上、中格；小写观察上伸与下伸。先大写、后小写，跟随轨迹书写。',latin_paths(pair),value)
assert letter_order==26 and len({c['characterVal'] for c in cards if c['category']=='LETTER'})==26
Path('src/main/resources/curriculum.json').write_text(json.dumps(cards,ensure_ascii=False,indent=2),encoding='utf-8')
print('Curriculum: 87 pinyin (including 24 tone cards), 100 hanzi, 26 uppercase/lowercase pairs; all have local stroke paths')
