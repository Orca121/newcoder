package com.newcoder.community.util;

import org.apache.commons.lang3.CharUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@Component

public class SensitiveFilter {

    private static final Logger logger = LoggerFactory.getLogger(SensitiveFilter.class);

    //替换符
    private static final String REPLACEMENT = "***";

    //根节点
    private TrieNode rootNode = new TrieNode();

    //前缀树结点（内部类）
    private class TrieNode{

        //关键词结束标识
        private boolean isKeywordEnd = false;

        //子节点（key是下级字符，value是下级节点）
        private Map<Character,TrieNode> subNodes = new HashMap<>();

        public boolean isKeywordEnd() {
            return isKeywordEnd;
        }

        public void setKeywordEnd(boolean keywordEnd) {
            isKeywordEnd = keywordEnd;
        }

        //添加子节点
        public void addSubNodes(Character c, TrieNode node) {
            subNodes.put(c, node);
        }

        //获取子节点
        public TrieNode getSubNodes(Character c) {
            return subNodes.get(c);
        }
    }

    //前缀树初始化，在构造器之后
    @PostConstruct
    public void init(){
        try(
                InputStream is = this.getClass().getClassLoader().getResourceAsStream("sensitive-words.txt");
                BufferedReader reader = new BufferedReader((new InputStreamReader(is)));
                ) {
            String keyword;
            while((keyword = reader.readLine()) != null){
                //添加到前缀树
                this.addKeyword(keyword);
            }
        } catch (IOException e) {
            logger.error("加载敏感词文件失败" + e.getMessage());
        }
    }

    //将一个敏感词添加到前缀树中
    private void addKeyword(String keyword){
        TrieNode tempNode = rootNode;
        for (int i = 0; i < keyword.length(); i++) {
            char c = keyword.charAt(i);
            TrieNode subNode = tempNode.getSubNodes(c);

            if(subNode == null){
                //初始化节点
                subNode = new TrieNode();
                tempNode.addSubNodes(c,subNode);
            }

            //指向子节点，进入下一轮循环
            tempNode = subNode;

            //设置结束标识
            if(i == keyword.length()-1){
                tempNode.setKeywordEnd(true);
            }
        }
    }

    // 判断是否为符号
    private boolean isSymbol(Character c) {
        // 0x2E80~0x9FFF 是东亚文字范围
        return !CharUtils.isAsciiAlphanumeric(c) && (c < 0x2E80 || c > 0x9FFF);
    }

    /**
     * 过滤敏感词
     *
     * @param text 待过滤的文本
     * @return 过滤后的文本
     */

    public String filter(String text){
        //空值处理
        if(StringUtils.isBlank(text)){
            return null;
        }

        //指针1
        TrieNode tempNode = rootNode;
        //指针2
        int left = 0;
        //指针3
        int right = 0;
        //结果
        StringBuilder sb = new StringBuilder();

        while(right < text.length()) {
            char c = text.charAt(right);

            //跳过符号
            if (isSymbol(c)) {
                //若指针1处于根节点，将此符号计入结果，让指针2向下走一步
                if (tempNode == rootNode) {
                    sb.append(c);
                    left++;
                }
                //无论符号在开头或中间，指针3都向下走一步
                right++;
                continue;
            }

            //检查下级节点
            tempNode = tempNode.getSubNodes(c);
            if (tempNode == null) {
                //以left开头的字符串不是敏感词
                sb.append(text.charAt(left));
                //进入下一位置
                right = ++left;
                //重新指向根节点
                tempNode = rootNode;
            } else if (tempNode.isKeywordEnd()) {
                //发现敏感词，将left~right字符串替换掉
                sb.append(REPLACEMENT);
                //进入下一个位置
                left = ++right;
                //重新指向根节点
                tempNode = rootNode;
            } else {
                // 检查下一个字符
                right++;
            }

            //检查一个敏感词是另一个敏感词子集的情况，如fabc和abc
            // 提前判断postion是不是到达结尾，要跳出while,如果是，则说明begin-position这个区间不是敏感词，但是里面不一定没有
            if (right == text.length() && left != right) {
                // 说明还剩下一段需要判断，则把position==++right
                // 并且当前的区间的开头字符是合法的
                sb.append(text.charAt(left));
                right = ++left;
                tempNode = rootNode;  // 前缀表从头开始了
            }
        }
        return sb.toString();
    }
}
