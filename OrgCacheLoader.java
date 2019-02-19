/*
 * Copyright © 2015-2026 the original author or authors.
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
package net.yto.gis.common.cache;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.common.base.Optional;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Lists;

import net.yto.gis.dto.AssignOrgInfo;
import net.yto.gis.service.AssignOrgService;

/**
 * <pre>
 * 名称: OrgCacheLoader
 * 描述: 网点缓存器
 * </pre>
 * @author yto.net.cn
 * @since 1.0.0
 * @desc 基于分布式的尽量不要使用该缓存。
 */
@Scope("singleton")
@Component
public class OrgCacheLoader extends CacheLoader<String, Optional<List<AssignOrgInfo>>> {
	
	
    /** public RangeServiceImpl(final OrgCacheLoader orgCacheLoader) {
        final int orgExpire = Integer.parseInt(System.getProperty(Keys.ASSIGN_CACHE_ORG_EXPIRE, DEFAULT_EXPIRE));
        //设置每10min更新一次缓存
        this.orgCache = CacheBuilder.newBuilder().expireAfterWrite(orgExpire, TimeUnit.MILLISECONDS)
                .expireAfterAccess(orgExpire, TimeUnit.MILLISECONDS).refreshAfterWrite(orgExpire, TimeUnit.MILLISECONDS).build(orgCacheLoader);
    }*/
    public static  final String ALL = "ALL";
    private static final Logger LOGGER = LoggerFactory.getLogger(OrgCacheLoader.class);
    @Autowired
    private AssignOrgService assignOrgService;
    /**
     * 构造方法
     */
    @Autowired
    public OrgCacheLoader(){
    }
    @Override
    public Optional<List<AssignOrgInfo>> load(final String key) throws Exception {
        if (StringUtils.equals(key, ALL)) {
            return Optional.fromNullable(find());
        } else {
            final AssignOrgInfo org = find(key);
            if (org != null) {
                return Optional.of(Lists.newArrayList(org));
            } else {
                return Optional.of(Collections.emptyList());
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<AssignOrgInfo> find() {
        List<AssignOrgInfo> orgs = null;
        List<AssignOrgInfo> groups = Lists.newArrayList();
        try {
              orgs = assignOrgService.findAllOrg();
              final List<AssignOrgInfo> orgList = orgs;
              orgs.forEach(org -> {
                 if (org !=null && "999999".equals(org.getOrgCode())) {
                     org.setLabel(org.getOrgName()+"["+org.getOrgCode()+"]");
                     grouping(org, orgList);
                     groups.add(org);
                 }
              });
        } catch (final Exception e) {
           LOGGER.error(e.getMessage());
        }
        return orgs;
    }

    private void grouping(final AssignOrgInfo parent, final List<AssignOrgInfo> orgs) {
        orgs.forEach(org -> {
            org.setLabel(org.getOrgName()+"["+org.getOrgCode()+"]");
            final String parentCode = parent.getOrgCode();
            if (StringUtils.equals(org.getParentOrgCode(), parentCode)) {
                parent.getChildren().add(org);
                grouping(org, orgs);
            }
        });
    }

    private AssignOrgInfo find(final String orgCode) {
        AssignOrgInfo org = null;
        try {
           org = assignOrgService.findAssignOrgByOrgCode(orgCode);
        } catch (final Exception e) {
            LOGGER.error(e.getMessage());
        }
        return org;
    }
}
