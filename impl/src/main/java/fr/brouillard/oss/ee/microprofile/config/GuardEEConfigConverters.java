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
            cString = type.getConstructor(String.class);
        } catch (NoSuchMethodException e) {}
        try {
            cCharSequence = type.getConstructor(CharSequence.class);
        } catch (NoSuchMethodException e) {}
         
    	final Constructor<T> constructor = cString == null ? cCharSequence : cString;
        if (constructor != null) {
            Converter<T> byConstructor = new Converter<T>() {
                @Override
                public T convert(String value) {
                    try {
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
            mValueOfString = type.getMethod(methodName, String.class);
        } catch (NoSuchMethodException e) {
        }
        try {
            mValueOfCharSequence = type.getMethod(methodName, CharSequence.class);
        } catch (NoSuchMethodException e) {
        }

        Method builder = null;

        if (mValueOfString != null && mValueOfString.getReturnType().equals(type)) {
            builder = mValueOfCharSequence;
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
        return Stream.of(constructor(type), valueOf(type), parse(type), asEnum(type))
                .filter(Optional::isPresent)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("no converter for type: " + type))
                .get();
    }
    
    private final static Converter<Boolean> BOOLEAN_CONVERTER = new Converter<Boolean>() {
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
            ) {
                return Boolean.TRUE;
            }
            
            return Boolean.FALSE;
        }
    };
    
    private final static Converter<Integer> INTEGER_CONVERTER = new Converter<Integer>() {
        @Override
        public Integer convert(String value) {
            try {
                return Integer.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in an Integer value", value));
            }
        }
    };
    
    private final static Converter<Long> LONG_CONVERTER = new Converter<Long>() {
        @Override
        public Long convert(String value) {
            try {
                return Long.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Long value", value));
            }
        }
    };

    private final static Converter<Float> FLOAT_CONVERTER = new Converter<Float>() {
        @Override
        public Float convert(String value) {
            try {
                return Float.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Float value", value));
            }
        }
    };

    private final static Converter<Double> DOUBLE_CONVERTER = new Converter<Double>() {
        @Override
        public Double convert(String value) {
            try {
                return Double.valueOf(value);
            } catch (NumberFormatException nfe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Double value", value));
            }
        }
    };
    
    private final static Converter<Duration> DURATION_CONVERTER = new Converter<Duration>() {
        @Override
        public Duration convert(String value) {
            try {
                return Duration.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Duration value", value));
            }
        }
    };

    private final static Converter<LocalTime> LOCALTIME_CONVERTER = new Converter<LocalTime>() {
        @Override
        public LocalTime convert(String value) {
            try {
                return LocalTime.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a LocalTime value", value));
            }
        }
    };

    private final static Converter<LocalDateTime> LOCALDATETIME_CONVERTER = new Converter<LocalDateTime>() {
        @Override
        public LocalDateTime convert(String value) {
            try {
                return LocalDateTime.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a LocalDateTime value", value));
            }
        }
    };

    private final static Converter<LocalDate> LOCALDATE_CONVERTER = new Converter<LocalDate>() {
        @Override
        public LocalDate convert(String value) {
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a LocalDate value", value));
            }
        }
    };

    private final static Converter<OffsetDateTime> OFFSETDATETIME_CONVERTER = new Converter<OffsetDateTime>() {
        @Override
        public OffsetDateTime convert(String value) {
            try {
                return OffsetDateTime.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a OffsetDateTime value", value));
            }
        }
    };

    private final static Converter<OffsetTime> OFFSETTIME_CONVERTER = new Converter<OffsetTime>() {
        @Override
        public OffsetTime convert(String value) {
            try {
                return OffsetTime.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a OffsetTime value", value));
            }
        }
    };

    private final static Converter<Instant> INSTANT_CONVERTER = new Converter<Instant>() {
        @Override
        public Instant convert(String value) {
            try {
                return Instant.parse(value);
            } catch (DateTimeParseException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in a Instant value", value));
            }
        }
    };

    private final static Converter<URL> URL_CONVERTER = new Converter<URL>() {
        @Override
        public URL convert(String value) {
            try {
                return new URL(value);
            } catch (MalformedURLException dtpe) {
                throw new IllegalArgumentException(String.format("cannot convert '%s' in an URL value", value));
            }
        }
    };
    
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
            
