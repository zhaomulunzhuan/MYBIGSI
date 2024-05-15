package com.chen;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Index implements Serializable{
    // 实现 Serializable 接口
    private static final long serialVersionUID = 1L;
    private static List<BloomFilter> bloomFilterList;
    private static Bitmatrix bitmatrix;

    private void setBitmatrix(Bitmatrix bm) {//用于反序列化
        bitmatrix=bm;
    }

    public static void BuildIndex(int bloomfilter_size, int k, List<String> input_file) throws IOException {//原始构建
        Metadata.set_bloomfilter_size(bloomfilter_size);
        Metadata.set_num_hashs(k);
        bloomFilterList=new ArrayList<>();
        for(int i=0;i<input_file.size();i++){
            //获取文件名，不包含扩展名
            Path inputPath = Paths.get(input_file.get(i));
            // 获取文件名（包含扩展名）
            String fileNameWithExtension = inputPath.getFileName().toString();
            // 获取不包含扩展名的文件名
            String fileName = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));

            if (Metadata.sampleToColour.containsKey(fileName)){
                System.err.println(fileName + " already in BIGSI index!");
            }else {
                Metadata.add_sample(fileName);
                List<String> kmers=Utils.getKmers(String.valueOf(inputPath));//得到输入文件的kmers列表
                bloomFilterList.add(new BloomFilter(bloomfilter_size,k));//创建一个布隆过滤器
                BloomFilter bloomFilter=bloomFilterList.get(Metadata.getIndexBySample(fileName));
                for(String kmer:kmers){
                    bloomFilter.add(kmer);
                }
            }
        }
        List<BitSet> transposedBitSets=transposeBloomFilters(bloomFilterList);
        int row_num=Metadata.getBloomfilterSize();
        int col_num=bloomFilterList.size();
        bitmatrix=new Bitmatrix(transposedBitSets,row_num,col_num);

//        bloomFilterList=null;

    }

    public static List<BitSet> transposeBloomFilters(List<BloomFilter> bloomFilters) {//如果一个布隆过滤器是一列，这里就是按列存储转换为按行存储
        int numCols=bloomFilters.size();
        int numRows=Metadata.getBloomfilterSize();
//        System.out.println("行数"+numRows);
//        System.out.println("列数"+numCols);
        List<BitSet> transposedBitSets = new ArrayList<>();
        for(int i=0;i<numRows;i++){
            BitSet transposedBitSet = new BitSet(numCols);
            for(int j=0;j<numCols;j++){
                boolean bit=bloomFilters.get(j).getBitSet().get(i);
                transposedBitSet.set(j,bit);
            }
            transposedBitSets.add(transposedBitSet);
        }

        return transposedBitSets;
    }

    public static void BulidFromFile(){//反序列化构建，即从文件中加载索引
        String MetaDataFile="D:/Code/Idea_Codes/BIGSI_FILE/serializeFIle"+"/"+"metadata.ser";
        Metadata.deserialize(MetaDataFile);
//        MetaData.outputMetadata();
        String indexFile="D:/Code/Idea_Codes/BIGSI_FILE/serializeFIle"+"/"+"index.ser";
        Index.deserialize(indexFile);
//        String indexFileAsrow="D:/Code/Idea_Codes/BIGSI_FILE/serializeFIle"+"/"+"indexAsrow.ser";
//        Index.deserializeBitmatrix(indexFileAsrow);
    }

    //按列查询
    public static List<String> querykmerAScol(String kmer){//按布隆过滤器查询
        int[] indexes = Utils.generateHashes(kmer,Metadata.getNumHashs(),Metadata.getBloomfilterSize());//对应的k个哈希值，即行索引
        List<String> result_sampels=new ArrayList<>();
        for(int i=0;i<Metadata.num_samples();i++){
            BloomFilter bloomFilter=bloomFilterList.get(i);
            if(bloomFilter.test(indexes)){
                result_sampels.add(Metadata.getSampleByIndex(i));
            }
        }
        return result_sampels;
    }

    public static void querySequenceAScol(String sequence, BufferedWriter writer) throws IOException {//查找长序列，每个kmer都存在才报告序列存在
        int kmersize=31;//根据数据集kmer长度简单写死
        List<String> kmerList=new ArrayList<>();
        // 切割sequence并将长度为kmersize的子字符串加入kmerList
        for (int i = 0; i <= sequence.length() - kmersize; i++) {
            String kmer = sequence.substring(i, i + kmersize);
            kmerList.add(kmer);
        }

        List<String> result=new ArrayList<>(querykmerAScol(kmerList.get(0)));
        for(String kmer:kmerList){
            result.retainAll(querykmerAScol(kmer));
        }

        writer.write("查询结果\n");
        // 将查询结果写入到结果文件
        if (!result.isEmpty()){
            for (String datasetName : result) {
                writer.write(datasetName + "\n");
            }
        }else {
            writer.write("未查询到包含查询序列的数据集"+"\n");
        }

    }

    public static void queryFileAScol(String filePath){//一个文件中有多个查询长序列，查询每一个并把查询结果写入输出文件
        String queryresultFile = "D:/Code/Idea_Codes/BIGSI_FILE"+"/"+"query_result(col).txt";//存放查询结果
        try(
                BufferedReader reader=new BufferedReader(new FileReader(filePath));
                BufferedWriter writer=new BufferedWriter(new FileWriter(queryresultFile))){
            String line;
            String sequence="";
            while ((line=reader.readLine())!=null){
                if(line.startsWith(">")){
                    //查询
                    if (!sequence.isEmpty()){
                        writer.write(sequence+"\n");
                        querySequenceAScol(sequence,writer);
                        writer.write(line+"\n");
                    }else {
                        writer.write(line+"\n");
                    }
                    sequence="";
                }else {
                    sequence+=line.trim().toUpperCase();
                }
            }
            if(!sequence.isEmpty()){
                writer.write(sequence + "\n");
                //查询最后一段序列
                querySequenceAScol(sequence,writer);
            }
        }catch (IOException e){
            System.err.println(e);
        }
    }


    public static BitSet performAndOperation(List<BitSet> bitSets) {//位数组按位与
        BitSet result = new BitSet(Metadata.num_samples());
        result.set(0, Metadata.num_samples()); // 将 BitSet 中的所有位设置为 true
        if (!bitSets.isEmpty()) {
            // 从第二个 BitSet 开始，依次与 result 进行按位与操作
            for (int i = 0; i < bitSets.size(); i++) {
                result.and(bitSets.get(i));
            }
        }
        return result;
    }

    //按行查询
    public static List<String> querykmer(String kmer){//查询一个kmer
        int[] indexes = Utils.generateHashes(kmer,Metadata.getNumHashs(),Metadata.getBloomfilterSize());//对应的k个哈希值，即行索引
        List<BitSet> bitSets=bitmatrix.get_rows(indexes);

        BitSet result = performAndOperation(bitSets);
        List<String> result_sampels=new ArrayList<>();

        int numBits = result.length();
        for (int i = 0; i < numBits; i++) {
            // 获取指定索引处的位的值
            boolean bitValue = result.get(i);
            // 输出位的值以及索引
            if (bitValue){
                result_sampels.add(Metadata.getSampleByIndex(i));
            }
        }

        return result_sampels;
    }

    public static void querySequence(String sequence, BufferedWriter writer) throws IOException {//查找长序列，每个kmer都存在才报告序列存在
        int kmersize=31;//根据数据集kmer长度简单写死
        List<String> kmerList=new ArrayList<>();
        // 切割sequence并将长度为kmersize的子字符串加入kmerList
        for (int i = 0; i <= sequence.length() - kmersize; i++) {
            String kmer = sequence.substring(i, i + kmersize);
            kmerList.add(kmer);
        }

        List<String> result=new ArrayList<>(querykmer(kmerList.get(0)));
        for(String kmer:kmerList){
            result.retainAll(querykmer(kmer));
        }

        writer.write("查询结果\n");
        // 将查询结果写入到结果文件
        if (!result.isEmpty()){
            for (String datasetName : result) {
                writer.write(datasetName + "\n");
            }
        }else {
            writer.write("未查询到包含查询序列的数据集"+"\n");
        }

    }

    public static void queryFile(String filePath){//一个文件中有多个查询长序列，查询每一个并把查询结果写入输出文件
        String queryresultFile = "D:/Code/Idea_Codes/BIGSI_FILE"+"/"+"query_result.txt";//存放查询结果
        try(
                BufferedReader reader=new BufferedReader(new FileReader(filePath));
                BufferedWriter writer=new BufferedWriter(new FileWriter(queryresultFile))){
            String line;
            String sequence="";
            while ((line=reader.readLine())!=null){
                if(line.startsWith(">")){
                    //查询
                    if (!sequence.isEmpty()){
                        writer.write(sequence+"\n");
                        querySequence(sequence,writer);
                        writer.write(line+"\n");
                    }else {
                        writer.write(line+"\n");
                    }
                    sequence="";
                }else {
                    sequence+=line.trim().toUpperCase();
                }
            }
            if(!sequence.isEmpty()){
                writer.write(sequence + "\n");
                //查询最后一段序列
                querySequence(sequence,writer);
            }
        }catch (IOException e){
            System.err.println(e);
        }
    }

    public static void insert(String datasetpath) throws IOException {//此时bloomFilterList已经销毁，只剩下按行存储的bitmatrix
        Path insertpath=Paths.get(datasetpath);
        // 获取文件名（包含扩展名）
        String fileNameWithExtension = insertpath.getFileName().toString();
        // 获取不包含扩展名的文件名
        String fileName = fileNameWithExtension.substring(0, fileNameWithExtension.lastIndexOf('.'));

        Metadata.add_sample(fileName);

        BloomFilter filter=new BloomFilter(Metadata.getBloomfilterSize(),Metadata.getNumHashs());

        List<String> kmers=Utils.getKmers(String.valueOf(insertpath));
        for(String kmer:kmers){
            filter.add(kmer);
        }

        bitmatrix.insertColumn(filter);

    }

    // 序列化成员函数
    public static void serialize(String filename) {
        bitmatrix.serialize(filename);

        String indexBFlist="D:/Code/Idea_Codes/BIGSI_FILE/serializeFIle"+"/"+"indexAscol.ser";
        serializeBFlist(indexBFlist);
//        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
////            out.writeObject(bloomFilterList);
////            out.writeObject(bitmatrix); // 序列化 bitmatrix
//            bitmatrix.serialize(filename);
//            System.out.println("Index 对象已成功序列化到 " + filename + " 文件中");
//        } catch (IOException e) {
//            System.err.println("序列化失败：" + e.getMessage());
//        }
    }

//    public static void serialize(String filename) {
//        serializeBitmatrix(filename);
//        serializeBFlist(filename);
//    }
//
//    public static void serializeBitmatrix(String filename){
//        String indexAsrowFile=filename+"indexAsrow.ser";
//        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(indexAsrowFile))) {
//            out.writeObject(bitmatrix);
//            System.out.println("bitmatrix 对象已成功序列化到 " + filename + " 文件中");
//        } catch (IOException e) {
//            System.err.println("bitmatrix 对象序列化失败：" + e.getMessage());
//        }
//    }
//
    public static void serializeBFlist(String filename){
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeObject(bloomFilterList);
            System.out.println("布隆过滤器列表 对象已成功序列化到 " + filename + " 文件中");
        } catch (IOException e) {
            System.err.println("序列化失败：" + e.getMessage());
        }
    }

    // 反序列化成员函数
    public static void deserialize(String filename) {
        bitmatrix= Bitmatrix.deserialize(filename);
        String indexBFlist="D:/Code/Idea_Codes/BIGSI_FILE/serializeFIle"+"/"+"indexAscol.ser";
        deserializeBFlist(indexBFlist);

    }

    public static void deserializeBFlist(String filename){
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            bloomFilterList = (List<BloomFilter>) in.readObject();
            System.out.println("布隆过滤器列表 已成功从 " + filename + " 文件中反序列化");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("反序列化失败：" + e.getMessage());
        }
    }
//
//
//    public static void deserializeBitmatrix(String filename){
//        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
//            bitmatrix = (Bitmatrix) in.readObject(); // 反序列化 bitmatrix
//            System.out.println("bitmatrix 已成功从 " + filename + " 文件中反序列化");
//        } catch (IOException | ClassNotFoundException e) {
//            System.err.println("反序列化失败：" + e.getMessage());
//        }
//    }


}
