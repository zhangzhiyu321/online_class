package com.zzy.backend.common.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 日期时间工具类
 * 提供常用的日期时间格式化和解析功能
 */
public class DateTimeUtil {

    /**
     * 标准日期格式：yyyy-MM-dd
     */
    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 紧凑日期格式：yyyyMMdd
     */
    public static final DateTimeFormatter DATE_COMPACT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    /**
     * 标准时间格式：HH:mm:ss
     */
    public static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss");

    /**
     * 简短时间格式：HH:mm
     */
    public static final DateTimeFormatter TIME_SHORT_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    /**
     * 标准日期时间格式：yyyy-MM-dd HH:mm:ss
     */
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 紧凑日期时间格式：yyyyMMddHHmmss
     */
    public static final DateTimeFormatter DATETIME_COMPACT_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * 文件路径日期格式：yyyy/MM/dd
     */
    public static final DateTimeFormatter DATE_PATH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    // ==================== 日期格式化 ====================

    /**
     * 格式化日期为标准格式（yyyy-MM-dd）
     */
    public static String formatDate(LocalDate date) {
        return date == null ? null : date.format(DATE_FORMATTER);
    }

    /**
     * 格式化日期为紧凑格式（yyyyMMdd）
     */
    public static String formatDateCompact(LocalDate date) {
        return date == null ? null : date.format(DATE_COMPACT_FORMATTER);
    }

    /**
     * 格式化日期为路径格式（yyyy/MM/dd）
     */
    public static String formatDatePath(LocalDate date) {
        return date == null ? null : date.format(DATE_PATH_FORMATTER);
    }

    /**
     * 格式化当前日期为标准格式
     */
    public static String formatCurrentDate() {
        return LocalDate.now().format(DATE_FORMATTER);
    }

    /**
     * 格式化当前日期为紧凑格式
     */
    public static String formatCurrentDateCompact() {
        return LocalDate.now().format(DATE_COMPACT_FORMATTER);
    }

    // ==================== 时间格式化 ====================

    /**
     * 格式化时间为标准格式（HH:mm:ss）
     */
    public static String formatTime(LocalTime time) {
        return time == null ? null : time.format(TIME_FORMATTER);
    }

    /**
     * 格式化时间为简短格式（HH:mm）
     */
    public static String formatTimeShort(LocalTime time) {
        return time == null ? null : time.format(TIME_SHORT_FORMATTER);
    }

    /**
     * 格式化当前时间为标准格式
     */
    public static String formatCurrentTime() {
        return LocalTime.now().format(TIME_FORMATTER);
    }

    /**
     * 格式化当前时间为简短格式
     */
    public static String formatCurrentTimeShort() {
        return LocalTime.now().format(TIME_SHORT_FORMATTER);
    }

    // ==================== 日期时间格式化 ====================

    /**
     * 格式化日期时间为标准格式（yyyy-MM-dd HH:mm:ss）
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_FORMATTER);
    }

    /**
     * 格式化日期时间为紧凑格式（yyyyMMddHHmmss）
     */
    public static String formatDateTimeCompact(LocalDateTime dateTime) {
        return dateTime == null ? null : dateTime.format(DATETIME_COMPACT_FORMATTER);
    }

    /**
     * 格式化当前日期时间为标准格式
     */
    public static String formatCurrentDateTime() {
        return LocalDateTime.now().format(DATETIME_FORMATTER);
    }

    /**
     * 格式化当前日期时间为紧凑格式
     */
    public static String formatCurrentDateTimeCompact() {
        return LocalDateTime.now().format(DATETIME_COMPACT_FORMATTER);
    }

    // ==================== 日期解析 ====================

    /**
     * 解析标准格式日期字符串（yyyy-MM-dd）
     */
    public static LocalDate parseDate(String dateStr) {
        return StringUtil.isEmpty(dateStr) ? null : LocalDate.parse(dateStr, DATE_FORMATTER);
    }

    /**
     * 解析紧凑格式日期字符串（yyyyMMdd）
     */
    public static LocalDate parseDateCompact(String dateStr) {
        return StringUtil.isEmpty(dateStr) ? null : LocalDate.parse(dateStr, DATE_COMPACT_FORMATTER);
    }

    // ==================== 时间解析 ====================

    /**
     * 解析标准格式时间字符串（HH:mm:ss）
     */
    public static LocalTime parseTime(String timeStr) {
        return StringUtil.isEmpty(timeStr) ? null : LocalTime.parse(timeStr, TIME_FORMATTER);
    }

    /**
     * 解析简短格式时间字符串（HH:mm）
     */
    public static LocalTime parseTimeShort(String timeStr) {
        return StringUtil.isEmpty(timeStr) ? null : LocalTime.parse(timeStr, TIME_SHORT_FORMATTER);
    }

    // ==================== 日期时间解析 ====================

    /**
     * 解析标准格式日期时间字符串（yyyy-MM-dd HH:mm:ss）
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return StringUtil.isEmpty(dateTimeStr) ? null : LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER);
    }

    /**
     * 解析紧凑格式日期时间字符串（yyyyMMddHHmmss）
     */
    public static LocalDateTime parseDateTimeCompact(String dateTimeStr) {
        return StringUtil.isEmpty(dateTimeStr) ? null : LocalDateTime.parse(dateTimeStr, DATETIME_COMPACT_FORMATTER);
    }
}

