package com.example.wusthelper.utils;

import android.util.Base64;

import com.example.wusthelper.bean.javabean.CourseBean;
import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Deflater;
import java.util.zip.Inflater;

public class CourseShareCodec {
    public static final String PROTOCOL = "wusthelper-course-share";
    public static final int VERSION = 2;
    public static final String SHARE_TYPE = "lover-course";

    private static final Gson GSON = new Gson();

    public static String encodeV2(String platform, String semester, String studentName, String studentId, List<CourseBean> courses) {
        V2Payload payload = new V2Payload();
        payload.protocol = PROTOCOL;
        payload.version = VERSION;
        payload.shareType = SHARE_TYPE;
        payload.platform = platform;
        payload.semester = semester == null ? "" : semester;
        payload.student = new Student();
        payload.student.name = studentName == null ? "" : studentName;
        payload.student.id = studentId == null ? "" : studentId;
        payload.courses = new ArrayList<>();
        if (courses != null) {
            for (CourseBean course : courses) {
                payload.courses.add(SharedCourse.from(course));
            }
        }
        byte[] jsonBytes = GSON.toJson(payload).getBytes(StandardCharsets.UTF_8);
        byte[] compressed = deflate(jsonBytes);
        if (compressed != null && compressed.length > 0) {
            return Base64.encodeToString(compressed, Base64.NO_WRAP);
        }
        return GSON.toJson(payload);
    }

    public static DecodeResult decode(String raw) {
        if (raw == null) {
            return DecodeResult.invalid();
        }
        String trimmed = raw.trim();
        V2Payload payload = tryDecodeV2(trimmed);
        if (payload != null) {
            return DecodeResult.v2(payload);
        }
        AndroidLegacyPayload legacy = tryDecodeLegacy(trimmed);
        if (legacy != null) {
            return DecodeResult.legacy(legacy);
        }
        return DecodeResult.invalid();
    }

    private static V2Payload tryDecodeV2(String raw) {
        List<byte[]> candidates = new ArrayList<>();
        candidates.add(raw.getBytes(StandardCharsets.UTF_8));
        try {
            byte[] base64Bytes = Base64.decode(raw, Base64.DEFAULT);
            candidates.add(base64Bytes);
            byte[] inflated = inflate(base64Bytes);
            if (inflated != null) {
                candidates.add(inflated);
            }
        } catch (Exception ignored) {
        }

        for (byte[] candidate : candidates) {
            try {
                V2Payload payload = GSON.fromJson(new String(candidate, StandardCharsets.UTF_8), V2Payload.class);
                if (payload != null
                        && PROTOCOL.equals(payload.protocol)
                        && payload.version == VERSION
                        && SHARE_TYPE.equals(payload.shareType)) {
                    return payload;
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    private static AndroidLegacyPayload tryDecodeLegacy(String raw) {
        if (raw.length() <= 13) {
            return null;
        }
        try {
            String content = raw.substring(3, 10) + raw.substring(13);
            content = new String(Base64.decode(content, Base64.DEFAULT), StandardCharsets.UTF_8);
            if (!content.startsWith("kjbk")) {
                return null;
            }
            String[] strs = content.split("\\?\\+/");
            if (strs.length != 5) {
                return null;
            }
            AndroidLegacyPayload payload = new AndroidLegacyPayload();
            payload.studentName = strs[1];
            payload.studentId = strs[2];
            payload.token = strs[3];
            payload.semester = strs[4];
            return payload;
        } catch (Exception ignored) {
            return null;
        }
    }

    private static byte[] deflate(byte[] input) {
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION, false);
        deflater.setInput(input);
        deflater.finish();
        byte[] buffer = new byte[4096];
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        while (!deflater.finished()) {
            int count = deflater.deflate(buffer);
            outputStream.write(buffer, 0, count);
        }
        deflater.end();
        return outputStream.toByteArray();
    }

    private static byte[] inflate(byte[] input) {
        Inflater inflater = new Inflater(false);
        inflater.setInput(input);
        byte[] buffer = new byte[4096];
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        try {
            while (!inflater.finished()) {
                int count = inflater.inflate(buffer);
                if (count == 0) {
                    if (inflater.needsInput() || inflater.needsDictionary()) {
                        break;
                    }
                } else {
                    outputStream.write(buffer, 0, count);
                }
            }
            inflater.end();
            byte[] inflated = outputStream.toByteArray();
            return inflated.length == 0 ? null : inflated;
        } catch (Exception e) {
            inflater.end();
            return null;
        }
    }

    public static class DecodeResult {
        public enum Type { V2, LEGACY, INVALID }
        public final Type type;
        public final V2Payload v2Payload;
        public final AndroidLegacyPayload legacyPayload;

        private DecodeResult(Type type, V2Payload v2Payload, AndroidLegacyPayload legacyPayload) {
            this.type = type;
            this.v2Payload = v2Payload;
            this.legacyPayload = legacyPayload;
        }

        public static DecodeResult v2(V2Payload payload) {
            return new DecodeResult(Type.V2, payload, null);
        }

        public static DecodeResult legacy(AndroidLegacyPayload payload) {
            return new DecodeResult(Type.LEGACY, null, payload);
        }

        public static DecodeResult invalid() {
            return new DecodeResult(Type.INVALID, null, null);
        }
    }

    public static class AndroidLegacyPayload {
        public String studentName;
        public String studentId;
        public String token;
        public String semester;
    }

    public static class V2Payload {
        public String protocol;
        public int version;
        public String shareType;
        public String platform;
        public String semester;
        public Student student;
        public List<SharedCourse> courses;
    }

    public static class Student {
        public String name;
        public String id;
    }

    public static class SharedCourse {
        public String name;
        public String teacher;
        public String teachClass;
        public int startWeek;
        public int endWeek;
        public int weekDay;
        public int startSection;
        public int endSection;
        public String classroom;

        public static SharedCourse from(CourseBean bean) {
            SharedCourse course = new SharedCourse();
            course.name = bean.getCourseName();
            course.teacher = bean.getTeacherName();
            course.teachClass = bean.getClassNo();
            course.startWeek = bean.getStartWeek();
            course.endWeek = bean.getEndWeek();
            course.weekDay = bean.getWeekday();
            int startTime = bean.getStartTime();
            int endTime = bean.getEndTime();
            if (startTime >= 1 && startTime <= 6 && (endTime == 0 || endTime <= 6)) {
                course.startSection = (startTime - 1) * 2 + 1;
                course.endSection = endTime > 0 ? endTime * 2 : course.startSection + 1;
            } else {
                course.startSection = startTime;
                course.endSection = endTime > 0 ? endTime : startTime + 1;
            }
            course.classroom = bean.getClassRoom();
            return course;
        }

        public CourseBean toCourseBean() {
            CourseBean bean = new CourseBean();
            bean.setCourseName(name);
            bean.setTeacherName(teacher);
            bean.setClassNo(teachClass);
            bean.setStartWeek(startWeek);
            bean.setEndWeek(endWeek);
            bean.setWeekday(weekDay);
            bean.setStartTime(startSection);
            bean.setEndTime(endSection);
            bean.setClassRoom(classroom);
            bean.setIsDefault(CourseBean.IS_MYSELF);
            return bean;
        }
    }
}
