package com.chen;

import java.io.IOException;
import java.util.List;

public class main {
    public static void main(String[] args) throws IOException {
        //每一行是一个kmer数据集的地址
        String filePath = "D:\\Code\\Idea_Codes\\BIGSI_FILE\\BIGSI_inputfiles.txt";

        List<String> inputFiles = Utils.readInputFiles(filePath);

        //根据最大基数确定布隆过滤器大小
        int b=6456730;//最大基数
        float FPR= 0.01F;//误报率
        int k=7;
        int bloomfilterSize= (int) (-1 * (k * b) / Math.log(1 - Math.pow(FPR, 1.0 / k)));

        //构建
//        long startBuild=System.currentTimeMillis();
//
//        Index.BuildIndex(bloomfilterSize,k,inputFiles);
//
//        long endBuild=System.currentTimeMillis();
//        long BuildTime=(endBuild-startBuild)/ 1000;
//        System.out.println("构建时间"+BuildTime+"秒");
//
//        Utils.serializeAll();


        //反序列化
        long startBuild=System.currentTimeMillis();
        Index.BulidFromFile();
        long endBuild=System.currentTimeMillis();
        long BuildTime=(endBuild-startBuild)/ 1000;
        System.out.println("反序列化构建时间"+BuildTime+"秒");

        //按行查询
        long startquery=System.currentTimeMillis();

        Index.queryFile("D:\\Code\\Idea_Codes\\BIGSI_FILE\\query.txt");

        long endquery=System.currentTimeMillis();
        long queryTime=endquery-startquery;
        System.out.println("查询时间"+queryTime+"毫秒");

        //按列查询
        long startqueryAScol=System.currentTimeMillis();

        Index.queryFileAScol("D:\\Code\\Idea_Codes\\BIGSI_FILE\\query.txt");

        long endqueryAScol=System.currentTimeMillis();
        long queryTimeAScol=endqueryAScol-startqueryAScol;
        System.out.println("按列查询时间"+queryTimeAScol+"毫秒");

//        //插入
//        long startinsert=System.currentTimeMillis();
//
//        index.insert("D:\\SequenceSearch_2\\kmersDatasets\\GCF_000005825.2_ASM582v2_genomic.fna");
//
//        long endinsert=System.currentTimeMillis();
//        long insertTime=endinsert-startinsert;
//        System.out.println("插入时间"+insertTime+"毫秒");





    }
}
