/**
 * Copyright © 2017 Matthieu Brouillard [http://oss.brouillard.fr/GuardEE] (matthieu@brouillard.fr)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.brouillard.oss.ee.microprofile.config;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.microprofile.config.spi.Converter;

public class GuardEEConfigConverters {
    public static Converter<?>[] defaultConverters() {
        return DEFAULT_CONVERTERS;
    }
    
    static <T> Optional<Converter<T>> constructor(Class<T> type) {
    	Constructor<T> cString = null;
    	Constructor<T> cCharSequence = null;
    	
        try {
            cString = type.getDeclaredConstructor(String.class);
        } catch (NoSuchMethodException e) {}
        try {
            cCharSequence = type.getDeclaredConstructor(CharSequence.class);
        } catch (NoSuchMethodException e) {}
         
    	final Constructor<T> constructor = cString == null ? cCharSequence : cString;
        if (constructor != null) {
            Converter<T> byConstructor = new Converter<T>() {
                @Override
                public T convert(String value) {
                    try {
                        constructor.setAccessible(true);
                    	return constructor.newInstance(value);
                    } catch (Exception e) {
                        String msg = String.format(
                                "cannot build a %s instance by calling constructor %s(%s)"
                                , type.getName()
                                , type.getSimpleName()
                                , value
                        );
                        throw new IllegalArgumentException(msg);
                    }
                }
            };
            
            return Optional.of(byConstructor);
        }
        
        return Optional.empty();
    }
    
    static <T> Optional<Converter<T>> valueOf(Class<T> type) {
        return getConverterByMethodInvocation(type, "valueOf");
    }

    private static <T> Optional<Converter<T>> getConverterByMethodInvocation(Class<T> type, String methodName) {
    	Method mValueOfString = null;
    	Method mValueOfCharSequence = null;
        try {
            mValueOfString = type.getDeclaredMethod(methodName, String.class);
        } catch (NoSuchMethodException e) {
        }
        try {
            mValueOfCharSequence = type.getDeclaredMethod(methodName, CharSequence.class);
        } catch (NoSuchMethodException e) {
        }

        Method builder = null;

        if (mValueOfString != null && mValueOfString.getReturnType().equals(type)) {
            builder = mValueOfString;
        } else if (mValueOfCharSequence != null && mValueOfCharSequence.getReturnType().equals(type)) {
            builder = mValueOfCharSequence;
        }
        
        if (builder != null) {
            Method effectiveBuilder = builder;
            Converter<T> byValueOf = new Converter<T>() {
                @SuppressWarnings("unchecked")
				@Override
                public T convert(String value) {
                    try {
                        effectiveBuilder.setAccessible(true);
                        return (T)effectiveBuilder.invoke(null, value);
                    } catch (Exception e) {
                        String msg = String.format(
                                "cannot build a %s instance by calling %s.%s(%s)"
                                , type.getName()
                                , type.getSimpleName()
                                , methodName
                                , value
                        );
                        throw new IllegalArgumentException(msg);
                    }
                }
            };
            return Optional.of(byValueOf);
        }

        return Optional.empty();
    }

    public static <T> Optional<Converter<T>> parse(Class<T> type) {
        return getConverterByMethodInvocation(type, "parse");
    }

    public static <T> Optional<Converter<T>> asEnum(Class<T> type) {
        if (type.isEnum()) {
            final List<T> enumsValues = Arrays.asList(type.getEnumConstants());
            Converter<T> enumConverter = new Converter<T>() {
                @SuppressWarnings("unchecked")
				@Override
                public T convert(String value) {
                    T t = (T)enumsValues
                            .stream()
                            .map(Enum.class::cast)
                            .filter(e -> e.name().equals(value))
                            .findFirst()
                            .orElseThrow(() -> {
                                String msg = String.format(
                                        "cannot use %s as an enum value of %s"
                                        , value
                                        , type.getName()
                                );
                                return new IllegalArgumentException(msg);
                            });
                    return t;
                }
            };
            return Optional.of(enumConverter);
        }

        return Optional.empty();
    }
    
    public static <T> Converter<T> forType(Class<T> type) {
        if (type.isPrimitive()) {
            return getPrimitiveConverter(type);
        }
        
        return Stream.of(constructor(type), valueOf(type), parse(type), asEnum(type))
                .filter(Optional::isPresent)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no converter for type: " + type))
                .get();
    }

    private static <T> Converter<T> getPrimitiveConverter(Class<T> type) {
        if (Boolean.TYPE.equals(type)) {
            return (Converter<T>)BOOLEAN_CONVERTER;
        } else if (Integer.TYPE.equals(type)) {
            return (Converter<T>)INTEGER_CONVERTER;
        } else if (Long.TYPE.equals(type)) {
            return (Converter<T>)LONG_CONVERTER;
        } else if (Float.TYPE.equals(type)) {
            return (Converter<T>)FLOAT_CONVERTER;
        } else if (Double.TYPE.equals(type)) {
            return (Converter<T>)DOUBLE_CONVERTER;
        }
        
        throw new IllegalStateException(String.format("primitive type %s has no associated Converter"));
    }

    private final static class BooleanConverter implements Converter<Boolean>, Serializable {
        @Override
        public Boolean convert(String value) {
            if (value == null) {
                return Boolean.FALSE;
            }

            String upper = value.toUpperCase();
            if ("TRUE".equals(upper)
                    || "1".equals(upper)
                    || "Y".equals(upper)
                    || "YES".equals(upper)
                    || "ON".equals(upper)
                    ) {
                return Boolean.TRUE;
            }

            return Boolean.FALSE;
        }
    }
    private final static Converter<Boolean> BOOLEAN_CONVERTER = new BooleanConverter();

    private final static class IntegerConverter implements Converter<Integer>, Serializable {
        @Override
        public Integer convert(String value) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in an Integer value", value));
            }
        }
    }
    private final static Converter<Integer> INTEGER_CONVERTER = new IntegerConverter();

    private final static class LongConverter implements Converter<Long>, Serializable {
        @Override
        public Long convert(String value) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Long value", value));
            }
        }
    }
    private final static Converter<Long> LONG_CONVERTER = new LongConverter();

    private final static class FloatConverter implements Converter<Float>, Serializable {
        @Override
        public Float convert(String value) {
            try {
                return Float.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Float value", value));
            }
        }
    }
    private final static Converter<Float> FLOAT_CONVERTER = new FloatConverter();

    private final static class DoubleConverter implements Converter<Double>, Serializable {
        @Override
        public Double convert(String value) {
            try {
                return Double.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Double value", value));
            }
        }
    }
    private final static Converter<Double> DOUBLE_CONVERTER = new DoubleConverter();

    private final static class DurationConverter implements Converter<Duration>, Serializable {
        @Override
        public Duration convert(String value) {
            try {
                return Duration.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Duration value", value));
            }
        }
    }
    private final static Converter<Duration> DURATION_CONVERTER = new DurationConverter();

    private final static class LocalTimeConverter implements Converter<LocalTime>, Serializable {
        @Override
        public LocalTime convert(String value) {
            try {
                return LocalTime.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a LocalTime value", value));
            }
        }
    }
    private final static Converter<LocalTime> LOCALTIME_CONVERTER = new LocalTimeConverter();

    private final static class LocalDateTimeConverter implements Converter<LocalDateTime>, Serializable {
        @Override
        public LocalDateTime convert(String value) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a LocalDateTime value", value));
            }
        }
    }
    private final static Converter<LocalDateTime> LOCALDATETIME_CONVERTER = new LocalDateTimeConverter();

    private final static class LocalDateConverter implements Converter<LocalDate>, Serializable {
        @Override
        public LocalDate convert(String value) {
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a LocalDate value", value));
            }
        }
    }
    private final static Converter<LocalDate> LOCALDATE_CONVERTER = new LocalDateConverter();

    private final static class OffsetDateTimeConverter implements Converter<OffsetDateTime>, Serializable {
        @Override
        public OffsetDateTime convert(String value) {
            try {
                return OffsetDateTime.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a OffsetDateTime value", value));
            }
        }
    }
    private final static Converter<OffsetDateTime> OFFSETDATETIME_CONVERTER = new OffsetDateTimeConverter();

    private final static class OffsetTimeConverter implements Converter<OffsetTime>, Serializable {
        @Override
        public OffsetTime convert(String value) {
            try {
                return OffsetTime.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a OffsetTime value", value));
            }
        }
    }
    private final static Converter<OffsetTime> OFFSETTIME_CONVERTER = new OffsetTimeConverter();

    private final static class InstantConverter implements Converter<Instant>, Serializable {
        @Override
        public Instant convert(String value) {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Instant value", value));
            }
        }
    }
    private final static Converter<Instant> INSTANT_CONVERTER = new InstantConverter();

    private final static class URLConverter implements Converter<URL>, Serializable {
        @Override
        public URL convert(String value) {
            try {
                return new URL(value);
            } catch (MalformedURLException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in an URL value", value));
            }
        }
    }
    private final static Converter<URL> URL_CONVERTER = new URLConverter();
    
    private final static Converter<?>[] DEFAULT_CONVERTERS = new Converter<?>[] {
            BOOLEAN_CONVERTER
            , INTEGER_CONVERTER
            , LONG_CONVERTER
            , FLOAT_CONVERTER
            , DOUBLE_CONVERTER
            , DURATION_CONVERTER
            , LOCALTIME_CONVERTER
            , LOCALDATE_CONVERTER
            , LOCALDATETIME_CONVERTER
            , OFFSETDATETIME_CONVERTER
            , OFFSETTIME_CONVERTER
            , INSTANT_CONVERTER
            , URL_CONVERTER
    };
}
            
