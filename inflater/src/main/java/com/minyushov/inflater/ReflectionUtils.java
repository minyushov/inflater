package com.minyushov.inflater;

import java.lang.reflect.Field;

final class ReflectionUtils {
	private ReflectionUtils() {
	}

	static Field getField(Class clazz, String fieldName) {
		try {
			final Field f = clazz.getDeclaredField(fieldName);
			f.setAccessible(true);
			return f;
		} catch (NoSuchFieldException ignored) {
		}
		return null;
	}

	static Object getValue(Field field, Object obj) {
		try {
			return field.get(obj);
		} catch (IllegalAccessException ignored) {
		}
		return null;
	}

	static void setValue(Field field, Object obj, Object value) {
		try {
			field.set(obj, value);
		} catch (IllegalAccessException ignored) {
		}
	}
}