package com.tinkerpop.blueprints.impls.usergrid;

import com.tinkerpop.blueprints.*;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.usergrid.drivers.blueprints.UsergridGraph;

import java.lang.reflect.Method;

/**
 * Created by ayeshadastagiri on 7/16/15.
 */
public class UsergridGraphSpecificTestSuite extends TestSuite {
    private String graphProperties = "blueprints-usergrid-graph/src/main/resources/usergrid.properties";
    private Graph graph;
    private String defaultType;

    @Override
    protected void setUp() throws Exception {
        System.out.println("Setting it up!");
        graph = getGraph();
    }

    public Graph getGraph() {
        return generateGraph(graphProperties);
    }

    public Graph generateGraph(String s) {
        PropertiesConfiguration conf = null;
        try {
            conf = new PropertiesConfiguration(s);
            defaultType = conf.getString("usergrid.defaultType");
        } catch (ConfigurationException e) {
            e.printStackTrace();
        }
        UsergridGraph graph = new UsergridGraph(conf);
        return graph;
    }


    public void testBasicUgAddVertex() {
        //Case1 : Vertex with valid id
        Vertex v = graph.addVertex("person/person1");
        assertEquals("person/blperson1", v.getId().toString());

        //Case2 : Adding vertex with default type
        v = graph.addVertex("blperson2");
        assertEquals(defaultType + "/person2", v.getId().toString());

        //Case3 : Changing the type of the vertex
        //TODO : once set type is implemented will uncomment this code.
        //v.setType("person");
        //assertEquals("person/person2", v.getId().toString());

    }

    public void testBasicUgSetVertexProperties() {
        //Case1 : Vertex with valid id and property
        Vertex v = graph.addVertex("person/person3");
        v.setProperty("age",20);
        assertEquals(20, v.getProperty("age"));

        //setting multiple properties - float and string
        v.setProperty("weight", 97.5);
        assertEquals(97.5, v.getProperty("weight"));

        v.setProperty("city","SanJose");
        assertEquals("SanJose", v.getProperty("city"));

        //setting null property
        v.setProperty("zipcode",null);
        assertEquals(null, v.getProperty("zipcode"));

    }

    public void testBasicUgAddEdge() {
        //Case1 : Edge with valid id
        Vertex v1 = graph.addVertex("person/person4");
        Vertex v2 = graph.addVertex("restaurants/Amici");

        Edge e1 = graph.addEdge(null, v1, v2, "likes");
        assertEquals(v1.getId()+"/likes/"+v2.getId(),e1.getId().toString());

        //Case2 : Edge with id -- should not throw errors.
        Edge e2 = graph.addEdge("id1", v1, v2, "likes");
        assertEquals(v1.getId()+"/likes/"+v2.getId(),e2.getId().toString());

        //Adding edge from a vertex.
        Edge e3 = v1.addEdge("visits",v2);
        assertEquals(v1.getId()+"/visits/"+v2.getId(),e3.getId().toString());

        Edge e4 = graph.addEdge(null, v1,v2,"suggests-1?");
        assertEquals(v1.getId()+"/suggests-1?/"+v2.getId(),e4.getId().toString());
    }

    public void testUgEdge() {
        Vertex v1 = graph.addVertex("person/person5");
        Vertex v2 = graph.addVertex("restaurants/CheeseCakeFactory");

        Edge e4 = graph.addEdge(null, v1,v2,"likes");
        assertEquals(v1.getId()+"/likes/"+v2.getId(),e4.getId().toString());
        assertEquals("person/person5",e4.getVertex(Direction.IN).getId());
        assertEquals("restaurants/CheeseCakeFactory",e4.getVertex(Direction.OUT).getId());
        assertEquals("likes",e4.getLabel());

    }


    @Override
    protected void tearDown() throws Exception {
        System.out.println("Running: tearDown");
        graph.shutdown();
        graph = null;
        assertNull(graph);
    }


}
