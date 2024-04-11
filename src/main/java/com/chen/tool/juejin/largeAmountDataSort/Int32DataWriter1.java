package com.chen.tool.juejin.largeAmountDataSort;


import cn.hutool.core.collection.ListUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author chenwh3
 */
@Slf4j
public class Int32DataWriter1 {

    private static final int BYTE_1M = 1024 * 1024;
    private static final int BYTE_10M = BYTE_1M * 10;
    private static final int BYTE_100M = BYTE_1M * 100;

    public static String filePath = "./output.bin";

    public static void buildNInt32(int count, String filePath) {
        Random random = new Random(0);
        try (FileChannel channel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ, StandardOpenOption.WRITE , StandardOpenOption.TRUNCATE_EXISTING , StandardOpenOption.CREATE)) {
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_WRITE, 0, count * 4);
            while (count-- > 0) {
                int i = random.nextInt();
                byte[] array = ByteBuffer.allocate(4).putInt(i).array();
                map.put(array);
            }
        } catch (IOException e) {
            log.error("", e);
        }
    }

    /**
     * @param len 初始长度
     */
    public static List<Integer> readInt32(String filePath, int len) {
        len = len <= 0 ? 16 : len;
        List<Integer> list = new ArrayList<>(len);
        byte[] bytes = new byte[4];

        try (FileChannel channel = FileChannel.open(Paths.get(filePath), StandardOpenOption.READ)) {
            MappedByteBuffer map = channel.map(FileChannel.MapMode.READ_ONLY, 0, len * 4);
            while (map.hasRemaining()) {
                map.get(bytes);
                list.add(ByteBuffer.wrap(bytes).getInt());
            }
        } catch (Exception e) {
            log.error("", e);
        }
        return list;
    }
    public static void main(String[] args) {
        int int32Num = (int) Math.pow(10, 8); // 10亿个int32整数 = 4gb
        long m1 = System.currentTimeMillis();
        buildNInt32(int32Num, filePath);
        long m2 = System.currentTimeMillis();
        List<Integer> list = readInt32(filePath, int32Num);
        long m3 = System.currentTimeMillis();
        System.out.println(ListUtil.sub(list, 0, 10));
        System.out.println(list.size());

        System.out.println("write cost: " + (m2 - m1) + "ms");
        System.out.println("read cost: " + (m3 - m2) + "ms");

//        int val = new Random(0).nextInt();
//        System.out.println(Integer.toString(val, 16));
//        System.out.println(new Random(0).nextInt());

    }
}