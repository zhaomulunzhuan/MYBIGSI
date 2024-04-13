package com.chen;

import java.io.IOException;
import java.util.List;

public class main {
    public static void main(String[] args) throws IOException {
        String filePath = "D:\\Code\\Idea_Codes\\BIGSI_FILE\\BIGSI_inputfiles.txt";

        List<String> inputFiles = Utils.readInputFiles(filePath);
//
//        int bloomfilterSize=76938883;
//        int numHashs=3;

        int bloomfilterSize=59645240;
        int numHashs=7;
        //构建
//        long startBuild=System.currentTimeMillis();
//
//        Index.BuildIndex(bloomfilterSize,numHashs,inputFiles);
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

        //查询
        long startquery=System.currentTimeMillis();

//        List<String> result_sampels=index.querykmer("AAAGAGACCGGCGATTCTAGTGAAATCGAAC");
//        index.querykmerbycols("AAAGAGACCGGCGATTCTAGTGAAATCGAAC");
        Index.queryFile("D:\\Code\\Idea_Codes\\BIGSI_FILE\\query.txt");

        long endquery=System.currentTimeMillis();
        long queryTime=endquery-startquery;
        System.out.println("查询时间"+queryTime+"毫秒");

//        //插入
//        long startinsert=System.currentTimeMillis();
//
//        index.insert("D:\\SequenceSearch_2\\kmersDatasets\\GCF_000005825.2_ASM582v2_genomic.fna");
//
//        long endinsert=System.currentTimeMillis();
//        long insertTime=endinsert-startinsert;
//        System.out.println("插入时间"+insertTime+"毫秒");
//
//
//        long startquery2=System.currentTimeMillis();
//        List<String> result_sampels2=index.querykmer("TTATTACACCTCCCTGAGGATACTCTTCTAA");
//        long endquery2=System.currentTimeMillis();
//        long queryTime2=endquery2-startquery2;
//        System.out.println("查询时间"+queryTime2+"毫秒");




    }
}
