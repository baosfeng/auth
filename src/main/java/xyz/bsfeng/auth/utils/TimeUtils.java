package xyz.bsfeng.auth.utils;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Date;

/**
 * @author Administrator
 * @date 2021/8/15 15:37
 * @since 1.0.0
 */
public class TimeUtils {

	private static final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH时mm分ss秒");

	/**
	 * 将毫秒转换为指定的时间
	 *
	 * @param mill 对应的毫秒值
	 * @return 格式化的时间字符串
	 */
	public static String mill2Time(long mill) {
		if (mill < 0) {
			throw new IllegalArgumentException("传递的参数不正确");
		}
		Date date = new Date(mill);
		LocalDateTime localDateTime = date.toInstant().atOffset(ZoneOffset.ofHours(0)).toLocalDateTime();
		String format = timeFormatter.format(localDateTime);
		int days = localDateTime.getDayOfYear();
		int year = localDateTime.getYear() - 1970;
		if (year == 0) {
			if (days == 1) {
				return format;
			}
			return days + "天" + format;
		}
		return year + "年" + days + "天" + format;
	}

}
