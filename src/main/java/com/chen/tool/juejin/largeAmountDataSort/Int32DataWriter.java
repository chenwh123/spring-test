package com.chen.tool.juejin.largeAmountDataSort;


import cn.hutool.core.collection.ListUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author chenwh3
 */
@Slf4j
public class Int32DataWriter {

    private static final int BYTE_1M = 1024 * 1024;
    private static final int BYTE_10M = BYTE_1M * 10;
    private static final int BYTE_100M = BYTE_1M * 100;

    public static String filePath = "./output.bin";

    public static void buildNInt32(int count, String filePath) {
        Random random = new Random(0);
        try (BufferedOutputStream dos = new BufferedOutputStream(Files.newOutputStream(Paths.get(filePath)), BYTE_10M)) { // 100M输出缓冲
            while (count-- > 0) {
                int i = random.nextInt();
                byte[] array = ByteBuffer.allocate(4).putInt(i).array();
                dos.write(array);
            }
            dos.flush();
        } catch (IOException e) {
            log.error("");
        }
    }

    /**
     * @param len 初始长度
     */
    public static List<Integer> readInt32(String filePath, int len) {
        len = len <= 0 ? 16 : len;
        List<Integer> list = new ArrayList<>(len);
        byte[] bytes = new byte[4];
        try (BufferedInputStream bis = new BufferedInputStream(Files.newInputStream(Paths.get(filePath)), BYTE_10M)) { // 100M输入缓冲
            while (bis.read(bytes) > 0) {
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