/*
 * Copyright (c) 2008-2019 Haulmont.
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

package com.haulmont.cuba.core.app.dynamicattributes;


import com.google.common.base.Strings;
import com.haulmont.cuba.core.EntityManager;
import com.haulmont.cuba.core.Persistence;
import com.haulmont.cuba.core.Query;
import com.haulmont.cuba.core.Transaction;
import com.haulmont.cuba.core.entity.*;
import com.haulmont.cuba.core.global.Scripting;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component(AttributeOptionsLoader.NAME)
public class AttributeOptionsLoaderImpl implements AttributeOptionsLoader {

    protected final Map<String, OptionsLoaderStrategy> loaderStrategies = new HashMap<>();

    @Inject
    protected Scripting scripting;
    @Inject
    protected Persistence persistence;

    protected static final Pattern COMMON_PARAM_PATTERN = Pattern.compile("\\$\\{(.+?)}");

    public interface OptionsLoaderStrategy {
        List loadOptions(BaseGenericIdEntity entity, CategoryAttribute attribute, String script);
    }

    @PostConstruct
    public void init() {
        loaderStrategies.put(CategoryAttributeOptionsLoaderType.GROOVY.getId(), this::executeGroovyScript);
        loaderStrategies.put(CategoryAttributeOptionsLoaderType.SQL.getId(), this::executeSql);
    }

    @Override
    public List loadOptions(BaseGenericIdEntity entity, CategoryAttribute attribute) {
        CategoryAttributeConfiguration configuration = attribute.getConfiguration();
        String loaderScript = configuration.getValuesLoaderScript();
        if (!Strings.isNullOrEmpty(loaderScript)) {
            OptionsLoaderStrategy loaderStrategy = resolveLoaderStrategy(configuration.getValuesLoaderType());

            List result = loaderStrategy.loadOptions(entity, attribute, loaderScript);

            return result == null ? Collections.emptyList() : result;
        } else {
            return Collections.emptyList();
        }
    }

    protected OptionsLoaderStrategy resolveLoaderStrategy(CategoryAttributeOptionsLoaderType loaderType) {
        OptionsLoaderStrategy loaderStrategy = loaderStrategies.get(loaderType.getId());
        if (loaderStrategy == null) {
            throw new IllegalStateException(String.format("Unsupported options loader type: %s", loaderType.getId()));
        }
        return loaderStrategy;
    }

    protected List executeSql(BaseGenericIdEntity entity, CategoryAttribute attribute, String script) {
        List result;
        Transaction tx = persistence.createTransaction();
        try {
            EntityManager em = persistence.getEntityManager();
            SqlQuery sqlQuery = buildSqlQuery(script, Collections.singletonMap("entity", entity));

            Query query = em.createNativeQuery(sqlQuery.sql);

            if (sqlQuery.params != null) {
                int i = 0;
                for (Object param : sqlQuery.params) {
                    query.setParameter(i++, param);
                }
            }

            result = query.getResultList();
            tx.commit();
        } finally {
            tx.end();
        }
        return result;
    }

    protected class SqlQuery {
        protected String sql;
        protected List<Object> params;

        public SqlQuery(String sql, List<Object> params) {
            this.sql = sql;
            this.params = params;
        }
    }

    protected SqlQuery buildSqlQuery(String script, Map<String, Object> params) {
        Matcher matcher = COMMON_PARAM_PATTERN.matcher(script);
        boolean result = matcher.find();
        if (result) {
            List<Object> sqlParams = new ArrayList<>();
            StringBuffer sql = new StringBuffer();
            do {
                String parameterName = matcher.group(1);
                sqlParams.add(getSqlParameterValue(parameterName, params));
                matcher.appendReplacement(sql, "?");
                result = matcher.find();
            } while (result);
            matcher.appendTail(sql);
            return new SqlQuery(sql.toString(), sqlParams);
        }
        return new SqlQuery(script, null);
    }

    protected Object getSqlParameterValue(String name, Map<String, Object> params) {
        if ("entity".equals(name)) {
            Entity entity = (Entity) params.get("entity");
            if (entity != null) {
                return entity.getId();
            }
        } else if (name != null && name.startsWith("entity.")) {
            Entity entity = (Entity) params.get("entity");
            if (entity != null) {
                String attributePath = name.substring(name.indexOf("entity."));
                Object value = entity.getValue(attributePath);
                return value instanceof Entity ? ((Entity) value).getId() : value;
            }
        }
        return null;
    }


    protected List executeGroovyScript(BaseGenericIdEntity entity, CategoryAttribute attribute, String script) {
        List result;
        Transaction tx = persistence.createTransaction();
        try {
            result = scripting.evaluateGroovy(script, Collections.singletonMap("entity", entity));
            tx.commit();
        } finally {
            tx.end();
        }
        return result;
    }
}
