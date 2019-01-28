/*
 *  Copyright (c) 2010-2019   the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.springframework.data.jpa.repository.query;

import org.springframework.data.repository.query.Parameters;
import org.springframework.data.repository.query.QueryMethodEvaluationContextProvider;
import org.springframework.data.spel.EvaluationContextProvider;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;

import javax.persistence.TemporalType;
import java.util.function.Function;

/**
 * <p>
 * <b>TemplateBasedQueryParameterSetterFactory</b> 参数设置工厂
 * </p>
 *
 * @author Rain
 * @since 2019-01-27
 */
public class TemplateBasedQueryParameterSetterFactory extends QueryParameterSetterFactory {

    private final ExpressionParser parser;
    private final QueryMethodEvaluationContextProvider evaluationContextProvider;
    private final Parameters<?, ?> parameters;

    TemplateBasedQueryParameterSetterFactory(ExpressionParser parser,
                                             QueryMethodEvaluationContextProvider evaluationContextProvider, Parameters<?, ?> parameters) {
        Assert.notNull(evaluationContextProvider, "EvaluationContextProvider must not be null!");
        Assert.notNull(parser, "ExpressionParser must not be null!");
        Assert.notNull(parameters, "Parameters must not be null!");

        this.evaluationContextProvider = evaluationContextProvider;
        this.parser = parser;
        this.parameters = parameters;
    }


    @Override
    QueryParameterSetter create(StringQuery.ParameterBinding binding, DeclaredQuery declaredQuery) {
        if (!binding.isExpression()) {
            return null;
        }

        Expression expression = parser.parseExpression(binding.getExpression());

        return createSetter(values -> evaluateExpression(expression, values), binding, null);
    }

    @Nullable
    private Object evaluateExpression(Expression expression, Object[] values) {

        EvaluationContext context = evaluationContextProvider.getEvaluationContext(parameters, values);
        return expression.getValue(context, Object.class);
    }

    private static QueryParameterSetter createSetter(Function<Object[], Object> valueExtractor, StringQuery.ParameterBinding binding,
                                                     @Nullable JpaParameters.JpaParameter parameter) {

        TemporalType temporalType = parameter != null && parameter.isTemporalParameter() //
                ? parameter.getRequiredTemporalType() //
                : null;

        return new QueryParameterSetter.NamedOrIndexedQueryParameterSetter(valueExtractor.andThen(binding::prepare),
                ParameterImpl.of(parameter, binding), temporalType);
    }

    public static QueryParameterSetterFactory parsingTemplate(ExpressionParser parser, EvaluationContextProvider evaluationContextProvider, JpaParameters parameters) {
        return null;
    }


    private static class ParameterImpl<T> implements javax.persistence.Parameter<T> {

        private final Class<T> parameterType;
        private final
        @Nullable
        String name;
        private final
        @Nullable
        Integer position;

        /**
         * Creates a new {@link ParameterImpl} for the given {@link JpaParameters.JpaParameter} and {@link StringQuery.ParameterBinding}.
         *
         * @param parameter can be {@literal null}.
         * @param binding   must not be {@literal null}.
         * @return a {@link javax.persistence.Parameter} object based on the information from the arguments.
         */
        static javax.persistence.Parameter<?> of(@Nullable JpaParameters.JpaParameter parameter, StringQuery.ParameterBinding binding) {

            Class<?> type = parameter == null ? Object.class : parameter.getType();

            return new ParameterImpl<>(type, getName(parameter, binding), binding.getPosition());
        }

        /**
         * Creates a new {@link ParameterImpl} for the given name, position and parameter type.
         *
         * @param parameterType must not be {@literal null}.
         * @param name          can be {@literal null}.
         * @param position      can be {@literal null}.
         */
        private ParameterImpl(Class<T> parameterType, @Nullable String name, @Nullable Integer position) {

            this.name = name;
            this.position = position;
            this.parameterType = parameterType;
        }

        /*
         * (non-Javadoc)
         * @see javax.persistence.Parameter#getName()
         */
        @Nullable
        @Override
        public String getName() {
            return name;
        }

        /*
         * (non-Javadoc)
         * @see javax.persistence.Parameter#getPosition()
         */
        @Nullable
        @Override
        public Integer getPosition() {
            return position;
        }

        /*
         * (non-Javadoc)
         * @see javax.persistence.Parameter#getParameterType()
         */
        @Override
        public Class<T> getParameterType() {
            return parameterType;
        }

        @Nullable
        private static String getName(@Nullable JpaParameters.JpaParameter parameter, StringQuery.ParameterBinding binding) {

            if (parameter == null) {
                return binding.getName();
            }

            return parameter.isNamedParameter() //
                    ? parameter.getName().orElseThrow(() -> new IllegalArgumentException("o_O parameter needs to have a name!")) //
                    : null;
        }
    }
}
