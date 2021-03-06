/*
 * Copyright 2016-present Open Networking Laboratory
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
package org.onosproject.yangutils.translator.tojava.javamodel;

import org.onosproject.yangutils.datamodel.YangLeaf;
import org.onosproject.yangutils.translator.tojava.JavaQualifiedTypeInfo;
import org.onosproject.yangutils.translator.tojava.utils.YangToJavaNamingConflictUtil;

import static org.onosproject.yangutils.translator.tojava.JavaQualifiedTypeInfo.updateLeavesJavaQualifiedInfo;
import static org.onosproject.yangutils.translator.tojava.utils.JavaIdentifierSyntax.getCamelCase;

/**
 * Represents java information corresponding to the YANG leaf.
 */
public class YangJavaLeaf
        extends YangLeaf
        implements JavaLeafInfoContainer {

    private JavaQualifiedTypeInfo javaQualifiedAccess;

    /**
     * Returns a new YANG leaf object with java qualified access details.
     */
    public YangJavaLeaf() {
        super();
        setJavaQualifiedInfo(new JavaQualifiedTypeInfo());
    }

    @Override
    public JavaQualifiedTypeInfo getJavaQualifiedInfo() {
        return javaQualifiedAccess;
    }

    @Override
    public void setJavaQualifiedInfo(JavaQualifiedTypeInfo typeInfo) {
        javaQualifiedAccess = typeInfo;

    }

    public String getJavaName(YangToJavaNamingConflictUtil conflictResolveConfig) {
        return getCamelCase(getName(), conflictResolveConfig);
    }

    @Override
    public boolean isLeafList() {
        return false;
    }

    @Override
    public void updateJavaQualifiedInfo() {
        updateLeavesJavaQualifiedInfo(this);
    }
}
