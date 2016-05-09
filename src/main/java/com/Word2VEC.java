package com;

import com.word2vect.domain.WordEntry;

import java.io.*;
import java.util.*;
import java.util.Map.Entry;

public class Word2VEC {

	public static void main(String[] args) throws IOException {

//		com.Learn learn = new com.Learn();
//		learn.learnFile(new File("/Users/tantairs/Downloads/SogouCA/resultbig.txt"));
//		learn.saveModel(new File("assert/vector.mod"));


		Word2VEC vec = new Word2VEC();
		vec.loadJavaModel("vector.mod");

		// System.out.println("中国" + "\t" +
		// Arrays.toString(vec.getWordVector("中国")));
		// ;
		// System.out.println("毛泽东" + "\t" +
		// Arrays.toString(vec.getWordVector("毛泽东")));
		// ;
		// System.out.println("足球" + "\t" +
		// Arrays.toString(vec.getWordVector("足球")));

		// com.Word2VEC vec2 = new com.Word2VEC();
		// vec2.loadGoogleModel("library/vectors.bin") ;
		//

		//
//        List<String> list = new ArrayList<>();
//        list.add("你");
//        list.add("是");
//        list.add("什么");
////        list.add("星座");
//        List<String> list2 = new ArrayList<>();
//        list2.add("你");
//        list2.add("的");
//        list2.add("生日");
////        list2.add("是");
////        list2.add("哪天");
//        List<String> list3 = new ArrayList<>();
//        list3.add("你");
//        list3.add("的");
//        list3.add("生日");
//        list3.add("是");
////        list3.add("哪天");
////		String str = "爱情";
////		long start = System.currentTimeMillis();
////		for (int i = 0; i < 100; i++) {
//        float[] a = vec.getVector((ArrayList)list);
//        float[] aa = a.clone();
//        for(int i = 0; i < a.length; i++){
//            System.out.println(aa[i]);
//        }
//        float[] b = vec.getVector((ArrayList)list2);
//        float[] bb = b.clone();
//        for(int i = 0; i < a.length; i++){
//            System.out.println(aa[i] + " : "+bb[i]);
//        }
        System.out.println("---------------------");
//        float[] c = vec.getWordVector("哪天");
////        for(int i = 0; i < a.length; i++){
////            System.out.println(c[i]);
////        }
//        System.out.println("---------------------");
        float[] a1 = vec.getWordVector("生日");
//        float[] b1 = vec.getVector((ArrayList)l3);
        System.out.println(a1.length);
//        for(int i = 0; i < a.length; i++){
//            System.out.println(a1[i] + " : "+ b[i]+" : " +b1[i]);
//        }
//      System.out.println(Arrays.equals(vec.getWordVector("土豆"),vec.getWordVector("马铃薯")));
//		System.out.println(Arrays.equals(vec.getVector((ArrayList)list), vec.getVector((ArrayList)list2)));

			;
//		}
//		System.out.println(System.currentTimeMillis() - start);

//		System.out.println(System.currentTimeMillis() - start);
		// System.out.println(vec2.distance(str));
		//
		//
		// //男人 国王 女人
//		System.out.println(vec.analogy("女优", "av", "范冰冰"));
		// System.out.println(vec2.analogy("毛泽东", "毛泽东思想", "邓小平"));
	}

	private HashMap<String, float[]> wordMap = new HashMap<String, float[]>();

	private int words;
	private int size;
	private int topNSize = 40;

	/**
	 * 加载模型
	 * 
	 * @param path
	 *            模型的路径
	 * @throws java.io.IOException
	 */
	public void loadGoogleModel(String path) throws IOException {
		DataInputStream dis = null;
		BufferedInputStream bis = null;
		double len = 0;
		float vector = 0;
		try {
			bis = new BufferedInputStream(new FileInputStream(path));
			dis = new DataInputStream(bis);
			// //读取词数
			words = Integer.parseInt(readString(dis));
			// //大小
			size = Integer.parseInt(readString(dis));
			String word;
			float[] vectors = null;
			for (int i = 0; i < words; i++) {
				word = readString(dis);
				vectors = new float[size];
				len = 0;
				for (int j = 0; j < size; j++) {
					vector = readFloat(dis);
					len += vector * vector;
					vectors[j] = (float) vector;
				}
				len = Math.sqrt(len);

				for (int j = 0; j < size; j++) {
					vectors[j] /= len;
				}

				wordMap.put(word, vectors);
				dis.read();
			}
		} finally {
			bis.close();
			dis.close();
		}
	}

	/**
	 * 加载模型
	 *
	 * @param path
	 *            模型的路径
	 * @throws java.io.IOException
	 */
	public void loadJavaModel(String path) throws IOException {
        System.out.println("加载中---------");
		try (DataInputStream dis = new DataInputStream(new BufferedInputStream(new FileInputStream(path)))) {
			words = dis.readInt();
			size = dis.readInt();

			float vector = 0;

			String key = null;
			float[] value = null;
			for (int i = 0; i < words; i++) {
				double len = 0;
				key = dis.readUTF();
				value = new float[size];
				for (int j = 0; j < size; j++) {
					vector = dis.readFloat();
					len += vector * vector;
					value[j] = vector;
				}

				len = Math.sqrt(len);

				for (int j = 0; j < size; j++) {
					value[j] /= len;
				}
				wordMap.put(key, value);
			}

		}
	}

	private static final int MAX_SIZE = 50;

	/**
	 * 近义词
	 *
	 * @return
	 */
	public TreeSet<WordEntry> analogy(String word0, String word1, String word2) {
		float[] wv0 = getWordVector(word0);
		float[] wv1 = getWordVector(word1);
		float[] wv2 = getWordVector(word2);

		if (wv1 == null || wv2 == null || wv0 == null) {
			return null;
		}
		float[] wordVector = new float[size];
		for (int i = 0; i < size; i++) {
			wordVector[i] = wv1[i] - wv0[i] + wv2[i];
		}
		float[] tempVector;
		String name;
		List<WordEntry> wordEntrys = new ArrayList<WordEntry>(topNSize);
		for (Entry<String, float[]> entry : wordMap.entrySet()) {
			name = entry.getKey();
			if (name.equals(word0) || name.equals(word1) || name.equals(word2)) {
				continue;
			}
			float dist = 0;
			tempVector = entry.getValue();
			for (int i = 0; i < wordVector.length; i++) {
				dist += wordVector[i] * tempVector[i];
			}
			insertTopN(name, dist, wordEntrys);
		}
		return new TreeSet<WordEntry>(wordEntrys);
	}

	private void insertTopN(String name, float score, List<WordEntry> wordsEntrys) {
		// TODO Auto-generated method stub
		if (wordsEntrys.size() < topNSize) {
			wordsEntrys.add(new WordEntry(name, score));
			return;
		}
		float min = Float.MAX_VALUE;
		int minOffe = 0;
		for (int i = 0; i < topNSize; i++) {
			WordEntry wordEntry = wordsEntrys.get(i);
			if (min > wordEntry.score) {
				min = wordEntry.score;
				minOffe = i;
			}
		}

		if (score > min) {
			wordsEntrys.set(minOffe, new WordEntry(name, score));
		}

	}

	public Set<WordEntry> distance(String queryWord) {

		float[] center = wordMap.get(queryWord);
		if (center == null) {
			return Collections.emptySet();
		}

        //test
//        for(int i = 0; i < center.length; i++)
//            System.out.println(center[i]);

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		double min = Float.MIN_VALUE;
		for (Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();

		return result;
	}

	public Set<WordEntry> distance(List<String> words) {

		float[] center = null;
		for (String word : words) {
			center = sum(center, wordMap.get(word));
		}

		if (center == null) {
			return Collections.emptySet();
		}

		int resultSize = wordMap.size() < topNSize ? wordMap.size() : topNSize;
		TreeSet<WordEntry> result = new TreeSet<WordEntry>();

		double min = Float.MIN_VALUE;
		for (Entry<String, float[]> entry : wordMap.entrySet()) {
			float[] vector = entry.getValue();
			float dist = 0;
			for (int i = 0; i < vector.length; i++) {
				dist += center[i] * vector[i];
			}

			if (dist > min) {
				result.add(new WordEntry(entry.getKey(), dist));
				if (resultSize < result.size()) {
					result.pollLast();
				}
				min = result.last().score;
			}
		}
		result.pollFirst();

		return result;
	}

    public float[] getVector(ArrayList<String> words){

        float[] center = null;
//        for(int i = 0; i < 10; i++){
//            System.out.print(wordMap.get("我"));
//        }
        int count = 0;

        Iterator<String> iterator = words.iterator();
        while (iterator.hasNext()){
            String word = iterator.next();
            float[] temp = wordMap.get(word);
//            System.out.print(temp);
            center = sum(center, wordMap.get(word));
        }
//        System.out.println("计算了 " + count+" 次");
//        int length = center.length;
//        float sum = 0.0f;
//        for(int i = 0; i < length; i++){
//            sum += center[i];
//        }
//        for(int i = 0; i < length; i++){
//            center[i] = center[i]/sum;
//            System.out.print(center[i]+" : ");
//        }
        return center;
    }

	private float[] sum(float[] center, float[] fs) {
		// TODO Auto-generated method stub

		if (center == null && fs == null) {
			return null;
		}

		if (fs == null) {
			return center;
		}

		if (center == null) {
			return fs;
		}

		for (int i = 0; i < fs.length; i++) {
			center[i] += fs[i];
		}

		return center;
	}

	/**
	 * 得到词向量
	 *
	 * @param word
	 * @return
	 */
	public float[] getWordVector(String word) {
		return wordMap.get(word);
	}

	public static float readFloat(InputStream is) throws IOException {
		byte[] bytes = new byte[4];
		is.read(bytes);
        System.out.println(getFloat(bytes));
		return getFloat(bytes);
	}

	/**
	 * 读取一个float
	 *
	 * @param b
	 * @return
	 */
	public static float getFloat(byte[] b) {
		int accum = 0;
		accum = accum | (b[0] & 0xff) << 0;
		accum = accum | (b[1] & 0xff) << 8;
		accum = accum | (b[2] & 0xff) << 16;
		accum = accum | (b[3] & 0xff) << 24;
		return Float.intBitsToFloat(accum);
	}

	/**
	 * 读取一个字符串
	 *
	 * @param dis
	 * @return
	 * @throws java.io.IOException
	 */
	private static String readString(DataInputStream dis) throws IOException {
		// TODO Auto-generated method stub
		byte[] bytes = new byte[MAX_SIZE];
		byte b = dis.readByte();
		int i = -1;
		StringBuilder sb = new StringBuilder();
		while (b != 32 && b != 10) {
			i++;
			bytes[i] = b;
			b = dis.readByte();
			if (i == 49) {
				sb.append(new String(bytes));
				i = -1;
				bytes = new byte[MAX_SIZE];
			}
		}
		sb.append(new String(bytes, 0, i + 1));
		return sb.toString();
	}

	public int getTopNSize() {
		return topNSize;
	}

	public void setTopNSize(int topNSize) {
		this.topNSize = topNSize;
	}

	public HashMap<String, float[]> getWordMap() {
		return wordMap;
	}

	public int getWords() {
		return words;
	}

	public int getSize() {
		return size;
	}

}
