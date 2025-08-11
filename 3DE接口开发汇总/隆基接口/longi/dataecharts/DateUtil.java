package longi.dataecharts;

import org.apache.commons.collections4.list.TreeList;
import org.apache.commons.lang3.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;

public class DateUtil {
    public static String queryDate = "";

    public static String queryRangeMonths1 = "";
    public static String queryRangeMonths2 = "";
    public static String getFirstDay() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                LocalDate.now().minusMonths(1)
                        .with(TemporalAdjusters.firstDayOfMonth()));
    }

    public static String getLastDay() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                LocalDate.now().minusMonths(1)
                        .with(TemporalAdjusters.lastDayOfMonth()));
    }

    public static String getLastThreeMonthFirstDay() {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                LocalDate.now().minusMonths(3)
                        .with(TemporalAdjusters.firstDayOfMonth()));
    }

    public static String getDateMonth(){
        if(StringUtils.isBlank(queryDate)){
            int day = LocalDate.now().getDayOfMonth();
            if(day < 8){
                queryDate = DateTimeFormatter.ofPattern("yyyy-MM").format(
                        LocalDate.now().minusMonths(2));
            }else {
                queryDate = DateTimeFormatter.ofPattern("yyyy-MM").format(
                        LocalDate.now().minusMonths(1));
            }
        }
        return queryDate;
    }

    public static List<String> getMonths1(){
        if(StringUtils.isBlank(queryRangeMonths1)){
               return new ArrayList<String>(){{
                   add(DateTimeFormatter.ofPattern("yyyy-MM").format(
                           LocalDate.now().minusMonths(1)));
               }};

        }
        String[] months = queryRangeMonths1.split(" - ");

        return getMonthBetween(months[0], months[1]);
    }

    public static String[] getMonths2(){
        String[] monthDays = new String[2];
        String nowMonth = DateTimeFormatter.ofPattern("yyyy-MM").format(
                LocalDate.now());
        if(StringUtils.isBlank(queryRangeMonths2)){
            monthDays[0] = nowMonth+"-01";
            monthDays[1] = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                    LocalDate.now());
        }else{
            String[] months = queryRangeMonths2.split(" - ");
            monthDays[0] = months[0]+"-01";
            if(Objects.equals(months[1],nowMonth)){
                monthDays[1] = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                        LocalDate.now());
            }else{
                LocalDate date = LocalDate.parse(months[1] + "-01");
                LocalDate dateEnd = date.with(TemporalAdjusters.lastDayOfMonth());
                monthDays[1] = DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                        dateEnd);
            }
        }
        return monthDays;
    }


    public static String getDateRange(){
        LocalDate date = LocalDate.parse(getDateMonth()+"-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String date1 = DateTimeFormatter.ofPattern("yyyy-MM").format(date);
        LocalDate lastMonth = date.plusMonths(1);
        String date2 = DateTimeFormatter.ofPattern("yyyy-MM").format(lastMonth);
        return date1+"-08~"+date2+"-07";
    }

    public static String getDateRange2(){
        LocalDate datenow1 = LocalDate.parse(getDateMonth()+"-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate lastMonth2 = datenow1.minusMonths(2);
        String date1 = DateTimeFormatter.ofPattern("yyyy-MM").format(lastMonth2);
        LocalDate datenow2 = LocalDate.parse(getDateMonth()+"-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate lastMonth = datenow2.plusMonths(1);
        String date2 = DateTimeFormatter.ofPattern("yyyy-MM").format(lastMonth);
        return date1+"-08~"+date2+"-07";
    }

    public static String getDateStart(){
        LocalDate date = LocalDate.parse(getDateMonth()+"-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String date1 = DateTimeFormatter.ofPattern("yyyy-MM").format(date);
        return date1+"-07";
    }

    public static Integer[] getMonth() {
        LocalDate date = LocalDate.parse(getDateMonth() + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate date2 = date.minusMonths(1);
        return new Integer[]{date2.getMonthValue(), date.getMonthValue()};
    }

    public static String getLast2Month() {
        LocalDate date = LocalDate.parse(getDateMonth() + "-01", DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        LocalDate date2 = date.minusMonths(1);
        return DateTimeFormatter.ofPattern("yyyy-MM").format(date2);
    }

    /**
     * 获取两个月份之间的所有月份(含跨年)
     *
     * @param minDate
     * @param maxDate
     * @return
     * @throws ParseException
     */
    public static List<String> getMonthBetween(String minDate, String maxDate) {
       TreeList<String> result = new TreeList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM");// 格式化为年月
        try {
            Calendar min = Calendar.getInstance();
            Calendar max = Calendar.getInstance();

            min.setTime(sdf.parse(minDate));
            min.set(min.get(Calendar.YEAR), min.get(Calendar.MONTH), 1);

            max.setTime(sdf.parse(maxDate));
            max.set(max.get(Calendar.YEAR), max.get(Calendar.MONTH), 2);

            Calendar curr = min;
            while (curr.before(max)) {
                result.add(sdf.format(curr.getTime()));
                curr.add(Calendar.MONTH, 1);
            }

            // 实现排序方法
            Collections.sort(result, (o1, o2) -> {
                String str1 =  o1;
                String str2 =  o2;
                SimpleDateFormat format = new SimpleDateFormat("yyyy-MM");
                Date date1 = null;
                Date date2 = null;
                try {
                    date1 = format.parse(str1);
                    date2 = format.parse(str2);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                if (date1.compareTo(date2) > 0) {
                    return -1;
                }
                return 1;
            });
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return result;
    }


    public static String getLastOneMonthLastDay2() {
        if(StringUtils.isBlank(DateUtil.queryRangeMonths2)){
            LocalDate dateEnd = LocalDate.now().minusMonths(1)
                    .with(TemporalAdjusters.lastDayOfMonth());
            return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                    dateEnd);
        }else{
            String[] months = queryRangeMonths2.split(" - ");
            LocalDate date = LocalDate.parse(months[1] + "-01");
            LocalDate dateEnd = date.with(TemporalAdjusters.lastDayOfMonth());
            return DateTimeFormatter.ofPattern("yyyy-MM-dd").format(
                    dateEnd);
        }

    }

    public static String getLastThreeMonthFirstDay2() {
        if(StringUtils.isBlank(DateUtil.queryRangeMonths2)){
            return   DateTimeFormatter.ofPattern("yyyy-MM").format(
                    LocalDate.now().minusMonths(3)) + "-01";
        }else{
            String[] months = DateUtil.queryRangeMonths2.split(" - ");
            return months[0]  + "-01";
        }

    }

    public static void main(String[] args) {
//        List<String> monthBetween = getMonthBetween("2021-04", "2023-04");
//        for (String month : monthBetween) {
//            System.out.println(month);
//        }
        System.out.println(getFirstDay());
        System.out.println(getLastDay());
        System.out.println(getLastThreeMonthFirstDay());
    }

}
