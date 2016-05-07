package com.word2vect;

import com.Word2VEC;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;
import com.hankcs.textrank.TextRankKeyword;

import java.io.*;
import java.util.*;

/**
 * Created by tantairs on 16/4/27.
 */
public class EmotibotTest {

    /**
     * 存储编辑距离的数组
     */
    int[][] editArray;

    boolean INPUT_QUESTION_FLAG = true;

    Word2VEC word2VEC = new Word2VEC();

    final int TOP_N = 200;

    public static void main(String[] args) throws IOException {

        EmotibotTest emotibotTest = new EmotibotTest();
        emotibotTest.runProgram();
    }


    /**
     * @throws IOException
     */
    public void runProgram() throws IOException {

        word2VEC.loadJavaModel("vector.mod");
        System.out.println("------加载完成------");

        TextRankKeyword textRankKeyword = new TextRankKeyword();

        Map<String, String> mapObject = new HashMap<>();//存放通过编辑距离过滤的 quetion 的关键词和相应的问答<keyword,answer>
        Map<String, String> mapSource = new HashMap<>();//存放一开始读入csv文件的数据,为总的问答句对<question,answer>
        SortedMap<Double, String> map = new TreeMap<>();//存放词向量距离的map <number,keyword>
        SortedMap<String, Integer> filterByEditDistanceMap = new TreeMap<>();//暂存编辑距离的questions 和其与目标question的距离<qustion,number>
        Map<String, Integer> newMap = new LinkedHashMap();//存放最终的TOP50的编辑距离处理后的问答句

        //处理小影个人信息,将信息构造成<句子向量,回答>
        try {
            File file = new File("/Users/tantairs/Desktop/小影个人信息.csv");
            InputStreamReader readerInfo = new InputStreamReader(new FileInputStream(file), "gbk");
            BufferedReader bufferedReader = new BufferedReader(readerInfo);
            String line = null;
            while ((line = bufferedReader.readLine()) != null) {
                String[] sentence = line.split(";");
                mapSource.put(sentence[0].trim(), sentence[1].trim());
            }

            while (true) {
                INPUT_QUESTION_FLAG = true;
                System.out.println("输入问题：");
                Scanner scanner = new Scanner(System.in);
                String str = scanner.nextLine();

                /**
                 * 下面需要调用方法来判断用户输入的句子是否为基本句型,如果满足则进行回答,反之,则过滤掉该句子
                 *
                 */
                if (doJudgeBasicSentence(str)) {
                    //输入的问题满足对小影个人信息的提问,进行处理

                    /**
                     * 第一步,先判断输入句子与内部设定的quetions的编辑距离,得出Top_N
                     */
                    Set<String> setTest = mapSource.keySet();
                    for (String s : setTest) {
                        int temp = calculateDistance(str, s);
                        filterByEditDistanceMap.put(s, temp);
                    }

                    ArrayList<Map.Entry<String, Integer>> list = new ArrayList<>(filterByEditDistanceMap.entrySet());
                    Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                        @Override
                        public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                            return o1.getValue() - o2.getValue();
                        }
                    });

                    //获取编辑距离TOP_N 近的句子
                    for (int i = 0; i < TOP_N; i++) {
                        newMap.put(list.get(i).getKey(), list.get(i).getValue());
                        System.out.println(list.get(i).getKey() + " : " + list.get(i).getValue());
                    }

                    /**
                     * 第二步,在Top_N个候选quetions中提取关键词,找出与输入句子的关键词最近的句子
                     */
                    //获取输入句子的关键词的词向量,这里写成一个函数,如果这里出现了异常,则只能利用编辑距离来预测了.
                    float[] keywordObject = null;
                    try {
                        String keyword = textRankKeyword.getKeyword("", str);
                        String aa = keyword.replace('#', ' ');
                        keywordObject = word2VEC.getWordVector(aa.trim());
                    } catch (Exception e) {
                        INPUT_QUESTION_FLAG = false;
                    }

                    //如果用户输入的句子不能提取关键词.
                    if (INPUT_QUESTION_FLAG) {
                        /**
                         * 下面这个循环里面在提取关键词的时候会有问题
                         */
                        Set<String> setEditDistance = newMap.keySet();
                        for (String s : setEditDistance) {
                            try {
                                String ss = textRankKeyword.getKeyword("", s);
                                String keyED = ss.replace('#', ' ');
                                mapObject.put(keyED.trim(), mapSource.get(s));
                            } catch (Exception e) {
                                continue;
                            }

                        }

                        Set<String> set = mapObject.keySet();
                        for (String s : set) {
                            double sumTmp = 0.0;
                            double modA = 0.0;
                            double modB = 0.0;
                            try {
                                float[] s_set = word2VEC.getWordVector(s);

                                for (int i = 0; i < 200; i++) {
                                    modA += keywordObject[i] * keywordObject[i];
                                    modB += s_set[i] * s_set[i];
                                    sumTmp += keywordObject[i] * s_set[i];
                                }

                            } catch (Exception e) {
                                continue;
                            }
                            //计算余弦距离
                            modA = Math.sqrt(modA);
                            modB = Math.sqrt(modB);
                            sumTmp = sumTmp / (modA * modB);
                            map.put(sumTmp, s);
                        }

                        //下面对结果进行处理
                        String finalAnswer = null;
                        String objectwordtemp = map.get(map.lastKey());
                        String objectSentence = mapObject.get(objectwordtemp);
                        System.out.println("词向量最小: " + objectSentence);
                        finalAnswer = objectSentence;
                        int distance = list.get(0).getValue();
                        if (distance <= 1) {
                            finalAnswer = mapSource.get(list.get(0).getKey());
                        }
                        System.out.println("distance < 2后的回答: " + finalAnswer);

                    } else {
                        System.out.println("直接通过编辑距离来回答");
                        System.out.println("回答: " + mapSource.get(list.get(0).getKey()));
                    }


                } else {
                    //转到其它地方处理
                    System.out.println("你输入的问题不满足对小影个人情况的提问,将转到其他地方进行处理");
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 输入两个词,利用词向量来计算出这两个词的余弦距离
     *
     * @param str1
     * @param str2
     * @return
     */
    public double calculateConsine(String str1, String str2) {
        double sumTmp = 0.0;
        double modA = 0.0;
        double modB = 0.0;
        String[] str1_U = str1.split("/");
        String[] str2_U = str2.split("/");

        try {
            float[] s_set = word2VEC.getWordVector(str1_U[0]);
            float[] keywordObject = word2VEC.getWordVector(str2_U[0]);

            for (int i = 0; i < 200; i++) {
                modA += keywordObject[i] * keywordObject[i];
                modB += s_set[i] * s_set[i];
                sumTmp += keywordObject[i] * s_set[i];
            }

        } catch (Exception e) {
//            System.out.println("词向量中找不到该词 :" + str2_U[0]);
        }

        //计算余弦距离
        modA = Math.sqrt(modA);
        modB = Math.sqrt(modB);
        sumTmp = sumTmp / (modA * modB);
        return sumTmp;
    }

    /**
     * 计算两个字符串的最小编辑距离 delete = 1, insert = 1, substitute = 1
     * 状态转移方程: D(i,j) = min{D(i-1,j)+1,D(i,j-1)+1,D(i-1,j-1)+str1[i]==str2[j] ? 0 : 1}
     * 这里将对字的运算改成词,以符合这边的需求.并且代价根据不同的词向量距离来赋值.
     *
     * @param str1 "o f a i l i n g"
     * @param str2 "o s a i l n"
     * @return 两个字符串的最小编辑距离 4
     */
    public int doEditDistance(String[] str1, String[] str2) {
        int length1 = str1.length;
        int length2 = str2.length;
        int count = 1;
        editArray = new int[length2 + 1][length1 + 1];
        editArray[0][0] = 0;
        for (int i = 0; i <= length1; i++) {
            editArray[0][i] = i;
        }
        for (int j = 0; j <= length2; j++) {
            editArray[j][0] = j;
        }
        for (int i = 1; i <= length2; i++) {
            for (int z = 1; z <= length1; z++) {

                if (!str1[z - 1].equals(str2[i - 1])) {
                    double tempValue = calculateConsine(str1[z - 1], str2[i - 1]);
                    if (tempValue < 0.1) {
                        count = 14;
                    } else if (tempValue >= 0.1 && tempValue <= 0.2) {
                        count = 12;
                    }
                    if (tempValue > 0.2 && tempValue <= 0.3) {
                        count = 10;
                    }
                    if (tempValue > 0.3 && tempValue <= 0.4) {
                        count = 8;
                    }
                    if (tempValue > 0.4 && tempValue <= 0.5) {
                        count = 6;
                    }
                    if (tempValue > 0.5 && tempValue <= 0.6) {
                        count = 4;
                    }
                    if (tempValue > 0.6 && tempValue <= 0.7) {
                        count = 4;
                    }
                    if (tempValue > 0.7 && tempValue <= 0.9) {
                        count = 1;
                    }
                    if (tempValue > 0.9) {
                        count = 0;
                    }

                }
                editArray[i][z] = min(editArray[i][z - 1] + 1, editArray[i - 1][z] + 1, editArray[i - 1][z - 1] + (str1[z - 1].equals(str2[i - 1]) ? 0 : count));
            }
        }
        return editArray[length2 - 1][length1 - 1];
    }

    /**
     * 调用上面的编辑距离方法,求出两个句子的编辑距离
     *
     * @param str1
     * @param str2
     * @return 返回编辑距离
     */
    public int calculateDistance(String str1, String str2) {
        List<Term> termList1 = StandardTokenizer.segment(str1);
        List<Term> termList2 = StandardTokenizer.segment(str2);
        int size1 = termList1.size();
        int size2 = termList2.size();
        int count1 = 0;
        int count2 = 0;
        String[] temp1 = new String[size1];
        String[] temp2 = new String[size2];
        Iterator<Term> iterator1 = termList1.iterator();
        while (iterator1.hasNext()) {
            temp1[count1] = iterator1.next().toString();
            count1++;
        }
        Iterator<Term> iterator2 = termList2.iterator();
        while (iterator2.hasNext()) {
            temp2[count2] = iterator2.next().toString();
            count2++;
        }
        return doEditDistance(temp1, temp2);
    }

    /**求出三个数的最小数
     *
     * @param a
     * @param b
     * @param c
     * @return
     */
    public int min(int a, int b, int c) {
        if (a < b) {
            if (a < c) {
                return a;
            } else {
                return c;
            }
        } else {
            if (b < c) {
                return b;
            } else {
                return c;
            }
        }

    }

    /**
     * 判断一个输入的句子是否是对小影的个人信息进行提问并需要回答的.
     *
     * @param str
     * @return
     */
    public boolean doJudgeBasicSentence(String str) {
        //这里逻辑判断有问题,需要更改
        if (str.startsWith("你") || str.startsWith("所以你") || str.startsWith("那你") || str.startsWith("难道你"))
            return true;
        if (str.contains("你") && str.contains("会")) {
            return true;
        }
        if (str.contains("你") && str.contains("谁")) {
            return true;
        }
        if (str.contains("你") && str.contains("能")) {
            return true;
        }
        else {
            return false;
        }

    }

    /**
     * 打印编辑距离的数组,用于检测和查看.
     */
    public void print() {
        for (int i = 0; i < editArray.length; i++) {
            for (int j = 0; j < editArray[i].length; j++) {
                System.out.print(editArray[i][j] + " ");
            }
            System.out.println();
        }
    }


}
