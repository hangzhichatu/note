package longi.common.util;

import longi.common.util.DiffMatchPatch.Diff;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * 文本差异高亮算法
 *
 * @Author : cron
 * @Date: 2020-01-15
 */
@SuppressWarnings("all")
public class CompareTextHighLightUtil {

    /**
     * 传入2个字符串进行相比高亮显示
     * eg:
     * 原数据一:王五张三
     * 原数据二:张三李四
     * <span style='color:red'>王五</span>张三
     * 张三<span style='color:red'>李四</span>
     */

    public static String[] getHighLightDifferentOld(String a, String b) {
        String[] temp = getDiff(a, b);
        String[] result = {getHighLight(a, temp[0]), getHighLight(b, temp[1])};
        return result;
    }

    private static String getHighLight(String source, String temp) {
        StringBuffer sb = new StringBuffer();
        char[] sourceChars = source.toCharArray();
        char[] tempChars = temp.toCharArray();
        boolean flag = false;
        for (int i = 0; i < sourceChars.length; i++) {
            if (tempChars[i] != ' ') {
                if (i == 0) {
                    sb.append("<span style='background-color:red'>").append(sourceChars[i]);
                } else if (flag) {
                    sb.append(sourceChars[i]);
                } else {
                    sb.append("<span style='background-color:red'>").append(sourceChars[i]);
                }
                flag = true;
                if (i == sourceChars.length - 1) {
                    sb.append("</span>");
                }
            } else if (flag) {
                sb.append("</span>").append(sourceChars[i]);
                flag = false;
            } else {
                sb.append(sourceChars[i]);
            }
        }
        return sb.toString();
    }

    public static String[] getDiff(String a, String b) {
        String[] result = null;
        //选取长度较小的字符串用来穷举子串
        if (a.length() < b.length()) {
            result = getDiff(a, b, 0, a.length());
        } else {
            result = getDiff(b, a, 0, b.length());
            result = new String[]{result[1], result[0]};
        }
        return result;
    }

    /**
     * 将a的指定部分与b进行比较生成比对结果
     */
    private static String[] getDiff(String a, String b, int start, int end) {
        String[] result = new String[]{a, b};
        int len = result[0].length();
        while (len > 0) {
            for (int i = start; i < end - len + 1; i++) {
                String sub = result[0].substring(i, i + len);
                int idx = -1;
                if ((idx = result[1].indexOf(sub)) != -1) {
                    result[0] = setEmpty(result[0], i, i + len);
                    result[1] = setEmpty(result[1], idx, idx + len);
                    if (i > 0) {
                        //递归获取空白区域左边差异
                        result = getDiff(result[0], result[1], 0, i);
                    }
                    if (i + len < end) {
                        //递归获取空白区域右边差异
                        result = getDiff(result[0], result[1], i + len, end);
                    }
                    len = 0;//退出while循环
                    break;
                }
            }
            len = len / 2;
        }
        return result;
    }

    /**
     * 将字符串s指定的区域设置成空格
     */
    public static String setEmpty(String s, int start, int end) {
        char[] array = s.toCharArray();
        for (int i = start; i < end; i++) {
            array[i] = ' ';
        }
        return new String(array);
    }

    public void getHighLightDifferentOld() {
        String a = "纸箱上盖_B楞_1730*1095*100mm_HI-MO3  60双玻售后件_图号PZ6030302312203-02";
        String b = "纸箱上盖_B楞_1730*1095*100mm_HI-MO3 60双玻售后件_图号PZ6030302312203-02";
        String[] anb = getHighLightDifferent(a, b);
        anb[0] = anb[0].replaceAll("red", "#FF9900");
        anb[1] = anb[1].replaceAll("red", "#2BD591");
    }

    public static void main(String[] args) {
    	String a = "纸箱上盖_B 楞_17 30*109 5*100mm_HI-MO3  60双玻售后件_图号PZ6030302312203-02";
        String b = "纸箱上盖_B楞_1730*1095*100mm_HI-MO3 60双玻售后件_图号PZ6030302312203-02";
        CompareTextHighLightUtil util = new CompareTextHighLightUtil();
//        util.getHighLightDifferent();
        String[] res = util.getHighLightDifferent(a, b);
        System.out.println(Arrays.toString(res));
    }
    //--------------new -------------------------

    public static String[] getHighLightDifferent(String altbe, String altaf) {
        //字符串1  altbe  字符串2  altaf
        List<Integer> beList = rememberSpacing(altbe);
        List<Integer> afList = rememberSpacing(altaf);
      //优化需求，空格不忽略，标红 modify by longi.suwei3 20220809
//        altbe = altbe.replace(" ", "");
//        altaf = altaf.replace(" ", "");
        LinkedList<Diff> t = new DiffMatchPatch().diff_main(altbe, altaf);
        StringBuffer s1 = new StringBuffer();
        StringBuffer s2 = new StringBuffer();
        Integer indexBe = 0;
        Integer indexAf = 0;
        for (Diff diff : t) {
            StringBuffer diffTextBe = new StringBuffer(diff.text);
            StringBuffer diffTextAf = new StringBuffer(diff.text);
            if ("EQUAL".equalsIgnoreCase(diff.operation.toString())) {
                addSpacing(beList, indexBe, diffTextBe);
                addSpacing(afList, indexAf, diffTextAf);
                s1.append(diffTextBe);
                s2.append(diffTextAf);

                indexBe += diffTextBe.length();
                indexAf += diffTextAf.length();
            }
            indexBe = appendString2("DELETE", diff, s1, s2, beList, indexBe);
            indexAf = appendString2("INSERT", diff, s2, s1, afList, indexAf);
        }
        String[] result = new String[2];
        result[0] = s1.toString();
        result[1] = s2.toString();
        return result;
    }

//    public static void appendString(String type, Diff diff, StringBuffer sbOne, StringBuffer sbTwo) {
//        if (type.equals(diff.operation.toString())) {
//            if (" ".equals(diff.text)) {
//                sbOne.append(" ");
//                sbTwo.append(" ");
//            } else {
//                sbOne.append("<em class='f-required'>").append(diff.text).append("</em>");
//            }
//        }
//    }

    private static List<Integer> rememberSpacing(String str) {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < str.length(); i++) {
            if (' ' == str.charAt(i)) {
                list.add(i);
            }
        }
        return list;
    }

    public static void addSpacing(List<Integer> list, Integer index, StringBuffer str) {
        for (Integer o : list) {
            if (o >= index && o < index + str.length()) {
                str.insert(o - index, ' ');
            }
        }
    }

    public static Integer appendString2(String type, Diff diff, StringBuffer sbOne,
                                        StringBuffer sbTwo, List<Integer> list, Integer i) {
        Integer result = i;
        if (type.equals(diff.operation.toString())) {
            StringBuffer sb = new StringBuffer(diff.text);
            for (Integer o : list) {
                if (o >= i && o < i + sb.length()) {
                    sb.insert(o - i, ' ');
                }
            }

            //优化需求，空格不忽略，标红 modify by longi.suwei3 20220809
            if(sb.toString().equals(" ")) {
            	sb = new StringBuffer("&nbsp;");
            }
            sbOne.append("</span><span style='background-color:red'>").append(sb).append("</span><span>");
            result = i + sb.length();
        }
        return result;
    }

    public void getHighLightDifferent() {
        String altbe = "纸箱上盖_B 楞_17 30*109 5*100mm_HI-MO3  60双玻售后件_图号PZ6030302312203-02";
        String altaf = "纸箱上盖_B楞_1730*1095*100mm_HI-MO3 60双玻售后件_图号PZ6030302312203-02";
        String[] highLightDifferentNew = getHighLightDifferent(altbe, altaf);
        highLightDifferentNew[0] = highLightDifferentNew[0].replaceAll("red", "#98FB98");
        highLightDifferentNew[0] = highLightDifferentNew[0].startsWith("</span>")?highLightDifferentNew[0].replaceFirst("</span>", ""):highLightDifferentNew[0];
        highLightDifferentNew[0] = highLightDifferentNew[0].startsWith("<span")?highLightDifferentNew[0]:"<span>"+highLightDifferentNew[0];

        highLightDifferentNew[0] = highLightDifferentNew[0].endsWith("</span>")?highLightDifferentNew[0]:highLightDifferentNew[0]+"</span>";
        highLightDifferentNew[1] = highLightDifferentNew[1].replaceAll("red", "#FFA07A");
        highLightDifferentNew[1] = highLightDifferentNew[1].startsWith("</span>")?highLightDifferentNew[1].replaceFirst("</span>", ""):highLightDifferentNew[1];
        highLightDifferentNew[1] = highLightDifferentNew[1].endsWith("</span>")?highLightDifferentNew[1]:highLightDifferentNew[1]+"</span>";
        highLightDifferentNew[1] = highLightDifferentNew[1].startsWith("<span")?highLightDifferentNew[1]:"<span>"+highLightDifferentNew[1];
    
    
        System.out.println(highLightDifferentNew[0]);
        System.out.println(highLightDifferentNew[1]);
        
    }

}