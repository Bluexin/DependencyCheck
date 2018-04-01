/*
 * This file is part of dependency-check-core.
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
 *
 * Copyright (c) 2015 Institute for Defense Analyses. All Rights Reserved.
 */
package org.owasp.dependencycheck.analyzer;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.owasp.dependencycheck.BaseTest;
import org.owasp.dependencycheck.analyzer.exception.AnalysisException;
import org.owasp.dependencycheck.dependency.Dependency;

import java.io.File;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.*;
import org.owasp.dependencycheck.Engine;
import org.owasp.dependencycheck.dependency.EvidenceType;

/**
 * Unit tests for NodePackageAnalyzer.
 *
 * @author Dale Visser
 */
public class NodePackageAnalyzerTest extends BaseTest {

    /**
     * The analyzer to test.
     */
    private NodePackageAnalyzer analyzer;
    /**
     * A reference to the engine.
     */
    private Engine engine;

    /**
     * Correctly setup the analyzer for testing.
     *
     * @throws Exception thrown if there is a problem
     */
    @Before
    @Override
    public void setUp() throws Exception {
        super.setUp();
        engine = new Engine(this.getSettings());
        analyzer = new NodePackageAnalyzer();
        analyzer.setFilesMatched(true);
        analyzer.initialize(getSettings());
        analyzer.prepare(engine);
    }

    /**
     * Cleanup temp files, close resources, etc.
     *
     * @throws Exception thrown if there is a problem
     */
    @After
    @Override
    public void tearDown() throws Exception {
        analyzer.close();
        engine.close();
        super.tearDown();
    }

    /**
     * Test of getName method, of class PythonDistributionAnalyzer.
     */
    @Test
    public void testGetName() {
        assertThat(analyzer.getName(), is("Node.js Package Analyzer"));
    }

    /**
     * Test of supportsExtension method, of class PythonDistributionAnalyzer.
     */
    @Test
    public void testSupportsFiles() {
        assertThat(analyzer.accept(new File("package-lock.json")), is(true));
        assertThat(analyzer.accept(new File("npm-shrinkwrap.json")), is(true));
    }

    /**
     * Test of inspect method, of class PythonDistributionAnalyzer.
     *
     * @throws AnalysisException is thrown when an exception occurs.
     */
    @Test
    public void testAnalyzeShrinkwrapJson() throws AnalysisException {
        final Dependency toScan = new Dependency(BaseTest.getResourceAsFile(this,
                "nodejs/npm-shrinkwrap.json"));
        analyzer.analyze(toScan, engine);
        assertEquals("Expected 1 dependency", engine.getDependencies().length, 1);
        final Dependency result = engine.getDependencies()[0];
        final String vendorString = result.getEvidence(EvidenceType.VENDOR).toString();
        assertThat(vendorString, containsString("Sanjeev Koranga"));
        assertThat(vendorString, containsString("dns-sync"));
        assertThat(result.getEvidence(EvidenceType.PRODUCT).toString(), containsString("dns-sync"));
        assertThat(result.getEvidence(EvidenceType.VERSION).toString(), containsString("0.1.0"));
        assertEquals(NodePackageAnalyzer.DEPENDENCY_ECOSYSTEM, result.getEcosystem());
        assertEquals("dns-sync", result.getName());
        assertEquals("0.1.0", result.getVersion());
    }

    /**
     * Test of inspect method, of class PythonDistributionAnalyzer.
     *
     * @throws AnalysisException is thrown when an exception occurs.
     */
    @Test
    public void testAnalyzePackageJsonWithShrinkwrap() throws AnalysisException {
        final Dependency packageLock = new Dependency(BaseTest.getResourceAsFile(this,
                "nodejs/package-lock.json"));
        final Dependency shrinkwrap = new Dependency(BaseTest.getResourceAsFile(this,
                "nodejs/npm-shrinkwrap.json"));
        engine.addDependency(packageLock);
        engine.addDependency(shrinkwrap);
        assertEquals(2, engine.getDependencies().length);
        analyzer.analyze(packageLock, engine);
        assertEquals(1, engine.getDependencies().length); //package-lock was removed without analysis
        analyzer.analyze(shrinkwrap, engine);
        assertEquals(1, engine.getDependencies().length); //shrinkwrap was removed with analysis adding 1 dependency
    }
}