package com.idx.smartspeakdock.music.util;


import com.idx.smartspeakdock.music.entity.Music;

import java.util.HashMap;


/**
 * Created by sunny on 18-1-4.
 */

//获取音乐数据，将数据存储在HaspMap中
public class MusicUtil {
    public static HashMap<String,Music> getMusic(){
        HashMap<String,Music> musicHashMap=new HashMap<String,Music>();

        String[] nameArray={"虫鸣","半月琴","星语星愿","飘雪","迴梦游仙","一剪梅","倩女幽魂","小星星",
                "雨夜","海边漫步","琵琶语","高山流水","春江花月夜","海浪","大冲浪","大河","流水",
                "急流","鸟鸣","阳关三叠","笑傲江湖","梅花三弄","蛙鸣","永远只有你","二重唱","下雨"};
        String[] urlArray={"https://s3.amazonaws.com/sqa-skills-source-file/Music/chongming1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/dizi1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/dizi2.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/dizi3.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/erhu1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/erhu2.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/erhu3.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/gangqin1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/gangqin2.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/gangqin3.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/guzheng1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/guzheng2.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/guzheng3.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/hailang1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/hailang2.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/liushui1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/liushui2.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/liushui3.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/niaoming1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/qinxiao1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/qinxiao2.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/qinxiao3.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/waming1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/xiaotiqin1.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/xiaotiqin2.mp3",
                "https://s3.amazonaws.com/sqa-skills-source-file/Music/xiaoyu1.mp3"};
       long[] dataArray={137064,303700,204983,237087,127086,279118,204539,277290,
                            178416,165956,254851,318198,563357,120059,130064,
                            854674,952895, 339670,824137, 335177,216607,600085,
                            90044,316447,230504,1094844};
            for (int i=0;i<nameArray.length;i++) {
                Music music=new Music();
                music.setTitle(nameArray[i]);
                music.setUrl(urlArray[i]);
                music.setDuration(dataArray[i]);
                musicHashMap.put(nameArray[i],music);
            }
            return musicHashMap;
    }

}
