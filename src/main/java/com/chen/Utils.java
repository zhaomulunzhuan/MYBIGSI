package com.chen;
import com.google.common.hash.Hashing;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Utils {
    public static int hash(String element, int seed, int m) {
        // 返回一个哈希值，该哈希值被限制在范围 [0, m) 内
        return Math.abs(Hashing.murmur3_32(seed).hashUnencodedChars(element).asInt()) % m;
    }

    public static int[] generateHashes(String element, int numberHashFunctions, int bloomFilterSize) {//元素，哈希函数个数，布隆过滤器大小
        int[] hashes = new int[numberHashFunctions];
        for (int seed = 0; seed < numberHashFunctions; seed++) {
            hashes[seed] = hash(element, seed, bloomFilterSize);
        }
        return hashes;
    }

    public static List<String> readInputFiles(String filePath) throws IOException {
        List<String> inputFiles = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                inputFiles.add(line);
            }
        }

        return inputFiles;
    }
    public static List<String> getKmers(String filepath) throws IOException {
        List<String> keys = new ArrayList<>();
        BufferedReader br = new BufferedReader(new FileReader(filepath));
        String line;
        while ((line = br.readLine()) != null) {
            keys.add(line);
        }
        br.close();
        return keys;
    }

    public static BitSet loadBitArray(String filePath) throws IOException {//从一个文件中加载布隆过滤器
        try (DataInputStream inputStream = new DataInputStream(new FileInputStream(filePath))) {
            byte[] byteArray = new byte[inputStream.available()];
            inputStream.readFully(byteArray);
            BitSet bitSet = BitSet.valueOf(byteArray);
            return bitSet;
        }
    }

    //序列化index和matadata
    public static void serializeAll() throws IOException {
        //元数据序列化
        String MetaDataFile="D:/Code/Idea_Codes/BIGSI_FILE/serializeFIle"+"/"+"metadata.ser";
        Metadata.serialize(MetaDataFile);
        //索引序列化，即将块信息和布隆过滤器信息序列化
        String indexFile="D:/Code/Idea_Codes/BIGSI_FILE/serializeFIle"+"/"+"index.ser";
        Index.serialize(indexFile);
//        String indexFile="D:/Code/Idea_Codes/BIGSI_FILE/serializeFIle"+"/";
//        Index.serialize(indexFile);
    }

}
