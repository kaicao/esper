/*
 ***************************************************************************************
 *  Copyright (C) 2006 EsperTech, Inc. All rights reserved.                            *
 *  http://www.espertech.com/esper                                                     *
 *  http://www.espertech.com                                                           *
 *  ---------------------------------------------------------------------------------- *
 *  The software in this package is published under the terms of the GPL license       *
 *  a copy of which has been included with this distribution in the license.txt file.  *
 ***************************************************************************************
 */
package com.espertech.esper.common.internal.epl.lookupplansubord;

import com.espertech.esper.common.client.type.EPTypeClass;
import com.espertech.esper.common.internal.epl.join.lookup.IndexMultiKey;
import com.espertech.esper.common.internal.epl.join.queryplan.QueryPlanIndexItem;

public class SubordinateQueryIndexDesc {
    public final static EPTypeClass EPTYPE = new EPTypeClass(SubordinateQueryIndexDesc.class);
    public final static EPTypeClass EPTYPEARRAY = new EPTypeClass(SubordinateQueryIndexDesc[].class);

    private final IndexKeyInfo optionalIndexKeyInfo;
    private final String indexName;
    private final IndexMultiKey indexMultiKey;
    private final QueryPlanIndexItem queryPlanIndexItem;

    public SubordinateQueryIndexDesc(IndexKeyInfo optionalIndexKeyInfo, String indexName, IndexMultiKey indexMultiKey, QueryPlanIndexItem queryPlanIndexItem) {
        this.optionalIndexKeyInfo = optionalIndexKeyInfo;
        this.indexName = indexName;
        this.indexMultiKey = indexMultiKey;
        this.queryPlanIndexItem = queryPlanIndexItem;
    }

    public IndexKeyInfo getOptionalIndexKeyInfo() {
        return optionalIndexKeyInfo;
    }

    public String getIndexName() {
        return indexName;
    }

    public IndexMultiKey getIndexMultiKey() {
        return indexMultiKey;
    }

    public QueryPlanIndexItem getQueryPlanIndexItem() {
        return queryPlanIndexItem;
    }
}
