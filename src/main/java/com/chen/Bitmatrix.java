package com.chen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

public class Bitmatrix implements Serializable {
    // 实现 Serializable 接口
    private static final long serialVersionUID = 1L;
    private int numRows;
    private int numCols;
    private List<BitSet> bitarraysAsRow;


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
    }

    public void set_row(int row_index,BitSet bitarray){
        bitarraysAsRow.set(row_index, bitarray);
    }
    public List<BitSet> get_rows(int[] row_indexs){
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


    public void insertColumn(BloomFilter bloomFilter) {//非常慢
        BitSet bitArray=bloomFilter.getBitSet();
        int columnIndex=numCols;
        // Insert the new bit array at the specified column index
        for (int row_index=0;row_index<numRows;row_index++) {
            BitSet bitarray=bitarraysAsRow.get(row_index);
            bitarray.set(columnIndex,bitArray.get(row_index));
        }
        numCols++;
    }



}
