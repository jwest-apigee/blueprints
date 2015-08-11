package com.tinkerpop.blueprints.impls.usergrid;


import com.tinkerpop.blueprints.*;
import com.tinkerpop.blueprints.impls.GraphTest;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by ayeshadastagiri on 7/13/15.
 */
public class UsergridGraphTest extends GraphTest {


    @Override
    public Graph generateGraph() {
        return generateGraph("/var/lib/jenkins/workspace/usergrid-blueprints/blueprints-usergrid-graph/src/main/resources/usergrid.properties");
    }

    @Override
    public Graph generateGraph(String filepath) {
        Graph usergridgraph = GraphFactory.open(filepath);
        return usergridgraph;
    }


    public void testVertexTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new VertexTestSuite(this));
        printTestPerformance("VertexTestSuite", this.stopWatch());
    }

    public void testEdgeTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new EdgeTestSuite(this));
        printTestPerformance("EdgeTestSuite", this.stopWatch());
    }

    public void testGraphTestSuite() throws Exception {
        this.stopWatch();
        doTestSuite(new GraphTestSuite(this));
        printTestPerformance("GraphTestSuite", this.stopWatch());
    }


    @Override
    public void doTestSuite(TestSuite testSuite) throws Exception {
        for (Method method : testSuite.getClass().getDeclaredMethods()) {
            if (method.getName().startsWith("test")) {
                System.out.println("Testing " + method.getName() + "...");
                Graph graph = this.generateGraph();
                try {
                    method.invoke(testSuite);
                }
                catch (InvocationTargetException e ){
                    System.out.println("InvocationTargetException exception : "+ e);
                }
                catch (Exception e)
                {
                    System.out.println("other exception : " + e);
                }
                System.out.println("exectuted tests for : " + method.getName());
                //graph.shutdown();
            }
        }
    }

}
