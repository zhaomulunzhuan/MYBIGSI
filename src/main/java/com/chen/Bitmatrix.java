package com.chen;

import java.io.*;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Bitmatrix implements Serializable {
    // 实现 Serializable 接口
    private static final long serialVersionUID = 1L;
    private static int numRows;
    private static int numCols;
    private static List<BitSet> bitarraysAsRow;//将布隆过滤器矩阵转换为按行存储，即每个布隆过滤器中相同索引的bit连续存储

    public Bitmatrix(List<BitSet> rows,int nums_rows,int nums_cols){
        bitarraysAsRow=rows;
        numRows=nums_rows;
        numCols=nums_cols;
    }

    public List<BitSet> getBitarraysAsRow(){
        return bitarraysAsRow;
    }

    public int get_numCols(){
        return numCols;
    }

    public BitSet get_row(int row_index){
        return bitarraysAsRow.get(row_index);
    }//根据行索引获得对应的行，即获取哈希到的一行

    public void set_row(int row_index,BitSet bitarray){
        bitarraysAsRow.set(row_index, bitarray);
    }//设置一行
    public List<BitSet> get_rows(int[] row_indexs){//获得一些行
        List<BitSet> bitarrays=new ArrayList<>();
        for(int row_index:row_indexs){
            bitarrays.add(get_row(row_index));
        }
        return bitarrays;
    }

    public void set_rows(List<Integer> row_indexs,List<BitSet> bitarrays){
        for(Integer row_index:row_indexs){
            set_row(row_index,bitarrays.get(row_index));
        }
    }

    public BitSet getColumn(int columnIndex) {//获取位矩阵中指定列(column_index)的位数组(bitarray)
        BitSet columnBits = new BitSet(bitarraysAsRow.size());
        for (int i = 0; i < bitarraysAsRow.size(); i++) {//每一行逐一获取指定列的位，非常慢
            BitSet rowBits = bitarraysAsRow.get(i);
            if (rowBits.get(columnIndex)) {
                columnBits.set(i);
            }
        }
        return columnBits;
    }

    public List<BitSet> getColumns(List<Integer> columnIndexes) {
        List<BitSet> columns = new ArrayList<>();
        for (int columnIndex : columnIndexes) {
            columns.add(getColumn(columnIndex));
        }
        return columns;
    }


    public void insertColumn(BloomFilter bloomFilter) {//非常慢 每行一bit一bit添加
        BitSet bitArray=bloomFilter.getBitSet();
        int columnIndex=numCols;
        // Insert the new bit array at the specified column index
        for (int row_index=0;row_index<numRows;row_index++) {
            BitSet bitarray=bitarraysAsRow.get(row_index);
            bitarray.set(columnIndex,bitArray.get(row_index));
        }
        numCols++;
    }

    // 序列化成员函数
    public void serialize(String filename) {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filename))) {
            out.writeInt(numRows);
            out.writeInt(numCols);
            out.writeObject(bitarraysAsRow);
            System.out.println("Bitmatrix 对象已成功序列化到 " + filename + " 文件中");
        } catch (IOException e) {
            System.err.println("序列化失败：" + e.getMessage());
        }
    }

    // 反序列化成员函数
    public static Bitmatrix deserialize(String filename) {
        Bitmatrix bitmatrix = null;
        try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filename))) {
            int numRows = in.readInt();
            int numCols = in.readInt();
            List<BitSet> bitarraysAsRow = (List<BitSet>) in.readObject();
            bitmatrix = new Bitmatrix(bitarraysAsRow, numRows, numCols);
            System.out.println("Bitmatrix 对象已成功从 " + filename + " 文件中反序列化");
        } catch (IOException | ClassNotFoundException e) {
            System.err.println("反序列化失败：" + e.getMessage());
        }
        return bitmatrix;
    }

}
